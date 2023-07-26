import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Frame {

    public static JFrame jf;
    public static JPanel jp;
    public static JTextField keywordTextField;
    public static JFileChooser folderChooser;
    public static int result;
    public static JComboBox<String> comboBox;
    public static JCheckBox checkBox;
    public static JPanel resultsPanel;
    public static JScrollPane scrollPane;
    public static JProgressBar progressBar;
    public static JButton folderPickerButton;
    public static JPanel panel;
    public static java.util.List<File> pdfFiles;

    private static File lastSelectedDir = null; // Store the last selected directory

    public void initFrame() {
        jf = new JFrame();
        jf.setTitle("KeyWord Finder");

        BufferedImage icon;
        try {
            icon = ImageIO.read(Frame.class.getResource("/KeyWord.png"));   //icon of the app
            jf.setIconImage(icon);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        jp = new JPanel();  //full panel
        jp.setPreferredSize(new Dimension(500, 600));   // dimensions 500*600
        panel = new JPanel();   //panel for static data (input, KW, button, CB)
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Set the layout to vertical BoxLayout

        JLabel inputLabel = new JLabel("Izberite vrsto datoteke: ");    //input elements
        String[] options = { "pdf" }; // , "docx", "xlsx" --> pride v novi verziji
        comboBox = new JComboBox<>(options);

        JPanel inputPanel = new JPanel();   // input panel
        inputPanel.add(inputLabel);
        inputPanel.add(comboBox);
        panel.add(inputPanel);

        JLabel kwLabel = new JLabel("Vnesite ključno/e besedo/e: ");    //KW elements
        keywordTextField = new JTextField(20);

        JPanel keywordPanel = new JPanel(); //KW panel
        keywordPanel.add(kwLabel);
        keywordPanel.add(keywordTextField);
        panel.add(keywordPanel);

        folderPickerButton = new JButton("Izberite mapo");  //button element + panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(folderPickerButton);
        panel.add(buttonPanel);

        checkBox = new JCheckBox(); //CB elements
        JLabel cbLabel = new JLabel("Celotna pot do datoteke");

        JPanel checkboxPanel = new JPanel();    //CB panel
        checkboxPanel.add(checkBox);
        checkboxPanel.add(cbLabel);
        panel.add(checkboxPanel);

        progressBar = new JProgressBar(0, 100); //progress bar
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setStringPainted(true); // Display percentage as text
        progressBar.setVisible(false);
        panel.add(progressBar);

        jp.add(panel);

        resultsPanel = new JPanel();    //panel for dynamic data (results)
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(resultsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        jp.add(scrollPane);

        jf.getContentPane().add(jp);// adding to content pane will work here. Please read the comment bellow.
        jf.pack();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Add this line to close the program when the frame is
                                                           // closed
        jf.setVisible(true); // Add this line to make the frame visible

        /* FUNCTIONS */

        //Function which searches for files with keyword in it
        folderPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //if input is empty, we dont search
                if (keywordTextField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Polje ključne besede je prazno!", "Alert",
                            JOptionPane.WARNING_MESSAGE);
                } else {

                    folderChooser = new JFileChooser();

                    // Set the current directory to the last selected directory
                    if (lastSelectedDir != null) {
                        folderChooser.setCurrentDirectory(lastSelectedDir);
                    }
                    // Show the folder chooser dialog
                    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    result = folderChooser.showOpenDialog(jf);

                    new Thread(new FolderPick()).start();   //we have to start a thread so we can update progress bar

                }
            }
        });

        //CB function which changes length of data to full pat or only shortened path
        // function is a part of the main Thread so everything is described there
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int hgtScroll = 0;

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // Clear previous results
                    if (resultsPanel != null) {
                        resultsPanel.removeAll();
                    }
                    if (!pdfFiles.isEmpty()) {
                        for (File file : pdfFiles) {
                            addResultToPanel(file.getAbsolutePath());
                        }
                    } else {
                        JPanel resultPanel = new JPanel();
                        resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

                        // Create a label to display the file path
                        JLabel label = new JLabel("Brez zadetkov");
                        resultPanel.add(label);

                        resultsPanel.add(resultPanel);
                    }
                    hgtScroll = (pdfFiles.size() + 1) * 32;
                    if (hgtScroll > 580 - panel.getPreferredSize().height) {
                        hgtScroll = 580 - panel.getPreferredSize().height;
                    }
                    scrollPane.setPreferredSize(new Dimension(480, hgtScroll));
                    // Refresh the UI
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    jp.revalidate();
                    jp.repaint();
                    jf.revalidate();
                    jf.repaint();
                    progressBar.setVisible(false);
                    folderPickerButton.setEnabled(true);
                    checkBox.setEnabled(true);

                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    // Clear previous results
                    if (resultsPanel != null) {
                        resultsPanel.removeAll();
                    }
                    if (!pdfFiles.isEmpty()) {
                        for (File file : pdfFiles) {
                            addResultToPanel(file.getAbsolutePath());
                        }
                    } else {
                        JPanel resultPanel = new JPanel();
                        resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

                        // Create a label to display the file path
                        JLabel label = new JLabel("Brez zadetkov");
                        resultPanel.add(label);

                        resultsPanel.add(resultPanel);
                    }
                    hgtScroll = (pdfFiles.size() + 1) * 32;
                    if (hgtScroll > 600 - panel.getPreferredSize().height) {
                        hgtScroll = 600 - panel.getPreferredSize().height;
                    }
                    scrollPane.setPreferredSize(new Dimension(480, hgtScroll));
                    // Refresh the UI
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    jp.revalidate();
                    jp.repaint();
                    jf.revalidate();
                    jf.repaint();
                    progressBar.setVisible(false);
                    folderPickerButton.setEnabled(true);
                    checkBox.setEnabled(true);

                }
            }
        });
        
    }

    //MAIN thread/function/class
    public static class FolderPick implements Runnable {

        @Override
        public void run() {

            int hgtScroll = 0;

            // Check if a folder was selected
            if (result == JFileChooser.APPROVE_OPTION) {

                progressBar.setVisible(true);
                folderPickerButton.setEnabled(false);
                checkBox.setEnabled(false);

                // Get the selected folder
                File selectedFolder = folderChooser.getSelectedFile();
                lastSelectedDir = selectedFolder;

                // Get the selected file ending (e.g., pdf)
                String selectedEnding = comboBox.getSelectedItem().toString();

                // Get the keyword entered by the user
                String keyword = keywordTextField.getText().trim().toLowerCase();

                // Find documents with the selected file ending and containing the keyword
                pdfFiles = findFilesWithEndingAndKeyword(selectedFolder, selectedEnding,
                        keyword);

                // Clear previous results
                if (resultsPanel != null) {
                    resultsPanel.removeAll();
                }

                // Display the list of PDF files containing the keyword
                if (!pdfFiles.isEmpty()) {
                    for (File file : pdfFiles) {
                        addResultToPanel(file.getAbsolutePath());
                    }
                } else {
                    JPanel resultPanel = new JPanel();
                    resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

                    // Create a label to display the file path
                    JLabel label = new JLabel("Brez zadetkov");
                    resultPanel.add(label);

                    resultsPanel.add(resultPanel);
                }

                //setting height of the results panel
                hgtScroll = (pdfFiles.size() + 1) * 32;
                if (hgtScroll > 600 - panel.getPreferredSize().height) {
                    hgtScroll = 600 - panel.getPreferredSize().height;
                }
                scrollPane.setPreferredSize(new Dimension(480, hgtScroll));
                // Refresh the UI
                resultsPanel.revalidate();
                resultsPanel.repaint();
                scrollPane.revalidate();
                scrollPane.repaint();
                jp.revalidate();
                jp.repaint();
                jf.revalidate();
                jf.repaint();
                progressBar.setVisible(false);
                folderPickerButton.setEnabled(true);
                checkBox.setEnabled(true);
            }
        }
    }

    private static void addResultToPanel(String filePath) {

        String txt = "";

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        //checking for checkbox state and fiduring out in which way to display data
        if (checkBox.isSelected()) {
            txt = filePath;
        } else {

            String[] pathParts = filePath.split("\\\\");
            if (pathParts.length > 2) {
                txt = pathParts[0] + "\\" + pathParts[1] + "\\...\\"
                        + pathParts[pathParts.length - 1];
            } else {
                txt = filePath;
            }
        }

        // Create a label to display the file path
        JLabel label = new JLabel(txt);
        resultPanel.add(label);

        // Create a button for opening the file
        JButton openButton = new JButton("Odpri");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement the action to open the file here
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        resultPanel.add(openButton);

        resultsPanel.add(resultPanel);
    }

    //function that finds matches
    private static List<File> findFilesWithEndingAndKeyword(File folder, String fileEnding, String keyword) {
        List<File> result = new ArrayList<>();

        int filesProcessed = 0;

        if (folder.isDirectory()) {
            try (Stream<Path> walk = Files.walk(folder.toPath())) {
                List<String> files = walk.map(Path::toString)
                        .filter(f -> f.toLowerCase().endsWith("." + fileEnding.toLowerCase()))
                        .collect(Collectors.toList());

                int size = files.size();

                for (String filePath : files) {
                    if (containsKeyword(filePath, keyword)) {
                        result.add(new File(filePath));
                    }
                    //updating progress bar
                    filesProcessed++;
                    int progressPercentage = (int) ((double) filesProcessed / size * 100);
                    //System.out.println(progressPercentage);
                    progressBar.setValue(progressPercentage);
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
        return result;
    }

    private static boolean containsKeyword(String filePath, String keyword) {
        try {
            File file = new File(filePath);
            PDDocument doc = Loader.loadPDF(file);
            PDFTextStripper findPhrase = new PDFTextStripper();
            String text = findPhrase.getText(doc);
            String PDF_content = text.toLowerCase();
            boolean result = PDF_content.contains(keyword);
            doc.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
