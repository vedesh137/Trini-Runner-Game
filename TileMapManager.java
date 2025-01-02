import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.ImageIcon;


/**
    The ResourceManager class loads and manages tile Images and
    "host" Sprites used in the game. Game Sprites are cloned from
    "host" Sprites.
*/
public class TileMapManager {

    private ArrayList<Image> tiles;
    private int currentMap = 0;
    private Image AmmoImage;
    private Image CollectibleImage;
    private Image SpikeImage;
    private Image DontImage;
    private Image GoImage;
    private Image bushes;
    private Image cannonImage;
    private JFrame window;
    private ScoreManager ScoreManager;
    private Image DoublesImage;

    private Enemy[] enemies;
    private BossEnemy[] bossEnemies;

private static final int TILE_SIZE = 64;
    public TileMapManager(JFrame window, ScoreManager ScoreManager) {
	this.window = window;
    this.ScoreManager = ScoreManager;

        loadTileImages();
        CollectibleImage =  ImageManager.loadImageAndResize("images/Money.png", TILE_SIZE, TILE_SIZE);
        AmmoImage =  ImageManager.loadImageAndResize("images/Ammo.png", TILE_SIZE, TILE_SIZE);  
        DoublesImage =  ImageManager.loadImageAndResize("images/Doubles.png", TILE_SIZE, TILE_SIZE);
        SpikeImage =  ImageManager.loadImageAndResize("images/Spikes.png", TILE_SIZE, TILE_SIZE);
        Image originalCannonImage = new ImageIcon("images/Cannon.png").getImage();
        DontImage = ImageManager.loadImageAndResize("images/dont.png", TILE_SIZE, TILE_SIZE);
        GoImage = ImageManager.loadImageAndResize("images/GO.png", TILE_SIZE, TILE_SIZE);
        bushes = ImageManager.loadImageAndResize("images/bushes.png", TILE_SIZE, TILE_SIZE);
        cannonImage = ImageManager.resizeImage(originalCannonImage, TileMap.getTileSize(), TileMap.getTileSize());
    }


     public TileMap loadMap(String filename)
        throws IOException
    {
        ArrayList<String> lines = new ArrayList<String>();
        int mapWidth = 0;
        int mapHeight = 0;

        // read every line in the text file into the list

        BufferedReader reader = new BufferedReader(
            new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
                mapWidth = Math.max(mapWidth, line.length());
            }
        }

        // parse the lines to create a TileMap
        mapHeight = lines.size();

        TileMap newMap = new TileMap(window, mapWidth, mapHeight);
        for (int y=0; y<mapHeight; y++) {
            String line = lines.get(y);
            for (int x=0; x<line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, tiles.get(tile));
                }
                else if (ch == 'o') {
                    newMap.addCollectible(CollectibleImage, x, y,64,64);  
                }
                else if (ch == 'p') {
                    newMap.addCannon(cannonImage, x, y, 0);  
                    newMap.setTile(x, y, tiles.get('O' - 'A')); // Placing an invisible tile over cannon so that it gets the properities of a solid tile and player can stand on top of it
                }
                else if (ch == 'r') {
                    newMap.addSpikes(SpikeImage, x, y,64,64);  
                }
                else if (ch == 'q') {
                    newMap.addObjects(DontImage, x, y,64,64);  
                }
                else if (ch == 's') {
                    newMap.addObjects(GoImage, x, y,64,64);  
                }
                else if (ch == 't') {
                    //code to spawn enemy
                    newMap.placeBasicenemy(x, y, 0);      
                }
                else if (ch == 'u') {
                    //code to spawn enemy
                    newMap.placeBasicenemy(x, y - 1, 1); 
                    //newMap.printEnemyTypes();
                }
                else if (ch == 'v') {
                    newMap.addAmmo(AmmoImage, x, y,64,64);  
                }
                else if (ch == 'w') {
                    newMap.addCannon(cannonImage, x, y, 1);  
                    newMap.setTile(x, y, tiles.get('O' - 'A')); // Placing an invisible tile over cannon so that it gets the properities of a solid tile and player can stand on top of it
                }
                
                else if (ch == '1') {
                    newMap.addObjects(bushes, x, y,64,64);  
                }
                else if (ch == '2') {
                    newMap.addDoubles(DoublesImage, x, y,64,64);  
                }

            }
        }

        return newMap;
    }


    public void loadTileImages() {
        // keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ folder

	File file;

	System.out.println("loadTileImages called.");

        tiles = new ArrayList<Image>();
        char ch = 'A';
        while (true) {
            String filename = "images/tile_" + ch + ".png";
	    file = new File(filename);
            if (!file.exists()) {
		System.out.println("Image file could not be opened: " + filename);
                break;
            }
	    else
		System.out.println("Image file opened: " + filename);
		Image tileImage = new ImageIcon(filename).getImage();
           	tiles.add(tileImage);
            ch++;
        }
    }


}
