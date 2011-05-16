import genj.gedcom.Gedcom;
import genj.report.CommandLineCapableReport;
import genj.report.Report;
import genj.report.ReportLoader;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReportBatches extends Report {

	private static Map<String, CommandLineCapableReport> reportMap;
	private static Method main;

	public String configDir = "";

	public void start(final Gedcom gedcom) throws Exception {

		final File directory;
		if (configDir.trim().length() == 0) {
			directory = new Report() {
			}.getDirectoryFromUser(getResources().getString("configDir.dialog.title"), getResources().getString("configDir.dialog.button"));
			if (directory == null)
				return;
			configDir = directory.getAbsolutePath();
		} else
			directory = new File(configDir);

		if (!directory.exists()) {
			generateConfigurationFiles(directory);
			return;
		}
		for (final File subDir : directory.listFiles()) {
			final String name = subDir.getName();
			if (subDir.isDirectory() && getReportMap().containsKey(name)) {
				getMain().invoke(getReportMap().get(name), createArgs(gedcom, subDir), getOut());
			}
		}
	}

	private String[] createArgs(Gedcom gedcom, final File subDir) {

		final File[] files = subDir.listFiles();
		final String fileNames[] = new String[files.length + 1];
		fileNames[0] = "file:" + gedcom.getOrigin().getFile().getAbsolutePath();
		for (int i = 0; i < files.length; i++) {
			fileNames[i + 1] = files[i].getPath();
		}
		return fileNames;
	}

	private void generateConfigurationFiles(final File dir) throws Exception {

		final Report[] reports = ReportLoader.getInstance().getReports();
		for (final String reportName : getReportMap().keySet()) {
			final CommandLineCapableReport report = reportMap.get(reportName);
			// TODO other slash for windows
			final String subDir = dir.getPath() + File.separator + reportName;
			new File(subDir).mkdirs();
			System.setOut(new PrintStream(subDir + File.separator + "config.txt"));
			getMain().invoke(report, new String[] {}, getOut());
		}
	}

	private static Method getMain() throws NoSuchMethodException {
		if (main != null)
			return main;
		main = CommandLineCapableReport.class.getMethod("startReports", String[].class, PrintWriter.class);
		return main;
	}

	private static Map<String, CommandLineCapableReport> getReportMap() {
		if (reportMap != null)
			return reportMap;
		reportMap = new HashMap<String, CommandLineCapableReport>();
		for (final Report report : ReportLoader.getInstance().getReports()) {
			if (report instanceof CommandLineCapableReport) {
				reportMap.put(report.getClass().getName(), (CommandLineCapableReport) report);
			}
		}
		return reportMap;
	}
}
