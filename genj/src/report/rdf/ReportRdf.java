package rdf;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.io.GedcomFormatException;
import genj.io.GedcomIOException;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.report.Report;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class ReportRdf extends Report {

	public class UriFormats {
		public String indi = "http://my.domain.com/gedcom/{0}.html";
		public String fam = "http://my.domain.com/gedcom/{0}.html";
		public String note = "http://my.domain.com/gedcom/{0}.html";
		public String obje = "http://my.domain.com/gedcom/{0}.html";
		public String repo = "http://my.domain.com/gedcom/{0}.html";
		public String sour = "http://my.domain.com/gedcom/{0}.html";
		public String subm = "http://my.domain.com/gedcom/{0}.html";

		private Map<String, String> getURIs() {
			Map<String, String> uris;
			uris = new HashMap<String, String>();
			uris.put("INDI", indi);
			uris.put("FAM", fam);
			uris.put("NOTE", note);
			uris.put("OBJE", obje);
			uris.put("REPO", repo);
			uris.put("SOUR", sour);
			uris.put("SUBM", subm);
			return uris;
		}
	}

	public class DisplayFormats {
		public String styleSheet = "http://www.w3.org/TR/rdf-sparql-XMLres/result-to-html.xsl";
		public String asText = "query-result.txt";
		public String asXml = "query-result.xml";
		public String converted = "converted-gedcom.n3";
		public boolean askForOverwrite = true;
	};

	private enum Extension {
		ttl("TURTLE"), n3("N3"), nt("N-TRIPPLE"), rdf("RDF/XML-ABBREV");
		// RDF/XML-ABBREV is less verbose, use RDF/XML for large models
		private final String language;

		private Extension(final String language) {
			this.language = language;
		}

		public String getLanguage() {
			return language;
		}
	}

	public class Queries {
		public String qGedcom = "";
		public String qFam = "";
		public String qIndi = "";
		public String qRules = "";
	}

	public UriFormats uriFormats = new UriFormats();
	public DisplayFormats displayFormats = new DisplayFormats();
	public Queries queries = new Queries();

	/** main */
	public void start(final Indi indi) throws IOException {
		final String query = getQueryPart(queries.qIndi, "query.indi");
		run(convert(indi.getGedcom()), String.format(query, indi.getId()));
	}

	/** main */
	public void start(final Fam fam) throws IOException {
		final String query = getQueryPart(queries.qFam, "query.fam");
		run(convert(fam.getGedcom()), String.format(query, fam.getId()));
	}

	/** main */
	public void start(final Gedcom gedcom) throws IOException {
		final String query = getQueryPart(queries.qGedcom, "query.gedcom");
		run(convert(gedcom), query);
	}

	/** main using default query, intended for command line version */
	public void start(final Entity entity) throws IOException {
		final String name = entity.getClass().getSimpleName().toLowerCase();
		final String query = getQueryPart("", "query."+name);
		run(convert(entity.getGedcom()), String.format(query, entity.getId()));
	}

	public void run(final InfModel model, final String query) throws FileNotFoundException, IOException {

		final String fullQuery = assembleQuery(query, model);

		if (displayFormats.asXml.trim().length() > 0) {
			final ResultSet resultSet = execSelect(fullQuery, model);
			if (displayFormats.styleSheet.trim().length() > 0)
				write(displayFormats.asXml, ResultSetFormatter.asXMLString(resultSet, displayFormats.styleSheet));
			else
				write(displayFormats.asXml, ResultSetFormatter.asXMLString(resultSet));
		}
		if (displayFormats.asText.trim().length() > 0) {
			// can't reuse a previously consumed resultset
			final ResultSet resultSet = execSelect(fullQuery, model);
			write(displayFormats.asText, ResultSetFormatter.asText(resultSet));
		}
		writeConvertedGedcom(model, displayFormats.converted);
	}

	private InfModel convert(final Gedcom gedcom) throws FileNotFoundException, IOException {
		final SemanticGedcomUtil util = new SemanticGedcomUtil();
		progress("converting");
		final Model rawModel = util.toRdf(gedcom, uriFormats.getURIs());
		progress("applying rules");
		final InfModel model = util.getInfModel(getQueryPart(queries.qRules, "query.rules"));
		progress("rules completed");
		return model;
	}

	private void progress(final String string) {
		final DateFormat dateFormat = new SimpleDateFormat(" HH:mm:ss.SSS ");
		final Date date = new Date();
		getOut().println("#########" + " " + dateFormat.format(date) + string);
		getOut().flush();
	}

	private void writeConvertedGedcom(final InfModel model, final String fileName) throws FileNotFoundException {
		if (fileName.trim().length() == 0)
			return;
		final String language;
		final String ext = fileName.replaceAll(".*\\.", "").toLowerCase();
		try {
			language = Extension.valueOf(ext).getLanguage();
		} catch (final IllegalArgumentException exception) {
			getOut().println(MessageFormat.format(getResources().getString("extension.error"), ext));
			return;
		}
		if (fileName.startsWith("#")) {
			model.write(getOut(), language);
			return;
		}
		final File file = new File(fileName);
		if (!doNotOverwrite(file)) {
			writeProgress(file);
			model.write(new FileOutputStream(file), language);
		}
	}

	private void write(final String name, final String content) throws IOException {
		if (name.equals("#")) {
			getOut().println("############################################");
			getOut().println(content);
			getOut().flush();
			return;
		}
		final File file = new File(name);
		if (doNotOverwrite(file))
			return;
		writeProgress(file);
		new FileOutputStream(name).write(content.getBytes());
	}

	private boolean doNotOverwrite(final File file) {
		if (file.exists() && displayFormats.askForOverwrite) {
			final String format = getResources().getString("overwrite.question");
			final String prompt = MessageFormat.format(format, file.getAbsoluteFile());
			final int rc = DialogHelper.openDialog(getName(), DialogHelper.WARNING_MESSAGE, prompt, Action2.yesNo(), null);
			return (rc != 0);
		}
		return false;
	}

	private void writeProgress(final File file) {
		final String format = getResources().getString("progress.writing");
		final String prompt = MessageFormat.format(format, file.getAbsoluteFile());
		getOut().println(prompt);
		getOut().flush();
	}

	private ResultSet execSelect(final String query, final InfModel model) throws FileNotFoundException, IOException {
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
		final ResultSet resultSet = queryExecution.execSelect();
		progress("query executed");
		return resultSet;
	}

	private String assembleQuery(final String query, final InfModel model) throws IOException, FileNotFoundException, UnsupportedEncodingException {
		final StringBuffer fullQuery = assemblePrefixes(model);
		fullQuery.append(getResources().getString("query.function.prefixes"));
		fullQuery.append(query);
		getOut().println(fullQuery);
		getOut().flush();
		return fullQuery.toString();
	}

	private String getQueryPart(final String queryPart, final String key) throws FileNotFoundException, IOException {
		final File file = new File(queryPart);
		if (file.isFile()) {
			final byte[] buffer = new byte[(int) file.length()];
			new RandomAccessFile(queryPart, "r").readFully(buffer);
			return new String(buffer, "UTF-8");
		} else if (queryPart.trim().equals("")) {
			return getResources().getString(key + ".1");
		} else if (queryPart.trim().matches("[0-9]*")) {
			return getResources().getString(key + "." + queryPart.trim());
		}
		return queryPart;
	}

	private static StringBuffer assemblePrefixes(final InfModel model) throws FileNotFoundException, IOException {
		final Map<String, String> prefixMap = model.getNsPrefixMap();
		final StringBuffer query = new StringBuffer();
		for (final Object prefix : prefixMap.keySet().toArray())
			query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(), prefixMap.get(prefix).toString()));
		return query;
	}

	public static void main(final String args[]) throws Throwable {

		final Gedcom gedcom = readGedcom(args[0]);
		final ReportRdf report = createReport();
		if (args.length == 1) {
			// run a gedcom based report with the hard coded default options
			report.start(gedcom);
			return;
		}
		for (int i = 1; i < args.length; i++) {
			// final Properties options = new Properties();
			// options.load(new FileInputStream(args[i]));
			// options.getProperty("context.id");
			// TODO replace report defaults from options
			final String id = args[i];
			if (id == null || id.trim().length() == 0)
				report.start(gedcom);
			else {
				Entity entity = gedcom.getEntity(id);
				if (entity == null)
					System.err.println(id + " not found in "+args[0]);
				else
					report.start(entity);
			}
		}
	}

	private static ReportRdf createReport() {
		return new ReportRdf() {

			private final PrintWriter printWriter = new PrintWriter(System.out);
			private final Resources resources = new Resources(ReportRdf.class.getResourceAsStream(ReportRdf.class.getSimpleName() + ".properties"));

			public PrintWriter getOut() {
				return printWriter;
			}

			protected Resources getResources() {
				return resources;
			}
		};
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
