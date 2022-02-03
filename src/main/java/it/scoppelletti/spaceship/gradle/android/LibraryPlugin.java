/*
 * Copyright (C) 2019-2020 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

import java.net.URI;
import java.util.Objects;
import javax.annotation.Nonnull;
import com.android.build.gradle.LibraryExtension;
import it.scoppelletti.spaceship.gradle.ProjectTools;
import it.scoppelletti.spaceship.gradle.SpaceshipPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Plug-in for Android libraries.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LibraryPlugin implements Plugin<Project> {
    private static final String BUILDTYPE_RELEASE = "release";
    private static final Logger myLogger = Logging.getLogger(
            LibraryPlugin.class);

    @Override
    public void apply(@Nonnull Project project) {
        URI devRepoUrl;
        ProjectTools projectTools;
        LibraryExtension androidExt;

        if (project.getPlugins().hasPlugin(SpaceshipPlugin.class)) {
            myLogger.info("Plugin {} already applied.", SpaceshipPlugin.class);
        } else {
            myLogger.info("Applying plugin {}.", SpaceshipPlugin.class);
            project.getPluginManager().apply(SpaceshipPlugin.class);
        }

        projectTools = new ProjectTools(project);
        devRepoUrl = projectTools.applyMavenPublish();

        androidExt = Objects.requireNonNull(
                project.getExtensions().findByType(LibraryExtension.class),
                () -> String.format("Extension %1$s not found.",
                        LibraryExtension.class));

        androidExt.getLibraryVariants().all(variant -> {
            AndroidLibraryTools tools;
            AndroidLibraryTaskNames taskNames;

            if (variant.getName().toLowerCase().contains(
                    LibraryPlugin.BUILDTYPE_RELEASE)) {
                taskNames = new AndroidLibraryTaskNames(variant);
                tools = new AndroidLibraryTools(project, variant, taskNames);

                tools.generateMetainf();
                tools.packageSources();

                if (projectTools.isKDocEnabled()) {
                    tools.generateKDoc();
                    tools.packageKDoc();
                }

                if (devRepoUrl != null) {
                    tools.publish();
                }
            }
        });

        project.afterEvaluate(prj -> {
            // - AGP 7.0.0
            // The new class
            // com.android.build.api.variant.LibraryAndroidComponentsExtension
            // does not implement the method getPackagingOptions.

            // - AGP 7.1.0
            // It is too late to modify excludes
            // It has already been read to configure this project.
            // Consider either moving this call to be during evaluation,
            // or using the variant API.
            // http://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.PackagingOptions.html
//            Set<String> excludes;
//            excludes = androidExt.getPackagingOptions().getExcludes();
//            excludes.remove("/META-INF/LICENSE.txt");
//            excludes.remove("/META-INF/NOTICE.txt");
//            androidExt.getPackagingOptions().setExcludes(excludes);

            if (devRepoUrl != null) {
                projectTools.definePublishingRepo(devRepoUrl,
                        AndroidLibraryTaskNames.ASSEMBLE);
            }
        });
    }
}
