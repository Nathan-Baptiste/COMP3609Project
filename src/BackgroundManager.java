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

	private String bgImages[];

  	private int[] moveAmount;

  	private Background[] backgrounds;
  	private int numBackgrounds;

  	private JPanel panel;			// JPanel on which backgrounds are drawn

  	public BackgroundManager(JPanel panel, int moveSize, int level) {
    		this.panel = panel;

			if (level == 1) {          //for forest themed level
				bgImages = new String[] {
						"src/images/Level1/Background/Sky.png",
						"src/images/Level1/Background/Clouds.png",
						"src/images/Level1/Background/Bg2Grass.png",
						"src/images/Level1/Background/Bg1Grass.png",
						"src/images/Level1/Background/Bg0Grass.png"
				};

				moveAmount = new int[]{1, 2, 4, 5, 10};
			}

			if (level == 2) {            //For castle themed level
				bgImages = new String[] {
						"src/images/Level2/Background/Bg0Wall.png",
						"src/images/Level2/Background/Bg1Pillar.png"
				};

				moveAmount = new int[]{1, 4};
			}


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

