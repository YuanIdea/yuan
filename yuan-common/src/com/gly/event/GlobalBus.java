package com.gly.event;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局事件总线。
 */
public class GlobalBus {
    /**
     * 单例模式的全局总线。
     */
    private static final GlobalBus INSTANCE = new GlobalBus();
    private final Map<Class<?>, List<EventListener>> listeners = new HashMap<>();

    /**
     * 不允许通过外部进行构造，保持单例模式。
     */
    private GlobalBus() {}

    /**
     * 注册监听器对象（自动扫描 @Subscribe 方法）
     * @param listener 监听对象。
     */
    public static void register(Object listener) {
        INSTANCE.register1(listener);
    }

    /**
     * 注销监听器
     * @param listener 监听对象。
     */
    public static void unregister(Object listener) {
        INSTANCE.unregister1(listener);
    }

    private void register1(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?> eventType = method.getParameterTypes()[0];
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new EventListener(listener, method));
            }
        }
    }

    /**
     * 注销监听器
     * @param listener 监听对象。
     */
    private void unregister1(Object listener) {
        listeners.values().forEach(list -> list.removeIf(e -> e.target == listener));
    }

    /**
     * 派发事件（支持冒泡），默认不参加事件源。
     * @param event 事件。
     */
    public static void dispatch(Event event) {
        INSTANCE.dispatch(event, null);
    }

    /**
     * 派发事件（支持冒泡）
     * @param type 事件类型。
     * @param data 事件传递数据。
     */
    public static void dispatch(EventType type, Object data) {
        INSTANCE.dispatch(new Event(type, data), null);
    }

    /**
     * 派发事件（支持冒泡）
     * @param type 事件类型。
     * @param data 事件传递数据。
     * @param source 事件源。
     */
    public static void dispatch(EventType type, Object data, Object source) {
        INSTANCE.dispatch(new Event(type, data), source);
    }

    /**
     * 派发事件（支持冒泡）
     * @param event 要派发的事件。
     * @param source 派发事件的事件源。
     */
    private void dispatch(Event event, Object source) {
        event.setSource(source);
        List<EventListener> handlers = listeners.get(event.getClass());
        if (handlers != null) {
            handlers.forEach(handler -> handler.handle(event));
        }

        // 冒泡逻辑（手动传递，无需继承）
        if (event.isBubbles() && source instanceof Container) {
            Container parent = ((Container) source).getParent();
            while (parent != null) {
                dispatch(event, parent);
                parent = parent.getParent();
            }
        }
    }

    /**
     * 事件监听器包装类
     */
    private static class EventListener {
        /** 目标监听对象。*/
        private final Object target;
        /** 事件处理方法。*/
        private final Method method;

        /**
         * 事件监听器构造函数。
         * @param target 目标监听对象。
         * @param method 事件处理方法。
         */
        EventListener(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        void handle(Event event) {
            try {
                method.invoke(target, event);
            } catch (Exception e) {
                throw new RuntimeException("事件处理失败", e);
            }
        }
    }
}
