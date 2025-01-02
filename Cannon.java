import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
public class Cannon {
    protected Bullet bullet;
    protected Image image;
    protected int x, y;
    protected boolean visible;
    protected int width;
    protected int height;
    protected TileMap tm;
    protected String direction;
    protected BufferedImage img;
    protected Animation shootAnimation;
    protected Animation currentAnimation;
    protected Animation idleAnimation;
    protected int timeElapsed = 0;  

    public Cannon(Image image, int x, int y, TileMap tm) {
        this.bullet = null;
        this.image = image;
        this.x = x;
        this.y = y;
        this.visible = true;
        this.tm = tm;


        // width = image.getWidth(null);
        // height = image.getHeight(null);
        width = 64;
        height = 64;
        direction = "Right";
        loadAnimations();
        currentAnimation = idleAnimation;
    }

    public void loadAnimations() {
        currentAnimation = new Animation(false);
		shootAnimation = new Animation(false);
        idleAnimation = new Animation(true);
;
        InputStream is = getClass().getResourceAsStream("images/peashooter_Attack_44x42.png");

        try{
            img = ImageIO.read(is);
    }
        catch(IOException e){
            e.printStackTrace();
        }

        BufferedImage rightwalkani[] = new BufferedImage[7]; 

        for (int i = 0; i < 7; i++) {
            rightwalkani[i] = img.getSubimage(i * 44, 0, 44, 40);
        }
		

		// Add right images to right animation
		for (int i = 0; i < 7; i++) {
			shootAnimation.addFrame(rightwalkani[i], 25);
		}

        //Idle animation
        InputStream is2 = getClass().getResourceAsStream("images/peashooter_Idle_44x42.png");

        try{
            img = ImageIO.read(is2);
    }
        catch(IOException e){
            e.printStackTrace();
        }


        BufferedImage idleani[] = new BufferedImage[11];

        for (int i = 0; i < 11; i++) {
            idleani[i] = img.getSubimage(i * 44, 0, 44, 40);
        }

        // Add right images to right animation
		for (int i = 0; i < 11; i++) {
			idleAnimation.addFrame(idleani[i], 50);
		}
        
        this.currentAnimation = idleAnimation;
        this.currentAnimation.start();

        
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    //set direction based on mouse position
    public void setDirection(int mouseX) {
        if (mouseX > x * tm.getTileSize()) {
            this.direction = "Left";
        } else {
            this.direction = "Right";
        }
    }

    public void draw(Graphics2D g2, int offsetX, int offsetY) {
        if (visible) {
            int tileSize = TileMap.getTileSize();
           
            // if (currentAnimation != null && !currentAnimation.isStillActive())
            // currentAnimation.start();
            
            if(currentAnimation != null)
            this.currentAnimation.update();

            //if current animation is shooting animation and is at last frame then set it to idle animation
            if(currentAnimation == shootAnimation && currentAnimation.isLastFrame()){
                //this.currentAnimation.stop();
                this.currentAnimation = idleAnimation;
                this.currentAnimation.start();
            }
            
            
            if (direction == "Right")
            g2.drawImage(currentAnimation.getImage(), x * tileSize + offsetX, y * tileSize + offsetY,64,64, null);
            else
            g2.drawImage(currentAnimation.getImage(), x * tileSize  + width + offsetX, y * tileSize + offsetY, -width,height, null);
            //System.out.println("Drawing Cannon at---: " + x + ", " + y);

        }
    }

    public void shootBullet(int mouseX, int mouseY,int offsetX, int offsetY) {
        int tileSize = TileMap.getTileSize();
        if (bullet == null) {
            //System.out.println("Bullet is null so Shooting bullet from shoot bullet-----------------------------------------");
           // Calculate the direction for the bullet based on mouse position
           int bulletDX = mouseX - (x + width / 2); // Adjust for cannons's position
           int bulletDY = mouseY - (y + height / 2); // Adjust for cannons's position
        
  
           // Normalize the direction vector
           double magnitude = Math.sqrt(bulletDX * bulletDX + bulletDY * bulletDY);
           bulletDX = (int) (bulletDX / magnitude);
           bulletDY = (int) (bulletDY / magnitude);
  
           // Set initial position of the bullet to be at the center of the cannons
           //if direction is right then increment x to the next tile
           if (direction == "Right")   {           
           //set currentanimation to shoot animation
              this.currentAnimation = shootAnimation;
              this.currentAnimation.start();
            bullet = new Bullet(x * tileSize + offsetX - 40, y * tileSize + offsetY, bulletDX, bulletDY, tm);
        }else{
            //set currentanimation to shoot animation
            this.currentAnimation = shootAnimation;
            this.currentAnimation.start();
                bullet = new Bullet(x * tileSize + offsetX + 64, y * tileSize + offsetY, bulletDX, bulletDY, tm);
            }
        }
        bullet.setTargetLoc(mouseX, mouseY);
     }



    public void updateBullet() {
    if (bullet != null) {
        bullet.move();
        if (bullet.isOutOfBounds()) {
            bullet = null; // Reset bullet when it goes out of bounds
        }
    }
    }

    public void drawBullet(Graphics2D g2) {
    if (bullet != null) {
        bullet.draw(g2);
    }
    }

    public Bullet getBullet() {
    return bullet;
    }

    
    public Rectangle getBounds() {
        return new Rectangle(
            x * tm.getTileSize() , 
            y * tm.getTileSize() + tm.getOffsetY() - 32, 
            width, 
            height - 32
        );
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}