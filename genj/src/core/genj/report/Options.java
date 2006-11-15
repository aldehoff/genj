/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Revision: 1.14 $ $Author: daniel-andre $ $Date: 2006-11-15 20:33:49 $
 */
package genj.report;

import genj.gedcom.PrivacyPolicy;
import genj.option.OptionProvider;
import genj.option.PropertyOption;

import java.util.List;

/**
 * Options for report package
 */
public class Options extends OptionProvider {
    
    /** 'singleton' instance */
    private static Options instance = new Options();
    
    /** Positions after decimal point */
    private int positions = 2;
    
    /** indent per level in reports */
    private int indentPerLevel = 5;
    
    /** birth symbol in reports */
    private String birthSymbol = "*";
    
    /** baptism symbol in reports */
    private String baptismSymbol =  "~";
    
    /** engagement symbol in reports */
    private String engagingSymbol = "o";
    
    /** marriage symbol in reports */
    private String marriageSymbol = "oo";
    
    /** divorce symbol in reports */
    private String divorceSymbol = "o|o";
    
    /** death symbol in reports */
    private String deathSymbol = "+";
    
    /** burial symbol in reports */
    private String burialSymbol = "[]";
    
    /** occupation symbol in reports */
    private String  occuSymbol = "=";

    /** residence symbol in reports */
    private String  resiSymbol = "^";
    
    /** child of symbol in reports */
    private String childOfSymbol = "/";
    
    /** tag marking private */
    public  String privateTag = "_PRIV";
    
    /** whether information pertaining to deceased people is public */
    public boolean deceasedIsPublic = true;
    
    /** number of years an event is private */
    public int yearsEventsArePrivate = 0; 
    
    public int getIndentPerLevel() {
        return indentPerLevel;
    }
    
    public void setIndentPerLevel(int set) {
        indentPerLevel = Math.max(2,set);
    }
    
    public int getPositions() {
        return positions;
    }
    
    public void setPositions(int set) {
        positions = Math.max(0,set);
    }
    
    public String getBirthSymbol() {
        return birthSymbol;
    }
    
    public void setBirthSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            birthSymbol = set;
        else
            birthSymbol = "*";
    }
    
    public String getBaptismSymbol() {
        return baptismSymbol;
    }
    
    public void setBaptismSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            baptismSymbol = set;
        else
            baptismSymbol = "~";
    }
    
    public String getEngagingSymbol() {
        return engagingSymbol;
    }
    
    public void setEngagingSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            engagingSymbol = set;
        else
            engagingSymbol = "o";
    }
    
    public String getMarriageSymbol() {
        return marriageSymbol;
    }
    
    public void setMarriageSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            marriageSymbol = set;
        else
            marriageSymbol = "oo";
    }
    
    public String getDivorceSymbol() {
        return divorceSymbol;
    }
    
    public void setDivorceSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            divorceSymbol = set;
        else
            divorceSymbol = "o|o";
    }
    
    public String getDeathSymbol() {
        return deathSymbol;
    }
    
    public void setDeathSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            deathSymbol = set;
        else
            deathSymbol = "+";
    }
    
    public String getBurialSymbol() {
        return burialSymbol;
    }
    
    public void setBurialSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            burialSymbol = set;
        else
            burialSymbol = "[]";
    }
    
    public String getOccuSymbol() {
		return occuSymbol;
	}

	public void setOccuSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
        	occuSymbol = set;
        else
        	occuSymbol = "=";
	}

	public String getResiSymbol() {
		return resiSymbol;
	}

	public void setResiSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
        	resiSymbol = set;
        else
        	resiSymbol = "^";
	}

	public String getChildOfSymbol() {
        return childOfSymbol;
    }
    
    public void setChildOfSymbol(String set) {
        if (set!=null&&set.trim().length()>0)
            childOfSymbol = set;
        else
            childOfSymbol = "/";
    }
    
    /**
     * accessor - PrivacyPolicy configured by user
     */
    public PrivacyPolicy getPrivacyPolicy() {
      return new PrivacyPolicy(deceasedIsPublic, yearsEventsArePrivate, privateTag);    
    }
    
    /**
     * callback - provide options during system init
     */
    public List getOptions() {
      // load report async
      new Thread(new Runnable() {
        public void run() {
          ReportLoader.getInstance();
        }
      }).start();
      
      // introspect for options
      return PropertyOption.introspect(getInstance());
    }
    
    /**
     * accessor - singleton instance
     */
    public static Options getInstance() {
        return instance;
    }
    
} //Options
