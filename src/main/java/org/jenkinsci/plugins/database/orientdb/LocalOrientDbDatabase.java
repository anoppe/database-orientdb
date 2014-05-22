package org.jenkinsci.plugins.database.orientdb;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import org.apache.commons.lang.NotImplementedException;
import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class LocalOrientDbDatabase extends Database {

	private static Logger logger = Logger.getLogger(LocalOrientDbDatabase.class.getName());
	
	private final File path;
	
	@DataBoundConstructor
	public LocalOrientDbDatabase(File path) {
		this.path = path;
	}
	
	public File getPath() {
		return path;
	}
	
	/**
	 * not used.
	 */
	@Override
	public DataSource getDataSource() throws SQLException {
		throw new NotImplementedException("Not implemented for this class, use method getDatabase() instead");
	}

	/**
	 * Aquire {@link OObjectDatabaseTx} instance. Will be created if not exists already.
	 * 
	 * @return	an initialized database instance.
	 */
	public synchronized OObjectDatabaseTx getDatabase() {
			
		if (!path.exists()) {
			boolean mkdirs = path.mkdirs();
			logger.info("database target directories (" + path + ") created: " + mkdirs);
		}

		
		if (path.list().length == 0) {
			logger.info("creating database @ " + path.getAbsolutePath());
			OObjectDatabaseTx tmpDatabase = new OObjectDatabaseTx("plocal:" + path.getAbsolutePath());
			
			if (!tmpDatabase.exists()) {
				tmpDatabase.create();
			}
			if (!tmpDatabase.isClosed()) {
				tmpDatabase.close();
			}
		}
		
		logger.info("loaded database from " + path.getAbsolutePath());
		
		OObjectDatabasePool oObjectDatabasePool = new OObjectDatabasePool("plocal:" + path.getAbsolutePath(), "admin", "admin");
		OObjectDatabaseTx database = oObjectDatabasePool.acquire();
		
		if (!database.exists()) {
			database.create();
		}
		
		if (database.isClosed()) {
			database.open("admin", "admin");
		}
		
		return database;
	}

	@Extension
	public static class LocalOrientDbDatabaseDescriptor extends DatabaseDescriptor {
		
		@Override
		public String getDisplayName() {
			return "Embedded local database (orientDB)";
		}
		
	}
	
	@Initializer(after=InitMilestone.PLUGINS_STARTED)
    public static void setDefaultGlobalDatabase() {
		logger.info("Post initialize");
        Jenkins j = Jenkins.getInstance();
        GlobalDatabaseConfiguration gdc = j.getExtensionList(GlobalConfiguration.class).get(GlobalDatabaseConfiguration.class);
        if (gdc!=null) {
            if (gdc.getDatabase()==null) {
            	logger.info("Setting default global database for LocalOrientDbDatabase");
            	gdc.setDatabase(new LocalOrientDbDatabase(new File(j.getRootDir(), "global")));
            }
        }
    }
	
	/**
	 * Close the database if open.
	 */
//	@Terminator(after=TermMilestone.STARTED)
//	public static void closeDatabaseOnShutdown() {
//		logger.info("Closing OrientDB resources");
//		OObjectDatabasePool.global().acquire().freeze();
//		OObjectDatabasePool.global().close();
//	}
}
