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

import java.util.Objects;
import javax.annotation.Nonnull;
import com.android.build.gradle.AppExtension;
import it.scoppelletti.spaceship.gradle.SpaceshipPlugin;
import it.scoppelletti.spaceship.gradle.android.model.SpaceshipAppExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Plug-in for Android Apps.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class AppPlugin implements Plugin<Project> {
    private static final Logger myLogger = Logging.getLogger(AppPlugin.class);

    @Override
    public void apply(@Nonnull Project project) {
        AppExtension androidExt;

        if (project.getPlugins().hasPlugin(SpaceshipPlugin.class)) {
            myLogger.info("Plugin {} already applied.", SpaceshipPlugin.class);
        } else {
            myLogger.info("Applying plugin {}.", SpaceshipPlugin.class);
            project.getPluginManager().apply(SpaceshipPlugin.class);
        }

        androidExt = Objects.requireNonNull(
                project.getExtensions().findByType(AppExtension.class),
                () -> String.format("Extension %1$s not found.",
                        AppExtension.class));

        project.getExtensions().create(SpaceshipAppExtension.NAME,
                SpaceshipAppExtension.class, project);

        androidExt.getApplicationVariants().all(variant -> {
            AndroidAppTools tools;
            AndroidAppTaskNames taskNames;

            taskNames = new AndroidAppTaskNames(variant);
            tools = new AndroidAppTools(project, variant, taskNames);

            tools.generateCredits();
        });
    }
}
