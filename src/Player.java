import java.awt.*;
import javax.swing.JPanel;
import javax.xml.transform.sax.SAXResult;

public class Player {			

   private static final int DX = 12;	// amount of X pixels to move in one keystroke
   private static final int DY = 32;	// amount of Y pixels to move in one keystroke
	protected static final int SCALE = 1;

   private static final int TILE_SIZE = 64;

   private JPanel panel;		// reference to the JFrame on which player is drawn
   private TileMap tileMap;
   private BackgroundManager bgManager;

   private int x;			// x-position of player's sprite
   private int y;			// y-position of player's sprite

   Graphics2D g2;
   private Dimension dimension;

	protected StripAnimation idleAnim;
	protected StripAnimation runAnim;
	protected StripAnimation jumpAnim;

	protected boolean facingRight = true;

   protected boolean jumping;
   protected boolean moving;
   private int timeElapsed;
   private int startY;

   private boolean goingUp;
   private boolean goingDown;

   protected boolean inAir;
   private int initialVelocity;
   private int startAir;

   public Player (JPanel panel, TileMap t, BackgroundManager b) {
      this.panel = panel;

      tileMap = t;			// tile map on which the player's sprite is displayed
      bgManager = b;			// instance of BackgroundManager

      goingUp = goingDown = false;
      inAir = false;

   }


   public Point collidesWithTile(int newX, int newY) {


	   int playerWidth = getDisplayWidth();
	   int offsetY = tileMap.getOffsetY();
	   int xTile = tileMap.pixelsToTiles(newX);
	   int yTile = tileMap.pixelsToTiles(newY - offsetY);

	  if (tileMap.getTile(xTile, yTile) != null) {
	        Point tilePos = new Point (xTile, yTile);
	  	return tilePos;
	  }
	  else {
		return null;
	  }
   }


   public Point collidesWithTileDown (int newX, int newY) {


	   int playerWidth = getDisplayWidth();
	   int playerHeight = getDisplayHeight();
	   int offsetY = tileMap.getOffsetY();
	  int xTile = tileMap.pixelsToTiles(newX);
	  int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
	  int yTileTo = tileMap.pixelsToTiles(newY - offsetY + playerHeight);

	  for (int yTile=yTileFrom; yTile<=yTileTo; yTile++) {
		if (tileMap.getTile(xTile, yTile) != null) {
	        	Point tilePos = new Point (xTile, yTile);
	  		return tilePos;
	  	}
		else {
			if (tileMap.getTile(xTile+1, yTile) != null) {
				int leftSide = (xTile + 1) * TILE_SIZE;
				if (newX + playerWidth > leftSide) {
				    Point tilePos = new Point (xTile+1, yTile);
				    return tilePos;
			        }
			}
		}
	  }

	  return null;
   }


   public Point collidesWithTileUp (int newX, int newY) {


	   int playerWidth = getDisplayWidth();

      	  int offsetY = tileMap.getOffsetY();
	  int xTile = tileMap.pixelsToTiles(newX);

	  int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
	  int yTileTo = tileMap.pixelsToTiles(newY - offsetY);

	  for (int yTile=yTileFrom; yTile>=yTileTo; yTile--) {
		if (tileMap.getTile(xTile, yTile) != null) {
	        	Point tilePos = new Point (xTile, yTile);
	  		return tilePos;
		}
		else {
			if (tileMap.getTile(xTile+1, yTile) != null) {
				int leftSide = (xTile + 1) * TILE_SIZE;
				if (newX + playerWidth > leftSide) {
				    Point tilePos = new Point (xTile+1, yTile);
				    return tilePos;
			        }
			}
		}

	  }

	  return null;
   }

/*

   public Point collidesWithTile(int newX, int newY) {

	 int playerWidth = playerImage.getWidth(null);
	 int playerHeight = playerImage.getHeight(null);

      	 int fromX = Math.min (x, newX);
	 int fromY = Math.min (y, newY);
	 int toX = Math.max (x, newX);
	 int toY = Math.max (y, newY);

	 int fromTileX = tileMap.pixelsToTiles (fromX);
	 int fromTileY = tileMap.pixelsToTiles (fromY);
	 int toTileX = tileMap.pixelsToTiles (toX + playerWidth - 1);
	 int toTileY = tileMap.pixelsToTiles (toY + playerHeight - 1);

	 for (int x=fromTileX; x<=toTileX; x++) {
		for (int y=fromTileY; y<=toTileY; y++) {
			if (tileMap.getTile(x, y) != null) {
				Point tilePos = new Point (x, y);
				return tilePos;
			}
		}
	 }

	 return null;
   }
*/


