package org.jboss.tools.vwatch.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.tools.vwatch.Settings;
import org.jboss.tools.vwatch.VWatch;
import org.jboss.tools.vwatch.model.Bundle;
import org.jboss.tools.vwatch.model.Installation;
import org.jboss.tools.vwatch.model.Issue;
import org.jboss.tools.vwatch.model.Severity;
import org.jboss.tools.vwatch.service.BundleService;
import org.jboss.tools.vwatch.service.ReportService;
import org.jboss.tools.vwatch.service.StopWatch;
import org.jboss.tools.vwatch.validator.PairValidator;

/**
 * Service providing final report generating from given installations
 * 
 * @author jpeterka
 * 
 */
public class BundleVersionReport extends Report {

	
	Logger log = Logger.getLogger(BundleVersionReport.class);
	List<Installation> installations; 

	/**
	 * Generates report
	 * 
	 * @param installations
	 *            given list of installations
	 * @param includeIUs
	 * 			  list of IUs to include in report
	 * @param excludeIUs
	 *            list of IUs to exclude from report
	 */
	
	public BundleVersionReport(List<Installation> installations) {
		this.installations = installations;
	}
	
	@Override
	public void generateReport() {
		
		File file = new File("report_detailed.html");
		String includeIUs = Settings.getIncludeIUs();
		String excludeIUs = Settings.getExcludeIUs();

		log.setLevel(Settings.getLogLevel());

		try {
			PrintWriter pw = new PrintWriter(file);
			BufferedWriter bw = new BufferedWriter(pw);
			String style = ReportService.getInstance().getCSSContent();
			bw.append("<html><head><title>JBDS Version Watch</title><style type=\"text/css\">"
					+ style + "</style></head>");
			bw.append("<body><h2>JBDS Version Watch</h2>");

			bw.append("<h2>Feature list" + (!PairValidator.isNullFilter(includeIUs) ? "<br/>&nbsp;includeIUs = /" + includeIUs + "/": "") + 
					(!PairValidator.isNullFilter(excludeIUs) ? (!PairValidator.isNullFilter(includeIUs)?" and ":"") + "<br/>&nbsp;excludeIUs = /" + excludeIUs  +"/": "") + "</h2>");
			generateTable(bw, installations, true, includeIUs, excludeIUs);
			bw.append("<br/><h2>Plugin list" + (!PairValidator.isNullFilter(includeIUs) ? "<br/>&nbsp;includeIUs = /" + includeIUs + "/": "") + 
					(!PairValidator.isNullFilter(excludeIUs) ? (!PairValidator.isNullFilter(includeIUs)?" and ":"") + "<br/>&nbsp;excludeIUs = /" + excludeIUs +"/": "") + "</h2>");
			generateTable(bw, installations, false, includeIUs, excludeIUs);

			long elapsed = StopWatch.stop();
			
			bw.append("<p>Generated by VersionWatch " + VWatch.VERSION + " in " +
				String.format("%d min, %d sec", 
							TimeUnit.MILLISECONDS.toMinutes(elapsed),
							TimeUnit.MILLISECONDS.toSeconds(elapsed) - 
							TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))
						)
				+ " at " + 
				(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date()) + ".</p>");
			bw.append("<p><div class=\"footspace\"></div></body></html>");
			bw.flush();

