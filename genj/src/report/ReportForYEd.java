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
import genj.gedcom.PropertyDate;
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

	private static final String MARRIAGE_SYMBOL = Options.getInstance()
			.getMarriageSymbol();

	private static final Charset UTF8 = Charset.forName("UTF8");

	public String indiUrl = getString("indiUrlDefault");
	public String familyUrl = getString("familyUrlDefault");

	public String familyColor = getString("familyColorDefault");
	public String maleColor = getString("maleColorDefault");
	public String femaleColor = getString("femaleColorDefault");
	public String sexlessColor = getString("sexlessColorDefault");

	private final String indiColors[] = createIndiColors();

	private final String xmlLinkContainer = getString("LinkContainer");
	private final String xmlPopUpContainer = getString("PopUpContainer");
	private final String xmlIndi = getString("IndiNode");
	private final String xmlFamily = getString("FamilyNode");
	private final String xmlEdge = getString("Edge");
	private final String xmlHead = getString("XmlHead");
	private final String xmlTail = getString("XmlTail");

	int edgeCount = 0;
	File reportFile;

	/** main */
	public void start(final Gedcom gedcom) throws IOException {

		final Writer out = createWriter();
		if (out == null)
			return;

		out.write(xmlHead);
		for (final Entity entity : gedcom.getEntities(Gedcom.FAM)) {
			out.write(createFamily((Fam) entity));
		}
		for (final Entity entity : gedcom.getEntities(Gedcom.INDI)) {
			out.write(createIndi((Indi) entity));
			out.write(createEdges((Indi) entity));
		}
		out.write(xmlTail);
		out.close();
		println("ready: " + reportFile.getAbsoluteFile());
	}

	private String[] createIndiColors() {
		final String[] result = new String[3];
		result[PropertySex.MALE] = maleColor;
		result[PropertySex.FEMALE] = femaleColor;
		result[PropertySex.UNKNOWN] = sexlessColor;
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

	private String createFamily(final Fam family) {
		final String id = family.getId();
		final Property marriage = family.getProperty(new TagPath("FAM:MARR"));
		final String label = marriage == null ? "" : marriage.getDisplayValue();
		return MessageFormat.format(xmlFamily, id, label, link(id, familyUrl),
				familyColor, popUp(null));
	}

	private String createIndi(final Indi indi) {
		final String id = indi.getId();
		final String label = indi.getName();
		return MessageFormat.format(xmlIndi, id, label, link(id, indiUrl),
				indiColors[indi.getSex()], popUp(null));
	}

	private String link(final String id, final String urlFormat) {
		if (urlFormat == null)
			return "";
		final String link = MessageFormat.format(urlFormat, id);
		return MessageFormat.format(xmlLinkContainer, link);
	}

	private String popUp(final String content) {
		if (content == null)
			return "";
		return MessageFormat.format(xmlPopUpContainer, content);
	}

	private String getString(final String key) {
		return getResources().getString(key);
	}

	private Writer createWriter() throws FileNotFoundException {

		reportFile = getFileFromUser(translate("name"), translate("save"),
				true, "graphml");
		if (reportFile == null)
			return null;

		final FileOutputStream fileOutputStream = new FileOutputStream(reportFile);
		final OutputStreamWriter streamWriter = new OutputStreamWriter(
				fileOutputStream, UTF8);
		return new BufferedWriter(streamWriter);
	}
}
