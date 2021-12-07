/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.test.tools.validate;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.spine.test.tools.validate.command.CreateProject;
import io.spine.test.tools.validate.entity.Project;
import io.spine.test.tools.validate.entity.Task;
import io.spine.test.tools.validate.event.ProjectCreated;
import io.spine.test.tools.validate.rejection.TestRejections;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.MessageWithConstraints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static java.util.stream.Collectors.toList;

@DisplayName("`(required)` constraint should be compiled so that")
class RequiredConstraintTest {

    @Test
    @DisplayName("a number field can have any value")
    void ignoreNumbers() {
        var singulars = Singulars.newBuilder().buildPartial();
        checkNoViolation(singulars, "required_numbers_are_not_validated");
    }

    @Nested
    @DisplayName("a `string` field")
    class StringField {

        private static final String FIELD = "not_empty_string";

        @Test
        @DisplayName("cannot be empty")
        void empty() {
            var singulars = Singulars.newBuilder().buildPartial();
            checkViolation(singulars, FIELD);
        }

        @Test
        @DisplayName("must have a non-empty value")
        void acceptNonEmptyString() {
            var singulars = Singulars.newBuilder()
                    .setNotEmptyString(" ")
                    .buildPartial();
            checkNoViolation(singulars, FIELD);
        }
    }

    @Nested
    @DisplayName("a `bytes` field")
    class BytesField {

        private static final String FIELD = "one_or_more_bytes";

        @Test
        @DisplayName("cannot be empty")
        void empty() {
            var singulars = Singulars.newBuilder().buildPartial();
            checkViolation(singulars, FIELD);
        }

        @Test
        @DisplayName("must have bytes, allowing all zeros")
        void nonEmpty() {
            var nonZeros = Singulars.newBuilder()
                    .setOneOrMoreBytes(ByteString.copyFrom("non-empty", UTF_8))
                    .buildPartial();
            checkNoViolation(nonZeros, FIELD);

            byte[] zeros = {0};
            var withZeroes = Singulars.newBuilder()
                    .setOneOrMoreBytes(ByteString.copyFrom(zeros))
                    .buildPartial();
            checkNoViolation(withZeroes, FIELD);
        }
    }

    @Nested
    @DisplayName("an enum field")
    class EnumField {

        private static final String FIELD = "not_vegetable";

        @Test
        @DisplayName("cannot have a zero-index enum item value")
        void zeroValue() {
            var singulars = Singulars.newBuilder()
                    .setNotVegetable(UltimateChoice.VEGETABLE)
                    .buildPartial();
            checkViolation(singulars, FIELD);
        }

        @Test
        @DisplayName("must have a non-zero index item value")
        void acceptNonDefaultEnum() {
            var singulars = Singulars.newBuilder()
                    .setNotVegetable(UltimateChoice.CHICKEN)
                    .buildPartial();
            checkNoViolation(singulars, FIELD);
        }
    }

    @Nested
    @DisplayName("a message field")
    class MessageField {

        protected static final String FIELD = "not_default";

        @Test
        @DisplayName("cannot have a default message value")
        void defaultValue() {
            var singulars = Singulars
                    .newBuilder()
                    .buildPartial();
            checkViolation(singulars, FIELD);
        }

        @Test
        @DisplayName("must have a not-default message value")
        void nonDefaultMessage() {
            var singulars = Singulars.newBuilder()
                    .setNotDefault(Enclosed.newBuilder()
                                           .setValue(newUuid()))
                    .buildPartial();
            checkNoViolation(singulars, FIELD);
        }

        @Test
        @DisplayName("cannot be of type `google.protobuf.Empty`")
        void notAllowEmptyRequired() {
            final var fieldName = "impossible";

            var unset = AlwaysInvalid.newBuilder().build();
            checkViolation(unset, fieldName);

            var set = AlwaysInvalid.newBuilder()
                    .setImpossible(Empty.getDefaultInstance())
                    .build();
            checkViolation(set, fieldName);
        }
    }

    @Test
    @DisplayName("all violations on a single message are collected")
    void collectManyViolations() {
        var instance = Singulars.getDefaultInstance();
        List<ConstraintViolation> violations = instance.validate();
        assertThat(violations).hasSize(4);
    }

    @Nested
    @DisplayName("a repeated number field")
    class RepeatedNumberField {

        protected static final String FIELD = "not_empty_list_of_longs";

        @Test
        @DisplayName("cannot be empty")
        void emptyRepeatedInt() {
            var instance = Collections.getDefaultInstance();
            checkViolation(instance, FIELD);
        }

        @Test
        @DisplayName("can have any items, including zero")
        void repeatedInt() {
            var instance = Collections.newBuilder()
                    .addNotEmptyListOfLongs(0L)
                    .buildPartial();
            checkNoViolation(instance, FIELD);
        }
    }

    @Nested
    @DisplayName("a map field with number values")
    class MapNumberField {

        private static final String FIELD = "not_empty_map_of_ints";

        @Test
        @DisplayName("cannot be empty")
        void empty() {
            var instance = Collections.getDefaultInstance();
            checkViolation(instance, FIELD);
        }

