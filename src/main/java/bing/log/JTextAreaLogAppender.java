package bing.log;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

/**
 * log4j自定义appender，截获log4j日志输出到JTextArea中
 *
 * @author IceWee
 */
public class JTextAreaLogAppender extends Thread {

    /**
     * 默认图形界面appender名称
     */
    static final String DEFAULT_APPENDER_NAME = "gui";

    private PipedReader reader;
    private JTextArea gui;
    private boolean run = true;

    public JTextAreaLogAppender(String appenderName, JTextArea textArea, JScrollPane scrollPane) throws IOException {
        Logger root = Logger.getRootLogger();
        // 获取子记录器的输出源
        Appender appender = root.getAppender(appenderName);
        // 定义一个未连接的输入流管道
        reader = new PipedReader();
        // 定义一个已连接的输出流管理，并连接到reader
        Writer writer = new PipedWriter(reader);
        // 设置 appender 输出流
        ((WriterAppender) appender).setWriter(writer);
        this.gui = textArea;
    }

    public JTextAreaLogAppender(JTextArea textArea, JScrollPane scrollPane) throws IOException {
        this(DEFAULT_APPENDER_NAME, textArea, scrollPane);
    }

    @Override
    public void run() {
        // 不间断地扫描输入流
        Scanner scanner = new Scanner(reader);
        // 将扫描到的字符流输出到指定的JTextArea组件
        while (this.run && scanner.hasNextLine()) {
            try {
                // 睡眠
                Thread.sleep(100);
                String line = scanner.nextLine();
                gui.append(line);
                gui.append("\n");
                // 使垂直滚动条自动向下滚动
                gui.setCaretPosition(gui.getDocument().getLength());
            } catch (InterruptedException e) {
                // 异常不做处理
            }
        }
    }

    public void setRun(boolean run) {
        this.run = run;
    }

}
