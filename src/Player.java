import java.awt.*;
import javax.swing.JPanel;

public class Player {

	private static final int DX = 12;
	private static final int DY = 32;
	protected static final int SCALE = 1;

	private static final int MAX_SPEED = 20;
	private static final int ACCEL = 1;
	protected int currentSpeed = 12;

	private static final int TILE_SIZE = 64;
	private static final int KNOCKBACK = 25;
	private static final int HIT_DURATION = 8;
	private static final int MAX_HEALTH = 5;
	private static final int DAMAGE_COOLDOWN_MAX = 6;
	private static final int INVINCIBLE_TIME = 20;
	private static final int BLOCK_DURATION = 10;
	private static final int BLOCK_PUSH = 24;
	private static final int BLOCK_COOLDOWN = 10;

	private SoundManager soundManager;
	private JPanel panel;
	protected TileMap tileMap;
	private BackgroundManager bgManager;

	private int x;
	private int y;
	private int timeElapsed;
	private int startY;
	private int initialVelocity;
	private int startAir;
	private int hitTimer = 0;
	private int blockTimer = 0;
	private int blockCooldownTimer = 0;
	private int damageCooldown = 0;
	private int invincibleTimer = 0;

	protected int collisionWidth = -1;
	protected int collisionHeight = -1;
	protected int hitSoundCooldown = 0;
	protected int deathSoundCooldown = 0;
	protected int chargeTime = 0;
	protected int shootTimer = 0;
	protected int health = 5;

	private boolean goingUp;
	private boolean goingDown;
	private boolean invincible = false;
	private boolean visible = true;
	private boolean blockOnCooldown = false;

	protected boolean facingRight = true;
	protected boolean jumping;
	protected boolean moving;
	protected boolean inAir;
	protected boolean attacking = false;
	protected boolean moveAttacking = false;
	protected boolean jumpAttacking = false;
	protected boolean blocking = false;
	protected boolean charging = false;
	protected boolean shooting = false;
	protected boolean gettingHit = false;
	protected boolean dead = false;

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
	protected Image hitImage;

	public Player(JPanel panel, TileMap t, BackgroundManager b) {
		this.panel = panel;
		soundManager = SoundManager.getInstance();
		tileMap = t;
		bgManager = b;
		goingUp = false;
		goingDown = false;
		inAir = false;
	}

