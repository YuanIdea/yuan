package com.gly;

import com.gly.util.PlotColors;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Plot {
    private String title;
    private String labelX;
    private String labelY;
    private String[] legend;
    private float[] lineWidth;
    private String[] style;
    private JDialog dialog;
    private JFrame owner;

    /**
     * Constructor.
     */
    public Plot() {
        title = "";
        labelX = "";
        labelY = "";
        legend = null;
        lineWidth = null;
        style = null;
    }

    /**
     * Constructor.
     *
     * @param owner the parent frame that owns this plot window, can be null for no owner
     */
    public Plot(JFrame owner) {
        title = "";
        labelX = "";
        labelY = "";
        legend = null;
        lineWidth = null;
        style = null;
        this.owner = owner;
    }

    public void plot(double[] y) {
        double[] x = new double[y.length];
        Arrays.setAll(x, i -> i + 1);
        plot(x, y);
    }

    public void init(Config config) {
        title = config.getTitle();
        labelX = config.getLabelX();
        labelY = config.getLabelY();
        legend = config.getLegend();
        lineWidth = config.getLineWidth();
        style = config.getStyle();
    }

    private void plot(double[] x, double[] y) {
        validateInput(x, y);
        XYChart chart = createChart();
        XYSeries series = chart.addSeries("Data", x, y);
        applySeriesStyle(series, 0); // Apply style for the first line
        displayChart(chart);
    }

    public void plot(double[] x, double[][] data) {
        if (data.length == 0) {
            return;
        }

        XYChart chart = createChart();
        int length = data.length;
        for (int i = 0; i < length; ++i) {
            double[] y = data[i];
            validateInput(x, y);
            String seriesName = (legend != null && legend.length == length) ? legend[i] : "Series " + (i + 1);
            XYSeries series = chart.addSeries(seriesName, x, y);
            applySeriesStyle(series, i);
        }
        displayChart(chart);
    }

    private XYChart createChart() {
        XYChart chart = new XYChartBuilder()
                .theme(Styler.ChartTheme.Matlab)
                .title(title)
                .xAxisTitle(labelX)
                .yAxisTitle(labelY)
                .build();

        Styler st = chart.getStyler();
        chart.getStyler().setPlotGridLinesVisible(false);

        st.setLegendVisible(true);
        st.setLegendPosition(Styler.LegendPosition.OutsideS);
        st.setLegendLayout(Styler.LegendLayout.Horizontal);
        st.setLegendBorderColor(Color.WHITE);

        st.setChartBackgroundColor(Color.WHITE);
        st.setPlotBackgroundColor(Color.WHITE);
        return chart;
    }

    private void applySeriesStyle(XYSeries series, int index) {
        // Set colors
        Color color = getColorForIndex(index);
        series.setLineColor(color);
        series.setMarkerColor(color);

        // Set line style
        float width = (lineWidth != null && lineWidth.length > index) ? lineWidth[index] : 1.0f;
        series.setLineWidth(width);

        String lineStyle = (style != null && style.length > index) ? style[index] : "-";
        if (lineStyle.contains("--")) {
            series.setLineStyle(SeriesLines.DASH_DASH);
        } else if (lineStyle.contains(":")) {
            series.setLineStyle(SeriesLines.DASH_DOT);
        } else if (lineStyle.contains("-.")) {
            series.setLineStyle(SeriesLines.DASH_DOT); // XChart does not have direct dash-dot; custom BasicStroke can be used for finer control
        } else {
            series.setLineStyle(SeriesLines.SOLID);
        }

        // Markers are disabled by default; setMarker(SeriesMarkers.CIRCLE) can be used if needed
        series.setMarker(SeriesMarkers.NONE);
    }

    private Color getColorForIndex(int index) {
        if (style != null && style.length > index) {
            char colorChar = !style[index].isEmpty() ? style[index].charAt(0) : 'b';
            Color color = PlotColors.CHAR_MAP.get(colorChar);
            if (color != null) return color;
        }
        return PlotColors.CYCLE.get(index % PlotColors.CYCLE.size());
    }

    private void displayChart(XYChart chart) {
        XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
        dialog = new JDialog(owner, "Figure", false);
        dialog.add(chartPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void validateInput(double[] x, double[] y) {
        if (x == null || y == null) throw new IllegalArgumentException("Array cannot be null");
        if (x.length != y.length) throw new IllegalArgumentException("X and Y must have the same length");
        if (x.length == 0) throw new IllegalArgumentException("Array cannot be empty");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLabelX(String labelX) {
        this.labelX = labelX;
    }

    public void setLabelY(String labelY) {
        this.labelY = labelY;
    }

    public void setLegend(String[] legend) {
        this.legend = legend;
    }

    public void setLineWidth(float[] lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setStyle(String[] style) {
        this.style = style;
    }

    public Dialog getDialog() {
        return dialog;
    }
}
