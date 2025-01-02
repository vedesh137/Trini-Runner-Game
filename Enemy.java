import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;

public abstract class Enemy {

    protected JFrame window;
    protected TileMap tileMap;

    protected int y;
    protected int x;
    protected double speedUp;

    protected int Speed = 3;
    protected int currSpeed = Speed;

    // Animations for the enemy.
    protected Animation deathAnimation;
    protected Animation attackAnimationleft;
    protected Animation attackAnimationright;
    protected Animation walkAnimationright;
    protected Animation walkAnimationleft;
    protected Animation currAnimation;
    protected Animation takeDamageLeft;

    // Detection
    protected Integer detectionRange;
    protected Rectangle2D aggressionBounds;

    // Enemy attributes.
    protected boolean dead;
    protected String direction;
    protected int ENTITY_DEFAULT_HEALTH;
    protected int health;
    protected int healthbarWidth;
    protected int knockbackDistance;
    protected boolean isattacking;
    protected int agroTimeElapsed = 0;
    protected final int SPEED_UP_DURATION = 20; // Number of agro time elapsed to speed up
    protected final int SPEED_UP_INTERVAL = 50; // Interval at which to apply speed up


    protected SoundManager soundManager;
    protected boolean isPlayedDetected;
    protected boolean withinRange;
    protected boolean isPlayerWithinAttackingRange;
    
    protected int width = 64;
    protected int height = 64;
    protected int EnemyOffsetY = 0;  // this is for spawning larger enemies on the ground
    protected boolean deathanimationplayed = false;
    BufferedImage img;

    public Enemy(JFrame window, TileMap tm) {
        this.window = window;
        tileMap = tm;

        // Loads the enemy's animations.
        loadAnimations();

        // Sets the current animation to something
        currAnimation = walkAnimationright;

        // Init state
        x = 0;
        y = 0;
        isattacking = false;
        healthbarWidth = 50;
        ENTITY_DEFAULT_HEALTH = 100;
        health = 100;
        dead = false;
        direction = "Right";
        speedUp = 2;
        isPlayedDetected = false;
        soundManager = SoundManager.getInstance();
        agroTimeElapsed = 0;
        withinRange = false;
        isPlayerWithinAttackingRange = false;
    }

    // Returns the hitbox of the enemy.
    public Rectangle getBoundingRectangle() {
        return new Rectangle(x, y, width, height);
    }

    public Rectangle2D.Double getDetectionArea() {
        double halfRange = detectionRange / 2.0;
        return new Rectangle2D.Double((x) - halfRange + width / 2, y, detectionRange, detectionRange / 8);
    }

