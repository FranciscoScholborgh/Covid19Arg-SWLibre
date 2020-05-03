/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package covid19arg.swlibre;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import logica.LecturaCSV;
import logica.ProcesaAlgoritmo;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jfree.chart.ChartPanel;
import utils.XYChart;
import utils.XYChartFactory;

/**
 *
 * @author frank
 */
public class Main extends JFrame {
    
    private XYChart lineChart;
    private XYChart scatterChart;
    private Expression regresionCasos;
    private Expression regresionMuertes;
    private JFrame licenseFrame = new Licencia();
    private JFrame ecuFrame;
    
    private int[] evaluateRegretion (Expression evaluator, int[] toPredict) {
        int predicted[] = new int[toPredict.length];
        for (int i = 0; i < toPredict.length; i++) {
            evaluator.setVariable("X", toPredict[i]);
            predicted[i] = (int) evaluator.evaluate();
        }
        return predicted;
    }
    
    private void evaluate() {
        try {
            Integer value = Integer.parseInt(testing.getText());
            if(value > 0) {
                String selected = (String) data_predition.getSelectedItem();
                Expression evaluator;
                if(selected.equals("Casos")) {
                    evaluator = regresionCasos;
                } else {
                    evaluator = regresionMuertes;
                }
                evaluator.setVariable("X", value);
                int predicted = (int) evaluator.evaluate();
                prediction.setText("El numero de " + selected + " para el dia " + value + " se proyecta en: " + predicted + " personas");
            }   
        } catch (NumberFormatException nfe) {
            prediction.setText("");
        }  
    }

