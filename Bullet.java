import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class Bullet {
    protected int x;
    protected int y;
    protected int targetX;
    protected int targetY;
    protected int dx;
    protected int dy;
    protected int width;
    protected int height;
    protected final int maxDistance;
    protected int distanceTraveled;
    // protected GamePanel gp;
    protected TileMap tm;
    protected JPanel panel;
    protected Image bulletImage;

    public Bullet(int x, int y, int dx, int dy, TileMap tm) {
        this.x = x - tm.getOffsetX();
        this.y = y ;
        this.dx = dx;
        this.dy = dy;
        this.width = 35; // Set bullet width
        this.height = 35; // Set bullet height
        this.maxDistance = 600; // Set the maximum distance the bullet can travel
        // this.gp = gp;
        this.tm = tm;
        this.distanceTraveled = 0;
        this.bulletImage = ImageManager.loadImage("images/bullet.png"); // Load bullet image
    }

    //check if about to collide with tile left or right
    public boolean isAboutToCollideWithTile(int newX, int newY) {
        //getting direction from velocity
        Point point = collidesWithTile(newX + width, newY);
        if (point != null) {
            //System.out.println("collided with tile for right at : " + point.x * 64 + " y: " + point.y + " Enemy  x: " + x + " y: " + y);
            return true;
        }
        Point point2 = collidesWithTile(newX, newY);
        if (point2 != null) {
            //System.out.println("collided with tile for left at x: " + point2.x * 64 + " y: " + point2.y + " Enemy  x: " + x + " y: " + y);
            return true;
        }
        return false;
    }



    public Point collidesWithTile(int newX, int newY) {

		int offsetY = tm.getOffsetY();
		int xTile = tm.pixelsToTiles(newX);
		int yTile = tm.pixelsToTiles(newY - offsetY);

		if (tm.getTile(xTile, yTile) != null) {
			Point tilePos = new Point(xTile, yTile);
			return tilePos;
		} else {
			return null;
		}
	}

    public void move() {
        //System.out.println("Bullet moving"+ "X: "+ x + "Y: "+ y + "dx: "+ dx + "dy: "+ dy);
        x += dx; // Move bullet horizontally
        y += dy; // Move bullet vertically
        int speed = 25; // Set bullet speed
        distanceTraveled += Math.abs(dx) + Math.abs(dy); // Update distance traveled

        //Check if bullet has almost travel the maximum distance
        if (distanceTraveled >= maxDistance - 100) {
            this.bulletImage = ImageManager.loadImage("images/explosion.gif"); // Load bullet image
        }
        
        // Check if the bullet has traveled the maximum distance
        if (distanceTraveled >= maxDistance || isAboutToCollideWithTile(x, y + 32)) {
            // Set the bullet to null to indicate it's ready to be shot again
            resetBullet();
        }

        // Adjust the bullet's direction towards the target
        double angle = Math.atan2(targetY - y, targetX - x - tm.getOffsetX());
        dx = (int) (speed * Math.cos(angle));
        dy = (int) (speed * Math.sin(angle));

        checkCollision();
        boolean collision = checkCollisionWithPlayer();
        if (collision) {
            tm.getPlayer().updateHealth(-50); // Reduce the player's health by 50
            resetBullet();
        }
    }
    public void resetBullet(){
        x = -100; // Move the bullet off-screen
        y = -100; // Move the bullet off-screen
        dx = 0; // Reset bullet velocity
        dy = 0; // Reset bullet velocity
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(bulletImage, x + tm.getOffsetX(), y, width, height, null); // Draw the bullet image
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x + tm.getOffsetX(), y, width, height);
    }

    public boolean checkCollision() {
        //check if hits enemy

        Rectangle bulletRect = new Rectangle(x, y, width, height);
        Enemy[] enemies = tm.getenemies(); // Get the array of enemies from the TileMap
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy.getBoundingRectangle().intersects(bulletRect)) {
                    enemy.updateHealth(40); // Reduce the enemy's health by 10
                    enemy.setX(enemy.getX() + -1 * enemy.getDirectionWeight() * enemy.getKnockbackDistance());
                    if (enemy instanceof BossEnemy) {
                        enemy.setAnimation();
                    }
                                        return true;
                }
            }
        }
        return false;
    }

    //check collision with player
    public boolean checkCollisionWithPlayer() {
        //check if hits player
        Rectangle bulletRect = new Rectangle(x, y, width, height);
        Player player = tm.getPlayer(); // Get the player from the TileMap
        if (player.getBounds().intersects(bulletRect)) {
            return true;
        }
        return false;
    }

    public boolean isOutOfBounds() {
        // Check if the bullet is out of bounds
        return x < 0 || y < 0 ;
        //return false;
    }

    //targeted mosuse controlled bullets
    public void setTargetLoc(int targetX,int targetY){
        //sets location
        this.targetX = targetX;
        this.targetY = targetY;

    }

    

}
