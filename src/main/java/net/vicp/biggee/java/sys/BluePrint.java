package net.vicp.biggee.java.sys;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @program: BluePrint
 * @description: 主类
 * @author: Biggee
 * @create: 2019-12-03 13:50
 **/
public class BluePrint extends TreeMap<UUID, Object> {
    public static void main(String[] args) {
        JFrame frame = new JFrame("BluePrintMain");

        GridLayout layout = new GridLayout();
        frame.setLayout(layout);

        JButton button = new JButton("Start");
        button.addActionListener(a -> net.vicp.biggee.kotlin.sys.core.BluePrint.INSTANCE.start());
        frame.add(button);

        JButton buttonEnd = new JButton("End");
        buttonEnd.addActionListener(a -> {
            try {
                net.vicp.biggee.kotlin.sys.core.BluePrint.INSTANCE.stopTomcat(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        frame.add(buttonEnd);

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {

            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                buttonEnd.doClick();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {

            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {

            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
