import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.ReferenceSet;
import genj.util.Resources;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

/**
 * GenJ -  ReportPhonetics
 * @version 0.2
 */
public class ReportPhonetics extends Report {

    public int outputFormat = 0;
    public boolean reportFirstNames = true;

    public static String[] outputFormats = {
        "Soundex", "Metaphone", "Double Metaphone", "NYSIIS", "Phonex"
    };

    private static Phonetics[] phonetics = {
        new Soundex(),
        new Metaphone(),
        new DoubleMetaphone(),
        new Nysiis(),
        new Phonex()
    };

    /**
     * Indication of how this reports shows information
     * to the user. Standard Out here only.
     */
    public boolean usesStandardOut() {
        return true;
    }

    /**
     * Tells whether this report doesn't change information in the Gedcom-file
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * @see genj.report.Report#accepts(java.lang.Object)
     */
    public String accepts(Object context) {
        // we accept GEDCOM or Individuals
        return context instanceof Indi || context instanceof Gedcom ? getName() : null;
    }

    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) {
      Entity[] indis = gedcom.getEntities(Gedcom.INDI, "");
      printPhonetic(gedcom, indis);
    }

    /**
     * Main for argument Individual
     */
    public void start(Indi indi) {

          String selection = (String) getValueFromUser(translate("select"), outputFormats, null);
          if (selection == null)
              return;
          else {
              for (int i = 0; i < outputFormats.length; i++) {
                  if (selection.equals(outputFormats[i])) {
                      outputFormat = i;
                      break;
                  }
              }
          }
          printPhonetic(indi);
    }

    /**
     * Returns the category of this report.
     */
    public Category getCategory() {
        return CATEGORY_ANALYSIS;
    }

    private void printPhonetic(Gedcom gedcom, Entity[] indis) {
        Indi indi = null;
        String str = "";

        println(translate("outputFormat")+": "+outputFormats[outputFormat]);
        println();

        if(reportFirstNames) {
            ReferenceSet names = new ReferenceSet();
            for (int i = 0; i < indis.length; i++) {
                indi = (Indi) indis[i];
                names.add(indi.getLastName(), indi);
            }
            Iterator last = names.getKeys(gedcom.getCollator()).iterator();
            while(last.hasNext()) {
                str = (String)last.next();
                println(str+": "+encode(str));
                Iterator first = names.getReferences(str).iterator();
                while(first.hasNext()) {
                    indi  = (Indi)first.next();
                    println(getIndent(2)+indi.getFirstName()+" ("+indi.getId()+")"+": "+encode(str));
                }
            }
        }
        else {
            TreeSet names = new TreeSet();
            for (int i = 0; i < indis.length; i++) {
                indi = (Indi) indis[i];
                names.add(indi.getLastName());
            }
            Iterator it = names.iterator();
            while(it.hasNext()) {
                str = (String)it.next();
                println(str+": "+encode(str));
            }
        }
    }

    private void printPhonetic(Indi indi) {

        // grab information from indi
        String firstName = indi.getFirstName();
        String lastName = indi.getLastName();

        println(translate("outputFormat")+": "+outputFormats[outputFormat]);
        println();

        if(reportFirstNames) {
            println(firstName+": "+encode(firstName));
            println(lastName+": "+encode(lastName));
        }
        else {
            println(lastName+": "+encode(lastName));
        }
    }

    private String encode(String input) {
        Phonetics p = phonetics[outputFormat];
        String s = p.encode(input, getResources());
        return s==null ? "" : s;
    }

    /**
     * Our phonetics interface
     */
    interface Phonetics {

        /**
         * encode implementation
         * @param resources TODO
         */
        public String encode(String name, Resources resources);

    } //Phonetics

    /**
     * from http://www.bgw.org/projects/java/
     * says GNU openSource but not sure what license
     *
     * This code is based on an implementation by Ed Parrish, which was
     * obtained from:
     *
     *    http://www.cse.ucsc.edu/~eparrish/toolbox/search.html
     *
     * also available (Apache Licsence) in the
     * org.apache.commons.codec.language package
     */

    static class DoubleMetaphone implements Phonetics {

        private int current;
        private int encodeLimit = 4;
        private StringBuffer primary = new StringBuffer();
        private StringBuffer alternate = new StringBuffer();
        private String input;

        private final static char[] vowels = { 'A', 'E', 'I', 'O', 'U', 'Y' };
        private final static char[] AEOU = { 'A', 'E', 'O', 'U' };
        private final static char[] AO = "AO".toCharArray();
        private final static char[] BDH = { 'B', 'D', 'H' };
        private final static char[] BFHLMNRVW_ = "BFHLMNRVW ".toCharArray();
        private final static char[] BH = { 'B', 'H' };
        private final static char[] BKLMNSTZ = "LTKSNMBZ".toCharArray();
        private final static char[] BP = "BP".toCharArray();
        private final static char[] CGQ = { 'C', 'G', 'Q' };
        private final static char[] CGLRT = { 'C', 'G', 'L', 'R', 'T' };
        private final static char[] CKQ = { 'C', 'K', 'Q' };
        private final static char[] CX = "CX".toCharArray();
        private final static char[] DT = "DT".toCharArray();
        private final static char[] EI = { 'E', 'I' };
        private final static char[] EIY = { 'E', 'I', 'Y' };
        private final static char[] EHI = { 'I', 'E', 'H' };
        private final static char[] KLS = "KLS".toCharArray();
        private final static char[] LMNW = "LMNW".toCharArray();
        private final static char[] ST = { 'S', 'T' };
        private final static char[] SZ = "SZ".toCharArray();
        private final static String[] AggiOggi = { "AGGI", "OGGI" };
        private final static String[] AiOi = { "AI", "OI" };
        private final static String[] AlleIllaIllo = { "ILLO", "ILLA", "ALLE" };
        private final static String[] AmOm = { "OM", "AM" };
        private final static String[] AsOs = { "AS", "OS" };
        private final static String[] ArchitOrchesOrchid = { "ARCHIT", "ORCHES", "ORCHID" };
        private final static String[] AuOu = { "AU", "OU" };
        private final static String[] BacherMacher = { "BACHER", "MACHER" };
        private final static String[] CeCiCy = { "CI", "CE", "CY" };
        private final static String[] CeCi = { "CE", "CI" };
        private final static String[] CiaCieCio = { "CIO", "CIE", "CIA" };
        private final static String[] CkCgCq = { "CK", "CG", "CQ" };
        private final static String[] DangerMangerRanger = { "DANGER", "RANGER", "MANGER" };
        private final static String[] DdDt = { "DD", "DT" };
        private final static String[] EauIau = { "IAU", "EAU" };
        private final static String[] EbEiElEpErEsEyIbIlInIe = { "ES", "EP", "EB", "EL", "EY", "IB", "IL", "IN", "IE", "EI", "ER" };
        private final static String[] EdEmEnErOoUy = { "OO", "ER", "EN", "UY", "ED", "EM" };
        private final static String[] EnEr = { "ER", "EN" };
        private final static String[] EwskiEwskyOwskiOwsky = { "EWSKI", "EWSKY", "OWSKI", "OWSKY" };
        private final static String[] GnKnPnPsWr = { "GN", "KN", "PN", "WR", "PS" };
        private final static String[] HaracHaris = { "HARAC", "HARIS" };
        private final static String[] HeimHoekHolmHolz = { "HEIM", "HOEK", "HOLM", "HOLZ" };
        private final static String[] HemHiaHorHym = { "HOR", "HYM", "HIA", "HEM" };
        private final static String[] IslYsl = { "ISL", "YSL" };
        private final static String[] MaMe = { "ME", "MA" };
        private final static String[] OgyRgy = { "RGY", "OGY" };
        private final static String[] SiaSio = { "SIO", "SIA" };
        private final static String[] TiaTch = { "TIA", "TCH" };
        private final static String[] UcceeUcces = { "UCCEE", "UCCES" };
        private final static String[] Van_Von_ = { "VAN ", "VON " };
        private final static String[] WiczWitz = { "WICZ", "WITZ" };
        private final static String[] ZaZiZo = { "ZO", "ZI", "ZA" };

        /** Creates new DoubleMetaphone */
        public DoubleMetaphone() {
        }

        public String getPrimary() {
            return primary.toString();
        }

        public StringBuffer getPrimaryBuffer() {
            return primary;
        }

        public String getAlternate() {
            return alternate.toString();
        }

        public StringBuffer getAlternateBuffer() {
            return alternate;
        }

        public int getEncodeLimit() {
            return encodeLimit;
        }

        public boolean setEncodeLimit(int newLimit) {
            if (newLimit < 1)
                return false;
            encodeLimit = newLimit;
            return true;
        }

        void setInput(String in) {
            if (in != null) {
                input = in.toUpperCase() + "     ";
            } else {
                input = "";
            }
        }

        void add(char ch) {
            add(ch, ch);
        }

        void add(char primaryChar, char alternateChar) {
            primary.append(primaryChar);
            alternate.append(alternateChar);
        }

        boolean charAt(int index, char[] list) {
            if (index < 0 || index >= input.length())
                return false;
            char value = input.charAt(index);
            for (int i = 0; i < list.length; i++) {
                if (value == list[i])
                    return true;
            }
            return false;
        }

        boolean stringAt(int start, int length, String str) {
            String[] list = new String[1];
            list[0] = str;
            return stringAt(start, length, list);
        }

        boolean stringAt(int start, int length, String[] list) {
            if (length <= 0)
                return false;
            for (int i = 0; i < list.length; i++) {
                if (input.regionMatches(start, list[i], 0, length))
                    return true;
            }
            return false;
        }

        boolean isVowel(int index) {
            return charAt(index, vowels);
        }

        boolean isSlavoGermanic() {
            if ((input.indexOf('W') > -1) || (input.indexOf('K') > -1) || (input.indexOf("CZ") > -1) || (input.indexOf("WITZ") > -1)) {
                return true;
            }
            return false;
        }

        void addCode(char ch, char code) {
            add(code);
            current++;
            if (input.charAt(current) == ch)
                current++;
        }

        public String encode(String in, Resources resources) {
            if (in == null || in.length() == 0)
                return null;

            primary.delete(0, primary.length());
            alternate.delete(0, alternate.length());
            int length = in.length();
            if (length < 1)
                return "";
            int last = length - 1; //zero based index
            setInput(in);
            current = 0;

            //skip these when at start of word
            if (stringAt(0, 2, GnKnPnPsWr))
                current++;

            //Initial 'X' is pronounced 'Z' e.g. 'Xavier'
            if (input.startsWith("X")) {
                add('S'); //'Z' maps to 'S'
                current++;
            }

            while (primary.length() < encodeLimit || alternate.length() < encodeLimit) {
                if (current >= length)
                    break;

                switch (input.charAt(current)) {
                    case 'A' :
                    case 'E' :
                    case 'I' :
                    case 'O' :
                    case 'U' :
                    case 'Y' :
                        if (current == 0)
                            add('A'); // all init vowels map to 'A'
                        current++;
                        break;

                    case 'B' :
                        // "-mb", e.g "dumb", already skipped over...
                        addCode('B', 'P');
                        break;

                    case '\u00c7' : // C with an accent at the bottom (C;)
                        add('S');
                        current++;
                        // Note: no doublecheck
                        break;

                    case 'C' :
                        // various germanic
                        if ((current > 1) && !isVowel(current - 2) && input.regionMatches(current - 1, "ACH", 0, 3) && (input.charAt(current + 2) != 'I' && input.charAt(current + 2) != 'E' || stringAt(current - 2, 6, BacherMacher))) {
                            add('K');
                            current += 2;
                            break;
                        }

                        // special case 'caesar'
                        if (current == 0 && input.regionMatches(current, "CAESAR", 0, 6)) {
                            add('S');
                            current += 2;
                            break;
                        }

                        //italian 'chianti'
                        if (input.regionMatches(current, "CHIA", 0, 4)) {
                            add('K');
                            current += 2;
                            break;
                        }

                        if (input.regionMatches(current, "CH", 0, 2)) {
                            //find 'michael'
                            if (current > 0 && input.regionMatches(current, "CHAE", 0, 4)) {
                                add('K', 'X');
                                current += 2;
                                break;
                            }

                            // greek roots e.g. 'chemistry', 'chorus'
                            if (current == 0 && (stringAt(current + 1, 5, HaracHaris) || stringAt((current + 1), 3, HemHiaHorHym)) && !input.regionMatches(0, "CHORE", 0, 5)) {
                                add('K');
                                current += 2;
                                break;
                            }

                            // germanic, greek, or otherwise 'ch' for 'kh' sound
                            if ((stringAt(0, 4, Van_Von_) || input.regionMatches(0, "SCH ", 0, 3)) // 'architect' but not 'arch', 'orchestra', 'orchid'
                            || stringAt(0, 6, ArchitOrchesOrchid)
                            || charAt(current + 2, ST)
                            || ((charAt(current - 1, AEOU) || current == 0) // e.g. 'wachtler', 'wechsler', but not 'tichner'
                            && charAt(current + 2, BFHLMNRVW_))) {
                                add('K');
                            } else {
                                if (current > 0) {
                                    if (input.regionMatches(0, "MC", 0, 2)) {
                                        // e.g. "McHugh"
                                        add('K');
                                    } else {
                                        add('X', 'K');
                                    }
                                } else {
                                    add('X');
                                }
                            }
                            current += 2;
                            break;
                        }

                        // e.g. 'czerny'
                        if (input.regionMatches(current, "CZ", 0, 2) && !input.regionMatches(current - 2, "WICZ", 0, 4)) {
                            add('S', 'X');
                            current += 2;
                            break;
                        }

                        // e.g. 'focaccia'
                        if (input.regionMatches(current + 1, "CIA", 0, 3)) {
                            add('X');
                            current += 3;
                            break;
                        }

                        // double 'C', but not if e.g. 'McClellan'
                        if (input.regionMatches(current, "CC", 0, 2) && !((current == 1) && (input.charAt(0) == 'M'))) {
                            // 'bellocchio' but not 'bacchus'
                            if (charAt(current + 2, EHI) && !input.regionMatches(current + 2, "HU", 0, 2)) {
                                // 'accident', 'accede' 'succeed'
                                if (((current == 1) && (input.charAt(current - 1) == 'A')) || stringAt(current - 1, 5, UcceeUcces)) {
                                    add('K');
                                    add('S');
                                } else { // 'bacci', 'bertucci', other italian
                                    add('X');
                                }
                                current += 3;
                                break;
                            } else { // Pierce's rule
                                add('K');
                                current += 2;
                                break;
                            }
                        }

                        if (stringAt(0, 2, CkCgCq)) {
                            add('K');
                            current += 2;
                            break;
                        }

                        if (stringAt(0, 2, CeCiCy)) {
                            // italian vs. english
                            if (stringAt(0, 3, CiaCieCio)) {
                                add('S', 'X');
                            } else {
                                add('S');
                            }
                            current += 2;
                            break;
                        }

                        // else
                        add('K');

                        // name sent in 'mac caffrey', 'mac gregor'
                        if (charAt(current + 1, CGQ)) {
                            current += 3;
                        } else {
                            if (charAt(current + 1, CKQ) && !stringAt(current + 1, 2, CeCi)) {
                                current += 2;
                            } else {
                                current++;
                            }
                        }
                        break;

                    case 'D' :
                        if (input.regionMatches(current, "DG", 0, 2)) {
                            if (charAt(current + 2, EIY)) {
                                //e.g. 'edge'
                                add('J');
                                current += 3;
                                break;
                            } else {
                                //e.g. 'edgar'
                                add('T');
                                add('K');
                                current += 2;
                                break;
                            }
                        }

                        if (stringAt(current, 2, DdDt)) {
                            add('T');
                            current += 2;
                            break;
                        }

                        //else
                        add('T');
                        current++;
                        break;

                    case 'F' : // NTR: this is typical default behavior
                        addCode('F', 'F');
                        break;

                    case 'G' :
                        if (input.charAt(current + 1) == 'H') {
                            if (current > 0 && !isVowel(current - 1)) {
                                add('K');
                                current += 2;
                                break;
                            }

                            if (current < 3) {
                                // 'ghislane', 'ghiradelli'
                                if (current == 0) {
                                    if (input.charAt(current + 2) == 'I') {
                                        add('J');
                                    } else {
                                        add('K');
                                    }
                                    current += 2;
                                    break;
                                }
                            }
                            //Parker's rule (with some further refinements) - e.g., 'hugh'
                            if ((current > 1 && charAt(current - 2, BDH)) //e.g., 'bough'
                            || (current > 2 && charAt(current - 3, BDH)) //e.g., 'broughton'
                            || (current > 3 && charAt(current - 4, BH))) {
                                current += 2;
                                break;
                            } else {
                                //e.g., 'laugh', 'McLaughlin', 'cough', 'gough', 'rough', 'tough'
                                if (current > 2 && input.charAt(current - 1) == 'U' && charAt(current - 3, CGLRT)) {
                                    add('F');
                                } else {
                                    if (current > 0 && input.charAt(current - 1) != 'I') {
                                        add('K');
                                    }
                                }
                                current += 2;
                                break;
                            }
                        }

                        boolean slavoGermanic = isSlavoGermanic();
                        if (input.charAt(current + 1) == 'N') {
                            if (current == 1 && isVowel(0) && !slavoGermanic) {
                                primary.append('K');
                                add('N');
                            } else {
                                //not e.g. 'cagney'
                                if (!input.regionMatches(current + 2, "EY", 0, 2) && (input.charAt(current + 1) != 'Y') && !slavoGermanic) {
                                    alternate.append('K');
                                    add('N');
                                } else {
                                    add('K');
                                    add('N');
                                }
                                current += 2;
                                break;
                            }
                        }

                        //'tagliaro'
                        if (input.regionMatches(current + 1, "LI", 0, 2) && !slavoGermanic) {
                            primary.append('K');
                            add('L');
                            current += 2;
                            break;
                        }

                        //-ges-,-gep-,-gel-, -gie- at beginning
                        if ((current == 0) && (input.charAt(current + 1) == 'Y' || stringAt(current + 1, 2, EbEiElEpErEsEyIbIlInIe))) {
                            add('K', 'J');
                            current += 2;
                            break;
                        }

                        // -ger-,  -gy-
                        if ((input.regionMatches(current + 1, "ER", 0, 2) || input.charAt(current + 1) == 'Y') && !stringAt(0, 6, DangerMangerRanger) && !charAt(current - 1, EI) && !stringAt(current - 1, 3, OgyRgy)) {
                            add('K', 'J');
                            current += 2;
                            break;
                        }

                        // italian e.g, 'biaggi'
                        if (charAt(current + 1, EIY) || stringAt(current - 1, 4, AggiOggi)) {
                            //obvious germanic
                            if ((stringAt(0, 4, Van_Von_) || input.regionMatches(0, "SCH", 0, 3)) || input.regionMatches(current + 1, "ET", 0, 2)) {
                                add('K');
                            } else {
                                //always soft if french ending
                                if (input.regionMatches(current + 1, "IER ", 0, 4)) {
                                    add('J');
                                } else {
                                    add('J', 'K');
                                }
                                current += 2;
                                break;
                            }
                        }

                        if (input.charAt(current + 1) == 'G') {
                            current += 2;
                        } else {
                            current++;
                        }
                        add('K');
                        break;

                    case 'H' :
                        // only keep if first & before vowel or btw. 2 vowels
                        if ((current == 0 || isVowel(current - 1)) && isVowel(current + 1)) {
                            add('H');
                            current += 2;
                        } else { // also takes care of 'HH'
                            current++;
                        }
                        break;

                    case 'J' :
                        //obvious spanish, 'jose', 'san jacinto'
                        if (stringAt(current, 4, "JOSE") || stringAt(0, 4, "SAN ")) {
                            if ((current == 0 && (input.charAt(current + 4) == ' ')) || stringAt(0, 4, "SAN ")) {
                                add('H');
                            } else {
                                add('J', 'H');
                            }
                            current += 1;
                            break;
                        }

                        if (current == 0 && !stringAt(current, 4, "JOSE")) {
                            add('J', 'A'); // Yankelovich/Jankelowicz
                        } else {
                            // spanish pron. of e.g. 'bajador'
                            if (isVowel(current - 1) && !isSlavoGermanic() && ((input.charAt(current + 1) == 'A') || (input.charAt(current + 1) == 'O'))) {
                                add('J', 'H');
                            } else {
                                if (current == last) {
                                    add('J', ' ');
                                } else {
                                    if (!charAt(current + 1, BKLMNSTZ) && !charAt(current - 1, KLS)) {
                                        add('J');
                                    }
                                }
                            }
                        }

                        current++;
                        if (input.charAt(current) == 'J')
                            current++; // doublecheck
                        break;

                    case 'K' : // NTR: this is typical default behavior
                        addCode('K', 'K');
                        break;

                    case 'L' :
                        if (input.charAt(current + 1) == 'L') {
                            //spanish e.g. 'cabrillo', 'gallegos'
                            if (((current == (length - 3)) && stringAt(current - 1, 4, AlleIllaIllo)) || ((stringAt((last - 1), 2, AsOs) || charAt(last, AO)) && stringAt(current - 1, 4, "ALLE"))) {
                                primary.append('L');
                                current += 2;
                                break;
                            }
                            current += 2;
                        } else {
                            current++;
                        }
                        add('L');
                        break;

                    case 'M' :
                        if ((stringAt(current - 1, 3, "UMB") && (((current + 1) == last) || stringAt(current + 2, 2, "ER"))) //'dumb','thumb'
                        || (input.charAt(current + 1) == 'M')) {
                            current += 2;
                        } else {
                            current++;
                        }
                        add('M');
                        break;

                    case 'N' : // NTR: this is typical default behavior
                        addCode('N', 'N');
                        break;

                    case '\u00D1': // N with a wiggle (N~)
                        current++;
                        add('N');
                        break;

                    case 'P' :
                        if (input.charAt(current + 1) == 'H') {
                            add('F');
                            current += 2;
                            break;
                        }

                        //also account for 'campbell', 'raspberry'
                        if (charAt(current + 1, BP))
                            current += 2;
                        else
                            current++;
                        add('P');
                        break;

                    case 'Q' : // NTR: this is typical default behavior
                        addCode('Q', 'K');
                        break;

                    case 'R' :
                        //french e.g. 'rogier', but exclude 'hochmeier'
                        if ((current == last) && !isSlavoGermanic() && stringAt(current - 2, 2, "IE") && !stringAt(current - 4, 2, MaMe)) {
                            alternate.append('R');
                        } else {
                            add('R');
                        }

                        current++;
                        if (input.charAt(current) == 'R')
                            current++; // doublecheck
                        break;

                    case 'S' :
                        //special cases 'island', 'isle', 'carlisle', 'carlysle'
                        if (stringAt(current - 1, 3, IslYsl)) {
                            current++;
                            break;
                        }

                        //special case 'sugar-'
                        if ((current == 0) && stringAt(current, 5, "SUGAR")) {
                            add('X', 'S');
                            current++;
                            break;
                        }

                        if (stringAt(current, 2, "SH")) {
                            //germanic
                            if (stringAt(current + 1, 4, HeimHoekHolmHolz)) {
                                add('S');
                            } else {
                                add('X');
                            }
                            current += 2;
                            break;
                        }

                        //italian & armenian
                        if (stringAt(current, 3, SiaSio) || stringAt(current, 4, "SIAN")) {
                            if (!isSlavoGermanic()) {
                                add('S', 'X');
                            } else {
                                add('S');
                            }
                            current += 3;
                            break;
                        }

                        //german & anglicisations, e.g. 'smith' match 'schmidt', 'snider' match 'schneider'
                        //also, -sz- in slavic language altho in hungarian it is pronounced 's'
                        if ((current == 0 && charAt(current + 1, LMNW)) || input.charAt(current + 1) == 'Z') {
                            add('S', 'X');
                            if (input.charAt(current + 1) == 'Z') {
                                current += 2;
                            } else {
                                current++;
                            }
                            break;
                        }

                        if (stringAt(current, 2, "SC")) {
                            //Schlesinger's rule
                            if (input.charAt(current + 2) == 'H') {
                                //dutch origin, e.g. 'school', 'schooner'
                                if (stringAt(current + 3, 2, EdEmEnErOoUy)) {
                                    //'schermerhorn', 'schenker'
                                    if (stringAt((current + 3), 2, EnEr)) {
                                        add('X', 'S');
                                        alternate.append('K');
                                    } else {
                                        add('S');
                                        add('K');
                                    }
                                    current += 3;
                                    break;
                                } else {
                                    if (current == 0 && !isVowel(3) && input.charAt(3) != 'W') {
                                        add('X', 'S');
                                    } else {
                                        add('X');
                                    }
                                    current += 3;
                                    break;
                                }
                            }

                            if (charAt(current + 2, EIY)) {
                                add('S');
                                current += 3;
                                break;
                            }

                            //else
                            add('S');
                            add('K');
                            current += 3;
                            break;
                        }

                        //french e.g. 'resnais', 'artois'
                        if (current == last && stringAt(current - 2, 2, AiOi)) {
                            alternate.append('S');
                        } else {
                            add('S');
                        }

                        if (charAt(current + 1, SZ)) {
                            current += 2;
                        } else {
                            current++;
                        }
                        break;

                    case 'T' :
                        if (stringAt(current, 4, "TION")) {
                            add('X');
                            current += 3;
                            break;
                        }

                        if (stringAt(current, 3, TiaTch)) {
                            add('X');
                            current += 3;
                            break;
                        }

                        if (stringAt(current, 2, "TH") || stringAt(current, 3, "TTH")) {
                            //special case 'thomas', 'thames' or germanic
                            if (stringAt(current + 2, 2, AmOm) || stringAt(0, 4, Van_Von_) || stringAt(0, 3, "SCH")) {
                                add('T');
                            } else {
                                add('0', 'T');
                            }
                            current += 2;
                            break;
                        }

                        if (charAt(current + 1, DT))
                            current += 2;
                        else
                            current++;
                        add('T');
                        break;

                    case 'V' : // NTR: this is typical default behavior
                        addCode('V', 'F');
                        break;

                    case 'W' :
                        //can also be in middle of word
                        if (stringAt(current, 2, "WR")) {
                            add('R');
                            current += 2;
                            break;
                        }

                        if (current == 0 && (isVowel(current + 1) || stringAt(current, 2, "WH"))) {
                            //Wasserman should match Vasserman
                            if (isVowel(current + 1)) {
                                add('A', 'F');
                            } else {
                                //need 'Uomo' to match 'Womo'
                                add('A');
                            }
                        }

                        //'Arnow' should match 'Arnoff'
                        if ((current == last && isVowel(current - 1)) || stringAt(current - 1, 5, EwskiEwskyOwskiOwsky) || stringAt(0, 3, "SCH")) {
                            alternate.append('F');
                            current += 1;
                            break;
                        }

                        //polish e.g. 'filipowicz'
                        if (stringAt(current, 4, WiczWitz)) {
                            add('T', 'F');
                            add('S', 'X');
                            current += 4;
                            break;
                        }

                        //else skip it
                        current += 1;
                        break;

                    case 'X' :
                        //french e.g. breaux
                        if (!(current == last && (stringAt((current - 3), 3, EauIau) || stringAt((current - 2), 2, AuOu)))) {
                            add('K');
                            add('S');
                        }

                        if (charAt(current + 1, CX)) {
                            current += 2;
                        } else {
                            current++;
                        }
                        break;

                    case 'Z' :
                        //chinese pinyin e.g. 'zhao'
                        if (input.charAt(current + 1) == 'H') {
                            add('J');
                            current += 2;
                            break;
                        } else {
                            if (stringAt(current + 1, 2, ZaZiZo) || (isSlavoGermanic() && (current > 0 && input.charAt(current - 1) != 'T'))) {
                                alternate.append('T');
                                add('S');
                            } else {
                                add('S');
                            }
                        }

                        if (input.charAt(current + 1) == 'Z') {
                            current += 2;
                        } else {
                            current++;
                        }
                        break;

                    case '0' :
                    case '1' :
                    case '2' :
                    case '3' :
                    case '4' :
                    case '5' :
                    case '6' :
                    case '7' :
                    case '8' :
                    case '9' :
                        add(input.charAt(current));
                        current++;
                        break;

                    default :
                        current++;
                } // switch
            } // while

            // Only give back the specified length
            if (primary.length() > encodeLimit) {
                primary.delete(encodeLimit, primary.length());
            }
            if (alternate.length() > encodeLimit) {
                alternate.delete(encodeLimit, alternate.length());
            }

            return primary.toString();
        }
    } //DoubleMetaphone

    static class Metaphone implements Phonetics {

        private static String vowels = "AEIOU";
        private static String frontv = "EIY";
        private static String varson = "CSPTG";
        private static final int maxCodeLen = 4;

        /**
         * get the MetaPhone code for THE FIRST WORD in this string
         */
        public String encode(String txt, Resources resources) {
            int mtsz = 0;
            boolean hard = false;
            if ((txt == null) || (txt.length() == 0))
                return null;

            // check the first letter is a character
            if (!Character.isLetter(txt.charAt(0)))
                return encode(txt.substring(1), resources);

            // single character is itself
            if (txt.length() == 1)
                return txt.toUpperCase();
            //
            char[] inwd = txt.toUpperCase().toCharArray();
            //
            String tmpS;
            StringBuffer local = new StringBuffer(40); // manipulate
            StringBuffer code = new StringBuffer(10); //   output
            // handle initial 2 characters exceptions
            switch (inwd[0]) {
                case 'K' :
                case 'G' :
                case 'P' : /* looking for KN, etc*/
                    if (inwd[1] == 'N')
                        local.append(inwd, 1, inwd.length - 1);
                    else
                        local.append(inwd);
                    break;
                case 'A' : /* looking for AE */
                    if (inwd[1] == 'E')
                        local.append(inwd, 1, inwd.length - 1);
                    else
                        local.append(inwd);
                    break;
                case 'W' : /* looking for WR or WH */
                    if (inwd[1] == 'R') { // WR -> R
                        local.append(inwd, 1, inwd.length - 1);
                        break;
                    }
                    if (inwd[1] == 'H') {
                        local.append(inwd, 1, inwd.length - 1);
                        local.setCharAt(0, 'W'); // WH -> W
                    } else
                        local.append(inwd);
                    break;
                case 'X' : /* initial X becomes S */
                    inwd[0] = 'S';
                    local.append(inwd);
                    break;
                default :
                    local.append(inwd);
            } // now local has working string with initials fixed
            int wdsz = local.length();
            int n = 0;
            while ((mtsz < maxCodeLen) && // max code size of 4 works well
            (n < wdsz)) {
                char symb = local.charAt(n);
                // remove duplicate letters except C
                if ((symb != 'C') && (n > 0) && (local.charAt(n - 1) == symb))
                    n++;
                else { // not dup
                    switch (symb) {
                        case 'A' :
                        case 'E' :
                        case 'I' :
                        case 'O' :
                        case 'U' :
                            if (n == 0) {
                                code.append(symb);
                                mtsz++;
                            }
                            break; // only use vowel if leading char
                        case 'B' :
                            if ((n > 0) && !(n + 1 == wdsz) && // not MB at end of word
                            (local.charAt(n - 1) == 'M')) {
                                code.append(symb);
                            } else
                                code.append(symb);
                            mtsz++;
                            break;
                        case 'C' : // lots of C special cases
                            /* discard if SCI, SCE or SCY */
                            if ((n > 0) && (local.charAt(n - 1) == 'S') && (n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0)) {
                                break;
                            }
                            tmpS = local.toString();
                            if (tmpS.indexOf("CIA", n) == n) { // "CIA" -> X
                                code.append('X');
                                mtsz++;
                                break;
                            }
                            if ((n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0)) {
                                code.append('S');
                                mtsz++;
                                break; // CI,CE,CY -> S
                            }
                            if ((n > 0) && (tmpS.indexOf("SCH", n - 1) == n - 1)) { // SCH->sk
                                code.append('K');
                                mtsz++;
                                break;
                            }
                            if (tmpS.indexOf("CH", n) == n) { // detect CH
                                if ((n == 0) && (wdsz >= 3) && // CH consonant -> K consonant
                                (vowels.indexOf(local.charAt(2)) < 0)) {
                                    code.append('K');
                                } else {
                                    code.append('X'); // CHvowel -> X
                                }
                                mtsz++;
                            } else {
                                code.append('K');
                                mtsz++;
                            }
                            break;
                        case 'D' :
                            if ((n + 2 < wdsz) && // DGE DGI DGY -> J
                            (local.charAt(n + 1) == 'G') && (frontv.indexOf(local.charAt(n + 2)) >= 0)) {
                                code.append('J');
                                n += 2;
                            } else {
                                code.append('T');
                            }
                            mtsz++;
                            break;
                        case 'G' : // GH silent at end or before consonant
                            if ((n + 2 == wdsz) && (local.charAt(n + 1) == 'H'))
                                break;
                            if ((n + 2 < wdsz) && (local.charAt(n + 1) == 'H') && (vowels.indexOf(local.charAt(n + 2)) < 0))
                                break;
                            tmpS = local.toString();
                            if ((n > 0) && (tmpS.indexOf("GN", n) == n) || (tmpS.indexOf("GNED", n) == n))
                                break; // silent G
                            if ((n > 0) && (local.charAt(n - 1) == 'G'))
                                hard = true;
                            else
                                hard = false;
                            if ((n + 1 < wdsz) && (frontv.indexOf(local.charAt(n + 1)) >= 0) && (!hard))
                                code.append('J');
                            else
                                code.append('K');
                            mtsz++;
                            break;
                        case 'H' :
                            if (n + 1 == wdsz)
                                break; // terminal H
                            if ((n > 0) && (varson.indexOf(local.charAt(n - 1)) >= 0))
                                break;
                            if (vowels.indexOf(local.charAt(n + 1)) >= 0) {
                                code.append('H');
                                mtsz++; // Hvowel
                            }
                            break;
                        case 'F' :
                        case 'J' :
                        case 'L' :
                        case 'M' :
                        case 'N' :
                        case 'R' :
                            code.append(symb);
                            mtsz++;
                            break;
                        case 'K' :
                            if (n > 0) { // not initial
                                if (local.charAt(n - 1) != 'C') {
                                    code.append(symb);
                                }
                            } else
                                code.append(symb); // initial K
                            mtsz++;
                            break;
                        case 'P' :
                            if ((n + 1 < wdsz) && // PH -> F
                            (local.charAt(n + 1) == 'H'))
                                code.append('F');
                            else
                                code.append(symb);
                            mtsz++;
                            break;
                        case 'Q' :
                            code.append('K');
                            mtsz++;
                            break;
                        case 'S' :
                            tmpS = local.toString();
                            if ((tmpS.indexOf("SH", n) == n) || (tmpS.indexOf("SIO", n) == n) || (tmpS.indexOf("SIA", n) == n))
                                code.append('X');
                            else
                                code.append('S');
                            mtsz++;
                            break;
                        case 'T' :
                            tmpS = local.toString(); // TIA TIO -> X
                            if ((tmpS.indexOf("TIA", n) == n) || (tmpS.indexOf("TIO", n) == n)) {
                                code.append('X');
                                mtsz++;
                                break;
                            }
                            if (tmpS.indexOf("TCH", n) == n)
                                break;
                            // substitute numeral 0 for TH (resembles theta after all)
                            if (tmpS.indexOf("TH", n) == n)
                                code.append('0');
                            else
                                code.append('T');
                            mtsz++;
                            break;
                        case 'V' :
                            code.append('F');
                            mtsz++;
                            break;
                        case 'W' :
                        case 'Y' : // silent if not followed by vowel
                            if ((n + 1 < wdsz) && (vowels.indexOf(local.charAt(n + 1)) >= 0)) {
                                code.append(symb);
                                mtsz++;
                            }
                            break;
                        case 'X' :
                            code.append('K');
                            code.append('S');
                            mtsz += 2;
                            break;
                        case 'Z' :
                            code.append('S');
                            mtsz++;
                            break;
                    } // end switch
                    n++;
                } // end else from symb != 'C'
                if (mtsz > 4)
                    code.setLength(4);
            }
            return code.toString();
        }
    } //MetaPhone

    /**
     * the New York State Identification and Intelligence Algorithm
     * modified from the verion at from http://www.bgw.org/projects/java/
     * says GNU open source but not sure what license (GPL?)
     * also available (Apache Licsence) in the
     * org.apache.commons.codec.language package
     */
    static class Nysiis implements Phonetics {

        boolean debug = false;
        StringBuffer word = null;

        /**
         * encode - Nysiis phonetic encoding.
         * @param  String originalWord
         *
         *
         * @return String - the encoded word
         *
         */
        public String encode(String originalWord, Resources resources) {
            if (originalWord == null || originalWord.length() == 0)
                return null;

            word = new StringBuffer(originalWord.toUpperCase());
            char first;

            //check we actually have a word!
            if (word.length() == 0)
                return null;

            // strip any trailing S or Zs
            while (word.toString().endsWith("S") || word.toString().endsWith("Z")) {
                word.deleteCharAt(word.length() - 1);
            }

            // remove any non character letter
            int current = 0;
            while (current < word.length()) {
                if (!Character.isLetter(word.charAt(current)))
                    word.deleteCharAt(current);
                else
                    current++;
            }

            // check there is a word left!
            if (word.length() == 0)
                return null;

            replaceFront("MAC", "MC");
            replaceFront("PF", "F");
            replaceEnd("IX", "IC");
            replaceEnd("EX", "EC");

            replaceEnd("YE", "Y");
            replaceEnd("EE", "Y");
            replaceEnd("IE", "Y");

            replaceEnd("DT", "D");
            replaceEnd("RT", "D");
            replaceEnd("RD", "D");

            replaceEnd("NT", "N");
            replaceEnd("ND", "N");

            // .EV => .EF
            replaceAll("EV", "EF", 1);

            first = word.charAt(0);

            // replace all vowels with 'A'
            // word = replaceAll(   word, "A",  "A" );
            replaceAll("E", "A");
            replaceAll("I", "A");
            replaceAll("O", "A");
            replaceAll("U", "A");

            // remove any 'W' that follows a vowel
            replaceAll("AW", "A");

            replaceAll("GHT", "GT");
            replaceAll("DG", "G");
            replaceAll("PH", "F");

            replaceAll("AH", "A", 1);
            replaceAll("HA", "A", 1);

            replaceAll("KN", "N");
            replaceAll("K", "C");

            replaceAll("M", "N", 1);
            replaceAll("Q", "G", 1);

            replaceAll("SH", "S");
            replaceAll("SCH", "S");

            replaceAll("YW", "Y");

            replaceAll("Y", "A", 1, word.length() - 2);

            replaceAll("WR", "R");

            replaceAll("Z", "S", 1);

            replaceEnd("AY", "Y");

            while (word.toString().endsWith("A")) {
                word.deleteCharAt(word.length() - 1);
            }

            // if the word was only As will be left with nothing
            if (word.length() == 0)
                return null;

            reduceDuplicates();

            if ('A' == first || 'E' == first || 'I' == first || 'O' == first || 'U' == first) {
                word.deleteCharAt(0);
                word.insert(0, first);
            }

            return word.toString();
        }

        private void reduceDuplicates() {
            char lastChar;
            StringBuffer newWord = new StringBuffer();

            if (0 == word.length()) {
                return;
            }

            lastChar = word.charAt(0);
            newWord.append(lastChar);
            for (int i = 1; i < word.length(); ++i) {
                if (lastChar != word.charAt(i)) {
                    newWord.append(word.charAt(i));
                }
                lastChar = word.charAt(i);
            }

            log("reduceDuplicates: " + word);

            word = newWord;
        }

        private void replaceAll(String find, String repl) {
            replaceAll(find, repl, 0, -1);
        }

        private void replaceAll(String find, String repl, int startPos) {
            replaceAll(find, repl, startPos, -1);
        }

        private void replaceAll(String find, String repl, int startPos, int endPos) {
            int pos = word.toString().indexOf(find, startPos);

      /*
      log("Nysiis.replaceAll(): "
        + "pos: "      + pos      + " "
        + "word: "     + word     + " "
        + "find: "     + find     + " "
        + "repl: "     + repl     + " "
        + "startPos: " + startPos + " "
        + "endPos: "   + endPos   + " "
      );
       */

            if (-1 == endPos) {
                endPos = word.length() - 1;
            }

            while (-1 != pos) {
                if (-1 != endPos && pos > endPos) {
                    log("stopping pos > endPos: " + pos + ":" + endPos);
                    break;
                }
                // log("word[" + word.length() + "]: " + word);
                // log("deleting at: " + pos + ", " + (find.length() - 1));

                word.delete(pos, pos + find.length());
                // log("del[" + word.length() + "]:  " + word);

                word.insert(pos, repl);
                // log("ins[" + word.length() + "]:  " + word);

                pos = word.toString().indexOf(find);
                // log("new pos[" + word.length() + "]: " + pos);
                log("replaceAll[" + find + "," + repl + "]: " + word);
            }

        }

        private void replaceFront(String find, String repl) {
            if (word.toString().startsWith(find)) {
                word.delete(0, find.length());
                word.insert(0, repl);
                log("replaceFront[" + find + "]: " + word);
            }
        }

        private void replaceEnd(String find, String repl) {
            if (word.toString().endsWith(find)) {
                word.delete(word.length() - find.length(), word.length());
                word.append(repl);
                log("replaceEnd[" + find + "]: " + word);
            }
        }

        private void log(String msg) {
            if (!debug) {
                return;
            }
            System.out.println(msg);
            System.out.flush();
        }
    } //Nysiis

    /**
     * A class to generate a phonex phonetic code of a string
     * @author jerome@hettich.org.uk
     */
    static class Phonex implements Phonetics {

        static public final char[] CHAR_MAPPING = "01230120022455012623010202".toCharArray();

        private int maxCodeLen = 4;

        /**
         * Find the phonex value of a String. This a hybrid of the soundex
         * and the metaphone algorithms which attemps to use the best
         * features of each.
         * Limitations: Input format is expected to be a single ASCII word
         * with only characters in the A - Z range
         * however if multiple words exist the code for ONLY the first
         * word will be returned. Punctuation and numbers are allowed as
         * they are ignored but accented characters are NOT (well they
         * will just be ignored too!)
         */
        public String encode(String txt, Resources resources) {

            if (txt == null || txt.length() == 0)
                return null;

            //convert to uppercase
            txt = txt.toUpperCase();

            //strip any remaining punctuation
            int current = 0;
            StringBuffer word = new StringBuffer(txt);
            while (current < word.length()) {
                if (!Character.isLetter(word.charAt(current)))
                    word = word.deleteCharAt(current);
                else
                    current++;
            }

            // preprocessing

            // strip any trailing S
            while (word.toString().endsWith("S"))
                word.deleteCharAt(word.length() - 1);

            // strip any initial H
            while (word.toString().startsWith("H"))
                word.deleteCharAt(0);

            // check there is a word left!
            if (word.length() == 0)
                return null;

            char[] input = word.toString().toCharArray();
            StringBuffer processed = new StringBuffer();

            // handle initial 1 and 2 characters exceptions
            switch (input[0]) {
                case 'K' :
                    if (input.length > 1 && input[1] == 'N')
                        processed.append(input, 1, input.length - 1);
                    else {
                        input[0] = 'C';
                        processed.append(input);
                    }
                    break;
                case 'P' :
                    if (input.length > 1 && input[1] == 'H') {
                        input[1] = 'F';
                        processed.append(input, 1, input.length - 1);
                    } else {
                        input[0] = 'B';
                        processed.append(input);
                    }
                    break;
                case 'W' :
                    if (input.length > 1 && input[1] == 'R')
                        processed.append(input, 1, input.length - 1);
                    else {
                        processed.append(input);
                    }
                    break;
                case 'E' :
                case 'I' :
                case 'O' :
                case 'U' :
                case 'Y' :
                    input[0] = 'A';
                    processed.append(input);
                    break;
                case 'V' :
                    input[0] = 'F';
                    processed.append(input);
                    break;
                case 'Q' :
                    input[0] = 'C';
                    processed.append(input);
                    break;
                case 'J' :
                    input[0] = 'G';
                    processed.append(input);
                    break;
                case 'Z' :
                    input[0] = 'S';
                    processed.append(input);
                    break;
                default :
                    processed.append(input);
            }

            // End of preprocessing. Find the actual code

            String processedString = processed.toString();

            StringBuffer code = new StringBuffer(maxCodeLen);
            char last, mapped;
            int incount = 1, count = 1;
            code.append(processedString.charAt(0));
            last = getCode(processedString, 0);
            while ((incount < processedString.length()) && (mapped = getCode(processedString, incount++)) != 0 && (count < maxCodeLen)) {
                if ((mapped != '0') && (mapped != last)) {
                    code.append(mapped);
                }
                last = mapped;
            }

            // padd to max code length
            while (code.length() < maxCodeLen)
                code.append('0');

            return code.toString().substring(0, maxCodeLen);
        }

        /**
         * Used internally by the Phonex algorithm.
         * returns the code for a character at a given location
         * in the given string
         */
        private char getCode(String s, int location) {
            Character a = null, b = null, c = null;

            if (location - 1 >= 0 && location - 1 < s.length())
                a = new Character(s.charAt(location - 1));

            if (location >= 0 && location < s.length())
                b = new Character(s.charAt(location));

            if (location + 1 >= 0 && location + 1 < s.length())
                c = new Character(s.charAt(location + 1));

            return getCode(a, b, c);
        }

        /**
         * Used to actually determine the code for a given character
         * (which also depends on the previous and next characters)
         */
        private char getCode(Character prev, Character c, Character next) {
            if (c == null || !Character.isLetter(c.charValue())) {
                return '0';
            } else {
                //handle exceptions
                // D or T followed by C
                if ((c.charValue() == 'D' || c.charValue() == 'T') && (next != null && next.charValue() == 'C'))
                    return '0';
                // L or R followed by vowel or end of name
                else if ((c.charValue() == 'L' || c.charValue() == 'R') && (next == null || next.charValue() == 'A' || next.charValue() == 'E' || next.charValue() == 'I' || next.charValue() == 'O' || next.charValue() == 'U'))
                    return '0';
                // D or G preceded by M or N
                else if ((c.charValue() == 'D' || c.charValue() == 'G') && (prev != null && (prev.charValue() == 'M' || prev.charValue() == 'N')))
                    return '0';
                else {
                    int loc = Character.toUpperCase(c.charValue()) - 'A';
                    if (loc < 0 || loc > (CHAR_MAPPING.length - 1))
                        return '0';
                    return CHAR_MAPPING[loc];
                }
            }
        }

        /**
         * Returns the maxCodeLen.
         */
        public int getMaxCodeLen() {
            return maxCodeLen;
        }

        /**
         * Sets the maxCodeLen.
         * @param maxCodeLen The maxCodeLen to set
         */
        public void setMaxCodeLen(int maxCodeLen) {
            this.maxCodeLen = maxCodeLen;
        }
    } //Phonex

    /**
     * The soundex implementation modified from the com.generationjava.util package
     * to cope with accented characters better
     * com.generationjava.util
     * it was origionally Licensed under the BSD license
     * see http://www.generationjava.com/licencing.shtml
     */

    public static class Soundex implements Phonetics {

        static public final char[] US_ENGLISH_SOUNDEX_MAPPING = "01230120022455012623010202".toCharArray();

        private String[] accents;

        private char[] soundexMapping;

        /** constructor */
        public Soundex() {
            this(US_ENGLISH_SOUNDEX_MAPPING);
        }

        /** constructor */
        public Soundex(char[] mapping) {
          // remember soundex mapping
          this.soundexMapping = mapping;
          // done
        }

        /**
         * Substitute an accent (if applicable) with a non-accented character
         * as specified in soundex.accents of ReportPhonetics.properties
         */
        public String substituteAccents(Resources resources, String str) {

          // have we parsed soundex accents yet?
          if (accents==null) {
            Vector buffer = new Vector(256);
            try {
              // loop over soundex accent tokens
              StringTokenizer tokens = new StringTokenizer(resources.getString("soundex.accents"));
                while (tokens.hasMoreTokens()) {
                  String token = tokens.nextToken();
                  int unicode = token.charAt(0);
                  String substitute = token.substring(1);
                  if (buffer.size()<unicode+1)
                    buffer.setSize(unicode+1);
                  buffer.set(unicode, substitute);
                }
            } catch (Throwable t) {
            }
            // now we have
            accents = (String[])buffer.toArray(new String[buffer.size()]);
          }

          // gather result
          StringBuffer result = new StringBuffer(str.length() * 2);
          for (int i = 0; i < str.length(); i++) {
              char c = str.charAt(i);
              if (c<accents.length&&accents[c]!=null)
                result.append(accents[c]);
              else
                result.append(c);
          }

          // done
          return result.toString();
        }

        /**
         * Get the SoundEx value of a string.
         * it will return the SoundEx code for the FIRST word in the string
         */
        public String encode(String s, Resources resources) {

          // safety check
          if (s == null || s.length() == 0)
              return null;

            // should get a true code for each acented character
            String str = substituteAccents(resources, s);

            // check the first letter is a character
            if (!Character.isLetter(str.charAt(0)))
                return encode(str.substring(1), resources);

            char out[] = { '0', '0', '0', '0' };
            char last, mapped;
            int incount = 1, count = 1;
            out[0] = Character.toUpperCase(str.charAt(0));
            last = getMappingCode(str.charAt(0));
            while ((incount < str.length()) && (mapped = getMappingCode(str.charAt(incount++))) != 0 && (count < 4)) {
                if ((mapped != '0') && (mapped != last)) {
                    out[count++] = mapped;
                }
                last = mapped;
            }
            return new String(out);
        }

        /**
         * Used internally by the SoundEx algorithm.
         */
        private char getMappingCode(char c) {
            if (!Character.isLetter(c)) {
                return '0';
            } else {
                int loc = Character.toUpperCase(c) - 'A';
                if (loc < 0 || loc > (soundexMapping.length - 1))
                    return '0';
                return soundexMapping[loc];
            }
        }
    } //Soundex

} //ReportSoundex
