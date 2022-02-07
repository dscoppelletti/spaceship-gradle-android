/*
 * Copyright (C) 2020-2021 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

package it.scoppelletti.spaceship.gradle.android;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import javax.annotation.Nonnull;
import com.android.build.gradle.api.ApplicationVariant;
import it.scoppelletti.spaceship.gradle.android.model.SpaceshipAppExtension;
import it.scoppelletti.spaceship.gradle.android.tasks.CreditsTask;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.TaskProvider;

/**
 * Tools.
 */
@SuppressWarnings("deprecation")
final class AndroidAppTools {

    private final Project myProject;
    private final SpaceshipAppExtension myAppExt;
    private final ApplicationVariant myVariant;
    private final AndroidAppTaskNames myTaskNames;

    /**
     * Constructor.
     *
     * @param project   Project.
     * @param variant   Variant.
     * @param taskNames Provides name and description of the tasks.
     */
    AndroidAppTools(@Nonnull Project project,
            @Nonnull ApplicationVariant variant,
            @Nonnull AndroidAppTaskNames taskNames) {
        myProject = Objects.requireNonNull(project,
                "Argument project is null.");
        myVariant = Objects.requireNonNull(variant,
                "Argument variant is null.");
        myTaskNames = Objects.requireNonNull(taskNames,
                "Argument taskNames is null.");

        myAppExt = Objects.requireNonNull(myProject.getExtensions()
                .findByType(SpaceshipAppExtension.class), () ->
                String.format("Extension %1$s not found.",
                        SpaceshipAppExtension.class));
    }

    /**
     * Defines the task {@code CreditsTask}.
     */
    @SuppressWarnings("Convert2Lambda")
    void generateCredits() {
        String outputName, templName;
        File assetsDir, outFile;
        URI databaseUrl;
        TaskProvider<Task> assetsTask;

        databaseUrl = myAppExt.getCredits().getDatabaseUrl();
        templName = myAppExt.getCredits().getTemplateName();
        outputName = myAppExt.getCredits().getOutputName();
        if (databaseUrl == null || StringUtils.isBlank(outputName) ||
                StringUtils.isBlank(templName)) {
            return;
        }

        outFile = myProject.getBuildDir().toPath()
                .resolve("generated")
                .resolve("it_scoppelletti_credits")
                .resolve(myVariant.getName())
                .resolve("assets")
                .resolve(outputName).toFile();

        myProject.getTasks().register(
                myTaskNames.getGenerateCreditsName(),
                CreditsTask.class, task -> {
                    task.setDescription(
                            myTaskNames.getGenerateCreditsDescription());
                    task.setGroup(BasePlugin.BUILD_GROUP);
                    task.setVariantName(myVariant.getName());
                    task.setDatabaseUrl(databaseUrl);
                    task.setTemplateName(templName);
                    task.setOutputFile(outFile);
                });

        assetsTask = myProject.getTasks().named(
                myTaskNames.getMergeAssetsName());

        // - AGP 7.0.0
        // If I scan the variant through by the method onVariants of the new
        // class ApplicationAndroidComponentsExtension, I get the following
        // error:
        //
        // A problem occurred configuring project ':app'.
        // > Task with name 'mergeDebugAssets' not found in project ':app'.
        //
        // But I can see the task mergeDebugAssets in the Gradle view of Android
        // Studio.
        
        assetsTask.configure(task ->
                task.dependsOn(myTaskNames.getGenerateCreditsName()));

        assetsDir = myProject.getBuildDir().toPath()
                .resolve("intermediates")
                .resolve("assets")
                .resolve(myVariant.getName())
                .resolve("merge"
                        .concat(StringUtils.capitalize(myVariant.getName()))
                        .concat("Assets")).toFile();

        assetsTask.configure(task ->

                // http://docs.gradle.org/7.2/userguide/validation_problems.html
                //  #implementation_unknown
                // Cannot replace Action by lambda
                task.doLast(new Action<Task>() {

                    @Override
                    public void execute(@Nonnull Task task) {
                        mergeAssets(task, outFile, assetsDir);
                    }
                }));
    }

    /**
     * No way to add any genetated assets folder to the task MergeAssets, so
     * copy manually into output directory of the task.
     */
    private void mergeAssets(Task task, File outFile, File assetsDir) {
        if (!outFile.exists()) {
            return;
        }

        if (!assetsDir.exists()) {
            task.getLogger().warn("Folder {} not exists.",
                    assetsDir);
            return;
        }

        myProject.copy(spec -> {
            spec.from(outFile);
            spec.into(assetsDir);
        });
    }
}
