import genj.gedcom.Gedcom;
import genj.report.CommandLineCapabaleReport;
import genj.report.Report;
import genj.report.ReportLoader;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReportBatches extends Report {

	private static final String MAIN = "startReports";

	public void start(final Gedcom gedcom) throws Exception, InvocationTargetException {

		final File directory = new Report() {
		}.getDirectoryFromUser("directory with configuration directories", "OK");

		if (!directory.exists()) {
			generateConfigurationFiles(directory);
			return;
		}
		for (final File subDir : directory.listFiles()) {
			if (subDir.isDirectory()) {
				try {
					final Class<?> reportClass = Class.forName(subDir.getName());
					if (CommandLineCapabaleReport.class.isAssignableFrom(reportClass)) {
						final Method main = reportClass.getMethod(MAIN, String[].class,PrintWriter.class);
						main.invoke(reportClass.newInstance(), getFileNames(gedcom,subDir), getOut());
					}
				} catch (ClassNotFoundException e) {

				}
			}
		}
	}

	private String[] getFileNames(Gedcom gedcom, final File subDir) {

		final File[] files = subDir.listFiles();
		final String fileNames[] = new String[files.length+1];
		fileNames[0] = "file:"+gedcom.getOrigin().getFile().getAbsolutePath();
		for (int i = 0; i < files.length; i++) {
			fileNames[i+1] = files[i].getPath();
		}
		return fileNames;
	}

	private void generateConfigurationFiles(final File dir) throws Exception {

		final Report[] reports = ReportLoader.getInstance().getReports();
		for (final Report report : reports) {
			if (report instanceof CommandLineCapabaleReport) {
				// TODO other slash for windows
				final String subDir = dir.getPath() + "/" + report.getClass().getName();
				final Method main = report.getClass().getMethod(MAIN, String[].class, PrintWriter.class);
				new File(subDir).mkdirs();
				System.setOut(new PrintStream(subDir + "/config.txt"));
				main.invoke(report, new String[] {}, getOut());
			}
		}
	}
}
