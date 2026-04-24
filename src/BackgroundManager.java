/* BackgroundManager manages many backgrounds (wraparound images 
   used for the game's background). 

   Backgrounds 'further back' move slower than ones nearer the
   foreground of the game, creating a parallax distance effect.

   When a sprite is instructed to move left or right, the sprite
   doesn't actually move, instead the backgrounds move in the 
   opposite direction (right or left).

*/

import java.awt.Graphics2D;
import javax.swing.JPanel;


public class BackgroundManager {

	private String bgImages[] = {"src/images/Level1/Background/Sky.png",
								"src/images/Level1/Background/Clouds.png",
			       	     		"src/images/Level1/Background/Bg2Grass.png",
				     			"src/images/Level1/Background/Bg1Grass.png",
				     			"src/images/Level1/Background/Bg0Grass.png"};

  	private int moveAmount[] = {1, 2, 3, 5, 10};
						// pixel amounts to move each background left or right
     						// a move amount of 0 makes a background stationary

  	private Background[] backgrounds;
  	private int numBackgrounds;

  	private JPanel panel;			// JPanel on which backgrounds are drawn

  	public BackgroundManager(JPanel panel, int moveSize) {
						// ignore moveSize
    		this.panel = panel;

    		numBackgrounds = bgImages.length;
    		backgrounds = new Background[numBackgrounds];

    		for (int i = 0; i < numBackgrounds; i++) {
       			backgrounds[i] = new Background(panel, bgImages[i], moveAmount[i]);
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

