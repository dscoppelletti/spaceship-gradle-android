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
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Credit.
 *
 * @since 1.0.0
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class CreditItem implements Comparable<CreditItem> {

    /**
     * XML element in credit database.
     */
    public static final String ELEMENT = "credit";

    /**
     * Key.
     */
    @Getter
    @Nonnull
    private final String key;

    /**
     * Whether the component must be cited regardless the using of its
     * artifacts.
     */
    @Getter
    @EqualsAndHashCode.Exclude
    private final boolean force;

    /**
     * Component.
     */
    @Getter
    @Setter
    @Nullable
    @EqualsAndHashCode.Exclude
    private String component;

    /**
     * Owner.
     */
    @Getter
    @Setter
    @Nullable
    @EqualsAndHashCode.Exclude
    private OwnerItem owner;

    /**
     * License.
     */
    @Getter
    @Setter
    @Nullable
    @EqualsAndHashCode.Exclude
    private LicenseItem license;

    @Override
    public int compareTo(@NotNull CreditItem op) {
        return StringUtils.compare(key, op.key);
    }
}
