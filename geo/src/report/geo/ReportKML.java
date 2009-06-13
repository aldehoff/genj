package geo;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import static java.text.MessageFormat.format;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.geo.GeoLocation;
import genj.geo.GeoService;
import genj.io.FileAssociation;
import genj.report.Report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import geo.kml.*;

/** This GenJ Report exports places for viewing online in Google Maps */
public class ReportKML extends Report {

	public int nrOfPrivateGenerations = 1;

	public String byFamilyDescription = "";
	public String byFamilyName = translate("byFamily");
	public String byGenerationDescription = "";
	public String byGenerationName = translate("byGeneration");
	public String byPlaceDescription = "";
	public String byPlaceName = translate("byPlace");

	public String reportDescription = translate("reportDefaultDescription");
	public String reportName = translate("reportDefaultName", "{0}");

	public String labelForGeneration = translate("generationNr", new Object[] {
			"{0}", "{1}" });
	public String labelForLocations = translate("locationsDefaultLabel");

	private Writer out;

	/** we're not a stdout report */
	public boolean usesStandardOut() {
		return false;
	}

	/** Our main method for one individual */
	public void start(Indi indi) {

		File kml = getKmlFile();
		if (kml == null)
			return;

		// write the KML file
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(kml), Charset.forName("UTF8")));
			writeKML(indi);
			out.close();
		} catch (IOException e) {
			getOptionFromUser(translate("ioerror", kml), OPTION_OK);
			return;
		}

		// let the user know
		getOptionFromUser(translate("done", new String[] { kml.getName(),
				kml.getName(), kml.getParent() }), OPTION_OK);

		// done
	}

	private Collection<GeoLocation> getLocations(Collection<Entity> entities) {
		Gedcom ged = entities.iterator().next().getGedcom();
		try {
			Collection<GeoLocation> locations //
			= GeoLocation.parseEntities(entities);
			return GeoService.getInstance() //
					.match(ged, locations, true);
		} catch (Exception e) {
			super.println(e.getMessage());
			return null;
		}
	}

	private File getKmlFile() {
		File kml = getFileFromUser(translate("which_kml_file"),
				translate("generate"));
		if (kml == null)
			return null;
		String suffix = FileAssociation.getSuffix(kml);
		if (!suffix.toLowerCase().equals("kml")) {
			kml = new File(kml.getAbsolutePath() + ".kml");
		}
		return kml;
	}

	/** write the KML file */
	private void writeKML(final Indi indi) throws IOException {

		final Collection<Entity> ancestors = new ArrayList<Entity>();
		getAncestors(ancestors, indi, nrOfPrivateGenerations);

		out
				.write("<kml xmlns='http://earth.google.com/kml/2.2'>\n"
						+ "\t<Document>\n"
						+ "\t\t<name><![CDATA["
						+ format(reportName, Names.getName(indi))
						+ "]]></name><visibility>1</visibility><open>1</open>\n"
						+ "\t\t<description><![CDATA["
						+ reportDescription
						+ "]]></description>\n"
						+ "\t\t<Style id='checkFolder'><ListStyle><listItemType>check</listItemType></ListStyle></Style>\n"
						+ "\t\t<Style id='radioFolder'><ListStyle><listItemType>radioFolder</listItemType></ListStyle></Style>\n"
						+ "\t\t<styleUrl>#radioFolder</styleUrl>\n");
		writeSosa("\t\t", indi);
		writeLocations("\t\t", getLocations(ancestors));
		writeLineage("\t\t", indi);
		out.write("\t</Document>\n"//
				+ "</kml>\n");
	}

	private void getAncestors(Collection<Entity> ancestors, Indi indi, int hide) {
		if (indi == null)
			return;
		if (hide <= 0)
			ancestors.add(indi);
		getAncestors(ancestors, indi.getBiologicalFather(), hide - 1);
		getAncestors(ancestors, indi.getBiologicalMother(), hide - 1);
	}

	private Iterator<GeoLocation> getLocations(Indi indi,
			boolean showBirthsOfChildern) {
		List<Fam> fams = Arrays.asList(indi.getFamiliesWhereSpouse());
		Collection<Entity> entities = new ArrayList<Entity>(fams);
		entities.add((Entity) indi);
		if (null == indi.getProperty(new TagPath("INDI:BIRT:PLAC"))) {
			entities.add((Entity) indi.getFamilyWhereBiologicalChild());
		}
		Collection<GeoLocation> locations = getLocations(entities);
		if (showBirthsOfChildern)
			addBirthsOfChildren(indi, locations);
		return locations == null ? null : locations.iterator();
	}

	private void addBirthsOfChildren(Indi indi,
			Collection<GeoLocation> locations) {
		try {
			Indi[] children = indi.getFamilyWhereBiologicalChild()
					.getChildren();
			for (Indi child : children) {
				Property birthOfChild = child.getProperty(new TagPath(
						"INDI:BIRT"));
				try {
					GeoLocation childsLocations = new GeoLocation(birthOfChild);
					locations.add(childsLocations);
				} catch (IllegalArgumentException e) {
					continue;
				}
			}
		} catch (NullPointerException e) {
		}
	}

	private void writeSosa(final String indent, final Indi indi)
			throws IOException {

		final Map<Integer, Indi> sosaIndis = new HashMap<Integer, Indi>();
		final int privateSosa = calc();
		sosaIndis.put(1, indi);
		final FolderWriter folderWriter = new FolderWriter(out, false, 0) {

			public void writeContent(String indent) throws IOException {
				Iterator<Integer> it = sosaIndis.keySet().iterator();
				while (it.hasNext()) {
					final int sosaNr = it.next();
					final Indi indi = sosaIndis.get(sosaNr);
					writeSosaIndi(indent, sosaNr, indi, sosaNr > privateSosa);
				}
			}
		};
		new FolderWriter(out, false, 0) {
			public void writeContent(String indent) throws IOException {
				for (int i = 1; sosaIndis.size() > 0; i++) {
					if (i > nrOfPrivateGenerations) {
						String label = translate("generation_" + (i - 1));
						label = format(labelForGeneration, i, label);
						folderWriter.write(indent, label, "");
					}
					nextGeneration(sosaIndis);
				}
			}
		}.write(indent, byGenerationName, byGenerationDescription);
	}

	private int calc() {
		int privateSosa = 1;
		for (int i = 0; i <= nrOfPrivateGenerations; i++)
			privateSosa *= 2;
		return privateSosa - 1;
	}

	private void nextGeneration(final Map<Integer, Indi> sosaIndis) {
		Map<Integer, Indi> ancestors = new HashMap<Integer, Indi>();
		Iterator<Integer> it = sosaIndis.keySet().iterator();
		while (it.hasNext()) {
			final int sosaNr = it.next();
			final Indi indi = sosaIndis.get(sosaNr);
			Indi father = indi.getBiologicalFather();
			if (father != null)
				ancestors.put(sosaNr * 2, father);
			Indi mother = indi.getBiologicalMother();
			if (mother != null)
				ancestors.put(sosaNr * 2 + 1, mother);
		}
		sosaIndis.clear();
		sosaIndis.putAll(ancestors);
	}

	private void writeLocations(final String indent,
			final Collection<GeoLocation> locations) throws IOException {

		// TODO sort locations alphabetically
		Iterator<GeoLocation> iterator = locations.iterator();
		new CompactPlacemarkWriter(out).write//
				(indent, iterator, byPlaceName, byPlaceDescription, true);
	}

	/** Writes the top level of a lineage section */
	private void writeLineage(final String indent, final Indi indi)
			throws IOException {
		new FolderWriter(out, false, 1) {
			public void writeContent(String indent) throws IOException {
				writeLineage(indent, 1, indi, nrOfPrivateGenerations);
			}
		}.write(indent, byFamilyName, byFamilyDescription);
	}

	/** Writes a sub level of a lineage section */
	private void writeLineage(final String indent, final int sosaNr,
			final Indi indi, final int hide) throws IOException {

		if (indi == null)
			return;
		if (hide > 0)
			writeLineageParents(indent, sosaNr, indi, hide);
		else {
			final Iterator<GeoLocation> locations = getLocations(indi,
					hide == 0);
			new FolderWriter(out, false, 0) {
				public void writeContent(String indent) throws IOException {
					new DetailedPlacemarkWriter(out).write//
							(indent, locations, labelForLocations, "", false);
					writeLineageParents(indent, sosaNr, indi, hide);
				}
			}.write(indent, sosaNr + ": " + indi.toString(), "");
		}
	}

	/** Writes the next generation of a lineage section */
	private void writeLineageParents(String indent, final int sosaNr,
			final Indi indi, final int hide) throws IOException {
		writeLineage(indent + "\t", sosaNr * 2, indi.getBiologicalFather(),
				hide - 1);
		writeLineage(indent + "\t", sosaNr * 2 + 1, indi.getBiologicalMother(),
				hide - 1);
	}

	private void writeSosaIndi(String indent, final int sosaNr,
			final Indi indi, boolean showBirthOfChildren) throws IOException {

		String folderName = sosaNr + ": " + indi;

		Iterator<GeoLocation> locations = getLocations(indi,
				showBirthOfChildren);
		if (locations != null) {
			new DetailedPlacemarkWriter(out).write //
					(indent, locations, folderName, "", false);
		} else {
			new FolderWriter(out, false, 1) {
				public void writeContent(String indent) throws IOException {
					// empty folder because no place mark is available
				}
			}.write(indent, folderName, "");
		}
	}
}
