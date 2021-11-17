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

package io.spine.tools.mc.java.gradle.plugins;

import com.google.common.base.Charsets;
import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.GenerateProtoTask.PluginOptions;
import io.spine.code.proto.DescriptorReference;
import io.spine.tools.gradle.ProtocConfigurationPlugin;
import io.spine.tools.gradle.SourceSetName;
import io.spine.tools.gradle.task.GradleTask;
import io.spine.tools.gradle.task.TaskName;
import io.spine.tools.mc.java.gradle.McJavaOptions;
import io.spine.tools.protoc.SpineProtocConfig;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static io.spine.io.Ensure.ensureFile;
import static io.spine.tools.gradle.ProtocPluginName.grpc;
import static io.spine.tools.gradle.ProtocPluginName.spineProtoc;
import static io.spine.tools.gradle.task.BaseTaskName.clean;
import static io.spine.tools.gradle.task.JavaTaskName.processResources;
import static io.spine.tools.gradle.task.Tasks.getSourceSetName;
import static io.spine.tools.mc.java.gradle.Artifacts.SPINE_PROTOC_PLUGIN_NAME;
import static io.spine.tools.mc.java.gradle.Artifacts.gRpcProtocPlugin;
import static io.spine.tools.mc.java.gradle.Artifacts.spineProtocPlugin;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.writeDescriptorReference;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.writePluginConfiguration;
import static io.spine.tools.mc.java.gradle.Projects.getMcJava;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A Gradle plugin that performs additional {@code protoc} configurations relevant
 * for Java projects.
 */
public final class JavaProtocConfigurationPlugin extends ProtocConfigurationPlugin {

    @Override
    protected void
    configureProtocPlugins(NamedDomainObjectContainer<ExecutableLocator> plugins, Project project) {
        plugins.create(grpc.name(),
                       locator -> locator.setArtifact(gRpcProtocPlugin().notation())
        );
        plugins.create(spineProtoc.name(),
                       locator -> locator.setArtifact(spineProtocPlugin().notation())
        );
    }

    @Override
    protected void customizeTask(GenerateProtoTask protocTask) {
        Helper helper = new Helper(protocTask);
        helper.configure();
    }

    /**
     * A method object configuring an instance of {@code GenerateProtoTask}.
     *
     * @see #customizeTask(GenerateProtoTask)
     */
    private static class Helper {

        private final Project project;
        private final GenerateProtoTask protocTask;
        private final SourceSetName sourceSetName;
        private final File descriptorFile;

        private Helper(GenerateProtoTask task) {
            this.project = task.getProject();
            this.protocTask = task;
            this.sourceSetName = getSourceSetName(protocTask);
            this.descriptorFile = new File(protocTask.getDescriptorPath());
        }

        private void configure() {
            customizeDescriptorSetGeneration();
            addTaskDependency();
            addPlugins();
        }

        private void customizeDescriptorSetGeneration() {
            setResourceDirectory();
            GradleTask writeRef = GradleTask.newBuilder(
                            writeDescriptorReference(sourceSetName), task -> writeRefFile())
                    .insertBeforeTask(processResources(sourceSetName))
                    .applyNowTo(project);
            protocTask.finalizedBy(writeRef.getTask());
        }

        private void setResourceDirectory() {
            Path resourceDirectory =
                    descriptorFile.toPath()
                                  .getParent();
            protocTask.getSourceSet()
                      .getResources()
                      .srcDir(resourceDirectory);
        }

        private void addTaskDependency() {
            Task writeConfig = writePluginConfigTask();
            protocTask.dependsOn(writeConfig);
        }

        private void addPlugins() {
            NamedDomainObjectContainer<PluginOptions> plugins = protocTask.getPlugins();
            plugins.create(grpc.name());
            plugins.create(spineProtoc.name(),
                            options -> {
                                options.setOutputSubDir("java");
                                String filePath = spineProtocConfigFile().toString();
                                String encodedPath = base64Encoded(filePath);
                                options.option(encodedPath);
                            });
        }

        private Path spineProtocConfigFile() {
            String prefix = sourceSetName.toPrefix();
            String fileName =  prefix.isEmpty()
                    ? "config.pb"
                    : prefix + "-config.pb";
            Path configFile = pluginTempDir().resolve(fileName);
            return configFile;
        }

        private Path pluginTempDir() {
            File buildDir = project.getBuildDir();
            Path result = Paths.get(buildDir.getAbsolutePath(), "tmp", SPINE_PROTOC_PLUGIN_NAME);
            return result;
        }

        /**
         * Creates a new {@code writePluginConfiguration} task
         * that is expected to run after the {@code clean} task.
         */
        private Task writePluginConfigTask() {
            TaskName taskName = writePluginConfiguration(sourceSetName);
            return GradleTask.newBuilder(taskName, task -> writePluginConfig())
                    .allowNoDependencies()
                    .applyNowTo(project)
                    .getTask()
                    .mustRunAfter(clean.name());
        }

        private void writePluginConfig() {
            Path configFile = spineProtocConfigFile();
            McJavaOptions options = getMcJava(project);
            SpineProtocConfig config = options.codegen.toProto();

            ensureFile(configFile);
            try (FileOutputStream fos = new FileOutputStream(configFile.toFile())) {
                config.writeTo(fos);
            } catch (FileNotFoundException e) {
                throw errorOn("create", e, configFile);
            } catch (IOException e) {
                throw errorOn("store", e, configFile);
            }
        }

        private void writeRefFile() {
            Path resourceDirectory = descriptorFile.toPath().getParent();
            DescriptorReference reference = DescriptorReference.toOneFile(descriptorFile);
            reference.writeTo(resourceDirectory);
        }

        private static
        IllegalStateException errorOn(String action, IOException cause, Path configFile) {
            return newIllegalStateException(
                    cause,
                    "Unable to %s Spine Protoc Plugin configuration file at: `%s`.",
                    action,
                    configFile);
        }

        private static String base64Encoded(String value) {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] valueBytes = value.getBytes(Charsets.UTF_8);
            String result = encoder.encodeToString(valueBytes);
            return result;
        }
    }
}
