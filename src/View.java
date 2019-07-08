import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 3 Chars Kaleidoscope
 *
 * @author Leonardo Ono (ono.leo@gmail.com)
 */
public class View extends JPanel implements KeyListener {

    private final BufferedImage offscreen;
    private final BufferedImage offscreen2;
    private final Timer timer;
    private Font font;
    private final Font titleFont = new Font("arial", Font.BOLD, 80);
    private final Point2D.Double[] triangle = new Point2D.Double[3];
    private final Polygon triangleClip = new Polygon();
    private String kaleidoscopeTitle;
    private final String[] chars = new String[3];
    private final Point[] charPositions = { new Point(), new Point(), new Point() };
    private double angle1;
    private double angle2;
    private double scale;

    public View() {
        final double triangleSide = 100;
        triangle[0] = new Point2D.Double(1 * triangleSide, 0);
        triangle[1] = new Point2D.Double(-0.5 * triangleSide, -0.86603 * triangleSide);
        triangle[2] = new Point2D.Double(-0.5 * triangleSide, 0.86603 * triangleSide);
        triangleClip.addPoint((int) (triangle[0].x + 1), (int) (triangle[0].y));
        triangleClip.addPoint((int) (triangle[1].x - 1), (int) (triangle[1].y - 2));
        triangleClip.addPoint((int) (triangle[2].x - 1), (int) (triangle[2].y + 2));

        offscreen = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        offscreen2 = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);

        generateRandomKaleidoscope();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 100, 1000 / 20);

        addKeyListener(this);
        setPreferredSize(new Dimension(800, 600));

    }

    private void update() {
        angle1 += 0.01875;
        angle2 += 0.00750;
        scale = 1.9 + 0.5 * Math.sin(Math.cos(angle2 * 30.0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.

        Graphics2D goffscr = (Graphics2D) offscreen.getGraphics();
        drawKaleidoscope(goffscr);
        drawKaleidoscopeTitle(goffscr);
        goffscr.dispose();

        Graphics2D goffscr2 = (Graphics2D) offscreen2.getGraphics();
        goffscr2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        goffscr2.drawImage(offscreen,0, 0, 400, 300, 0, 0, 800, 600, null);
        goffscr2.dispose();

        g.drawImage(offscreen2, 0, 0, 800, 600, 0, 0, 400, 300, null);
    }

    private void drawKaleidoscopeTitle(Graphics2D g) {
        g.setFont(titleFont);
        String title = "\"" + kaleidoscopeTitle + "\"";
        FontMetrics fm = g.getFontMetrics();
        int titleX = -360;
        int titleY = 300 - fm.getHeight() + fm.getHeight() / 2;
        g.setColor(Color.BLACK);
        for (int y = -6; y <= 6; y++) {
            for (int x = -6; x <= 6; x++) {
                g.drawString(title, titleX + x, titleY + y);
            }
        }
        g.setColor(Color.WHITE);
        g.drawString(title, titleX, titleY);
    }

    private void generateRandomKaleidoscope() {
        font = new Font("Arial", Font.BOLD, (int) (120 + 50 * Math.random()));
        for (int i = 0; i < 3; i++) {
            chars[i] = "" + (char) (33 + 94 * Math.random());
            charPositions[i].x = (int) (- 80 * Math.random());
            charPositions[i].y = (int) (80 * Math.random() - 20);
        }
        kaleidoscopeTitle = chars[0] + chars[1] + chars[2];
    }

    private void drawKaleidoscope(Graphics2D g) {
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, 800, 600);
        g.translate(400, 300);
        for (int y = -260; y <= 260; y += 260) {
            for (int x = -600; x <= 300; x += 300) {
                if (y != 0 && x == -600) continue;
                drawHexagon(g, y != 0 ? x - 150 : x, y);
            }
        }
    }

    private void drawHexagon(Graphics2D g, double px, double py) {
        for (int i = 0; i < 2; i++) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(px, py);
            g2d.scale(1, i == 0 ? -1 : 1);
            drawHalfHexagon(g2d, i == 0 ? 0 : 60);
            g2d.dispose();
        }
    }

    private void drawHalfHexagon(Graphics2D g, double firstAngle) {
        for (int i = 0; i < 3; i ++) {
            g.translate(triangle[0].x, triangle[0].y);
            g.rotate(Math.toRadians(i == 0 ? firstAngle : 120));
            g.translate(-triangle[0].x, -triangle[0].y);
            drawTriangle(g);
        }
    }

    private void drawTriangle(Graphics2D g) {
        g = (Graphics2D) g.create();

        g.setFont(font);
        g.setClip(triangleClip);

        g.scale(scale, scale);
        g.rotate(angle1);

        g.setColor(Color.GRAY);
        g.drawString(chars[0], charPositions[0].x, charPositions[0].y);

        g.rotate(-angle1);
        g.rotate(-angle2);
        g.setColor(Color.WHITE);
        g.drawString(chars[1], charPositions[1].x, charPositions[1].y);

        g.rotate(+angle1);
        g.rotate(+angle2);
        g.setColor(Color.DARK_GRAY);
        g.drawString(chars[2], charPositions[2].x, charPositions[2].y);

        g.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            generateRandomKaleidoscope();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View view = new View();
            JFrame frame = new JFrame();
            frame.setTitle("Java 3 Chars Kaleidoscope - "
                    + "Press SPACE key to generate next random kaleidoscope");


            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            view.requestFocus();
        });
    }

}