	public Point collidesWithTile(int newX, int newY) {
		int offsetY = tileMap.getOffsetY();
		int playerHeight = getDisplayHeight();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTileTop = tileMap.pixelsToTiles(newY - offsetY);
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

	public Point collidesWithTileDown(int newX, int newY) {
		int playerWidth = getDisplayWidth();
		int playerHeight = getDisplayHeight();
		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
		int yTileTo = tileMap.pixelsToTiles(newY - offsetY + playerHeight);

		for (int yTile = yTileFrom; yTile <= yTileTo; yTile++) {
			if (tileMap.getTile(xTile, yTile) != null) {
				return new Point(xTile, yTile);
			} else {
				if (tileMap.getTile(xTile + 1, yTile) != null) {
					int leftSide = (xTile + 1) * TILE_SIZE;
					if (newX + playerWidth > leftSide) {
						return new Point(xTile + 1, yTile);
					}
				}
			}
		}

		return null;
	}

	public Point collidesWithTileUp(int newX, int newY) {
		int playerWidth = getDisplayWidth();
		int offsetY = tileMap.getOffsetY();
		int xTile = tileMap.pixelsToTiles(newX);
		int yTileFrom = tileMap.pixelsToTiles(y - offsetY);
		int yTileTo = tileMap.pixelsToTiles(newY - offsetY);

		for (int yTile = yTileFrom; yTile >= yTileTo; yTile--) {
			if (tileMap.getTile(xTile, yTile) != null) {
				return new Point(xTile, yTile);
			} else {
				if (tileMap.getTile(xTile + 1, yTile) != null) {
					int leftSide = (xTile + 1) * TILE_SIZE;
					if (newX + playerWidth > leftSide) {
						return new Point(xTile + 1, yTile);
					}
				}
			}
		}

		return null;
	}

	public synchronized void move(int direction) {
		if (blocking) return;
		if (gettingHit) return;
		if (dead) return;
		if (!panel.isVisible()) return;
		if ((attacking && !moveAttacking) && !inAir && !jumping) return;

		int newX = x;
		Point tilePos = null;

		if (direction == 1) {
			moving = true;
			facingRight = false;
			newX = x - currentSpeed;

			if (newX < 0) {
				x = 0;
				return;
			}

			tilePos = collidesWithTile(newX, y);
		} else if (direction == 2) {
			moving = true;
			facingRight = true;

			int playerWidth = getDisplayWidth();
			newX = x + currentSpeed;
			int tileMapWidth = tileMap.getWidthPixels();

			if (newX + playerWidth >= tileMapWidth) {
				x = tileMapWidth - playerWidth;
				return;
			}

			tilePos = collidesWithTile(newX + playerWidth, y);
		} else if (direction == 3 && !jumping && !inAir) {
			jump();
			return;
		}

		if (tilePos != null) {
			if (direction == 1) {
				x = ((int) tilePos.getX() + 1) * TILE_SIZE;
			} else if (direction == 2) {
				int playerWidth = getDisplayWidth();
				x = ((int) tilePos.getX()) * TILE_SIZE - playerWidth;
			}
		} else {
			if (direction == 1) {
				int offsetXBefore = tileMap.getOffsetX();
				x = newX;

				if (offsetXBefore < 0) {
					bgManager.moveLeft();
				}
			} else if (direction == 2) {
				int offsetXBefore = tileMap.getOffsetX();
				x = newX;

				if (offsetXBefore < 0) {
					bgManager.moveRight();
				}
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

			Point tilePosLeft = collidesWithTileAtPoint(x, y + playerHeight + 1);
			Point tilePosRight = collidesWithTileAtPoint(x + playerWidth - 1, y + playerHeight + 1);

			if (tilePosLeft == null && tilePosRight == null) {
				return true;
			} else {
				return false;
			}
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

	public void jump() {
		if (dead) return;
		if (blocking) return;
		if (!panel.isVisible() || jumping || inAir) return;
		if (attacking && !inAir) return;
		if (moveAttacking) return;

		playJumpSound();

		jumping = true;
		timeElapsed = 0;
		goingUp = true;
		goingDown = false;
		startY = y;
		initialVelocity = 50;
	}

	public void update() {
		int distance = 0;
		int newY = 0;

		idleAnim.update();
		runAnim.update();
		jumpAnim.update();

		if (hitSoundCooldown > 0) {
			hitSoundCooldown--;
		}

		if (deathSoundCooldown > 0) {
			deathSoundCooldown--;
		}

		if (blocking) {
			blockAnim.update();
			blockTimer--;

			if (blockTimer <= 0) {
				blocking = false;
				blockTimer = 0;
				blockOnCooldown = true;
				blockCooldownTimer = BLOCK_COOLDOWN;
			}
		}

		if (blockOnCooldown) {
			blockCooldownTimer--;

			if (blockCooldownTimer <= 0) {
				soundManager.playSound("recharge", false);
				blockOnCooldown = false;
				blockCooldownTimer = 0;
			}
		}

		if (gettingHit) {
			attacking = false;
			moveAttacking = false;
			jumpAttacking = false;

			if (hitSoundCooldown == 0) {
				playHitSound();
				hitSoundCooldown = 20;
			}
		}

		if (dead) {
			moving = false;
			jumping = false;
			attacking = false;
			charging = false;
			shooting = false;

			if (deathSoundCooldown == 0) {
				playDeathSound();
				deathSoundCooldown = 1000;
			}

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

		if (chargeAnim != null) {
			chargeAnim.update();
		}

		if (shootAnim != null) {
			shootAnim.update();
		}

		if (jumpChargeAnim != null) {
			jumpChargeAnim.update();
		}

		if (jumpShootAnim != null) {
			jumpShootAnim.update();
		}

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

		if (invincible) {
			invincibleTimer--;

			if (invincibleTimer % 4 < 2) {
				visible = true;
			} else {
				visible = false;
			}

			if (invincibleTimer <= 0) {
				invincible = false;
				visible = true;
			}
		}

		if (gettingHit) {
			hitTimer--;

			if (hitTimer <= 0) {
				gettingHit = false;
			}
		}

		int screenBottom = panel.getHeight();

		if (!dead && y > screenBottom + 200) {
			health = 0;
			dead = true;
			moving = false;
			jumping = false;
			attacking = false;
			charging = false;
			shooting = false;
			blocking = false;
			fall();
			return;
		}

		if (jumping || inAir) {
			distance = (int) (initialVelocity * timeElapsed - 3.0 * timeElapsed * timeElapsed);
			newY = startY - distance;

			if (newY > y && goingUp) {
				goingUp = false;
				goingDown = true;
			}

			if (goingUp) {
				Point tilePos = collidesWithTileUp(x, newY);

				if (tilePos != null) {
					int offsetY = tileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;
					int bottomTileY = topTileY + TILE_SIZE;
					y = bottomTileY;
					fall();
				} else {
					y = newY;
				}
			} else if (goingDown) {
				Point tilePos = collidesWithTileDown(x, newY);

				if (tilePos != null) {
					int playerHeight = getDisplayHeight();
					int offsetY = tileMap.getOffsetY();
					int topTileY = ((int) tilePos.getY()) * TILE_SIZE + offsetY;
					y = topTileY - playerHeight;
					goingDown = false;
					jumping = false;
					inAir = false;
					jumpAttacking = false;
				} else {
					y = newY;
				}
			}
		}

		if (!moving) {
			currentSpeed = 12;
		} else {
			currentSpeed = Math.min(currentSpeed + ACCEL, MAX_SPEED);
		}

		moving = false;
	}

	public void moveUp() {
		if (!panel.isVisible()) return;
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

	public void heal(int amount) {
		if (dead) return;

		health += amount;

		if (health > MAX_HEALTH) {
			health = MAX_HEALTH;
		}
	}

	public boolean takeDamage(int dmg, boolean hitFromRight) {
		if (invincible || dead || blocking) return false;

		health -= dmg;

		if (hitFromRight) {
			x -= KNOCKBACK;
		} else {
			x += KNOCKBACK;
		}

		if (x < 0) {
			x = 0;
		}

		if (x > tileMap.getWidthPixels() - getDisplayWidth()) {
			x = tileMap.getWidthPixels() - getDisplayWidth();
		}

		gettingHit = true;
		hitTimer = HIT_DURATION;

		if (health <= 0) {
			soundManager.playSound("playerDead", false);
			playDeathSound();
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

		soundManager.playSound("block", false);
		playBlockSound();

		if (!blocking) {
			blocking = true;
			blockTimer = BLOCK_DURATION;
			blockAnim.start();
		}
	}

	public void blockPush(boolean hitFromRight) {
		if (!blocking) return;

		if (hitFromRight) {
			x -= BLOCK_PUSH;
		} else {
			x += BLOCK_PUSH;
		}

		if (x < 0) {
			x = 0;
		}

		if (x > tileMap.getWidthPixels() - getDisplayWidth()) {
			x = tileMap.getWidthPixels() - getDisplayWidth();
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

		if (jumpAttacking) {
			return jumpAttackAnim.getImage();
		}

		if (moveAttacking) {
			return moveAttackAnim.getImage();
		}

		if (inAir || jumping) {
			if (charging && jumpChargeAnim != null) {
				return jumpChargeAnim.getImage();
			}

			if (shooting && jumpShootAnim != null) {
				return jumpShootAnim.getImage();
			}

			return jumpAnim.getImage();
		}

		if (attacking) {
			return attackAnim.getImage();
		}

		if (charging) {
			return chargeAnim.getImage();
		}

		if (shooting && !moving) {
			return shootAnim.getImage();
		}

		if (moving) {
			return runAnim.getImage();
		}

		return idleAnim.getImage();
	}

	public int getDisplayWidth() {
		return (idleAnim.getImage().getWidth(null) * SCALE) - 20;
	}

	public int getDisplayHeight() {
		return (idleAnim.getImage().getHeight(null) * SCALE) + 45;
	}

	public void respawn(int x, int y, boolean restoreHealth) {
		setX(x);
		setY(y);

		if (restoreHealth) {
			health = MAX_HEALTH;
		}

		dead = false;
		visible = true;
		invincible = false;
		invincibleTimer = 0;
		fall();
	}

	public Rectangle getHitBox() {
		int width = getDisplayWidth();
		int height = getDisplayHeight();
		return new Rectangle(x, y, width, height);
	}

	public boolean isAttackActiveFrame() {
		int frame = -1;

		if (attacking) {
			frame = attackAnim.getCurrentFrame();
		} else if (moveAttacking) {
			frame = moveAttackAnim.getCurrentFrame();
		} else if (jumpAttacking) {
			frame = jumpAttackAnim.getCurrentFrame();
		}

		return frame >= 2 && frame <= 4;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public boolean isDead() {
		return dead;
	}

	protected void playAttackSound() {}
	protected void playBlockSound() {}
	protected void playDeathSound() {}
	protected void playHitSound() {}
	protected void playJumpSound() {}
}