package com.gly;

import com.gly.event.DoneEvent;
import com.gly.event.GlobalBus;
import com.gly.io.csv.Reader;
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
        Config jsonConfig = JsonUtil.load(name, Config.class);
        if (jsonConfig != null) {
            String inputName = PathUtil.resolveAbsolutePath(root, jsonConfig.getInputPathName());
            int[] rangeRows = jsonConfig.getRangeRows();
            double[][] data;
            if (rangeRows == null) {
                data = Reader.readToDoubleArray2(inputName, 1, jsonConfig.getInputIndex());
            } else {
                data = Reader.readToDoubleArray2(inputName, rangeRows[0], rangeRows[1], jsonConfig.getInputIndex());
            }
            data = ArrayUtils.transpose(data);
            if (data != null) {
                double[] x = ArrayUtils.rangeD(0, data[0].length);
                Plot plot = new Plot();
                plot.init(jsonConfig);
                int length = data.length;
                double[][][] seriesData = new double[length][][]; // data.length条线
                for (int i = 0; i < length; ++i) {
                    seriesData[i] = new double[][]{x, data[i]};
                }
                plot.plot(seriesData);
                plot.getFrame().addWindowListener(closeHandler());
            }
        }
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
                // 窗口已经关闭并且 dispose 完成
                GlobalBus.dispatch(new DoneEvent(Chart.this));
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口正在关闭;
            }
        };
    }
}

