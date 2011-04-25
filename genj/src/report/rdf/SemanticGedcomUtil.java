package rdf;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;

import java.util.Map;

import org.jfree.util.Log;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class SemanticGedcomUtil {

	private SemanticGedcomModel rdfModel;

	/**
	 * Recursively adds anonymous properties to a resource.
	 * 
	 * @param resource
	 * @param properties
	 *            each {@link genj.gedcom.Property} is added as a
	 *            {@link com.hp.hpl.jena.rdf.model.Property} to the specified
	 *            {@link Resource}, children are added to children.
	 */
	private void addProperties(final Resource resource, final Property... properties) {
		if (properties == null)
			return;
		for (final Property property : properties) {
			final String value = property.getValue();
			final String tag = property.getTag();
			if (value.startsWith("@")){
				final String id = value.replaceAll("@", "");
				final Entity entity = property.getGedcom().getEntity(id);
				rdfModel.addConnection(resource, id, tag, entity.getTag());
			} else {
				final Resource propertyResource = rdfModel.addProperty(resource, tag);
				addProperties(propertyResource, property.getProperties());

				// TODO convert dates to to typed literals
				// TODO add lat/long for places using cached locations from the GEO report?
				rdfModel.addPropertyValue(propertyResource, value);
			}
		}
	}

	public Model toRdf(final Gedcom gedcom,final  Map<String, String> uriFormats) {
		rdfModel = new SemanticGedcomModel(uriFormats);
		for (final Entity entity : gedcom.getEntities()) {
			Log.info(entity.getId());
			final Resource resource = rdfModel.addEntity(entity.getId(), entity.getTag());
			addProperties(resource, entity.getProperties());
		}
		return rdfModel.getModel();
	}
}
