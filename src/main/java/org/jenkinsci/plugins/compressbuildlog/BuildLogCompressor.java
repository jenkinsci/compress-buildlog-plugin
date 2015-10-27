package org.jenkinsci.plugins.compressbuildlog;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class BuildLogCompressor extends JobProperty<AbstractProject<?, ?>> {

    @DataBoundConstructor
    public BuildLogCompressor() {
        // required for form magic
    }

    @Extension
    public final static class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "Compress Build Log";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            if (formData.isNullObject()) {
                return null;
            }
            JSONObject prerequisites = formData.getJSONObject("buildlogcompression");
            if (prerequisites.isNullObject()) {
                return null;
            }
            return req.bindJSON(BuildLogCompressor.class,prerequisites);
        }
    }


    @Extension
    public final static class CompressBuildlogRunListener extends RunListener<Run> {

        @Override
        public void onFinalized(Run run) {

            if (run.getParent().getProperty(BuildLogCompressor.class) == null) {
                LOGGER.log(Level.FINER, String.format("Skipping %s because the project is not configured to have compressed logs", run));
                return;
            }

            Util.compressLogFile(run);

            Plugin mvn = Jenkins.getInstance().getPlugin("maven-plugin");
            if (mvn!= null) {
                // compress module logs, if this is a maven reactor build
                Class<?> c;
				try {
					c = Class.forName("org.jenkinsci.plugins.compressbuildlog.MavenModuleLogCompressor");
					Constructor<?> con = c.getConstructor(Run.class);
					((Runnable)con.newInstance(run)).run();
				} catch (Throwable e) {
					LOGGER.warning("While creating instance of MavenModuleLogCompressor: "+e);
					e.printStackTrace();
				}
            }
        }

		static final Logger LOGGER = Logger.getLogger(CompressBuildlogRunListener.class.getName());
    }


}
