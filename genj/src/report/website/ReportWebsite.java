package website;
/**
 * A report for making a website with all information
 * 
 * author   = Erik Melkersson, erik.melkersson@gmail.com
 * version  = 0.2 beta
 * category = Chart
 * name     = Website
 */
import genj.option.CustomOption;
import genj.option.Option;
import genj.report.Report;
import genj.gedcom.*;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ReportWebsite extends Report {
	//public boolean reportPrivateData = false;
	public boolean reportLinksToMap = true;
	public boolean reportNowLiving = false;
	public String reportIndexFileName = "index.html";
	public String listPersonFileName = "listing.html";
	public String listSourceFileName = "sources.html";
	public String listRepositoryFileName = "repositories.html";
	
    public String reportTitle = "Relatives";
    protected String reportWelcomeText = "On these pages my ancestors are presented";
    public boolean displaySosaStradonitz = false;
	protected HashMap<String, String> sosaStradonitzNumber = null; 
    public boolean displayGenJFooter = true;
	public String placeDisplayFormat = "all";

	/** Base source file of the css */
	protected static final String cssBaseFile = "html/style.css";

	/** How the tree on each person should look like */
    public int treeType = 0;
    public String[] treeTypes = {translate("treeLTR"), translate("treeRTL")}; //, translate("treeTopDown")};
	protected static final String[] cssTreeFile = {"html/treel2r.css", "html/treer2l.css"};

    /** Colors of the output */
	public String cssTextColor = "000";
	public String cssBackgroundColor = "FFF";
	public String cssLinkColor = "009";
	public String cssVistedLinkColor = "609";
	public String cssBorderColor = "000";

	/** Select background image in the boxes */
    public int boxBackground = 0;
    public String[] boxBackgrounds = {translate("green"), translate("blue")};
    protected static final String[] boxBackgroundImages = {"html/bkgr_green.png", "html/bkgr_blue.png"};
	
    /** Collecting data to the index */
    protected List<Indi> personsWithImage = null;
    
	/**
	 * Main for argument Gedcom
	 */
	public void start(Gedcom gedcom) throws Exception {
		// Reset some variables
		sosaStradonitzNumber = new HashMap<String, String>();
		personsWithImage = new ArrayList<Indi>();

		// Ask for info
		
		File dir = getDirectoryFromUser(translate("qOutputDir"), translate("qOk"));
		if (dir == null) 
		  return; // Operation canceled by user
		
		// make sure directory exists
		dir.mkdirs();
		
		// Ask if ok to overwrite if there were files
		if (dir.list().length > 0) {
			if (! getOptionFromUser(translate("qOverwrite"), OPTION_OKCANCEL)) 
				return; // Operation canceled by user
		}

		if (displaySosaStradonitz) {
			Indi rootIndi = (Indi)getEntityFromUser(translate("selectSosaStradonitzRoot"), gedcom, Gedcom.INDI);
			makeSosaStradonitzNumbering(rootIndi, 1);
		}
		
		// Validate values
		
		// Try to make a translator for css values
		HashMap<String,String> translator;
		try {
			translator = makeCssColorSettings();
		} catch (InvalidParameterException e) {
			getOptionFromUser(e.getMessage(), OPTION_OK);
			return;
		}
		
		// Start modifying things
		
		// Copy the correct background image
		copyBackgroundImage(dir);
		
		// Make a css file with current settings
		makeCss(dir, translator);
		
	    // Iterate over all individuals
		Entity[] indis = gedcom.getEntities(Gedcom.INDI, "");
		//Collection<Indi> indis = gedcom.getIndis();
		for(Entity indi : indis) {
			println("Exporting person " + indi.getId() + " " + getName((Indi)indi));
			File indiFile = makeDirFor(indi.getId(), dir);
			createIndiDoc((Indi)indi, indiFile.getParentFile()).toFile(indiFile);
		}
		
	    // Iterate over all sources
		Entity[] sources = gedcom.getEntities(Gedcom.SOUR, "");
		for(Entity source : sources) {
			println("Exporting source " + source.getId());
			File indiFile = makeDirFor(source.getId(), dir);
			createSourceDoc((Source)source, indiFile.getParentFile()).toFile(indiFile);
		}

	    // Iterate over all sources
		Entity[] repos = gedcom.getEntities(Gedcom.REPO, "");
		for(Entity repo : repos) {
			println("Exporting repository " + repo.getId());
			File indiFile = makeDirFor(repo.getId(), dir);
			createRepoDoc((Repository)repo, indiFile.getParentFile()).toFile(indiFile);
		}

		// Make a start page and indexes
		Arrays.sort(indis, new PropertyComparator("INDI:NAME"));
		Arrays.sort(sources, new EntityComparator());
		Arrays.sort(repos, new EntityComparator());
		makeStartpage(dir, indis, sources, repos);
		makePersonIndex(dir, indis);
		if (sources.length > 0)
			makeEntityIndex(dir, sources, "sourceIndex", listSourceFileName);
		if (repos.length > 0)
			makeEntityIndex(dir, repos, "repositoryIndex", listRepositoryFileName);
	}

	protected void makeSosaStradonitzNumbering(Indi person, int number) {
		sosaStradonitzNumber.put(person.getId(), Integer.toString(number));
		Fam fam = person.getFamilyWhereBiologicalChild();
		if (fam != null) {
			Indi father = fam.getHusband();
			if (father != null) makeSosaStradonitzNumbering(father, number * 2);
			Indi mother = fam.getWife();
			if (mother != null) makeSosaStradonitzNumbering(mother, number * 2 + 1);
		}
	}
	
	/**
	 * Copy the correct background image 
	 */
	protected void copyBackgroundImage(File dir) throws IOException {
		File sourceFile = new File(getFile().getParentFile(), boxBackgroundImages[boxBackground]);
		File dstFile = new File(dir, "bkgr.png");
		copyFile(sourceFile, dstFile);
	}

	/**
	 * To make it possible to enter a longer text
	 */
	protected List<? extends Option> getCustomOptions() {
		return Collections.singletonList(new TextAreaOption());
	}
	/**
	 * To make it possible to enter a longer text
	 */
	private class TextAreaOption extends CustomOption {
		private JTextArea text = new JTextArea(reportWelcomeText);
		protected JComponent getEditor() {
			return new JScrollPane(text);
		}
		protected void commit(JComponent editor) {
			reportWelcomeText = text.getText();
		}
		public String getName() {
			return translate("reportWelcomeText");
		}
		public String getToolTip() {
			return "Enter your page description here. It will be enclosed in <p>-tags.";
		}
		public void persist() {
			getRegistry().put("reportWelcomeText", text.getText());
		}
		public void restore() {
			text.setText(getRegistry().get("reportWelcomeText", ""));
		}
	}

	protected void makeStartpage(File dir, Entity[] indis, Entity[] sources, Entity[] repos) {
		println("Making start-page");
		File startFile = new File(dir.getAbsolutePath() + File.separator + reportIndexFileName);
		Html html = new Html(reportTitle, "");
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		bodyNode.appendChild(html.h1(reportTitle));
		bodyNode.appendChild(html.pNewlines(reportWelcomeText));
		Element div1 = html.div("left");
		div1.appendChild(html.h2(translate("personIndex")));
		String lastLetter = "";
		for (Entity indi : indis) {
			String lastname = ((Indi)indi).getLastName();  
			String letter = "?";
			if (lastname != null && !lastname.isEmpty()) letter = lastname.substring(0, 1); // Get first letter of last name
			if (! letter.equals(lastLetter)) {
				div1.appendChild(html.link(listPersonFileName + "#" + letter, letter));				
				div1.appendChild(html.text(", "));				
				lastLetter = letter;
			}
		}
		bodyNode.appendChild(div1);

		Element div2 = html.div("right");
		div2.appendChild(html.h2(translate("personGallery")));
		for (Indi indi : personsWithImage) { 
			div2.appendChild(html.link(addressTo(indi.getId()), html.img(addressToDir(indi.getId()) + "tree.jpg", getName(indi))));				
		}
		bodyNode.appendChild(div2);

		if (sources.length > 0) {
			Element div3 = html.div("left");
			div3.appendChild(html.h2(translate("sourceIndex")));
			lastLetter = "";
			for (Entity source : sources) { 
				String letter = source.toString().substring(0, 1); // Get first letter
				if (! letter.equals(lastLetter)) {
					div3.appendChild(html.link(listSourceFileName + "#" + letter, letter));				
					div3.appendChild(html.text(", "));				
					lastLetter = letter;
				}
			}
			bodyNode.appendChild(div3);
		}

		if (repos.length > 0) {
			Element div4 = html.div("left");
			div4.appendChild(html.h2(translate("repositoryIndex")));
			lastLetter = "";
			for (Entity repo : repos) { 
				String letter = repo.toString().substring(0, 1); // Get first letter
				if (! letter.equals(lastLetter)) {
					div4.appendChild(html.link(listRepositoryFileName + "#" + letter, letter));				
					div4.appendChild(html.text(", "));				
					lastLetter = letter;
				}
			}
			bodyNode.appendChild(div4);
			
		}
		
		makeFooter(bodyNode, html);
		html.toFile(startFile);
	}

	protected void makeEntityIndex(File dir, Entity[] sources, String name, String fileName) {
		name = translate(name);
		println("Making "+ name);
		File startFile = new File(dir.getAbsolutePath() + File.separator + fileName);
		Html html = new Html(name, "");
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		bodyNode.appendChild(html.h1(name));
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		String lastLetter = "";
		for (Entity source : sources) { 
			String text = source.toString();
			String letter = text.substring(0, 1); // Get first letter
			if (! letter.equals(lastLetter)) {
				div1.appendChild(html.anchor(letter));
				div1.appendChild(html.h2(letter));
				lastLetter = letter;
			}
			div1.appendChild(html.link(addressTo(source.getId()), text));
			div1.appendChild(html.br());
		}				
		html.toFile(startFile);
		makeFooter(bodyNode, html);
	}

	protected void makePersonIndex(File dir, Entity[] indis) {
		println("Making person index");
		File startFile = new File(dir.getAbsolutePath() + File.separator + listPersonFileName);
		Html html = new Html(translate("personIndex"), "");
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		bodyNode.appendChild(html.h1(translate("personIndex")));
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		String lastLetter = "";
		for (Entity indi : indis) { 
			String lastname = ((Indi)indi).getLastName();  
			String letter = "?";
			if (lastname != null && !lastname.isEmpty()) letter = lastname.substring(0, 1); // Get first letter of last name
			if (! letter.equals(lastLetter)) {
				div1.appendChild(html.anchor(letter));
				div1.appendChild(html.h2(letter));
				lastLetter = letter;
			}
			String text = getName((Indi)indi) + " (";
			if (!isPrivate((Indi)indi)) {
				PropertyDate birth = ((Indi)indi).getBirthDate();
				if (birth != null && birth.getStart().isValid()) text += birth.getStart().getYear();
				text += " - ";
				PropertyDate death = ((Indi)indi).getDeathDate();
				if (death != null && death.getStart().isValid()) text += death.getStart().getYear();
			} else {
				text += translate("notPublic");
			}
			text += ")";
			div1.appendChild(html.link(addressTo(indi.getId()), text));
			div1.appendChild(html.br());
		}				
		html.toFile(startFile);
		makeFooter(bodyNode, html);
	}

	protected class EntityComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity arg0, Entity arg1) {
			return arg0.toString().compareTo(arg1.toString());
		}
	}

	/**
	 * Make a directory for each object 
	 * @param id Id of the object
	 * @param dir The user selected output dir
	 * @return a File object with the directory
	 * @throws Exception
	 */
	protected File makeDirFor(String id, File dir) throws Exception {
		String path = addressTo(id);
		// Create the directory
		String fileSep = File.separator;
		if (fileSep.equals("\\")) {
			fileSep = "\\\\"; // Fix for Windows backslash separator
		}
		path = path.replaceAll("/", fileSep);
		File indiFile = new File(dir.getAbsolutePath() + File.separator + path);
		File indiDir = indiFile.getParentFile();
		indiDir.mkdirs();
		return indiFile;
	}

	/**
	 * Create a document for each individual
	 */
	protected Html createIndiDoc(Indi indi, File indiDir) {
		List<String> handledTags = new ArrayList<String>();
		
		String linkPrefix = relativeLinkPrefix(indi.getId());

		Html html = new Html(getName(indi), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();

		// Link to start and index-page
		Element divlink = html.div("backlink");
		bodyNode.appendChild(divlink);
		divlink.appendChild(html.link(linkPrefix + reportIndexFileName, translate("startPage")));
		divlink.appendChild(html.br());
		divlink.appendChild(html.link(linkPrefix + listPersonFileName, translate("personIndex")));
		
		// Add a decendant tree
		addDecendantTree(bodyNode, indi, "", linkPrefix, html);

		Property name = indi.getProperty("NAME");
		if (name != null) {
			Element h1 = html.h1(getName(indi));
			bodyNode.appendChild(h1);
			// SOUR - Sources
			Element sources = processSources(name, linkPrefix, html);
			if (sources != null) h1.appendChild(sources);
			// NOTE
			Element note = processNote(name.getProperty("NOTE"), linkPrefix, html);
			if (note != null) bodyNode.appendChild(html.p(note));
			reportUnhandledProperties(indi.getProperty("NAME"), new String[]{"SOUR","NOTE"});
		} else {
			bodyNode.appendChild(html.h1("("+translate("unknown")+")"));
		}
		handledTags.add("NAME");

		// Find out how much we may display		
		boolean isPrivate = isPrivate(indi); 
		
		Element div1 = html.div("left");
		if (! isPrivate) {
			div1.appendChild(html.h2(translate("facts")));
			// get sex
			div1.appendChild(html.p(Gedcom.getName("SEX") + ": " + 
					PropertySex.getLabelForSex(indi.getSex())));
			handledTags.add("SEX");
			reportUnhandledProperties(indi.getProperty("SEX"), null);
			// get birth/death
			Element birth = processEventDetail((PropertyEvent)indi.getProperty("BIRT"), 
					linkPrefix, indiDir, html, true); 
			if (birth != null) div1.appendChild(birth);
			handledTags.add("BIRT");
			Element death = processEventDetail((PropertyEvent)indi.getProperty("DEAT"), 
					linkPrefix, indiDir, html, true); 
			if (death != null) div1.appendChild(death);
			handledTags.add("DEAT");  
		}
		
		// Display parents
		div1.appendChild(html.h2(translate("parents")));
		List<PropertyFamilyChild> famRefs = indi.getProperties(PropertyFamilyChild.class);
		if (famRefs.isEmpty()) {
			div1.appendChild(html.p(translate("unknown")));
		} else {
			for (PropertyFamilyChild famRef : famRefs) {
				Fam fam = famRef.getFamily();
				Element p = html.p();
				div1.appendChild(p);
				Boolean bio = famRef.isBiological();
				if (! (bio == null || bio.booleanValue())) {
				    Property pedi = famRef.getProperty("PEDI");
				    if (pedi!=null) {
				    	p.appendChild(html.text(pedi.getValue() + ": "));
				    	p.appendChild(html.br());
				    }
				}
				getReferenceLink(famRef, p, linkPrefix, html, true);
				Element notes = processNotes(famRef, linkPrefix, html);
				if (notes != null) p.appendChild(notes);
				reportUnhandledProperties(famRef, new String[]{"PEDI", "NOTE"});
			}
		}
		handledTags.add("FAMC");

		// Find spouses and children
		List<PropertyFamilySpouse> famss = indi.getProperties(PropertyFamilySpouse.class);
		if (!famss.isEmpty()) {
			for (PropertyFamilySpouse pfs : famss) {
				Element h2 = html.h2(Gedcom.getName("FAM") + " - ");
				div1.appendChild(h2);
				Fam fam = pfs.getFamily();
				Indi spouse = fam.getOtherSpouse(indi);
				if (spouse != null) {
					h2.appendChild(html.link(linkPrefix + addressTo(spouse.getId()),getName(spouse)));
				} else {
					h2.appendChild(html.text(translate("unknown")));
				}
				// Notes on the link to family
				Element notesFAMS = processNotes(pfs, linkPrefix, html);
				if (notesFAMS != null) div1.appendChild(notesFAMS);
				
				if (! isPrivate) {
					Element sourceSup = processSources(fam, linkPrefix, html);
					if (sourceSup != null) {
						h2.appendChild(sourceSup);
					}
					// Event tags
					for (String tag : new String[] {"ENGA", "MARR", "MARB", "MARC", "MARL", "MARS", "EVEN", "ANUL", "CENS", "DIV", "DIVF"}) {
						for (Property event : fam.getProperties(tag)) {
							div1.appendChild(processEventDetail(event, linkPrefix, indiDir, html, true));
						}
					}
					// Single tags
					for (String tag : new String[] {"NCHI"}) {
						Property singleTag = fam.getProperty(tag);
						if (singleTag != null) {
							div1.appendChild(html.text(Gedcom.getName(tag) + ": " + singleTag.getDisplayValue()));
						}
					}
					Element notesP = processNotes(fam, linkPrefix, html);
					if (notesP != null) div1.appendChild(notesP);
					
					Element images = processObjects(fam, linkPrefix, indiDir, html, true);
					if (images != null)	div1.appendChild(images);
					handledTags.add("OBJE");
					
					reportUnhandledProperties(fam, new String[]{"HUSB", "WIFE", "CHIL", "CHAN", "NOTE", "SOUR", 
							"ENGA", "MARR", "MARB", "MARC", "MARL", "MARS", "EVEN", "ANUL", "CENS", "DIV", "DIVF",
							"NCHI", "OBJE"});
				}
				Indi[] children = fam.getChildren(true);
				if (children.length > 0) {
					div1.appendChild(html.p(Gedcom.getName("CHIL", true) + ":"));
					Element childrenList = doc.createElement("ul");
					for (Indi child : children) {
						Element childEl = doc.createElement("li");
						childEl.appendChild(html.link(linkPrefix + addressTo(child.getId()), getName(child)));
						childrenList.appendChild(childEl);
					}
					div1.appendChild(childrenList);
				}
				reportUnhandledProperties(pfs, null);
			}
		}
		handledTags.add("FAMS");
		bodyNode.appendChild(div1);

		// If the person is alive (or not confirmed dead), we have displayed enough
		if (isPrivate) return html;

		Element div2 = html.div("right");
		for (String tag : new String[]{"CAST", "DSCR", "EDUC", "IDNO", "NATI", "NCHI", "NMR", "OCCU", "PROP", "RELI", "RESI", "SSN", "TITL",
				"CHR", "CREM", "BURI", "BAPM", "BARM", "BASM", "BLES", "CHRA", "CONF", "FCOM", "ORDN", "NATU", "EMIG", "IMMI", "CENS", "PROB", "WILL", "GRAD", "RETI", "EVEN"}) {
			processOtherEventTag(tag, indi, linkPrefix, indiDir, div2, html);
			handledTags.add(tag);  
		}
		for (String tag : new String[]{"SUBM", "ALIA", "ANCI", "DESI"}) {
			Property[] refs = indi.getProperties(tag);
			if (refs.length > 0) {
				div2.appendChild(html.h2(Gedcom.getName(tag)));
				Element p = html.p();
				for (Property ref : refs) {
					if (ref instanceof PropertyXRef) {
						getReferenceLink((PropertyXRef)ref, p, linkPrefix, html, false);
						if (p.hasChildNodes()) div2.appendChild(p);
						reportUnhandledProperties(ref, null); // There should not be anything here
					} else {
						println(tag + " is not reference:" + ref.toString());
					}
				}
			}
			handledTags.add(tag);  
		}
		Property[] refs = indi.getProperties("ASSO");
		if (refs.length > 0) {
			div2.appendChild(html.h2(Gedcom.getName("ASSO")));
			for (Property ref : refs) {
				if (ref instanceof PropertyXRef) {
					Property relation = ref.getProperty("RELA"); // Must exist according to spec
					Element p = html.p(relation.getDisplayValue() + ": ");
					getReferenceLink((PropertyXRef)ref, p, linkPrefix, html, false);
					if (p.hasChildNodes()) div2.appendChild(p);
					Element notes = processNotes(ref, linkPrefix, html);
					if (notes != null) p.appendChild(notes);
					Element sources = processSources(ref, linkPrefix, html);
					if (sources != null) p.appendChild(sources);
					reportUnhandledProperties(ref, new String[] {"RELA", "NOTE", "SOUR"});
				} else {
					println("ASSO is not reference:" + ref.toString());
				}
			}
			handledTags.add("ASSO");  
		}
		
		
		if (div2.hasChildNodes()) bodyNode.appendChild(div2);

		// OBJE - Images etc
		Element p = processObjects(indi, linkPrefix, indiDir, html, false);
		if (p != null) {
			Element divImages = html.div("left");
			divImages.appendChild(html.h2(translate("images")));
			divImages.appendChild(p);
			bodyNode.appendChild(divImages);
			personsWithImage.add(indi); // Add to the list of persons displayed in the gallery
		}
		handledTags.add("OBJE");

		Element divBottom = html.div("bottom");
		bodyNode.appendChild(divBottom);
		processNumberNoteSourceChangeRest(indi, linkPrefix, divBottom, html, handledTags);
		
		makeFooter(bodyNode, html);
		return html;
	}

	/**
	 * Create a document for each source
	 */
	protected Html createSourceDoc(Source source, File sourceDir) {
		List<String> handledTags = new ArrayList<String>();
		
		String linkPrefix = relativeLinkPrefix(source.getId());

		Html html = new Html(Gedcom.getName("SOUR") + " " + source.getId() + ": " + source.getTitle(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		
		Element divlink = html.div("backlink");
		bodyNode.appendChild(divlink);
		divlink.appendChild(html.link(linkPrefix + reportIndexFileName, translate("startPage")));
		divlink.appendChild(html.br());
		divlink.appendChild(html.link(linkPrefix + listSourceFileName, translate("sourceIndex")));
		
		bodyNode.appendChild(html.h1(source.getTitle()));
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		handledTags.add("TITL");

		processSimpleTags(source, new String[] {"TEXT", "AUTH", "ABBR", "PUBL"}, div1, html, handledTags);

		// REPO
		for (PropertyRepository repo : source.getProperties(PropertyRepository.class)) {
			div1.appendChild(html.h2(Gedcom.getName("REPO")));
			Element p = html.p();
			div1.appendChild(p);
			
			Repository ent = (Repository)repo.getTargetEntity();
			// Make a link to it
			p.appendChild(html.link(linkPrefix + addressTo(ent.getId()), ent.toString()));
			// Handle CALN, numbering
			for (Property caln : repo.getProperties("CALN")) {
				p.appendChild(html.text(", " + caln.getDisplayValue()));
				Property medi = caln.getProperty("MEDI");
				if (medi != null) p.appendChild(html.text(medi.getDisplayValue()));
				reportUnhandledProperties(caln, new String[] {"MEDI"});
			}
			// Handle notes
			Element notes = processNotes(repo, linkPrefix, html);
			if (notes != null) div1.appendChild(notes);
			
			reportUnhandledProperties(repo, new String[] {"NOTE", "CALN"});
		}
		handledTags.add("REPO");
		
		// OBJE - Images etc
		Element images = processObjects(source, linkPrefix, sourceDir, html, false);
		if (images != null) {			
			div1.appendChild(html.h2(translate("images")));
			div1.appendChild(images);
		}
		handledTags.add("OBJE");
		
		Element div2 = html.div("right");
		bodyNode.appendChild(div2);
		processReferences(source, linkPrefix, div2, html, handledTags);
		
		Element divBottom = html.div("bottom");
		bodyNode.appendChild(divBottom);
		processNumberNoteSourceChangeRest(source, linkPrefix, divBottom, html, handledTags);

		makeFooter(bodyNode, html);
		return html;
	}

	protected Html createRepoDoc(Repository repo, File parentFile) {
		List<String> handledTags = new ArrayList<String>();
		String linkPrefix = relativeLinkPrefix(repo.getId());

		Html html = new Html(Gedcom.getName("REPO") + " " + repo.getId() + ": " + repo.toString(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		
		Element divlink = html.div("backlink");
		bodyNode.appendChild(divlink);
		divlink.appendChild(html.link(linkPrefix + reportIndexFileName, translate("startPage")));
		divlink.appendChild(html.br());
		divlink.appendChild(html.link(linkPrefix + listRepositoryFileName, translate("repositoryIndex")));
		
		bodyNode.appendChild(html.h1(repo.toString()));
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		handledTags.add("NAME");

		// ADDR
		Property addr = repo.getProperty("ADDR");
		if (addr != null) {
			div1.appendChild(html.h2(Gedcom.getName("ADDR")));
			div1.appendChild(html.p(addr.getDisplayValue()));
			for (String subTag : new String[] {"ADR1", "ADR2", "CITY", "STAE", "POST", "CTRY"}) {
				Property subProp = addr.getProperty(subTag);
				if (subProp != null) {
					div1.appendChild(html.p(Gedcom.getName(subTag) + ": " + subProp.getDisplayValue()));
				}
			}
			reportUnhandledProperties(addr, new String[] {"ADR1", "ADR2", "CITY", "STAE", "POST", "CTRY"});
		}
		handledTags.add("ADDR");

		processSimpleTags(repo, new String[] {"PHON"}, div1, html, handledTags); 

		// References
		Element div2 = html.div("right");
		bodyNode.appendChild(div2);
		processReferences(repo, linkPrefix, div2, html, handledTags);
		
		Element divBottom = html.div("bottom");
		bodyNode.appendChild(divBottom);
		processNumberNoteSourceChangeRest(repo, linkPrefix, divBottom, html, handledTags);

		makeFooter(bodyNode, html);
		return html;
	}

	protected void makeFooter(Element appendTo, Html html) {
		// Footer
		if (displayGenJFooter) {
			Element divFooter = html.div("footer");
			appendTo.appendChild(divFooter);
			Element p = html.p(translate("footerText") + " ");
			p.appendChild(html.link("http://genj.sourceforge.net/", "GenealogyJ"));
			divFooter.appendChild(p);
		}
	}

	protected String getName(Indi indi) {
		String name = indi.getName();
		if (sosaStradonitzNumber.get(indi.getId()) != null) { 
			name += " (" + sosaStradonitzNumber.get(indi.getId()) + ")";
		}
		return name;
	}

	/**
	 * 
	 * @param indi The person to check
	 * @return true if the person is dead, born before a date or settings allow all to be displayed, false otherwise 
	 */
	protected boolean isPrivate(Indi indi) {
		if (reportNowLiving) return false;
		if (indi.isDeceased()) return false;
		if (bornBeforeDate(indi)) return false;
		return true;
	}
	
	/**
	 * Helper method to isPrivate
	 * @param indi
	 * @return true if person is confirmed to be born before a certain date
	 */
	protected boolean bornBeforeDate(Indi indi) {
		if (indi.getBirthDate() != null && indi.getBirthDate().isComparable()) { 
			if (indi.getBirthDate().compareTo(new PropertyDate(1900)) < 0) return true;
			return false;
		}
		for (Indi child : indi.getChildren()) { //If parent to someone old (born before date above) 
			if (bornBeforeDate(child)) return true;
		}
		return false; // Not confirmed born before the date
	}
	
	protected void processReferences(Property ent, String linkPrefix,
			Element appendTo, Html html, List<String> handledTags) {
		// List who is referencing this source, not part of the source file but exists when running the code  
		List<PropertyXRef> refs = ent.getProperties(PropertyXRef.class);
		appendTo.appendChild(html.h2(translate("references")));
		Element p = html.p();
		appendTo.appendChild(p);
		for (PropertyXRef ref : refs) {
			getReferenceLink(ref, p, linkPrefix, html, true);
		}
		handledTags.add("XREF");
	}

	protected void getReferenceLink(PropertyXRef ref, Element appendTo,
			String linkPrefix, Html html, boolean addNewline) {
		if (ref.isValid()) {
			Entity refEnt = ref.getTargetEntity();
			if (refEnt instanceof Indi) {
				// Make a link to it if indi
				appendTo.appendChild(html.link(linkPrefix + addressTo(refEnt.getId()), getName((Indi)refEnt)));
				if (addNewline) appendTo.appendChild(html.br());
			} else if (refEnt instanceof Fam) {
				// make a link to the man & wife, if family
				Indi husb = ((Fam)refEnt).getHusband();
				Indi wife = ((Fam)refEnt).getWife();
				if (husb != null) {
					appendTo.appendChild(html.link(linkPrefix + addressTo(husb.getId()), getName(husb)));
					if (addNewline || wife != null) appendTo.appendChild(html.br());
				}
				if (wife != null) {
					appendTo.appendChild(html.link(linkPrefix + addressTo(wife.getId()), getName(wife)));
					if (addNewline) appendTo.appendChild(html.br());
				}
			} else {
				appendTo.appendChild(html.link(linkPrefix + addressTo(refEnt.getId()), refEnt.toString()));
				if (addNewline) appendTo.appendChild(html.br());
			}
		}
	}

	
	
	/**
	 * Handles images in OBJE-properties
	 * @param prop The Property containing the OBJE-properties
	 * @param linkPrefix
	 * @param dstDir
	 * @param html
	 * @param smallThumbs Making the thumbs really small (intended for family images)
	 * @return paragraph with images, or null
	 */
	protected Element processObjects(Property prop, String linkPrefix, File dstDir,
			Html html, boolean smallThumbs) {
		Property[] objects = prop.getProperties("OBJE");
		if (objects.length == 0) return null;
		Element p = html.p();
		int imgSize = 200;
		if (smallThumbs) imgSize = 100;
		for (int i = 0; i < objects.length; i++){
			// Get the title
			Property titleProp = objects[i].getProperty("TITL");
			String title = null;
			if (titleProp != null) title = titleProp.getValue(); // XXX title is not used
			// Get form of object
			Property formProp = objects[i].getProperty("FORM");
			if (formProp != null) {
				if (! formProp.getValue().matches("^jpe?g|gif|JPE?G|gif|PNG|png$")) {
					println("  Currently unsupported FORM in OBJE:" + formProp.getValue());
				}
			}
			// Find image
			PropertyFile file = (PropertyFile)objects[i].getProperty("FILE");
			if (file != null) {
				// Copy the file to dstDir
				File srcFile = file.getFile();
				if (srcFile != null) {
					File dstFile = new File(dstDir, srcFile.getName());
					File thumbFile = new File(dstFile.getParentFile(), "thumb_" + dstFile.getName());
					try {
						copyFile(srcFile, dstFile);
						// Create a thumb
						makeThumb(dstFile, imgSize, imgSize, thumbFile);
						// For the ancestor tree on other pages
						if (i == 0) makeThumb(dstFile, 50, 70, new File(dstFile.getParentFile(), "tree.jpg"));
						// Make img-reference to the image
						p.appendChild(html.link(dstFile.getName(), html.img(thumbFile.getName(), title)));
					} catch (IOException e) {
						println("  Error in copying file or making thumb: " + 
								srcFile.getName() + e.getMessage());
					}
					reportUnhandledProperties(objects[i], new String[]{"FILE", "TITL", "FORM"});
				} else {
					println("  FILE ref but no file was found");
				}
			} else {
				println("  OBJE without FILE is currently not handled");
			}
		}
		if (p.hasChildNodes()) return p;
		return null;
	}

	/**
	 * Handle:
	 *  +1 <<SOURCE_CITATION>>  {0:M}
	 *  +1 <<NOTE_STRUCTURE>>  {0:M}
	 *  +1 RFN <PERMANENT_RECORD_FILE_NUMBER>  {0:1}
	 *  +1 AFN <ANCESTRAL_FILE_NUMBER>  {0:1}
	 *  +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
	 *   +2 TYPE <USER_REFERENCE_TYPE>  {0:1}
	 *  +1 RIN <AUTOMATED_RECORD_ID>  {0:1}
	 *  +1 <<CHANGE_DATE>>  {0:1}
	 * @param prop
	 * @param linkPrefix
	 * @param appendTo
	 * @param html
	 */
	protected void processNumberNoteSourceChangeRest(Property prop, String linkPrefix,
			Element appendTo, Html html, List<String> handledTags) {

		// SOUR
		Element sources = processSources(prop, linkPrefix, html);
		if (sources != null) {
			appendTo.appendChild(html.h2(Gedcom.getName("SOUR", true)));
			appendTo.appendChild(sources);
		}
		handledTags.add("SOUR");
		
		// NOTE
		Element notes = processNotes(prop, linkPrefix, html);
		if (notes != null) {
			appendTo.appendChild(html.h2(Gedcom.getName("NOTE", true)));
			appendTo.appendChild(notes);
		}
		handledTags.add("NOTE");
		
		/*  +1 RFN <PERMANENT_RECORD_FILE_NUMBER>  {0:1}
		 *  +1 AFN <ANCESTRAL_FILE_NUMBER>  {0:1}
		 *  +1 RIN <AUTOMATED_RECORD_ID>  {0:1}		 */
		processSimpleTags(prop, new String[] {"RFN", "AFN", "RIN"}, appendTo, html, handledTags);
		/*  +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
		 *   +2 TYPE <USER_REFERENCE_TYPE>  {0:1}  */
		Property[] refns = prop.getProperties("REFN");
		if (refns.length > 0) {
			appendTo.appendChild(html.h2(Gedcom.getName("REFN")));
			for (Property refn : refns) {
				Element p = html.p(refn.getDisplayValue());
				Property type = refn.getProperty("TYPE");
				if (type != null) p.appendChild(html.text(" (" + type.getDisplayValue() + ")"));
				appendTo.appendChild(p);
				reportUnhandledProperties(refn, new String[] {"TYPE"});
			}
			handledTags.add("REFN");
		}

		// CHAN
		appendTo.appendChild(html.h2(translate("other")));
		PropertyChange lastUpdate = (PropertyChange)prop.getProperty("CHAN");
		if (lastUpdate != null) {
			Element p = html.p(translate("dataUpdated") + 
					" " + lastUpdate.getDisplayValue());
			appendTo.appendChild(p);
			handledTags.add("CHAN");
			Element chanNotes = processNotes(prop, linkPrefix, html);
			if (notes != null) {
				p.appendChild(html.text(" "));
				p.appendChild(chanNotes);
			}
			reportUnhandledProperties(lastUpdate, new String[] {"NOTE"});
		}
		appendTo.appendChild(html.p(translate("pageCreated") + 
				" " + (new PropertyChange()).getDisplayValue()));
		
		// Add all other attributes
		reportUnhandledProperties(prop, (String[])handledTags.toArray(new String[0])); 
		Element otherProperties = getAllProperties(prop, html, handledTags);
		if (otherProperties != null)
		appendTo.appendChild(otherProperties);


	}

	/**
	 * Handle simple text tags without any sub tags
	 * @param prop
	 * @param tags
	 * @param appendTo
	 * @param html
	 * @param handledTags
	 */
	protected void processSimpleTags(Property prop, String[] tags, Element appendTo, Html html, List<String> handledTags) {
		for (String tag : tags) {
			processSimpleTag(prop, tag, appendTo, html, handledTags);
		}
	}

	/**
	 * Handle simple text tags without any sub tags
	 * @param prop
	 * @param tag
	 * @param appendTo
	 * @param html
	 * @param handledTags
	 */
	protected void processSimpleTag(Property prop, String tag, Element appendTo, Html html, List<String> handledTags) {
		Property[] subProps = prop.getProperties(tag);
		if (subProps.length > 0) {
			appendTo.appendChild(html.h2(Gedcom.getName(tag)));
			for (Property subProp : subProps) {
				appendTo.appendChild(html.p(subProp.getDisplayValue()));
				reportUnhandledProperties(subProp, null);
			}
		}
		handledTags.add(tag);
	}
	
	protected void processOtherEventTag(String tag, Property prop, String linkPrefix,
			File dstDir, Element appendTo, Html html) {
		Property[] subProp = prop.getProperties(tag);
		if (subProp.length == 0) return;
		appendTo.appendChild(html.h2(Gedcom.getName(tag)));
		for (int i = 0; i < subProp.length; i++){
			appendTo.appendChild(processEventDetail(subProp[i], linkPrefix, dstDir, html, false));
		}
	}

	/** 
	 * Handles both EVEN and some other types BIRT, DEAT, BURY, etc
	 * EVENT_DETAIL: =
	 *  X n  TYPE <EVENT_DESCRIPTOR>  {0:1}
	 *  X n  DATE <DATE_VALUE>  {0:1}
	 *  X n  <<PLACE_STRUCTURE>>  {0:1}
	 *  X n  <<ADDRESS_STRUCTURE>>  {0:1}
	 *   n  AGE <AGE_AT_EVENT>  {0:1}
	 *   n  AGNC <RESPONSIBLE_AGENCY>  {0:1}
	 *   n  CAUS <CAUSE_OF_EVENT>  {0:1}
	 *  X n  <<SOURCE_CITATION>>  {0:M}
	 *   n  <<MULTIMEDIA_LINK>>  {0:M}
	 *  X n  <<NOTE_STRUCTURE>>  {0:M}
	 *
	 *  For full support: (also handle)
	 *    FAMC @<XREF:FAM>@  {0:1}  (BIRT/CHR/ADOP)
	 *         +1 ADOP <ADOPTED_BY_WHICH_PARENT>  {0:1}   (ADOP)
	 */
	
	protected Element processEventDetail(Property event, String linkPrefix, 
			File dstDir, Html html, boolean displayTagDescription) {
		if (event == null) return null;
		Element p = html.p();

		if (displayTagDescription) {
			String description = "";
			if (!event.getTag().equals("EVEN")) {
				p.appendChild(html.text(Gedcom.getName(event.getTag()) + ": "));
			}
		}
		Property type = event.getProperty("TYPE");
		if (type != null) {
			p.appendChild(html.text(type.getDisplayValue() + " "));
		}

		p.appendChild(html.text(event.getDisplayValue() + " "));
		
		// DATE - DATE_VALUE
		PropertyDate date = (PropertyDate)event.getProperty("DATE");
		if (date != null) 
			p.appendChild(html.text(date.getDisplayValue() + " "));
		// PLAC - PLACE STRUCTURE
		Element place = processPlace(event.getProperty("PLAC"), linkPrefix, html);
		if (place != null) p.appendChild(place);
		// ADDRESS_STRUCTURE
		Element address = processAddress(event.getProperty("ADDR"), html);
		if (address != null) p.appendChild(address);
		// SOUR - Sources
		Element sources = processSources(event, linkPrefix, html);
		if (sources != null) p.appendChild(sources);
		// NOTE
		Element note = processNote(event.getProperty("NOTE"), linkPrefix, html);
		if (note != null) {
			p.appendChild(html.br());
			p.appendChild(note);
		}
		// AGE, AGNC, CAUS
		for (String tag : new String[] {"AGE", "AGNC", "CAUS"}) {
			Property tagProp = event.getProperty(tag);
			if (tagProp != null) p.appendChild(html.text(Gedcom.getName(tag) + " " + tagProp.getDisplayValue()));
		}
		// FAMC, FAMC:ADOP (for those events supporting that)
		Property famRef = event.getProperty("FAMC");
		if (famRef != null) { 
			if (famRef instanceof PropertyXRef) {
				Fam fam = (Fam)((PropertyXRef)famRef).getTargetEntity();
				Property adoptedBy = famRef.getProperty("ADOP");
				if (adoptedBy != null) makeLinkToFamily(p, fam, adoptedBy.getValue(), linkPrefix, html);
				else makeLinkToFamily(p, fam, null, linkPrefix, html);
			} else {
				println(event.getTag() + ":FAMC is not a reference:" + event.getValue());
			}
		}
		// OBJE - MULTIMEDIA
		Element pObj = processObjects(event, linkPrefix, dstDir, html, true);
		if (pObj != null && pObj.hasChildNodes()) {
			NodeList nl = pObj.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) p.appendChild(nl.item(i));
		}
		
		reportUnhandledProperties(event, new String[]{"DATE", "PLAC", "TYPE", "NOTE", "SOUR", "ADDR", "AGE", "AGNC", "CAUS", "FAMC"});
		return p;
	}

	protected void makeLinkToFamily(Element appendTo, Fam fam, String memberOfFamily, String linkPrefix, Html html) {
		Indi husb = fam.getHusband();
		Indi wife = fam.getWife();
		if (memberOfFamily == null || memberOfFamily.equals("BOTH")) {
			if (husb != null) {
				appendTo.appendChild(html.link(linkPrefix + addressTo(husb.getId()), getName(husb)));
				if (wife != null) appendTo.appendChild(html.text(" " + translate("and") + " "));
			}
			if (wife != null) appendTo.appendChild(html.link(linkPrefix + addressTo(wife.getId()), getName(wife)));
		} else {
			if (memberOfFamily.equals("WIFE")) {
				if (wife != null) appendTo.appendChild(html.link(linkPrefix + addressTo(wife.getId()), getName(wife)));
			} else if (memberOfFamily.equals("HUSB")) {
				if (husb != null) appendTo.appendChild(html.link(linkPrefix + addressTo(husb.getId()), getName(husb)));
			} else {
				println("Invalid value on member of family:" + memberOfFamily);
			}
		}
	}
	
	protected Element processPlace(Property place, String linkPrefix, Html html) {
		if (place == null) return null;
		Element span = html.span("place", placeDisplayFormat.equals("all") ? place.getValue() : place.format(placeDisplayFormat).replaceAll("^(,|(, ))*", "").trim());
		// SOUR - Sources
		Element sources = processSources(place, linkPrefix, html);
		if (sources != null) span.appendChild(sources);
		// NOTE
		Element note = processNote(place.getProperty("NOTE"), linkPrefix, html);
		if (note != null) {
			span.appendChild(html.br());
			span.appendChild(note);
		}
		// MAP - Geografic position
		Property map = place.getProperty("MAP");
		if (map != null && reportLinksToMap) {
			String latitude = map.getProperty("LATI").getDisplayValue();
			String longitude = map.getProperty("LONG").getDisplayValue();
			if (latitude.startsWith("S") || latitude.startsWith("s")) latitude = "-" + latitude.substring(1);
			else latitude = latitude.substring(1);
			if (longitude.startsWith("W") || longitude.startsWith("w")) longitude = "-" + longitude.substring(1);
			else longitude = longitude.substring(1);
			span.appendChild(html.text(" "));
			span.appendChild(html.link(translate("mapLink", new Object[] {latitude, longitude}),
					translate("linkToMap")));
			reportUnhandledProperties(map, new String[]{"LATI", "LONG"});
		}
		reportUnhandledProperties(place, new String[]{"SOUR", "NOTE", "MAP"});
		return span;
	}

	protected Element processAddress(Property address, Html html) {
		if (address == null) return null;
		Element span = html.span("address");
		span.appendChild(html.text(address.getDisplayValue()));
		String[] subTags = new String[]{"ADR1", "ADR2", "CITY", "STAE", "POST", "CTRY"};
		for (int i = 0; i < subTags.length; i++) {
			Property subProp = address.getProperty(subTags[i]);
			if (subProp != null) span.appendChild(html.text(", " + subProp.getDisplayValue()));
		}
		reportUnhandledProperties(address, subTags);
		return span;
	}

	/** 
	 * Handles all SOUR-stuff in prop 
	 * @return a <sup> element or null
	 */
	protected Element processSources(Property prop, String linkPrefix, Html html) {
		Property[] sources = prop.getProperties("SOUR");
		if (sources.length > 0) {
			Element sup = html.sup("source");
			for (int i = 0; i < sources.length; i++) {
				Source source = (Source)((PropertySource)sources[i]).getTargetEntity();
				if (i > 0) sup.appendChild(html.text(", "));
				sup.appendChild(html.link(linkPrefix + addressTo(source.getId()), 
						source.getId()));
				// It may contain subtags according to gedcom spec
				reportUnhandledProperties(sources[i], null);
			}
			return sup;
		}
		return null;
	}

	protected Element processNotes(Property prop, String linkPrefix, Html html) {
		Property[] notes = prop.getProperties("NOTE");
		if (notes.length > 0) {
			Element p = html.p(Gedcom.getName("NOTE", true) + ": ");
			for (int i = 0; i < notes.length; i++) {
				p.appendChild(processNote(notes[i], linkPrefix, html));
			}
			return p;
		}
		return null;
	}

	/**
	 * Handles a note
	 * @return A <span>-element
	 */
	protected Element processNote(Property note, String linkPrefix, Html html) {
		if (note == null) return null;
		Element noteEl = html.spanNewlines("note", note.getDisplayValue());
		Element sourcesEl = processSources(note, linkPrefix, html);
		if (sourcesEl != null) noteEl.appendChild(sourcesEl);
		reportUnhandledProperties(note, new String[]{"SOUR"});
		return noteEl;
	}

	protected void addDecendantTree(Element whereToAdd, Indi indi, String relation, String linkPrefix, Html html) {
		if (indi == null) return;
		// Add him/herself
		String relationClass = relation;
		if (relation.length() == 0) relationClass = "ident";
		Element div = html.div("anc " + relationClass);
		Element link = html.link(linkPrefix + addressTo(indi.getId()), getName(indi));
		link.appendChild(html.br());
		if (!isPrivate(indi)) {
			// Display dates
			PropertyDate birthDate = indi.getBirthDate();
			if (birthDate != null) {
				link.appendChild(html.text(birthDate.getDisplayValue()));	
			}
			PropertyDate deathDate = indi.getDeathDate();
			if (deathDate != null) {
				link.appendChild(html.text(" -- " + deathDate.getDisplayValue()));	
			}
		}
		div.appendChild(link);
		whereToAdd.appendChild(div);
	
		// Add parents		
		Indi f = indi.getBiologicalFather();
		Indi m = indi.getBiologicalMother();
		if (f != null || m != null) {
			div.appendChild(html.div("l1", " "));
			div.appendChild(html.div("l2", " "));
			if (relation.length() == 2) { // More lines are drawn at that level in the tree
				div.appendChild(html.div("l3", " "));
				div.appendChild(html.div("l4", " "));
			}
			if (relation.length() < 3) {
				addDecendantTree(whereToAdd, m, relation + "m", linkPrefix, html);
				addDecendantTree(whereToAdd, f, relation + "f", linkPrefix, html);
			}
		}
		// Just to have a css-class to make a distance to the text below the tree
		if (relation.length() == 0) {
			Element p = html.p();
			p.setAttribute("class", "treeMargin");
			whereToAdd.appendChild(p);
		}
	
	}

	protected void reportUnhandledProperties(Property current, String[] handled) {
		Property[] properties = current.getProperties();
		if (properties.length == 0) return;
		for (int i = 0; i < properties.length; i++) {
			String tag = properties[i].getTag();
			if (! isIn(tag, handled)) {
				println("  Unhandled tag:" + current.getTag() + ":" + tag);
			}
		}
	}

	protected boolean isIn (String value, String[] list) {
		if (list == null) return false;
		for (int i = 0; i < list.length; i++) {
			if (value.equals(list[i])) return true;
		}
		return false;
	}

	protected Element getAllProperties(Property current, Html html, List<String> ignore) {
		// Add all other attributes
		Property[] properties = current.getProperties();
		if (properties.length > 0) {
			Element propertiesList = html.ul();
			for (int i = 0; i < properties.length; i++) {
				if (ignore == null || ! ignore.contains(properties[i].getTag())) {
					Element li = html.li(properties[i].getTag() + " " +
							properties[i].getDisplayValue());
					Element subProperties = getAllProperties(properties[i], html, null);
					if (subProperties != null) li.appendChild(subProperties);
					propertiesList.appendChild(li);
				}
			}
			if (propertiesList.hasChildNodes())	return propertiesList;
		}
		return null;
	}

	/**
	 * Check color settings
	 */
	protected HashMap<String, String> makeCssColorSettings() {
		HashMap<String, String> translator = new HashMap<String, String>();
		addColorToMap(translator, "cssTextColor", cssTextColor);
		addColorToMap(translator, "cssBackgroundColor", cssBackgroundColor);
		addColorToMap(translator, "cssLinkColor", cssLinkColor);
		addColorToMap(translator, "cssVistedLinkColor", cssVistedLinkColor);
		addColorToMap(translator, "cssBorderColor", cssBorderColor);
		return translator;
	}

	protected void addColorToMap(HashMap<String, String> translator, String name, String value) {
		final Pattern colorPattern = Pattern.compile("[0-9a-fA-F]{3}|[0-9a-fA-F]{6}");
		if (! colorPattern.matcher(value).matches()) {
			throw new InvalidParameterException(name + " has incorrect value: " + value);
		}
		translator.put(name, value);
	}

	/**
	 * Make a css, with current settings
	 * @param dir The output directory
	 * @throws IOException in case of file error
	 */
	protected void makeCss(File dir, HashMap<String, String> translator) throws IOException {
		println("Making css-file");
		copyTextFileModify(getFile().getParentFile().getAbsolutePath() + File.separator + cssBaseFile,
				dir.getAbsolutePath() + File.separator + "style.css", translator, false);	
		copyTextFileModify(getFile().getParentFile().getAbsolutePath() + File.separator + cssTreeFile[treeType],
				dir.getAbsolutePath() + File.separator + "style.css", translator, true);	
	}

	/**
	 * Calculate the address of an object
	 * Make a directory structure that works for many objecs
	 * @return the address excluding any leading /. For example "indi4/04/12/index.html"
	 */
	protected String addressTo(String id) {
		return addressToDir(id) + reportIndexFileName;
	}

	/**
	 * Calculate the address of an objects directory
	 * Make a directory structure that works for many objecs
	 * @return the address excluding any leading /. For example "indi4/04/12/"
	 */
	protected String addressToDir(String id) {
		StringBuffer address = new StringBuffer();
		// Check the type of object
		String prefix = "unknown";
		String type = id.substring(0, 1);
		if (type.equals("I")) {	prefix = "indi"; }
		if (type.equals("S")) {	prefix = "source"; }
		if (type.equals("R")) {	prefix = "repository"; }
		address.append(prefix);
		// Get the id-number
		String idString = id.substring(1); // Remove leading I
		int i = idString.length();
		if (i % 2 == 1) { 
			i += 1; 
			idString = "0" + idString;
		}
		address.append(i);
		// Create the address
		while (idString.length() > 0) {
			address.append('/').append(idString.substring(0, 2));
			idString = idString.substring(2);
		}
		address.append('/');
		return address.toString();
	}

	/**
	 * At what depth in the file tree is an object located
	 */
	protected int addressDepth(String id) {
		return id.length() / 2 + 1;
	}

	/**
	 * Calculate the address of a relative link
	 */
	protected String relativeLinkPrefix(String fromId) {
		StringBuffer address = new StringBuffer();
		for (int i = 0; i < addressDepth(fromId); i++) {
			address.append("../");
		}
		return address.toString();
	}

	/**
	 * Make a thumb that fits into width wmax and height hmax
	 */
	protected void makeThumb(File imgFile, int wmax, int hmax, File thumbFile) throws IOException {
		BufferedImage originalImage = ImageIO.read(imgFile);
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		float wscale = (float)wmax / width;
		float hscale = (float)hmax / height;
		if (wscale > hscale) wscale = hscale;
		width = (int)(width * wscale);
		height = (int)(height * wscale);
		BufferedImage thumbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(originalImage, 0, 0, width, height, null);
		ImageIO.write(thumbImage, "jpg", thumbFile);
	}

	protected void copyFile(File src, File dst) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1)
				out.write(buffer, 0, bytesRead); // write
		} finally {
			if (in != null) in.close();
			if (out != null) out.close();
		}
	}

	protected void copyTextFileModify(String inFile, String outFile, HashMap<String,String> translator, boolean append) throws IOException {
		final Pattern replacePattern = Pattern.compile(".*\\{(\\w+)\\}.*");
		BufferedReader in = null;
		BufferedWriter out = null;
		try {
			in = new BufferedReader(new FileReader(inFile));
			out = new BufferedWriter(new FileWriter(outFile, append));
			String buffer = in.readLine();
			while (buffer != null) {
				Matcher m = replacePattern.matcher(buffer);
				while (m.matches()) {
					String key = m.group(1);
					buffer = buffer.replaceAll("\\{"+key+"\\}", translator.get(key));
					m = replacePattern.matcher(buffer);
				}
				out.write(buffer);
				out.newLine();
				buffer = in.readLine();
			}
		} finally {
			if (in != null) in.close();
			if (out != null) out.close();
		}
	}

	
}

	