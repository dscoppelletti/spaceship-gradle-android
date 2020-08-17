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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

/**
 * Extension object for the plug-in {@code AppPlugin}.
 *
 * @see   it.scoppelletti.spaceship.gradle.android.AppPlugin
 * @since 1.0.0
 */
public class SpaceshipAppExtension {

    /**
     * Name of this extension object.
     */
    public static final String NAME = "spaceshipApp";

    /**
     * Credits configuration.
     */
    @Getter
    @Nonnull
    private final CreditsConfig credits;

    /**
     * Constructor.
     */
    @Inject
    public SpaceshipAppExtension(@Nonnull Project project,
            @Nonnull ObjectFactory objectFactory) {
        credits = objectFactory.newInstance(CreditsConfig.class, project);
    }

    /**
     * Configures credits.
     *
     * @param action Configurator.
     */
    public void credits(@Nonnull Action<CreditsConfig> action) {
        action.execute(credits);
    }
}