			printErrorLogFooter();

		} catch (Exception e) {
			log.error("IO error" + e.getMessage());
			e.printStackTrace();
			return;
		}

		log.warn("Report generated to file:///" + file.getAbsolutePath());
	}

	private void generateTable(BufferedWriter bw,
			List<Installation> installations, boolean feature, String includeIUs, String excludeIUs)
			throws IOException {

		log.setLevel(Settings.getLogLevel());

		BundleService bs = new BundleService();

		SortedSet<String> featureSet = new TreeSet<String>();

		for (Installation i : installations) {
			for (Bundle b : i.getBundles(feature)) {
				if (
						(PairValidator.isNullFilter(includeIUs) || b.getName().matches(includeIUs)) && 
						(PairValidator.isNullFilter(excludeIUs) || !b.getName().matches(excludeIUs))
					) {
					/*
					if (featureSet.contains(b.getName())) {
						Issue i = new Issue();
						i.setSeverity(2);
						i.setMessage("Multiple bundle versions");
						b.getIssues().add(i);
					}
					*/
					featureSet.add(b.getName());
				}
			}
		}

		bw.append("<table width=\"100%\" max-width=\"1024px\">");

		// first row

		String bundles = "Plugin";
		String bundleTitle = "";
		if (feature) {
			bundles = "Feature";
		}
		if (!PairValidator.isNullFilter(includeIUs))
		{
			bundleTitle += " includeIUs = /" + includeIUs + "/";
		}
		if (!PairValidator.isNullFilter(excludeIUs))
		{
			bundleTitle += (!PairValidator.isNullFilter(includeIUs)?", ":"") + " excludeIUs = /" + excludeIUs + "/";
		}
		printErrorLogHeader(bundles);

		StringBuffer headerRow = new StringBuffer("<tr class=\"header\"><td title=\"" + bundleTitle + "\"><b>" + bundles + "</b></td>");
		for (Installation i : installations) {
			headerRow.append("<td><b>" + i.getRootFolderName() + "</b></td>");
		}
		headerRow.append("</tr>\n");
		
		// next rows
		int rowCount = 0;
		int showHeaderEveryXRows = 40;
		for (String s : featureSet) {
			if (rowCount % showHeaderEveryXRows == 0)
			{
				bw.append(headerRow);
			}
			rowCount++;
			bw.append("<tr><td id=\"" + bundles + "_" + s + "\"><a href=\"#"
					+ bundles + "_" + s + "\">" + s + "</a></td>");

			for (Installation i : installations) {
				Bundle bundleFromList = bs.getBundleFromList(
						i.getBundles(feature), s);					
				
				log.debug("Plugin ID: " + s);
				
				String tooltip = " title=\"" + i.getRootFolderName() + "&#10;";
				if (bundleFromList != null) {
					tooltip += bundleFromList.getFullName() + "&#10;";
					String c = "";

					int max = bundleFromList.getMaxSeverity();
					if (max == 0)
						c = " class=\"ok\" ";
					else if (max == 1)
						c = " class=\"info\" ";
					else if (max == 3)
						c = " class=\"warning\" ";
					else if (max == 4)
						c = " class=\"error\" ";
					else if (max == 2) 
						c = " class=\"ignored\" ";

					if (bundleFromList.getIssues().size() > 0) {
						tooltip += bundleFromList.getErrorsAndWarnings();
						tooltip += "\" ";
					} else {
						tooltip += "Nothing suspicious";
						tooltip += "\" ";
						c = " class=\"normal\" ";
					}

					printErrorLogInformation(i, bundleFromList);

					bw.append("<td " + c + " " + tooltip + ">"
							+ getIcons(bundleFromList) + bundleFromList.getVersions().toString()  + "</td>");
				} else {
					bw.append("<td " + "Bundle not available in this version\" class=\"none\">N/A</td>");
				}
			}

			bw.append("</tr>\n");
		}

		bw.append("</table>");

	}

	private String getIcons(Bundle b) {
		String ret = "";
		if (b.getBumped()) {
			String relPath = ReportService.getInstance().getBumpIcoPath();
			ret = "<img src=\"" + relPath + "\"/>";
		}
		return ret;
	}

	private void printErrorLogHeader(String text) {
		log.warn("----------------------------------------------------------------------------------------------------");
		log.warn("Errors found in " + text + ":");
	}

	private void printErrorLogInformation(Installation i, Bundle bundle) {
		for (Issue issue : bundle.getIssues()) {
			if (issue.getSeverity() == Severity.ERROR)
				log.error(bundle.getName() + "," + bundle.getVersion()
						+ " from " + i.getRootFolderName() + " "
						+ issue.getDescription());
		}
	}

	private void printErrorLogFooter() {
		log.warn("----------------------------------------------------------------------------------------------------");
		log.warn("");
	}

	public List<Installation> getInstallations() {
		return installations;
	}

	@Override
	protected void generateBody() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected String getFileName() {
		return "report_detailed.html";
	}

	
	

}
