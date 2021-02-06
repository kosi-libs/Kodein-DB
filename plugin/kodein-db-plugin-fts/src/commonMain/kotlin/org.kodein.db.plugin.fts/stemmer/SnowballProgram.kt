package org.kodein.db.plugin.fts.stemmer

import org.kodein.db.plugin.fts.Stemmer


internal abstract class SnowballProgram protected constructor(): Stemmer {

    abstract fun stem(): Boolean

    override fun stemOf(word: String): String {
        setCurrent(word)
        return if (stem()) getCurrent()
        else word
    }

    /**
     * Set the current string.
     */
    fun setCurrent(value: String) {
        current.setRange(0, current.length, value)
        cursor = 0
        limit = current.length
        limit_backward = 0
        bra = cursor
        ket = limit
    }

    /**
     * Get the current string.
     */
    fun getCurrent(): String {
        val result = current.toString()
        // Make a new StringBuffer.  If we reuse the old one, and a user of
        // the library keeps a reference to the buffer returned (for example,
        // by converting it to a String in a way which doesn't force a copy),
        // the buffer size will not decrease, and we will risk wasting a large
        // amount of memory.
        // Thanks to Wolfram Esser for spotting this problem.
        current = StringBuilder()
        return result
    }

    // current string
    protected var current: StringBuilder
	protected var cursor = 0
	protected var limit = 0
	protected var limit_backward = 0
	protected var bra = 0
	protected var ket = 0

    protected fun in_grouping(s: CharArray, min: Int, max: Int): Boolean {
        if (cursor >= limit) return false
        var ch = current[cursor]
        if (ch.toInt() > max || ch.toInt() < min) return false
        ch = (ch - min.toChar()).toChar()
        if (s[ch.toInt() shr 3].toInt() and (0X1 shl (ch.toInt() and 0X7)) == 0) return false
        cursor++
        return true
    }

    protected fun in_grouping_b(s: CharArray, min: Int, max: Int): Boolean {
        if (cursor <= limit_backward) return false
        var ch = current[cursor - 1]
        if (ch.toInt() > max || ch.toInt() < min) return false
        ch = (ch - min.toChar()).toChar()
        if (s[ch.toInt() shr 3].toInt() and (0X1 shl (ch.toInt() and 0X7)) == 0) return false
        cursor--
        return true
    }

    protected fun out_grouping(s: CharArray, min: Int, max: Int): Boolean {
        if (cursor >= limit) return false
        var ch = current[cursor]
        if (ch.toInt() > max || ch.toInt() < min) {
            cursor++
            return true
        }
        ch = (ch - min.toChar()).toChar()
        if (s[ch.toInt() shr 3].toInt() and (0X1 shl (ch.toInt() and 0X7)) == 0) {
            cursor++
            return true
        }
        return false
    }

    protected fun out_grouping_b(s: CharArray, min: Int, max: Int): Boolean {
        if (cursor <= limit_backward) return false
        var ch = current[cursor - 1]
        if (ch.toInt() > max || ch.toInt() < min) {
            cursor--
            return true
        }
        ch = (ch - min.toChar()).toChar()
        if (s[ch.toInt() shr 3].toInt() and (0X1 shl (ch.toInt() and 0X7)) == 0) {
            cursor--
            return true
        }
        return false
    }

    protected fun in_range(min: Int, max: Int): Boolean {
        if (cursor >= limit) return false
        val ch = current[cursor]
        if (ch.toInt() > max || ch.toInt() < min) return false
        cursor++
        return true
    }

    protected fun in_range_b(min: Int, max: Int): Boolean {
        if (cursor <= limit_backward) return false
        val ch = current[cursor - 1]
        if (ch.toInt() > max || ch.toInt() < min) return false
        cursor--
        return true
    }

    protected fun out_range(min: Int, max: Int): Boolean {
        if (cursor >= limit) return false
        val ch = current[cursor]
        if (!(ch.toInt() > max || ch.toInt() < min)) return false
        cursor++
        return true
    }

    protected fun out_range_b(min: Int, max: Int): Boolean {
        if (cursor <= limit_backward) return false
        val ch = current[cursor - 1]
        if (!(ch.toInt() > max || ch.toInt() < min)) return false
        cursor--
        return true
    }

    protected fun eq_s(s_size: Int, s: String): Boolean {
        if (limit - cursor < s_size) return false
        var i: Int
        i = 0
        while (i != s_size) {
            if (current[cursor + i] != s[i]) return false
            i++
        }
        cursor += s_size
        return true
    }

    protected fun eq_s_b(s_size: Int, s: String): Boolean {
        if (cursor - limit_backward < s_size) return false
        var i: Int
        i = 0
        while (i != s_size) {
            if (current[cursor - s_size + i] != s[i]) return false
            i++
        }
        cursor -= s_size
        return true
    }

