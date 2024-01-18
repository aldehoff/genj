package rdf;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

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
			final String tag = property.getTag();
			if (property instanceof PropertyDate) {
				final PropertyDate date = (PropertyDate) property;
				// TODO after/before, ranges, julian etc
				if (date.isValid() && date.getFormat().toString().equals("")) {
					final PointInTime start = date.getStart();
					if (start.isComplete() && start.isGregorian()) {
						final Resource propertyResource = rdfModel.addProperty(resource, tag, null);
						rdfModel.addLiteral(propertyResource, "value", toXsdDateTime(start));
						continue;
						// other dates get default treatment
					}
				}
			}
			final String value = property.getValue();
			if (value.startsWith("@")) {
				final String id = value.replaceAll("@", "");
				final String referredTag = property.getGedcom().getEntity(id).getTag();
				rdfModel.addConnection(resource, id, tag, referredTag);
			} else {
				final Resource propertyResource = rdfModel.addProperty(resource, tag, value);
				addProperties(propertyResource, property.getProperties());
				if (property instanceof PropertyName) {
					final PropertyName name = (PropertyName) property;
					rdfModel.addLiteral(propertyResource, "first", name.getFirstName());
					rdfModel.addLiteral(propertyResource, "last", name.getLastName());
					rdfModel.addLiteral(propertyResource, "suffix", name.getSuffix());
				}
				if (property instanceof PropertyPlace) {
					// TODO add lat/long using cached locations from the GEO
					// report?
				}
			}
		}
	}

	private static XSDDateTime toXsdDateTime(final PointInTime pit) {
		final Calendar calendar = new GregorianCalendar(pit.getYear(), pit.getMonth(), pit.getDay());
		return new XSDDateTime(calendar);
	}

	public Model toRdf(final Gedcom gedcom, final Map<String, String> uriFormats) {
		rdfModel = new SemanticGedcomModel(uriFormats);
		for (final Entity entity : gedcom.getEntities()) {
			final Resource resource = rdfModel.addEntity(entity.getId(), entity.getTag());
			addProperties(resource, entity.getProperties());
		}
		return rdfModel.getModel();
	}

	public Model getInfModel(final String rules) {
		
		for (String key:SemanticGedcomModel.PREFIXES.keySet()) {
			PrintUtil.registerPrefix(key, SemanticGedcomModel.PREFIXES.get(key));
		}
		final GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		reasoner.setMode(GenericRuleReasoner.HYBRID);
		
		final InfModel infModel = ModelFactory.createInfModel(reasoner, rdfModel.getModel());
		infModel.prepare();
		return infModel;
	}
}
