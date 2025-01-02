import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Rectangle;
public class Objects {
    private Image image;
    public int x, y;
    private int width, height; 
    private boolean visible;
    private TileMap map; 

    public Objects(TileMap map, Image image, int x, int y, int width, int height) {
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
            g2.drawImage(image, 
                         x * map.getTileSize() + map.getOffsetX(),
                         y * map.getTileSize() + map.getOffsetY()+9,
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
