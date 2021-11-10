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
package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import groovy.lang.Closure;
import io.spine.tools.code.Indent;
import io.spine.tools.java.fs.DefaultJavaPaths;
import io.spine.tools.mc.gradle.ModelCompilerOptions;
import io.spine.tools.mc.java.codegen.JavaCodegenConfig;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.tools.gradle.Projects.getDefaultMainDescriptors;
import static io.spine.tools.gradle.Projects.getDefaultTestDescriptors;
import static io.spine.tools.mc.gradle.ModelCompilerOptionsKt.getModelCompiler;

/**
 * A configuration for the Spine Model Compiler for Java.
 */
@SuppressWarnings({
        "PublicField", "WeakerAccess" /* Expose fields as a Gradle extension */,
        "ClassWithTooManyMethods" /* The methods are needed for handing default values. */,
        "ClassWithTooManyFields", "PMD.TooManyFields" /* Gradle extensions are flat like this. */,
        "RedundantSuppression" /* "ClassWithTooManyFields" is sometimes not recognized by IDEA. */
})
public class McJavaOptions {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * The name of the extension, as it appears in a Gradle build script.
     */
    static final String NAME = "java";

    /**
     * The absolute path to the Protobuf source code under the {@code main} directory.
     */
    public String mainProtoDir;

    /**
     * The absolute path to the test Protobuf source directory.
     */
    public String testProtoDir;

    /**
     * The absolute path to the main Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedMainDir;

    /**
     * The absolute path to the main {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedMainGrpcDir;

    /**
     * The absolute path to the main target generated resources directory.
     */
    public String generatedMainResourcesDir;

    /**
     * The absolute path to the test Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedTestDir;

    /**
     * The absolute path to the test target generated resources directory.
     */
    public String generatedTestResourcesDir;

    /**
     * The absolute path to the test {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedTestGrpcDir;

    /**
     * The absolute path to the main target generated rejections root directory.
     */
    public String generatedMainRejectionsDir;

    /**
     * The absolute path to the test target generated rejections root directory.
     */
    public String generatedTestRejectionsDir;

    /**
     * The indent for the generated code in the validating builders.
     */
    public Indent indent = Indent.of4();

    /**
     * The absolute paths to directories to delete.
     *
     * <p>Either this property OR {@code dirToClean} property is used.
     */
    public List<String> dirsToClean = new ArrayList<>();

    public final CodeGenAnnotations generateAnnotations = new CodeGenAnnotations();

    /**
     * Code generation configuration.
     *
     * @see #codegen(Action)
     */
    public JavaCodegenConfig codegen;

    public List<String> internalClassPatterns = new ArrayList<>();

    public List<String> internalMethodNames = new ArrayList<>();

    private Project project;

    public static File descriptorSetFileOf(Project project, boolean main) {
        File result = main
                      ? getDefaultMainDescriptors(project)
                      : getDefaultTestDescriptors(project);
        return result;
    }

    /**
     * Injects the dependency to the given project.
     */
    public void injectProject(Project project) {
        this.project = checkNotNull(project);
        this.codegen = new JavaCodegenConfig(project);
    }

    /**
     * Obtains the extension name of the plugin.
     */
    public static String name() {
        return NAME;
    }

    /**
     * Configures the Model Compilation code generation by applying the given action.
     */
    public void codegen(Action<JavaCodegenConfig> action) {
        action.execute(codegen);
    }

    static DefaultJavaPaths def(Project project) {
        return DefaultJavaPaths.at(project.getProjectDir());
    }

    @SuppressWarnings({
            "PMD.MethodNamingConventions",
            "FloggerSplitLogStatement" // See: https://github.com/SpineEventEngine/base/issues/612
    })
    private static FluentLogger.Api _debug() {
        return logger.atFine();
    }

    public static String getMainProtoDir(Project project) {
        McJavaOptions extension = getMcJavaOptions(project);
        _debug().log("Extension is `%s`.", extension);
        String protoDir = extension.mainProtoDir;
        _debug().log("`modelCompiler.mainProtoSrcDir` is `%s`.", protoDir);
        return pathOrDefault(protoDir,
                             def(project).src()
                                         .mainProto());
    }

    public static String getTestProtoDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).testProtoDir,
                             def(project).src()
                                         .testProto());
    }

    public static String getGeneratedMainJavaDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedMainDir,
                             def(project).generated()
                                         .mainJava());
    }

    public static String getGeneratedMainGrpcDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedMainGrpcDir,
                             def(project).generated()
                                         .mainGrpc());
    }

    public static String getGeneratedMainResourcesDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedMainResourcesDir,
                             def(project).generated()
                                         .mainResources());
    }

    public static String getGeneratedTestJavaDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedTestDir,
                             def(project).generated()
                                         .testJava());
    }

    public static String getGeneratedTestResourcesDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedTestResourcesDir,
                             def(project).generated()
                                         .testResources());
    }

    public static String getGeneratedTestGrpcDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedTestGrpcDir,
                             def(project).generated()
                                         .testGrpc());
    }

    public static String getGeneratedMainRejectionsDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedMainRejectionsDir,
                             def(project).generated()
                                         .mainSpine());
    }

    public static String getGeneratedTestRejectionsDir(Project project) {
        return pathOrDefault(getMcJavaOptions(project).generatedTestRejectionsDir,
                             def(project).generated()
                                         .testSpine());
    }

    private static String pathOrDefault(String path, Object defaultValue) {
        return isNullOrEmpty(path)
               ? defaultValue.toString()
               : path;
    }

    public static Indent getIndent(Project project) {
        Indent result = getMcJavaOptions(project).indent;
        _debug().log("The current indent is %d.", result.size());
        return result;
    }

    @SuppressWarnings("unused")
    public void setIndent(int indent) {
        this.indent = Indent.of(indent);
        _debug().log("Indent has been set to %d.", indent);
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Closure<?> closure) {
        project.configure(generateAnnotations, closure);
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Action<? super CodeGenAnnotations> action) {
        action.execute(generateAnnotations);
    }

    public static CodeGenAnnotations getCodeGenAnnotations(Project project) {
        CodeGenAnnotations annotations = getMcJavaOptions(project).generateAnnotations;
        return annotations;
    }

    public static ImmutableSet<String> getInternalClassPatterns(Project project) {
        List<String> patterns = getMcJavaOptions(project).internalClassPatterns;
        return ImmutableSet.copyOf(patterns);
    }

    public static ImmutableSet<String> getInternalMethodNames(Project project) {
        List<String> patterns = getMcJavaOptions(project).internalMethodNames;
        return ImmutableSet.copyOf(patterns);
    }

    /**
     * Obtains the instance of the extension from the given project.
     */
    public static McJavaOptions getMcJavaOptions(Project project) {
        ModelCompilerOptions mcOptions = getModelCompiler(project);
        ExtensionAware extensionAware = (ExtensionAware) mcOptions;
        McJavaOptions mcJavaExtension =
                extensionAware.getExtensions()
                              .getByType(McJavaOptions.class);
        return mcJavaExtension;
    }
}
