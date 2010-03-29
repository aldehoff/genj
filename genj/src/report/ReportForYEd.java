/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Options;
import genj.report.Report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class ReportForYEd extends Report {

	public int famImageWidth = 100;
	public int famImageHeight = 150;
	public int indiImageWidth = 100;
	public int indiImageHeight = 150;
	public String indiUrl = getString("indiUrlDefault");
	public String familyUrl = getString("familyUrlDefault");
	public boolean showDates = true;
	public boolean showPlaces = true;
	public boolean showOccupation = true;
	public String imageExtensions = "jpg jpeg gif png";
	public String place_display_format = "";

	private final String XML_LINK_CONTAINER = getString("LinkContainer");
	private final String XML_POPUP_CONTAINER = getString("PopUpContainer");
	private final String XML_FAMILY = getString("FamilyNode");
	private final String XML_INDI = getString("IndiNode");
	private final String XML_EDGE = getString("Edge");
	private final String XML_HEAD = getString("XmlHead");
	private final String XML_TAIL = getString("XmlTail");

	private static final String INDI_COLORS[] = createIndiColors();
	private static final Options OPTIONS = Options.getInstance();

	private int edgeCount = 0;
	private File reportFile;

	/** main */
	public void start(final Gedcom gedcom) throws IOException {

//		final List<? extends Entity> indis = Arrays.asList(gedcom.getEntities(Gedcom.INDI, "INDI:OBJE"));
//		final Collection<Fam> fams = new HashSet<Fam>();
//		for (Indi indi:indis){
//			
//		}
		generateReport(gedcom.getFamilies(), gedcom.getIndis());
	}

	/** main */
	public void start(final Indi indi) throws IOException {

		final Collection<Indi> indis = new HashSet<Indi>();
		final Collection<Fam> fams = new HashSet<Fam>();
		buildCollections(indi, indis, fams);
		generateReport(fams, indis);
	}

	/** main */
	public void start(final Fam fam) throws IOException {

		final Collection<Indi> indis = new HashSet<Indi>();
		final Collection<Fam> fams = new HashSet<Fam>();
		buildCollections(fam.getHusband(), indis, fams);
		buildCollections(fam.getWife(), indis, fams);
		generateReport(fams, indis);
	}

	private void buildCollections(final Indi indi,
			final Collection<Indi> indis, final Collection<Fam> fams)
			throws FileNotFoundException, IOException {
		collectAncestors(indis, fams, indi);
		collectDecendants(indis, fams, indi);
	}

	private void collectAncestors(final Collection<Indi> indis,
			final Collection<Fam> fams, final Indi indi) {

		if (indi == null)
			return;
		indis.add(indi);
		for (final Fam fam : indi.getFamiliesWhereChild()) {
			fams.add(fam);
			collectAncestors(indis, fams, fam.getHusband());
			collectAncestors(indis, fams, fam.getWife());
		}
	}

	/** also sons-in-law and doughters-in-law are also added to the collection */
	private void collectDecendants(final Collection<Indi> indis,
			final Collection<Fam> fams, final Indi indi) {

		if (indi == null)
			return;
		indis.add(indi); // (un)married children
		for (final Fam fam : indi.getFamiliesWhereSpouse()) {
			// married children and their spouses
			indis.add(fam.getHusband());
			indis.add(fam.getWife());
			fams.add(fam);
			for (final Indi child : fam.getChildren()) {
				collectDecendants(indis, fams, child);
			}
		}
	}
	private static final PointInTime pit = new PointInTime(1,1,2200);

	/** Start after collecting the entities for the report */
	private void generateReport(final Collection<Fam> families,
			final Collection<Indi> indis) throws FileNotFoundException,
			IOException {

		final Writer out = createWriter();
		if (out == null)
			return;
		println("creating: " + reportFile.getAbsoluteFile());

		out.write(XML_HEAD+"\n");
		for (final Indi indi : sortByBirthDate(indis)) {
			out.write(createNode(indi)+"\n");
			println(indi.getBirthAsString());
		}
		for (final Fam fam : families) {
			out.write(createNode(fam)+"\n");
		}
		for (final Indi indi : indis) {
			out.write(createIndiToFam(indi, families)+"\n");
			out.write(createFamToIndi(indi, families)+"\n");
		}
		out.write(XML_TAIL+"\n");

		out.flush();
		out.close();
		println("ready with: " + reportFile.getAbsoluteFile());
	}

	private List<Indi> sortByBirthDate(final Collection<Indi> indis) {
		// hoping this could influence yEd's layout, but it seems not
		final List<Indi> sortedIndis = new ArrayList<Indi> (indis);
		Collections.sort(sortedIndis,new Comparator<Indi>(){

			@Override
			public int compare(final Indi i1, final Indi i2) {
				
				final Delta p1 = i1.getAge(pit );
				final Delta p2 = i2.getAge(pit );
			    
			    // null?
			    if (p1==p2  ) return  0;
			    if (p1==null) return  1;
			    if (p2==null) return -1;
			    
			    // let p's compare themselves
			    return -p1.compareTo(p2);
			}});
		return sortedIndis;
	}

	private static String[] createIndiColors() {

		final String[] result = new String[3];
		result[PropertySex.MALE] = "#CCCCFF";
		result[PropertySex.FEMALE] = "#FF99CC";
		result[PropertySex.UNKNOWN] = "#CCCCCC";
		return result;
	}

	private String createIndiToFam(final Indi indi,
			final Collection<Fam> families) {

		String s = "";
		for (final Fam fam : indi.getFamiliesWhereSpouse()) {
			if (families.contains(fam))
				s += MessageFormat.format(XML_EDGE, edgeCount++, indi.getId(),
						fam.getId());
		}
		return s;
	}

	private String createFamToIndi(final Indi indi,
			final Collection<Fam> families) {

		String s = "";
		for (final Fam fam : indi.getFamiliesWhereChild()) {
			if (families.contains(fam))
				s += MessageFormat.format(XML_EDGE, edgeCount++, fam.getId(),
						indi.getId());
		}
		return s;
	}

	private String createNode(final Fam family) {

		final String id = family.getId();
		final String label = createLabel(family);
		final String height = label.contains( "<html>")?"42.0":"27.0";
		return MessageFormat.format(XML_FAMILY, id, escape(label), createLink(id,
				familyUrl), createPopUpContainer(label),height);
	}

	private String createNode(final Indi indi) {

		final String id = indi.getId();
		final String label = createLabel(indi);
		return MessageFormat.format(XML_INDI, id, escape(label),
				createLink(id, indiUrl), INDI_COLORS[indi.getSex()],
				createPopUpContainer(label));
	}

	private String getImage(final Entity entity, final int width,
			final int height) {

		if (width == 0 || height == 0)
			return null;
		final Property property = entity.getPropertyByPath("INDI:OBJE:FILE");
		if (property == null)
			return null;
		final String value = property.getValue();
		final String extension = value.toLowerCase().replaceAll(".*\\.", "");
		if (imageExtensions.contains(extension)) {
			return MessageFormat.format(
					"<img src=\"{3}\" width=\"{4}\" heigth=\"{5}\">", value,
					width, height);
		}
		return null;
	}

	private String createLabel(final Fam family) {

		final String image = getImage(family, famImageWidth, famImageHeight);
		final String mariage = showEvent(OPTIONS.getMarriageSymbol(),
				(PropertyEvent) family.getProperty("MARR"));
		final String divorce = showEvent(OPTIONS.getDivorceSymbol(),
				(PropertyEvent) family.getProperty("DIV"));

		if (mariage.equals("") && divorce.equals("") && image==null)
			return "";
		final String format;
		if (image != null) {
			format = "<html><body><table><tr>"
					+ "<td>{0}<br>{1}</td><td>{3}</td>"
					+ "</tr></table></body></html>";
		} else if ( divorce.equals("") || mariage.equals("")) {
			format = "{0}{1}";
		} else {
			format = "<html><body>{0}<br>{1}</body></html>";
		}
		return wrap(format, mariage, divorce, image, famImageWidth,
				famImageHeight);
	}

	private String createLabel(final Indi indi) {

		final String image = getImage(indi, indiImageWidth, indiImageHeight);
		final String name = indi.getPropertyDisplayValue("NAME");
		final String occu = indi.getPropertyDisplayValue("OCCU");

		final String birth = showEvent(OPTIONS.getBirthSymbol(),
				(PropertyEvent) indi.getProperty("BIRT"));
		final String death = showEvent(OPTIONS.getDeathSymbol(),
				(PropertyEvent) indi.getProperty("DEAT"));

		final String format;
		if (image != null) {
			format = "<html><body><table><tr>"
					+ "<td>{0}<br>{1}<br>{2}<br>{3}</td><td>{4}</td>"
					+ "</tr></table></body></html>";
		} else if (showOccupation && occu != null && !occu.trim().equals("")) {
			format = "<html><body>{0}<br>{1}<br>{2}<br>{3}</body></html>";
		} else if (!birth.equals("") || !death.equals("")) {
			format = "<html><body>{0}<br>{1}<br>{2}</body></html>";
		} else {
			format = "{0}";
		}
		return wrap(format, name, birth, death, occu, image);
	}

	private String wrap(final String format, final Object... args) {

		return MessageFormat.format(format, args);
	}

	private String escape(final String content) {

		return content.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
	}

	private String showEvent(final String symbol, final PropertyEvent event) {

		if (event == null || !(showDates || showPlaces))
			return "";
		final Property date = event.getDate(true);
		final Property place = event.getProperty("PLAC");
		if (date == null && place == null)
			return "";
		final String string = (date == null || !showDates ? "" : date
				.getDisplayValue())
				+ " "
				+ (place == null || !showPlaces ? "" : place.format(
						place_display_format).replaceAll("^(,|(, ))*", "")
						.trim());
		if (string.trim().equals(""))
			return "";
		return symbol + " " + string;
	}

	private String createLink(final String id, final String urlFormat) {

		if (urlFormat == null)
			return "";
		final String link = MessageFormat.format(urlFormat, id);
		return MessageFormat.format(XML_LINK_CONTAINER, link);
	}

	/**
	 * @param content
	 *            text only, no HTML
	 */
	private String createPopUpContainer(final String content) {

		if (content == null)
			return "";
		return MessageFormat.format(XML_POPUP_CONTAINER, content);
	}

	private String getString(final String key) {

		return getResources().getString(key);
	}

	private Writer createWriter() throws FileNotFoundException {

		final String extension = "graphml";
		reportFile = getFileFromUser(translate("name"), translate("save"),
				true, extension);
		if (reportFile == null)
			return null;
		if (!reportFile.getName().toLowerCase().endsWith("." + extension)) {
			reportFile = new File(reportFile.getPath() + "." + extension);
		}
		final FileOutputStream fileOutputStream = new FileOutputStream(
				reportFile);
		final OutputStreamWriter streamWriter = new OutputStreamWriter(
				fileOutputStream, Charset.forName("UTF8"));
		return new BufferedWriter(streamWriter);
	}
}
