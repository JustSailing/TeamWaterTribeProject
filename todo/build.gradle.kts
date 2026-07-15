plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.watertribe"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.hibernate.orm:hibernate-community-dialects")
	implementation("org.xerial:sqlite-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-crypto")
	implementation("org.xerial:sqlite-jdbc:3.45.1.0")
	implementation("org.hibernate.orm:hibernate-community-dialects:7.1.8.Final")
	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.h2database:h2")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	testImplementation("com.h2database:h2:2.4.240")
	// the core cucumber code
    testImplementation("io.cucumber:cucumber-java:7.34.4")
	// the integration code for cucumber & junit
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.34.4")
	// the sub module that gives us access to the junit test suite feature
    testImplementation("org.junit.platform:junit-platform-suite:1.14.1")
	// This gives access to the Selenium ecosystem	// This lets us inject our cucumber test resources into our step classes
	testImplementation("io.cucumber:cucumber-spring:7.34.4")
	// Selenium for browser automation E2E tests
	testImplementation("org.seleniumhq.selenium:selenium-java:4.20.0")
	testImplementation("io.rest-assured:rest-assured:6.0.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
