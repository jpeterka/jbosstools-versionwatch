package org.jboss.tools.vwatch;

import org.apache.log4j.Level;
/**
 * Version Watch Settings class
 * Currently supported 
 * @author Jiri Peterka
 *
 */
public class Settings {
	
	private static Level loglevel = Level.WARN;	// -Dloglevel=4
	// Properties that can be used with Version Watch (-Dproperty)
	public static final String md5checkVMProperty = "vwatch.md5check"; 
	public static final String productVMProperty= "vwatch.product";
	public static final String includeVersionsProperty= "vwatch.includeVersions";
	public static final String excludeVersionsProperty= "vwatch.excludeVersions";
	public static final String includeIUsProperty= "vwatch.includeIUs";
	public static final String excludeIUsProperty= "vwatch.excludeIUs";
	// Default values
	private static boolean md5checkEnabled = false;	
	private static String product = "";
	private static String includeVersions = "\\d+\\.\\d+\\.\\d+";
	private static String excludeVersions = "";
	private static String includeIUs = ".*";
	private static String excludeIUs = "";
		
	public static void setLogLevel(Level level) {
		Settings.loglevel = level;
	}
	public static Level getLogLevel() {
		return Settings.loglevel;
	}
	
	public static boolean isMd5checkEnabled() {
		return md5checkEnabled;
	}

	public static void setMd5checkEnabled(boolean md5checkEnabled) {
		Settings.md5checkEnabled = md5checkEnabled;
	}
	public static void setProduct(String product) {
		Settings.product = product;
	}
	public static String getProduct() {
		return product;
	}

	public static String getIncludeVersions() {
		return includeVersions;
	}
	public static void setIncludeVersions(String includeVersions) {
		Settings.includeVersions = includeVersions;
	}
	public static String getExcludeVersions() {
		return excludeVersions;
	}
	public static void setExcludeVersions(String excludeVersions) {
		Settings.excludeVersions = excludeVersions;
	}

	public static String getIncludeIUs() {
		return includeIUs;
	}
	public static void setIncludeIUs(String includeIUs) {
		Settings.includeIUs = includeIUs;
	}
	public static String getExcludeIUs() {
		return excludeIUs;
	}
	public static void setExcludeIUs(String excludeIUs) {
		Settings.excludeIUs = excludeIUs;
	}
}