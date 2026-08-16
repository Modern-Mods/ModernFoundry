# Unreleased
* Added GitHub Actions release automation for Modern Foundry.
    * Builds Modern Foundry on every push and publishes only new `mod_version` releases.
* Limited release creation to new `mod_version` values.
    * Repeated pushes continue to build without creating duplicate releases for the same version.
* Fixed Linux GitHub Actions wrapper permissions for the ModernFoundry build.
* Updated the ModernFoundry workflow to fetch the required Hilt release JAR without rebuilding Hilt.
