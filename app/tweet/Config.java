package tweet;

import java.io.File;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;

public class Config {
	private Hashtable<String,String> terms = new Hashtable<String,String>();
	private Hashtable<String,List<String>> excels = new Hashtable<String,List<String>>();
	private int pageSize;
	private int maxPages;

	public Config(File file) throws ConfigurationException {
          load(file);
        }

	public Config(String filename) throws ConfigurationException {
          File file = new File(filename);
          load(file);
        }

        private void load(File file) throws ConfigurationException {
		HierarchicalINIConfiguration config = new HierarchicalINIConfiguration();
		DefaultExpressionEngine ee = new DefaultExpressionEngine();
		ee.setPropertyDelimiter("::");
		config.setExpressionEngine(ee);
		config.load(file);

		SubnodeConfiguration general = config.getSection("general");
		pageSize = general.getInt("pagesize", 100);
		maxPages = general.getInt("maxpages", 500);

		SubnodeConfiguration search = config.getSection("search");
        for (Iterator<String> i=search.getKeys();i.hasNext();) {
        	String key = i.next();
        	terms.put(key, search.getString(key));
        }

        SubnodeConfiguration excel = config.getSection("excel");
        for (Iterator<String> i=excel.getKeys();i.hasNext();) {
        	String key = i.next();
        	String[] terms = excel.getStringArray(key);
        	excels.put(key, Arrays.asList(terms));
        }
	}
	
	Set<String> getExcelSet() {
		return excels.keySet();
	}

	public List<String> getTermsForExcel(String filename) {
		return excels.get(filename);
	}

	public Set<String> getQueryNames() {
		return terms.keySet();
	}
	
	public String getQueryForName(String queryName) {
		return terms.get(queryName);
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getMaxPages() {
		return maxPages;
	}
}
