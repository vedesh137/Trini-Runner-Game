import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.awt.Point;

public class Player {

	private static final int DX = 16; // amount of X pixels to move in one keystroke
	private static final int DY = 32; // amount of Y pixels to move in one keystroke

	private static final int TILE_SIZE = 64;

	private JFrame window; // reference to the JFrame on which player is drawn
	private TileMap tileMap;
	private BackgroundManager bgManager;

	private int x; // x-position of player's sprite
	private int y; // y-position of player's sprite

	Graphics2D g2;
	//private Dimension dimension;

	private Image playerImage; // legacy payer image variable
	BufferedImage img;

	private boolean jumping;
	private int timeElapsed;
	private int startY;

	private boolean goingUp;
	private boolean goingDown;
	private boolean goingRight;
	private boolean goingLeft;
	private boolean attacking;

	private boolean inAir;
	private int initialVelocity;
	private int startAir;
	private SoundManager sm;
	private Animation idleAnimation; // idle animation
	private Animation currAnimation; // current animation
	private Animation walkAnimationright; // walking animation right
	private Animation walkAnimationleft; // walking animation left
	private Animation jumpAnimationright; // jumping animation right
	private Animation jumpAnimationleft; // jumping animation left
	private Animation attackAnimationLeft;
	private Animation attackAnimationRight;
	private Animation slingShotAnimationRight;
	private Animation slingShotAnimationLeft;
	private int width, height;

	private int health;
	private boolean dead;
	private int moneyCollected;
	private int Stamina;
	private PlayerBullet bullet;
	private int numBullets;

	// Define dash-related properties
	private static final int DASH_SPEED = 200; // Adjust speed as needed
	private static final int DASH_DURATION = 30; // Adjust duration as needed
	private static final int DASH_COOLDOWN = 50; // Adjust cooldown time as needed

	private int dashCooldown = 0; // Tracks cooldown time
	private boolean dashing = false; // Tracks if the player is currently dashing
	private int dashTimer = 0; // Timer for dash duration

	public Player(JFrame window, TileMap t, BackgroundManager b) {
		this.bullet = null;
		this.window = window;

		tileMap = t; // tile map on which the player's sprite is displayed
		bgManager = b; // instance of BackgroundManager

		goingUp = goingDown = false;
		inAir = false;
		attacking = false;

		loadAnimations();
		currAnimation = walkAnimationright;

		playerImage = getCurrentAnimation().getImage();


		// width = playerImage.getWidth(null); // Get the width of the image
		// height = playerImage.getHeight(null); // Get the height of the image
		width = 64; // manually setting
		height = 64;	
		sm = sm.getInstance();
		health = 2000;
		moneyCollected = 0;
		Stamina = 25;
		dead = false;
		numBullets = 20;
	}
	public void die() {
        System.out.println("Player has died.");
		sm.playSound("dead", false);
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

	public Point collidesWithTileDown(int newX, int newY) {
		
		int playerWidth = playerImage.getWidth(null);
		int playerHeight = playerImage.getHeight(null);
		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
		int yTileTo = tileMap.pixelsToTiles(newY - offsetY + playerHeight);

		for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
			if (tileMap.getTile(xTile, yTile) != null) {
				Point tilePos = new Point(xTile, yTile);
				return tilePos;
			} else {
				if (tileMap.getTile(xTile + 1, yTile) != null) {
					int leftSide = (xTile + 1) * TILE_SIZE;
					if (newX + playerWidth > leftSide) {
						Point tilePos = new Point(xTile + 1, yTile);
						return tilePos;
					}
				}
			}
		}

		return null;
	}

	public Point collidesWithTileUp(int newX, int newY) {

		int playerWidth = playerImage.getWidth(null);

		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);

		int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
		int yTileTo = tileMap.pixelsToTiles(newY - offsetY);

		for (int yTile = yTileFrom; yTile >= yTileTo; yTile--) {
			if (tileMap.getTile(xTile, yTile) != null) {
				Point tilePos = new Point(xTile, yTile);
				return tilePos;
			} else {
				if (tileMap.getTile(xTile + 1, yTile) != null) {
					int leftSide = (xTile + 1) * TILE_SIZE;
					if (newX + playerWidth > leftSide) {
						Point tilePos = new Point(xTile + 1, yTile);
						return tilePos;
					}
				}
			}

		}

