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
 */
package genj.gedcom;

import genj.util.WordBuffer;

import java.util.StringTokenizer;

/**
 * Gedcom Property : AGE
 */
public class PropertyAge extends Property {
    
    public final static String TAG = "AGE";
    
    /** the age */
    private int years = 0, months = 0, days = 0;
    
    /** as string */
    private String ageAsString;
    
    /** localizations */
    private final static String
    YEAR  = Gedcom.resources.getString("time.year"  ),
    YEARS = Gedcom.resources.getString("time.years" ),
    MONTH = Gedcom.resources.getString("time.month" ),
    MONTHS= Gedcom.resources.getString("time.months"),
    DAY   = Gedcom.resources.getString("time.day"   ),
    DAYS  = Gedcom.resources.getString("time.days"  );
    
    /**
     * Returns <b>true</b> if this property is valid
     */
    public boolean isValid() {
        return ageAsString==null;
    }
    
    /**
     * @see genj.gedcom.Property#addNotify(genj.gedcom.Property)
     */
    void addNotify(Property parent) {
        // continue
        super.addNotify(parent);
        // try to update age
        updateAge();
        // done
    }
    
    /**
     * Accessor Tag
     */
    public String getTag() {
        return TAG;
    }
    
    /**
     * @see genj.gedcom.Property#setTag(java.lang.String)
     */
    Property init(String tag, String value) throws GedcomException {
        assume(TAG.equals(tag), UNSUPPORTED_TAG);
        return super.init(tag,value);
    }
    
    /**
     * Accessor Value
     */
    public String getValue() {
        if (ageAsString!=null)
            return ageAsString;
        return getAgeString(years,months,days, true, true);
    }
    
    /**
     * Accessor Value
     */
    public void setValue(String newValue) {
        // try to parse
        if (parseAgeString(newValue))
            ageAsString = null;
        else
            ageAsString = newValue;
        // notify
        modNotify();
        // Done
    }
    
    /**
     * Parse Age String
     */
    private boolean parseAgeString(String s) {
        
        days=0;months=0;years=0;
        
        // try to parse delta string tokens
        StringTokenizer tokens = new StringTokenizer(s);
        while (tokens.hasMoreTokens()) {
            
            String token = tokens.nextToken();
            int len = token.length();
            
            // check 1234x
            if (len<2) return false;
            for (int i=0;i<len-1;i++) {
                if (!Character.isDigit(token.charAt(i))) return false;
            }
            // check last
            switch (token.charAt(len-1)) {
                case 'y' : years = s2i(token, 0, token.length()-1); break;
                case 'm' : months= s2i(token, 0, token.length()-1); break;
                case 'd' : days  = s2i(token, 0, token.length()-1); break;
                default  : return false;
            }
        }
        
        // parsed!
        return years>=0&&months>=0&&days>=0&&(years+months+days>0);
        
    }
    
