import javax.swing.*;

public class MazeGenerator extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MazeGenerator frame = new MazeGenerator();
            boolean startGame = WelcomeScreen.showWelcome(frame);
            if (startGame) {
                frame.setVisible(true);
                frame.bgMusic = SoundManager.playSoundLoop("backsound.wav");
                SwingUtilities.invokeLater(() -> frame.generateMaze());
            } else {
                System.exit(0);
            }
        });
    }
}