import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class PlayerBullet extends Bullet {
    
    
    public PlayerBullet(int x, int y, int dx, int dy, TileMap tm) {
        
        super(x, y + 20, dx ,dy, tm); // lowering bullet a bit to appear to come out of players slingshot

        this.bulletImage = ImageManager.loadImage("images/playerbullet.png"); // Load bullet image
        
    }

    


}
