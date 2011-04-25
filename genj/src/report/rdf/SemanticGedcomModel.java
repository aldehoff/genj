package rdf;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class SemanticGedcomModel {

	private static final String TYPE = "http://genj.sourceforge.net/rdf/gedcom/type/";
	private static final String PREDICATE = "http://genj.sourceforge.net/rdf/gedcom/predicate/";
	
	private final Model model = ModelFactory.createDefaultModel();
	private final Property valueProperty = getModel().createProperty(PREDICATE + "value");
	
	private final Map<String, String> prefixes = new HashMap<String, String>();
	private final Map<String, Property> properties = new HashMap<String, Property>();
	private final Map<String, Resource> types = new HashMap<String, Resource>();
	private final Map<String, String> uriFormats;

	/**
	 * @param uriFormats
	 *            pairs of entity tags and URIs (preferably URLs)
	 */
	public SemanticGedcomModel(final Map<String, String> uriFormats) {
		this.uriFormats = uriFormats;
		prefixes.put("p", PREDICATE);
		prefixes.put("t", TYPE);
		getModel().setNsPrefixes(prefixes);
	}

	private Resource toType(final String tag) {
		if (!types.containsKey(tag))
			types.put(tag, getModel().createResource(prefixes.get("t") + tag));
		return types.get(tag);
	}

	private Property toProperty(final String tag) {
		if (!properties.containsKey(tag))
			properties.put(tag, getModel().createProperty(prefixes.get("p") + tag));
		return properties.get(tag);
	}

	public Resource addEntity(final String id, final String tag) {
		return getModel().createResource(toUri(id, tag), toType(tag));
	}

	private String toUri(final String id, final String tag) {
		return MessageFormat.format(uriFormats.get(tag), id);
	}

	public void addPropertyValue(final Resource resource, final String value) {
		if (value == null || value.trim().length() == 0)
			return;
		resource.addProperty(valueProperty, value);
	}

	public Resource addProperty(final Resource resource, final String tag) {
		final Resource property = getModel().createResource(toType(tag));
		resource.addProperty(toProperty(tag), property);
		return property;
	}

	public Model getModel() {
		return model;
	}

	public void addConnection(final Resource referrer, final String id, final String referrerTag, final String referredTag) {
		Resource referred = model.getResource(toUri(id, referredTag));
		referrer.addProperty(toProperty(referrerTag),referred);
	}
}
