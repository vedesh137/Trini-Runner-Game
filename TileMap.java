import java.awt.Image;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JFrame;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.*;

/**
 * The TileMap class contains the data for a tile-based
 * map, including Sprites. Each tile is a reference to an
 * Image. Images are used multiple times in the tile map.
 * map.
 */

public class TileMap {
    private ScoreManager scoreManager;
    private static final int TILE_SIZE = 64;
    private static final int TILE_SIZE_BITS = 6;
    private LinkedList<Collectible> collectibles = new LinkedList<>();
    private LinkedList<Cannon> cannons = new LinkedList<>();
    private LinkedList<Spikes> spikes = new LinkedList<>();
    private LinkedList<Objects> objects = new LinkedList<>();
    private LinkedList<Doubles> Doubles = new LinkedList<>();
    private LinkedList<Ammobox> Ammoboxes = new LinkedList<>(); 
    private Image[][] tiles;
    public Enemy[] mainenemies;
    private int numenemies;
    private int screenWidth, screenHeight;
    private int mapWidth, mapHeight;
    private int offsetY;
    private SoundManager sm;
    private LinkedList sprites;
    private Player player;
    private Boolean levelCompleted = false;

    BackgroundManager bgManager;

    private JFrame window;
    private Dimension dimension;
    private int numBasicEnemies;
    private int ticks;

    /**
     * Creates a new TileMap with the specified width and
     * height (in number of tiles) of the map.
     */
    public TileMap(JFrame window, int width, int height) {

        this.window = window;
        dimension = window.getSize();
        scoreManager = new ScoreManager();
        screenWidth = dimension.width;
        screenHeight = dimension.height;

        mapWidth = width;
        mapHeight = height;
        numBasicEnemies = 0;
        sm = sm.getInstance();
        // initialize Eney array
        mainenemies = new Enemy[0];
        // get the y offset to draw all sprites and tiles

        offsetY = screenHeight - tilesToPixels(mapHeight);
        //System.out.println("offsetY: " + offsetY);

        bgManager = new BackgroundManager(window, 12);

        tiles = new Image[mapWidth][mapHeight];
        player = new Player(window, this, bgManager);
        sprites = new LinkedList();

        Image playerImage = player.getImage();
        int playerHeight = playerImage.getHeight(null);

        int x, y;

        x = 192; // position player in 'random' location
        y = dimension.height - (TILE_SIZE + playerHeight);

        player.setX(x);
        player.setY(y);

        ticks = 0;
        // System.out.println("Player coordinates: " + x + "," + y);

    }

    public void addCollectible(Image image, int x, int y, int width, int height) {
        collectibles.add(new Collectible(this, image, x, y, width, height));
    }
    public void addDoubles(Image image, int x, int y, int width, int height) {
        Doubles.add(new Doubles(this, image, x, y, width, height));
    }
    public void addAmmo(Image image, int x, int y, int width, int height) {
        Ammoboxes.add(new Ammobox(this, image, x, y, width, height));
    }
    public void addSpikes(Image image, int x, int y, int width, int height) {
        spikes.add(new Spikes(this, image, x, y, width, height));
    }

    public void addObjects(Image image, int x, int y, int width, int height) {
        objects.add(new Objects(this, image, x, y, width, height));
    }
    public void checkSpikesCollision() {
        for (Spikes spike : spikes) {
            if (spike.getBounds().intersects(player.getBounds())) {
                player.updateHealth(-player.getHealth()); // Set player health to 0
                player.die(); // Call the player's die method
                
                break; // Exit the loop after handling collision
               
            }
        }
    }
    public void checkCollectibleCollision(Player player) {
        for (Collectible collectible : collectibles) {
            if (collectible.isVisible() && player.getBounds().intersects(collectible.getBounds())) {
                collectible.setVisible(false); // Hide collectible
                scoreManager.addScore(100);
                sm.playSound("money", false);
                // Add any additional logic for when the collectible is picked up, like
                // increasing score
            }
        }
    }
    public void checkDoublesCollision() {
        Iterator<Doubles> it = Doubles.iterator();
        while (it.hasNext()) {
            Doubles doubles = it.next();
            if (doubles.isVisible() && doubles.getBounds().intersects(player.getBounds())) {
                doubles.setVisible(false); // Make the doubles object invisible after collecting
                player.updateHealth(200); // Increase player health by 10
                sm.playSound("eat", false);
            }
        }
    }

