import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GUI extends JFrame {
    private static GUI instance;
    private int totalReadersCreated = 0;
    private int totalWritersCreated = 0;
    private double speedMultiplier = 1.0;
    private static volatile boolean paused = false;

    private JPanel panel;
    private JSlider speedSlider;
    private Timer animationTimer;

    static class Person {
        double x, y;
        double targetX, targetY;
        Color color1, color2;
        boolean isReader;
        int id;
        String state = "THINKING"; // "THINKING", "WAITING", "ACTIVE"
        float alpha = 0f;

        Person(double x, double y, boolean isReader, int id) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
            this.isReader = isReader;
            this.id = id;
            if (isReader) {
                color1 = new Color(0, 210, 255);
                color2 = new Color(58, 123, 213);
            } else {
                color1 = new Color(255, 65, 108);
                color2 = new Color(255, 75, 43);
            }
        }

        void update() {
            x = targetX;
            y = targetY;
            alpha = 1.0f;
        }

        void draw(Graphics2D g2) {
            Composite oldComp = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            
            GradientPaint gp = new GradientPaint((float)x, (float)y, color1, (float)x, (float)(y+30), color2);
            g2.setPaint(gp);
            
            g2.fillOval((int)x + 5, (int)y, 20, 20);
            g2.fill(new RoundRectangle2D.Double(x, y + 18, 30, 24, 12, 12));
            
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval((int)x + 5, (int)y, 20, 20);
            g2.draw(new RoundRectangle2D.Double(x, y + 18, 30, 24, 12, 12));
            
            // Draw Name ID under person
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String name = (isReader ? "Reader " : "Writer ") + id;
            g2.drawString(name, (int)x - 5, (int)y + 55);
            
            g2.setComposite(oldComp);
        }
    }

    private static Map<String, Person> entities = new ConcurrentHashMap<>();

    private GUI() {
        setTitle("Readers-Writers (Writers Priority) - Live Simulation");
        setSize(950, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(240, 242, 245));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Calculate counts dynamically from exact states
                int waitR = 0, waitW = 0, actR = 0, actW = 0;
                for (Person p : entities.values()) {
                    if (p.state.equals("WAITING")) { if (p.isReader) waitR++; else waitW++; }
                    else if (p.state.equals("ACTIVE")) { if (p.isReader) actR++; else actW++; }
                }

                drawSection(g2, "Thinking / Sleeping Processes", 50, 60, 830, 160, new Color(245, 245, 245));
                drawSection(g2, "Waiting Readers (Blue): " + waitR, 50, 250, 380, 160, new Color(225, 240, 255));
                drawSection(g2, "Waiting Writers (Red): " + waitW, 500, 250, 380, 160, new Color(255, 230, 235));
                drawSection(g2, "Critical Section", 280, 440, 380, 200, new Color(255, 255, 255));
                
                g2.setColor(new Color(60, 60, 60));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                g2.drawString("Total Created Readers: " + totalReadersCreated, 50, 35);
                g2.drawString("Total Created Writers: " + totalWritersCreated, 520, 35);

                for (Person p : entities.values()) {
                    p.draw(g2);
                }
            }
            
            private void drawSection(Graphics2D g2, String title, int x, int y, int w, int h, Color fill) {
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(x + 5, y + 5, w, h, 25, 25);
                g2.setColor(fill);
                g2.fillRoundRect(x, y, w, h, 25, 25);
                g2.setColor(new Color(180, 190, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, w, h, 25, 25);
                g2.setColor(new Color(50, 50, 50));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString(title, x + 20, y + 30);
            }
        };
        add(panel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        controlPanel.setBackground(Color.WHITE);
        JButton restartBtn = new JButton("Restart Simulation");
        restartBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        restartBtn.setBackground(new Color(40, 167, 69));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setFocusPainted(false);
        restartBtn.addActionListener(e -> {
            setPaused(false);
            MainMethod.startSimulation();
        });
        
        JButton pauseBtn = new JButton("Pause");
        pauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pauseBtn.setBackground(new Color(255, 193, 7));
        pauseBtn.setForeground(Color.BLACK);
        pauseBtn.setFocusPainted(false);
        pauseBtn.addActionListener(e -> {
            paused = !paused;
            pauseBtn.setText(paused ? "Continue" : "Pause");
        });
        
        controlPanel.add(restartBtn);
        controlPanel.add(pauseBtn);
        JLabel speedLabel = new JLabel("Simulation Speed:");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        speedSlider = new JSlider(10, 300, 100); 
        speedSlider.setBackground(Color.WHITE);
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(10, new JLabel("0.1x"));
        labelTable.put(100, new JLabel("1x"));
        labelTable.put(200, new JLabel("2x"));
        labelTable.put(300, new JLabel("3x"));
        speedSlider.setLabelTable(labelTable);

        speedSlider.addChangeListener(e -> {
            speedMultiplier = speedSlider.getValue() / 100.0;
        });

        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        add(controlPanel, BorderLayout.SOUTH);

        animationTimer = new Timer(16, e -> {
            for (Person p : entities.values()) p.update();
            panel.repaint();
        });
        animationTimer.start();
    }

    private void recalculateTargets() {
        int rThink = 0, wThink = 0, rWait = 0, wWait = 0, rAct = 0, wAct = 0;
        
        // Ensure deterministic display order by fixed IDs to prevent layout glitches (random swapping)
        for (int i = 1; i <= Math.max(8, totalReadersCreated); i++) {
            Person p = entities.get("R" + i);
            if (p != null) {
                if (p.state.equals("THINKING")) {
                    p.targetX = 65 + (rThink % 8) * 60; p.targetY = 110 + (rThink / 8) * 55; rThink++;
                } else if (p.state.equals("WAITING")) {
                    p.targetX = 65 + (rWait % 6) * 55; p.targetY = 295 + ((rWait / 6) % 2) * 60; rWait++;
                } else if (p.state.equals("ACTIVE")) {
                    p.targetX = 300 + (rAct % 6) * 55; p.targetY = 485 + ((rAct / 6) % 3) * 55; rAct++;
                }
            }
        }
        
        for (int i = 1; i <= Math.max(8, totalWritersCreated); i++) {
            Person p = entities.get("W" + i);
            if (p != null) {
                if (p.state.equals("THINKING")) {
                    p.targetX = 545 + (wThink % 8) * 60; p.targetY = 110 + (wThink / 8) * 55; wThink++;
                } else if (p.state.equals("WAITING")) {
                    p.targetX = 515 + (wWait % 6) * 55; p.targetY = 295 + ((wWait / 6) % 2) * 60; wWait++;
                } else if (p.state.equals("ACTIVE")) {
                    p.targetX = 450 + (wAct % 3) * 60; p.targetY = 510 + ((wAct / 3) % 2) * 55; wAct++;
                }
            }
        }
    }

    public static void setState(boolean isReader, int id, String state) {
        if (instance == null) return;
        SwingUtilities.invokeLater(() -> {
            String key = (isReader ? "R" : "W") + id;
            Person p = entities.get(key);
            if (p == null) {
                p = new Person(isReader ? 100 : 800, 110, isReader, id);
                entities.put(key, p);
            }
            p.state = state;
            instance.recalculateTargets();
            instance.panel.repaint();
        });
    }

    public static void setCreatedProcesses(int r, int w) {
        if (instance == null) return;
        SwingUtilities.invokeLater(() -> {
            instance.totalReadersCreated = r;
            instance.totalWritersCreated = w;
        });
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(boolean p) {
        paused = p;
    }

    public static double getSpeedMultiplier() {
        return instance != null ? instance.speedMultiplier : 1.0;
    }

    public static void init() {
        if (instance == null) {
            SwingUtilities.invokeLater(() -> {
                instance = new GUI();
                instance.setVisible(true);
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                entities.clear();
                instance.recalculateTargets();
                instance.panel.repaint();
            });
        }
    }
}
