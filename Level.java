import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Color;

public class Level {
    private TileMap tileMap;
    private JFrame window;
    private TileMapManager tmm;
    private int currLevel;
    private SoundManager sm;
    private ScoreManager scoreManager;
    private boolean displayLevelUpMessage;
    private boolean displayWinMessage;
    private long messageDisplayTime;

    // Initialize levels
    public Level(JFrame window, ScoreManager scoreManager) {
        this.window = window;
        this.scoreManager = scoreManager;
        sm = SoundManager.getInstance();
        tmm = new TileMapManager(window, this.scoreManager);
        this.displayLevelUpMessage = false;
        this.displayWinMessage = false;
        this.messageDisplayTime = 0;
    }

    public void loadLevel(int level) {
        currLevel = level;
        String path;

        if(level == 1) {
            path = "maps/map1.txt";
        } else if(level == 2) {
            path = "maps/map2.txt";
        } else if(level == 3) {
            path = "maps/map3.txt";
        } else {
            path = "";
        }

        sm.stopAllSounds();
        sm.playSound(level == 1 ? "background" : "new", false);
        sm.playSound("background", true);

        try {
            tileMap = tmm.loadMap(path);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }

        // Reset message display settings whenever a new level is loaded
        displayLevelUpMessage = false; // Ensure it's set to false by default
        displayWinMessage = false;
        messageDisplayTime = System.currentTimeMillis();
    }

    public void draw(Graphics2D g) {
        tileMap.draw(g);
        if (displayLevelUpMessage) {
            if (System.currentTimeMillis() - messageDisplayTime < 3000) { // Display for 3 seconds
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("You Advanced to the New Level", window.getWidth() / 2 - 150, window.getHeight() / 2);
            } else {
                displayLevelUpMessage = false;
            }
        }
        if (displayWinMessage) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("You Win!", window.getWidth() / 2 - 75, window.getHeight() / 2 - 30);
            g.drawString("Press ESCAPE on your keyboard to quit", window.getWidth() / 2 - 220, window.getHeight() / 2 + 30);
        }
    }

    public void update() {
        tileMap.update();

        // Check if the player has reached the required score to advance levels or win the game
        if (currLevel < 3) {
            int scoreThreshold = currLevel == 1 ? 7500 : 9000;
            if (tileMap.getScoreManager().getScore() > scoreThreshold) {
                System.out.println("Next Level!");
                currLevel++;
                loadLevel(currLevel);
                if (currLevel == 2 || currLevel == 3) { // Set message display only when advancing to levels 2 and 3
                    displayLevelUpMessage = true;
                    messageDisplayTime = System.currentTimeMillis();
                }
            }
        } else if (currLevel == 3 && tileMap.getScoreManager().getScore() > 10000) {
            displayWinMessage = true; // Display win message
        }
    }

    public Player getPlayer() {
        return tileMap.getPlayer();
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public void playerDash() {
        tileMap.getPlayer().dash();
    }
}
