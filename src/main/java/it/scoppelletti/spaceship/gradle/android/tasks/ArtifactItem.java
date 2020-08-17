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
package it.scoppelletti.spaceship.gradle.android.tasks;

import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Artifact.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
final class ArtifactItem {
    static final String ELEMENT = "artifact";
    static final String GROUPID_ATTR = "groupId";
    static final String ARTIFACTID_ATTR = "artifactId";

    @Getter
    @Nonnull
    private final String groupId;

    @Getter
    @Nonnull
    private final String artifactId;

    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder(groupId)
                .append(':')
                .append(artifactId).toString();
    }
}
