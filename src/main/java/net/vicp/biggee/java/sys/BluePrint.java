package net.vicp.biggee.java.sys;

import net.vicp.biggee.kotlin.sys.core.NakedBoot;
import net.vicp.biggee.kotlin.util.FileIO;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.TreeMap;

/**
 * @program: BluePrint
 * @description: 主类
 * @author: Biggee
 * @create: 2019-12-03 13:50
 **/
public class BluePrint extends TreeMap<String, Object> implements ServletContextListener {
    public static BluePrint INSTANCE = null;
    private static Logger logger = null;

    public BluePrint() {
        INSTANCE = this;
    }

    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            InitGlobalFont(new Font("Ubuntu", Font.PLAIN, 12));  //统一设置字体
        }

        JFrame frame = new JFrame("BluePrintMain");

        GridLayout layout = new GridLayout();
        frame.setLayout(layout);

        JButton button = new JButton("Start");
        button.addActionListener(a -> net.vicp.biggee.kotlin.sys.core.NakedBoot.INSTANCE.start());
        //button.setFont(Font.getFont(Font.SANS_SERIF));
        frame.add(button);

        JButton buttonEnd = new JButton("End");
        buttonEnd.addActionListener(a -> {
            try {
                net.vicp.biggee.kotlin.sys.core.NakedBoot.INSTANCE.stopTomcat(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        frame.add(buttonEnd);

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) { }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                buttonEnd.doClick();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) { }

            @Override
            public void windowIconified(WindowEvent windowEvent) { }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {}

            @Override
            public void windowActivated(WindowEvent windowEvent) {}

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {}
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * 统一设置字体，父界面设置之后，所有由父界面进入的子界面都不需要再次设置字体
     */
    private static void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

    /**
     * * Notification that the web application initialization process is starting.
     * All ServletContextListeners are notified of context initialization before
     * any filter or servlet in the web application is initialized.
     * The default implementation is a NO-OP.
     *
     * @param sce Information about the ServletContext that was initialized
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        put(sce.getClass().getName(), sce);
        final File path = FileIO.INSTANCE.bornDir(System.getProperty("catalina.base") + File.separator + "upload");
        NakedBoot.setUploadDir(path.getAbsolutePath());
        NakedBoot.getGlobalSetting().put("uploadDir", NakedBoot.getUploadDir());

        Logger log = Logger.getLogger(BluePrint.class);//获取log对象
        FileAppender fileAppender = (FileAppender) Logger.getRootLogger().getAppender("File");//获取FileAppender对象
        fileAppender.setFile(NakedBoot.getUploadDir() + File.separator + "ssm.log");//重新设置输出日志的路径和文件名
        fileAppender.activateOptions();//使设置的FileAppender起作用
        logger = log;
        logger.info("++++++++++++++++Server Init+++++++++++++++++++");
        logger.info("path:" + NakedBoot.getUploadDir());
    }

    /**
     * * Notification that the servlet context is about to be shut down. All
     * servlets and filters have been destroyed before any
     * ServletContextListeners are notified of context destruction.
     * The default implementation is a NO-OP.
     *
     * @param sce Information about the ServletContext that was destroyed
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("++++++++++++++++Server Destroy+++++++++++++++++++");
        remove(sce.getClass().getName());
    }

    @Override
    protected void finalize() throws Throwable {
        remove(getClass().getName());
        logger.error("++++++++++++++++try " + toString() + " finalize+++++++++++++++++++");
        super.finalize();
    }

    @Override
    public String toString() {
        return "BluePrint{" + new Date() + "}";
    }
}
