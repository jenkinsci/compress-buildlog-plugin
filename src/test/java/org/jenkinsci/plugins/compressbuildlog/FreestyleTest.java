package org.jenkinsci.plugins.compressbuildlog;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.StringWriter;

public class FreestyleTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testCompression() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject();
        project.addProperty(new BuildLogCompressor());



        final FreeStyleBuild build = j.buildAndAssertSuccess(project);
        final File logFile = build.getLogFile();
        Assert.assertTrue("log file is gzipped", logFile.getName().endsWith(".gz"));
        Assert.assertFalse("original log file does not exist", new File(logFile.getParent(), logFile.getName().replace(".gz", "")).exists());

        StringWriter w = new StringWriter();
        build.getLogText().writeLogTo(0, w);
        Assert.assertTrue("log contains expected content", w.getBuffer().toString().contains("Building in workspace"));
    }
}
