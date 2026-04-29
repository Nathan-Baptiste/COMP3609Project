import java.awt.*;
import javax.swing.JPanel;
import javax.xml.transform.sax.SAXResult;

public class Player {

   private static final int DX = 12;	// amount of X pixels to move in one keystroke
   private static final int DY = 32;	// amount of Y pixels to move in one keystroke
	protected static final int SCALE = 1;

   private static final int TILE_SIZE = 64;

	// HIT EFFECT
	protected Image hitImage;
	protected boolean gettingHit = false;
	private int hitTimer = 0;
	private static final int HIT_DURATION = 8; // ~0.4 sec

	private static final int KNOCKBACK = 25;

   private JPanel panel;		// reference to the JFrame on which player is drawn
   protected TileMap tileMap;
   private BackgroundManager bgManager;

   private int x;			// x-position of player's sprite
   private int y;			// y-position of player's sprite

	protected int collisionWidth = -1;
	protected int collisionHeight = -1;

   Graphics2D g2;
   private Dimension dimension;

	protected StripAnimation idleAnim;
	protected StripAnimation runAnim;
	protected StripAnimation jumpAnim;
	protected StripAnimation attackAnim;
	protected StripAnimation moveAttackAnim;
	protected StripAnimation jumpAttackAnim;
	protected StripAnimation chargeAnim;
	protected StripAnimation shootAnim;
	protected StripAnimation jumpChargeAnim;
	protected StripAnimation jumpShootAnim;
	protected StripAnimation blockAnim;
	protected Image deathImage;

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

	protected boolean attacking = false;
	protected boolean moveAttacking = false;
	protected boolean jumpAttacking = false;

	protected boolean blocking = false;
	private int blockTimer = 0;
	private static final int BLOCK_DURATION = 10;

	private boolean blockOnCooldown = false;
	private int blockCooldownTimer = 0;
	private static final int BLOCK_COOLDOWN = 10;

	protected boolean charging = false;
	protected int chargeTime = 0;
	protected boolean shooting = false;
	protected int shootTimer = 0;

	protected int health = 5;
	protected boolean dead = false;
	private static final int MAX_HEALTH = 5;
	private int damageCooldown = 0;
	private static final int DAMAGE_COOLDOWN_MAX = 6;

	private boolean invincible = false;
	private int invincibleTimer = 0;
	private static final int INVINCIBLE_TIME = 20; // ~1 second (20 * 50ms)

	private boolean visible = true; // for flashing

   public Player (JPanel panel, TileMap t, BackgroundManager b) {
      this.panel = panel;

      tileMap = t;			// tile map on which the player's sprite is displayed
      bgManager = b;			// instance of BackgroundManager

      goingUp = goingDown = false;
      inAir = false;

   }


    public Point collidesWithTile(int newX, int newY) {

        int offsetY = tileMap.getOffsetY();
        int playerHeight = getDisplayHeight();
        int xTile = tileMap.pixelsToTiles(newX);

        int yTileTop    = tileMap.pixelsToTiles(newY - offsetY);
        int yTileBottom = tileMap.pixelsToTiles(newY - offsetY + playerHeight - 1);

        for (int yTile = yTileTop; yTile <= yTileBottom; yTile++) {
            if (tileMap.getTile(xTile, yTile) != null) {
                return new Point(xTile, yTile);
            }
        }

        return null;
    }

