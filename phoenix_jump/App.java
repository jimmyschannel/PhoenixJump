import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Phoenix Jump");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PhoenixJump phoenix = new PhoenixJump(); // Corrected class name
        frame.add(phoenix);
        frame.pack();
        phoenix.requestFocus();
        frame.setVisible(true);
    }
}
