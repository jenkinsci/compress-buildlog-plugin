package org.jenkinsci.plugins.compressbuildlog;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.jvnet.hudson.test.JenkinsRule;

public class BuildLogCompressorPipelineTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void testIsApplicable() throws Exception {
        WorkflowJob workflowJob = r.jenkins.createProject(WorkflowJob.class, "pipeline");
        Assert.assertNotNull(workflowJob);

        BuildLogCompressor.DescriptorImpl descriptor = new BuildLogCompressor.DescriptorImpl();
        Assert.assertNotNull(descriptor);

        boolean res = descriptor.isApplicable(workflowJob.getClass());
        Assert.assertFalse("TODO does not work with post-JEP-210 Pipeline", res);
    }
}