    public Point collidesWithTileAtPoint(int newX, int newY) {
        int offsetY = tileMap.getOffsetY();
        int xTile = tileMap.pixelsToTiles(newX);
        int yTile = tileMap.pixelsToTiles(newY - offsetY);

        if (tileMap.getTile(xTile, yTile) != null) {
            return new Point(xTile, yTile);
        }
        return null;
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

   public synchronized void move (int direction) {

	   if (blocking) return;
	   if (gettingHit) return;
	   if (dead) return;

      int newX = x;
      Point tilePos = null;

      if (!panel.isVisible ()) return;
	  if ((attacking && !moveAttacking) && !inAir && !jumping) return;

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
             x = ((int) tilePos.getX() + 1) * TILE_SIZE;	   // keep flush with right side of tile
	 }
         else
         if (direction == 2) {

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
		  fall();
	  }
      }
   }


	public boolean isInAir() {

		if (!jumping && !inAir) {

			int playerHeight = getDisplayHeight();
			int playerWidth = getDisplayWidth();

			Point tilePosLeft  = collidesWithTileAtPoint(x, y + playerHeight + 1);
			Point tilePosRight = collidesWithTileAtPoint(x + playerWidth - 1, y + playerHeight + 1);

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

	   if (dead) return;
	   if (blocking) return;

      if (!panel.isVisible () || jumping || inAir) return;
	  if (attacking && !inAir) return;
	  if (moveAttacking) return;

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

	   if (blocking) {

		   blockAnim.update();
		   blockTimer--;

		   // force stop when time ends
		   if (blockTimer <= 0) {
			   blocking = false;
			   blockTimer = 0;

			   // START COOLDOWN
			   blockOnCooldown = true;
			   blockCooldownTimer = BLOCK_COOLDOWN;
		   }

	   }

	   // BLOCK COOLDOWN TIMER
	   if (blockOnCooldown) {
		   blockCooldownTimer--;

		   if (blockCooldownTimer <= 0) {
			   blockOnCooldown = false;
			   blockCooldownTimer = 0;
		   }
	   }



	   if (gettingHit) {
		   attacking = false;
		   moveAttacking = false;
		   jumpAttacking = false;
	   }

	   if (dead) {
		   moving = false;
		   jumping = false;
		   attacking = false;
		   charging = false;
		   shooting = false;
		   fall();
	   }

	   if (attacking && !gettingHit) {
		   attackAnim.update();

		   if (attackAnim.isFinished()) {
			   attacking = false;
		   }
	   }

	   if (moveAttacking) {
		   moveAttackAnim.update();

		   if (moveAttackAnim.isFinished()) {
			   moveAttacking = false;
		   }
	   }

	   if (jumpAttacking) {
		   jumpAttackAnim.update();

		   if (jumpAttackAnim.isFinished()) {
			   jumpAttacking = false;
		   }
	   }

	   if (chargeAnim != null)
		   chargeAnim.update();

	   if (shootAnim != null)
		   shootAnim.update();

	   if (jumpChargeAnim != null)
		   jumpChargeAnim.update();

	   if (jumpShootAnim != null)
		   jumpShootAnim.update();

      timeElapsed++;

	   if (charging) {
		   chargeTime++;
	   }

	   if (shooting) {
		   shootTimer--;

		   if (shootTimer <= 0) {
			   shooting = false;
			   shootTimer = 0;
			   shootAnim.start();
		   }
	   }

	   if (damageCooldown > 0) {
		   damageCooldown--;
	   }

	   // Handle invincibility + flashing
	   if (invincible) {
		   invincibleTimer--;

		   // toggle visibility every few frames
		   visible = (invincibleTimer % 4 < 2);

		   if (invincibleTimer <= 0) {
			   invincible = false;
			   visible = true;
		   }
	   }

	   // HIT TIMER
	   if (gettingHit) {
		   hitTimer--;
		   if (hitTimer <= 0) {
			   gettingHit = false;
		   }
	   }

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

      	  		int offsetY = tileMap.getOffsetY();
			int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;
			int bottomTileY = topTileY + TILE_SIZE;

		   	y = bottomTileY;
		   	fall();
		}
	   	else {
			y = newY;
	   	}
            }
	    else
	    if (goingDown) {
		Point tilePos = collidesWithTileDown (x, newY);
	   	if (tilePos != null) {				// hits a tile going up

			int playerHeight = getDisplayHeight();
		    goingDown = false;

			int offsetY = tileMap.getOffsetY();
		    int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;

			y = topTileY - playerHeight;

	  	    jumping = false;
		    inAir = false;
			jumpAttacking = false;
	       }
	       else {
		    y = newY;
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

	public int getHealth() {
		return health;
	}

	public boolean takeDamage(int dmg, boolean hitFromRight) {
		if (invincible || dead || blocking) return false;

		health -= dmg;

		// KNOCKBACK
		if (hitFromRight) {
			x -= KNOCKBACK; // hit from right → push left
		} else {
			x += KNOCKBACK; // hit from left → push right
		}

		// clamp inside map
		if (x < 0) x = 0;
		if (x > tileMap.getWidthPixels() - getDisplayWidth())
			x = tileMap.getWidthPixels() - getDisplayWidth();

		// HIT STATE
		gettingHit = true;
		hitTimer = HIT_DURATION;

		if (health <= 0) {
			health = 0;
			dead = true;
			visible = true;
		}

		invincible = true;
		invincibleTimer = INVINCIBLE_TIME;

		return true;
	}

	public void startBlock() {
		if (dead || gettingHit) return;
		if (blockOnCooldown) return;

		if (!blocking) {
			blocking = true;
			blockTimer = BLOCK_DURATION;

			blockAnim.start();
		}
	}

	public void stopBlock() {
		blocking = false;
		blockTimer = 0;
	}

	public Image getImage() {

		if (gettingHit && hitImage != null) {
			return hitImage;
		}

		if (dead) {
			return deathImage;
		}

		if (blocking) {
			return blockAnim.getImage();
		}

	   //P1 Attack States
		if (jumpAttacking)
			return jumpAttackAnim.getImage();

		if (moveAttacking)
			return moveAttackAnim.getImage();

		//Air States
		if (inAir || jumping) {
			if (charging && jumpChargeAnim != null)
				return jumpChargeAnim.getImage();

			if (shooting && jumpShootAnim != null)
				return jumpShootAnim.getImage();

			return jumpAnim.getImage();
		}

		//Ground States
		if (attacking)
			return attackAnim.getImage();

		if (charging)
			return chargeAnim.getImage();

		if (shooting && !moving)
			return shootAnim.getImage();

		if (moving)
			return runAnim.getImage();

		return idleAnim.getImage();
	}

	public int getDisplayWidth() {
		return (idleAnim.getImage().getWidth(null) * SCALE) - 20;
	}
	public int getDisplayHeight() {
		return (idleAnim.getImage().getHeight(null) * SCALE) + 45;
	}

	public void respawn(int x, int y) {
		setX(x);
		setY(y);

		health = MAX_HEALTH;
		dead = false;

		visible = true;
		invincible = false;
		invincibleTimer = 0;

		fall();
	}

	public Rectangle getHitBox() {
		int width = getDisplayWidth() ;
		int height = getDisplayHeight();

		return new Rectangle(x, y, width, height);
	}


	public boolean isAttackActiveFrame() {

		int frame = -1;

		if (attacking)
			frame = attackAnim.getCurrentFrame();
		else if (moveAttacking)
			frame = moveAttackAnim.getCurrentFrame();
		else if (jumpAttacking)
			frame = jumpAttackAnim.getCurrentFrame();

		// Frames 2, 3, 4 (0-based index = 3rd, 4th, 5th frames)
		return frame >= 2 && frame <= 4;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isDead() {
		return dead;
	}

}