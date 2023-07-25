import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

    private JFrame jf;
    private JPanel jp;
    private JTextField keywordTextField;
    private JFileChooser  folderChooser;
    private int result;
    private JComboBox<String> comboBox;
    private JCheckBox checkBox;
    private JPanel resultsPanel;
    private JScrollPane scrollPane;

    public void initFrame() {
        jf = new JFrame();
        jp = new JPanel();
        jp.setPreferredSize(new Dimension(400, 400));// changed it to preferredSize, Thanks!

        JLabel inputLabel = new JLabel("Izberite vrsto datoteke: ");
        jp.add(inputLabel);

        String[] options = { "pdf", "docx", "xlsx" };
        comboBox = new JComboBox<>(options);
        jp.add(comboBox);

        JLabel kwLabel = new JLabel("Vnesite ključno/e besedo/e: ");
        jp.add(kwLabel);

        keywordTextField = new JTextField(20);
        jp.add(keywordTextField);

        JButton folderPickerButton = new JButton("Select Folder");
        jp.add(folderPickerButton);

        checkBox = new JCheckBox();
        jp.add(checkBox);

        JLabel cbLabel = new JLabel("Celotna pot do datoteke");
        jp.add(cbLabel);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(resultsPanel);
        jp.add(scrollPane);

        jf.getContentPane().add(jp);// adding to content pane will work here. Please read the comment bellow.
        jf.pack();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Add this line to close the program when the frame is
                                                           // closed
        jf.setVisible(true); // Add this line to make the frame visible

        /* FUNKCIJE */

        // ActionListener for the folder picker button
        folderPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (keywordTextField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Polje ključne besede je prazno!", "Alert",
                            JOptionPane.WARNING_MESSAGE);
                } else {

                    folderChooser = new JFileChooser();
                    // Show the folder chooser dialog
                    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    result = folderChooser.showOpenDialog(jf);

                    folderPick(folderChooser, result);
                    
                }
            }
        });

        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (keywordTextField.getText().equals("")) {

                    } else {
                        folderPick(folderChooser, result);
                    }
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    if (keywordTextField.getText().equals("")) {

                    } else {
                        folderPick(folderChooser, result);
                    }
                }
            }
        });
    }

    private void folderPick(JFileChooser folderChooser, int result){

                    // Check if a folder was selected
                    if (result == JFileChooser.APPROVE_OPTION) {
                        // Get the selected folder
                        File selectedFolder = folderChooser.getSelectedFile();

                        // Get the selected file ending (e.g., pdf)
                        String selectedEnding = comboBox.getSelectedItem().toString();

                        // Get the keyword entered by the user
                        String keyword = keywordTextField.getText().trim().toLowerCase();

                        // Find documents with the selected file ending and containing the keyword
                        java.util.List<File> pdfFiles = findFilesWithEndingAndKeyword(selectedFolder, selectedEnding,
                                keyword);

                        // Clear previous results
                        if (resultsPanel != null) {
                            resultsPanel.removeAll();
                        }

                        // Display the list of PDF files containing the keyword
                        if (!pdfFiles.isEmpty()) {
                            for (File file : pdfFiles) {
                                if (checkBox.isSelected()) {
                                    addResultToPanel(file.getAbsolutePath());
                                } else {

                                    String[] pathParts = file.getAbsolutePath().split("\\\\");
                                    if (pathParts.length > 2) {
                                        addResultToPanel(pathParts[0] + "/" + pathParts[1] + "/.../"
                                                + pathParts[pathParts.length - 1]);
                                    } else {
                                        addResultToPanel(pathParts[0] + "/" + pathParts[1]);
                                    }
                                }
                            }
                        } else {
                            addResultToPanel("Brez zadetkov");
                        }

                        scrollPane.setPreferredSize(new Dimension(400, scrollPane.getPreferredSize().height + 50));
                        // Refresh the UI
                        resultsPanel.revalidate();
                        resultsPanel.repaint();
                        scrollPane.revalidate();
                        scrollPane.repaint();
                        jp.revalidate();
                        jp.repaint();
                        jf.revalidate();
                        jf.repaint();
                    }
    }

    private void addResultToPanel(String filePath) {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Create a label to display the file path
        JLabel label = new JLabel(filePath);
        resultPanel.add(label);

        // Create a button for opening the file
        JButton openButton = new JButton("Open");
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

    private List<File> findFilesWithEndingAndKeyword(File folder, String fileEnding, String keyword) {
        List<File> result = new ArrayList<>();
        if (folder.isDirectory()) {
            try (Stream<Path> walk = Files.walk(folder.toPath())) {
                List<String> files = walk.map(Path::toString)
                        .filter(f -> f.toLowerCase().endsWith("." + fileEnding.toLowerCase()))
                        .collect(Collectors.toList());

                for (String filePath : files) {
                    if (containsKeyword(filePath, keyword)) {
                        result.add(new File(filePath));
                    }
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
        return result;
    }

    private boolean containsKeyword(String filePath, String keyword) {
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
