package geo.kml;

import genj.geo.GeoLocation;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;

abstract class PlacemarkWriter extends KmlWriter{

	private final static DecimalFormat FORMAT //
	= new DecimalFormat("##0.###", new DecimalFormatSymbols(Locale.US));

	private final int folded;

	private boolean showIds;
	
	PlacemarkWriter(Writer out, int folded, boolean showIds) {
		super(out);
		this.folded = folded;
		this.showIds = showIds;
	}

	public void write(final String indent, final Iterator<GeoLocation> locations,
			final String name, final String description, final boolean radio)
			throws IOException {

		if (locations==null)
			return;

		new FolderWriter(out, radio, 0) {

			public void writeContent(final String indent) throws IOException {
				while (locations.hasNext()) {
					writePlacemark(indent, locations.next());
				}
			}

		}.write(indent, name, description);
	}

	private void writePlacemark(final String indent,
			final GeoLocation location) throws IOException {
		if (!location.isValid())
			return;
		out.write(indent
				+ "<Placemark>" //
				+ "<name>" + location.toString() + "</name>"
				+ "<Snippet maxLines='1'/><description><![CDATA[\n");
		writePlacemarkContent(indent + "\t", location, showIds);
		out.write(indent + "]]></description><Point>" //
				+ "<coordinates>" //
				+ FORMAT.format(location.getX()) + "," //
				+ FORMAT.format(location.getY()) //
				+ "</coordinates></Point></Placemark>\n");
	}

	protected abstract void writePlacemarkContent(String indent, GeoLocation location, boolean showIds)
			throws IOException;
}