    //check if on floor 
    public boolean  isAboutToFallInSpikes(){
        //adding pad to x to check if enemy is about to fall in spikes
        int paddedx = x;
        if (getDirection() == "Right")
        paddedx = x + width ;
        if (getDirection() == "Left")
        paddedx = x - width;
        Point point = collidesWithTileDown(paddedx, y - 4 + EnemyOffsetY); // 4 is the offset of enemy from the floor
        if (point != null) {
            //int topTileY = ((int) point.getY()) * 64 + tileMap.getOffsetY();
            //y = topTileY - height;
            //System.out.println("collided with tile at x is on floor: " + point.x * 64 + " y: " + point.y + " Enemy  x: " + x + " y: " + y);
            return true;
        }
        return false;
    }
    //check if about to collide with tile left or right
    public boolean isAboutToCollideWithTile(int newX, int newY) {
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

   //check if about to collide with tile left or right
   public boolean isAboutToCollideWithTileAhead(int newX, int newY) {
    Point point = collidesWithTile(newX + width + width, newY);
    if (point != null) {
        //System.out.println("collided with tile for right at : " + point.x * 64 + " y: " + point.y + " Enemy  x: " + x + " y: " + y);
        return true;
    }
    Point point2 = collidesWithTile(newX - width, newY);
    if (point2 != null) {
        //System.out.println("collided with tile for left at x: " + point2.x * 64 + " y: " + point2.y + " Enemy  x: " + x + " y: " + y);
        return true;
    }
    return false;
}

   //check if the tile ahead is spikes not null
    public boolean isAboutToFallofmap(){
        
        Point point = collidesWithTile(x + -width * getDirectionWeight(), y + height);
        if (point != null) {
            //System.out.println("collided with tile for right at : " + point.x * 64 + " y: " + point.y + " Enemy  x: " + x + " y: " + y);
            return false;
        }
        //System.out.println("No collision detected, not on floor.");
        return true;
    }
   


    public Point collidesWithTile(int newX, int newY) {

		//int playerWidth = playerImage.getWidth(null);
		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTile = tileMap.pixelsToTiles(newY - offsetY);

		if (tileMap.getTile(xTile, yTile) != null) {
			Point tilePos = new Point(xTile, yTile);
			return tilePos;
		} else {
			return null;
		}
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

    // Updates the enemy.
    public void update() {
        // Starts the currently assigned animation.
        // if animation not started start it
        if (currAnimation != null)
            if (!currAnimation.isStillActive() && currAnimation != deathAnimation)
                currAnimation.start();
                if (currAnimation == deathAnimation && !deathanimationplayed){
            deathanimationplayed = true;
            currAnimation.start();
        }
            
        if (!dead)
            setAnimation(); // sets animation for left or right
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

    // Loads the animaions by cutting up strip file.
    public void loadAnimations() {

        walkAnimationright = new Animation(false);
        walkAnimationleft = new Animation(false);

        attackAnimationleft = new Animation(false);
        attackAnimationright = new Animation(false);

        deathAnimation = new Animation(false);

        // Load Images for right attack using submiage

        // cutting the image from the sprite sheet
        InputStream is = getClass().getResourceAsStream("images/Pamela_atlas.png");

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // attack animation left
        BufferedImage leftattackani[] = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            leftattackani[i] = img.getSubimage(i * 64, 13 * 64, 64, 64);
        }

        for (int i = 0; i < 6; i++) {
            attackAnimationleft.addFrame(leftattackani[i], 100);
        }

        // attack animation right

        BufferedImage rightattackani[] = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            rightattackani[i] = img.getSubimage(i * 64, 15 * 64, 64, 64);
        }

        for (int i = 0; i < 6; i++) {
            attackAnimationright.addFrame(rightattackani[i], 100);
        }

        // walk animation right

        BufferedImage walkrightani[] = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            walkrightani[i] = img.getSubimage(i * 64, 11 * 64, 64, 64);
        }

        for (int i = 0; i < 6; i++) {
            walkAnimationright.addFrame(walkrightani[i], 100);
        }

        // walk animation left

        BufferedImage walkleftani[] = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            walkleftani[i] = img.getSubimage(i * 64, 9 * 64, 64, 64);
        }

        for (int i = 0; i < 6; i++) {
            walkAnimationleft.addFrame(walkleftani[i], 100);
        }

        // deathanimation

