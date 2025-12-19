import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class WelcomeScreen extends JDialog {

    private boolean startGame = false;

    public WelcomeScreen(JFrame parent) {
        super(parent, "Welcome", true);

        SoundManager.playSound("start.wav");
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(65, 105, 225, 50));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(65, 105, 225));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel titleLabel1 = new JLabel("🍄 SUPER MARIO 🍄");
        titleLabel1.setFont(new Font("Monospaced", Font.BOLD, 36));
        titleLabel1.setForeground(new Color(255, 253, 150));
        titleLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel2 = new JLabel("MAZE QUEST");
        titleLabel2.setFont(new Font("Monospaced", Font.BOLD, 42));
        titleLabel2.setForeground(Color.WHITE);
        titleLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(titleLabel1);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(titleLabel2);
        mainPanel.add(Box.createVerticalStrut(35));

        JLabel subtitleLabel = new JLabel("⭐ Path Finding Visualization ⭐");
        subtitleLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(172, 209, 175));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(45));

        JButton playButton = createPlayButton();
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> {
            startGame = true;
            dispose();
        });
        mainPanel.add(playButton);

        mainPanel.add(Box.createVerticalStrut(20));

        JButton exitButton = createExitButton();
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> {
            startGame = false;
            dispose();
        });
        mainPanel.add(exitButton);

        mainPanel.add(Box.createVerticalStrut(15));

        JLabel footerLabel = new JLabel("Press PLAY to start your adventure!");
        footerLabel.setFont(new Font("Monospaced", Font.ITALIC, 14));
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(footerLabel);

        add(mainPanel, BorderLayout.CENTER);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private JButton createPlayButton() {
        JButton btn = new JButton("🎮 PLAY GAME");
        btn.setFont(new Font("Monospaced", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setMaximumSize(new Dimension(220, 50));

        Color buttonColor = new Color(172, 209, 175);

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                AbstractButton button = (AbstractButton) c;
                int width = button.getWidth();
                int height = button.getHeight();

                Color currentColor = button.getModel().isRollover() ? buttonColor.brighter() : buttonColor;
                if (button.getModel().isPressed()) {
                    currentColor = buttonColor.darker();
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, brighten(currentColor, 0.4f),
                        0, height, currentColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 15, 15);

                GradientPaint highlight = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 120),
                        0, height / 2, new Color(255, 255, 255, 0)
                );
                g2d.setPaint(highlight);
                g2d.fillRoundRect(0, 0, width, height / 2, 15, 15);

                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 15, 15);

                super.paint(g, c);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
        });

        return btn;
    }

    private JButton createExitButton() {
        JButton btn = new JButton("❌ EXIT");
        btn.setFont(new Font("Monospaced", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setMaximumSize(new Dimension(130, 38));

        Color buttonColor = new Color(255, 127, 127);

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                AbstractButton button = (AbstractButton) c;
                int width = button.getWidth();
                int height = button.getHeight();

                Color currentColor = button.getModel().isRollover() ? buttonColor.brighter() : buttonColor;
                if (button.getModel().isPressed()) {
                    currentColor = buttonColor.darker();
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, brighten(currentColor, 0.4f),
                        0, height, currentColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 12, 12);

                GradientPaint highlight = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 120),
                        0, height / 2, new Color(255, 255, 255, 0)
                );
                g2d.setPaint(highlight);
                g2d.fillRoundRect(0, 0, width, height / 2, 12, 12);

                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
        });

        return btn;
    }

    private Color brighten(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }

    public boolean isStartGame() {
        return startGame;
    }

    public static boolean showWelcome(JFrame parent) {
        WelcomeScreen welcome = new WelcomeScreen(parent);
        welcome.setVisible(true);
        return welcome.isStartGame();
    }
}
