import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Frame {
    public void initFrame() {
        JFrame jf = new JFrame();
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(400, 400));// changed it to preferredSize, Thanks!

        JLabel inputLabel = new JLabel("Izberite vrsto datoteke: ");
        jp.add(inputLabel);

        String[] options = { "doc/docx", "pdf", "xls/xlsx" };
        JComboBox<String> comboBox = new JComboBox<>(options);
        jp.add(comboBox);

        JButton folderPickerButton = new JButton("Select Folder");
        jp.add(folderPickerButton);

        jf.getContentPane().add(jp);// adding to content pane will work here. Please read the comment bellow.
        jf.pack();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Add this line to close the program when the frame is
                                                           // closed
        jf.setVisible(true); // Add this line to make the frame visible

        /* FUNKCIJE */

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
                    java.io.File selectedFolder = folderChooser.getSelectedFile();
                    // Do something with the selected folder, such as displaying its path
                    System.out.println("Selected Folder: " + selectedFolder.getAbsolutePath());
                }
            }
        });
    }
}
