import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;

public class ScoreManager {
    private int score;
    private Font font;
    private Color currentColor = Color.WHITE; 
    private long lastScoreTime; 
    private boolean scoreChanged = false;
    private Image bannerImage; 

    public ScoreManager() {
        this.score = 0;
        this.font = new Font("Comic Sans MS", Font.BOLD, 20);
        // Load the banner image
        this.bannerImage = Toolkit.getDefaultToolkit().getImage("images/banner.png");
    }

    // add amount to the score
    public void addScore(int amount) {
        this.score += amount;
        this.scoreChanged = true;
        this.lastScoreTime = System.currentTimeMillis();
        this.currentColor = Color.GREEN; 
    }

    
    private void updateScoreAppearance() {
        if (scoreChanged && (System.currentTimeMillis() - lastScoreTime > 500)) { 
            this.currentColor = Color.WHITE;
            this.scoreChanged = false;
        }
    }

    
    public void draw(Graphics2D g2) {
        updateScoreAppearance(); // Update the score's appearance
        
        // x position for the banner based on the text width and some padding
        String scoreText = "Score: $" + score;
        int textWidth = g2.getFontMetrics().stringWidth(scoreText);
        int xPosition = 62; 
        int yPosition = 37; //adjusted to align with the banner
        
        // the banner image first
        if (bannerImage != null) {
            g2.drawImage(bannerImage, 0, 0, null);
        }
        
        // score text on top of the banner
        g2.setFont(font);
        g2.setColor(currentColor);
        g2.drawString(scoreText, xPosition, yPosition);
    }


    public int getScore() {
        return score;
    }
}