    /**
     * Creates new form Main
     */
    public Main() {
        try {
            initComponents();
            testing.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent  e) {
                    char input = e.getKeyChar();
                    if((input < '0' || input> '9') && input != '\b') {
                        e.consume();
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            evaluate();
                        });
                    }
                }
            });
            
            data_predition.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent event) {
                    if (event.getStateChange() == ItemEvent.SELECTED) {
                        evaluate();
                    }
                }
            });

            this.lineChart= XYChartFactory.createXYLineChart("Covid-19 Argentina", "Días", "Personas");
            ChartPanel chartPanel = this.lineChart.getPanel();
            chartPanel.setPreferredSize(this.graph.getPreferredSize());
            this.graph.removeAll();
            this.graph.setLayout(new BorderLayout());
            this.graph.add(chartPanel, BorderLayout.CENTER);
            this.graph.validate();
            
            this.scatterChart = XYChartFactory.createXYScatterChart("Covid-19 Argentina (Centroides)", "Días", "Personas");
            ChartPanel chartPanel2 = this.scatterChart.getPanel();
            chartPanel2.setPreferredSize(this.cent_graph.getPreferredSize());
            this.cent_graph.removeAll();
            this.cent_graph.setLayout(new BorderLayout());
            this.cent_graph.add(chartPanel2, BorderLayout.CENTER);
            this.cent_graph.validate();
            
            LecturaCSV csvReader = new LecturaCSV("Covid-19-Argentina.csv");
            csvReader.procesarCSV();
            int[] dias = csvReader.getDiasN();
            int[] casos = csvReader.getConfirmadosN();
            int[] muertes = csvReader.getMuertesN();
            this.lineChart.prepareSerie("Casos(Datos reales)", Color.BLACK, dias, casos);
            this.lineChart.prepareSerie("Muertes(Datos reales)", Color.BLUE ,dias, muertes);
            
            ProcesaAlgoritmo dvCasos = new ProcesaAlgoritmo(dias, casos);
            String regCasos =  dvCasos.regresionLineal();
            this.regresionCasos = new ExpressionBuilder(regCasos)
                    .variables("X")
                    .build();
            int[] regEvalCasos = evaluateRegretion(regresionCasos, dias);
            this.lineChart.prepareSerie("Casos(Reregesión)", Color.MAGENTA, dias, regEvalCasos);
            
            ArrayList<String> centCasos = dvCasos.clusteringKMeans(3);
            int[] xcCasos = new int[centCasos.size()];
            int[] ycCasos = new int[centCasos.size()];
            int x = 0;
            for (String centCaso : centCasos) {
                String[] lineaSeparada = centCaso.split(",");
                xcCasos[x] = (int) Double.valueOf(lineaSeparada[0]).doubleValue();
                ycCasos[x++] = (int) Double.valueOf(lineaSeparada[1]).doubleValue();
            }
            this.scatterChart.prepareSerie("Centroides casos", Color.BLACK, xcCasos, ycCasos);
            
            ProcesaAlgoritmo dvMuertes = new ProcesaAlgoritmo(dias, muertes);
            String regMuertes =  dvMuertes.regresionLineal();
            this.regresionMuertes = new ExpressionBuilder(regMuertes)
                    .variables("X")
                    .build();
            int[] regEvalMuertes = evaluateRegretion(regresionMuertes, dias);
            this.lineChart.prepareSerie("Muertes(Reregesión)", null,  dias, regEvalMuertes);
            
            ArrayList<String> centMuertes = dvMuertes.clusteringKMeans(3);
            int[] xcMuertes = new int[centMuertes.size()];
            int[] ycMuertes = new int[centMuertes.size()];
            x = 0;
            for (String centMuerte : centMuertes) {
                String[] lineaSeparada = centMuerte.split(",");
                xcMuertes[x] = (int) Double.valueOf(lineaSeparada[0]).doubleValue();
                ycMuertes[x++] = (int) Double.valueOf(lineaSeparada[1]).doubleValue();
            }
            this.scatterChart.prepareSerie("Centroides muertes", null, xcMuertes, ycMuertes);
            
            this.scatterChart.prepareSerie("Casos confirmados", Color.MAGENTA, dias, casos);
            this.scatterChart.prepareSerie("Muertes", null , dias, muertes);

            lineChart.enable_seriesHidding();
            scatterChart.enable_seriesHidding();
            ecuFrame = new Ecuaciones(regCasos, regMuertes);
            
            this.flag_icon.setToolTipText("Icon made by Roundicons from www.flaticon.com");
            this.license_icon.setToolTipText("Icon made by Freepik from www.flaticon.com");
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flag_icon = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        info_panel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        data_predition = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        prediction = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        testing = new javax.swing.JTextField();
        verRegEC = new javax.swing.JButton();
        license_icon = new javax.swing.JLabel();
        Regres = new javax.swing.JTabbedPane();
        graph = new javax.swing.JPanel();
        cenpred = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cent_graph = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        flag_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        flag_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arg_flag.png"))); // NOI18N

        title.setFont(new java.awt.Font("Garamond", 1, 24)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("COVID-19 EN ARGENTINA");
        title.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        info_panel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(0, 0, 0), new java.awt.Color(0, 0, 0), null));
        info_panel.setMaximumSize(new java.awt.Dimension(32767, 100));
        info_panel.setMinimumSize(new java.awt.Dimension(0, 100));

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        jLabel1.setText("Información");

        data_predition.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Casos", "Muertes" }));

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        jLabel2.setText("Dia");

        prediction.setEditable(false);

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        jLabel3.setText("Proyección");

        verRegEC.setText("Ecuaciones");
        verRegEC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verRegECActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout info_panelLayout = new javax.swing.GroupLayout(info_panel);
        info_panel.setLayout(info_panelLayout);
        info_panelLayout.setHorizontalGroup(
            info_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(info_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(data_predition, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testing, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(prediction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(verRegEC, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        info_panelLayout.setVerticalGroup(
            info_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(info_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(info_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(data_predition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(prediction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(testing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(verRegEC))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        license_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        license_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/certificado.png"))); // NOI18N
        license_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                license_iconMouseClicked(evt);
            }
        });

        graph.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout graphLayout = new javax.swing.GroupLayout(graph);
        graph.setLayout(graphLayout);
        graphLayout.setHorizontalGroup(
            graphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        graphLayout.setVerticalGroup(
            graphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 236, Short.MAX_VALUE)
        );

        Regres.addTab("Grafica", graph);

        jLabel4.setText("Centroides Casos:");

        jLabel6.setText("Centroides muertes");

        javax.swing.GroupLayout cent_graphLayout = new javax.swing.GroupLayout(cent_graph);
        cent_graph.setLayout(cent_graphLayout);
        cent_graphLayout.setHorizontalGroup(
            cent_graphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        cent_graphLayout.setVerticalGroup(
            cent_graphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout cenpredLayout = new javax.swing.GroupLayout(cenpred);
        cenpred.setLayout(cenpredLayout);
        cenpredLayout.setHorizontalGroup(
            cenpredLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cenpredLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cenpredLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cenpredLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                        .addGap(112, 112, 112))
                    .addComponent(cent_graph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        cenpredLayout.setVerticalGroup(
            cenpredLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cenpredLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cenpredLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cent_graph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Regres.addTab("Centroide", cenpred);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(flag_icon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(license_icon))
                    .addComponent(info_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Regres))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flag_icon)
                    .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(license_icon, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(info_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Regres)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void license_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_license_iconMouseClicked
        // TODO add your handling code here:
        licenseFrame.setVisible(true);
    }//GEN-LAST:event_license_iconMouseClicked

    private void verRegECActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verRegECActionPerformed
        // TODO add your handling code here:
        ecuFrame.setVisible(true);
    }//GEN-LAST:event_verRegECActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                 try{
  
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                }
                catch (Exception e)
                 {
                  e.printStackTrace();
                 }
                 
                 
                 
                new Main().setVisible(true);
               
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane Regres;
    private javax.swing.JPanel cenpred;
    private javax.swing.JPanel cent_graph;
    private javax.swing.JComboBox<String> data_predition;
    private javax.swing.JLabel flag_icon;
    private javax.swing.JPanel graph;
    private javax.swing.JPanel info_panel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel license_icon;
    private javax.swing.JTextField prediction;
    private javax.swing.JTextField testing;
    private javax.swing.JLabel title;
    private javax.swing.JButton verRegEC;
    // End of variables declaration//GEN-END:variables
}
