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

val baseVersion by extra("2.0.0-SNAPSHOT.95")
val timeVersion by extra("2.0.0-SNAPSHOT.92")
val toolBaseVersion by extra("2.0.0-SNAPSHOT.93")
val mcVersion by extra("2.0.0-SNAPSHOT.89")
val serverVersion by extra("2.0.0-SNAPSHOT.100")

/**
 * Version of `SpineEventEngine/validation` modules.
 *
 * Keep in mind, the Validation library is tightly connected to ProtoData. For the version
 * of ProtoData, see `buildSrc/src/main/kotlin/io/spine/internal/dependency/Spine.kt`.
 */
val validationVersion by extra("2.0.0-SNAPSHOT.22")
val protoDataVersion by extra("0.2.7")

val mcJavaVersion by extra("2.0.0-SNAPSHOT.97")
val versionToPublish by extra(mcJavaVersion)
