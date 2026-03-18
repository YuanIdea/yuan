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
    private JFrame frame;

    public Plot() {
        title = "";
        labelX = "";
        labelY = "";
        legend = null;
        lineWidth = null;
        style = null;
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
        applySeriesStyle(series, 0); // 应用第一条线的样式
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
                .width(700).height(500)
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
            series.setLineStyle(SeriesLines.DASH_DOT); // XChart 没有直接的 dash-dot，可以用自定义 BasicStroke 或近似
            // 如需更精细控制，可自定义 BasicStroke
        } else {
            series.setLineStyle(SeriesLines.SOLID);
        }

        // 默认不显示点，如果需要可设置 setMarker(SeriesMarkers.CIRCLE)
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
        // Use SwingWrapper to display the chart.
        SwingWrapper<XYChart> sw = new SwingWrapper<>(chart);
        this.frame = sw.displayChart();
        frame.setTitle("Figure");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
}
