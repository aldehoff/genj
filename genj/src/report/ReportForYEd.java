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
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
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

public class ReportForYEd extends Report {

	private static final String FILE_EXTENSION = "graphml";

	private static final String SYMBOL_MARRIAGE = Options.getInstance()
			.getMarriageSymbol();
	private static final String SYMBOL_DIVORCE = Options.getInstance()
			.getDivorceSymbol();
	private static final String SYMBOL_BIRTH = Options.getInstance()
			.getBirthSymbol();
	private static final String SYMBOL_DEATH = Options.getInstance()
			.getDeathSymbol();

	private static final TagPath TAG_MARRIAGE = new TagPath("FAM:MARR");
	private static final TagPath TAG_DIVORCE = new TagPath("FAM:DIV");
	private static final TagPath TAG_BIRTH = new TagPath("FAM:BIRT");
	private static final TagPath TAG_DEATH = new TagPath("FAM:DEAT");

	private static final Charset UTF8 = Charset.forName("UTF8");

	public String indiUrl = getString("indiUrlDefault");
	public String familyUrl = getString("familyUrlDefault");

	private final String indiColors[] = createIndiColors();

	private final String xmlLinkContainer = getString("LinkContainer");
	private final String xmlPopUpContainer = getString("PopUpContainer");
	private final String xmlIndi = getString("IndiNode");
	private final String xmlFamily = getString("FamilyNode");
	private final String xmlEdge = getString("Edge");
	private final String xmlHead = getString("XmlHead");
	private final String xmlTail = getString("XmlTail");

	private int edgeCount = 0;
	private File reportFile;

	/** main */
	public void start(final Gedcom gedcom) throws IOException {

		final Writer out = createWriter();
		if (out == null)
			return;
		println("creating: " + reportFile.getAbsoluteFile());

		out.write(xmlHead);
		for (final Entity entity : gedcom.getEntities(Gedcom.FAM)) {
			out.write(createNode((Fam) entity));
		}
		for (final Entity entity : gedcom.getEntities(Gedcom.INDI)) {
			out.write(createNode((Indi) entity));
			out.write(createEdges((Indi) entity));
		}
		out.write(xmlTail);

		out.flush();
		out.close();
		println("ready with: " + reportFile.getAbsoluteFile());
	}

	private String[] createIndiColors() {

		final String[] result = new String[3];
		result[PropertySex.MALE] = "#CCCCFF";
		result[PropertySex.FEMALE] = "#FF99CC";
		result[PropertySex.UNKNOWN] = "#CCCCCC";
		return result;
	}

	private String createEdges(final Indi indi) {

		String s = "";
		for (final Fam fam : indi.getFamiliesWhereSpouse()) {
			s += MessageFormat.format(xmlEdge, edgeCount++, indi.getId(), fam
					.getId());
		}
		for (final Fam fam : indi.getFamiliesWhereChild()) {
			s += MessageFormat.format(xmlEdge, edgeCount++, fam.getId(), indi
					.getId());
		}
		return s;
	}

	private String createNode(final Fam family) {

		final String id = family.getId();
		return MessageFormat.format(xmlFamily, id, createLabel(family),
				createLink(id, familyUrl),
				createPopUpContainer(createPopUpContent(family)));
	}

	private String createNode(final Indi indi) {

		final String id = indi.getId();
		return MessageFormat.format(xmlIndi, id, createLabel(indi), createLink(
				id, indiUrl), indiColors[indi.getSex()],
				createPopUpContainer(createPopUpContent(indi)));
	}

	private String createLabel(final Fam family) {

		final String mariage = showEvent(SYMBOL_MARRIAGE,
				(PropertyEvent) family.getProperty(TAG_MARRIAGE));
		final String divorce = showEvent(SYMBOL_DIVORCE, (PropertyEvent) family
				.getProperty(TAG_DIVORCE));

		if (mariage == null && divorce == null)
			return "";
		if (mariage != null && divorce != null)
			return MessageFormat.format("<html><body>{0}<br>{1}</body></html>",
					mariage, divorce).replaceAll(">", "&gt;").replaceAll("<",
					"&lt;");
		if (mariage != null)
			return mariage;
		if (divorce != null)
			return divorce;
		return mariage;
	}

	private String createLabel(final Indi indi) {
		final String name = indi.getPropertyDisplayValue("NAME");
		String birth = showEvent(SYMBOL_BIRTH, (PropertyEvent) indi
				.getProperty(TAG_BIRTH));
		String death = showEvent(SYMBOL_DEATH, (PropertyEvent) indi
				.getProperty(TAG_DEATH));
		if (name == null && birth == null && death == null)
			return "";
		if (birth == null && death == null)
			return name;
		if (birth == null)
			birth = "";
		if (death == null)
			death = "";
		// TODO debug why events don't appear; profession and image; optional dates and places
		return MessageFormat.format(
				"<html><body>{0}<br>{1}<br>{2}</body></html>", name, birth,
				death).replaceAll(">", "&gt;").replaceAll("<", "&lt;");
	}

	private String showEvent(final String symbol, final PropertyEvent event) {

		if (event == null)
			return null;
		final Property date = event.getDate(true);
		final Property place = event.getProperty("PLAC");
		if (date == null && place == null)
			return null;
		return symbol
				+ " "
				+ (date == null ? "" : date.getDisplayValue())
				+ " "
				+ (place == null ? "" : place.getDisplayValue().replaceAll(
						",.*", ""));
	}

	private String createPopUpContent(final Fam family) {
		// by default the label is used as pop up
		return null;
	}

	private String createPopUpContent(final Indi indi) {
		// by default the label is used as pop up
		return null;
	}

	private String createLink(final String id, final String urlFormat) {

		if (urlFormat == null)
			return "";
		final String link = MessageFormat.format(urlFormat, id);
		return MessageFormat.format(xmlLinkContainer, link);
	}

	private String createPopUpContainer(final String content) {

		if (content == null)
			return "";
		return MessageFormat.format(xmlPopUpContainer, content);
	}

	private String getString(final String key) {

		return getResources().getString(key);
	}

	private Writer createWriter() throws FileNotFoundException {

		reportFile = getFileFromUser(translate("name"), translate("save"),
				true, FILE_EXTENSION);
		if (reportFile == null)
			return null;
		if (!reportFile.getName().toLowerCase().endsWith("." + FILE_EXTENSION)) {
			reportFile = new File(reportFile.getPath() + "." + FILE_EXTENSION);
		}
		final FileOutputStream fileOutputStream = new FileOutputStream(
				reportFile);
		final OutputStreamWriter streamWriter = new OutputStreamWriter(
				fileOutputStream, UTF8);
		return new BufferedWriter(streamWriter);
	}
}
