object Configuration {
    const val compileSdk = 34
    const val targetSdk = 34
    const val minSdk = 23
    private const val majorVersion = 1
    private const val minorVersion = 23
    private const val patchVersion = 0
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val versionCode = 27
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "com.matrix.autoreply"
}