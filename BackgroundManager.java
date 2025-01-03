

import java.awt.Graphics2D;
import javax.swing.JFrame;


public class BackgroundManager {

	private String[] bgImages = {
        "images/backgrounds/layer2_07.png",
        "images/backgrounds/layer2_06.png",
        "images/backgrounds/layer2_05.png",
        "images/backgrounds/layer2_04.png",
        "images/backgrounds/layer2_03.png",
        "images/backgrounds/layer2_02.png",
        "images/backgrounds/layer2_01.png"};

  	private int moveAmount[] = {1, 2, 3, 4, 4, 4, 5, 10};  
						// pixel amounts to move each background left or right
     						// a move amount of 0 makes a background stationary

  	private Background[] backgrounds;
  	private int numBackgrounds;

  	private JFrame window;			// JFrame on which backgrounds are drawn

  	public BackgroundManager(JFrame window, int moveSize) {
						// ignore moveSize
    		this.window = window;

    		numBackgrounds = bgImages.length;
    		backgrounds = new Background[numBackgrounds];

    		for (int i = 0; i < numBackgrounds; i++) {
       			backgrounds[i] = new Background(window, bgImages[i], moveAmount[i]);
    		}
  	} 


  	public void moveRight() { 
		for (int i=0; i < numBackgrounds; i++)
      			backgrounds[i].moveRight();
  	}


  	public void moveLeft() {
		for (int i=0; i < numBackgrounds; i++)
      			backgrounds[i].moveLeft();
  	}


  	// The draw method draws the backgrounds on the screen. The
  	// backgrounds are drawn from the back to the front.

  	public void draw (Graphics2D g2) { 
		for (int i=0; i < numBackgrounds; i++)
      			backgrounds[i].draw(g2);
  	}

}