   public synchronized void move (int direction) {

      int newX = x;
      Point tilePos = null;

      if (!panel.isVisible ()) return;

      if (direction == 1) {		// move left
		  moving = true;
		  facingRight = false;
          newX = x - DX;
	  if (newX < 0) {
		x = 0;
		return;
	  }

	  tilePos = collidesWithTile(newX, y);
      }
      else
      if (direction == 2) {		// move right
		  moving = true;
		  facingRight = true;

		  int playerWidth = getDisplayWidth();
          newX = x + DX;

      	  int tileMapWidth = tileMap.getWidthPixels();

	  if (newX + playerWidth >= tileMapWidth) {
	      x = tileMapWidth - playerWidth;
	      return;
	  }

	  tilePos = collidesWithTile(newX+playerWidth, y);
      }
      else				// jump
      if (direction == 3 && !jumping && !inAir) {
          jump();
	  return;
      }

      if (tilePos != null) {
         if (direction == 1) {
	     System.out.println (": Collision going left");
             x = ((int) tilePos.getX() + 1) * TILE_SIZE;	   // keep flush with right side of tile
	 }
         else
         if (direction == 2) {
	     System.out.println (": Collision going right");

			 int playerWidth = getDisplayWidth();
			 int playerHeight = getDisplayHeight();
             x = ((int) tilePos.getX()) * TILE_SIZE - playerWidth; // keep flush with left side of tile
	 }
      }
      else {
          if (direction == 1) {
	      x = newX;
	      bgManager.moveLeft();
          }
	  else
	  if (direction == 2) {
	      x = newX;
	      bgManager.moveRight();
   	  }

	  if (isInAir()) {
		  System.out.println("In the air. Starting to fall.");
		  fall();
	  }
      }
   }


	public boolean isInAir() {

		if (!jumping && !inAir) {

			int playerHeight = getDisplayHeight();
			int playerWidth = getDisplayWidth();

			Point tilePosLeft  = collidesWithTile(x, y + playerHeight + 1);
			Point tilePosRight = collidesWithTile(x + playerWidth - 1, y + playerHeight + 1);

			if (tilePosLeft == null && tilePosRight == null)
				return true;
			else
				return false;
		}

		return false;
	}


   protected void fall() {

      jumping = false;
      inAir = true;
      timeElapsed = 0;

      goingUp = false;
      goingDown = true;

      startY = y;
      initialVelocity = 0;
   }


   public void jump () {

      if (!panel.isVisible () || jumping || inAir) return;

      jumping = true;
      timeElapsed = 0;

      goingUp = true;
      goingDown = false;

      startY = y;
      initialVelocity = 50;
   }


   public void update () {
      int distance = 0;
      int newY = 0;

	   idleAnim.update();
	   runAnim.update();
	   jumpAnim.update();

      timeElapsed++;

      if (jumping || inAir) {
	   distance = (int) (initialVelocity * timeElapsed -
                             3.0 * timeElapsed * timeElapsed);
	   newY = startY - distance;

	   if (newY > y && goingUp) {
		goingUp = false;
 	  	goingDown = true;
	   }

	   if (goingUp) {
		Point tilePos = collidesWithTileUp (x, newY);
	   	if (tilePos != null) {				// hits a tile going up
		   	System.out.println ("Jumping: Collision Going Up!");

      	  		int offsetY = tileMap.getOffsetY();
			int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;
			int bottomTileY = topTileY + TILE_SIZE;

		   	y = bottomTileY;
		   	fall();
		}
	   	else {
			y = newY;
			System.out.println ("Jumping: No collision.");
	   	}
            }
	    else
	    if (goingDown) {
		Point tilePos = collidesWithTileDown (x, newY);
	   	if (tilePos != null) {				// hits a tile going up
		    System.out.println ("Jumping: Collision Going Down!");

			int playerHeight = getDisplayHeight();
		    goingDown = false;

      	            int offsetY = tileMap.getOffsetY();
		    int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;

	            y = topTileY - playerHeight;
	  	    jumping = false;
		    inAir = false;
	       }
	       else {
		    y = newY;
		    System.out.println ("Jumping: No collision.");
	       }
	   }
      }

	   moving = false;
   }


   public void moveUp () {

      if (!panel.isVisible ()) return;

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
		if (inAir || jumping) {
			return jumpAnim.getImage(); // fallback for now
		}
		else if (moving) {
			return runAnim.getImage();
		}
		else {
			return idleAnim.getImage();
		}
	}

	public int getDisplayWidth() {
		return idleAnim.getImage().getWidth(null) * SCALE;
	}
	public int getDisplayHeight() {
		return idleAnim.getImage().getHeight(null) * SCALE;
	}

	public void respawn(int x, int y) {
		setX(x);
		setY(y);
		fall();
	}

	public Rectangle getHitBox() {
		int width = getDisplayWidth();
		int height = getDisplayHeight();

		return new Rectangle(x, y, width, height);
	}

}