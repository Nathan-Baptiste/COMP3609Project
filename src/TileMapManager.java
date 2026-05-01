import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.ImageIcon;


/**
    The TileMapeManager class loads and manages tile Images and
    "host" Sprites used in the game. Game Sprites are cloned from
    "host" Sprites.
*/
public class TileMapManager {

    private ArrayList<Image> tiles;
    private int currentMap = 0;

    private JPanel panel;


    public TileMapManager(JPanel panel) {
	this.panel = panel;

        loadTileImages();
    }


     public TileMap loadMap(String filename, int level)
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

        TileMap newMap = new TileMap(panel, mapWidth, mapHeight, level);
        for (int y=0; y<mapHeight; y++) {
            String line = lines.get(y);
            for (int x=0; x<line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, tiles.get(tile));
                } else if (ch == '1') {
                    int spawnX = x * 64;
                    int spawnY = y * 64 + newMap.getOffsetY() - 50;
                    newMap.addSlime(new Slime(panel, newMap, spawnX, spawnY));
                } else if (ch == '2') {
                    int spawnX = x * 64;
                    int spawnY = y * 64 + newMap.getOffsetY() - 50;
                    newMap.addSkeleton(new Skeleton(panel, newMap, spawnX, spawnY));
                } else if (ch == '3') {
                    int spawnX = x * 64;
                    int spawnY = y * 64 + newMap.getOffsetY() - 50;
                    newMap.addBear(new Bear(panel, newMap, spawnX, spawnY));
                } else if (ch == '4') {
                    int spawnX = x * 64;
                    int spawnY = y * 64 + newMap.getOffsetY() - 50;
                    newMap.addMinitroll(new Minitroll(panel, newMap, spawnX, spawnY));
                }
                else if (ch == '*') {
                    int spawnX = x * 64;
                    int spawnY = y * 64 + newMap.getOffsetY();

                    newMap.addCollectible(
                            new Collectible(spawnX, spawnY, Collectible.COIN)
                    );
                }
                else if (ch == 'o') {
                    int spawnX = x * 64;
                    int spawnY = y * 64 + newMap.getOffsetY();

                    int foodType = Collectible.foodTypeFromTilePosition(x, y);

                    newMap.addCollectible(
                            new Collectible(spawnX, spawnY, foodType)
                    );
                } else if (ch == '!') {
                    int spawnX = x * 64;
                    int spawnY = y * 64;
                    Image flagImg = ImageManager.loadImage("src/images/ItemsandObjects/EndFlag.png");
                    newMap.addEndFlag(new EndFlag(spawnX, spawnY, flagImg, 3, 0, -266));
                }
            }
        }

        return newMap;
    }

    // -----------------------------------------------------------
    // code for loading sprites and images
    // -----------------------------------------------------------


    public void loadTileImages() {
        // keep looking for tile A,B,C, etc. this makes it
        // easy to drop new tiles in the images/ folder

	File file;

	System.out.println("loadTileImages called.");

        tiles = new ArrayList<Image>();
        char ch = 'A';
        while (true) {
            String filename = "src/images/Tiles/tile_" + ch + ".png";
	    file = new File(filename);
            if (!file.exists()) {
		System.out.println("Image file could not be opened: " + filename);
                break;
            }
	    else
		System.out.println("Image file opened: " + filename);
            ImageIcon icon = new ImageIcon(filename);
            Image img = icon.getImage();

            Image scaledImg = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            tiles.add(scaledImg);
            ch++;
        }
    }

}
