/**
 * Console.java
 * A client of the SF genj GEDCOM model which providedes a text UI to 
 * browsing and editing gedcom.
 * $Header: /cygdrive/c/temp/cvs/genj/sandbox/console/src/core/com/sadinoff/genj/console/Console.java,v 1.17 2006-05-16 23:43:23 sadinoff Exp $
 
 ** This program is licenced under the GNU license, v 2.0
 *  AUTHOR: Danny Sadinoff
 */

package com.sadinoff.genj.console;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.util.Origin;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Console {
    private static final boolean SOUND = Boolean.getBoolean("console.sound");//experimental feature
    protected static final String LB = System.getProperty("line.separator");

    protected static final Map<Integer, String> sexMap  = new HashMap<Integer,String>();
    static
    {
        sexMap.put(PropertySex.MALE,"M");
        sexMap.put(PropertySex.FEMALE,"F");
        sexMap.put(PropertySex.UNKNOWN,"U");
    }

    protected final Gedcom gedcom;
    protected BufferedReader in;
    protected PrintWriter out;
    private boolean dirty = false;
    
    protected boolean isDirty()
    {
        return dirty;
    }

    protected void setDirty(boolean newValue)
    {
        dirty = newValue;
    }
    /**
     * Constructor for a Console session.
     * @gedcomArg the Gedcom to be edited.
     * @param userInput the BufferedReader from whence to fetch the typed input
     * @param output Where to send user output.  As of right now, warnings go here too.
     */
    public Console(Gedcom gedcomArg, final BufferedReader userInput, final PrintWriter output) {
        in = userInput;
        out = output;
        gedcom = gedcomArg;
        
    }
    /**
     * Constructor for a Console session attached to System.in and System.out
     * @param gedcomArg the Gedcom to be edited.
     */
    public Console(Gedcom gedcomArg) {
        this(gedcomArg, new BufferedReader(new InputStreamReader(System.in)), new PrintWriter(System.out));
    }

    
    /**
     * experimental sound support...
     *      
     */
    enum UIFeedbackType{ SYNTAX_ERROR, MOTION_SIDEWAYS, MOTION_UP, MOTION_DOWN, MOTION_SPOUSE,
                       MOTION_HYPERSPACE,
                       HIT_WALL,
                       SET_VALUE,
                       NOT_FOUND,
                       STALL,
                       MISSILE_LOCK,
                       INTERSECT_GRANITE_CLOUD,
    };

    /**
     * Provide the user feedback that an even occurred
     * @param event
     */
    public void giveFeedback(UIFeedbackType event) {
        if (! SOUND)
            return;
      switch(  event)
      {
      case SYNTAX_ERROR:
          AudioUtil.play("/Users/dsadinoff/sound/huh?.wav");
          break;
      case MOTION_HYPERSPACE:
          AudioUtil.play("/Users/dsadinoff/sound/hyperspace.wav");
          break;
      case HIT_WALL:
          AudioUtil.play("/Users/dsadinoff/sound/thump1.wav");
          break;
      case SET_VALUE:
          AudioUtil.play("/Users/dsadinoff/sound/pop.wav");
          break;
      default:
      }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)  
        throws Exception
    {
        if( args.length< 1)
        {
            System.err.println("usage: java [classpath_options] "+ Console.class.getName() +" filename ");
            System.err.println("       java [classpath_options] "+ Console.class.getName() +" -u URL");
            System.exit(1);
        }

        Origin origin;
        if( args.length ==2 )
        {
            if(! args[0].equals("-u"))
            {
                System.err.println("Unknown option "+args[0]);
                System.err.println("usage: java [classpath_options] "+ Console.class.getName() +" filename ");
                System.err.println("       java [classpath_options] "+ Console.class.getName() +" -u URL");
                System.exit(1);
            }
            URL url = new URL(args[0]);
            origin = Origin.create(url);
        }
        else
        {
            origin = Origin.create(new File(args[0]).toURL());
        }
        // read the gedcom file

        GedcomReader reader = new GedcomReader(origin);
        Gedcom gedcom = reader.read();
        Console tt = new Console(gedcom);
        tt.go();
    }

    interface Action
    {
        enum ArgType{ARG_NO, ARG_YES,ARG_OPTIONAL};
        Indi doIt(Indi theIndi, String arg) throws Exception;
        String getDoc();
        ArgType getArgUse();
        String getArgName();
        boolean modifiesDatamodel();
    }

    abstract class ActionHelper implements Action
    {
        public ArgType getArgUse(){ return ArgType.ARG_NO; }
        public String getArgName(){ return null; }
        public boolean modifiesDatamodel() { return false; } 
    }

    /*
     * @param arg string to be parsed
     * @default the value to be returned if it's null
     * @throws NumberParseException on numberparse error.
     */
    final protected int parseInt(String arg, int nullDefault) {
        if (null == arg)
                return nullDefault;

        return Integer.parseInt(arg);
    }
    
    /**
     * fetch the  
     * @return A Sorted Map of 
     */
    protected Map<List<String>,Action> getActionMap()
    {
        final Map<List<String>,Action>  actionMap = new LinkedHashMap<List<String>,Action>();
        

        actionMap.put(Arrays.asList(new String[]{"version"}), new ActionHelper(){public Indi doIt(Indi ti, String arg){
            out.println(getVersion());
            return ti;}
        public String getDoc() {return "print this help message";}
            });        
        
        
        actionMap.put(Arrays.asList(new String[]{"help"}), new ActionHelper(){public Indi doIt(Indi ti, String arg){
            out.println(getHelpText(actionMap));
            return ti;}
        public String getDoc() {return "print this help message";}
            });
        actionMap.put(Arrays.asList(new String[]{"exit","quit"}), new ActionHelper(){
            public Indi doIt(Indi ti, String arg) throws IOException  {
                if( !isDirty())
                    System.exit(0);
                out.println();
                out.println("There are unsaved changes!");
                out.print("Are you sure that you want to exit? [y/N]: ");
                out.flush();
                String line = in.readLine();
                if( line.toLowerCase().startsWith("y"))
                    System.exit(0);
                out.println("Try the 'save FILENAME' command.");
                return ti;
            }
                public String getDoc(){return "Exit the program.";}
            });

        
        
        actionMap.put(Arrays.asList(new String[]{"save"}), new Action(){
            
            public Indi doIt(Indi ti, String arg){
                try{
                    File saveTo = new File( arg );
                    File tempFile = File.createTempFile(arg,"",saveTo.getCanonicalFile().getParentFile());
                    
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile));
                    
                    GedcomWriter writer = new GedcomWriter(gedcom,arg,null,fos);                    
                    writer.write(); //closes fos
                    if(!  tempFile.renameTo(saveTo) )
                        throw new Exception("unable to rename "+tempFile+" to "+saveTo);
                    out.println("Wrote file "+arg+" successfully.");
                    out.println("Remember: the pathname of this GEDCOM file is embedded within it.");
                    setDirty(false);
                }
                catch( Exception e)
                {
                    out.println("failure writing to arg: "+e);
                }
                return ti;
            }
            public boolean modifiesDatamodel() { return true; } 

        public String getDoc() { return "Save gedcom with filename FNAME";}
        public ArgType getArgUse() { return ArgType.ARG_YES; }
        public String getArgName() { return "FNAME";}
        });
        

        actionMap.put(Arrays.asList(new String[]{"look","l", "x"}), new ActionHelper()
                {
                    public Indi doIt(final Indi ti ,final String targetID){
                        if( targetID != null && targetID.length()>0)
                        {
                            Indi target = (Indi)gedcom.getEntity("INDI", targetID);
                            if( null == target)
                                out.println("No INDI record with that ID");
                            else
                                out.println(dump(target));
                        }
                        else
                            out.println(dump(ti));
                        return ti;
                    }
                    public String getDoc(){return "Dump Detailed information on the current person [person with ID]";}
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL; }
                    public String getArgName() { return "ID";}
                    });        
        
        actionMap.put(Arrays.asList(new String[]{"gind","goto", "g"}), new Action()
                {
                    public Indi doIt(final Indi ti ,final String targetID){
                        Entity  newEntity = gedcom.getEntity("INDI", targetID);
                        if (null == newEntity)
                        {
                            System.out.println("Can't find entity named "+targetID);
                            return ti;
                        }
                        Indi newInd = (Indi)newEntity;
                        giveFeedback(UIFeedbackType.MOTION_HYPERSPACE);                                                    
                        return newInd;
                    }
                    public boolean modifiesDatamodel() { return false; } 
                    public String getDoc(){return "Go to Individual with identifier ID";}
                    public ArgType getArgUse() {  return ArgType.ARG_YES;}
                    public String getArgName() {  return "ID"; }
                });

        actionMap.put(Arrays.asList(new String[]{"search","find"}), new Action()
                {
                    public Indi doIt(final Indi ti ,final String searchArg){
                        out.println(" Search Results:[[");
                        for( Object entity : gedcom.getEntities("INDI"))
                        {
                            Indi candidate = (Indi)entity;
                            if( candidate.getName().toLowerCase().contains(searchArg.toLowerCase()))
                                out.println("  "+candidate);
                        }
                        out.println(" ]]");
                        out.println();
                        return ti;
                    }
                    public boolean modifiesDatamodel() { return false; } 
                    public String getDoc(){return "Show list of individuals with names containing STR as a substring";}
                    public ArgType getArgUse() {  return ArgType.ARG_YES;}
                    public String getArgName() {  return "STR"; }
                });
        
        
        actionMap.put(Arrays.asList(new String[]{"gdad","gd"}), new ActionHelper()
                {
                    public Indi doIt(final Indi ti , String arg){
                        Indi dad = ti.getBiologicalFather();
                        if( null == dad)
                        {   
                            out.println("sorry, no dad.  Try cdad.\n");
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return ti;
                        }
                        else
                            return dad;
                    }
                    public String getDoc(){return "Go to Biological Father";}
                });        
        
        actionMap.put(Arrays.asList(new String[]{"gmom","gm"}), new ActionHelper()
                {
                    public Indi doIt(final Indi ti, String arg){
                        Indi mom = ti.getBiologicalMother();
                        if( null == mom)
                        {   
                            out.println("sorry, no mom.  Try cmom.\n");
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return ti;
                        }
                        else
                            return mom;
                    }
                    public String getDoc(){return "Go to Biological Mother";}
                });
        
        actionMap.put(Arrays.asList(new String[]{"gspo","gsp"}),new Action()
                {

                    public Indi doIt(Indi theIndi, String arg) {
                        Fam[] marriages = theIndi.getFamiliesWhereSpouse();
                        if( marriages.length ==0)
                        {
                            out.println("Not married.");
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return theIndi;
                        }
                        
                        int targetMarriage;
                        try
                        {
                            int marriageArg = parseInt(arg,1);
                            targetMarriage = marriageArg -1;
                        }
                        catch(NumberFormatException nfe)
                        {
                            out.println("couldn't parse "+arg+" as a number");
                            return theIndi;
                        }
                        return  marriages[targetMarriage].getOtherSpouse(theIndi);
                    }

                    public boolean modifiesDatamodel() { return false; } 
                    
                    public String getDoc() {return "go to [Nth] spouse";}
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "N";}
                    });

        
        actionMap.put(Arrays.asList(new String[]{"gsib"}), new Action()
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        Fam bioKidFamily = theIndi.getFamilyWhereBiologicalChild();
                        if( null == bioKidFamily)
                        {
                            out.println("not a kid in a biofamily");
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return theIndi;
                        }
                        Indi[]sibs =  bioKidFamily.getChildren();
                        if( arg == null || arg.length() == 0)
                        {
                            //find the next kid in the family;
                            int myIndex =-1; 
                            for( int i =0; i<sibs.length; i++)
                                if( sibs[i]==theIndi)  //somewhat risky. 
                                    myIndex =i;
                            if( myIndex == -1)
                            {
                                out.println("Aiee: can't find myself.");
                                return theIndi;
                            }
                            return sibs[(myIndex+1)%sibs.length];
                        }
                        else
                        {//return specified sib
                            try 
                            {
                                int kidNumber = parseInt(arg, 0);
                                if( kidNumber <1 || kidNumber > sibs.length)
                                {
                                    out.println("bad sib number");
                                    return theIndi;
                                }
                                return sibs[kidNumber-1];
                            }
                            catch(NumberFormatException nfe)
                            {
                                out.println("couldn't parse "+arg+" as a number");
                                return theIndi;
                            }
                        }
                    }
                    public boolean modifiesDatamodel() { return false; } 
                    public String getDoc() {    return "go to next [Nth] sibling";}
                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "N";}

                });
   
        actionMap.put(Arrays.asList(new String[]{"gchi","gkid"}), new Action()
                {
                    
                public String getDoc() {    return "go to first [Nth] child ";}
                public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                public String getArgName() { return "N";}
                public boolean modifiesDatamodel() { return false; } 
            public Indi doIt(Indi theIndi, String arg) {
                
                // FIX: M'th marriage not implemented.
                Indi[] children=  theIndi.getChildren();
                if(0==children.length)
                {
                    out.println("no kids!");
                    giveFeedback(UIFeedbackType.HIT_WALL);                            
                    return theIndi;
                }
                if( null == arg || arg.length()==0)
                    return children[0];
                try 
                {
                    int kidNumber = Integer.parseInt(arg);
                    if( kidNumber <1 || kidNumber > children.length)
                    {
                        out.println("bad sib number");
                        return theIndi;
                    }
                    return children[kidNumber-1];
                }
                catch(NumberFormatException nfe)
                {
                    out.println("couldn't parse ["+arg+"] as a number");
                }
                return theIndi;
            }
                });
        

        actionMap.put(Arrays.asList(new String[]{"cbro","cb"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi newSib =  createBiologicalSibling(ti,PropertySex.MALE);
                        if(null != arg && arg.length() > 0)
                            setFirstName(newSib, arg);
                        return newSib;
                    }

                    public String getDoc(){return "Create a biological brother [with first name FNAME]";}
                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                    public String getArgName() {return "FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });

        actionMap.put(Arrays.asList(new String[]{"csis"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi newSib =  createBiologicalSibling(ti,PropertySex.FEMALE);
                        if(null != arg && arg.length() > 0)
                            setFirstName(newSib, arg);
                        return newSib;
                    }
                        
                    public String getDoc(){return "Create a biological sister [with first name FNAME]";}
                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                    public String getArgName() {return "FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });

        actionMap.put(Arrays.asList(new String[]{"cson"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        final int marriageNumber = parseInt(arg,0);
                        Indi kid = createChild(ti,marriageNumber-1,PropertySex.MALE);
                        if( 0 == marriageNumber &&null != arg && arg.length()>0)
                            setFirstName(kid,arg);
                        return kid;
                    }
                        
                    public String getDoc(){return "Create son in default/[nth] marriage, with first name FNAME";}

                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}

                    public String getArgName() {return "N/FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });
        
        actionMap.put(Arrays.asList(new String[]{"cdaut", "cdau","cd"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        int marriageNumber = parseInt(arg,0);
                        Indi kid = createChild(ti,marriageNumber-1,PropertySex.FEMALE);
                        if( 0 == marriageNumber &&null != arg && arg.length()>0)
                            setFirstName(kid,arg);
                        return kid;
                    }
                    
                    public String getDoc(){return "Create daughter in default/[nth] marriage, with first name FNAME";}
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "N/FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });

        
        actionMap.put(Arrays.asList(new String[]{"cspou", "csp","cspouse"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi spouse = createFamilyAndSpouse(ti);
                        if(null != arg && arg.length() > 0 )
                            setFirstName(spouse,arg);
                        return spouse;
                    }
                    public String getDoc(){return "Create and goto a spouse of the opposite sex [with First name FNAME]";}
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });

        
        actionMap.put(Arrays.asList(new String[]{"cdad"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi parent= createParent(ti,PropertySex.MALE);
                        if(null != arg && arg.length() > 0 )
                            setFirstName(parent,arg);
                        return parent;
                    }
                    public String getDoc(){return "Create and goto a father [with first name FNAME]";}
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });
        actionMap.put(Arrays.asList(new String[]{"cmom"}), new Action()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi parent= createParent(ti,PropertySex.FEMALE);
                        if(null != arg && arg.length() > 0 )
                            setFirstName(parent,arg);
                        return parent;
                    }
                    public String getDoc(){return "Create and goto a mother [with first name FNAME]";}
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "FNAME";}
                    public boolean modifiesDatamodel() { return true; } 
                });

                
        actionMap.put(Arrays.asList(new String[]{"rsib"}), new Action()
                {
                    public Indi doIt(final Indi ti, final String existingSibID) throws GedcomException{
                        Fam theFam = getCreateBiologicalFamily(ti);
                        Indi existingSib = (Indi)gedcom.getEntity("INDI", existingSibID);
                        if (null == existingSib)
                        {
                            System.out.println("Can't find entity named "+existingSibID);
                            return ti;
                        }
                        Fam existingFam = existingSib.getFamilyWhereBiologicalChild();
                        if( null != existingFam )
                        {
                            out.println("Error. Individual "+existingSib+" is already a bio-child in family "+existingFam);
                            return ti;
                        }
                        theFam.addChild(existingSib);
                        return existingSib;
                    }
                    public String getDoc(){return "relate the current Individual to an individual with identifier ID";}
                    public ArgType getArgUse() { return ArgType.ARG_YES;}
                    public String getArgName() { return "ID";}
                    public boolean modifiesDatamodel() { return true; } 
                });        
        /*
        actionMap.put(Arrays.asList(new String[]{"del","delete"}), new ActionHelper()
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        gedcom.deleteEntity(ti);
                        //FIX handle empty database.
                      out.println("Individual Removed.  Returning to Gedcom root...");
                        return (Indi)gedcom.getFirstEntity(Gedcom.INDI);
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc(){return "Delete the current Individual and return to the root of the Gedcom file";}
                });
        */
        

        actionMap.put(Arrays.asList(new String[]{"sname","snam","n"}), new Action()
                {
                    final Pattern firstLastPat = Pattern.compile("((\\S+\\s+)+)(\\S+)");
                    public Indi doIt(Indi theIndi, String arg) {
                        Matcher firstLastMatcher = firstLastPat.matcher(arg);
                        if( ! firstLastMatcher.find())
                        {
                            out.println("syntax error: snam first last");
                        }
                        String first = firstLastMatcher.group(1).trim();
                        String last = firstLastMatcher.group(3);
                        theIndi.setName(first, last);
                        return theIndi;
                    }
                    
                    public String getDoc() {return "set name to FIRST LAST";}
                    public ArgType getArgUse() { return ArgType.ARG_YES;}
                    public String getArgName() { return "FIRST LAST"; }
                    public boolean modifiesDatamodel() { return true; 
                    } 
                });

        actionMap.put(Arrays.asList(new String[]{"sfnm","fn","sfn"}), new Action()
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        theIndi.setName(arg,theIndi.getLastName());
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return "set First name to FIRSTNAME";}
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "FIRSTNAME";}
                });
        actionMap.put(Arrays.asList(new String[]{"slnm","ln","sln"}), new Action()
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        theIndi.setName(theIndi.getFirstName(), arg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return "set Last name to LAST";}
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "LAST";}
                });

        actionMap.put(Arrays.asList(new String[]{"ssex","sex"}), new Action()
                {
                    public Indi doIt(Indi theIndi, String newSex) {
                        newSex = newSex.toLowerCase();
                        if(newSex.equals("m"))
                            theIndi.setSex(PropertySex.MALE);
                        else if( newSex.equals("f"))
                            theIndi.setSex(PropertySex.FEMALE);
                        else if(newSex.equals("u"))
                            theIndi.setSex(PropertySex.UNKNOWN);
                        else
                            out.println("ERROR: argument to ssex must be one of M,F,U");
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return "set sex of current individual to S. Must be one of {M,F,U}.";}
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "S";}
                });

        actionMap.put(Arrays.asList(new String[]{"bday","b"}), new Action()
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        PropertyDate date =theIndi.getBirthDate() ;
                        if(null == date) {
                            theIndi.setValue(new TagPath("INDI:BIRT:DATE"),"");
                             date =theIndi.getBirthDate() ;
                        }
                        setDate(date, arg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return "set birthday to BDAY";}
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "BDAY";}
                });


        actionMap.put(Arrays.asList(new String[]{"dday","d"}), new Action()
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        PropertyDate date =theIndi.getDeathDate() ;
                        if(null == date) {
                            theIndi.setValue(new TagPath("INDI:DEAT:DATE"),"");
                             date =theIndi.getDeathDate() ;
                        }
                        setDate(date, arg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return "set death day to DDAY";}
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "DDAY";}
                });

        
        return actionMap;
    }
    
    
    public void go()  throws Exception{
        Indi theIndi = (Indi)gedcom.getFirstEntity(Gedcom.INDI);

            final Map<List<String>,Action> actionMap = getActionMap();

            Map<String, Action> commandToAction= expandActionMap(actionMap);

        Pattern commandPat = Pattern.compile("^(\\w+)(\\s+(\\w.*))?");
        for(;;)
        {
            out.println("------");
            out.print("You are at: ");
            out.println(brief(theIndi));
            out.print("> ");
            out.flush();
            final String line = in.readLine().trim();
            if( line.length() ==0)
                continue;
            Matcher lineMatcher = commandPat.matcher(line);
            if( ! lineMatcher.matches())
            {
                out.println("syntax error.  Type 'help' for help");
                continue;
            }
            String command = lineMatcher.group(1);
            String args = lineMatcher.group(3);
            if( ! commandToAction.containsKey(command) ) {
                out.println("unknown command. Type 'help' for help");
                giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                continue;
            }
            Action action = commandToAction.get(command);
            try
            {
                if(action.modifiesDatamodel())
                    setDirty(true);
                theIndi = action.doIt(theIndi, args);
            }
            catch( Exception re)
            {
                out.println("ERROR: "+re);
                re.printStackTrace();
            }
        }
    }

    private Map<String, Action> expandActionMap(Map<List<String>, Action> actionMap) {
        Map<String,Action>  theMap = new HashMap<String, Action>();
        for(Map.Entry<List<String>,Action> entry  : actionMap.entrySet())
            for( String command : entry.getKey())
            {
                if( theMap.containsKey(command))
                {
                    throw new RuntimeException("Configuration ERROR!  overlapping command definitions for "+command);
                }
                theMap.put(command, entry.getValue());
            }
        return theMap;
    }

    /**
     * Translate a family to a string for output on the console.
     * @param fam the Family to be emitted
     * @return a string-representation.
     */
    private String dump(Fam fam)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(fam+LB);
        buf.append("h:"+ fam.getHusband()+LB);
        buf.append("w:"+fam.getWife()+LB);          
        for( Indi child: fam.getChildren() )
        {
            buf.append("\t");
            buf.append(child.toString());
            buf.append(LB);
        }
        buf.append("\t");
        return buf.toString();
    }
    
    private String indent(String str )
    {
        StringBuffer buf = new StringBuffer(str.length());
        for( String line : str.split("\\r?\\n"))
        {
            buf.append("  ");
            buf.append(line);
            buf.append(LB);
        }
        return buf.toString();
    }

    protected String brief(final Indi theInd)
    {
        StringBuffer buf = new StringBuffer(theInd.toString());
        buf.append( "[");
        buf.append( sexMap.get(theInd.getSex()));
        buf.append("]");
        buf.append(LB);
        buf.append(" ");
        buf.append(" born:{");
        buf.append(theInd.getBirthAsString());
        buf.append("}  died:{");
        buf.append(theInd.getDeathAsString());
        buf.append("}");
        buf.append(LB);
        return buf.toString();
    }
    
    protected String dump(final Indi theInd)
    {
        StringBuffer buf = new StringBuffer(brief(theInd));
        Fam bioKidFamily = theInd.getFamilyWhereBiologicalChild();
        if( null != bioKidFamily)
        {
            buf.append("Child in family:"+LB);
            buf.append(indent(dump(bioKidFamily)));
        }
        buf.append("Marriages:");
        buf.append(LB);
        Fam[] spouseFamilies = theInd.getFamiliesWhereSpouse();
        for(Fam fam : spouseFamilies)
        {
            buf.append(indent(dump(fam)));
        }
        return buf.toString();
    }

    protected Indi createChild(final Indi parent, int marriageIndex, int sex) throws GedcomException
    {
        Fam[] families = parent.getFamiliesWhereSpouse();
        Fam theFamily;
        if( families.length > 1)
        {
            theFamily = families[marriageIndex];
        }
        else if( families.length== 0)
        {
            createFamilyAndSpouse(parent);
            theFamily = parent.getFamiliesWhereSpouse()[0];
        }
        else
            theFamily = families[0];
        Indi child = (Indi)gedcom.createEntity(Gedcom.INDI);
        child.setSex(sex);
        theFamily.addChild(child);
        Indi father = child.getBiologicalFather();
        child.setName("",father.getLastName());
        return child;
    }
    
    protected Indi createFamilyAndSpouse(Indi ti) throws GedcomException
    {
        Fam theFamily =  (Fam) gedcom.createEntity(Gedcom.FAM);
        Indi spouse = (Indi)gedcom.createEntity(Gedcom.INDI);
        if(ti.getSex() == PropertySex.FEMALE)
        {
            theFamily.setWife(ti);
            spouse.setSex(PropertySex.MALE);
            theFamily.setHusband(spouse);
        }
        else
        {
            theFamily.setHusband(ti);
            spouse.setSex(PropertySex.FEMALE);
            theFamily.setWife(spouse);
        }
        return spouse;
    }
    
    protected final Fam getCreateBiologicalFamily(Indi ti ) throws GedcomException
    {
        Fam theFam =  ti.getFamilyWhereBiologicalChild();
        if( null == theFam)
        {
            Indi dad = createParent(ti,PropertySex.MALE);
            theFam =  ti.getFamilyWhereBiologicalChild();
        }
        return theFam;
    }
    
    protected Indi createBiologicalSibling(Indi ti, int sex) throws GedcomException {
        Fam theFam =  getCreateBiologicalFamily(ti);
        Indi child = (Indi)gedcom.createEntity(Gedcom.INDI);
        child.setSex(sex);
        theFam.addChild(child);
        Indi father = child.getBiologicalFather();
        child.setName("",father.getLastName());
        return child;       
    }
    
    
    /*
     * creates a pair of parents, and returns one of them.  
     * Also, link theChild in as a the sole child of the new FAM 
     * <B>NOTE</b> This won't be appropriate in 100% of cases, (just 95).
     */
    protected Indi createParent(Indi theChild, int sex) throws GedcomException
    {
        if( null != theChild.getFamilyWhereBiologicalChild())
            throw new IllegalArgumentException("can't have >1 biological Family");
        Indi parent = (Indi)gedcom.createEntity(Gedcom.INDI);
        parent.setSex(sex);
        Indi newOtherParent = createFamilyAndSpouse(parent);
        if( PropertySex.MALE  == sex)
            parent.setName("",theChild.getLastName());
        else
            newOtherParent.setName("",theChild.getLastName());
        Fam newFamily = parent.getFamiliesWhereSpouse()[0];
        newFamily.addChild(theChild);
        return parent;
    }

    /**
     * 
     * @param date the date property to set
     * @param newValue the new string value
     * @return true if the value was set.
     */
    protected boolean setDate(PropertyDate date, String newValue)
    {
        String oldValue = date.getValue();
        date.setValue(newValue);
        if( date.isValid())
            return true;
        out.println("Couldn't parse the date.");
        date.setValue(oldValue);
        assert(date.isValid());
        return false;
    }
    
    private static String getVersion()
    {
        return "This is GenJ-Console version $Revision: 1.17 $".replace("Revision:","").replace("$","");
    }
    

    private static String getHelpText(Map<List<String>, Action> actionMap) {
        
        /*
        String[] help = {" COMMAND LIST :",
        "====Entity Creation ====",
        "cdau - create daughter ",
        "cson - create son ",
        "csis - create sister ",
        "cbro - create brother ",
        "cspo- create spouse",
        "cdad- create father",
        "cmom- create mother",

        "====Navigation=====",
        "gspo [n]- goto [nth] spouse",
        "gsib [n]- goto next[nth] sibling",
        "gchi [n]- goto first[nth] child",
        "gmom - goto mother",
        "gdad - goto father",
        
        "====Edits=====",
        "snam, n - set name",
        "anam - add name",
        "bday, b - set birthdate",
        "dday, d - set death date",
        
        "==OTHER==",
        "quit    -  exits the program",
        "save [filename] - saves",
        "help  - display this message",
        ""};
        */
        StringBuffer buf = new StringBuffer(1000);
        buf.append("Available Commands:");
        buf.append(LB);
        for( List<String> actionKey: actionMap.keySet())
        {
            Action a = actionMap.get(actionKey);
            for(String cmdName : actionKey)
            {
                buf.append(cmdName);
                buf.append(" ");
                switch (a.getArgUse())
                {
                case ARG_OPTIONAL:
                    buf.append('[');
                    buf.append(a.getArgName());
                    buf.append(']');
                    break;
                case ARG_YES:
                    buf.append(a.getArgName());
                    break;
                }
                buf.append(LB);
            }
            buf.append("-");
            buf.append(a.getDoc());
            buf.append(LB);
            buf.append(LB);
        }
        return buf.toString();
    }
    
    private void setFirstName(Indi indi, String firstName) {
        indi.setName(firstName,indi.getLastName());
    }

}


