package geo.kml;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.geo.GeoLocation;

import java.io.IOException;
import java.io.Writer;

public class DetailedPlacemarkWriter extends PlacemarkWriter {

	public DetailedPlacemarkWriter(Writer out) {
		super(out, 0);
	}
	protected void writePlacemarkContent(String indent, GeoLocation location)
			throws IOException {
		for (int p = 0; p < location.getNumProperties(); p++) {
			out.write(indent);
			Property prop = location.getProperty(p);
			Property date = prop.getProperty("DATE", true);
			if (date != null) {
				out.write(date.toString());
				out.write(" ");
			}
			out.write(Gedcom.getName(prop.getTag()));
			out.write(" ");
			out.write(prop.getEntity().toString());
			out.write("<br>\n");
		}
	}

}
