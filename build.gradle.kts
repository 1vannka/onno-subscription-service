plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "onno-subscription-service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val onnoVersion = "2.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")

	implementation("su.onno:onno-framework-starter:$onnoVersion")
	implementation("su.onno:onno-ui-starter:$onnoVersion")
	implementation("su.onno:onno-auth-starter:$onnoVersion")

	runtimeOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}