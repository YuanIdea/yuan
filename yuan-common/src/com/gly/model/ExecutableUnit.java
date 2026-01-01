package com.gly.model;

import javax.swing.*;

/**
 * 可执行单元接口。
 */
public interface ExecutableUnit {
   /**
    * 初始化。
    * @param root 根路径。
    * @param name 配置名称。
    * @param owner 父亲单元。
    */
   void init(String root, String name, JFrame owner);

   String getName();

   /**
    * 开始执行程序。
    */
   void start();

   /**
    * 停止程序。
    */
   void stop();

   /**
    * 程序是否结束。
    * @return true程序运行结束， 否则没有结束。
    */
   boolean isDone();

   /**
    * 获得运行结果。
    * @return 运行结果。
    */
   Object getResult();
}