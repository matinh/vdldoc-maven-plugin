package com.github.matinh.vdldoc.maven.plugins;

/*
 * Copyright 2016 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
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
 * @since 1.0-SNAPSHOT
 */
@Mojo(name = "vdldoc", defaultPhase = LifecyclePhase.SITE, requiresProject = true)
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
     * If {@code false}, build will continue when generation of documentation fails.
     */
    @Parameter(defaultValue = "true", property = "maven.vdldoc.failOnError")
    private boolean failOnError;

    /**
     * Skip the generation of VDL documentation.
     */
    @Parameter(defaultValue = "false", property = "maven.vdldoc.skip")
    private boolean skip;

    /**
     * Patterns to include when searching for taglib descriptor files.
     */
    @Parameter(defaultValue = "**/*.taglib.xml", property = "includes")
    private List<String> includes;

    /**
     * Patterns to exclude when searching for taglib descriptor files.
     */
    @Parameter(defaultValue = "target/**", property = "excludes")
    private List<String> excludes;

    /**
     *  Location of the output directory for the generated report.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}", property = "maven.vdldoc.outputDirectory")
    private File reportOutputDirectory;

    /**
     * Name of the directory (inside the reporting-output-directory) into which
     * Vdldoc will place the generated documentation.
     */
    @Parameter(defaultValue = "vdldoc", property = "maven.vdldoc.destDir")
    private String destDir;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File srcDirectory;


    public void execute() throws MojoExecutionException
    {
        if (skip) {
            getLog().info("Skipping generation of Vdldoc.");
            return;
        }

        try {
            generateDocumentation();
        }
        catch (Exception e) {
            if (failOnError)
                throw new MojoExecutionException("Error generating Vdldoc!", e);

            // log the failure and continue
            getLog().warn("Failed to generate documentation: " + e.getLocalizedMessage());
            getLog().debug(e);
        }
    }

    public void generate(Sink sink, Locale locale) throws MavenReportException
    {
        try {
            execute();
        }
        catch (MojoExecutionException e) {
            throw new MavenReportException(e.getMessage(), (Exception) e.getCause());
        }
    }

    public String getOutputName()
    {
        return destDir + File.separator + "index";
    }

    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    public String getName(Locale locale)
    {
        return getBundle(locale).getString("report.vdldoc.name");
    }

    public String getDescription(Locale locale)
    {
        return getBundle(locale).getString("report.vdldoc.description");
    }

    public void setReportOutputDirectory(File file)
    {
        reportOutputDirectory = file;
    }

    public File getReportOutputDirectory()
    {
        return reportOutputDirectory;
    }

    public boolean isExternalReport()
    {
        return true;
    }

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
    private ResourceBundle getBundle(Locale locale)
    {
        return ResourceBundle.getBundle("vdldoc-report", locale, getClass().getClassLoader());
    }

    private void generateDocumentation()
    {
        VdldocGenerator generator = new VdldocGenerator();
        generator.setWindowTitle(browserTitle);
        generator.setDocTitle(documentTitle);
        generator.setOutputDirectory(new File(reportOutputDirectory, destDir));
        // TODO add further support
//        generator.setCssLocation("/uri/to/style.css"); // Optional (overrides default CSS).
//        generator.setFacesConfig(new File("/path/to/faces-config.xml")); // Optional.
//        generator.setAttributes(new File("/path/to/cc-attributes.properties")); // Optional.
//        generator.setHideGeneratedBy(true); // Optional.
        final List<String> taglibs = scanForTaglibs(srcDirectory, includes, excludes);
        getLog().debug("Found taglibs: " + taglibs);
        for (String taglib : taglibs) {
            generator.addTaglib(new File(srcDirectory, taglib));
        }

        generator.generate();
    }

    private List<String> scanForTaglibs(File basedir, List<String> includes, List<String> excludes)
    {
        List<String> result = new ArrayList<String>();
        if (basedir.exists()) {
            org.codehaus.plexus.util.DirectoryScanner scanner = new org.codehaus.plexus.util.DirectoryScanner();

            scanner.setBasedir(basedir);

            if (includes != null) {
                scanner.setIncludes(processIncludesExcludes(includes));
            }

            if (excludes != null) {
                scanner.setExcludes(processIncludesExcludes(excludes));
            }

            scanner.scan();
            Collections.addAll(result, scanner.getIncludedFiles());
        }
        return result;
    }

    // based on maven-surefire-plugin's DirectoryScanner
    private static String[] processIncludesExcludes(List<String> list)
    {
        List<String> newList = new ArrayList<String>();
        for (Object aList : list) {
            String include = (String) aList;
            String[] includes = include.split(",");
            Collections.addAll(newList, includes);
        }

        String[] incs = new String[newList.size()];
        for (int i = 0; i < incs.length; i++) {
            incs[i] = newList.get(i);
        }

        return incs;
    }
}