		return null;
	}

	public synchronized void move(int direction) {

		int newX = x;
		Point tilePos = null;

		if (!window.isVisible())
			return;

		if (direction == 1) {
			goingLeft = true;
			goingRight = false;

			if (!jumping) {
				//System.out.println("Not in the air");
				currAnimation = walkAnimationleft;
			} else {
				//System.out.println("In the air");
				currAnimation = jumpAnimationleft;
			}

			if (!currAnimation.isStillActive()) {
				// playerImage = playerLeftImage;

				currAnimation.start();
			}
			newX = x - DX;
			if (newX < 0) {
				x = 0;
				return;
			}

			tilePos = collidesWithTile(newX, y);
		} else if (direction == 2) { // move right

			if (!jumping) {
				//System.out.println("Not in the air");
				currAnimation = walkAnimationright;
			} else {
				//System.out.println("In the air");
				currAnimation = jumpAnimationright;
			}

			if (!currAnimation.isStillActive()) {
				// playerImage = playerLeftImage;

				currAnimation.start();
			}
			goingRight = true;
			goingLeft = false;
			// playerImage = playerRightImage;
			int playerWidth = playerImage.getWidth(null);
			newX = x + DX;

			int tileMapWidth = tileMap.getWidthPixels();

			if (newX + playerImage.getWidth(null) >= tileMapWidth) {
				x = tileMapWidth - playerImage.getWidth(null);
				return;
			}

			tilePos = collidesWithTile(newX + playerWidth, y);
		} else // jump
		if (direction == 3 && !jumping && !inAir) {
			// stop current animation if active
			if (currAnimation.isStillActive() && currAnimation != jumpAnimationleft
					&& currAnimation != jumpAnimationright) {
				currAnimation.stop();

			}
			if (goingRight && !goingLeft) {
				//System.out.println("Going right");
				currAnimation = jumpAnimationright;
			}
			if (goingLeft && !goingRight) {
				//System.out.println("Going left");
				currAnimation = jumpAnimationleft;
			}

			jump();
			currAnimation.start();

			return;
		}

		if (tilePos != null) {
			if (direction == 1) {
				//System.out.println(": Collision going left");
				x = ((int) tilePos.getX() + 1) * TILE_SIZE; // keep flush with right side of tile
			} else if (direction == 2) {
				//System.out.println(": Collision going right");
				int playerWidth = playerImage.getWidth(null);
				x = ((int) tilePos.getX()) * TILE_SIZE - playerWidth; // keep flush with left side of tile
			}
		} else {
			if (direction == 1) {
				x = newX;
				bgManager.moveLeft();
			} else if (direction == 2) {
				x = newX;
				bgManager.moveRight();
			}

			if (isInAir()) {
				//System.out.println("In the air. Starting to fall.");
				if (direction == 1) { // make adjustment for falling on left side of tile
					int playerWidth = playerImage.getWidth(null);
					x = x - playerWidth + DX;
				}
				fall();
			}
		}
	}

	public boolean isInAir() {

		int playerHeight;
		Point tilePos;

		if (!jumping && !inAir) {
			playerHeight = playerImage.getHeight(null);
			tilePos = collidesWithTile(x, y + playerHeight + 1); // check below player to see if there is a tile

			if (tilePos == null) // there is no tile below player, so player is in the air
				return true;
			else // there is a tile below player, so the player is on a tile
				return false;
		}

		return false;
	}

	private void fall() {

		jumping = false;
		inAir = true;
		timeElapsed = 0;

		goingUp = false;
		goingDown = true;

		startY = y;
		initialVelocity = 0;
	}

	public void jump() {

		if (!window.isVisible())
			return;

		jumping = true;
		timeElapsed = 0;

		goingUp = true;
		goingDown = false;

		startY = y;
		initialVelocity = 70;
	}

	public void update() {
		int distance = 0;
		int newY = 0;

		currAnimation.update();

		timeElapsed++;

		if (jumping || inAir) {
			distance = (int) (initialVelocity * timeElapsed -
					4.9 * timeElapsed * timeElapsed);
			newY = startY - distance;

			if (newY > y && goingUp) {
				goingUp = false;
				goingDown = true;
			}

			if (goingUp) {
				Point tilePos = collidesWithTileUp(x, newY);
				if (tilePos != null) { // hits a tile going up
					//System.out.println("Jumping: Collision Going Up!");

					int offsetY = tileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;
					int bottomTileY = topTileY + TILE_SIZE;

					y = bottomTileY;
					fall();
				} else {
					y = newY;
					//System.out.println("Jumping: No collision.");
				}
			} else if (goingDown) {
				Point tilePos = collidesWithTileDown(x, newY);
				if (tilePos != null) { // hits a tile going up
					//System.out.println("collided with tile at x is on floor: " + tilePos.x * 64 + " y: " + tilePos.y + " Player  x: " + x + " y: " + y);
					//System.out.println("Jumping: Collision Going Down!");
					
					int playerHeight = playerImage.getHeight(null);
					goingDown = false;

					int offsetY = tileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;

					y = topTileY - playerHeight;
					jumping = false;
					inAir = false;
				} else {
					y = newY;
					//System.out.println("Jumping: No collision.");
				}
			}
		}

		//dashing
		if (!dead){

			if (dashing) {
				// Decrement dash timer
				dashTimer--;
	
				if (dashTimer <= 0) {
					// End dash when timer reaches 0
					dashing = false;
				}
			}
	
			// Update dash cooldown
			if (dashCooldown > 0) {
				dashCooldown--;
			}
		}


    // Update player's vertical position due to gravity for dashing 
    if (!isOnFloor() && !jumping && !inAir) {
		fall(); // Only apply gravity when not jumping or in the air
    }

	}

	//check if player on floor
	public boolean isOnFloor() {
		int playerHeight = playerImage.getHeight(null);
		Point tilePos = collidesWithTile(x, y + playerHeight + 1);
		if (tilePos != null) {
			return true;
		} else {
			//System.out.println("Not on floor");
			return false;
		}
	}


	public void dash() {
		if (dashCooldown == 0 && !dashing) { // Check if dash is available and not already dashing
			// Set dashing flag to true
			dashing = true;
			dashTimer = DASH_DURATION; // Set dash timer
	
			// Adjust player speed based on direction
			if (getDirection().equals("Right")) {
				// Dash right
				int newX = x + DASH_SPEED;
				Point tilePos = collidesWithTile(newX, y);
				if (tilePos == null) { // Check for collision
					x = newX;
				}
			} else if (getDirection().equals("Left")) {
				// Dash left
				int newX = x - DASH_SPEED;
				Point tilePos = collidesWithTile(newX, y);
				if (tilePos == null) { // Check for collision
					x = newX;
				}
			}
	
			// Start cooldown timer
			dashCooldown = DASH_COOLDOWN;
		}
	}

	public void moveUp() {

		if (!window.isVisible())
			return;

		y = y - DY;
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

	public Image getImage() {
		return currAnimation.getImage();
	}

	public Animation getCurrentAnimation() {
		return currAnimation;
	}

	//getwidth
	public int getWidth() {
		return width;
	}

	//getheight
	public int getHeight() {
		return height;
	}

	//get direction
	public String getDirection() {
		if(goingRight) {
			return "Right";
		}
		else {
			return "Left";
		}
	}

	//get health
	public int getHealth() {
		return health;
	}

	//set health
	public void updateHealth(int health) {
		this.health += health;
	}

	//get money collected
	public int getMoneyCollected() {
		return moneyCollected;
	}

	//get stamina
	public int getStamina() {
		return Stamina;
	}

	public void setHealth(int newHealth) {
		this.health = newHealth;
	}

	// limiting player attack to only register after last frame if animation, returning true if last frame for player to attack
	public boolean canAttackNow() {
		//can attack every 5 time elapsed
		if (timeElapsed % 5 == 0) {
			return true;
		}
	return false;
	}
	
	// player attack

	public void attack() {

		// if player not attacking then start attack animation
		if (!attacking) {
			if (goingRight && !goingLeft) {
				currAnimation = attackAnimationRight;
			}
			if (goingLeft && !goingRight) {
				currAnimation = attackAnimationLeft;
			}
			if (currAnimation != null && !currAnimation.isStillActive())
			currAnimation.start();
		}
	}

	 //update bullets
	 public void updateAmmo(int numBulletsadd) {
		this.numBullets+= numBulletsadd;
		System.out.println("Ammo updated to: " + numBullets);
		}

	//get bullets
	public int getAmmo() {
		return numBullets;
	}

	//get duration of time left for next dash 
	public int getDashCooldown() {
		return dashCooldown;
	}

	// draw cooldown bar
	public void drawCooldown(Graphics2D g) {
		int cooldownWidth = 200; 
		int cooldownHeight = 20; 
	
		int remainingCooldown = DASH_COOLDOWN - dashCooldown;
		if (remainingCooldown < 0) {
			remainingCooldown = 0;
		}
		
		int xcoord = 25;
		int ycoord = 110
		;
		int currentCooldownBar = (int)(((float)remainingCooldown / DASH_COOLDOWN) * cooldownWidth);
	
		Rectangle2D.Double cooldownBar = new Rectangle2D.Double(xcoord, ycoord, currentCooldownBar, cooldownHeight);
	
		
		Point2D start = new Point2D.Float(xcoord, ycoord);
		Point2D end = new Point2D.Float(xcoord + currentCooldownBar, ycoord);
	
		
		Color startColor = new Color(0, 0, 255, 200);
		Color endColor = new Color(0, 255, 255, 200);
	
		
		GradientPaint gradientPaint = new GradientPaint(start, startColor, end, endColor);
	
		
		g.setPaint(gradientPaint);
	
		
		g.fill(cooldownBar);
		g.draw(cooldownBar);
	
		Rectangle2D.Double cooldownBarOutline = new Rectangle2D.Double(xcoord, ycoord, cooldownWidth, cooldownHeight); // Slightly bigger cooldown bar
		g.setStroke(new BasicStroke(3));
		g.setColor(new Color(0, 0, 139)); 
		g.draw(cooldownBarOutline);
	}
	

	
	 public void shootPlayerBullet(int mouseX, int mouseY,int offsetX, int offsetY) {
		if (numBullets > 0) {
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
           if (getDirection() == "Left")    {     
			numBullets--;     
           bullet = new PlayerBullet(x * 1 + offsetX - 40, y * 1 , bulletDX, bulletDY, tileMap);
		currAnimation = slingShotAnimationLeft;
		currAnimation.start();
		}	
              else{
				numBullets--;
                bullet = new PlayerBullet(x * 1 + offsetX + 64, y * 1 , bulletDX, bulletDY, tileMap);
				currAnimation = slingShotAnimationRight;
				currAnimation.start();
			}
        }
		//System.out.println("Setting bullet location as x: " + x + " y: " + y + " offsetX: " + offsetX + " offsetY: " + offsetY);
        bullet.setTargetLoc(mouseX, mouseY);
		
	}
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

    public PlayerBullet getBullet() {
    return bullet;
    }



	public Rectangle getBounds2() {
		// Replace the following with actual dimensions and position
		return new Rectangle(x + tileMap.getOffsetX() + 20, y + tileMap.getOffsetY() - 540, width, height);
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	public void loadAnimations() {

		walkAnimationright = new Animation(false);
		walkAnimationleft = new Animation(false);
		jumpAnimationright = new Animation(false);
		jumpAnimationleft = new Animation(false);
		attackAnimationLeft = new Animation(false);
		attackAnimationRight = new Animation(false);
		slingShotAnimationRight = new Animation(false);
		slingShotAnimationLeft = new Animation(false);

		// Load images for right movement
		// Load Images for right walk  using submiage

        //cutting the image from the sprite sheet
        InputStream is = getClass().getResourceAsStream("images/powley_atlas.png");

        try{
            img = ImageIO.read(is);
    }
        catch(IOException e){
            e.printStackTrace();
        }

        BufferedImage rightwalkani[] = new BufferedImage[7]; 

        for (int i = 0; i < 7; i++) {
            rightwalkani[i] = img.getSubimage(i * 64, 11 * 64, 64, 64);
        }
		

		// Add right images to right animation
		for (int i = 0; i < 7; i++) {
			walkAnimationright.addFrame(rightwalkani[i], 25);
		}

		BufferedImage leftImages[] = new BufferedImage[7];

		for (int i = 0; i < 7; i++) {
			leftImages[i] = img.getSubimage(i * 64, 9 * 64, 64, 64);
		}

		// Add left images to left animation
		for (int i = 0; i < 7; i++) {
			walkAnimationleft.addFrame(leftImages[i], 25);
		}

		// Load Images for right jump

		BufferedImage jumpRightImages[] = new BufferedImage[7];

		for (int i = 0; i < 7; i++) {
			jumpRightImages[i] = img.getSubimage(i * 64, 3 * 64, 64, 64);
		}

		// Add right images to right animation
		for (int i = 6; i >= 0; i--) {
			jumpAnimationright.addFrame(jumpRightImages[i], 170);
		}

		// Load Images for left jump inversly to get the left jump animation

		BufferedImage jumpLeftImages[] = new BufferedImage[7];

		for (int i = 0; i < 7; i++) {
			// Calculate the starting x-coordinate for the subimage
			int startX = (6 - i) * 64;
			
			// Create the subimage by reading from the rightmost position towards the left
			jumpLeftImages[i] = img.getSubimage(startX, 64, 64, 64);
		}


		// Add left images to left animation

		for (int i = 0; i < 6; i++) {
			jumpAnimationleft.addFrame(jumpLeftImages[i], 170);
		}



		// Load Images for left attack

		BufferedImage attackLeftImages[] = new BufferedImage[9];

		for (int i = 0; i < 6; i++) {
			attackLeftImages[i] = img.getSubimage(i * 128, 31 * 64, 128, 128);
		}

		// Add Images for Attack Left to left animation

		for (int i = 0; i < 6; i++) {
			// attackAnimationLeft.addFrame(attackLeftImages[i], 18);
			BufferedImage frame = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = frame.createGraphics();
			g2d.drawImage(attackLeftImages[i], -16, -32, 128, 128, null);
			g2d.dispose();
			
    		attackAnimationLeft.addFrame(frame, 60);
			// attackAnimationLeft.addFrame(attackLeftImages[i], 18, x - 32, y - 32);
		}

		// Add Images for Attack Left to left animation
		BufferedImage attackRightImages[] = new BufferedImage[9];

		for (int i = 0; i < 6; i++) {
			attackRightImages[i] = img.getSubimage(i * 128, 35 * 64, 128, 128);
		}

		for (int i = 0; i < 6; i++) {
			// attackAnimationLeft.addFrame(attackRightImages[i], 18);
			BufferedImage frame = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = frame.createGraphics();
			g2d.drawImage(attackRightImages[i], -32, -32, 128, 128, null);
			g2d.dispose();
			
    		attackAnimationRight.addFrame(frame, 60);
			
		}

		// Load images for slingshot 

		// Load Images for right walk  using submiage

		//cutting the image from the sprite sheet
		InputStream is2 = getClass().getResourceAsStream("images/player_slingshot_right.png");

		try{
			img = ImageIO.read(is2);
	}
		catch(IOException e){
			e.printStackTrace();

		}

		//right walk animation
		BufferedImage slingshotright[] = new BufferedImage[4];

		for (int i = 0; i < 4; i++) {
			slingshotright[i] = img.getSubimage(i * 64+ 512, 0, 64, 64);
		}

		// Add right images to right animation

		for (int i = 0; i < 4; i++) {
			slingShotAnimationRight.addFrame(slingshotright[i], 50);
		}



		InputStream is3 = getClass().getResourceAsStream("images/player_sling_shot_left.png");

		try{
			img = ImageIO.read(is3);
	}
		catch(IOException e){
			e.printStackTrace();

		}


		// Load Images for left walk

		BufferedImage slingshotleft[] = new BufferedImage[4];

		for (int i = 0; i < 4; i++) {
			slingshotleft[i] = img.getSubimage(i * 64 + 512, 0, 64, 64);
		}

		// Add left images to left animation

		for (int i = 0; i < 4; i++) {
			slingShotAnimationLeft.addFrame(slingshotleft[i], 50);
		}

		// initial animation when start game
		currAnimation = walkAnimationright;
	}


	public boolean isDead() {
        return this.dead;
    }


}
