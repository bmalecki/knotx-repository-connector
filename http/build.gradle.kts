/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.tools.ant.filters.ReplaceTokens
import org.nosphere.apache.rat.RatTask

plugins {
  id("java-library")
  id("maven-publish")
  id("signing")
  id("org.nosphere.apache.rat") version "0.4.0"
}

group = "io.knotx"

// -----------------------------------------------------------------------------
// Dependencies
// -----------------------------------------------------------------------------

apply(from = "../gradle/common.deps.gradle.kts")
apply(from = "../gradle/codegen.deps.gradle.kts")
dependencies {
  api("io.knotx:knotx-fragment-api")
  api("io.knotx:knotx-server-http-api")
  api(group = "com.google.guava", name = "guava")
  api(group = "commons-io", name = "commons-io")
  api(group = "org.apache.commons", name = "commons-lang3")
  api(group = "com.typesafe", name = "config")
  api(group = "commons-collections", name = "commons-collections")
}

// -----------------------------------------------------------------------------
// Source sets
// -----------------------------------------------------------------------------

apply(from = "../gradle/common.gradle.kts")
sourceSets.named("main") {
  java.srcDir("src/main/generated")
}

// -----------------------------------------------------------------------------
// Tasks
// -----------------------------------------------------------------------------


tasks {
  named<RatTask>("rat") {
    excludes.addAll("**/*.json", "**/*.MD", "**/*.templ", "**/*.adoc", "**/build/*", "**/out/*", "**/generated/*", "/src/test/resources/*", "*.iml")
  }
  getByName("build").dependsOn("rat")

  named<Test>("test") {
    useJUnitPlatform()
    testLogging { showStandardStreams = true }
    testLogging { showExceptions = true }
    failFast = true
  }
}

// -----------------------------------------------------------------------------
// Publication
// -----------------------------------------------------------------------------
tasks.register<Jar>("sourcesJar") {
  from(sourceSets.named("main").get().allJava)
  classifier = "sources"
}

tasks.register<Jar>("javadocJar") {
  from(tasks.named<Javadoc>("javadoc"))
  classifier = "javadoc"
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = "knotx-repository-connector-http"
      from(components["java"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["javadocJar"])
      pom {
        name.set("Knot.x Core Repository Connector FS")
        description.set("Repository Connector FS - enables fetching templates from filesystem repositories")
        url.set("http://knotx.io")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
              id.set("marcinczeczko")
              name.set("Marcin Czeczko")
              email.set("https://github.com/marcinczeczko")
          }
          developer {
              id.set("skejven")
              name.set("Maciej Laskowski")
              email.set("https://github.com/Skejven")
          }
          developer {
              id.set("tomaszmichalak")
              name.set("Tomasz Michalak")
              email.set("https://github.com/tomaszmichalak")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/Knotx/knotx-repository-connector.git")
          developerConnection.set("scm:git:ssh://github.com:Knotx/knotx-repository-connector.git")
          url.set("http://knotx.io")
        }
      }
    }
    repositories {
      maven {
        val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
        url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        credentials {
          username = if (project.hasProperty("ossrhUsername")) project.property("ossrhUsername")?.toString() else "UNKNOWN"
          password = if (project.hasProperty("ossrhPassword")) project.property("ossrhPassword")?.toString() else "UNKNOWN"
          println("Connecting with user: ${username}")
        }
      }
    }
  }
}

signing {
  sign(publishing.publications["mavenJava"])
}

tasks.named<Javadoc>("javadoc") {
  if (JavaVersion.current().isJava9Compatible) {
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
  }
}