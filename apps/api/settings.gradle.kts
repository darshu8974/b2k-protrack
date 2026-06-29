plugins {
	// Lets Gradle auto-provision the Java 21 toolchain when it is not already installed.
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "api"
