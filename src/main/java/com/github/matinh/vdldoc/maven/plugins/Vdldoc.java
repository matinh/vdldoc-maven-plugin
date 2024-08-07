/*
 * Copyright 2016-2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.matinh.vdldoc.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.plexus.util.StringUtils;
import org.omnifaces.vdldoc.VdldocGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Generate documentation for JSF tag libraries via OmniFaces Vdldoc.
 *
 * @author martin
 * @since 1.0-alpha-1
 */
@Mojo(name = "vdldoc", defaultPhase = LifecyclePhase.SITE)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public class Vdldoc
    extends AbstractMojo
    implements MavenReport
{
    /**
     * Browser window title.
     */
    @Parameter(defaultValue = "${project.name} VDL Documentation")
    private String browserTitle;

    /**
     * Documentation title.
     */
    @Parameter(defaultValue = "${project.name} VDL Documentation")
    private String documentTitle;

    /**
     * If {@code false}, build will continue when generation of documentation
     * fails.
     */
    // we initialize to default value here for unit testing.
    @Parameter(defaultValue = "true", property = "maven.vdldoc.failOnError")
    private boolean failOnError = true;

    /**
     * Skip the generation of VDL documentation.
     */
    @Parameter(defaultValue = "false", property = "maven.vdldoc.skip")
    private boolean skip;

    /**
     * Patterns to include when searching for taglib descriptor files.
     */
    @Parameter(defaultValue = "**/*.taglib.xml",
        property = "maven.vdldoc.includes")
    private List<String> includes;

    /**
     * Patterns to exclude when searching for taglib descriptor files.
     */
    @Parameter(defaultValue = "target/**", property = "maven.vdldoc.excludes")
    private List<String> excludes;

    /**
     * Location of the output directory for the generated report.
     * This configuration is usually only useful if you call the mojo directly
     * from the command-line.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}",
        property = "maven.vdldoc.outputDirectory")
    private File reportOutputDirectory;

    /**
     * Name of the directory (inside the reporting-output-directory) into which
     * Vdldoc will place the generated documentation.
     */
    // we initialize to default value here for unit testing.
    @Parameter(defaultValue = "vdldoc", property = "maven.vdldoc.destDir")
    private String destDir = "vdldoc";

    /**
     * URI of the CSS file. This defaults to Vdldocs internal CSS.
     * @since 1.0
     */
    @Parameter(property = "maven.vdldoc.css")
    private String css;

    /**
     * Path to the faces-config.xml file.
     * @since 1.0
     */
    @Parameter(property = "maven.vdldoc.facesConfig")
    private String facesConfig;

    /**
     * Path to properties file containing descriptions for implied attributes
     * of composite components, such as 'id', 'rendered', etc.
     * @since 1.0
     */
    @Parameter(property = "maven.vdldoc.attributesFile")
    private String attributesFile;

    /**
     * Hide the "output generated by" footer.
     * @since 1.0
     */
    @Parameter(defaultValue = "false",
        property = "maven.vdldoc.hideGeneratedBy")
    private boolean hideGeneratedBy;


    // ============ end of writable mojo parameters ===========

    /**
     * The directory to scan for taglib files.
     */
    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File srcDirectory;

    /**
     * The documentation generator to use.
     */
    private VdldocGenerator generator = new VdldocGenerator();


    /**
     * Set the generator to use for documentation generation.
     * <p>
     * This method is meant for unit tests only.
     * </p>
     * @param gen The generator to use. Must not be {@code null}.
     */
    void setGenerator(final VdldocGenerator gen)
    {
        this.generator = gen;
    }

    @Override
    public void execute() throws MojoExecutionException
    {
        if (skip) {
            getLog().info("Skipping generation of Vdldoc.");
            return;
        }

        try {
            generateDocumentation();
        } catch (Exception e) {
            if (failOnError) {
                throw new MojoExecutionException("Error generating Vdldoc!", e);
            }

            // log the failure and continue
            final String reason = e.getLocalizedMessage() == null
                ? e.toString() : e.getLocalizedMessage();
            getLog().warn("Failed to generate documentation: "
                + reason);
            getLog().debug(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void generate(final Sink sink, final Locale locale)
        throws MavenReportException
    {
        try {
            execute();
        } catch (MojoExecutionException e) {
            throw new MavenReportException(e.getMessage(),
                (Exception) e.getCause());
        }
    }

    @Override
    public String getOutputName()
    {
        return destDir + File.separator + "index";
    }

    @Override
    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    @Override
    public String getName(final Locale locale)
    {
        return getBundle(locale).getString("report.vdldoc.name");
    }

    @Override
    public String getDescription(final Locale locale)
    {
        return getBundle(locale).getString("report.vdldoc.description");
    }

    @Override
    public void setReportOutputDirectory(final File file)
    {
        reportOutputDirectory = file;
    }

    @Override
    public File getReportOutputDirectory()
    {
        return reportOutputDirectory;
    }

    @Override
    public boolean isExternalReport()
    {
        return true;
    }

    @Override
    public boolean canGenerateReport()
    {
        return true;
    }

    /**
     * Gets the resource bundle for the specified locale.
     *
     * @param locale The locale of the currently generated report.
     * @return The resource bundle for the requested locale.
     */
    private ResourceBundle getBundle(final Locale locale)
    {
        return ResourceBundle.getBundle(
            "vdldoc-report", locale, getClass().getClassLoader());
    }

    private void generateDocumentation()
    {
        generator.setWindowTitle(browserTitle);
        generator.setDocTitle(documentTitle);
        generator.setOutputDirectory(new File(reportOutputDirectory, destDir));

        if (!StringUtils.isEmpty(css)) {
            getLog().debug("Using CSS file " + css);
            generator.setCssLocation(css);
        }

        if (!StringUtils.isEmpty(facesConfig)) {
            getLog().debug("Using faces-config file "
                + facesConfig);
            generator.setFacesConfig(new File(facesConfig));
        }
        if (!StringUtils.isEmpty(attributesFile)) {
            getLog().debug("Using attributes-file "
                + attributesFile);
            generator.setAttributes(new File(attributesFile));
        }
        if (hideGeneratedBy) {
            getLog().debug("Hiding 'generated-by' message.");
            generator.setHideGeneratedBy(true);
        }

        // TODO add support for quiet-flag

        final List<String> taglibs = scanForTaglibs(
            srcDirectory, includes, excludes);
        getLog().debug("Found taglibs: " + taglibs);
        for (String taglib : taglibs) {
            generator.addTaglib(new File(srcDirectory, taglib));
        }

        generator.generate();
    }

    private List<String> scanForTaglibs(final File basedir,
        final List<String> includeList, final List<String> excludeList)
    {
        List<String> result = new ArrayList<>();
        if (basedir != null && basedir.exists()) {
            org.codehaus.plexus.util.DirectoryScanner scanner =
                new org.codehaus.plexus.util.DirectoryScanner();

            scanner.setBasedir(basedir);

            if (includeList != null) {
                scanner.setIncludes(processIncludesExcludes(includeList));
            }

            if (excludeList != null) {
                scanner.setExcludes(processIncludesExcludes(excludeList));
            }

            scanner.scan();
            Collections.addAll(result, scanner.getIncludedFiles());
        }
        return result;
    }

    // based on maven-surefire-plugin's DirectoryScanner
    private static String[] processIncludesExcludes(final List<String> list)
    {
        List<String> newList = new ArrayList<>();
        for (String aList : list) {
            String[] includes = aList.split(",");
            Collections.addAll(newList, includes);
        }

        String[] incs = new String[newList.size()];
        for (int i = 0; i < incs.length; i++) {
            incs[i] = newList.get(i);
        }

        return incs;
    }
}
