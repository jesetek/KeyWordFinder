import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private JTextField keywordTextField;

    public void initFrame() {
        JFrame jf = new JFrame();
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(400, 400));// changed it to preferredSize, Thanks!

        JLabel inputLabel = new JLabel("Izberite vrsto datoteke: ");
        jp.add(inputLabel);

        String[] options = { "pdf", "docx", "xlsx" };
        JComboBox<String> comboBox = new JComboBox<>(options);
        jp.add(comboBox);

        keywordTextField = new JTextField(20);
        jp.add(keywordTextField);

        JButton folderPickerButton = new JButton("Select Folder");
        jp.add(folderPickerButton);

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
                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                // Show the folder chooser dialog
                int result = folderChooser.showOpenDialog(jf);

                // Check if a folder was selected
                if (result == JFileChooser.APPROVE_OPTION) {
                    // Get the selected folder
                    File selectedFolder = folderChooser.getSelectedFile();

                    // Get the selected file ending (e.g., pdf)
                    String selectedEnding = comboBox.getSelectedItem().toString();

                    // Get the keyword entered by the user
                    String keyword = keywordTextField.getText().trim().toLowerCase();

                    // Find documents with the selected file ending and containing the keyword
                    List<File> pdfFiles = findFilesWithEndingAndKeyword(selectedFolder, selectedEnding, keyword);

                    // Display the list of PDF files containing the keyword
                    if (!pdfFiles.isEmpty()) {
                        for (File file : pdfFiles) {
                            System.out.println("PDF File with the keyword: " + file.getAbsolutePath());
                        }
                    } else {
                        System.out.println("No PDF files found with the selected ending and containing the keyword.");
                    }
                }
            }
        });
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
