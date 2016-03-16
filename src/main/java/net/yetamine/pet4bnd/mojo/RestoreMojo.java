/*
 * Copyright 2016 Yetamine
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

package net.yetamine.pet4bnd.mojo;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.yetamine.pet4bnd.model.format.PetFormat;

/**
 * Restores the baselines and resets the change information in the source file.
 */
@Mojo(name = "restore", requiresDirectInvocation = true)
public final class RestoreMojo extends AbstractPet4BndMojo {

    /** Location of the source file. */
    @Parameter(defaultValue = "${pet4bnd.source}", property = "pet4bnd.source", required = false)
    private String source;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        final Path sourcePath = resolvePath(source).orElseGet(this::getDefaultSourcePath);

        final Log log = getLog();
        log.info(String.format("Updating definition file: %s", sourcePath));
        final PetFormat definition = resolveDefinition(parseSource(sourcePath));

        try {
            definition.restore();
            definition.store(sourcePath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        log.info(String.format("Target bundle version: %s", definition.version().resolution()));
    }
}