        BufferedImage deathani[] = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            deathani[i] = img.getSubimage(i * 64, 20 * 64, 64, 64);
        }

        for (int i = 0; i < 6; i++) {
            deathAnimation.addFrame(deathani[i], 100);
        }

        currAnimation = walkAnimationleft;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setSpawn(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean takingDamage() {
        return currAnimation == takeDamageLeft;
    }
    
    public Animation getcurrAnimation() {
        return currAnimation;
    }

    public boolean isDead() {
        return this.dead;
    }

    // Sets the enemy to dead.
    public void setDead(boolean dead) {
        this.dead = dead;

        soundManager.playSound("kill", false);
        if (dead) {
            currAnimation = deathAnimation;
        }
    }

    // Sets the agrovation on the player, adjusts animation and
    // directionaccordingly.
    public void setAgro(boolean isAttacking, String direction) {
    isattacking = isAttacking;

    if (isAttacking) {
        // Attack mode activated
        agroTimeElapsed++;
        // System.out.println("Agro time elapsed: " + agroTimeElapsed);

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
    }
}
    
    private void speedUp() {
        // Adjust the speed here
        if (isAboutToFallofmap() || isAboutToCollideWithTileAhead(x, y) ){
            return;
        }
        if( currAnimation == attackAnimationright) {
            x = x + 32;
            System.out.println(getDirection() + " Speeding up! Right");
        }
        else if (currAnimation == attackAnimationleft) {
            x = x - 32;
            System.out.println(getDirection() + " Speeding up! Left");
        }
    }

    // returns the hitbox of the enemy's attack range hitbox
    public Rectangle getAttackRange() {

            return new Rectangle(x - width/4, y, (width * 3)/2, height);
       
    }

    // check player range
    public boolean isPlayerWithinRange(Rectangle2D playerBounds, boolean isPlayerDead) {
        return getDetectionArea().intersects(playerBounds) && !isPlayerDead;
    }

    public boolean isPlayerWithinAttackingRange(Rectangle2D playerBounds, boolean isPlayerDead) {
        return getAttackRange().intersects(playerBounds) && !isPlayerDead;
    }

    // Response of the enemy to player proximity.
    public void engagePlayer(Rectangle2D playerBounds, int playerX, boolean isPlayerDead) {
        this.withinRange = isPlayerWithinRange(playerBounds, isPlayerDead);
        this.isPlayerWithinAttackingRange = isPlayerWithinAttackingRange(playerBounds, isPlayerDead);
        // if (isPlayerWithinAttackingRange)
        // System.out.println("Player within attacking range");
        String direction = "Right";

        // Initiate response if player detected within range.
        if (withinRange && !isPlayedDetected) {
            soundManager.playSound("detected", false);
            soundManager.playSound("enemyAttack", false);
            isPlayedDetected = true;
        } else if (!withinRange) {
            isPlayedDetected = false;
        }

        // Adjust tactics based on player position.
        if (withinRange) {
            direction = (getX() - playerX > playerX - getX()) ? "Left" : "Right";
        }

        // Execute strategy based on current scenario.
        setAgro(withinRange, direction);
    }

    // Returns current Image of animation
    public Image getImage() {
        return currAnimation.getImage();
    }

    // Updates the health of the enemy.
    public void updateHealth(int damage) {
        health -= damage;

        if (health <= 0) {
            setDead(true);
        }
    }

    // Set health
    public void setHealth(int health) {
        this.health = health;
    }

    // Returns the direction of the enemy based on their speed.
    public String getDirection() {
        return currSpeed > 0 ? "Right" : "Left";
    }

    // Returns the weight of the direction of the enemy.
    public int getDirectionWeight() {
        return getDirection().equals("Right") ? 1 : -1;
    }

    // Gets the knockback distance for entity.
    public int getKnockbackDistance() {
        // check if about to collide with tile left or right
        if (isAboutToCollideWithTileAhead(x , y)) {
            return 0;
        }
        // check if about to fallinto spikes
        if (isAboutToFallofmap()) {
            return 0;
            
        }
        else
            return 20;
        
       // return knockbackDistance;
    }

    public void drawHealth(Graphics2D g, int offsetX) {
        int currentHealthBar = (int) ((((float) health / ENTITY_DEFAULT_HEALTH) * (healthbarWidth + 10))); 
                                                                                                           
                                                                                                           
        Rectangle2D.Double healthBar = new Rectangle2D.Double(offsetX + x - (healthbarWidth - width) / 2, y - 20,
                currentHealthBar, 10); 

        // Define the start and end points of the gradient
        Point2D start = new Point2D.Float((float) (offsetX + x - (healthbarWidth - width) / 2), (float) (y - 20));
        Point2D end = new Point2D.Float((float) (offsetX + x + currentHealthBar - (healthbarWidth - width) / 2),
                (float) (y - 20));

        // Define the colors for the gradient
        Color startColor = new Color(255, 0, 0, 200); // Bright red color with transparency
        Color endColor = new Color(255, 255, 0, 200); // Yellow color with transparency

        // Create a GradientPaint object
        GradientPaint gradientPaint = new GradientPaint(start, startColor, end, endColor);

        // Set the paint to the gradient
        g.setPaint(gradientPaint);

        // Fill the health bar with the gradient
        g.fill(healthBar);
        g.draw(healthBar);

        Rectangle2D.Double healthBarOutline = new Rectangle2D.Double(offsetX + x - (healthbarWidth - width) / 2, y - 20,
                healthbarWidth + 10, 10); // Slightly bigger health bar
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(190, 38, 35)); // Dark red outline
        g.draw(healthBarOutline);
    }

    // Sets the current animation of the enemy based on the direction of the
    // enemy.
    public void setAnimation() {
        if (getDirection() == "Right") {
            currAnimation = walkAnimationright;
        } else {
            currAnimation = walkAnimationleft;
        }

        // for attacking now
        if (isattacking) {
            if (getDirection() == "Right") {
                currAnimation = attackAnimationright;
            } else {
                currAnimation = attackAnimationleft;
            }
        }
    }

}