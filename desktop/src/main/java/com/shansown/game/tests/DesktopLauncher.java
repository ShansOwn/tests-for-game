
package com.shansown.game.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.shansown.game.tests.utils.TestsList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DesktopLauncher extends JFrame {

    public DesktopLauncher () throws HeadlessException {
        super("libgdx Tests");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new TestList());
        pack();
        setSize(getWidth(), 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Runs the {@link ApplicationListener} with the given name.
     *
     * @param testName the name of a test class
     * @return {@code true} if the test was found and run, {@code false} otherwise
     */
    public static boolean runTest (String testName) {
        ApplicationListener test = TestsList.newTest(testName);
        if (test == null) {
            return false;
        }
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 640;
        config.height = 480;
        config.title = testName;
        config.forceExit = false;
        new LwjglApplication(test, config);
        return true;
    }

    class TestList extends JPanel {
        public TestList () {
            setLayout(new BorderLayout());

            final JButton button = new JButton("Run Test");

            final JList list = new JList(TestsList.getNames().toArray());
            JScrollPane pane = new JScrollPane(list);

            DefaultListSelectionModel m = new DefaultListSelectionModel();
            m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            m.setLeadAnchorNotificationEnabled(false);
            list.setSelectionModel(m);

            list.addMouseListener(new MouseAdapter() {
                public void mouseClicked (MouseEvent event) {
                    if (event.getClickCount() == 2) button.doClick();
                }
            });

            list.addKeyListener(new KeyAdapter() {
                public void keyPressed (KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) button.doClick();
                }
            });

            final Preferences prefs = new LwjglPreferences(new FileHandle(new LwjglFiles().getExternalStoragePath()
                    + ".prefs/lwjgl-tests"));
            list.setSelectedValue(prefs.getString("last", null), true);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed (ActionEvent e) {
                    String testName = (String)list.getSelectedValue();
                    prefs.putString("last", testName);
                    prefs.flush();
                    dispose();
                    runTest(testName);
                }
            });

            add(pane, BorderLayout.CENTER);
            add(button, BorderLayout.SOUTH);
        }
    }

    /**
     * Runs a libgdx test.
     *
     * If no arguments are provided on the command line, shows a list of tests to choose from.
     * If an argument is present, the test with that name will immediately be run.
     *
     * @param argv command line arguments
     */
    public static void main (String[] argv) throws Exception {
        if (argv.length > 0) {
            if (runTest(argv[0])) {
                return;
                // Otherwise, fall back to showing the list
            }
        }
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new DesktopLauncher();
    }
}
