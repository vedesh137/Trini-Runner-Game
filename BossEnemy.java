import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class BossEnemy extends Enemy {
    public BossEnemy(JFrame window, TileMap tm) {
        super(window, tm);
        tileMap = tm;	
        this.window = window;

        //Loads the enemy's sprites.
        loadAnimations();

        //Sets the current animations.
        currAnimation = getcurrAnimation();

        //Defines enemy parameters.
        x = 0;
        y = 0;
        Speed = 4;
        currSpeed = Speed;
        knockbackDistance = 10;
        detectionRange = 800;
        ENTITY_DEFAULT_HEALTH = 2000;
        health = ENTITY_DEFAULT_HEALTH;

        this.width = 128;
        this.height = 128;
        healthbarWidth = 300;

       // EnemyOffsetY = 0;
        
    }

   

     // Loads the animaions by cutting up strip file.
     public void loadAnimations() {

        walkAnimationleft = new Animation(false);
        takeDamageLeft = new Animation(false);
        attackAnimationleft = new Animation(false);
        deathAnimation = new Animation(false);

        // Load Images for Left attack using submiage
        // cutting the image from the sprite sheet
        InputStream is = getClass().getResourceAsStream("images/boss_atlas.png");

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // attack animation left
        BufferedImage takeDamage[] = new BufferedImage[5];

        for (int i = 0; i < 5; i++) {
            takeDamage[i] = img.getSubimage(i * 288, 3 * 160, 288, 160);
        }

        for (int i = 0; i < 5; i++) {
            takeDamageLeft.addFrame(takeDamage[i], 25);
        }

        //attack animation Left

        BufferedImage Leftattackani[] = new BufferedImage[16];

        for (int i = 0; i < 15; i++) {
            Leftattackani[i] = img.getSubimage(i * 288, 2 * 160, 288, 160);
        }

        for (int i = 0; i < 15; i++) {
            attackAnimationleft.addFrame(Leftattackani[i], 25);
        }

        // Load Images for Left walk using submiage

        Image[] walkLeftImages = new Image[6];

        // cutting the image from the sprite sheet

        // walk animation Left
        BufferedImage Leftwalkani[] = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            Leftwalkani[i] = img.getSubimage(i * 288, 1 * 160, 288, 160);
        }

        for (int i = 0; i < 6; i++) {
            walkAnimationleft.addFrame(Leftwalkani[i], 100);
        }

      
        // death animation Left

        BufferedImage Leftdeathani[] = new BufferedImage[28];

        for (int i = 0; i < 22; i++) {
            Leftdeathani[i] = img.getSubimage(i * 288, 4 * 160, 288, 160);
        }

        for (int i = 0; i < 22; i++) {
            deathAnimation.addFrame(Leftdeathani[i], 125);
        }

        currAnimation = walkAnimationleft;
    }


    //check if on tile
    public Point collidesWithTileDown(int newX, int newY) {
        int playerWidth = currAnimation.getImage().getWidth(null);
        int playerHeight = currAnimation.getImage().getHeight(null);
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(newX);
        int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
        int yTileTo = tileMap.pixelsToTiles(newY - offsetY + playerHeight);
    
        for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null) {
                Point tilePos = new Point(xTile, yTile);
                //System.out.println("Collision detected at tile: (" + xTile + ", " + yTile + ")");
                return tilePos;
            } else {
                if (tileMap.getTile(xTile + 1, yTile) != null) {
                    int leftSide = (xTile + 1) * 64; // 64 is the tile size
                    if (newX + playerWidth > leftSide) {
                        Point tilePos = new Point(xTile + 1, yTile);
                        //System.out.println("Collision detected at tile next to current: (" + (xTile + 1) + ", " + yTile + ")");
                        return tilePos;
                    }
                }
            }
        }
        //System.out.println("No collision detected, not on floor.");
        return null;
    }


    public void update() {
        //System.out.println(agroTimeElapsed);
        // Starts the currently assigned animation.
        // if animation not started start it
        if (currAnimation != null)
            if (!currAnimation.isStillActive() && currAnimation != deathAnimation && currAnimation != takeDamageLeft)
                currAnimation.start();

            if (currAnimation == deathAnimation && !deathanimationplayed){
            deathanimationplayed = true;
            currAnimation.start();
        }
            
        if (!dead)
            //setAnimation(); // sets animation for left or right
            if (!isPlayerWithinAttackingRange) {
        // If the enemy is not dead, continue to roam the plan as long as there are
        // tiles on the bottom before and after the enemy.
        if (!dead) {
            if (!isAboutToCollideWithTile(x + currSpeed, getY())) {
                //colliding with floor?
                if (!isAboutToFallInSpikes()) {
                    //System.out.println("Not On floor");
                    currSpeed = -currSpeed;
                }
                x += currSpeed;
            } else {
                currSpeed = -currSpeed;
            }

            // if enemy is attacking then increase speed
            if (isattacking) {
                currSpeed = (int) Math.round((speedUp * Speed));
            }
        }

    }
    }

  

    public void setAnimation() {

}


    // Sets the agrovation on the player, adjusts animation and
    // directionaccordingly.
    public void setAgro(boolean isAttacking, String direction) {
        isattacking = isAttacking;
        if (!dead){
        if (isAttacking) {
            if(currAnimation == takeDamageLeft && currAnimation.isLastFrame() || currAnimation == walkAnimationleft){
            currAnimation.stop();  
            currAnimation = attackAnimationleft;
            currAnimation.start();
        }
            // Attack mode activated
            agroTimeElapsed++;
            //System.out.println("Agro time elapsed: " + agroTimeElapsed);
    
            // Check if it's time to speed up
            if (agroTimeElapsed % SPEED_UP_INTERVAL == 0 && SPEED_UP_INTERVAL != 0) {
                // Speed up every 20 agro time elapsed
                if (agroTimeElapsed % 20 == 0) {
                    speedUp();
                    System.out.println("Speeding up!");
                }
            }
    
            int sign = direction.equals("Left") ? -1 : 1;
            currSpeed = Math.abs(currSpeed) * sign;
        } else {
            // Attack mode deactivated
            currSpeed = Speed * (currSpeed < 0 ? -1 : 1);
            // if (currAnimation != takeDamageLeft){
            
            // currAnimation = walkAnimationleft;}
            if (currAnimation.isLastFrame() && currAnimation == takeDamageLeft || currAnimation == attackAnimationleft){
            //System.out.println("Setting animation to walk");
            currAnimation.stop();
            currAnimation = walkAnimationleft;
            currAnimation.start();
        }
        }

    }
    }

    
    private void speedUp() {
        // Adjust the speed here
        if (isAboutToFallofmap() || isAboutToCollideWithTileAhead(x, y) ){
            System.out.println("About to fall off map or collide with tile ahead");
            return;
        }
        if (!dead){
        if( getDirectionWeight() == 1) {
            x = x + 50;
            System.out.println(getDirection() + " Speeding up! Right");
        }
        else if (getDirectionWeight() == -1) {
            x = x - 50;
            System.out.println(getDirection() + " Speeding up! Left");
        }
    }
}


    

    public Rectangle getBoundingRectangle() {
        return new Rectangle(x, y, width, height);
    }


    public Rectangle2D.Double getDetectionArea() {
        double halfRange = detectionRange / 2.0;
        return new Rectangle2D.Double((x) - halfRange + width / 2, y, detectionRange + 20, detectionRange / 8);
    }

    // Sets the enemy to dead.
    public void setDead(boolean dead) {
        this.dead = dead;

        soundManager.playSound("kill", false);
        if (dead) {
            currAnimation = deathAnimation;
        }

        //awarding player 1000 points for killing the enemy
        tileMap.getScoreManager().addScore(1000);
    }
   

}
