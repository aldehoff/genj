package geo.kml;

import static java.text.MessageFormat.format;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.geo.GeoLocation;
import genj.report.Options;

import java.io.IOException;
import java.io.Writer;

public class DetailedPlacemarkWriter extends PlacemarkWriter {

	private String indiLink;
	private String famLink;

	public DetailedPlacemarkWriter(Writer out, boolean showIds,
			String indiLink, String famLink) {
		super(out, 0, showIds);
		this.indiLink = indiLink;
		this.famLink = famLink;
	}

	protected void writePlacemarkContent(String indent, GeoLocation location,
			boolean showIds) throws IOException {
		for (int p = 0; p < location.getNumProperties(); p++) {
			Property prop = location.getProperty(p);
			Entity entity = prop.getEntity();
			out.write(indent);
			addDate(prop);
			
			// TODO make usage of abbreviations optional
			out.write(Options.getInstance().getSymbol(prop.getTag()));
			
			// TODO add links for individuals in marriage
			out.write(" " + entity.toString(showIds));
			
			if (entity instanceof Indi && !indiLink.equals("")) {
				out.write(" " + format(indiLink, entity.getId()));
			}
			if (entity instanceof Fam && !famLink.equals("")) {
				out.write(" " + format(famLink, entity.getId()));
			}
			out.write("<br>\n");
		}
	}

	private void addDate(Property prop) throws IOException {
		Property date = prop.getProperty("DATE", true);
		if (date != null) {
			out.write(date.toString());
			out.write(" ");
		}
	}
}
