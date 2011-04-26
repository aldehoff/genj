package rdf;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

import java.io.IOException;
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

	public class QueryFormats {
		public String styleSheet = "https://develop01.dans.knaw.nl/svn/mixed/alfalab/annotations/trunk/src/main/resources/query-playground.xsl";
		public boolean showQuery = true;
		public boolean showAsText = true;
		public boolean showAsXml = true;
	};

	private static final String SAMPLE_QUERY = "SELECT ?indi ?name { ?indi a t:INDI ; p:NAME [p:value ?name] .}";
	
	public String rdfFormat = "N3";
	public UriFormats uriFormats = new UriFormats();
	public QueryFormats queryFormats = new QueryFormats();

	private Model model;

	/** main */
	public void start(final Indi indi) throws IOException {
		// TODO insert a line into the query like: "LET (?rootIndiId := "+indi.getId()+")"
		run(indi.getGedcom(),SAMPLE_QUERY);
	}

	/** main */
	public void start(final Fam fam) throws IOException {
		// TODO insert a line into the query like: "LET (?rootFamId := "+fam.getId()+")"
		run(fam.getGedcom(),SAMPLE_QUERY);
	}

	/** main */
	public void start(final Gedcom gedcom) throws IOException {
		run(gedcom, SAMPLE_QUERY);
	}

	public void run(final Gedcom gedcom, String query) {
		model = new SemanticGedcomUtil().toRdf(gedcom, new UriFormats().getURIs());

		if (rdfFormat != null && rdfFormat.trim().length() > 0)
			model.write(getOut(), rdfFormat);

		final String fullQuery = assembleQuery(query);

		if (queryFormats.showAsXml) {
			separate();
			getOut().println(ResultSetFormatter.asXMLString(execSelect(fullQuery)));
		}
		if (queryFormats.showAsText) {
			separate();
			getOut().println(ResultSetFormatter.asText(execSelect(fullQuery)));
		}
		if (queryFormats.styleSheet != null && queryFormats.styleSheet.trim().length() > 0) {
			separate();
			getOut().println(ResultSetFormatter.asXMLString(execSelect(fullQuery), queryFormats.styleSheet));
		}
	}
	
	private void separate() {
		getOut().println("---------------------------------------------");
	}

	private ResultSet execSelect(final String query) {
		final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
		return queryExecution.execSelect();
	}

	private String assembleQuery(final String queryString) {
		final StringBuffer query = assemblePrefixes();
		query.append(queryString);
		if (queryFormats.showQuery) {
			separate();
			getOut().println(query.toString());
		}
		return query.toString();
	}

	public StringBuffer assemblePrefixes() {
		Map<String, String> prefixMap = model.getNsPrefixMap();
		StringBuffer query = new StringBuffer();
		for (Object prefix : prefixMap.keySet().toArray())
			query.append(String.format("PREFIX %s: <%s> \n", prefix.toString(), prefixMap.get(prefix).toString()));
		query.append("PREFIX  fn: <http://www.w3.org/2005/xpath-functions#> \n");
		query.append("PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> \n");
		query.append("PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#> \n");
		return query;
	}
}
