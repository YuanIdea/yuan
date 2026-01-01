package com.gly.event;

/**
 * 事件类。
 */
public class Event {
    private  EventType type;                // 事件类型
    private Object data;                    // 数据
    private Object source;                  // 事件源
    private boolean bubbles;                // 是否冒泡

    public Event(Object data) {
        this.data = data;
    }

    public Event(EventType type) {
        this.type = type;
        this.bubbles = false;
    }

    public Event(EventType type, Object data) {
        this.type = type;
        this.data = data;
        this.bubbles = false;
    }

    public Event(EventType type, Object data, Object source, boolean bubbles) {
        this.type = type;
        this.data = data;
        this.source = source;
        this.bubbles = bubbles;
    }

    /**
     * 获取事件类型。
     * @return 返回事件类型。
     */
    public EventType getType() { return type; }

    /**
     * 获得事件源。
     * @return 返回事件源。
     */
    public Object getSource() { return source; }

    /**
     * 设置事件源。
     * @param source 触发事件的事件源。
     */
    public void setSource(Object source) { this.source = source; }

    /**
     * 是否冒泡。
     * @return 是否冒泡。
     */
    public boolean isBubbles() { return bubbles; }

    /**
     * 设置是否冒泡。
     * @param bubbles 指定是否冒泡。
     */
    public void setBubbles(boolean bubbles) {
        this.bubbles  = bubbles;
    }

    /**
     * 获取数据。
     * @return 返回数据。
     */
    public Object getData() {
        return data;
    }
}