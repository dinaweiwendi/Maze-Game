package byog.Core;

//import byog.TileEngine.TERenderer;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

//import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
//import java.io.readObject;
import java.io.IOException;

import java.util.Random;


public class Game {
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private static boolean gameOver = false;
    private Player deniel;
    private Player eve;
    private Door lockdoor;
    private Long seed;

    private static TETile randomTile(Random rd) {
        int number = RandomUtils.uniform(rd, 0, 3);
        switch (number) {
            case 0:
                return Tileset.WALL;
            default:
                return Tileset.NOTHING;
        }
    }

    private static TETile[][] loadWorld() {
        File f = new File("./byog/Core/world.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (TETile[][]) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                StdDraw.clear();
                System.exit(0);

            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        System.exit(0);
        return new TETile[WIDTH][HEIGHT];
    }

    private static void saveWorld(TETile[][] world) {
        File f = new File("./byog/Core/world.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(world);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public void link(Square a, Square b, TETile[][] world, Random rd) {
        //a is the lower one, b is the upper one
        //Random rd = new Random();
        Position astart;
        Position aend;
        Position bstart;
        Position bend;
        if ((b.position.y - a.position.y - a.height + 1) > 3) {
            int hub = RandomUtils.uniform(rd, a.position.y + a.height, b.position.y);
            int ax = RandomUtils.uniform(rd, a.position.x + 1, a.position.x + a.width - 1);
            astart = new Position(ax, a.position.y + a.height - 1);
            aend = new Position(ax, hub);

            int bx = RandomUtils.uniform(rd, b.position.x + 1, b.position.x + b.width - 1);
            bstart = new Position(bx, hub);
            bend = new Position(bx, b.position.y);
        } else {
            astart = new Position(a.position.x + 1, a.position.y + a.height - 1);
            aend = new Position(a.position.x + 1, b.position.y);
            bstart = aend;
            bend = bstart;
        }
        vhallway(world, astart, aend);
        vhallway(world, bstart, bend);

        if (astart.x == bstart.x) {
            return;
        }
        if (Math.abs(astart.x - bstart.x) == 1) {
            turn(world, aend);
            turn(world, bstart);
        } else {
            horhallway(world, aend, bstart);
            turn(world, aend);
            turn(world, bstart);
        }

    }

    private void turn(TETile[][] world, Position a) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (world[a.x + i][a.y + j] == Tileset.NOTHING) {
                    world[a.x + i][a.y + j] = Tileset.WALL;
                }
            }
        }
    }

    private void horhallway(TETile[][] world, Position a, Position b) {
        int len;
        Position small;
        Position big;
        if (a.x < b.x) {
            small = a;
            big = b;
        } else {
            small = b;
            big = a;
        }
        len = big.x - small.x - 1;
        for (int i = 0; i < len; i++) {
            world[small.x + 1 + i][small.y] = Tileset.FLOOR;
            if (world[small.x + 1 + i][small.y - 1] == Tileset.NOTHING) {
                world[small.x + 1 + i][small.y - 1] = Tileset.WALL;
            }
            if (world[small.x + 1 + i][small.y + 1] == Tileset.NOTHING) {
                world[small.x + 1 + i][small.y + 1] = Tileset.WALL;
            }
        }

    }

    private void vhallway(TETile[][] world, Position s, Position e) {
        //point s is lower than point e
        int len;
        len = e.y - s.y + 1;
        for (int i = 0; i < len; i++) {
            world[s.x][s.y + i] = Tileset.FLOOR;
            if (world[s.x - 1][s.y + i] == Tileset.NOTHING) {
                world[s.x - 1][s.y + i] = Tileset.WALL;
            }
            if (world[s.x + 1][s.y + i] == Tileset.NOTHING) {
                world[s.x + 1][s.y + i] = Tileset.WALL;
            }
        }
    }

    public void linktheroom(TETile[][] world, Squaregroup sg, Random rd) {
        for (int i = 0; i < sg.sarray.length; i++) {
            for (int j = 0; j < sg.sarray[i].size - 1; j++) {
                link(sg.sarray[i].ss[j], sg.sarray[i].ss[j + 1], world, rd);
            }
        }

        for (int i = 0; i < (sg.sarray.length - 1); i++) {
            int minsize = Math.min(sg.sarray[i].size, sg.sarray[i + 1].size);
            int inde = RandomUtils.uniform(rd, 0, minsize);
            Square r1 = sg.sarray[i].ss[inde];
            Square r2 = sg.sarray[i + 1].ss[inde];
            linkcolumn(r1, r2, world, rd);
        }
    }

    public void linkcolumn(Square a, Square b, TETile[][] world, Random rd) {
        //b is the right one,a is left one
        Position as;
        Position ae;
        Position bs;
        Position be;
        Position down;
        Position up;
        if ((b.position.x - a.position.x - a.width + 1) > 1) {
            int hub = RandomUtils.uniform(rd, a.position.x + a.width, b.position.x - 1);
            //给拐角留出距离
            int ay = RandomUtils.uniform(rd, a.position.y + 1, a.position.y + a.height - 1);
            //这里改成了-1
            as = new Position(a.position.x + a.width - 1 - 1, ay); //y坐标
            ae = new Position(hub + 1, ay);

            int by = RandomUtils.uniform(rd, b.position.y + 1, b.position.y + b.height - 1);
            bs = new Position(hub - 1, by);
            be = new Position(b.position.x + 1, by);
        } else {
            as = new Position(a.position.x + a.width - 1 - 1, a.position.y + 1);
            // +1 change to random number
            ae = new Position(b.position.x + 1, a.position.y + 1);
            bs = ae;
            be = as;
        }
        horhallway(world, as, ae); //
        horhallway(world, bs, be);
        up = new Position(bs.x + 1, bs.y);
        down = new Position(ae.x - 1, ae.y);

        if (bs.y > ae.y) {
            vhallway(world, down, up);
        } else {
            vhallway(world, up, down);
        }
        turn(world, down);
        turn(world, up);
    }

    public TETile[][] designworld(TETile[][] world, Random rd) {
        // i is horizental  and j is vertical
        Square s;
        int i;
        int j;
        Squaregroup squaregroup = new Squaregroup();

        int count;
        count = 0;
        for (i = 0; i < WIDTH; i++) {
            squaregroup.sarray[count] = new Squarearray();
            for (j = 0; j < HEIGHT; j++) {
                world[i][j] = randomTile(rd);
                if (world[i][j] == Tileset.WALL) {
                    Position squarep = new Position(i, j);
                    s = makesquare(world, squarep, squaregroup.sarray[count], rd);
                    if (s != null) {
                        makefloor(world, s, squarep);
                        if (j <= 25) {
                            j = j + s.height + 1;
                        }
                    }
                }
            }
            if (j > 25 && i < 70) {
                i = i + 9;
                count = count + 1;
            } else {
                linktheroom(world, squaregroup, rd);
                lockdoor = makedoor(world, squaregroup, rd);
                deniel = makeplayer(world, rd);
                eve = makeplayer2(world, rd);
                makestair(world, squaregroup, rd);
                return world;
            }
        }
        return world;
    }

    public Square makesquare(TETile[][] world, Position p, Squarearray sa, Random rd) {
        int x;
        int y;
        Square s;
        x = RandomUtils.uniform(rd, 5, 9);
        y = RandomUtils.uniform(rd, 5, 7);

        if (p.x + x < WIDTH && p.y + y < HEIGHT) {
            s = new Square(x, y, p);
            sa.addsquare(s);
            for (int i = 0; i < x; i++) {
                world[p.x + i][p.y] = Tileset.WALL;
            }
            for (int i = 0; i < y; i++) {
                world[p.x][p.y + i] = Tileset.WALL;
            }
            for (int i = 0; i < x; i++) {
                world[p.x + i][p.y + y - 1] = Tileset.WALL;
            }
            for (int i = 0; i < y; i++) {
                world[p.x + x - 1][p.y + i] = Tileset.WALL;
            }
        } else {
            world[p.x][p.y] = Tileset.NOTHING;
            s = null;
        }
        return s;
    }

    public void makefloor(TETile[][] world, Square s, Position p) {
        for (int i = 1; i < s.width - 1; i++) {
            for (int j = 1; j < s.height - 1; j++) {
                world[p.x + i][p.y + j] = Tileset.FLOOR;
            }
        }
    }

    public Player makeplayer(TETile[][] world, Random rd) {
        int xcoor = 0;
        int ycoor = 0;
        while (world[xcoor][ycoor] != Tileset.FLOOR) {
            xcoor = RandomUtils.uniform(rd, 0, WIDTH - 1);
            ycoor = RandomUtils.uniform(rd, 0, HEIGHT - 1);
        }
        world[xcoor][ycoor] = Tileset.PLAYER;
        Position am = new Position(xcoor, ycoor);
        Player de = new Player(am);
        return de;
    }

    public Player makeplayer2(TETile[][] world, Random rd) {
        int xcoor = 0;
        int ycoor = 0;
        while (world[xcoor][ycoor] != Tileset.FLOOR) {
            xcoor = RandomUtils.uniform(rd, 0, WIDTH - 1);
            ycoor = RandomUtils.uniform(rd, 0, HEIGHT - 1);
        }
        world[xcoor][ycoor] = Tileset.FLOWER;
        Position am = new Position(xcoor, ycoor);
        Player de = new Player(am);
        return de;
    }

    public Door makedoor(TETile[][] world, Squaregroup sg, Random rd) {
        int x;
        int y;
        x = RandomUtils.uniform(rd, 0, sg.sarray.length - 1);
        Square target = sg.sarray[x].ss[0];
        y = RandomUtils.uniform(rd, target.position.x + 1, target.position.x + target.width - 1);
        Position tardoor = new Position(y, target.position.y);
        world[y][target.position.y] = Tileset.LOCKED_DOOR;
        Door lodoor = new Door(tardoor);
        return lodoor;
    }

    public void makestair(TETile[][] world, Squaregroup sg, Random rd) {
        Square ds = sg.sarray[sg.sarray.length - 1].ss[0];
        world[ds.position.x + ds.width - 2][ds.position.y + 1] = Tileset.DOWNSTAIR;
        Square us = sg.sarray[0].ss[sg.sarray[0].size - 1];
        world[us.position.x + 1][us.position.y + us.height - 2] = Tileset.UPSTAIR;
    }

    public TETile[][] downorup(Long newseed) {
        TETile[][] downworld = new TETile[WIDTH][HEIGHT];
        seed = newseed;
        Random rd = new Random(newseed);
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                downworld[x][y] = Tileset.NOTHING;
            }
        }
        downworld = designworld(downworld, rd);
        for (int a = 0; a < WIDTH; a++) {
            for (int b = 0; b < HEIGHT; b++) {
                if (downworld[a][b].equals(Tileset.PLAYER)) {
                    Position d = new Position(a, b);
                    deniel = new Player(d);
                }
                if (downworld[a][b].equals(Tileset.FLOWER)) {
                    Position d = new Position(a, b);
                    eve = new Player(d);
                }
            }
        }
        return downworld;
    }

    private TETile[][] denielmove(TETile[][] world, Player D, char c) {
        if (c == 'W') {
            if (world[D.p.x][D.p.y + 1].equals(Tileset.FLOWER)) {
                return world;
            }
            if (world[D.p.x][D.p.y + 1].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed  - 1;
                return downorup(sed);
            }
            if (world[D.p.x][D.p.y + 1].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x][D.p.y + 1].equals(Tileset.WALL)) {
                world[D.p.x][D.p.y + 1] = Tileset.PLAYER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.y++;
            }
        }
        if (c == 'S') {
            if (world[D.p.x][D.p.y - 1].equals(Tileset.FLOWER)) {
                return world;
            }
            if (world[D.p.x][D.p.y - 1].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x][D.p.y - 1].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x][D.p.y - 1].equals(Tileset.WALL)) {
                world[D.p.x][D.p.y - 1] = Tileset.PLAYER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.y--;
            }
        }
        if (c == 'A') {
            if (world[D.p.x - 1][D.p.y].equals(Tileset.FLOWER)) {
                return world;
            }
            if (world[D.p.x - 1][D.p.y].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x - 1][D.p.y].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x - 1][D.p.y].equals(Tileset.WALL)) {
                world[D.p.x - 1][D.p.y] = Tileset.PLAYER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.x--;
            }
        }
        if (c == 'D') {
            if (world[D.p.x + 1][D.p.y].equals(Tileset.FLOWER)) {
                return world;
            }
            if (world[D.p.x + 1][D.p.y].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x + 1][D.p.y].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x + 1][D.p.y].equals(Tileset.WALL)) {
                world[D.p.x + 1][D.p.y] = Tileset.PLAYER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.x++;
            }
        }
        return world;
    }
    public TETile[][] evemove(TETile[][] world, Player D, char c) {
        if (c == 'I') {
            if (world[D.p.x][D.p.y + 1].equals(Tileset.PLAYER)) {
                return world;
            }
            if (world[D.p.x][D.p.y + 1].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x][D.p.y + 1].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x][D.p.y + 1].equals(Tileset.WALL)) {
                world[D.p.x][D.p.y + 1] = Tileset.FLOWER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.y++;
            }
        }
        if (c == 'J') {
            if (world[D.p.x - 1][D.p.y].equals(Tileset.PLAYER)) {
                return world;
            }
            if (world[D.p.x - 1][D.p.y].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x - 1][D.p.y].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x - 1][D.p.y].equals(Tileset.WALL)) {
                world[D.p.x - 1][D.p.y] = Tileset.FLOWER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.x--;
            }
        }
        if (c == 'K') {
            if (world[D.p.x][D.p.y - 1].equals(Tileset.PLAYER)) {
                return world;
            }
            if (world[D.p.x][D.p.y - 1].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x][D.p.y - 1].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x][D.p.y - 1].equals(Tileset.WALL)) {
                world[D.p.x][D.p.y - 1] = Tileset.FLOWER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.y--;
            }
        }
        if (c == 'L') {
            if (world[D.p.x + 1][D.p.y].equals(Tileset.PLAYER)) {
                return world;
            }
            if (world[D.p.x + 1][D.p.y].equals(Tileset.DOWNSTAIR)) {
                Long sed = seed - 1;
                return downorup(sed);
            }
            if (world[D.p.x + 1][D.p.y].equals(Tileset.UPSTAIR)) {
                Long sed = seed + 1;
                return downorup(sed);
            }
            if (!world[D.p.x + 1][D.p.y].equals(Tileset.WALL)) {
                world[D.p.x + 1][D.p.y] = Tileset.FLOWER;
                world[D.p.x][D.p.y] = Tileset.FLOOR;
                D.p.x++;
            }
        }
        return world;
    }

    public void drawFrame(int s) {
        //s 有 0，1 三种情况，0是开始界面，1是输入seed
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear();
        StdDraw.clear(Color.black);
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.enableDoubleBuffering();

        // Draw the GUI
        if (!gameOver) {
            if (s == 0) {
                Font smallFont = new Font("Monaco", Font.BOLD, 30);
                StdDraw.setFont(smallFont);
                StdDraw.text(midWidth, Math.round(HEIGHT * 3 / 4), "CS61B: THE GAME");
                Font bigFont = new Font("Monaco", Font.BOLD, 20);
                StdDraw.setFont(bigFont);
                StdDraw.setPenColor(Color.BLACK);
                StdDraw.text(midWidth, midHeight, "New Game (N)");
                StdDraw.text(midWidth, midHeight - 1, "Load Game (L)");
                StdDraw.text(midWidth, midHeight - 2, "Quit (Q)");
                StdDraw.show();
            }
            if (s == 1) {
                Font smallFont = new Font("Monaco", Font.BOLD, 30);
                StdDraw.setFont(smallFont);
                StdDraw.text(midWidth, Math.round(HEIGHT * 3 / 4), "Please enter the seed");
            }
        } else {

            Font smallFont = new Font("Monaco", Font.BOLD, 30);
            StdDraw.setFont(smallFont);
            StdDraw.text(midWidth, Math.round(HEIGHT * 3 / 4), "GAME OVER");
        }
        StdDraw.show();
    }

    public void drawHUD(TETile[][] finalworld, Position mouse) {
        int x = mouse.x;
        int y = mouse.y;

        StdDraw.clear(new Color(0, 0, 0));
        TETile[][] newworld = TETile.copyOf(finalworld);

        int numXTiles = newworld.length;
        int numYTiles = newworld[0].length;
        for (int a = 0; a < numXTiles; a += 1) {
            for (int b = 0; b < numYTiles; b += 1) {
                if (newworld[a][b] == null) {
                    throw new IllegalArgumentException("Tile at position x=" + a + ", y=" + b
                            + " is null.");
                }
                newworld[a][b].draw(a, b);
            }
        }
        Font markFont = new Font("Monaco", Font.BOLD, 15);
        StdDraw.setPenColor(Color.white);
        StdDraw.setFont(markFont);

        if (newworld[x][y].equals(Tileset.FLOOR)) {
            StdDraw.text(3, HEIGHT - 1, "FLOOR");
        }
        if (newworld[x][y].equals(Tileset.WALL)) {
            StdDraw.text(3, HEIGHT - 1, "WALL");
        }
        if (newworld[x][y].equals(Tileset.PLAYER)) {
            StdDraw.text(3, HEIGHT - 1, "PLAYER1");
        }
        if (newworld[x][y].equals(Tileset.FLOWER)) {
            StdDraw.text(3, HEIGHT - 1, "PLAYER2");
        }
        if (newworld[x][y].equals(Tileset.LOCKED_DOOR)) {
            StdDraw.text(3, HEIGHT - 1, "DOOR");
        }
        if (newworld[x][y].equals(Tileset.NOTHING)) {
            StdDraw.text(3, HEIGHT - 1, "NOTHING");
        }
        if (newworld[x][y].equals(Tileset.DOWNSTAIR)) {
            StdDraw.text(3, HEIGHT - 1, "DOWNSTAIR");
        }
        if (newworld[x][y].equals(Tileset.UPSTAIR)) {
            StdDraw.text(3, HEIGHT - 1, "UPSTAIR");
        }
        StdDraw.show();
    }

    public void playWithKeyboard() {
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        drawFrame(0);
        for (int w = 0; w < 1000; w++) {
            while (!StdDraw.hasNextKeyTyped()) {
                StdDraw.pause(1000);
            }
            char whattodo = StdDraw.nextKeyTyped();
            if (whattodo == 'N') {
                drawFrame(1);
                String sed = JOptionPane.showInputDialog(null,
                        "Enter your seed:");
                char[] arr = sed.toCharArray(); // returns a length 4 char array ['l','i','n','e']
                int i = 1; String sewwd = "";
                if ((arr[arr.length - 1] == 'S') | (arr[arr.length - 1] == 's')) {
                    sewwd = sewwd + arr[i];
                    i += 1;
                } else {
                    System.exit(0);
                }
                seed = Long.parseLong(sewwd);
                Random rd = new Random(seed);
                for (int x = 0; x < WIDTH; x += 1) {
                    for (int y = 0; y < HEIGHT; y += 1) {
                        finalWorldFrame[x][y] = Tileset.NOTHING;
                    }
                }
                finalWorldFrame = designworld(finalWorldFrame, rd); //这里的保存和quit还有问题
                break;
            } else if (whattodo == 'L') {
                finalWorldFrame = loadWorld();
                for (int a = 0; a < WIDTH; a++) {
                    for (int b = 0; b < HEIGHT; b++) {
                        if (finalWorldFrame[a][b].equals(Tileset.PLAYER)) {
                            Position d = new Position(a, b);
                            deniel = new Player(d);
                        }
                        if (finalWorldFrame[a][b].equals(Tileset.FLOWER)) {
                            Position d = new Position(a, b);
                            eve = new Player(d);
                        }
                    }
                }
                break;
            } else if (whattodo == 'Q') { //注意这里nexttypedkey去过一次就没有了
                System.exit(0);
            } else {
                continue;
            }
        }
        TERenderer ter = new TERenderer(); ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(finalWorldFrame);
        int prevX = 0; int prevY = 0; Position mouse;
        for (int i = 0; i < 10000; i++) {
            while (!StdDraw.hasNextKeyTyped()) {
                try {
                    int mouseX = (int) StdDraw.mouseX();
                    int mouseY = (int) StdDraw.mouseY();
                    if (mouseX != prevX || mouseY != prevY) {
                        prevX = mouseX;
                        prevY = mouseY;
                        mouse = new Position(prevX, prevY);
                        drawHUD(finalWorldFrame, mouse);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
            char ip = StdDraw.nextKeyTyped(); String vc = "WASDIJKL";
            if (ip == 'Q') {
                saveWorld(finalWorldFrame);
                System.exit(0);
            } else if (vc.indexOf(ip) != -1) {
                finalWorldFrame = whomove(ip, finalWorldFrame);
                ter.renderFrame(finalWorldFrame);
            } else {
                continue;
            }
        }
    }
    private TETile[][] whomove(char ip, TETile[][] finalWorldFrame) {
        if (ip == 'W' || ip == 'A' || ip == 'S' || ip == 'D') {
            TETile[][] finalWorld = denielmove(finalWorldFrame, deniel, ip);
            return finalWorld;
        } else if (ip == 'I' || ip == 'J' || ip == 'K' || ip == 'L') {
            TETile[][] finalWorld = evemove(finalWorldFrame, eve, ip);
            return finalWorld;
        }
        return finalWorldFrame;
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    //String timestamp = LocalDateTime.now().toString();
    //StdDraw.text(70, HEIGHT - 1, timestamp);
    //StdDraw.show();

    public TETile[][] playWithInputString(String input) {
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        char[] arr = input.toCharArray(); // returns a length 4 char array ['l','i','n','e']
        int i = 1;
        String sed = "";
        int movebegin = 0;
        if ((arr[0] == 'N') | (arr[0] == 'n')) {
            while ((arr[i] != 'S') && (arr[i] != 's')) {
                sed = sed + arr[i];
                i += 1;
                movebegin = i;
            }
            seed = Long.parseLong(sed);
            finalWorldFrame = new TETile[WIDTH][HEIGHT];
            Random rd = new Random(seed);
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    finalWorldFrame[x][y] = Tileset.NOTHING;
                }
            }
            finalWorldFrame = designworld(finalWorldFrame, rd);
            //return finalWorldFrame;
        } else if (arr[0] == 'L' || arr[0] == 'l') {
            finalWorldFrame = loadWorld();
            if (finalWorldFrame[0][0] != null) {
                for (int a = 0; a < WIDTH; a++) {
                    for (int b = 0; b < HEIGHT; b++) {
                        if (finalWorldFrame[a][b].equals(Tileset.PLAYER)) {
                            Position d = new Position(a, b);
                            deniel = new Player(d);
                        }
                        if (finalWorldFrame[a][b].equals(Tileset.FLOWER)) {
                            Position d = new Position(a, b);
                            eve = new Player(d);
                        }
                    }
                }
                movebegin = 0;
            } else {
                Random rd = new Random(123);
                for (int x = 0; x < WIDTH; x += 1) {
                    for (int y = 0; y < HEIGHT; y += 1) {
                        finalWorldFrame[x][y] = Tileset.NOTHING;
                    }
                }
                finalWorldFrame = designworld(finalWorldFrame, rd);
            }
        }
        if (arr.length > movebegin + 1) {
            for (int j = movebegin + 1; j < arr.length - 1 && arr[j] != ':'; j++) {
                char move = arr[j];
                finalWorldFrame = whomove(move, finalWorldFrame);
            }
        }
        if (arr[arr.length - 1] == 'Q' | arr[arr.length - 1] == 'q') {
            saveWorld(finalWorldFrame);
        }
        return finalWorldFrame;
    }

    private class Position {
        int x;
        int y;

        private Position(int hor, int ver) {
            x = hor;
            y = ver;
        }
    }

    private class Square {
        int width;
        int height;
        Position position;

        private Square(int x, int y, Position p) {
            width = x;
            height = y;
            position = p;
        }
    }

    private class Squaregroup {
        Squarearray[] sarray;

        private Squaregroup() {
            sarray = new Squarearray[8];
        }
    }

    private class Squarearray {
        Square[] ss;
        int size;

        private Squarearray() {
            ss = new Square[5];
            size = 0;
        }

        private void addsquare(Square s) {
            ss[size] = s;
            size = size + 1;
        }
    }

    public class Player {
        Position p;

        private Player(Position position) {
            p = position;
        }
    }

    public class Door {
        Position p;

        private Door(Position position) {
            p = position;
        }
    }
}

