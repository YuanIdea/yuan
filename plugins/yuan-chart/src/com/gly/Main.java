package com.gly;

public class Main {
    public static void main(String[] args) {
        // Example 1: Draw a single curve, automatically generate X-axis (1,2,3,...)
        double[] y1 = {2.3, 4.5, 3.1, 5.6, 7.8, 6.4};
        Plot plot1 = new Plot();
        plot1.setTitle("Single Curve Example");
        plot1.setLabelX("Index");
        plot1.setLabelY("Value");
        plot1.plot(y1);  // Automatically generate X = 1,2,3,...

        // Wait a moment for the first window to display (non-blocking, but simple pause for demonstration)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        // Example 2: Draw multiple curves with custom X and styles
        double[] x2 = {0.0, 1.5, 2.0, 3.5, 4.0, 5.0};
        double[] y2a = {1.0, 2.5, 3.0, 4.2, 5.1, 6.8};
        double[] y2b = {0.5, 1.8, 2.2, 3.9, 4.7, 5.5};

        Plot plot2 = new Plot();
        plot2.setTitle("Multiple Curves Comparison");
        plot2.setLabelX("Time (s)");
        plot2.setLabelY("Amplitude");
        plot2.setLegend(new String[]{"Curve A", "Curve B"});  // Set legend
        plot2.setLineWidth(new float[]{2.0f, 1.5f});       // Line widths
        plot2.setStyle(new String[]{"-", "--"});           // Line styles: solid, dashed

        // Pass multiple data sets (each data set is a 2D array [x[], y[]])
        double[][] data1 = {x2, y2a};
        double[][] data2 = {x2, y2b};
        plot2.plot(data1, data2);   // Draw two curves

        System.out.println("All charts have been displayed, please check the windows.");
    }
}
