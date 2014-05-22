package org.jenkinsci.plugins.database.orientdb;

import hudson.Extension;
import hudson.model.TopLevelItem;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.jenkinsci.plugins.database.PerItemDatabaseDescriptor;
import org.jenkinsci.plugins.database.PerItemDatabase;
import org.kohsuke.stapler.DataBoundConstructor;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class PerItemOrientDbDatabase extends PerItemDatabase {

	private transient Map<TopLevelItem, ODatabase> sources;

	@DataBoundConstructor
	public PerItemOrientDbDatabase() {
	}

	@Override
	public DataSource getDataSource(TopLevelItem item) throws SQLException {
		return null;
	}

	public synchronized ODatabase getDatabase(TopLevelItem item) {
		if (sources == null) {
			sources = new WeakHashMap<TopLevelItem, ODatabase>();
		}

		ODatabase database = sources.get(item);

		if (database == null) {
			database = new OObjectDatabaseTx("plocal:" + item.getRootDir().getAbsolutePath() + "/data");

			if (!database.exists()) {
				database.create();
			}

			if (database.isClosed()) {
				// admin/admin are default credentials for local db's.
				database.open("admin", "admin");
			}

			sources.put(item, database);
		}

		return database;
	}

	public void close() {
		Set<TopLevelItem> keySet = sources.keySet();
		for (TopLevelItem topLevelItem : keySet) {
			sources.get(topLevelItem).close();
		}
	}

	@Extension
	public static class PerItemOrientDbDatabaseDescriptor extends PerItemDatabaseDescriptor {

		@Override
		public String getDisplayName() {
			return "Embedded local database (OrientDB)";
		}

	}

//	@Terminator(after = TermMilestone.STARTED)
//	public static void closeResources() {
//		Jenkins jenkins = Jenkins.getInstance();
//		PerItemDatabaseConfiguration gdc = jenkins.getExtensionList(PerItemDatabaseConfiguration.class).get(PerItemDatabaseConfiguration.class);
//
//		if (gdc == null) {
//			return;
//		}
//
//		PerItemDatabase database = gdc.getDatabase();
//		if (database != null && database instanceof PerItemOrientDbDatabase) {
//			((PerItemOrientDbDatabase) database).close();
//		}
//
//	}
}