    //check amobox collision
    public void checkAmmoboxCollision() {
        for (Ammobox ammobox : Ammoboxes) {
            if (ammobox.isVisible() && player.getBounds().intersects(ammobox.getBounds())) {
                ammobox.setVisible(false); // Hide ammobox
                player.updateAmmo(10); // Increase player ammo by 10
            }
        }
    }
    
    public void addCannon(Image image, int x, int y, int type) {
        // System.out.println("Adding cannon at--------------------------------------------------------------------: " + x
        if(type == 0)
        cannons.add(new Cannon(image, x, y, this));
        else if(type == 1)
        cannons.add(new EnemyCannon(image, x, y, this));
    }

    public void printEnemyTypes() {
        if (mainenemies != null) {
            for (Enemy enemy : mainenemies) {
                if (enemy instanceof BasicEnemy) {
                    System.out.println("Basic Enemy");
                } else if (enemy instanceof BossEnemy) {
                    System.out.println("Boss Enemy");
                } else {
                    System.out.println("Unknown Enemy Type");
                }
            }
        }
    }

    public void placeBasicenemy(int x, int y, int type) {
        // Increment the count of basic enemies
        this.numBasicEnemies++;
    
        // Create a new array to store enemies with increased size
        Enemy[] updatedEnemies = new Enemy[this.numBasicEnemies];
    
        // Copy existing enemies to the new array
        for (int i = 0; i < this.numBasicEnemies - 1; i++) {
            updatedEnemies[i] = this.mainenemies[i];
        }
        
        
        // Create and add the new enemy to the array
        if (type == 0)
        updatedEnemies[this.numBasicEnemies - 1] = new BasicEnemy(window, this);
        else if (type == 1)
        updatedEnemies[this.numBasicEnemies - 1] = new BossEnemy(window, this);

        // Set the position of the new enemy
        updatedEnemies[this.numBasicEnemies - 1].setX(tilesToPixels(x));
        updatedEnemies[this.numBasicEnemies - 1].setY(tilesToPixels(y) + offsetY + 4);
    
        // Update the mainenemies array reference
        this.mainenemies = updatedEnemies;
    
        //System.out.println("Placing basic enemy at (" + x + ", " + y + ")");
    }

    

    /**
     * Gets the width of this TileMap (number of pixels across).
     */
    public int getWidthPixels() {
        return tilesToPixels(mapWidth);
    }

    /**
     * Gets the width of this TileMap (number of tiles across).
     */
    public int getWidth() {
        return mapWidth;
    }

    /**
     * Gets the height of this TileMap (number of tiles down).
     */
    public int getHeight() {
        return mapHeight;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetX() {
        return Math.max(Math.min(screenWidth / 2 - Math.round(player.getX()) - TILE_SIZE, 0),
                screenWidth - tilesToPixels(mapWidth));
    }

    /**
     * Gets the tile at the specified location. Returns null if
     * no tile is at the location or if the location is out of
     * bounds.
     */
    public Image getTile(int x, int y) {
        if (x < 0 || x >= mapWidth ||
                y < 0 || y >= mapHeight) {
            return null;
        } else {
            return tiles[x][y];
        }
    }

    /**
     * Sets the tile at the specified location.
     */
    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }

    /**
     * Gets an Iterator of all the Sprites in this map,
     * excluding the player Sprite.
     */

    public Iterator getSprites() {
        return sprites.iterator();
    }