        @Test
        @DisplayName("can have entries with any values, including zero")
        void mapOfInts() {
            var instance = Collections.newBuilder()
                    .putNotEmptyMapOfInts(0, 0)
                    .buildPartial();
            checkNoViolation(instance, FIELD);
        }
    }

    @Nested
    @DisplayName("a map field with string values")
    class MapStringField {

        private static final String FIELD = "contains_a_non_empty_string_value";

        @Test
        @DisplayName("cannot be empty")
        void empty() {
            var instance = Collections.getDefaultInstance();
            checkViolation(instance, FIELD);
        }

        @Test
        @DisplayName("cannot have a single empty value entry")
        void nonEmptyValue() {
            var empty = Collections.newBuilder()
                    .putContainsANonEmptyStringValue("", "")
                    .buildPartial();
            checkViolation(empty, FIELD);

            var nonEmpty = Collections.newBuilder()
                    .putContainsANonEmptyStringValue("", "")
                    .putContainsANonEmptyStringValue("foo", "bar")
                    .buildPartial();
            checkNoViolation(nonEmpty, FIELD);
        }

        @Test
        @DisplayName("must have at least one non-empty entry")
        void mapOfStrings() {
            var instance = Collections.newBuilder()
                    .putContainsANonEmptyStringValue("", " ")
                    .buildPartial();
            checkNoViolation(instance, FIELD);
        }
    }

    @Nested
    @DisplayName("a repeated enum field")
    class RepeatedEnumField {

        private static final String FIELD = "at_least_one_piece_of_meat";

        @Test
        @DisplayName("cannot be empty")
        void emptyRepeatedEnum() {
            var instance = Collections.getDefaultInstance();
            checkViolation(instance, FIELD);
        }

        @Test
        @DisplayName("cannot have all items with zero-index enum item value")
        void repeatedDefaultEnum() {
            var allZero = Collections.newBuilder()
                    .addAtLeastOnePieceOfMeat(UltimateChoice.VEGETABLE)
                    .addAtLeastOnePieceOfMeat(UltimateChoice.VEGETABLE)
                    .buildPartial();
            checkViolation(allZero, FIELD);
        }

        @Test
        @DisplayName("must have at least one value with non-zero emum item value")
        void repeatedEnum() {
            var instance = Collections.newBuilder()
                    .addAtLeastOnePieceOfMeat(UltimateChoice.FISH)
                    .addAtLeastOnePieceOfMeat(UltimateChoice.VEGETABLE)
                    .buildPartial();
            checkNoViolation(instance, FIELD);
        }
    }

    @Nested
    @DisplayName("the first field in a message which is")
    class FirstFieldCheck {

        @Nested
        @DisplayName("a command")
        class InCommand {

            @Test
            @DisplayName("cannot be empty")
            void notSet() {
                var msg = CreateProject.newBuilder().buildPartial();
                checkViolation(msg, "id");
            }

            @Test
            @DisplayName("must have a non-empty value")
            void set() {
                var msg = CreateProject.newBuilder()
                        .setId(newUuid())
                        .build();
                assertValid(msg);
            }
        }

        @Nested
        @DisplayName("an event")
        class InEvent {

            @Test
            @DisplayName("can be empty")
            void notSet() {
                var msg = ProjectCreated.newBuilder().build();
                assertValid(msg);
            }
        }

        @Nested
        @DisplayName("a rejection")
        class InRejection {

            @Test
            @DisplayName("can be empty")
            void notSet() {
                var msg = TestRejections.CannotCreateProject.newBuilder().build();
                assertValid(msg);
            }
        }

        @Nested
        @DisplayName("an entity state")
        class InEntityState {

            @Test
            @DisplayName("cannot be empty")
            void notSet() {
                var msg = Project.newBuilder().buildPartial();
                checkViolation(msg, "id");
            }

            @Test
            @DisplayName("must have a non-empty value")
            void set() {
                var msg = Project.newBuilder()
                        .setId(newUuid())
                        .build();
                assertValid(msg);
            }

            @Test
            @DisplayName("allowing to omit, if set as not `required` explicitly")
            void notRequired() {
                var msg = Task.newBuilder().build();
                assertValid(msg);
            }
        }

        private void assertValid(MessageWithConstraints msg) {
            assertThat(msg.validate()).isEmpty();
        }
    }

    private static void checkViolation(MessageWithConstraints message, String field) {
        checkViolation(message, field, "must be set");
    }

    private static void checkViolation(MessageWithConstraints message,
                                       String field,
                                       String errorMessagePart) {
        List<ConstraintViolation> violations = message.validate();
        var stringViolations = violationAtField(violations, field);
        assertThat(stringViolations).hasSize(1);
        var violation = stringViolations.get(0);
        assertThat(violation.getMsgFormat()).contains(errorMessagePart);
    }

    private static void checkNoViolation(MessageWithConstraints message, String field) {
        List<ConstraintViolation> violations = message.validate();
        var stringViolations = violationAtField(violations, field);
        assertThat(stringViolations).isEmpty();
    }

    private static List<ConstraintViolation>
    violationAtField(List<ConstraintViolation> violations, String fieldName) {
        return violations
                .stream()
                .filter(violation -> violation.getFieldPath()
                                              .getFieldName(0)
                                              .equals(fieldName))
                .collect(toList());
    }
}
