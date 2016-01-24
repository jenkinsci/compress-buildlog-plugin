package org.jenkinsci.plugins.compressbuildlog;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Run;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MavenModuleLogCompressor implements Runnable {
	private Run run;

	public MavenModuleLogCompressor(Run run) {
		this.run = run;
	}

	public void run() {
		if (run instanceof MavenModuleSetBuild) {
			LOGGER.log(Level.FINE, "Detected MavenModuleSetBuild");
			MavenModuleSetBuild ms = (MavenModuleSetBuild) run;

			Map<MavenModule, MavenBuild> moduleBuilds = ms
					.getModuleLastBuilds();
			for (Entry<MavenModule, MavenBuild> d : moduleBuilds.entrySet()) {
				if (d != null) {
					MavenBuild moduleRun = d.getValue();
					Util.compressLogFile(moduleRun);
				}
			}
		}
	}

	static final Logger LOGGER = Logger
			.getLogger(MavenModuleLogCompressor.class.getName());
}
