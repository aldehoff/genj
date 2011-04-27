package rdf;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
		public boolean showQuery = true;
		public boolean showAsText = true;
		public boolean showAsXml = true;
		public String rdfFormat = "N3";
	};

	public class Queries {
		/** TODO rather read the queries from a file? */
		public String qGedcom = "SELECT ?indi ?name { ?indi a t:INDI ; p:NAME [p:value ?name] .}";
		public String qFam = "SELECT ?indi ?name { ?indi a t:INDI ; p:FAMC ?fam ; p:NAME [p:value ?name] . ?fam p:id '%s' .}";
		public String qIndi = "SELECT ?indi ?name { ?indi a t:INDI ; p:id '%s' ; p:NAME [p:value ?name] .}";
	}

	public UriFormats uriFormats = new UriFormats();
	public DisplayFormats displayFormats = new DisplayFormats();
	public Queries queries = new Queries();

	private Model model;

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
		getOut().println("started conversion");
		model = new SemanticGedcomUtil().toRdf(gedcom, uriFormats.getURIs());
		model.read(rulesAsStream(), null, "N3");
		final String fullQuery = assemblePrefixes().append(query).toString();
		getOut().println("finished conversion");

		if (displayFormats.rdfFormat != null && displayFormats.rdfFormat.trim().length() > 0) {
			separate();
			model.write(getOut(), displayFormats.rdfFormat);
		}
		if (displayFormats.showQuery)
			show(fullQuery);
		if (displayFormats.showAsXml)
			show(ResultSetFormatter.asXMLString(execSelect(fullQuery)));
		if (displayFormats.showAsText)
			show(ResultSetFormatter.asText(execSelect(fullQuery)));
		if (displayFormats.styleSheet != null && displayFormats.styleSheet.trim().length() > 0)
			show(ResultSetFormatter.asXMLString(execSelect(fullQuery), displayFormats.styleSheet));
	}

	private void show(final String result) {
		separate();
		getOut().println(result);
	}

	private void separate() {
		getOut().println("########################################################");
	}

	private ResultSet execSelect(final String query) {
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
		return queryExecution.execSelect();
	}

	public StringBuffer assemblePrefixes() throws FileNotFoundException, IOException {
		final Map<String, String> prefixMap = model.getNsPrefixMap();
		final StringBuffer query = new StringBuffer();
		for (final Object prefix : prefixMap.keySet().toArray())
			query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(), prefixMap.get(prefix).toString()));
		query.append(getResources().getString("queryFunctions"));
		return query;
	}

	private InputStream rulesAsStream() throws UnsupportedEncodingException {
		// TODO see http://tech.groups.yahoo.com/group/jena-dev/message/42968
		// TODO adjust the rules to our model
		final String rules = "";// getResources().getString("queryRules");
		final byte[] bytes = rules.getBytes("UTF-8");
		return new ByteArrayInputStream(bytes);
	}
}
