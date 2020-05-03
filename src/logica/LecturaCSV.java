/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logica;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
/**
 *
 * @author 
 */

public class LecturaCSV {
    
    private String dirCSV;
    private ArrayList<String> dias, muertes, confirmados;
    private int[] diasN, muertesN, confirmadosN;
    
    public LecturaCSV(String path){
        dirCSV = path;
        
        dias = new ArrayList<>();
        muertes = new ArrayList<>();
        confirmados = new ArrayList<>();
    }
    
    public void procesarCSV() throws FileNotFoundException{
        File archivoCSV = new File(dirCSV);
        
        Scanner lector = new Scanner(archivoCSV);
        String linea;
        String[] lineaSeparada;
        while(lector.hasNext()){
            linea = lector.nextLine();
            lineaSeparada = linea.split(",");
            dias.add(lineaSeparada[0]);
            confirmados.add(lineaSeparada[1]);
            muertes.add(lineaSeparada[2]);            
        }
        lector.close();
        
        dias.remove(0);
        confirmados.remove(0);
        muertes.remove(0);
        
        diasN = new int [dias.size()];
        muertesN = new int [muertes.size()];
        confirmadosN = new int[confirmados.size()];
        
        for (int i = 0; i < dias.size(); i++) {
            diasN[i] = Integer.parseInt(dias.get(i));
            muertesN[i] = Integer.parseInt(muertes.get(i));
            confirmadosN[i] = Integer.parseInt(confirmados.get(i));
        }     
        
    }

    public int[] getDiasN() {
        return diasN;
    }

    public int[] getMuertesN() {
        return muertesN;
    }

    public int[] getConfirmadosN() {
        return confirmadosN;
    }
    
    
    
}
