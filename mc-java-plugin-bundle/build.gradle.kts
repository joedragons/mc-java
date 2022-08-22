/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

plugins {
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

dependencies {
    implementation(project(":mc-java"))
    implementation(project(":mc-java-protoc"))
}

application {
    mainClass.set("io.spine.tools.mc.java.protoc.Plugin")
}

publishing {
    val pGroup = project.group.toString()
    val pName = project.name.toString()
    val pVersion = project.version.toString()

    publications {
        create("fat-jar", MavenPublication::class) {
            //TODO:2022-08-22:alex.tymchenko: prefix!
            //TODO:2022-08-22:alex.tymchenko: remove the "small" JAR.
            groupId = pGroup
            artifactId = pName
            version = pVersion

            artifact(tasks.shadowJar) {
                // Avoid `-all` suffix in the published artifact.
                // We cannot remove the suffix by setting the `archiveClassifier` for
                // the `shadowJar` task because of the duplication check for pairs
                // (classifier, artifact extension) performed by `ValidatingMavenPublisher` class.
                classifier = ""
            }
        }
    }
}

tasks.publish {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    setZip64(true)
    setClassifier("")
    mergeServiceFiles("desc.ref")
}

// See https://github.com/johnrengelman/shadow/issues/153.
tasks.shadowDistTar.get().enabled = false
tasks.shadowDistZip.get().enabled = false
