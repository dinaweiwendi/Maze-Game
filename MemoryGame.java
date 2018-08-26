package byog.lab6;

import byog.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        int seed = Integer.parseInt(args[0]);
        MemoryGame game = new MemoryGame(40, 40);
        game.startGame(seed);
    }

    public MemoryGame(int width, int height) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear();
        StdDraw.enableDoubleBuffering();
        //TODO: Initialize random number generator
    }

    public String generateRandomString(int n, int s) {
        Random rd=new Random(s);
        char[] result = new char[n];
        String output;
        for (int i = 0; i < n; i++) {

            int number = RandomUtils.uniform(rd, 0, CHARACTERS.length);
            result[i] = CHARACTERS[number];
        }
        output = String.valueOf(result);
        return output;
    }
    public void drawFrame(String s) {
            Font font = new Font("Monaco", Font.BOLD, 30);
            StdDraw.setFont(font);
            StdDraw.clear();
            StdDraw.text(20,20,s);
            StdDraw.setPenColor(StdDraw.PINK);
            StdDraw.show();
        }
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen


    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        StdDraw.clear();
        char[] charArray;
        String str = letters;
        charArray = str.toCharArray();
        for(int i = 0; i < charArray.length; i++) {

            StdDraw.text(20, 20, Character.toString(charArray[i]));
            StdDraw.show();
            //StdDraw.pause(10);
            StdDraw.clear();
            System.out.println("what");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        char[] res = new char[n];
        for (int i = 0; i < n; i++) {
            StdDraw.clear();
            System.out.println("im here");
            for (int w = 0; w < 60; w++) {
                if (StdDraw.hasNextKeyTyped()) {
                    System.out.println("dina good luck");
                    res[i] = StdDraw.nextKeyTyped();
                    System.out.println(res[i]);

                    break;
                } else {
                    System.out.println("i am in a loop");
                    StdDraw.pause(1000);
                }
            }

        }

        String result = String.valueOf(res);
        drawFrame(result);
        //StdDraw.pause(1000);
        return result;
    }




    public void startGame(int s) {
        String answer;
        String guess;


        for(int i = 1; i < 100; i ++) {

            drawFrame("Round:" + i );
            //StdDraw.pause(1000);
            answer = generateRandomString(i, s);
            flashSequence(answer);
            guess = solicitNCharsInput(i);
            if(answer.compareTo(guess) !=0 ) {
                StdDraw.clear();
                StdDraw.text(20,20,"Game Over");
                StdDraw.show();
                //StdDraw.pause(2000);

                return;
            }
        }
    }

}
