# Releasing

Releases are automated from Git tags. The published Maven version is taken **only from the tag** (the `v` prefix is stripped). When `releaseVersion` is not passed, `gradle.properties` `version` is used for local builds.

## Tag format

- Stable: `vMAJOR.MINOR.PATCH` (example: `v0.2.0`)
- Pre-release: `vMAJOR.MINOR.PATCH-suffix` (example: `v1.0.0-rc1`)
- Tags containing `SNAPSHOT` are rejected.

## What the workflow does

On matching tag push, [`.github/workflows/release.yml`](.github/workflows/release.yml):

1. Runs the full `./gradlew check` suite with `-PreleaseVersion=…` aligned to the tag.
2. Signs the `maven` publication and runs `publishToSonatype` + `closeAndReleaseSonatypeStagingRepository` against **Sonatype Central** staging (see `build.gradle.kts`).
3. Creates a **GitHub Release** for the tag and attaches the main JAR, sources JAR, and Javadoc JAR.

## Manual dry run (optional)

Without Central credentials you can still verify packaging:

```bash
./gradlew check publishMavenPublicationToMavenLocal -PreleaseVersion=1.2.3
```
