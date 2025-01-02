import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
public class EnemyCannon extends Cannon {
  

    public EnemyCannon(Image image, int x, int y, TileMap tm) {
        super(image, x, y, tm);
        this.image = image;
        this.x = x;
        this.y = y;
        this.tm = tm;

      
    }

    public void loadAnimations() {
        currentAnimation = new Animation(false);
		shootAnimation = new Animation(false);
        idleAnimation = new Animation(true);
;
        InputStream is = getClass().getResourceAsStream("images/enemy_peashooter_Attack_44x42.png");

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

        // Idle animation
        InputStream is2 = getClass().getResourceAsStream("images/enemy_peashooter_Idle_44x42.png");

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

    public void shootBullet(int offsetX, int offsetY) {
        int tileSize = TileMap.getTileSize();
        if (bullet == null) {
            //System.out.println("Bullet is null so Shooting bullet from shoot bullet-----------------------------------------");
           // Calculate the direction for the bullet based on mouse position
           int bulletDX = tm.getPlayer().getX() - (x + width / 2); // Adjust for cannons's position
           int bulletDY = tm.getPlayer().getY() - (y + height / 2); // Adjust for cannons's position
        
  
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
        bullet.setTargetLoc(tm.getPlayer().getX() + offsetX, tm.getPlayer().getY());
     }

     public Rectangle getAttackBounds() {
        return new Rectangle(
            x * tm.getTileSize() - 400, 
            y * tm.getTileSize() + tm.getOffsetY() - 32, 
            width + 800, 
            height + 128
        );
    }

    public Rectangle getBounds() {
        //bounds centered to sprite and below cannon so that player cant shoot it
        return new Rectangle(
            x * tm.getTileSize()  + 20, 
            y * tm.getTileSize() + tm.getOffsetY() + 32, 
            width - 32, 
            height - 32
        );
    }
    public void draw(Graphics2D g2, int offsetX, int offsetY) {

        //checking the players coordinates then facing that direction
        if (tm.getPlayer().getX() > x * tm.getTileSize()) {
            //System.out.println("Player is on the right side of the cannon" + tm.getPlayer().getX() + " and my x is" + x);
            direction = "Left";
        } else {
            //System.out.println("Player is on the left side of the cannon" + tm.getPlayer().getX() + " and my x is" + x);
            direction = "Right";
        }




        timeElapsed++;
        if (timeElapsed % 100 == 0 && tm.getPlayer().getBounds().intersects(getAttackBounds()))
        shootBullet(tm.getOffsetX(), tm.getOffsetY());
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

   
}