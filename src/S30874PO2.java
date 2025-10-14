
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class S30874PO2 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Autoslalom");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocation(350, 100);

        Board board = new Board();
        frame.add(board);
        frame.setVisible(true);
    }
}
//    package p02.game;
    class Board extends JPanel implements KeyListener, Runnable {
        private int[] track;
        private int carPosition;
        private boolean running;
        private Thread gameThread;
        private int tickInterval;
        private Random random;
        private Image backgroundImage;
        private Image carImage;
        private Image[] obstacleImages;
        private int[] previousTrack;

        private RoadSide roadSide1;
        private RoadSide roadSide2;
        private SevenSegmentDigit units;
        private SevenSegmentDigit tens;
        private SevenSegmentDigit hundreds;

        private int[][] rowPositions = {
                {160, 380},  // Pos1
                {240, 310},  // Pos2
                {350, 230},  // Pos3
                {420, 170},  // Pos4
                {500, 90},   // Pos5
                {560, 40},   // Pos6
        };

        public Board() {
            track = new int[7];
            previousTrack = new int[7];
            carPosition = 1;

            running = false;
            tickInterval = 500;
            random = new Random();
            setFocusable(true);
            addKeyListener(this);

            try {
                backgroundImage = ImageIO.read(new File("C:\\Users\\Yevhen\\Desktop\\Java\\Autoslalom\\src\\Tor.png"));
                carImage = ImageIO.read(new File("C:\\Users\\Yevhen\\Desktop\\Java\\Autoslalom\\src\\Car.png"));
                obstacleImages = new Image[6];
                for (int i = 0; i < 6; i++) {
                    obstacleImages[i] = ImageIO.read(new File("C:\\Users\\Yevhen\\Desktop\\Java\\Autoslalom\\src\\Pos" + (i + 1) + ".png"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            units = new SevenSegmentDigit();
            tens = new SevenSegmentDigit();
            hundreds = new SevenSegmentDigit();
            tens.setLocation(100, 100);

            units.addListener((e -> tens.plusOne()));
            tens.addListener((e -> hundreds.plusOne()));

            hundreds.setBounds(80, 10, 50, 100);
            tens.setBounds(180, 10, 50, 100);
            units.setBounds(280, 10, 50, 100);
            add(hundreds);
            add(tens);
            add(units);

            roadSide2 = new RoadSide(3);
            roadSide1 = new RoadSide(0);
            setLayout(null);
            roadSide1.setBounds(-20, 0, 800, 600);
            roadSide2.setBounds(-20, 0, 800, 600);
            add(roadSide1);
            add(roadSide2);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            if (carImage != null) {
                int carX = carPosition * ((getWidth() / 3) - 75);
                int carY = getHeight() - carImage.getHeight(null) + 65;
                g.drawImage(carImage, carX, carY, this);
            }


            for (int i = 1; i < 7; i++) {
                if (track[i] != -1 && obstacleImages[i - 1] != null) {
                    int obstacleX = 0;
                    switch (6 - i) {
                        case 5 -> obstacleX = rowPositions[i - 1][0] + track[i] * ((getWidth() / 3) - 100);
                        case 4 -> obstacleX = rowPositions[i - 1][0] + track[i] * ((getWidth() / 3) - 125);
                        case 3 -> obstacleX = rowPositions[i - 1][0] + track[i] * ((getWidth() / 3) - 145);
                        case 2 -> obstacleX = rowPositions[i - 1][0] + track[i] * ((getWidth() / 3) - 170);
                        case 1 -> obstacleX = rowPositions[i - 1][0] + track[i] * ((getWidth() / 3) - 195);
                        case 0 -> obstacleX = rowPositions[i - 1][0] + track[i] * ((getWidth() / 3) - 220);
                    }
                    int obstacleY = rowPositions[i - 1][1];
                    g.drawImage(obstacleImages[i - 1], obstacleX, obstacleY, this);
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_A) {
                carPosition = Math.max(0, carPosition - 1);
            } else if (e.getKeyCode() == KeyEvent.VK_D) {
                carPosition = Math.min(2, carPosition + 1);
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                if (!running) {
                    units.Reset();
                    tens.Reset();
                    hundreds.Reset();
                    startGame();
                }
            }
            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        private void startGame() {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        private void updateScoreDisplay() {
            units.setVisible(true);
            tens.setVisible(tens.getValue() >= 1 || (tens.getValue() >= 0 && hundreds.getValue() >= 1));
            hundreds.setVisible(hundreds.getValue() >= 1);

            units.repaint();
            tens.repaint();
            hundreds.repaint();
        }


        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(tickInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                moveObstacles();
                if (checkCollision()) {
                    running=false;
                    resetGame();
                } else {
                    increaseDifficulty();
                    units.plusOne();
                    updateScoreDisplay();
                }
                repaint();
            }
        }

        private void moveObstacles() {
            roadSide1.nextImage();
            roadSide2.nextImage();
            System.arraycopy(track, 0, previousTrack, 0, 7);

            System.arraycopy(track, 1, track, 0, 6);
            track[6] = generateObstacle();
        }

        private int generateObstacle() {
            int pos;

            int zeros = 0;
            if (hundreds.getValue() == 0) {
                zeros++;
            }
            if (tens.getValue() == 0) {
                zeros++;
            }
            if (units.getValue() == 0) {
                zeros++;
            }

            int prog = 4 - zeros;

            do {
                pos = random.nextInt(3);
            } while (!isObstacleValid(pos) || random.nextInt(prog) != 0);

            return pos;
        }

        private boolean isObstacleValid(int pos) {
            if (track[1] == pos) {
                return false;
            }
            if (track[2] == pos && previousTrack[1] == pos) {
                return false;
            }
            return true;
        }

        private boolean checkCollision() {
            return track[0] == carPosition;
        }

        private void resetGame() {
            carPosition = 1;
            track = new int[7];
            for (int i = 0; i < track.length; i++) {
                track[i] = -1;
            }
            tickInterval = 500;
            repaint();
        }

        private void increaseDifficulty() {
            if (tickInterval > 200) {
                tickInterval -= 5;
            }
        }
    }

// package p02.pres;
class SevenSegmentDigit extends JPanel implements ActionListener {
    private int value;
    private List<ActionListener> listeners;

    public SevenSegmentDigit() {
        value = 0;
        listeners = new ArrayList<>();
        setPreferredSize(new Dimension(50, 100));
        setOpaque(false);
    }

    public void addListener(ActionListener listener) {
        listeners.add(listener);
    }

    private void fireEvent(ActionEvent event) {
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    public void setValue(int value) {
        this.value = value;
        repaint();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        drawDigit(g, value);
    }

    private void drawDigit(Graphics g, int digit) {
        boolean[] segments = getSegments(digit);
        int width = getWidth();
        int height = getHeight();
        int segmentWidth = width / 5;
        int segmentHeight = height / 10;

        if (segments[0]) g.fillRect(segmentWidth, 0, width - 2 * segmentWidth, segmentHeight);
        if (segments[3]) g.fillRect(segmentWidth, height / 2 - segmentHeight / 2, width - 2 * segmentWidth, segmentHeight);
        if (segments[6]) g.fillRect(segmentWidth, height - segmentHeight, width - 2 * segmentWidth, segmentHeight);
        if (segments[1]) g.fillRect(width - segmentWidth, segmentHeight, segmentWidth, height / 2 - segmentHeight);
        if (segments[2]) g.fillRect(width - segmentWidth, height / 2, segmentWidth, height / 2 - segmentHeight);
        if (segments[4]) g.fillRect(0, height / 2, segmentWidth, height / 2 - segmentHeight);
        if (segments[5]) g.fillRect(0, segmentHeight, segmentWidth, height / 2 - segmentHeight);
    }

    private boolean[] getSegments(int digit) {
        boolean[][] segmentData = {
                {true, true, true, false, true, true, true}, // 0
                {false, true, true, false, false, false, false}, // 1
                {true, true, false, true, true, false, true}, // 2
                {true, true, true, true, false, false, true}, // 3
                {false, true, true, true, false, true, false}, // 4
                {true, false, true, true, false, true, true}, // 5
                {true, false, true, true, true, true, true}, // 6
                {true, true, true, false, false, false, false}, // 7
                {true, true, true, true, true, true, true}, // 8
                {true, true, true, true, false, true, true} // 9
        };
        return segmentData[digit];
    }

    public void Start() {
        setValue(0);
        fireEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "StartEvent"));
    }

    public void plusOne() {
        value++;
        if (value > 9) {
            value = 0;
            fireEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "PlusOneEvent"));
        }
        repaint();
    }

    public void Reset() {
        setValue(0);
        fireEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ResetEvent"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("PlusOneEvent")) {
            plusOne();
        } else if (e.getActionCommand().equals("ResetEvent")) {
            Reset();
        } else if (e.getActionCommand().equals("StartEvent")) {
            Start();
        }
    }
}


// package p02.pres;
class RoadSide extends JPanel {
    private Image[] roadSideImages;
    private int currentImageIndex;

    public RoadSide(int startPos) {
        roadSideImages = new Image[6];
        currentImageIndex = startPos;

        try {
            for (int i = 0; i < 6; i++) {
                roadSideImages[i] = ImageIO.read(new File("C:\\Users\\Yevhen\\Desktop\\Java\\Autoslalom\\src\\RoadSide" + (i + 1) + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setPreferredSize(new Dimension(800, 600));
        setOpaque(false);
    }

    public void nextImage() {
        currentImageIndex = (currentImageIndex + 1) % roadSideImages.length;
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (roadSideImages[currentImageIndex] != null) {
            g.drawImage(roadSideImages[currentImageIndex], 0, 0, getWidth(), getHeight(), this);
        }
    }
}






