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
import com.android.build.gradle.api.ApplicationVariant;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides name and description of the tasks.
 */
@SuppressWarnings("deprecation")
final class AndroidAppTaskNames {

    private final ApplicationVariant myVariant;

    /**
     * Name of the task for merging the assets.
     */
    @Getter
    private final String mergeAssetsName;

    /**
     * Name of the task for generating the credit file.
     */
    @Getter
    private final String generateCreditsName;

    AndroidAppTaskNames(@Nonnull ApplicationVariant variant) {
        String varName;

        myVariant = Objects.requireNonNull(variant,
                "Argument variant is null.");

        varName = StringUtils.capitalize(variant.getName());
        mergeAssetsName = "merge".concat(varName).concat("Assets");
        generateCreditsName = "generate".concat(varName).concat("Credits");
    }

    /**
     * Gets the description of the task for generating the credit file.
     *
     * @return Value.
     */
    String getGenerateCreditsDescription() {
        return String.format("Generate credits for %1$s.", myVariant.getName());
    }
}
