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
 * $Revision: 1.1 $ $Author: nmeier $ $Date: 2004-12-09 03:38:18 $
 */
package genj.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
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
  private void init(String title, Plot plot) {
    setLayout(new BorderLayout());
    
    add(new ChartPanel(new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true)), BorderLayout.CENTER);
  }
  
  /**
   * Constructor
   */
  public Chart(String title, String labelAxisX, String labelAxisY, IndexedSeries[] series, final int rangeStart, int rangeEnd, NumberFormat format, boolean stacked) {

    // prepare chart setup
    NumberAxis xAxis = new NumberAxis(labelAxisX);
    xAxis.setAutoRangeIncludesZero(false);
    
    NumberAxis yAxis = new NumberAxis(labelAxisY);
    yAxis.setNumberFormatOverride(format);
    
    XYItemRenderer renderer = stacked ? new StackedXYAreaRenderer() : new XYAreaRenderer();
    XYPlot plot = new XYPlot(IndexedSeries.asTableXYDataset(series, rangeStart, rangeEnd), xAxis, yAxis, renderer);

    // init
    init(title, plot);
    
    // done
  }
  
  /**
   * Constructor
   */
  public Chart(String title, String labelAxisX, String labelAxisY, XYSeries[] series, NumberFormat format) {
    
    // prepare chart setup
    NumberAxis xAxis = new NumberAxis(labelAxisX);
    xAxis.setAutoRangeIncludesZero(false);
    
    NumberAxis yAxis = new NumberAxis(labelAxisY);
    yAxis.setNumberFormatOverride(format);
    
    XYItemRenderer renderer = new StandardXYItemRenderer();
    
    XYPlot plot = new XYPlot(XYSeries.toXYDataset(series), xAxis, yAxis, renderer);

    // init
    init(title, plot);
    
    // done
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
    init(title, plot);
    
    // done
  }

} //Chart
