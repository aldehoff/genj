package rdf;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.report.BatchCompatible;
import genj.report.Report;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class ReportRdf extends Report implements BatchCompatible {

	public class Queries {
		public String qGedcom = "";
		public String qFam = "";
		public String qIndi = "";
		public String qMedia = "";
		public String qNote = "";
		public String qRepository = "";
		public String qSource = "";
		public String qSubmitter = "";
		public String qRules = "";
	}

	public class DisplayFormats {
		public String styleSheet = "http://www.w3.org/TR/rdf-sparql-XMLres/result-to-html.xsl";
		public String baseFileName = "rdf";
		public String extensions = "n3 xml";
		public boolean askForOverwrite = true;
	};

	public class UriFormats {
		public String fam = "http://my.domain.com/gedcom/{0}.html";
		public String indi = "http://my.domain.com/gedcom/{0}.html";
		public String obje = "http://my.domain.com/gedcom/{0}.html";
		public String note = "http://my.domain.com/gedcom/{0}.html";
		public String repo = "http://my.domain.com/gedcom/{0}.html";
		public String sour = "http://my.domain.com/gedcom/{0}.html";
		public String subm = "http://my.domain.com/gedcom/{0}.html";

		private Map<String, String> getURIs() {
			Map<String, String> uris;
			uris = new HashMap<String, String>();
			uris.put("FAM", fam);
			uris.put("INDI", indi);
			uris.put("OBJE", obje);
			uris.put("NOTE", note);
			uris.put("REPO", repo);
			uris.put("SOUR", sour);
			uris.put("SUBM", subm);
			return uris;
		}
	}

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

	public UriFormats uriFormats = new UriFormats();
	public DisplayFormats output = new DisplayFormats();
	public Queries queries = new Queries();

	public void start(final Gedcom gedcom) throws IOException {

		final String query = getQuery(queries.qGedcom, "query.gedcom");
		if (optionsOk(query))
			run("", convert(gedcom), query);
	}

	public void start(final Entity entity) throws IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		// use reflection to get the query option for the actual type of entity
		final String name = entity.getClass().getSimpleName();
		final String resourceKeyBase = "query." + name.toLowerCase();
		final String value = (String) queries.getClass().getField("q" + name).get(queries);

		final String query = getQuery(value, resourceKeyBase);
		if (optionsOk(query))
			run(entity.getId(), convert(entity.getGedcom()), String.format(query, entity.getId()));
	}

	public void run(final String id, final InfModel model, final String query) throws FileNotFoundException, IOException {

		final String fullQuery = assembleQuery(query, model);

		for (final String extension : output.extensions.toLowerCase().split("[^A-Za-z0-9]+")) {
			final String fileName = output.baseFileName + "-" + id + "." + extension;
			if (extension.equals("xml")) {
				final ResultSet resultSet = execSelect(fullQuery, model);
				if (resultSet != null) {
					if (output.styleSheet.trim().length() > 0)
						write(fileName, ResultSetFormatter.asXMLString(resultSet, output.styleSheet));
					else
						write(fileName, ResultSetFormatter.asXMLString(resultSet));
				}
			} else if (extension.equals("txt")) {
				final ResultSet resultSet = execSelect(fullQuery, model);
				if (resultSet != null)
					write(fileName, ResultSetFormatter.asText(resultSet));
			} else {
				writeConvertedGedcom(model, fileName);
			}
		}
	}

	private boolean optionsOk(final String query) {

		boolean ok = true;
		output.baseFileName = initFile(output.baseFileName, translate("base.output.file"));
		if (output.baseFileName == null || output.baseFileName.trim().length() == 0) {
			println("no output file name");
			ok = false;
		}
		if (output.extensions.trim().length() == 0) {
			println("no output extension");
			ok = false;
		}
		if (query.trim().length() == 0) {
			println("no query");
			ok = false;
		}
		if (!query.trim().toLowerCase().startsWith("select ")) {
			println("no valid select query: " + query);
			ok = false;
		}
		return ok;
	}

	private InfModel convert(final Gedcom gedcom) throws FileNotFoundException, IOException {

		final SemanticGedcomUtil util = new SemanticGedcomUtil();
		final String query = getQuery(queries.qRules, "query.rules");

		// TODO cash converted model in a map
		final String cashKey = Arrays.deepToString(uriFormats.getURIs().values().toArray()) + query;

		progress("converting");
		final Model rawModel = util.toRdf(gedcom, uriFormats.getURIs());
		progress("applying rules");
		final InfModel model = util.getInfModel(query);
		progress("rules completed");
		return model;
	}

	private void progress(final String string) {

		final DateFormat dateFormat = new SimpleDateFormat(" HH:mm:ss.SSS ");
		final Date date = new Date();
		getOut().println(dateFormat.format(date) + string);
		getOut().flush();
	}

	private void writeConvertedGedcom(final InfModel model, final String fileName) throws FileNotFoundException {

		final String language;
		final String ext = fileName.replaceAll(".*\\.", "").toLowerCase();
		try {
			language = Extension.valueOf(ext).getLanguage();
		} catch (final IllegalArgumentException exception) {
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

	private void write(final String fileName, final String content) throws IOException {
		if (fileName.startsWith("#")) {
			getOut().println("############################################");
			getOut().println(content);
			getOut().flush();
			return;
		}
		final File file = new File(fileName);
		if (doNotOverwrite(file))
			return;
		writeProgress(file);
		new FileOutputStream(fileName).write(content.getBytes());
	}

	private boolean doNotOverwrite(final File file) {
		if (file.exists() && output.askForOverwrite) {
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

	private ResultSet execSelect(final String query, final InfModel model) {
		try {
			final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
			final ResultSet resultSet = queryExecution.execSelect();
			progress("query executed");
			flush();
			return resultSet;
		} catch (QueryParseException exception) {
			println(exception.getMessage());
			println(query);
			flush();
		}
		return null;
	}

	private String assembleQuery(final String query, final InfModel model) throws IOException, FileNotFoundException, UnsupportedEncodingException {
		final StringBuffer fullQuery = assemblePrefixes(model);
		fullQuery.append(getResources().getString("query.function.prefixes"));
		fullQuery.append(query);
		return fullQuery.toString();
	}

	private String getQuery(final String queryPart, final String resourceKeyBase) throws FileNotFoundException, IOException {
		final File file = new File(queryPart);
		if (file.isFile()) {
			final byte[] buffer = new byte[(int) file.length()];
			new RandomAccessFile(queryPart, "r").readFully(buffer);
			return new String(buffer, "UTF-8");
		} else if (queryPart.trim().equals("")) {
			return getResources().getString(resourceKeyBase + ".1");
		} else if (queryPart.trim().matches("[0-9]*")) {
			return getResources().getString(resourceKeyBase + "." + queryPart.trim());
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
}
