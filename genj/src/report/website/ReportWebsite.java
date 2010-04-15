package website;
/**
 * A report for making a website with all information
 * 
 * author   = Erik Melkersson, erik.melkersson@gmail.com
 * version  = 0.2 beta
 * category = Chart
 * name     = Website
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyRepository;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import genj.gedcom.MultiLineProperty.Iterator;
import genj.option.CustomOption;
import genj.option.Option;
import genj.report.Report;
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
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
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
import org.w3c.dom.Node;
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
    
    /** Used for handling note and sources on a person/item */
    protected Element sourceDiv = null;
    protected int sourceCounter = 0;
    protected Element noteDiv = null;
    protected int noteCounter = 0;

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
		makeCssAndJs(dir, translator);
		
	    // Iterate over all individuals
		Entity[] indis = gedcom.getEntities(Gedcom.INDI, "");
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

	    // Iterate over all media objects
		Entity[] objects = gedcom.getEntities(Gedcom.OBJE, "");
		for(Entity object : objects) {
			println("Exporting object " + object.getId());
			File indiFile = makeDirFor(object.getId(), dir);
			createMultimediaDoc((Media)object, indiFile.getParentFile()).toFile(indiFile);
		}

	    // Iterate over all notes
		Entity[] notes = gedcom.getEntities(Gedcom.NOTE, "");
		for(Entity note : notes) {
			println("Exporting note " + note.getId());
			File indiFile = makeDirFor(note.getId(), dir);
			createNoteDoc((Note)note, indiFile.getParentFile()).toFile(indiFile);
		}

	    // Iterate over all submitters
		Entity[] submitters = gedcom.getEntities(Gedcom.SUBM, "");
		for(Entity submitter : submitters) {
			println("Exporting submitter " + submitter.getId());
			File indiFile = makeDirFor(submitter.getId(), dir);
			createSubmitterDoc((Submitter)submitter, indiFile.getParentFile()).toFile(indiFile);
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
		makeSearchDataPage(dir, indis);
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

	protected void makeSearchDataPage(File dir, Entity[] indis) throws IOException {
		println("Making search data file");
		File file = new File(dir, "searchData.js");
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")); 
			out.write("var searchValues = [");
			boolean first = true;
			for (Entity indi : indis) {
				if (!first) {
					out.write(",");
					out.newLine();
				}
				first = false;
				String displayName = ((Indi)indi).getName().replace('"', ' ');
				String simpleName = displayName.toLowerCase();
				out.write("[\"" + simpleName + "\",\"" + indi.getId().substring(1) + "\",\"" + displayName + "\"]");
				// XXX sosa-number too
			}
			out.write("];");
		} finally {
			if (out != null) out.close();
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
		bodyNode.appendChild(div1);
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
		// Search form
		// In head
		html.addJSFile("search.js");
		html.addJSFile("searchData.js");
		// Here
		Element searchForm = html.form(null, "return displayResult();"); //id, onsubmit
		searchForm.appendChild(html.text(Gedcom.getName("NAME")+" "));
		searchForm.appendChild(html.input("searchName", "name")); // id, name
		searchForm.appendChild(html.button(translate("searchButton"), "displayResult();")); // value, onclick
		div1.appendChild(searchForm);
		Element divResult = html.divId("searchResult");
		divResult.appendChild(html.text(" "));
		div1.appendChild(divResult);
		
/*		<a href="#" onclick="displaySimple();">Simple</a> 
		<a href="#" onclick="displayAdvanced();">Advanced</a>
		<div id="searchSimple"><form onsubmit="displayResult();">Name <input id="searchName" name="name"/><input onclick="displayResult();" type="button" value="Search"/></form></div>
		<div id="searchAdvanced" style="display:none;"><form>
		First name <input name="firstname"/><br />
		Surname <input name="surname"/><br />
		</form></div>
		<div id="searchResult" style="display:none;"></div>*/
		

		Element div2 = html.div("right");
		div2.appendChild(html.h2(translate("personGallery")));
		for (Indi indi : personsWithImage) { 
			div2.appendChild(html.link(addressTo(indi.getId()), html.img(addressToDir(indi.getId()) + "gallery.jpg", getName(indi))));				
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
		bodyNode.appendChild(backlink(null, "", html));
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
		makeFooter(bodyNode, html);
		html.toFile(startFile);
	}

	protected void makePersonIndex(File dir, Entity[] indis) {
		println("Making person index");
		File startFile = new File(dir.getAbsolutePath() + File.separator + listPersonFileName);
		Html html = new Html(translate("personIndex"), "");
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		bodyNode.appendChild(backlink(null, "", html));
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
		makeFooter(bodyNode, html);
		html.toFile(startFile);
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
		List<String> handledProperties = new ArrayList<String>();
		resetNoteAndSourceList();
				
		String linkPrefix = relativeLinkPrefix(indi.getId());

		Html html = new Html(getName(indi), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();

		// Add a decendant tree
		addDecendantTree(bodyNode, indi, "", linkPrefix, html);

		Property[] names = indi.getProperties("NAME");
		for (Property name : names) {
			Element h1 = html.h1(getName(indi, name));
			bodyNode.appendChild(h1);
			processSourceRefs(h1, name, linkPrefix, indiDir, html);
			processNoteRefs(h1, name, linkPrefix, indiDir, html);
			Property nick = name.getProperty("NICK");
			if (nick != null) {
				bodyNode.appendChild(html.p(Gedcom.getName("NICK") + ": " + nick.getDisplayValue()));
			}
			String constructedName = constructName(name); //NPFX, GIVN, SPFX, SURN, NSFX
			if (constructedName != null) bodyNode.appendChild(html.p(constructedName));

			for (String subTag : new String[] {"FONE", "ROMN"}) {
				Property fone = name.getProperty(subTag);
				if (fone != null) {
					String type = "";
					Property typeProp = fone.getProperty("TYPE"); // Should be here according to spec
					if (typeProp != null) type = typeProp.getDisplayValue(); 
					Element p = html.p(Gedcom.getName(subTag) + " " + type + ": " + typeProp.getDisplayValue());
					bodyNode.appendChild(p);
					Property foneNick = name.getProperty("NICK");
					String constructedFoneName = constructName(fone); //NPFX, GIVN, SPFX, SURN, NSFX
					if (constructedFoneName != null) p.appendChild(html.text(", " + constructedName));
					if (foneNick != null) p.appendChild(html.text(", " + 
							Gedcom.getName("NICK") + " " + foneNick.getDisplayValue()));
					processSourceRefs(p, fone, linkPrefix, indiDir, html);
					processNoteRefs(p, fone, linkPrefix, indiDir, html);
					reportUnhandledProperties(foneNick, new String[] {"TYPE", "SOUR", "NOTE", "NICK", "NPFX", "GIVN", "SPFX", "SURN", "NSFX"});
				}
			}

			reportUnhandledProperties(name, new String[]{"SOUR", "NOTE", "NICK", "NPFX", "GIVN", "SPFX", "SURN", "NSFX"});
		}
		if (names == null) bodyNode.appendChild(html.h1("("+translate("unknown")+")"));
		handledProperties.add("NAME");

		// Find out how much we may display		
		boolean isPrivate = isPrivate(indi); 
		
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		if (! isPrivate) {
			div1.appendChild(html.h2(translate("facts")));
			// get sex
			Property sex = indi.getProperty("SEX");
			if (sex != null) {
				div1.appendChild(html.p(Gedcom.getName("SEX") + ": " + 
						PropertySex.getLabelForSex(indi.getSex())));
				reportUnhandledProperties(sex, null);
			}
			handledProperties.add("SEX");
			// get birth/death
			Element birth = processEventDetail((PropertyEvent)indi.getProperty("BIRT"), 
					linkPrefix, indiDir, html, true); 
			if (birth != null) div1.appendChild(birth);
			handledProperties.add("BIRT");
			Element death = processEventDetail((PropertyEvent)indi.getProperty("DEAT"), 
					linkPrefix, indiDir, html, true); 
			if (death != null) div1.appendChild(death);
			handledProperties.add("DEAT");  

			for (String tag : new String[]{"CAST", "DSCR", "EDUC", "IDNO", "NATI", "NCHI", "NMR", "OCCU", "PROP", "RELI", "RESI", "SSN", "TITL", "FACT",
					"ADOP", "CHR", "CREM", "BURI", "BAPM", "BARM", "BASM", "BLES", "CHRA", "CONF", "FCOM", "ORDN", "NATU", "EMIG", "IMMI", "CENS", "PROB", "WILL", "GRAD", "RETI", "EVEN"}) {
				processOtherEventTag(tag, indi, linkPrefix, indiDir, div1, html);
				handledProperties.add(tag);  
			}
			for (String tag : new String[]{"SUBM", "ALIA", "ANCI", "DESI"}) {
				Property[] refs = indi.getProperties(tag);
				if (refs.length > 0) {
					div1.appendChild(html.h2(Gedcom.getName(tag)));
					Element p = html.p();
					for (Property ref : refs) {
						if (ref instanceof PropertyXRef) {
							getReferenceLink((PropertyXRef)ref, p, linkPrefix, html, false);
							if (p.hasChildNodes()) div1.appendChild(p);
							reportUnhandledProperties(ref, null); // There should not be anything here
						} else {
							println(tag + " is not reference:" + ref.toString());
						}
					}
				}
				handledProperties.add(tag);  
			}
			Property[] refs = indi.getProperties("ASSO");
			if (refs.length > 0) {
				div1.appendChild(html.h2(Gedcom.getName("ASSO")));
				for (Property ref : refs) {
					if (ref instanceof PropertyXRef) {
						Property relation = ref.getProperty("RELA"); // Must exist according to spec
						Element p = html.p(relation.getDisplayValue() + ": ");
						getReferenceLink((PropertyXRef)ref, p, linkPrefix, html, false);
						if (p.hasChildNodes()) div1.appendChild(p);
						processNoteRefs(p, ref, linkPrefix, indiDir, html);
						processSourceRefs(p, ref, linkPrefix, indiDir, html);
						reportUnhandledProperties(ref, new String[] {"RELA", "NOTE", "SOUR"});
					} else {
						println("ASSO is not reference:" + ref.toString());
					}
				}
				handledProperties.add("ASSO");  
			}
			
			// OBJE - Images etc
			Element p = processMultimediaLink(indi, linkPrefix, indiDir, html, false, true);
			if (p != null) {
				div1.appendChild(p);
				personsWithImage.add(indi); // Add to the list of persons displayed in the gallery
			}
			handledProperties.add("OBJE");
		}
		
		// *** Family div ***
		
		Element div2 = html.div("right");
		bodyNode.appendChild(div2);
		
		// Display parents
		div2.appendChild(html.h2(translate("parents")));
		List<PropertyFamilyChild> famRefs = indi.getProperties(PropertyFamilyChild.class);
		if (famRefs.isEmpty()) {
			div2.appendChild(html.p(translate("unknown")));
		} else {
			for (PropertyFamilyChild famRef : famRefs) {
				Fam fam = famRef.getFamily();
				Element p = html.p();
				div2.appendChild(p);
				Boolean bio = famRef.isBiological();
				if (! (bio == null || bio.booleanValue())) {
				    Property pedi = famRef.getProperty("PEDI");
				    if (pedi!=null) {
				    	p.appendChild(html.text(pedi.getValue() + ": "));
				    	p.appendChild(html.br());
				    }
				}
				Property status = famRef.getProperty("STAT");
				if (status != null) {
			    	p.appendChild(html.text(Gedcom.getName("STAT") + ": " + status.getDisplayValue()));
			    	p.appendChild(html.br());
				}
				getReferenceLink(famRef, p, linkPrefix, html, true);
				processNoteRefs(p, famRef, linkPrefix, indiDir, html);
				reportUnhandledProperties(famRef, new String[]{"PEDI", "NOTE"});
			}
		}
		handledProperties.add("FAMC");

		// Find spouses and children
		List<PropertyFamilySpouse> famss = indi.getProperties(PropertyFamilySpouse.class);
		if (!famss.isEmpty()) {
			for (PropertyFamilySpouse pfs : famss) {
				// XXX multimedia objects should be in family's directory 
				// instead	of indiDir, i.e. replace indiDir within this loop
				Element h2 = html.h2(Gedcom.getName("FAM") + " - ");
				div2.appendChild(h2);
				Fam fam = pfs.getFamily();
				if (fam == null) {
					println(" Reference to invalid family: " + pfs.getValue());
					continue;
				}
				Indi spouse = fam.getOtherSpouse(indi);
				if (spouse != null) {
					h2.appendChild(html.link(linkPrefix + addressTo(spouse.getId()),getName(spouse)));
				} else {
					h2.appendChild(html.text(translate("unknown")));
				}
				// Notes on the reference itself
				processNoteRefs(h2, pfs, linkPrefix, indiDir, html);
				
				List<String>handledFamProperties = new ArrayList<String>();
				handledFamProperties.add("HUSB");
				handledFamProperties.add("WIFE");
				if (! isPrivate) {
					// Event tags
					for (String tag : new String[] {"ENGA", "MARR", "MARB", "MARC", "MARL", "MARS", "EVEN", "ANUL", "CENS", "DIV", "DIVF"}) {
						for (Property event : fam.getProperties(tag)) {
							div2.appendChild(processEventDetail(event, linkPrefix, indiDir, html, true));
						}
						handledFamProperties.add(tag);
					}
					// Single tags
					for (String tag : new String[] {"NCHI"}) {
						Property singleTag = fam.getProperty(tag);
						if (singleTag != null) {
							div2.appendChild(html.text(Gedcom.getName(tag) + ": " + singleTag.getDisplayValue()));
						}
						handledFamProperties.add(tag);
					}
					Element images = processMultimediaLink(fam, linkPrefix, indiDir, html, true, false);
					if (images != null)	div2.appendChild(images);
					handledFamProperties.add("OBJE");
					handledFamProperties.add("CHIL"); //See below
					for (String tag : new String[]{"SUBM"}) {
						Property[] refs = indi.getProperties(tag);
						if (refs.length > 0) {
							div1.appendChild(html.h2(Gedcom.getName(tag)));
							Element p = html.p();
							for (Property ref : refs) {
								if (ref instanceof PropertyXRef) {
									getReferenceLink((PropertyXRef)ref, p, linkPrefix, html, false);
									if (p.hasChildNodes()) div1.appendChild(p);
									reportUnhandledProperties(ref, null); // There should not be anything here
								} else {
									println(tag + " is not reference:" + ref.toString());
								}
							}
						}
						handledFamProperties.add(tag);  
					}

					/* XXX Not yet handled 
				    +1 <<LDS_SPOUSE_SEALING>>  {0:M}
				    */
					processNumberNoteSourceChangeRest(fam, linkPrefix, div2, indiDir, html, handledFamProperties);
				}
				Indi[] children = fam.getChildren(true);
				if (children.length > 0) {
					div2.appendChild(html.p(Gedcom.getName("CHIL", true) + ":"));
					Element childrenList = doc.createElement("ul");
					for (Indi child : children) {
						Element childEl = doc.createElement("li");
						childEl.appendChild(html.link(linkPrefix + addressTo(child.getId()), getName(child)));
						childrenList.appendChild(childEl);
					}
					div2.appendChild(childrenList);
				}
				reportUnhandledProperties(pfs, null);
			}
		}
		handledProperties.add("FAMS");

		processNumberNoteSourceChangeRest(indi, linkPrefix, div1, indiDir, html, handledProperties);
		addNoteAndSourceList(bodyNode);
		
		// Link to start and index-page
		bodyNode.appendChild(backlink(listPersonFileName, linkPrefix, html));
		makeFooter(bodyNode, html);
		return html;
	}

	/**
	 * Construct a name based on PERSONAL_NAME_PIECES
	 * NICK is not inserted here
	 */
	protected String constructName(Property nameProp) {
		StringBuffer sb = new StringBuffer();
		for (String tag : new String[] {"NPFX", "GIVN", "SPFX", "SURN", "NSFX"}) {
			for (Property subProp : nameProp.getProperties(tag)) {
				if (sb.length() > 0) sb.append(' ');
				sb.append(subProp.getDisplayValue());
			}
		}
		if (sb.length() > 0) return sb.toString();
		else return null;
	}
	
	
	/**
	 * Create a document for each source
	 */
	protected Html createSourceDoc(Source source, File sourceDir) {
		List<String> handledProperties = new ArrayList<String>();
		resetNoteAndSourceList();
		
		String linkPrefix = relativeLinkPrefix(source.getId());

		Html html = new Html(Gedcom.getName("SOUR") + " " + source.getId() + ": " + source.getTitle(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		
		bodyNode.appendChild(html.h1(source.getTitle()));
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		handledProperties.add("TITL");

		processSimpleTags(source, new String[] {"TEXT", "AUTH", "ABBR", "PUBL"}, div1, html, handledProperties);

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
			processNoteRefs(div1, repo, linkPrefix, sourceDir, html);
			
			reportUnhandledProperties(repo, new String[] {"NOTE", "CALN"});
		}
		handledProperties.add("REPO");
		
		// DATA
		Property data = source.getProperty("DATA");
		if (data !=null) {
			div1.appendChild(html.h2(Gedcom.getName("DATA")));
			for (Property event : data.getProperties("EVEN")) {
				Element p = html.p(Gedcom.getName("EVEN") + ": ");
				for (String eventType : event.getValue().split(",")) {
					p.appendChild(html.text(Gedcom.getName(eventType.trim()) + " "));
				}
				// DATE
				Property date = event.getProperty("DATE");
				if (date != null) 
					p.appendChild(html.text(date.getDisplayValue() + " "));
				// PLAC
				Property placeProp = event.getProperty("PLAC");
				Element place = processPlace(placeProp, linkPrefix, sourceDir, html);
				if (place != null) p.appendChild(place);
				reportUnhandledProperties(event, new String[] {"DATE", "PLAC"});
			}
			Property agency = data.getProperty("AGNC");
			if (agency != null) {
				div1.appendChild(html.p(Gedcom.getName("AGNC") + ": " + agency.getDisplayValue()));
			}
			this.processNoteRefs(div1, data, linkPrefix, sourceDir, html);
			reportUnhandledProperties(data, new String[] {"EVEN", "AGNC", "NOTE"});
		}
		handledProperties.add("DATA");
		
		// OBJE - Images etc
		Element images = processMultimediaLink(source, linkPrefix, sourceDir, html, false, false);
		if (images != null) {			
			div1.appendChild(html.h2(translate("images")));
			div1.appendChild(images);
		}
		handledProperties.add("OBJE");
		
		Element div2 = html.div("right");
		bodyNode.appendChild(div2);
		processReferences(source, linkPrefix, div2, html, handledProperties);
		
		processNumberNoteSourceChangeRest(source, linkPrefix, div1, sourceDir, html, handledProperties);
		addNoteAndSourceList(bodyNode);

		bodyNode.appendChild(backlink(listSourceFileName, linkPrefix, html));
		makeFooter(bodyNode, html);
		return html;
	}

	protected Html createRepoDoc(Repository repo, File repoDir) {
		List<String> handledProperties = new ArrayList<String>();
		String linkPrefix = relativeLinkPrefix(repo.getId());
		resetNoteAndSourceList();

		Html html = new Html(Gedcom.getName("REPO") + " " + repo.getId() + ": " + repo.toString(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();
		
		bodyNode.appendChild(html.h1(repo.toString()));
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		handledProperties.add("NAME");

		// ADDR
		processAddresses(div1, repo, html, handledProperties, true);

		// References
		Element div2 = html.div("right");
		processReferences(repo, linkPrefix, div2, html, handledProperties);
		if (div2.hasChildNodes()) bodyNode.appendChild(div2);
		
		processNumberNoteSourceChangeRest(repo, linkPrefix, div1, repoDir, html, handledProperties);
		addNoteAndSourceList(bodyNode);

		bodyNode.appendChild(backlink(listRepositoryFileName, linkPrefix, html));
		makeFooter(bodyNode, html);
		return html;
	}

	protected Html createMultimediaDoc(Media object, File objectDir) {
		List<String> handledProperties = new ArrayList<String>();
		String linkPrefix = relativeLinkPrefix(object.getId());
		resetNoteAndSourceList();

		Html html = new Html(Gedcom.getName("OBJE") + " " + object.getId() + ": " + object.toString(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();

		bodyNode.appendChild(html.h1(object.getTitle()));
		
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);
		
		processSimpleTag(object, "TITL", div1, html, handledProperties);
		processSimpleTag(object, "FORM", div1, html, handledProperties);
		
		/* XXX BLOBs not handled yet 
		 * Only in 5.5, not in 5.5.1
	    +1 BLOB        {1:1}
	      +2 CONT <ENCODED_MULTIMEDIA_LINE>  {1:M}
	    +1 OBJE @<XREF:OBJE>@ {0:1}
		 */

		/* Handle FILE, 5.5.1
		+1 FILE <MULTIMEDIA_FILE_REFN> {1:M} p.54
		  +2 FORM <MULTIMEDIA_FORMAT> {1:1} p.54
		    +3 TYPE <SOURCE_MEDIA_TYPE> {0:1} p.62
		  +2 TITL <DESCRIPTIVE_TITLE> {0:1} p.48
		*/
		Element p = html.p();
		for (PropertyFile file : object.getProperties(PropertyFile.class)) {
			// Get title
			String title = null;
			Property titleProp = file.getProperty("TITL");
			if (titleProp != null) {
				title = titleProp.getDisplayValue();
				reportUnhandledProperties(titleProp, null);
			}
			// Get form of object
			Property formProp = file.getProperty("FORM");
			if (formProp != null) {
				if (! formProp.getValue().matches("^jpe?g|gif|JPE?G|gif|PNG|png$")) {
					println(" Currently unsupported FORM in OBJE:" + formProp.getValue());
				}
				Property type = formProp.getProperty("TYPE");
				if (type != null) {
					// XXX What to do with type?
					reportUnhandledProperties(type, null);
				}
				reportUnhandledProperties(formProp, new String[] {"TYPE"});
			}
			
			int imgSize = 100;
			// Copy the file to dstDir
			File srcFile = file.getFile();
			if (srcFile != null) {
				File dstFile = new File(objectDir, srcFile.getName());
				File thumbFile = new File(dstFile.getParentFile(), "thumb_" + dstFile.getName());
				try {
					if (!dstFile.exists() || !thumbFile.exists() || 
							srcFile.lastModified() > dstFile.lastModified()) {
						copyFile(srcFile, dstFile);
						// Create a thumb
						makeThumb(dstFile, imgSize, imgSize, thumbFile);
					}

					// Make img-reference to the image
					p.appendChild(html.link(dstFile.getName(), html.img(thumbFile.getName(), title)));
				} catch (IOException e) {
					println(" Error in copying file or making thumb: " + 
							srcFile.getName() + e.getMessage());
				}
			} else {
				println(" FILE ref but no file was found");
			}
			reportUnhandledProperties(file, new String[] {"TITL", "FORM"});
		}
		if (p.hasChildNodes()) div1.appendChild(p);
		handledProperties.add("FILE");
		
		// References
		Element div2 = html.div("right");
		processReferences(object, linkPrefix, div2, html, handledProperties);
		if (div2.hasChildNodes()) bodyNode.appendChild(div2);

		processNumberNoteSourceChangeRest(object, linkPrefix, div1, objectDir, html, handledProperties);
		addNoteAndSourceList(bodyNode);
		bodyNode.appendChild(backlink(null, linkPrefix, html));
		makeFooter(bodyNode, html);
		return html;
	}
	
	protected Html createNoteDoc(Note note, File noteDir) {
		List<String> handledProperties = new ArrayList<String>();
		String linkPrefix = relativeLinkPrefix(note.getId());
		resetNoteAndSourceList();

		Html html = new Html(Gedcom.getName("NOTE") + " " + note.getId() + ": " + note.toString(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();

		bodyNode.appendChild(html.h1(Gedcom.getName("NOTE") + note.getId() + ": " + note.toString()));
		
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);

		// Print the note itself.
		appendDisplayValue(div1, note, false, html);

		// References
		Element div2 = html.div("right");
		processReferences(note, linkPrefix, div2, html, handledProperties);
		if (div2.hasChildNodes()) bodyNode.appendChild(div2);
		
		processNumberNoteSourceChangeRest(note, linkPrefix, div1, noteDir, html, handledProperties);
		addNoteAndSourceList(bodyNode);
		bodyNode.appendChild(backlink(null, linkPrefix, html));
		makeFooter(bodyNode, html);
		return html;
	}

	protected Html createSubmitterDoc(Submitter submitter, File submitterDir) {
		List<String> handledProperties = new ArrayList<String>();
		String linkPrefix = relativeLinkPrefix(submitter.getId());
		resetNoteAndSourceList();

		Html html = new Html(Gedcom.getName("SUBM") + " " + submitter.getId() + ": " + submitter.getName(), linkPrefix);
		Document doc = html.getDoc();
		Element bodyNode = html.getBody();

		bodyNode.appendChild(html.h1(submitter.getName()));
		handledProperties.add("NAME");
		
		Element div1 = html.div("left");
		bodyNode.appendChild(div1);

		// ADDRESS_STRUCTURE
		processAddresses(div1, submitter, html, handledProperties, true);

		// LANG
		processSimpleTag(submitter, "LANG", div1, html, handledProperties);
		
		// OBJE - Images etc
		Element images = processMultimediaLink(submitter, linkPrefix, submitterDir, html, true, false);
		if (images != null) div1.appendChild(images);
		handledProperties.add("OBJE");
		
		// References
		Element div2 = html.div("right");
		processReferences(submitter, linkPrefix, div2, html, handledProperties);
		if (div2.hasChildNodes()) bodyNode.appendChild(div2);

		processNumberNoteSourceChangeRest(submitter, linkPrefix, div1, submitterDir, html, handledProperties);
		addNoteAndSourceList(bodyNode);
		bodyNode.appendChild(backlink(null, linkPrefix, html));
		makeFooter(bodyNode, html);
		return html;
	}

	protected Element backlink(String indexFileName, String linkPrefix, Html html) {
		Element divlink = html.div("backlink");
		divlink.appendChild(html.link(linkPrefix + reportIndexFileName, translate("startPage")));
		if (indexFileName != null) {
			divlink.appendChild(html.text(" "));
			divlink.appendChild(html.link(linkPrefix + indexFileName, translate("indexPage")));
		}
		return divlink;
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
	protected String getName(Indi indi, Property nameProp) {
		String name = nameProp.getDisplayValue();
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
			Element appendTo, Html html, List<String> handledProperties) {
		// List who is referencing this source, not part of the source file but exists when running the code  
		List<PropertyXRef> refs = ent.getProperties(PropertyXRef.class);
		if (refs.size() > 0) {
			appendTo.appendChild(html.h2(translate("references")));
			Element p = html.p();
			appendTo.appendChild(p);
			for (PropertyXRef ref : refs) {
				getReferenceLink(ref, p, linkPrefix, html, true);
			}
		}
		handledProperties.add("XREF");
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
	protected Element processMultimediaLink(Property prop, String linkPrefix, File dstDir,
			Html html, boolean smallThumbs, boolean makeGalleryImage) {
		Property[] objects = prop.getProperties("OBJE");
		if (objects.length == 0) return null;
		Element p = html.p();
		int imgSize = 200;
		if (smallThumbs) imgSize = 100;
		for (int i = 0; i < objects.length; i++){
			if (objects[i] instanceof PropertyMedia) {
				Media media = (Media)((PropertyMedia)objects[i]).getTargetEntity();
				if (media != null) {
					if (media.getFile() != null) {
						// XXX Ugly code: 
						// Assume just one image, even though gedcom 551 says it can be multiple
						// The GenJ code seems to assume just one.
						p.appendChild(html.link(linkPrefix + addressToDir(media.getId()) + media.getFile().getName(), 
								html.img(linkPrefix + addressToDir(media.getId()) + "thumb_" + media.getFile().getName(), media.getTitle())));
					} else {
						println(" Media references are not handled yet...");
					}
					reportUnhandledProperties(objects[i], null);
				} else {
					println(" Invalid media reference to non existing object:" + objects[i].getValue());
				}
			} else {
				// Get the title
				Property titleProp = objects[i].getProperty("TITL");
				String title = null;
				if (titleProp != null) title = titleProp.getValue();
				// Get form of object
				Property formProp = objects[i].getProperty("FORM"); // 5.5 style
				if (formProp != null) {
					if (! formProp.getValue().matches("^jpe?g|gif|JPE?G|gif|PNG|png$")) {
						println(" Currently unsupported FORM in OBJE:" + formProp.getValue());
					}
					reportUnhandledProperties(formProp, null);
				}
				// Find file
				// XXX May have several FILE properties in 5.5.1 and FORM is a sub prop to FILE 
				PropertyFile file = (PropertyFile)objects[i].getProperty("FILE");
				if (file != null) {
					// Get form of object 5.5.1 style
					formProp = file.getProperty("FORM");
					if (formProp != null) {
						if (! formProp.getValue().matches("^jpe?g|gif|JPE?G|gif|PNG|png$")) {
							println(" Currently unsupported FORM in OBJE:" + formProp.getValue());
						}
						reportUnhandledProperties(formProp, null);
					}
					reportUnhandledProperties(file, new String[] {"FORM"});
					// Copy the file to dstDir
					File srcFile = file.getFile();
					if (srcFile != null) {
						File dstFile = new File(dstDir, srcFile.getName());
						File thumbFile = new File(dstFile.getParentFile(), "thumb_" + dstFile.getName());
						try {
							if (!dstFile.exists() || !thumbFile.exists() || 
									srcFile.lastModified() > dstFile.lastModified()) {
								copyFile(srcFile, dstFile);
								// Create a thumb
								makeThumb(dstFile, imgSize, imgSize, thumbFile);
							}
							// For the gallery
							if (makeGalleryImage) {
								File galleryImage = new File(dstFile.getParentFile(), "gallery.jpg");
								if (!galleryImage.exists() || srcFile.lastModified() > galleryImage.lastModified())
									makeThumb(dstFile, 50, 70, galleryImage);
								makeGalleryImage = false;
							}

							// Make img-reference to the image
							p.appendChild(html.link(dstFile.getName(), html.img(thumbFile.getName(), title)));
						} catch (IOException e) {
							println(" Error in copying file or making thumb: " + 
									srcFile.getName() + e.getMessage());
						}
						reportUnhandledProperties(objects[i], new String[]{"FILE", "TITL", "FORM"});
					} else {
						println(" FILE ref but no file was found");
					}
				} else {
					println(" OBJE without FILE is currently not handled");
				}
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
			Element appendTo, File destDir, Html html, List<String> handledProperties) {

		// SOUR
		if (! prop.getTag().equals("SOUR")) {
			Element sourceP = html.p(); 
			processSourceRefs(sourceP, prop, linkPrefix, destDir, html);
			if (sourceP.hasChildNodes()) {
				appendTo.appendChild(html.h2(Gedcom.getName("SOUR", true)));
				appendTo.appendChild(sourceP);
			}
		}
		handledProperties.add("SOUR");

		if (! prop.getTag().equals("NOTE")) {
			Element noteP = html.p(); 
			processNoteRefs(noteP, prop, linkPrefix, destDir, html);
			if (noteP.hasChildNodes()) {
				appendTo.appendChild(html.h2(Gedcom.getName("NOTE", true)));
				appendTo.appendChild(noteP);
			}
		}
		handledProperties.add("NOTE");
		
		/*  +1 RFN <PERMANENT_RECORD_FILE_NUMBER>  {0:1}
		 *  +1 AFN <ANCESTRAL_FILE_NUMBER>  {0:1}
		 *  +1 RIN <AUTOMATED_RECORD_ID>  {0:1}		 */
		processSimpleTags(prop, new String[] {"RFN", "AFN", "RIN"}, appendTo, html, handledProperties);
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
			handledProperties.add("REFN");
		}

		// CHAN
		appendTo.appendChild(html.h2(translate("other")));
		PropertyChange lastUpdate = (PropertyChange)prop.getProperty("CHAN");
		if (lastUpdate != null) {
			Element p = html.p(translate("dataUpdated") + 
					" " + lastUpdate.getDisplayValue());
			appendTo.appendChild(p);
			handledProperties.add("CHAN");
			processNoteRefs(p, lastUpdate, linkPrefix, destDir, html);
			reportUnhandledProperties(lastUpdate, new String[] {"NOTE"});
		}
		appendTo.appendChild(html.p(translate("pageCreated") + 
				" " + (new PropertyChange()).getDisplayValue()));
		
		// Add all other attributes
		reportUnhandledProperties(prop, (String[])handledProperties.toArray(new String[0])); 
		Element otherProperties = getAllProperties(prop, html, handledProperties);
		if (otherProperties != null)
		appendTo.appendChild(otherProperties);

	}

	/**
	 * Handle simple text tags without any sub tags
	 * @param prop
	 * @param tags
	 * @param appendTo
	 * @param html
	 * @param handledProperties
	 */
	protected void processSimpleTags(Property prop, String[] tags, Element appendTo, Html html, List<String> handledProperties) {
		for (String tag : tags) {
			processSimpleTag(prop, tag, appendTo, html, handledProperties);
		}
	}

	/**
	 * Handle simple text tags without any sub tags
	 * @param prop
	 * @param tag
	 * @param appendTo
	 * @param html
	 * @param handledProperties
	 */
	protected void processSimpleTag(Property prop, String tag, Element appendTo, Html html, List<String> handledProperties) {
		Property[] subProps = prop.getProperties(tag);
		if (subProps.length > 0) {
			appendTo.appendChild(html.h2(Gedcom.getName(tag)));
			for (Property subProp : subProps) {
				Element p = html.p();
				this.appendDisplayValue(p, subProp, true, html);
				appendTo.appendChild(p);
				
				reportUnhandledProperties(subProp, null);
			}
		}
		handledProperties.add(tag);
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
		Element place = processPlace(event.getProperty("PLAC"), linkPrefix, dstDir, html);
		if (place != null) p.appendChild(place);
		// ADDRESS_STRUCTURE
		List<String> handledProperties = new ArrayList<String>(); // XXX Use this instead? (addr can handle mote props)
		processAddresses(p, event, html, handledProperties, false);

		// XXX RESN (In gedcom 5.5.1)
		
		// SOUR - Sources
		processSourceRefs(p, event, linkPrefix, dstDir, html);
		// NOTE
		processNoteRefs(p, event, linkPrefix, dstDir, html);
		// AGE, AGNC, CAUS
		for (String tag : new String[] {"AGE", "AGNC", "CAUS", "RELI"}) {
			Property tagProp = event.getProperty(tag);
			if (tagProp != null) {
				p.appendChild(html.text(Gedcom.getName(tag) + " " + tagProp.getDisplayValue()));
				this.reportUnhandledProperties(tagProp, null);
			}
		}
		// HUSB:AGE, WIFE:AGE, may exist on Family events
		for (String tag : new String[] {"HUSB", "WIFE"}) {
			Property tagProp = event.getProperty(tag);
			if (tagProp != null) {
				Property age = tagProp.getProperty("AGE");
				if (age != null) {
					p.appendChild(html.text(Gedcom.getName(tag) + " " + Gedcom.getName("AGE") + " " +
							age.getDisplayValue()));
					this.reportUnhandledProperties(age, null);
				}
				reportUnhandledProperties(tagProp, new String[]{"AGE"});
			}
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
		Element pObj = processMultimediaLink(event, linkPrefix, dstDir, html, true, false);
		if (pObj != null && pObj.hasChildNodes()) {
			NodeList nl = pObj.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) p.appendChild(nl.item(i));
		}
		
		reportUnhandledProperties(event, new String[]{"DATE", "PLAC", "TYPE", "NOTE", "SOUR", "ADDR", "PHON", "EMAIL", "FAX", "WWW", "AGE", "AGNC", "CAUS", "FAMC"});
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
	
	protected Element processPlace(Property place, String linkPrefix, File dstDir, Html html) {
		if (place == null) return null;
		Element span = html.span("place", placeDisplayFormat.equals("all") ? place.getValue() : place.format(placeDisplayFormat).replaceAll("^(,|(, ))*", "").trim());
		// SOUR - Sources
		processSourceRefs(span, place, linkPrefix, dstDir, html);
		// NOTE
		processNoteRefs(span, place, linkPrefix, dstDir, html);
		// Variants of the name of the place
		for (String subTag : new String[] {"FONE", "ROMN"}) {
			Property fone = place.getProperty(subTag);
			if (fone != null) {
				String type = "";
				Property typeProp = fone.getProperty("TYPE"); // Should be here according to spec
				if (typeProp != null) type = typeProp.getDisplayValue(); 
				span.appendChild(html.text(Gedcom.getName(subTag) + " " + type + ": " + 
						(placeDisplayFormat.equals("all") ? fone.getValue() : fone.format(placeDisplayFormat).replaceAll("^(,|(, ))*", "").trim())));
				reportUnhandledProperties(fone, new String[] {"TYPE"});
			}
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

	final String[] addressOtherProperties = new String[] {"PHON", "EMAIL", "FAX", "WWW"}; 

	protected boolean processAddressesHasData(Property prop) {
		if (prop.getProperty("ADDR") != null) return true;
		for (String tag : addressOtherProperties) {
			if (prop.getProperty(tag) != null) return true;
		}
		return false;
	}
	
	protected void processAddresses(Element appendTo, Property prop, Html html, List<String> handledProperties, boolean bigDisplayWithHeading) {
		if (! processAddressesHasData(prop)) return;
		if (bigDisplayWithHeading) appendTo.appendChild(html.h2(Gedcom.getName("ADDR")));

		Element span = html.span("address");
		if (! bigDisplayWithHeading) appendTo.appendChild(span);
		
		Property address = prop.getProperty("ADDR");
		if (address != null) {
			// Direct text
			appendDisplayValue(span, address, ! bigDisplayWithHeading, html);
			// The specified sub properties, city etc
			final String[] addressSubProperties = new String[]{"ADR1", "ADR2", "ADR3", "CITY", "STAE", "POST", "CTRY"};
			for (String subTag : addressSubProperties) {
				Property subProp = address.getProperty(subTag);
				if (subProp != null) {
					if (bigDisplayWithHeading) span.appendChild(html.br());
					else span.appendChild(html.text(", ")); 
					span.appendChild(html.text(subProp.getDisplayValue()));
					reportUnhandledProperties(subProp, null);
				}
			}
			reportUnhandledProperties(address, addressSubProperties);
		}
		if (bigDisplayWithHeading && span.hasChildNodes()) appendTo.appendChild(html.p(span));
		handledProperties.add("ADDR");

		for (String tag : addressOtherProperties) {
			for (Property subProp : prop.getProperties(tag)) {
				Node value = html.text(subProp.getDisplayValue());
				if (tag.equals("EMAIL")) value = html.link("mailto:" + subProp.getDisplayValue(), subProp.getDisplayValue());
				if (tag.equals("WWW")) value = html.link(subProp.getDisplayValue(), subProp.getDisplayValue());
				if (bigDisplayWithHeading) {
					Element p = html.p(Gedcom.getName(tag) + ": ");
					p.appendChild(value);
					appendTo.appendChild(p);
				} else {
					span.appendChild(html.text(", "));
					span.appendChild(value);
				}
			}
			handledProperties.add(tag);
		}
	}

	/**
	 * Adds references in the source list and adds a superscript-link on appendTo 
	 */
	protected void processSourceRefs(Element appendTo, Property prop, String linkPrefix, File dstDir, Html html) {
		Property[] sourceRefs = prop.getProperties("SOUR");
		if (sourceRefs.length > 0) {
	    	if (sourceDiv == null) {
	    		sourceDiv = html.div("left");
	    		sourceCounter = 1;
	    	}
			Element sup = html.sup("source");
			for (Property sourceRef : sourceRefs) {
				if (sup.hasChildNodes()) sup.appendChild(html.text(", "));
				sup.appendChild(addSourceRef(sourceRef, linkPrefix, dstDir, html));
			}
			appendTo.appendChild(sup);
		}
	}

	/**
	 * Adds the source to the list and return an internal link (#S[n]) to it
	 * @param sourceRef
	 * @return
	 */
	protected Element addSourceRef(Property sourceRef, String linkPrefix, File dstDir, Html html) {
		int number = sourceCounter++;
		Element p = html.p();
		sourceDiv.appendChild(p);
		Element anchor = html.anchor("S" + number);
		p.appendChild(anchor);
		anchor.appendChild(html.text("S" + number + ": "));
	
		if (sourceRef instanceof PropertySource) {
			// Reference
			// Link to source
			Source source = (Source)((PropertySource)sourceRef).getTargetEntity();
			if (source != null)
				p.appendChild(html.link(linkPrefix + addressTo(source.getId()), source.toString()));
			else 
				p.appendChild(html.text("(" + translate("unknown") + ")"));
			// PAGE
			Property page = sourceRef.getProperty("PAGE");
			if (page != null) {
				p.appendChild(html.text(" " + Gedcom.getName("PAGE") + ": " + 
						page.getDisplayValue()));
	       		reportUnhandledProperties(page, null);
			}
			// EVEN
			Property even = sourceRef.getProperty("EVEN");
			if (even != null) {
				p.appendChild(html.text(" " + Gedcom.getName("EVEN") + ": " +
						even.getDisplayValue()));
				Property role = even.getProperty("ROLE");
				if (role != null) {
					p.appendChild(html.text(" " + Gedcom.getName("ROLE") + ": " +
						role.getDisplayValue()));
	           		reportUnhandledProperties(role, null);
				}
	       		reportUnhandledProperties(even, new String[] {"ROLE"});
			}
			// DATA
			Property data = sourceRef.getProperty("DATA");
			if (data != null) {
				p.appendChild(html.text(" " + Gedcom.getName("DATA") + ": " +
						data.getDisplayValue()));
				Property date = data.getProperty("DATE");
				if (date != null) p.appendChild(html.text(" " + date.getDisplayValue()));
				Property text = data.getProperty("TEXT");
	   			if (text != null) p.appendChild(html.text(" " + text.getDisplayValue()));
	   			reportUnhandledProperties(data, new String[] {"DATE", "TEXT"});
			}
			// QUAY
			Property quay = sourceRef.getProperty("QUAY");
			if (quay != null) {
				p.appendChild(html.text(" " + Gedcom.getName("QUAY") + ": " + 
						quay.getDisplayValue())); // XXX Check out what this turns into (0..3) should be translated into something readable
	       		reportUnhandledProperties(quay, null);
			}
			// OBJE, in new paragraph
			Element pObj = processMultimediaLink(sourceRef, linkPrefix, dstDir, html, true, false);
			if (pObj != null) sourceDiv.appendChild(pObj);
					
	   		reportUnhandledProperties(sourceRef, new String[] {"PAGE", "EVEN", "DATA", "QUAY", "OBJE", "NOTE"});
		} else {
			// Direct source text
			appendDisplayValue(p, sourceRef, false, html);
			for (Property text : sourceRef.getProperties("TEXT")) {
				Element sp = html.p();
				sourceDiv.appendChild(sp);
				sp.appendChild(html.text(Gedcom.getName("TEXT") + ": "));
				appendDisplayValue(sp, text, false, html);
			}
	   		reportUnhandledProperties(sourceRef, new String[] {"TEXT", "NOTE"});
		}
		// Notes
		processNoteRefs(p, sourceRef, linkPrefix, dstDir, html);  
		return html.link("#S" + number, "S" + number);
	}

	/**
	 * Adds references in the notes list and adds a superscript-link on appendTo 
	 */
	protected void processNoteRefs(Element appendTo, Property prop, String linkPrefix, File dstDir, Html html) {
		Property[] noteRefs = prop.getProperties("NOTE");
		if (noteRefs.length > 0) {
	    	if (noteDiv == null) {
	    		noteDiv = html.div("left");
	    		noteCounter = 1;
	    	}
			Element sup = html.sup("note");
			for (Property noteRef : noteRefs) {
				if (sup.hasChildNodes()) sup.appendChild(html.text(", "));
				sup.appendChild(addNoteRef(noteRef, linkPrefix, dstDir, html));
			}
			appendTo.appendChild(sup);
		}
	}

	/**
	 * Handles a note, adds it to the note-list
	 * @param noteRef
	 * @param linkPrefix
	 * @param dstDir
	 * @param html
	 * @return
	 */
	protected Element addNoteRef(Property noteRef, String linkPrefix, File dstDir,
			Html html) {
		int number = noteCounter++;
		Element p = html.p();
		noteDiv.appendChild(p);
		Element anchor = html.anchor("N" + number);
		p.appendChild(anchor);
		anchor.appendChild(html.text("N" + number + ": "));
		
		if (noteRef instanceof PropertyNote) {
			// Reference
			Note note = (Note)((PropertyNote)noteRef).getTargetEntity();
			p.appendChild(html.link(linkPrefix + addressTo(note.getId()), note.toString()));
		} else {
			// Direct text
			appendDisplayValue(p, noteRef, false, html);
		}
		// Sources
		processSourceRefs(p, noteRef, linkPrefix, dstDir, html);
	
		reportUnhandledProperties(noteRef, new String[]{"SOUR"});
		return html.link("#N" + number, "N" + number);
	}

	/**
	 * Handle the value of multiline properties
	 * @param appendTo
	 * @param prop
	 * @param ignoreNewLine
	 * @param html
	 */
	protected void appendDisplayValue(Element appendTo, Property prop, boolean ignoreNewLine, Html html) {
		if (prop instanceof MultiLineProperty) {
			Iterator lineIter = ((MultiLineProperty)prop).getLineIterator();
			boolean firstLine = true;
			do {
				if (! firstLine && ! ignoreNewLine) appendTo.appendChild(html.br());
				appendTo.appendChild(html.text(lineIter.getValue()));
				firstLine = false;
			} while (lineIter.next());
		} else {
			appendTo.appendChild(html.text(prop.getDisplayValue()));
		}
	}

	protected void resetNoteAndSourceList() {
		sourceDiv = null;
		noteDiv = null;
	}

	protected void addNoteAndSourceList(Element appendTo) {
		if (noteDiv != null) appendTo.appendChild(noteDiv);
		if (sourceDiv != null) appendTo.appendChild(sourceDiv);
	}

	/**
	 * Handles a note
	 * @return A <span>-element
	 */
/*	protected Element processNote(Property note, String linkPrefix, Html html) {
		if (note == null) return null;
		Element noteEl = html.spanNewlines("note", note.getDisplayValue());
		Element sourcesEl = processSources(note, linkPrefix, html);
		if (sourcesEl != null) noteEl.appendChild(sourcesEl);
		reportUnhandledProperties(note, new String[]{"SOUR"});
		return noteEl;
	}*/

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
		translator.put("indexFile", reportIndexFileName); // Lägg till värden för js-fil
		translator.put("noSearchResults", translate("noSearchResults")); // Lägg till värden för js-fil
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
	protected void makeCssAndJs(File dir, HashMap<String, String> translator) throws IOException {
		println("Making css-file");
		copyTextFileModify(getFile().getParentFile().getAbsolutePath() + File.separator + cssBaseFile,
				dir.getAbsolutePath() + File.separator + "style.css", translator, false);	
		copyTextFileModify(getFile().getParentFile().getAbsolutePath() + File.separator + cssTreeFile[treeType],
				dir.getAbsolutePath() + File.separator + "style.css", translator, true);	
		copyTextFileModify(getFile().getParentFile().getAbsolutePath() + File.separator + "html/search.js",
				dir.getAbsolutePath() + File.separator + "search.js", translator, false);	
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
		String type = id.substring(0, 1);
		String prefix = type.toLowerCase();
		if (type.equals("I")) {	prefix = "indi"; }
		if (type.equals("S")) {	prefix = "source"; }
		if (type.equals("R")) {	prefix = "repository"; }
		if (type.equals("O")) {	prefix = "object"; }
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
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(src).getChannel();
			destination = new FileOutputStream(dst).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source!=null) try { source.close(); } catch (Throwable t){};
			if (destination!=null) try { destination.close(); } catch (Throwable t){};
		}

	}

	protected void copyTextFileModify(String inFile, String outFile, HashMap<String,String> translator, boolean append) throws IOException {
		final Pattern replacePattern = Pattern.compile(".*\\{(\\w+)\\}.*");
		BufferedReader in = null;
		BufferedWriter out = null;
		try {
			in = new BufferedReader(new FileReader(inFile));
//			out = new BufferedWriter(new FileWriter(outFile, append));
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile, append), "UTF-8")); 
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

	