package com.gly;

import com.gly.event.DoneEvent;
import com.gly.event.GlobalBus;
import com.gly.io.csv.Reader;
import com.gly.log.Logger;
import com.gly.model.BaseExecutable;
import com.gly.util.ArrayUtils;
import com.gly.util.JsonUtil;
import com.gly.util.PathUtil;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Chart extends BaseExecutable {
    public void start() {
        String root = getRoot();
        String name = getName();

        Config config = JsonUtil.load(name, Config.class);
        if (config == null) {
            Logger.error("configuration loading failed");
            return;
        }

        // Load raw data
        double[][] rawData = loadData(root, config);
        if (rawData == null) {
            Logger.error("Data loading failed, error already logged");
            return;
        }

        // Transpose data (assume each column is a curve)
        double[][] transposedData = ArrayUtils.transpose(rawData);
        if (transposedData == null || transposedData.length == 0) {
            Logger.error("No valid data, return early");
            return;
        }

        // Create and display the plot
        Plot plot = createPlot(config, transposedData);
        plot.getFrame().addWindowListener(closeHandler());
    }

    /**
     * Load data from file according to configuration.
     *
     * @param root   The root directory of the project.
     * @param config Information configuration file.
     */
    private double[][] loadData(String root, Config config) {
        String inputPath = PathUtil.resolveAbsolutePath(root, config.getInputPathName());
        int[] rangeRows = config.getRangeRows();
        int[] columns = config.getInputIndex();

        try {
            if (rangeRows == null) {
                return Reader.readToDoubleArray2(inputPath, 1, columns);
            } else {
                return Reader.readToDoubleArray2(inputPath, rangeRows[0], rangeRows[1], columns);
            }
        } catch (Exception e) {
            // In production, use a logging framework
            Logger.error("Failed to read file: " + inputPath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a plot object based on configuration and transposed data.
     *
     * @param config Information configuration file.
     * @param data   Data for multiple curves to be plotted.
     */
    private Plot createPlot(Config config, double[][] data) {
        // Generate X-axis data (starting from 0, length = number of columns in the first data row)
        double[] x = ArrayUtils.rangeD(0, data[0].length);
        int seriesCount = data.length;

        // Build the 3D array required by the plot: each element is [xArray, yArray]
        double[][][] seriesData = new double[seriesCount][][];
        for (int i = 0; i < seriesCount; ++i) {
            seriesData[i] = new double[][]{x, data[i]};
        }

        Plot plot = new Plot();
        plot.init(config);
        plot.plot(seriesData);
        return plot;
    }

    public void stop() {

    }

    public Object getResult() {
        return null;
    }

    private WindowAdapter closeHandler() {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // The window has been closed and dispose is complete.
                GlobalBus.dispatch(new DoneEvent(Chart.this));
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // The window is closing;
            }
        };
    }
}

