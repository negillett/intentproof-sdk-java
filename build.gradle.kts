import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.component.AdhocComponentWithVariants

plugins {
  `java-library`
  `java-test-fixtures`
  `maven-publish`
  signing
  jacoco
  alias(libs.plugins.nexuspublish)
  alias(libs.plugins.errorprone)
  alias(libs.plugins.spotless)
}

description =
    "IntentProof Java SDK: structured ExecutionEvent emission for verification and ingest."

/**
 * Gradle signing accepts only an 8-hex OpenPGP key id (`00B5050F` or `0x00B5050F`). Secrets often
 * hold a 16-char long id, a 40-char fingerprint, or `rsa4096/HEX` from `gpg -K` — normalize to the
 * low 32 bits (last 8 hex digits).
 */
fun normalizePgpKeyIdForGradle(raw: String): String {
  var s = raw.trim().substringAfterLast('/', raw.trim()).trim()
  if (s.startsWith("0x", ignoreCase = true)) {
    s = s.substring(2).trim()
  }
  s = s.replace(":", "").replace(Regex("\\s"), "")
  require(s.isNotEmpty()) { "signingKeyId is blank" }
  require(s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
    "signingKeyId must be hex (set SIGNING_GPG_KEY_ID to the 8-char id, 16-char long id, 40-char fingerprint, or the part after / from gpg --list-secret-keys --keyid-format long)"
  }
  require(s.length >= 8) { "signingKeyId must contain at least 8 hex digits (got length ${s.length})" }
  return s.takeLast(8).uppercase()
}

val releaseVersionProperty = providers.gradleProperty("releaseVersion")
if (releaseVersionProperty.isPresent) {
  version = releaseVersionProperty.get()
}

repositories { mavenCentral() }

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
  withJavadocJar()
  withSourcesJar()
}

// Do not attach test-fixture variants to the published `java` component. They are for in-repo
// tests only; leaving them on often breaks :signMavenPublication (lazy providers) and adds POM
// warnings. Gradle consumers still resolve the main library + module metadata as usual.
pluginManager.withPlugin("java-test-fixtures") {
  val javaComponent = components.named("java").get() as AdhocComponentWithVariants
  javaComponent.withVariantsFromConfiguration(configurations.getByName("testFixturesApiElements")) {
    skip()
  }
  javaComponent.withVariantsFromConfiguration(
      configurations.getByName("testFixturesRuntimeElements")) {
    skip()
  }
  configurations.findByName("testFixturesSourcesElements")?.let { cfg ->
    javaComponent.withVariantsFromConfiguration(cfg) { skip() }
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-parameters")
  options.compilerArgs.addAll(
      listOf(
          "-Xlint:all",
          "-Xlint:-processing",
      ),
  )
  if (name != "compileJava") {
    // Test-only Throwable/Deque subclasses trigger serialVersionUID noise without adding value.
    options.compilerArgs.add("-Xlint:-serial")
  }
  options.errorprone {
    // Error Prone on production sources only; tests/fixtures use patterns EP flags heavily.
    enabled.set(name == "compileJava")
    // Doclint requires @return tags; one-line summaries would duplicate those lines.
    disable("MissingSummary")
  }
}

tasks.withType<Javadoc>().configureEach {
  options.encoding = "UTF-8"
  (options as org.gradle.external.javadoc.StandardJavadocDocletOptions).links(
      "https://docs.oracle.com/en/java/javase/21/docs/api/",
      "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/${libs.versions.jackson.get()}/",
  )
}

dependencies {
  api(libs.jackson.databind)
  errorprone(libs.errorprone.core)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockito.core)
  testImplementation(testFixtures(project))
  testRuntimeOnly(libs.junit.platform.launcher)
}

jacoco {
  toolVersion = libs.versions.jacoco.get()
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
}

tasks.jacocoTestCoverageVerification {
  dependsOn(tasks.test)
  violationRules {
    rule {
      limit {
        counter = "LINE"
        value = "COVEREDRATIO"
        minimum = "1.0".toBigDecimal()
      }
    }
    rule {
      limit {
        counter = "INSTRUCTION"
        value = "COVEREDRATIO"
        minimum = "1.0".toBigDecimal()
      }
    }
  }
}

tasks.check {
  dependsOn(tasks.javadoc)
  dependsOn(tasks.jacocoTestCoverageVerification)
}

spotless {
  java {
    target("src/**/*.java")
    googleJavaFormat(libs.versions.googleJavaFormat.get())
  }
  kotlinGradle { target("*.gradle.kts") }
}

tasks.jar {
  manifest {
    attributes(
        mapOf(
            "Implementation-Title" to "IntentProof Java SDK",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "IntentProof",
        )
    )
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      groupId = project.group.toString()
      artifactId = "intentproof-sdk"
      version = project.version.toString()
      pom {
        name.set("IntentProof Java SDK")
        description.set(project.description)
        url.set("https://github.com/IntentProof/intentproof-sdk-java")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/IntentProof/intentproof-sdk-java.git")
          developerConnection.set("scm:git:ssh://git@github.com/IntentProof/intentproof-sdk-java.git")
          url.set("https://github.com/IntentProof/intentproof-sdk-java")
        }
        developers {
          developer {
            id.set("intentproof")
            name.set("IntentProof")
            email.set(
                (findProperty("developerContactEmail") as String?)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "intentproof@users.noreply.github.com")
            organization.set("IntentProof")
            organizationUrl.set("https://github.com/IntentProof")
            url.set("https://github.com/IntentProof/intentproof-sdk-java")
          }
        }
        issueManagement {
          system.set("GitHub")
          url.set("https://github.com/IntentProof/intentproof-sdk-java/issues")
        }
      }
    }
  }
}

tasks.register<Exec>("intentproofSpecConformance") {
  group = "verification"
  description = "Run canonical IntentProof specification (`intentproof-spec`) Vitest oracle (Node.js + npm on PATH)"
  commandLine("bash", rootProject.file("scripts/spec-conformance.sh").absolutePath)
}

// Sonatype Central (replaces legacy OSSRH). Credentials: project properties
// sonatypeUsername / sonatypePassword (e.g. ORG_GRADLE_PROJECT_sonatypeUsername in CI).
// https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
    }
  }
}

signing {
  val signingKeyRaw = (findProperty("signingKey") as String?)?.takeIf { it.isNotBlank() }
  if (signingKeyRaw != null) {
    // CI secrets sometimes carry CRLF; armored ASCII must use Unix newlines for reliable decoding.
    val signingKey = signingKeyRaw.replace("\r\n", "\n").trim()
    val signingPassword = (findProperty("signingPassword") as String?)?.trim() ?: ""
    val keyId =
        (findProperty("signingKeyId") as String?)?.trim()?.takeIf { it.isNotEmpty() }
    // 3-arg useInMemoryPgpKeys picks the PGPSecretKey matching keyId. If that id is a stub subkey or
    // does not match decryptable material, extractPrivateKey can yield null → NPE in BC during sign.
    // 2-arg uses the ring's primary secret (typical single-key OSS setup). Opt in via property or CI.
    val usePrimarySecret =
        (findProperty("signingUsePrimarySecret") as String?)?.equals("true", ignoreCase = true) == true
    if (keyId != null && !usePrimarySecret) {
      useInMemoryPgpKeys(normalizePgpKeyIdForGradle(keyId), signingKey, signingPassword)
    } else {
      useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications["maven"])
  }
}