    protected fun eq_v(s: CharSequence): Boolean {
        return eq_s(s.length, s.toString())
    }

    protected fun eq_v_b(s: CharSequence): Boolean {
        return eq_s_b(s.length, s.toString())
    }

    protected fun find_among(v: Array<Among>, v_size: Int): Int {
        var i = 0
        var j = v_size
        val c = cursor
        val l = limit
        var common_i = 0
        var common_j = 0
        var first_key_inspected = false
        while (true) {
            val k = i + (j - i shr 1)
            var diff = 0
            var common = if (common_i < common_j) common_i else common_j // smaller
            val w = v[k]
            var i2: Int
            i2 = common
            while (i2 < w.s_size) {
                if (c + common == l) {
                    diff = -1
                    break
                }
                diff = current[c + common] - w.s[i2]
                if (diff != 0) break
                common++
                i2++
            }
            if (diff < 0) {
                j = k
                common_j = common
            } else {
                i = k
                common_i = common
            }
            if (j - i <= 1) {
                if (i > 0) break // v->s has been inspected
                if (j == i) break // only one item in v

                // - but now we need to go round once more to get
                // v->s inspected. This looks messy, but is actually
                // the optimal approach.
                if (first_key_inspected) break
                first_key_inspected = true
            }
        }
        while (true) {
            val w = v[i]
            if (common_i >= w.s_size) {
                cursor = c + w.s_size
                if (w.process == null) return w.result
                var res: Boolean

                res = w.process.invoke(this)
                cursor = c + w.s_size
                if (res) return w.result
            }
            i = w.substring_i
            if (i < 0) return 0
        }
    }

    // find_among_b is for backwards processing. Same comments apply
    protected fun find_among_b(v: Array<Among>, v_size: Int): Int {
        var i = 0
        var j = v_size
        val c = cursor
        val lb = limit_backward
        var common_i = 0
        var common_j = 0
        var first_key_inspected = false
        while (true) {
            val k = i + (j - i shr 1)
            var diff = 0
            var common = if (common_i < common_j) common_i else common_j
            val w = v[k]
            var i2: Int
            i2 = w.s_size - 1 - common
            while (i2 >= 0) {
                if (c - common == lb) {
                    diff = -1
                    break
                }
                diff = current[c - 1 - common] - w.s[i2]
                if (diff != 0) break
                common++
                i2--
            }
            if (diff < 0) {
                j = k
                common_j = common
            } else {
                i = k
                common_i = common
            }
            if (j - i <= 1) {
                if (i > 0) break
                if (j == i) break
                if (first_key_inspected) break
                first_key_inspected = true
            }
        }
        while (true) {
            val w = v[i]
            if (common_i >= w.s_size) {
                cursor = c - w.s_size
                if (w.process == null) return w.result
                var res: Boolean
                res = w.process.invoke(this)
                cursor = c - w.s_size
                if (res) return w.result
            }
            i = w.substring_i
            if (i < 0) return 0
        }
    }

    /* to replace chars between c_bra and c_ket in current by the
     * chars in s.
     */
    protected fun replace_s(c_bra: Int, c_ket: Int, s: String): Int {
        val adjustment = s.length - (c_ket - c_bra)
        current.setRange(c_bra, c_ket, s)
        limit += adjustment
        if (cursor >= c_ket) cursor += adjustment else if (cursor > c_bra) cursor = c_bra
        return adjustment
    }

    protected fun slice_check() {
        if (bra < 0 || bra > ket || ket > limit || limit > current.length) // this line could be removed
        {
            error("faulty slice operation")
        }
    }

    protected fun slice_from(s: String) {
        slice_check()
        replace_s(bra, ket, s)
    }

    protected fun slice_from(s: CharSequence) {
        slice_from(s.toString())
    }

    protected fun slice_del() {
        slice_from("")
    }

    protected fun insert(c_bra: Int, c_ket: Int, s: String) {
        val adjustment = replace_s(c_bra, c_ket, s)
        if (c_bra <= bra) bra += adjustment
        if (c_bra <= ket) ket += adjustment
    }

    protected fun insert(c_bra: Int, c_ket: Int, s: CharSequence) {
        insert(c_bra, c_ket, s.toString())
    }

    /* Copy the slice into the supplied StringBuilder */
    protected fun slice_to(s: StringBuilder): StringBuilder {
        slice_check()
        s.setRange(0, s.length, current.substring(bra, ket))
        return s
    }

    protected fun assign_to(s: StringBuilder): StringBuilder {
        s.setRange(0, s.length, current.substring(0, limit))
        return s
    }

    init {
        current = StringBuilder()
        setCurrent("")
    }
}