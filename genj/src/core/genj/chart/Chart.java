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
 * $Revision: 1.4 $ $Author: nmeier $ $Date: 2004-12-14 18:27:33 $
 */
package genj.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * A chart component wrapping JFreeChart
 */
public class Chart extends JPanel {
  
  /**
   * Initializer
   */
  private void init(String title, Plot plot, boolean legend) {
    setLayout(new BorderLayout());
    
    add(new ChartPanel(new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend)), BorderLayout.CENTER);
  }
  
  /**
   * Constructor
   */
  public Chart(String title, String labelAxisX, String labelAxisY, IndexedSeries[] series, NumberFormat format, boolean stacked) {

    // prepare chart setup
    NumberAxis xAxis = new NumberAxis(labelAxisX);
    xAxis.setAutoRangeIncludesZero(false);
    
    NumberAxis yAxis = new NumberAxis(labelAxisY);
    yAxis.setNumberFormatOverride(format);
    
    XYItemRenderer renderer = stacked ? new StackedXYAreaRenderer() : new XYAreaRenderer();
    XYPlot plot = new XYPlot(IndexedSeries.asTableXYDataset(series), xAxis, yAxis, renderer);

    // init
    init(title, plot, true);
    
    // done
  }
  
  /**
   * Constructor
   */
  public Chart(String title, String labelAxisX, String labelAxisY, XYSeries[] series, NumberFormat format, boolean shapes) {
    
    // prepare chart setup
    NumberAxis xAxis = new NumberAxis(labelAxisX);
    xAxis.setAutoRangeIncludesZero(false);
    
    NumberAxis yAxis = new NumberAxis(labelAxisY);
    yAxis.setNumberFormatOverride(format);
    
    XYItemRenderer renderer = new StandardXYItemRenderer(shapes ? StandardXYItemRenderer.SHAPES_AND_LINES : StandardXYItemRenderer.LINES);
    
    XYPlot plot = new XYPlot(XYSeries.toXYDataset(series), xAxis, yAxis, renderer);

    // init
    init(title, plot, true);
    
    // done
  }
  
  /**
   * Constructor - a pie chart
   */
  public Chart(String title, IndexedSeries series, String[] categories, boolean legend) {
    
    PiePlot plot = new PiePlot(IndexedSeries.asPieDataset(series, categories));
    plot.setLabelGenerator(new StandardPieItemLabelGenerator());
    plot.setInsets(new Insets(0, 5, 5, 5));
    
    init(title, plot, legend);
    
  }
  
  /**
   * Constructor 
   */
  public Chart(String title, String labelCatAxis, IndexedSeries[] series, String[] categories, NumberFormat format, boolean isStacked, boolean isVertical) {

    // wrap into JFreeChart
    CategoryAxis categoryAxis = new CategoryAxis(labelCatAxis);
    NumberAxis valueAxis = new NumberAxis();
    valueAxis.setNumberFormatOverride(format);

    BarRenderer renderer;
    if (isStacked) {
      renderer = new StackedBarRenderer();
    } else {
      renderer = new BarRenderer();
    }
    
    // TODO parameterize colors
    renderer.setSeriesPaint(0, Color.BLUE);
    renderer.setSeriesPaint(1, Color.RED);
    
    // prepare plot
    CategoryPlot plot = new CategoryPlot(IndexedSeries.asCategoryDataset(series, categories), categoryAxis, valueAxis, renderer);
    plot.setOrientation(!isVertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);

    // init
    init(title, plot, true);
    
    // done
  }

} //Chart
