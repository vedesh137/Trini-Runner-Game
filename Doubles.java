import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Rectangle;
public class Doubles {
    private Image image;
    public int x, y;
    private int width, height;
    private boolean visible;
    private TileMap map; 
    private float animationState = 0; // for a slower or faster animation

    public Doubles(TileMap map, Image image, int x, int y, int width, int height) {
        this.map = map;
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
    }

    public Rectangle getBounds() {
        return new Rectangle(
            x * map.getTileSize() , 
            y * map.getTileSize() + map.getOffsetY(), 
            width, 
            height
        );
    }

    public void draw(Graphics2D g2) {
        if (visible) {
            // floating effect
            int animationOffset = (int)(Math.sin(animationState) * 5); // 5 pixel height difference
            animationState += 0.1; 

            // Draw the collectible image with animation
            g2.drawImage(image, 
                         x * map.getTileSize() + map.getOffsetX(),
                         y * map.getTileSize() + map.getOffsetY() - animationOffset,
                         null);
            
            Rectangle bounds = getBounds();
             }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}