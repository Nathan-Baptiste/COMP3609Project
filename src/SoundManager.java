import javax.sound.sampled.AudioInputStream;		// for playing sound clips
import javax.sound.sampled.*;
import java.io.*;

import java.util.HashMap;				// for storing sound clips


public class SoundManager {				// a Singleton class
	HashMap<String, Clip> clips;

	private static SoundManager instance = null;	// keeps track of Singleton instance

	private SoundManager () {
		Clip clip;
		clips = new HashMap<String, Clip>();
		
		//Clip clip = loadClip("sounds/background.wav");
		//clips.put("background", clip);		// background theme sound

		//Items and Objects
		clip = loadClip("src/sounds/ItemsandObjects/Coin.wav");
		clips.put("coin", clip);

		clip = loadClip("src/sounds/ItemsandObjects/Eating.wav");
		clips.put("eat", clip);

		clip = loadClip("src/sounds/ItemsandObjects/Heal.wav");
		clips.put("heal", clip);

		clip = loadClip("src/sounds/ItemsandObjects/EndFlag.wav");
		clips.put("goal", clip);

		//Block
		clip = loadClip("src/sounds/Players/Block/Block.wav");
		clips.put("block", clip);

		clip = loadClip("src/sounds/Players/Block/BlockRecharged.wav");
		clips.put("recharge", clip);

		//Game Over
		clip = loadClip("src/sounds/ItemsandObjects/PlayerDead.wav");
		clips.put("playerDead", clip);

		clip = loadClip("src/sounds/ItemsandObjects/GameOver.wav");
		clips.put("gameOver", clip);

		//Bow
		clip = loadClip("src/sounds/Players/Bow/ChargeBow.wav");
		clips.put("chargeBow", clip);

		clip = loadClip("src/sounds/Players/Bow/ShootBow.wav");
		clips.put("shootBow", clip);

		//Bears
		clip = loadClip("src/sounds/Enemies/Bear/BearAttack.wav");
		clips.put("bearAttack", clip);

		clip = loadClip("src/sounds/Enemies/Bear/BearChase.wav");
		clips.put("bearChase", clip);

		clip = loadClip("src/sounds/Enemies/Bear/BearHit.wav");
		clips.put("bearHit", clip);

		clip = loadClip("src/sounds/Enemies/Bear/BearIdle.wav");
		clips.put("bearIdle", clip);

		//Slimes
		clip = loadClip("src/sounds/Enemies/Slime/SlimeAttack.wav");
		clips.put("slimeAttack", clip);

		clip = loadClip("src/sounds/Enemies/Slime/SlimeHit.wav");
		clips.put("slimeHit", clip);

		clip = loadClip("src/sounds/Enemies/Slime/SlimeMove.wav");
		clips.put("slimeMove", clip);

		//Skeletons
		clip = loadClip("src/sounds/Enemies/Skeleton/SkeletonHit.wav");
		clips.put("skeletonHit", clip);

		clip = loadClip("src/sounds/Enemies/Skeleton/SkeletonShoot.wav");
		clips.put("skeletonShoot", clip);

		clip = loadClip("src/sounds/Enemies/Slime/SkeletonCharge.wav");
		clips.put("skeletonCharge", clip);

		//Minitroll
		clip = loadClip("src/sounds/Enemies/Minitroll/MinitrollChase.wav");
		clips.put("minitrolChase", clip);

		clip = loadClip("src/sounds/Enemies/Minitroll/MinitrollClose.wav");
		clips.put("minitrolClose", clip);

		clip = loadClip("src/sounds/Enemies/Minitroll/MinitrollExplode.wav");
		clips.put("minitrolExplode", clip);

		clip = loadClip("src/sounds/Enemies/Minitroll/MinitrollHit.wav");
		clips.put("minitrolHit", clip);

		clip = loadClip("src/sounds/Enemies/Minitroll/MinitrollLaughing.wav");
		clips.put("minitrolLaughing", clip);

		//Player 1
		clip = loadClip("src/sounds/Players/P1/P1Attack.wav");
		clips.put("attackP1", clip);

		clip = loadClip("src/sounds/Players/P1/P1Block.wav");
		clips.put("p1Block", clip);

		clip = loadClip("src/sounds/Players/P1/P1Death.wav");
		clips.put("p1Death", clip);

		clip = loadClip("src/sounds/Players/P1/P1Hit.wav");
		clips.put("p1Hit", clip);

		clip = loadClip("src/sounds/Players/P1/P1Jump.wav");
		clips.put("p1Jump", clip);

		clip = loadClip("src/sounds/Players/P1/P1Sword.wav");
		clips.put("p1Sword", clip);

		//Player 2
		clip = loadClip("src/sounds/Players/P2/P2Block.wav");
		clips.put("p2Block", clip);

		clip = loadClip("src/sounds/Players/P2/P2Death.wav");
		clips.put("p2Death", clip);

		clip = loadClip("src/sounds/Players/P2/P2Hit.wav");
		clips.put("p2Hit", clip);

		clip = loadClip("src/sounds/Players/P2/P2Jump.wav");
		clips.put("p2Jump", clip);

		clip = loadClip("src/sounds/Players/P2/P2Respawn.wav");
		clips.put("p2Respawn", clip);

		clip = loadClip("src/sounds/lvl1.wav");
		clips.put("lvl1", clip);

		clip = loadClip("src/sounds/lvl2.wav");
		clips.put("lvl2", clip);
	}


	public static SoundManager getInstance() {	// class method to get Singleton instance
		if (instance == null)
			instance = new SoundManager();
		
		return instance;
	}		


	public Clip getClip (String title) {

		return clips.get(title);		// gets a sound by supplying key
	}


    	public Clip loadClip (String fileName) {	// gets clip from the specified file
 		AudioInputStream audioIn;
		Clip clip = null;

		try {
    			File file = new File(fileName);
    			audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL()); 
    			clip = AudioSystem.getClip();
    			clip.open(audioIn);
		}
		catch (Exception e) {
 			System.out.println ("Error opening sound files: " + e);
		}
    		return clip;
    	}


    	public void playSound(String title, Boolean looping) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.setFramePosition(0);
			if (looping)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				clip.start();
		}
    	}

	public void playCoin() {
		try {
			AudioInputStream audioIn =
					AudioSystem.getAudioInputStream(new File("src/sounds/ItemsandObjects/Coin.wav"));

			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);

			clip.setFramePosition(0);
			clip.start();

		} catch (Exception e) {
			System.out.println("Coin sound error: " + e);
		}
	}


    	public void stopSound(String title) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.stop();
		}
    	}

	public void stopMusic() {
		stopSound("lvl1");
		stopSound("lvl2");
	}

	public void playLevelMusic(int level) {
		stopMusic();

		if (level == 1) {
			playSound("lvl1", true);
		}
		else if (level == 2) {
			playSound("lvl2", true);
		}
	}

}