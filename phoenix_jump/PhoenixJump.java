import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*; 
import java.io.File;
import java.io.IOException;

public class PhoenixJump extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image[] birdImages; // Array to hold phoenix stances
    Image backgroundImg;
    Image topPipeImg;
    Image bottomPipeImg;
    int currentImageIndex = 0; 

    // Background animation
    int backgroundX = 0; 
    int backgroundScrollSpeed = 2;
    int backgroundWidth; 

    // Bird properties
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 50; 
    int birdHeight = 40;
    double birdRotation = 0; // Rotation angle in radians

    // Audio
    Clip backgroundMusicClip; // Clip for background music

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe properties
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    Timer animationTimer; 
    boolean gameOver = false;
    boolean paused = true; 
    double score = 0;

    PhoenixJump() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        birdImages = new Image[] {
            new ImageIcon(getClass().getResource("./phoenix1.png")).getImage(),
            new ImageIcon(getClass().getResource("./phoenix2.png")).getImage(),
            new ImageIcon(getClass().getResource("./phoenix3.png")).getImage()
        };
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        backgroundWidth = backgroundImg.getWidth(null); // Set background width dynamically
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImages[currentImageIndex]);
        pipes = new ArrayList<>();

        // Pipe placement timer
        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.setInitialDelay(1500);
        placePipeTimer.start();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        // Animation timer
        animationTimer = new Timer(200, e -> animateBird());
        animationTimer.start();

        // Start background music
        playBackgroundMusic("./riseofphoenix.wav");
    }

    void playBackgroundMusic(String filePath) {
        try {
            File musicFile = new File(getClass().getResource(filePath).getPath());
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioStream);

            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); 
            backgroundMusicClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
          
        }
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    void animateBird() {
        currentImageIndex = (currentImageIndex + 1) % birdImages.length; // Cycle through images
        bird.img = birdImages[currentImageIndex];

        // Adjust bird direction based on velocity
        if (velocityY < 0) {
            birdRotation = Math.toRadians(-20); // Rotate up
        } else {
            birdRotation = Math.toRadians(20); // Rotate down
        }
        repaint(); 
    }

    void scrollBackground() {
        backgroundX -= backgroundScrollSpeed; // Move the background to the left
        if (backgroundX <= -backgroundWidth) {
            backgroundX = 0; 
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw scrolling background
        g2d.drawImage(backgroundImg, backgroundX, 0, backgroundWidth, boardHeight, null);
        g2d.drawImage(backgroundImg, backgroundX + backgroundWidth, 0, backgroundWidth, boardHeight, null);

        // Draw rotated bird
        g2d.rotate(birdRotation, bird.x + bird.width / 2, bird.y + bird.height / 2); // Rotate around the bird's center
        g2d.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        g2d.rotate(-birdRotation, bird.x + bird.width / 2, bird.y + bird.height / 2); // Reset rotation

        // Draw pipes
        for (Pipe pipe : pipes) {
            g2d.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score display
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.PLAIN, 32));

        if (gameOver) {
            g2d.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g2d.drawString(String.valueOf((int) score), 10, 35);
        }

        // Pause text
        if (paused) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Paused", boardWidth / 2 - 60, boardHeight / 2);
        }
    }

    public void move() {
        if (!paused) {
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y, 0);

            // Move pipes
            for (Pipe pipe : pipes) {
                pipe.x += velocityX;

                if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                    score += 0.5;
                    pipe.passed = true;
                }

                if (collision(bird, pipe)) {
                    gameOver = true;
                }
            }

            if (bird.y > boardHeight) {
                gameOver = true;
            }

            // Scroll background
            scrollBackground();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) {
            move();
            repaint();
        }
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            animationTimer.stop();
            stopBackgroundMusic(); // Stop music when game ends
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!paused) {
                velocityY = -9;
            }

            if (gameOver) {
                // Restart game
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                placePipeTimer.restart();
                gameLoop.restart();
                animationTimer.restart();
                playBackgroundMusic("./background_music.wav"); // Restart music
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = !paused; // Toggle pause by pressing 'P'
            if (paused) {
                animationTimer.stop();
                backgroundMusicClip.stop(); // Pause music
            } else {
                animationTimer.start();
                backgroundMusicClip.start(); // Resume music
            }
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
