val junitVersion = "5.10.2"


plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
    `java-library`
    jacoco
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.19"
}

val projectName = "kommand"
val githubUsername = "manami-project"

repositories {
    mavenCentral()
}

group = "io.github.manamiproject"
version = project.findProperty("release.version") as String? ?: ""

dependencies {
    api(kotlin("stdlib"))

    implementation(platform(kotlin("bom")))

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

kotlin {
    explicitApi()
    jvmToolchain(JavaVersion.VERSION_21.toString().toInt())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.required.set(false)
    reports.junitXml.required.set(true)
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javaDoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = projectName
            url = uri("https://maven.pkg.github.com/$githubUsername/$projectName")
            credentials {
                username = parameter("GH_USERNAME", githubUsername)
                password = parameter("GH_PACKAGES_RELEASE_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = projectName
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javaDoc.get())

            pom {
                packaging = "jar"
                name.set(projectName)
                description.set("This library allows you to easily create kotlin-style wrappers for command line functions.")
                url.set("https://github.com/$githubUsername/$projectName")

                licenses {
                    license {
                        name.set("AGPL-V3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:$githubUsername/$projectName.git")
                    developerConnection.set("scm:git:ssh://github.com:$githubUsername/$projectName.git")
                    url.set("https://github.com/$githubUsername/$projectName")
                }
            }
        }
    }
}

coverallsJacoco {
    reportPath = "${layout.buildDirectory}/reports/jacoco/test/jacocoFullReport.xml"
}

tasks.jacocoTestReport {
    reports {
        html.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory}/reports/jacoco/test/jacocoFullReport.xml"))
    }
    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}

fun parameter(name: String, default: String = ""): String {
    val env = System.getenv(name) ?: ""
    if (env.isNotBlank()) {
        return env
    }

    val property = project.findProperty(name) as String? ?: ""
    if (property.isNotEmpty()) {
        return property
    }

    return default
}