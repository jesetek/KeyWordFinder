import java.awt.Dimension;
import javax.swing.*;

public class Frame {
    public void initFrame() {
        JFrame jf = new JFrame();
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(400, 400));// changed it to preferredSize, Thanks!
        jf.getContentPane().add(jp);// adding to content pane will work here. Please read the comment bellow.
        jf.pack();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Add this line to close the program when the frame is closed
        jf.setVisible(true); // Add this line to make the frame visible
    }
}
