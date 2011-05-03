package rdf;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
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

	public enum Extension {
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
		final String query = getQueryPart(queries.qIndi,"query.indi");
		run(indi.getGedcom(), String.format(query, indi.getId()));
	}

	/** main */
	public void start(final Fam fam) throws IOException {
		final String query = getQueryPart(queries.qFam,"query.fam");
		run(fam.getGedcom(), String.format(query, fam.getId()));
	}

	/** main */
	public void start(final Gedcom gedcom) throws IOException {
		final String query = getQueryPart(queries.qGedcom,"query.gedcom");
		run(gedcom, query);
	}

	public void run(final Gedcom gedcom, final String query) throws FileNotFoundException, IOException {

		final SemanticGedcomUtil util = new SemanticGedcomUtil();
		final Model rawModel = util.toRdf(gedcom, uriFormats.getURIs());
		final Model model = util.getInfModel(getQueryPart(queries.qRules,"query.rules"));
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

	private void writeConvertedGedcom(final Model model, final String fileName) throws FileNotFoundException {
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
	}

	private ResultSet execSelect(final String query, final Model model) throws FileNotFoundException, IOException {
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
		return queryExecution.execSelect();
	}

	private String assembleQuery(final String query, final Model model) throws IOException, FileNotFoundException, UnsupportedEncodingException {
		final StringBuffer fullQuery = assemblePrefixes(model);
		fullQuery.append(getResources().getString("query.function.prefixes"));
		fullQuery.append(query);
		getOut().println(fullQuery);
		return fullQuery.toString();
	}

	private String getQueryPart(final String queryPart, final String key) throws FileNotFoundException, IOException {
		final File file = new File(queryPart);
		if (file.isFile()) {
			final byte[] buffer = new byte[(int) file.length()];
			new RandomAccessFile(queryPart, "r").readFully(buffer);
			return new String(buffer, "UTF-8");
		} else if (queryPart.trim().equals("")) {
			return getResources().getString(key+".1");
		} else if (queryPart.trim().matches("[0-9]*")) {
			return getResources().getString(key +"."+ queryPart.trim());
		}
		return queryPart;
	}

	private static StringBuffer assemblePrefixes(final Model model) throws FileNotFoundException, IOException {
		final Map<String, String> prefixMap = model.getNsPrefixMap();
		final StringBuffer query = new StringBuffer();
		for (final Object prefix : prefixMap.keySet().toArray())
			query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(), prefixMap.get(prefix).toString()));
		return query;
	}
}
