package org.jenkinsci.plugins.compressbuildlog;

import hudson.model.Run;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.compressbuildlog.BuildLogCompressor.CompressBuildlogRunListener;

public class Util {

	public static void compressLogFile(Run run) {
	    File log = run.getLogFile();
	
	    if (log.getName().endsWith(".gz")) {
	        // ignore already compressed log
	        CompressBuildlogRunListener.LOGGER.log(Level.FINER, String.format(
	                "Skipping %s because the log is already compressed",
	                run));
	        return;
	    }
	    String gzippedLogName = log.getName() + ".gz";
	
	    if (log.getName().equals("log")) {
	        CompressBuildlogRunListener.LOGGER.log(Level.FINE, String.format("Compressing build log of %s", run));

	
	        try {
	            InputStream fis = new BufferedInputStream(new FileInputStream(log));
	            OutputStream fos = new BufferedOutputStream(
	        	    			new FileOutputStream(new File(log.getParentFile(), gzippedLogName)));
	            GZIPOutputStream gzos = new GZIPOutputStream(fos);
	            int copiedBytes = IOUtils.copy(fis, gzos);
	
	            if (copiedBytes != log.length()) {
	                CompressBuildlogRunListener.LOGGER.log(Level.WARNING, String.format("Expected to copy %d bytes but copied %d from %s", copiedBytes, log.length(), log.getAbsolutePath()));
	            }
	
	            gzos.finish();
	            tryClose(fis);
	            tryClose(gzos);
	            
	            CompressBuildlogRunListener.LOGGER.log(Level.FINE, String.format("Finished compressing build log of %s", run));
	        } catch (IOException e) {
	            CompressBuildlogRunListener.LOGGER.log(Level.WARNING, String.format("Failed to compress build log of %s to %s", run, gzippedLogName));
	            return;
	        }
	
	        // XXX try multiple times because Windows?
	        if (!log.delete()) {
	            CompressBuildlogRunListener.LOGGER.log(Level.WARNING, String.format("Failed to delete build log of %s after compression", run));
	        }
	    }
	}

	private static void tryClose(Closeable c) {
	    try{
		c.close();
	    }
	    catch(IOException e){
		CompressBuildlogRunListener.LOGGER.log(Level.WARNING, String.format("Failed to close "+c, e));
	    }
	}
}
