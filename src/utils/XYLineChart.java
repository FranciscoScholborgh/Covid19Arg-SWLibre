/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Frank_000
 */
public class XYLineChart extends XYChart{
    
    public XYLineChart(String title, String x_name, String y_name, XYSeriesCollection dataset) {
        super(ChartFactory.createXYLineChart(title, x_name, y_name, dataset));
    }
    
}