    /**
     * Class method to convert a pixel position to a tile position.
     */

    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }

    /**
     * Class method to convert a pixel position to a tile position.
     */

    public static int pixelsToTiles(int pixels) {
        return (int) Math.floor((float) pixels / TILE_SIZE);
    }

    /**
     * Class method to convert a tile position to a pixel position.
     */

    public static int tilesToPixels(int numTiles) {
        return numTiles * TILE_SIZE;
    }

    /**
     * Draws the specified TileMap.
     */
    public void draw(Graphics2D g2) {
        int mapWidthPixels = tilesToPixels(mapWidth);

        // get the scrolling position of the map
        // based on player's position

        int offsetX = screenWidth / 2 - Math.round(player.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidthPixels);

        // draw the background first

        bgManager.draw(g2);
        scoreManager.draw(g2);
        // draw the visible tiles

        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
        for (int y = 0; y < mapHeight; y++) {
            for (int x = firstTileX; x <= lastTileX; x++) {
                Image image = getTile(x, y);
                if (image != null) {
                    g2.drawImage(image,
                            tilesToPixels(x) + offsetX,
                            tilesToPixels(y) + offsetY,
                            null);
                }
            }
        }

        // Updates the enemy animations.
        if (mainenemies != null) {
            for (Enemy a : mainenemies) {
                if (a != null) {
                    a.getcurrAnimation().update();
                }
            }
            

            // draw player
            // System.out.println("Player coordinates: " + Math.round(player.getX()) + "," +
            // Math.round(player.getY()));
            player.getCurrentAnimation().update();
            // playeranimation.drawsomewhere(g2, Math.round(player.getX()) + offsetX,
            // Math.round(player.getY()), 0);
            g2.drawImage(player.getImage(), Math.round(player.getX()) + offsetX, Math.round(player.getY()), null);
            // Rectangle bounds = player.getBounds();
            // g2.drawRect(bounds.x , bounds.y, bounds.width, bounds.height);



            // Hitboxes
            /*
            Rectangle bounds2 = player.getBounds();
            g2.drawRect(bounds2.x + offsetX, bounds2.y, bounds2.width, bounds2.height);

            // draw player attackbox
            Rectangle2D.Double attackRegion = getAttackRegion(player.getDirection());
            g2.drawRect((int) attackRegion.x + offsetX, (int) attackRegion.y, (int) attackRegion.width,
                    (int) attackRegion.height);

            // draw enemy hitbox
            if (mainenemies != null) {
                for (Enemy a : mainenemies) {
                    if (a != null) {
                        Rectangle enemyBounds = a.getBoundingRectangle();
                        g2.setColor(Color.BLACK);
                        g2.drawRect((int) enemyBounds.x + offsetX, (int) enemyBounds.y, (int) enemyBounds.width,
                                (int) enemyBounds.height);
                    }
                }
            }

            
 */
            // draw enemy attacking range hitbox
            // if attacking
            for (Enemy a : mainenemies) {
                if (isAttackingPlayer(a) && !a.isDead() && !a.takingDamage()) {
                    //Rectangle enemyBoundsattac = a.getAttackRange();
                    //take damage every 5 ticks
                    ticks++;
                    if (ticks % 15 == 0){
                    player.updateHealth(-40);
                    if(a instanceof BossEnemy)
                    player.updateHealth(-80);
                       // System.out.println("Player health: " + player.getHealth());
                }
                    // g2.drawRect((int) enemyBoundsattac.x + offsetX, (int) enemyBoundsattac.y,
                    //         (int) enemyBoundsattac.width, (int) enemyBoundsattac.height);
                }
            }

            
/*
            // draw enemy aggression range hitbox
            for (Enemy a : mainenemies) {
                Rectangle2D.Double enemyBounds = a.getDetectionArea();
                g2.drawRect((int) enemyBounds.x + offsetX, (int) enemyBounds.y, (int) enemyBounds.width,
                        (int) enemyBounds.height);
            }


            //draw cannon hitbox
            for (Cannon cannon : cannons) {
                Rectangle cannonBounds = cannon.getBounds();
                g2.drawRect((int) cannonBounds.x + offsetX, (int) cannonBounds.y, (int) cannonBounds.width,
                        (int) cannonBounds.height);
            }

            //draw canon attack hitbox
            for (Cannon cannon : cannons) {
                Rectangle cannonBounds = cannon.getBounds();
                g2.drawRect((int) cannonBounds.x + offsetX, (int) cannonBounds.y, (int) cannonBounds.width,
                        (int) cannonBounds.height);
            }


            //draw bullet hitbox
            for (Cannon cannon : cannons) {
                Bullet bullet = cannon.getBullet();
                if (bullet != null) {
                    Double bulletBounds = bullet.getBoundingRectangle();
                    g2.drawRect((int) bulletBounds.x, (int) bulletBounds.y, (int) bulletBounds.width,
                            (int) bulletBounds.height);
                }
            }

             */
           
            // drawing objects
            for (Collectible collectible : collectibles) {
                if (collectible.isVisible()) {
                    collectible.draw(g2); // Now calling the method without offsets
                }
            }
            for (Doubles Doubles : Doubles) {
                if (Doubles.isVisible()) {
                    Doubles.draw(g2); // Now calling the method without offsets
                }
            }
            for (Spikes spikes : spikes) {
                if (spikes.isVisible()) {
                    spikes.draw(g2); // Now calling the method without offsets
                }
            }
            for (Ammobox Ammobox : Ammoboxes) {
                if (Ammobox.isVisible()) {
                    Ammobox.draw(g2); // Now calling the method without offsets
                }
            }
            for (Objects objects : objects) {
                if (objects.isVisible()) {
                    objects.draw(g2); // Now calling the method without offsets
                }
            }
            for (Cannon cannon : cannons) {
                // System.out.println("Cannon at------------------: " + cannon.getX() + ", " +
                // cannon.getY());
                if (cannon.isVisible()) {
                    cannon.draw(g2, offsetX, offsetY);
                }
            }

            // draw cannon bullets for each
            for (Cannon cannon : cannons) {
                // System.out.println("Cannon at------------------: " + cannon.getX() + ", " +
                // cannon.getY());
                cannon.drawBullet(g2);
            }

            //draw player bullet
            player.drawBullet(g2);
            //drawing dash cooldown
            player.drawCooldown(g2);

            // Animation playerAnimation = player.getCurrentAnimation();

            // Draws the enemy animations.
            if (mainenemies != null) {

                for (Enemy a : mainenemies) {
                    if (a != null) {
                        // System.out.println("Drawing enemy animations");
                        // a.getcurrAnimation().drawsomewhere(imageContext, a.x, a.y, offsetX);
                        if (a instanceof BossEnemy) {
                            if (a.getDirectionWeight() == -1)
                            g2.drawImage(a.getImage(), a.x - 80 + offsetX, a.y - 36, 288,160,null);
                            else
                            g2.drawImage(a.getImage(), a.x - 80 + 288 + offsetX, a.y - 36, -288,160,null);
                        if (!a.isDead())
                            a.drawHealth(g2, offsetX);
                        }
                        else
                        {
                            g2.drawImage(a.getImage(), a.x + offsetX, a.y, null);
                            if (!a.isDead())
                                a.drawHealth(g2, offsetX);
                        }


                    }
                }
            }
        }

    }

    // Gets player attack region
    public Rectangle2D.Double getAttackRegion(String direction) {
        int attackX = player.getX();
        int attackY = player.getY();
        int attackRange = 50;
        int playerHeight = player.getHeight();
        int playerWidth = player.getWidth();

        if (direction.equals("Left")) {
            return new Rectangle2D.Double(attackX - attackRange, attackY, attackRange, playerHeight);
        } else {
            attackX += playerWidth;
            return new Rectangle2D.Double(attackX, attackY, attackRange, playerHeight);
        }
    }

    // Draws the enemy animations.
   
    public void moveLeft() {
        int x, y;
        x = player.getX();
        y = player.getY();

        String mess = "Going left. x = " + x + " y = " + y;
        // System.out.println(mess);

        player.move(1);

    }

    public void moveRight() {
        int x, y;
        x = player.getX();
        y = player.getY();

        //String mess = "Going right. x = " + x + " y = " + y;
        // System.out.println(mess);

        player.move(2);

    }

    public void shootAllCannonBullets(int x, int y) {
        for (Cannon cannon : cannons) {
            // print all cannon info to console
            /// System.out.println("Cannon at------------------: " + cannon.getX() + ", " +
            // cannon.getY());

            if (cannon.isVisible() && cannon != null && cannon == getAboveCannon()) {
                //System.out.println("Shooting cannon bullet!");
                cannon.shootBullet(x, y, getOffsetX(), offsetY);
            }

        }
    }


    // shooting player bullets
    public void playerShoot(int x, int y) {
        // if player is not null and not above a cannon
        if (player != null && !isAboveCannon()) {
            //System.out.println("Player shooting!");
            player.shootPlayerBullet(x, y, getOffsetX(), offsetY); // this plays the animation based on the direction

        }
        // else
        // System.out.println("Player null!");
    }

    //player dash
    public void playerDash() {
        if (player != null) {
            
            player.dash();
        }
    }



    public void playerAttack() {
        if (player != null  && player.canAttackNow()) {

            player.attack();

            Rectangle2D.Double attackRegion = getAttackRegion(player.getDirection());
            // System.out.println("Player attacking!" + player.getDirection());
            // check if the enemy is in the attacking region
            if ((mainenemies != null)) {
                for (Enemy a : mainenemies) {
                    if (a != null) {
                        if (attackRegion.intersects(a.getBoundingRectangle()) && !a.isDead()) {
                            a.updateHealth(50);
                            a.setX(a.getX() - 1 * a.getDirectionWeight() * a.getKnockbackDistance());
                            if (a instanceof BossEnemy) {
                                //cast type to boss enemy
                                BossEnemy bossEnemy = (BossEnemy) a;
                                bossEnemy.setAnimation();
                                //System.out.println("Boss Enemy hit!");
                            }
                        }
                    }
                }
            }

        }

    }

    public void jump() {
        int x, y;
        x = player.getX();
        y = player.getY();

        String mess = "Jumping. x = " + x + " y = " + y;
        // System.out.println(mess);

        player.move(3);

    }

    public static int getTileSize() {
        return TILE_SIZE;
    }

    public void update() {

        //check objects collision
        checkDoublesCollision();
        checkAmmoboxCollision();
        checkSpikesCollision();
        checkCollectibleCollision(player);

        //update player
        player.update();
        //update player bullet
        player.updateBullet();
        
        //update enemies
        updateEnemies();
        
        //update cannon bullets
        updateCannonBullets();
        
    }

    public Player getPlayer() {
        return player;
    }

    //get enemies 
    public Enemy[] getenemies() {
        return mainenemies;
    }
  

    // determine if entity is attacking a player
    public boolean isAttackingPlayer(Enemy a) {
        return a.getAttackRange().intersects(player.getBounds());
    }

    public void updateEnemies() {
        if (mainenemies != null) {
            for (Enemy a : mainenemies) {
                if (a != null) {
                    a.update();
                    a.engagePlayer(player.getBounds(), player.getX(), false);
                }
            }
        }
    }

    //update cannon bullets
    public void updateCannonBullets() {
        for (Cannon cannon : cannons) {
            cannon.updateBullet();
        }
    }

    //determines if player is inside cannon hitbox
    public boolean isAboveCannon() {
        for (Cannon cannon : cannons) {
            
            //if player bounds intersect with cannon bounds
            if (cannon.getBounds().intersects(player.getBounds())) {
                return true;
            }

            // if (cannon instanceof EnemyCannon)
            // return false;
        }
        return false;
    }

    //returns cannon the player is above of
    public Cannon getAboveCannon() {
        for (Cannon cannon : cannons) {
            //if player bounds intersect with cannon bounds
            if (cannon.getBounds().intersects(player.getBounds())) {
                return cannon;
            }
        }
        return null;
    }

    //sets direction of specific cannon
    public void setDirectionCannon(int mouseX) {
        for (Cannon cannon : cannons) {
            //if player bounds intersect with cannon bounds
            if (isAboveCannon()) {
                cannon.setDirection(mouseX - getOffsetX());
            }
        }
    }

    // get score manager
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
}
