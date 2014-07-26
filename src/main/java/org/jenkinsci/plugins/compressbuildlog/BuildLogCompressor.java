package org.jenkinsci.plugins.compressbuildlog;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

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
            File log = run.getLogFile();
            if (log.getName().endsWith(".gz")) {
                // ignore already compressed log
                LOGGER.log(Level.FINER, String.format("Skipping %s because the log is already compressed", run));
                return;
            }

            if (run.getParent().getProperty(BuildLogCompressor.class) == null) {
                LOGGER.log(Level.FINER, String.format("Skipping %s because the project is not configured to have compressed logs", run));
                return;
            }

            String gzippedLogName = log.getName() + ".gz";

            if (log.getName().equals("log")) {
                LOGGER.log(Level.FINE, String.format("Compressing build log of %s", run));

                // regular, expected log file
                FileInputStream fis = null;
                FileOutputStream fos = null;
                GZIPOutputStream gzos = null;

                try {
                    fis = new FileInputStream(log);
                    fos = new FileOutputStream(new File(log.getParentFile(), gzippedLogName));
                    gzos = new GZIPOutputStream(fos);
                    int copiedBytes = IOUtils.copy(fis, gzos);

                    if (copiedBytes != log.length()) {
                        LOGGER.log(Level.WARNING, String.format("Expected to copy %d bytes but copied %d from %s", copiedBytes, log.length(), log.getAbsolutePath()));
                    }

                    gzos.finish();
                    LOGGER.log(Level.FINE, String.format("Finished compressing build log of %s", run));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, String.format("Failed to compress build log of %s to %s", run, gzippedLogName));
                    return;
                } finally {
                    IOUtils.closeQuietly(fis);
                    IOUtils.closeQuietly(fos);
                    IOUtils.closeQuietly(gzos);
                }

                // XXX try multiple times because Windows?
                if (!log.delete()) {
                    LOGGER.log(Level.WARNING, String.format("Failed to delete build log of %s after compression", run));
                }
            }
        }

        private static final Logger LOGGER = Logger.getLogger(CompressBuildlogRunListener.class.getName());
    }


}
