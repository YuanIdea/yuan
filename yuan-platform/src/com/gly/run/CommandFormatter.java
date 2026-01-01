package com.gly.run;

import java.util.List;

class CommandFormatter {
    static String formatCommand(List<String> command) {
        String split = " ";
        StringBuilder sb = new StringBuilder();
        // 第一行：Java路径（带颜色）
        sb.append(quoteArgument(command.get(0)));

        // 后续参数分组处理
        for (int i = 1; i < command.size(); ++i) {
            String arg = command.get(i);

            // JVM参数分组显示
            if (arg.startsWith("-D") || arg.startsWith("-X")) {
                sb.append(split).append(quoteArgument(arg));
            }
            // 特殊处理-classpath
            else if (arg.equals("-cp") || arg.equals("-classpath")) {
                sb.append(split).append(arg).append(split);
                String cp = command.get(++i);
                sb.append(cp);
            }
            // 主类名单独显示
            else {
                sb.append(split).append(quoteArgument(arg));
            }
        }

        return sb.toString().trim();
    }

    // 路径带空格时添加双引号
    private static String quoteArgument(String arg) {
        if (arg.contains(" ") || arg.contains(";")) {
            return "\"" + arg + "\"";
        }
        return arg;
    }
}
