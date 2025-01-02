import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Rectangle;
public class Ammobox {
    private Image image;
    public int x, y;
    private int width, height;  // Assuming these are dimensions of the collectible
    private boolean visible;
    private TileMap map;  // Reference to the TileMap
    private float animationState = 0; // A variable to track the animation state for visual effects

    public Ammobox(TileMap map, Image image, int x, int y, int width, int height) {
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
            
            int animationOffset = (int)(Math.sin(animationState) * 5); 
            animationState += 0.1; 

            
            g2.drawImage(image, 
                         x * map.getTileSize() + map.getOffsetX(),
                         y * map.getTileSize() + map.getOffsetY() - animationOffset,
                         null);
            
            
            
            
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}