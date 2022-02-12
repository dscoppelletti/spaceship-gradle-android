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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import it.scoppelletti.spaceship.gradle.android.AndroidLibraryTools;
import it.scoppelletti.spaceship.gradle.android.model.CreditItem;
import it.scoppelletti.spaceship.gradle.model.SpaceshipExtension;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Generate the credit file.
 */
public class CreditsTask extends DefaultTask {

    /**
     * Namespace of the credit database XML file.
     */
    @SuppressWarnings("unused")
    public static final String NAMESPACE =
            "http://www.scoppelletti.it/ns/credits/1";

    /**
     * Template variable {@code credits}.
     */
    public static final String VAR_CREDITS = "credits";

    /**
     * Variant name.
     */
    @Getter(onMethod_ = { @Input })
    @Setter
    private String variantName;

    /**
     * Credit database URL.
     */
    @Getter(onMethod_ = { @Input })
    @Setter
    private URI databaseUrl;

    /**
     * Template name.
     */
    @Getter(onMethod_ = { @Input })
    @Setter
    private String templateName;

    /**
     * Credit output file
     */
    @Getter(onMethod_ = { @OutputFile })
    @Setter
    private File outputFile;

    /**
     * Sole constructor.
     */
    @Inject
    public CreditsTask() {
    }

    /**
     * Executes the task.
     */
    @TaskAction
    public void run() {
        String configName;
        File outDir;
        Configuration config;
        CreditDatabase database;
        SpaceshipExtension spaceshipExt;
        ITemplateEngine templEngine;
        Context templCtx;
        Set<CreditItem> credits;

        if (StringUtils.isBlank(variantName)) {
            throw new NullPointerException("Property variantName is null.");
        }

        Objects.requireNonNull(databaseUrl, "Property databaseUrl is null.");

        if (StringUtils.isBlank(templateName)) {
            throw new NullPointerException("Property templateName is null.");
        }

        Objects.requireNonNull(outputFile, "Property creditFile is null.");

        spaceshipExt = Objects.requireNonNull(getProject().getExtensions()
                .findByType(SpaceshipExtension.class), () ->
                String.format("Extension %1$s not found.",
                        SpaceshipExtension.class));

        database = CreditDatabase.load(databaseUrl);

        configName = variantName.concat(AndroidLibraryTools.CONFIG_RUNTIME);
        config = getProject().getConfigurations().getByName(configName);

        outDir = outputFile.getParentFile();
        if (outDir != null && !outDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outDir.mkdirs();
        }

        credits = new TreeSet<>();
        database.getCredits().forEach(item -> {
            if (item.isForce()) {
                credits.add(item);
            }
        });

        config.getAllDependencies().withType(ExternalDependency.class)
                .forEach(dep -> addDependency(credits, dep, database));

        templCtx = new Context();
        templCtx.setVariable(CreditsTask.VAR_CREDITS, credits);
        templEngine = spaceshipExt.getTemplateEngine();

        try (Writer out = new FileWriter(outputFile);
             BufferedWriter writer = new BufferedWriter(out)) {
            templEngine.process(templateName, templCtx, writer);
        } catch (IOException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
    }

    private void addDependency(Set<CreditItem> credits, ExternalDependency dep,
            CreditDatabase database) {
        String artifactId, groupId;
        CreditItem credit;
        ArtifactItem artifact;

        groupId = Objects.requireNonNull(dep.getGroup(),
                "Argument dep.group is null.");
        artifactId = dep.getName();
        artifact = new ArtifactItem(groupId, artifactId);
        getLogger().debug("Detect artifact {}.", artifact);

        credit = database.getCreditByArtifact(artifact);
        if (credit == null) {
            getLogger().warn("No credit found for artifact {}.", artifact);
            return;
        }

        credits.add(credit);
    }
}
