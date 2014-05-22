package org.jenkinsci.plugins.database.orientdb;

import hudson.Extension;
import hudson.model.RootAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@Extension
public class GlobalDatabaseConsole implements RootAction {

	@Inject
	private GlobalDatabaseConfiguration globalDatabaseConfiguration;
	
	@Override
	public String getUrlName() {
		return "global-orientdb-console";
	}

	@Override
	public String getDisplayName() {
		return "Global Database Console";
	}

	@Override
	public String getIconFileName() {
		return "terminal.png";
	}

	 public HttpResponse doExecute(@QueryParameter final String sql) {
        Jenkins.getInstance().checkPermission(Jenkins.RUN_SCRIPTS);
        
        Map<String, Object> results = new HashMap<String, Object>();
        try {
        	Database database = globalDatabaseConfiguration.getDatabase();
        	
        	if (database instanceof LocalOrientDbDatabase) {
        		OObjectDatabaseTx oDatabase = ((LocalOrientDbDatabase) database).getDatabase();
        		List<ODocument> result = oDatabase.command(new OSQLSynchQuery<ODocument>(sql)).execute();
        		results.put("results", result);
        	}
        	
        } catch (Throwable t) {
        	results.put("message", t.getMessage());
        }
        
        return HttpResponses.forwardToView(this, "index").with(results);
    }
	 
}