    /**
     * Calculate int from string
     */
    private int s2i(String str, int start, int end) {
        try {
            return Integer.parseInt(str.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Update the age
     */
    public boolean updateAge() {
        
        // calc delta
        int[] delta = PointInTime.getDelta(getEarlier(), getLater());
        if (delta==null)
            return false;
        
        years = delta[0];
        months = delta[1];
        days = delta[2];
        
        ageAsString = null;
        
        // done
        return true;
    }
    
    /**
     * @see genj.gedcom.Property#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        // no age or only string values?
        if (!(o instanceof PropertyAge))
            return super.compareTo(o);
        PropertyAge other = (PropertyAge)o;
        // ages available?
        if (ageAsString!=null||other.ageAsString!=null)
            return super.compareTo(other);
        // compare years
        int delta = years - other.years;
        if (delta!=0) return delta;
        // .. months
        delta = months - other.months;
        if (delta!=0) return delta;
        // .. days
        delta = days - other.days;
        return delta;
    }
    
    /**
     * @see genj.gedcom.Property#getProxy()
     */
    public String getProxy() {
        return "Age";
    }
    
    /**
     * Calculates earlier point in time (the birth)
     *
     *  INDI:EVENT:AGE -> INDI:BIRT:DATE
     *
     *  FAM:MARR:HUSB:AGE -> FAM:HUSB -> INDI:BIRT:DATE
     *
     *  FAM:MARR:WIFE:AGE -> FAM:WIFE -> INDI:BIRT:DATE
     */
    public PointInTime getEarlier() {
        Entity e = getEntity();
        // might FAM:MARR:WIFE|HUSB:AGE
        if (e instanceof Fam) {
            Property parent = getParent();
            if (parent.getTag().equals(PropertyHusband.TAG))
                e = ((Fam)e).getHusband();
            if (parent.getTag().equals(PropertyWife.TAG))
                e = ((Fam)e).getWife();
        }
        // check individual?
        if (!(e instanceof Indi)) return null;
        // date
        PropertyDate birth = ((Indi)e).getBirthDate();
        return birth!=null ? birth.getStart() : null;
    }
    
    /**
     * Calculates later point in time (the event)
     *
     * INDI:EVENT:AGE -> INDI:EVENT:DATE
     *
     * FAM:EVENT:HUSB:AGE -> FAM:EVENT:DATE
     *
     * FAM:EVENT:WIFE:AGE -> FAM:EVENT:DATE
     *
     */
    public PointInTime getLater() {
        Property parent = getParent();
        // might FAM:MARR:WIFE|HUSB:AGE
        if (parent.getTag().equals(PropertyHusband.TAG)||parent.getTag().equals(PropertyWife.TAG)) {
            // one more up
            parent = parent.getParent();
        }
        // check event
        if (!(parent instanceof PropertyEvent))
            return null;
        PropertyDate date = ((PropertyEvent)parent).getDate();
        // start of date
        return date!=null ? date.getStart() : null;
    }
    
    /**
     * Calculate an age string "99y 9m 9d"
     */
    public static String getAgeString(PointInTime earlier, PointInTime later, boolean localize, boolean shortWriting) {
        
        // try to calc delta
        int[] delta = PointInTime.getDelta(earlier,later);
        if (delta==null)
            return "";
        
        // convert into string
        return getAgeString(delta[0], delta[1], delta[2], localize, shortWriting);
    }
    
    /**
     * Calculate Age String
     */
    public static String getAgeString(int y, int m, int d, boolean localize, boolean shortWriting) {
        
        // calculate output
        WordBuffer buffer = new WordBuffer();
        if (!localize) {
            if (y>0) buffer.append(y+"y");
            if (m>0) buffer.append(m+"m");
            if (d>0) buffer.append(d+"d");
        } else {
            if (y==0&&m==0&&d==0) {
                if(shortWriting)
                    return "<1 "+DAY.substring(0,1).toLowerCase();
                else
                    return "<1 "+DAY;
            }
            if (y>0) {
                buffer.append(""+y);
                if(shortWriting)
                    buffer.append(y==1?YEAR.substring(0,1).toLowerCase() :YEARS.substring(0,1).toLowerCase() );
                else
                    buffer.append(y==1?YEAR :YEARS );
            }
            if (m>0) {
                if(shortWriting)
                    buffer.append(""+m).append(m==1?MONTH.substring(0,1).toLowerCase():MONTHS.substring(0,1).toLowerCase());
                else
                    buffer.append(""+m).append(m==1?MONTH:MONTHS);
            }
            if (d>0) {
                if(shortWriting)
                    buffer.append(""+d).append(d==1?DAY.substring(0,1).toLowerCase()  :DAYS.substring(0,1).toLowerCase()  );
                else
                    buffer.append(""+d).append(d==1?DAY  :DAYS  );
            }
        }
        
        // done
        return buffer.toString();
    }
    
} //PropertyAge
