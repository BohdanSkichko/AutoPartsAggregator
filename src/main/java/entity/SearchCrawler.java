/*
package entity;

import javafx.stage.WindowEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.io.PrintWriter;
import java.util.HashMap;

public class SearchCrawler extends JFrame {

    // Максимальное количество раскрывающихся значений URL.
    private static final String[] MAX_URLS =
            {"50", "100", "500", "1000"};
    // Кэш-память для списка ограничений робота.
    private HashMap disallowListCache = new HashMap();
    // Элементы управления графического интерфейса панели Search.
    private JTextField startTextField;
    private JComboBox maxComboBox;
    private JCheckBox limitCheckBox;
    private JTextField logTextField;
    private JTextField searchTextField;
    private JCheckBox caseCheckBox;
    private JButton searchButton;
    // Элементы управления графического интерфейса панели Stats.
    private JLabel crawlingLabel2;
    private JLabel crawledLabel2;
    private JLabel toCrawlLabel2;
    private JProgressBar progressBar;
    private JLabel matchesLabel2;
    // Список соответствий.
    private JTable table;
    // Флаг отображения состояния поиска.
    private boolean crawling;
    // Файл журнала для текстового вывода.
    private PrintWriter logFileWriter;
    // Конструктор для поискового червя.
    public SearchCrawler()
    {
        // Установка заголовка приложения.
        setTitle("Search Crawler");
        // Установка размеров окна.
        setSize(600, 600);
        // ОБработка событий по закрытию окна.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });
        // Установить меню “файл”.
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Exit",
                KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        // Установить панель поиска.
        JPanel searchPanel = new JPanel();
        GridBagConstraints constraints;
        GridBagLayout layout = new GridBagLayout();
        searchPanel.setLayout(layout);
        JLabel startLabel = new JLabel("Start URL:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(startLabel, constraints);
        searchPanel.add(startLabel);
        startTextField = new JTextField();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(startTextField, constraints);
        searchPanel.add(startTextField);
        JLabel maxLabel = new JLabel("Max URLs to Crawl:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(maxLabel, constraints);
        searchPanel.add(maxLabel);
        maxComboBox = new JComboBox(MAX_URLS);
        maxComboBox.setEditable(true);
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(maxComboBox, constraints);
        searchPanel.add(maxComboBox);
        limitCheckBox =
                new JCheckBox("Limit crawling to Start URL site");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);
        layout.setConstraints(limitCheckBox, constraints);
        searchPanel.add(limitCheckBox);
        JLabel blankLabel = new JLabel();
        constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(blankLabel, constraints);
        searchPanel.add(blankLabel);
        JLabel logLabel = new JLabel("Matches Log File:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(logLabel, constraints);
        searchPanel.add(logLabel);
        String file =
                System.getProperty("user.dir") +
                        System.getProperty("file.separator") +
                        "crawler.log";
        logTextField = new JTextField(file);
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 0, 5);
        layout.setConstraints(logTextField, constraints);
        searchPanel.add(logTextField);
        JLabel searchLabel = new JLabel("Search String:");
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 0, 0);
        layout.setConstraints(searchLabel, constraints);
        searchPanel.add(searchLabel);
        searchTextField = new JTextField();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 0, 0);
        constraints.gridwidth= 2;
        constraints.weightx = 1.0d;
        private void actionExit() {
        System.exit(0);
    }
}
*/
