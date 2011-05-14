package genj.report;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.io.GedcomFormatException;
import genj.io.GedcomIOException;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.util.Origin;
import genj.util.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Properties;

public class CommandLineCapabaleReport extends Report {

	private PrintWriter printWriter;
	private Resources resources;

	/**
	 * To be called by the <code>public void main(String[] args)</code> method
	 * of the implementing class.
	 * 
	 * @param args
	 *            first argument: is url for gedcom file. Subsequent arguments:
	 *            Entity-IDs and/or file names. The files content override
	 *            previously set default options.
	 * @throws Exception
	 */
	public void startReports(final String[] args) throws Throwable {

		for (Field f:getClass().getFields()){
			System.out.println(f.getName());
		}
		
		if (args == null || args.length == 0)
			throw new IllegalArgumentException();

		printWriter = new PrintWriter(System.out);
		resources = new Resources(getClass().getResourceAsStream(getClass().getSimpleName() + ".properties"));

		final Gedcom gedcom = readGedcom(args[0]);
		if (args.length == 1) {
			// run a gedcom based report with the hard coded default options
			start(gedcom);
		}
		for (int i = 1; i < args.length; i++) {
			if (new File(args[i].trim()).isFile()) {
				loadOptions(args[i]);
			} else {
				final String id = args[i].trim();
				if (id == null || id.length() == 0) {
					start(gedcom);
				} else {
					final Entity entity = gedcom.getEntity(id);
					if (entity == null)
						System.err.println(id + " not found in " + args[0]);
					else
						start(entity);
				}
			}
		}
	}

	private void loadOptions(final String fileName) throws IOException, FileNotFoundException {
		final Properties options = new Properties();
		options.load(new FileInputStream(fileName));
		for (final String key: options.keySet().toArray(new String[options.size()])){
			final String value = options.getProperty(key);
			// TODO
			System.err.println(fileName+" "+key);
		}
		System.err.println("loading options not yet implemented, continuing with defaults");
	}

	public PrintWriter getOut() {

		// called by the GUI?
		if (printWriter == null)
			return super.getOut();

		// command line version
		return printWriter;
	}

	private static Gedcom readGedcom(final String url) throws MalformedURLException, GedcomIOException, GedcomFormatException, IOException {
		final GedcomReaderContext context = new GedcomReaderContext() {
			public String getPassword() {
				throw new UnsupportedOperationException("passwords not implemented for command line reports");
			}

			public void handleWarning(final int line, final String warning, final Context context) {
				System.err.println(line + ": " + warning);
			}
		};
		return GedcomReaderFactory.createReader(Origin.create(url), context).read();
	}
}
