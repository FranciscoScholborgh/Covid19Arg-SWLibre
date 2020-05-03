/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logica;

import java.util.ArrayList;
import weka.classifiers.functions.LinearRegression;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Lal
 */

public class ProcesaAlgoritmo {
    
    private final Instances dataset;
        
    public ProcesaAlgoritmo(int[] x, int[] y){
        FastVector vector = new FastVector(2);
        vector.addElement(new Attribute("x"));
        vector.addElement(new Attribute("y"));
        dataset = new Instances("dataset", vector, x.length);
        
        for (int i = 0; i < x.length; i++) {
            Instance instance = new Instance(2);
            instance.setValue((Attribute) vector.elementAt(0), x[i]);
            instance.setValue((Attribute) vector.elementAt(1), y[i]);
            dataset.add(instance);
        }
    }
    
    public String regresionLineal() throws Exception{
        dataset.setClassIndex(dataset.numAttributes() - 1); //elegir la columna clase
        
        LinearRegression lire = new LinearRegression();
        lire.buildClassifier(dataset);
        //System.out.println(lire);
        //System.out.println(Arrays.toString(lr.coefficients()));
        
        double cf[] = lire.coefficients();
        //System.out.println("Coeficiente x "+c[0]);
        //System.out.println("Intercepto "+c[2]);
        
        String ecuation = "(" + String.format("%.3f", cf[0]) + ") * X + (" + String.format("%.3f", cf[2]) + ")";
        return ecuation.replace(",", ".");
       
    }
    
    public ArrayList<String> clusteringKMeans(int num) throws Exception{
        dataset.setClassIndex(-1); //numero negativo indica que no hay clase
        
        SimpleKMeans skm=new SimpleKMeans();
        skm.setNumClusters(num);
        skm.buildClusterer(dataset); 
        System.out.println("Modelo: " + skm);
        
        System.out.println("Centroides ");
        Instances cents = skm.getClusterCentroids();
        ArrayList<String> centroids = new ArrayList<>();
        for (int i = 0; i < cents.numInstances(); i++) {
            Instance ins = cents.instance(i);
            System.out.println("Punto (X, Y): (" + ins.toString(0) + ", " + ins.toString(1) + ")");
            centroids.add(ins.toString(0) + ", " + ins.toString(1));
        }
        return centroids;
    }
    
}
