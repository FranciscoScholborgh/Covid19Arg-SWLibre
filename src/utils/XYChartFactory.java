/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Frank_000
 */
public class XYChartFactory {
    
    public static XYChart createXYLineChart(String title, String x_name, String y_name) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYLineChart lineChart = new XYLineChart( title,  x_name,  y_name, dataset);
        return lineChart;
    }
    
    public static XYChart createXYScatterChart(String title, String x_name, String y_name) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYScatterChart scatterChart = new XYScatterChart(title, x_name, y_name, dataset);
        return scatterChart;
    }
}
