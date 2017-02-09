/*
 * Copyright 2017 The Apache Software Foundation.
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReportException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.omnifaces.vdldoc.VdldocGenerator;

import java.io.File;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for mojo class {@link Vdldoc}.
 *
 * @author <a href="mailto:martin.hoeller@xss.co.at">Martin Hoeller</a>
 * @version $$Revision$$
 */
public class VdldocTest
{
    private Vdldoc mojo;

    @Before
    public void setUp() throws Exception
    {
        mojo = new Vdldoc();
    }

    @Test
    public void testGenerate() throws MavenReportException
    {
        final VdldocGenerator generator = createMockGenerator();

        mojo.setGenerator(generator);
        mojo.setReportOutputDirectory(new File("/some/outputDir"));
        mojo.generate(null, Locale.getDefault());

        EasyMock.verify(generator);
    }

    @Test
    public void testGenerateFailed()
    {
        try {
            // illegal initialization => NPE should be caught by mojo and
            // re-thrown as MojoExecutionException.
            mojo.setGenerator(null);
            mojo.generate(null, Locale.getDefault());
            fail("Expected exception not thrown.");
        }
        catch (MavenReportException ex) {
            // expected exception, ok.
            assertTrue(ex.getCause() instanceof NullPointerException);
        }
    }

    @Test
    public void testExecute() throws MojoExecutionException
    {
        final VdldocGenerator generator = createMockGenerator();

        mojo.setGenerator(generator);
        mojo.setReportOutputDirectory(new File("/some/outputDir"));
        mojo.execute();

        EasyMock.verify(generator);
    }

    @Test
    public void testExecuteFailed() throws MojoExecutionException
    {
        try {
            // illegal initialization => NPE should be caught by mojo and
            // re-thrown as MojoExecutionException.
            mojo.setGenerator(null);
            mojo.execute();
            fail("Expected exception not thrown.");
        }
        catch (MojoExecutionException ex) {
            // expected exception, ok.
            assertTrue(ex.getCause() instanceof NullPointerException);
        }
    }

    private VdldocGenerator createMockGenerator()
    {
        final VdldocGenerator generator =
            EasyMock.createMock(VdldocGenerator.class);
        generator.setWindowTitle(EasyMock.anyString());
        EasyMock.expectLastCall().once();
        generator.setDocTitle(EasyMock.anyString());
        EasyMock.expectLastCall().once();
        generator.setOutputDirectory((File) EasyMock.anyObject());
        EasyMock.expectLastCall().once();
        generator.generate();
        EasyMock.expectLastCall().once();
        EasyMock.replay(generator);
        return generator;
    }

    @Test
    public void testGetOutputName() throws Exception
    {
        // We cannot check the first part, as the mojo got no parameters injected.
        assertTrue(mojo.getOutputName().endsWith(File.separator + "index"));
    }

    @Test
    public void testGetCategoryName() throws Exception
    {
        assertEquals(Vdldoc.CATEGORY_PROJECT_REPORTS, mojo.getCategoryName());
    }

    @Test
    public void testGetName() throws Exception
    {
        assertEquals("VDL Docs", mojo.getName(Locale.ENGLISH));
    }

    @Test
    public void testGetDescription() throws Exception
    {
        assertEquals("VDL documentation.", mojo.getDescription(Locale.ENGLISH));
    }

    @Test
    public void testSetReportOutputDirectory() throws Exception
    {
        assertNull(mojo.getReportOutputDirectory());

        mojo.setReportOutputDirectory(new File("/some/dir"));
        final File outputDir = mojo.getReportOutputDirectory();

        assertNotNull(outputDir);
        assertEquals("/some/dir", outputDir.getAbsolutePath());
    }

    @Test
    public void testIsExternalReport() throws Exception
    {
        assertTrue("The VDLDOC mojo should create an external report",
            mojo.isExternalReport());
    }

    @Test
    public void testCanGenerateReport() throws Exception
    {
        assertTrue("The VDLDOC mojo should be able to generate a report.",
            mojo.canGenerateReport());
    }
}