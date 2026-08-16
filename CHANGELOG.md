# Unreleased
* Added GitHub Actions release automation.
    * Builds Hilt before Modern Foundry and publishes the resulting JAR as a latest-marked GitHub release.
* Limited release creation to new `mod_version` values.
    * Repeated pushes continue to build without creating duplicate releases for the same version.
* Fixed Linux GitHub Actions wrapper permissions for the Hilt and ModernFoundry builds.
