package com.gly;

import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;

public class Item {
    String className;
    double probability;
    int x;
    int y;
    int w;
    int h;


    public Item(DetectedObjects.DetectedObject obj, float scaleX, float scaleY, int imgWidth, int imgHeight) {
        Rectangle rect = obj.getBoundingBox().getBounds();
        x = Math.max(0, Math.min((int) (rect.getX() * scaleX), imgWidth - 1));
        y = Math.max(0, Math.min((int) (rect.getY() * scaleY), imgHeight - 1));
        w = Math.max(1, Math.min((int) (rect.getWidth() * scaleX), imgWidth - x));
        h = Math.max(1, Math.min((int) (rect.getHeight() * scaleY), imgHeight - y));
        probability = obj.getProbability();
        className = obj.getClassName();
    }
}
