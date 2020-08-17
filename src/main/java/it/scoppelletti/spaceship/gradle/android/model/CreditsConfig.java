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

package it.scoppelletti.spaceship.gradle.android.model;

import java.net.URI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import it.scoppelletti.spaceship.gradle.ProjectTools;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;

/**
 * Credits configuration.
 *
 * @since 1.0.0
 */
public class CreditsConfig {

    /**
     * Database URL.
     */
    @Getter
    @Setter
    @Nullable
    private URI databaseUrl;

    /**
     * Template name.
     */
    @Getter
    @Setter
    @Nonnull
    private String templateName =
            "it/scoppelletti/spaceship/gradle/android/tasks/credits.html";

    /**
     * Output credit file name;
     */
    @Getter
    @Setter
    @Nonnull
    private String outputName = "credits.html";

    @Inject
    public CreditsConfig(@Nonnull Project project) {
        String name;
        ProjectTools tools;

        tools = new ProjectTools(project);
        databaseUrl = tools.getCreditDatabaseUrl();

        name = tools.getCreditTemplateName();
        if (StringUtils.isNotBlank(name)) {
            templateName = name;
        }
    }
}
