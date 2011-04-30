package rdf;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
		/** TODO rather read the queries from a file? */
		public String qGedcom = "SELECT ?indi ?name { ?indi a t:INDI ; p:NAME [p:value ?name] .}";
		public String qFam = "SELECT ?indi ?name { ?indi a t:INDI ; p:FAMC ?fam ; p:NAME [p:value ?name] . ?fam p:id '%s' .}";
		public String qIndi = "SELECT ?indi ?name { ?indi a t:INDI ; p:id '%s' ; p:NAME [p:value ?name] .}";
	}

	public UriFormats uriFormats = new UriFormats();
	public DisplayFormats displayFormats = new DisplayFormats();
	public Queries queries = new Queries();

	/** main */
	public void start(final Indi indi) throws IOException {
		run(indi.getGedcom(), String.format(queries.qIndi, indi.getId()));
	}

	/** main */
	public void start(final Fam fam) throws IOException {
		run(fam.getGedcom(), String.format(queries.qFam, fam.getId()));
	}

	/** main */
	public void start(final Gedcom gedcom) throws IOException {
		run(gedcom, queries.qGedcom);
	}

	public void run(final Gedcom gedcom, final String query) throws FileNotFoundException, IOException {

		final Model model = new SemanticGedcomUtil().toRdf(gedcom, uriFormats.getURIs());
		model.read(rulesAsStream(), null, "N3");

		final String fullQuery = assemblePrefixes(model).append(query).toString();
		getOut().println(fullQuery);

		if (displayFormats.asXml.trim().length() > 0) {
			if (displayFormats.styleSheet.trim().length() > 0)
				write(displayFormats.asXml, ResultSetFormatter.asXMLString(execSelect(fullQuery, model), displayFormats.styleSheet));
			else
				write(displayFormats.asXml, ResultSetFormatter.asXMLString(execSelect(fullQuery, model)));
		}
		if (displayFormats.asText.trim().length() > 0) {
			write(displayFormats.asText, ResultSetFormatter.asText(execSelect(fullQuery, model)));
		}
		if (displayFormats.converted.trim().length() != 0) {
			final String language;
			final String ext = displayFormats.converted.replaceAll(".*\\.", "").toLowerCase();
			try {
				language = Extension.valueOf(ext).getLanguage();
			} catch (final IllegalArgumentException exception) {
				getOut().write(MessageFormat.format(getResources().getString("extension.error"), ext));
				return;
			}
			if (displayFormats.asText.startsWith("#")) {
				model.write(getOut(), language);
				return;
			}
			final File file = new File(displayFormats.converted);
			if (!doNotOverwrite(file)) {
				writeProgress(file);
				model.write(new FileOutputStream(displayFormats.converted), language);
			}
		}
	}

	private void write(final String name, final String content) throws IOException {
		if (displayFormats.asText.equals("#")) {
			getOut().write("############################################");
			getOut().write(content);
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

	private static ResultSet execSelect(final String query, final Model model) {
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
		return queryExecution.execSelect();
	}

	private StringBuffer assemblePrefixes(final Model model) throws FileNotFoundException, IOException {
		final Map<String, String> prefixMap = model.getNsPrefixMap();
		final StringBuffer query = new StringBuffer();
		for (final Object prefix : prefixMap.keySet().toArray())
			query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(), prefixMap.get(prefix).toString()));
		query.append(getResources().getString("queryFunctions"));
		return query;
	}

	private static InputStream rulesAsStream() throws UnsupportedEncodingException {
		// TODO see http://tech.groups.yahoo.com/group/jena-dev/message/42968
		// TODO adjust the rules to our model
		final String rules = "";// getResources().getString("queryRules");
		final byte[] bytes = rules.getBytes("UTF-8");
		return new ByteArrayInputStream(bytes);
	}
}
