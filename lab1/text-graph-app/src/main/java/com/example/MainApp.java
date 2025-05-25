package com.example;

import com.example.gui.MainFrame;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(args);
            frame.setVisible(true);
        });
    }
}
