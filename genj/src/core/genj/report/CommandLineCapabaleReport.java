package genj.report;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import genj.io.GedcomFormatException;
import genj.io.GedcomIOException;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.option.PropertyOption;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.DialogHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.List;

import com.ibm.icu.text.MessageFormat;

public class CommandLineCapabaleReport extends Report {

	private static final Resources RESOURCES = Resources.get(CommandLineCapabaleReport.class);
	private static final String ENTITY_KEY = "reports.for.entities";
	private PrintWriter printWriter;

	/**
	 * To be called by the <code>public static void main(String[] args)</code>
	 * method of the implementing class.
	 * 
	 * @param args
	 *            First argument: url of gedcom file.<br>
	 *            Subsequent arguments: configuration files.<br>
	 *            Without any argument a sample configuration file is generated.
	 * @throws Exception
	 */
	public void startReports(final String[] args) throws Throwable {

		if (args == null || args.length == 0) {
			showOptions();
			return;
		}

		// to override getOut()
		printWriter = new PrintWriter(System.out);

		final Gedcom gedcom = readGedcom(args[0]);
		if (args.length == 1 && accepts(gedcom) != null) {

			// run a gedcom based report with the hard coded default options
			start(gedcom);
		}
		for (int i = 1; i < args.length; i++) {
			for (final String id : loadOptions(args[i])) {
				if (id.toLowerCase().equals("gedcom")) {
					if (accepts(gedcom) == null)
						inputError("report.requires.entity",getName());
					else
						start(gedcom);
				} else {
					final Entity entity = gedcom.getEntity(id);
					if (entity == null)
						inputError("report.did.not.find",getName(),id,args[0]);
					else if (accepts(entity) == null)
						inputError("report.does.not.support",getName());
					else
						start(entity);
				}
			}
		}
	}
	
	private void inputError(final String key, final String...args  ){
		final String msg = RESOURCES.getString(key);
		System.err.println(MessageFormat.format(msg,args));
	}

	private String[] loadOptions(final String fileName) throws IOException, FileNotFoundException {

		final Resources options = new Resources(new FileInputStream(fileName));
		final List<PropertyOption> props = PropertyOption.introspect(this, true);
		options.load(new FileInputStream(fileName));
		for (final PropertyOption prop : props) {
			final String prefix = prop.getCategory() == null ? "" : prop.getCategory() + ".";
			final String value = options.getString(prefix + prop.getProperty());
			if (value != null) {
				prop.setValue(value);
			}
		}
		return (options.getString(ENTITY_KEY)).split("[^A-Za-z0-9]+");
	}

	private void showOptions() {

		showReportProperties(getName(), getCategory(), getAuthor(), getVersion(), getLastUpdate());
		showSupportedEntities(new Indi(), new Fam("FAM", ""), new Media("OBJE", ""), new Note("NOTE", ""), new Submitter("SUBM", ""), new Source("SOUR", ""));
		final List<PropertyOption> props = PropertyOption.introspect(this, true);
		String lastPrefix = "";
		for (final PropertyOption prop : props) {
			final String prefix = prop.getCategory() == null ? "" : prop.getCategory() + ".";
			if (!prefix.equals(lastPrefix)) {
				System.out.println();
				System.out.println("############ " + translateOption(prop.getCategory()));
				lastPrefix = prefix;
			}
			System.out.println();
			System.out.println("# " + translateOption(prop.getProperty()));
			System.out.println(prefix + prop.getProperty() + " = " + prop.getValue());
		}
	}

	private void showReportProperties(final String... names) {
		for (final String name : names)
			if (name != null)
				System.out.println("# " + name);
	}

	private void showSupportedEntities(final Entity... entities) {

		final Gedcom gedcom = new Gedcom();
		System.out.println("#");
		for (final Entity entity : entities)
			if (accepts(entity) != null)
				System.out.println("# " + gedcom.getNextAvailableID(entity.getTag()) + " " + entity.getPropertyName());
		System.out.println("");
		System.out.println("# " + RESOURCES.getString(ENTITY_KEY));
		System.out.print(ENTITY_KEY + " = ");
		for (final Entity entity : entities)
			if (accepts(entity) != null)
				System.out.print(" " + gedcom.getNextAvailableID(entity.getTag()));
		if (accepts(gedcom) != null)
			System.out.print(" gedcom");
		System.out.println("");
	}

	public PrintWriter getOut() {

		// GUI version
		if (printWriter == null)
			return super.getOut();

		// command line version
		return printWriter;
	}

	private static Gedcom readGedcom(final String url) throws MalformedURLException, GedcomIOException, GedcomFormatException, IOException {

		final GedcomReaderContext context = new GedcomReaderContext() {
			public String getPassword() {
				return DialogHelper.openDialog(url, DialogHelper.QUESTION_MESSAGE, "password", "", this);
			}

			public void handleWarning(final int line, final String warning, final Context context) {
				System.err.println(line + ": " + warning);
			}
		};
		return GedcomReaderFactory.createReader(Origin.create(url), context).read();
	}
}
