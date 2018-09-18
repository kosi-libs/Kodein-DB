import org.kodein.internal.gradle.KodeinVersions

plugins {
    id("kodein-android")
}

dependencies {
//    expectedBy(project(":ldb:k-ldb:k-ldb-tests-common"))

    compile(project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-android"))

    testImplementation(project(":test:test-utils-jvm"))
    androidTestImplementation("com.android.support:support-annotations:27.1.1")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-test:${KodeinVersions.kotlin}")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-test-junit:${KodeinVersions.kotlin}")
    androidTestImplementation("junit:junit:4.12")
    androidTestImplementation("org.kodein.log:kodein-log-frontend-print-jvm:1.0.0")

}

android {
    defaultConfig {
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
}
