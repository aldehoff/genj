/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.report.Report;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;

/**
 * A report showing age distribution for males/females
 */
public class ReportDemography extends Report {

  /**
   * n/a
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * main
   */
  public void start(Object context) {
    
    // assume gedcom
    Gedcom gedcom = (Gedcom)context;
    
    // gather data
    Demography d = new Demography();
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) 
      d.add((Indi)indis.next());
    
    // show it
    showComponentToUser(new ChartPanel(getChart(d, gedcom.getName())));
      
    // done
  }

  /**
   * create a chart to show
   */
  private JFreeChart getChart(Demography d, String ged) {
    
    CategoryAxis categoryAxis = new CategoryAxis();
    NumberAxis valueAxis = new NumberAxis();
    valueAxis.setNumberFormatOverride(new DecimalFormat("#; #"));
    StackedBarRenderer renderer = new StackedBarRenderer();
    renderer.setSeriesPaint(0, Color.BLUE);
    renderer.setSeriesPaint(1, Color.RED);
    CategoryPlot plot = new CategoryPlot(d, categoryAxis, valueAxis, renderer);
    plot.setOrientation(PlotOrientation.HORIZONTAL);
    return new JFreeChart("Demographic Pyramid for "+ged, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
  }
  
  /** 
   * the demographic data
   */
  private static class Demography extends AbstractDataset implements CategoryDataset {

    private final String[] rows = { "males", "females"};
    
    private final String[] cols = { "100+", "90-100", "80-90", "70-80", "60-70", "50-60", "40-50", "30-40", "20-30", "10-20", "0-10" };
    
    private int[][] values = new int[2][11];
    
    /** add individuals' demographic factor */
    void add(Indi indi) {
      
      PropertyDate birth = indi.getBirthDate();
      PropertyDate death = indi.getDeathDate();
      if (birth==null||death==null)
        return;
      
      Delta delta = Delta.get(birth.getStart(), death.getStart());
      if (delta==null||delta.getYears()<0)
        return;
      
      int col = delta.getYears()>=100 ? 10 : delta.getYears()/10;
          
      int row = indi.getSex() == PropertySex.MALE ? 0 : 1;
      values[row][col] ++;
      
    }
    
    /** row for index */
    public Comparable getRowKey(int pos) {
      return rows[pos];
    }

    /** index for row */
    public int getRowIndex(Comparable row) {
      for (int i=0; i<rows.length; i++) 
        if (rows[i].equals(row)) return i;
      throw new IllegalArgumentException();
    }

    /** all row keys  */
    public List getRowKeys() {
      return Arrays.asList(rows);
    }

    /** column for index */
    public Comparable getColumnKey(int pos) {
      return cols[pos];
    }

    /** index for column */
    public int getColumnIndex(Comparable col) {
      for (int i=0; i<cols.length; i++) 
        if (cols[i].equals(cols)) return i;
      throw new IllegalArgumentException();
    }

    /** all columns */
    public List getColumnKeys() {
      return Arrays.asList(cols);
    }

    /** value for col, row */
    public Number getValue(Comparable row, Comparable col) {
      return getValue(getRowIndex(row), getColumnIndex(col));
    }

    /** value for col, row */
    public Number getValue(int row, int col) {
      int result = values[row][10-col];
      return new Integer(row==1 ? result : -result);
    }
    
    /** #rows */
    public int getRowCount() {
      return rows.length;
    }

    /** #cols */
    public int getColumnCount() {
      return cols.length;
    }
    
  }

} //ReportLifeExpectancy
