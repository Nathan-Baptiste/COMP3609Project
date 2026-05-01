import javax.swing.JPanel;
import java.awt.*;

public class Player1 extends Player {

    private int attackDamage = 15;

    SoundManager soundManager;

    public Player1(JPanel panel, TileMap tileMap, BackgroundManager backgroundManager) {
        super(panel, tileMap, backgroundManager);

        soundManager = SoundManager.getInstance();

        Image idleStrip = ImageManager.loadImage("src/images/Player1/Player1Idle.png");
        idleAnim = new StripAnimation(idleStrip, 4, 0, 0, panel, true);

        Image runStrip = ImageManager.loadImage("src/images/Player1/Player1Run.png");
        runAnim = new StripAnimation(runStrip, 8, 0, 0, panel, true);

        Image jumpStrip = ImageManager.loadImage("src/images/Player1/Player1Jump.png");
        jumpAnim = new StripAnimation(jumpStrip, 5, 0, 0, panel, false);

        Image attackStrip = ImageManager.loadImage("src/images/Player1/Player1Attack.png");
        attackAnim = new StripAnimation(attackStrip, 6, 0, 0, panel, false);

        Image moveAttackStrip = ImageManager.loadImage("src/images/Player1/Player1RunAttack.png");
        moveAttackAnim = new StripAnimation(moveAttackStrip, 8, 0, 0, panel, false);

        Image jumpAttackStrip = ImageManager.loadImage("src/images/Player1/Player1JumpAttack.png");
        jumpAttackAnim = new StripAnimation(jumpAttackStrip, 6, 0, 0, panel, false);

        Image blockStrip = ImageManager.loadImage("src/images/Player1/Player1Block.png");
        blockAnim = new StripAnimation(blockStrip, 4, 0, 0, panel, true);


        hitImage = ImageManager.loadImage("src/images/Player1/Player1Hit.png");
        deathImage = ImageManager.loadImage("src/images/Player1/PlayerDeath.png");

        idleAnim.start();
        runAnim.start();
        jumpAnim.start();
        attackAnim.start();
        moveAttackAnim.start();
        jumpAttackAnim.start();
        blockAnim.start();
    }

    public void attack() {
        if (attacking || moveAttacking || jumpAttacking) return;

        soundManager.playSound("p1Sword", false);
        soundManager.playSound("p1Attack", false);

        if (inAir || jumping) {
            jumpAttacking = true;
            jumpAttackAnim.start();
        }
        else if (moving) {
            moveAttacking = true;
            moveAttackAnim.start();
        }
        else {
            attacking = true;
            attackAnim.start();
        }
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    protected void playAttackSound() {
        soundManager.playSound("p1Sword", false);
        soundManager.playSound("p1Attack", false);
    }

    protected void playBlockSound() {
        soundManager.playSound("p1Block", false);
    }

    protected void playDeathSound() {
        soundManager.playSound("p1Death", false);
    }

    protected void playHitSound() {
        soundManager.playSound("p1Hit", false);
    }

    protected void playJumpSound() {
        soundManager.playSound("p1Jump", false);
    }
}