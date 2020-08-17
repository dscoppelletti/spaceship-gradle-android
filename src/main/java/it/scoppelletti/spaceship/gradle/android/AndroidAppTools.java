/*
 * Copyright (C) 2020 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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
import it.scoppelletti.spaceship.gradle.model.SpaceshipExtension;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.TaskProvider;

/**
 * Tools.
 */
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
    void generateCredits() {
        String outputName, templName;
        File assetsDir, outFile;
        URI databaseUrl;
        TaskProvider<CreditsTask> creditsTask;

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
                .resolve(myVariant.getDirName())
                .resolve("assets")
                .resolve(outputName).toFile();

        creditsTask = myProject.getTasks().register(
                myTaskNames.getGenerateCreditsName(),
                CreditsTask.class, task -> {
            task.setDescription(myTaskNames.getGenerateCreditsDescription());
            task.setGroup(BasePlugin.BUILD_GROUP);
            task.setVariantName(myVariant.getName());
            task.setDatabaseUrl(databaseUrl);
            task.setTemplateName(templName);
            task.setOutputFile(outFile);
        });

        myVariant.getPreBuildProvider().configure(task ->
                task.dependsOn(creditsTask));

        assetsDir = myProject.getBuildDir().toPath()
                .resolve("intermediates")
                .resolve("merged_assets")
                .resolve(myVariant.getDirName())
                .resolve("out").toFile();

        // No way to add any genetated assets folder to the task MergeAssets,
        // so copy manually into output directory of the task.
        myVariant.getMergeAssetsProvider().configure(task ->
                task.doLast(dummy -> {
                    if (!outFile.exists()) {
                        return;
                    }

                    if (!assetsDir.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        assetsDir.mkdirs();
                    }

                    myProject.copy(spec -> {
                        spec.from(outFile);
                        spec.into(assetsDir);
                    });
                }));
    }
}
