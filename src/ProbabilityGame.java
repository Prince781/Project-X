/***************************************
 * Probability Game - Project X
 * ------------------------------------
 * An academic exploration into 
 * mathematical probability of user
 * decision, using a visual game
 * consisting of successive circle 
 * reductions.
 * ------------------------------------
 * 2013 Princeton Ferro
****************************************/
package game;

import game.GameObject.GameObjectColor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

public class ProbabilityGame extends Thread {
	private JFrame window;
	private Graphics2D g2d; //initial buffer
	private Graphics2D b_g2d; //background graphics
	private Canvas canvas;
	private BufferStrategy strategy;
	private BufferedImage background;
	private int d[] = {600,800};
	private int content_d[] = new int[2];
	private boolean running = true;
	private GraphicsConfiguration cfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	public int frameLimit = 60;
	//game values
	private long lastClickTime = 0;
	private boolean gameStarted = false;
	private class GameCircle extends GameObject {
		private class GameAttrs {
			public int radius;
			public int u_radius;
			public boolean revealed = false;
		}
		GameAttrs game = new GameAttrs();
		public GameCircle(int radius, int u_radius, String ...attrs) {
			super("circle", attrs);
			game.radius = radius;
			transform.r = game.radius*10; //set realistic radius
			Random rand = new Random();
			game.u_radius = u_radius; //set unknown black circle radius
		}
	}
	private ArrayList<GameCircle> gameObjects = new ArrayList<GameCircle>();
	private int max_circle_n = 10; //maximum circle radius
	//game values
	//main menu values
	private ArrayList<GameCircle> menuObjects = new ArrayList<GameCircle>();
	private boolean menuVisible = false; //only false at end of fading
	private long menuFadeTime = 0; //disappearance initialization time for the main menu
	private class GameButton extends GameObject {
		public String buttonText = "Button";
		private class TextColor {
			int r = 0;
			int g = 0;
			int b = 0;
			int a = 255;
			public Color toColor() {
				return new Color(r,g,b,a);
			}
		}
		public TextColor textColor = new TextColor();
		public void renderText(Graphics2D g, int padding_x, int padding_y, Font ...font) {
			if (font.length==0) g.setFont(new Font("Arial", (int)transform.width-padding_x, (int)transform.height-padding_y));
			else g.setFont(font[0]);
			g.setColor(textColor.toColor());
			g.drawString(buttonText, (int)transform.x+padding_x, (int)transform.y+padding_y);
		}
		public GameButton(String bText, boolean rounded, String ...attrs) {
			super(rounded?"roundedRect":"rect", attrs);
			buttonText = bText;
		}
	}
	private ArrayList<GameButton> menuButtons = new ArrayList<GameButton>(); //list of menu buttons
	//main menu values
	//other values
		private final long second_n = (long)Math.pow(10, 9); //divide by this to get seconds
		private final long msecond_n = (long)Math.pow(10, 5); //divide by this to get milliseconds
		private boolean mouseHasMoved = false;
		private boolean mouseHasClicked = false;
		private Point mouseMovePosition;
		private Point mouseClickPosition;
	//other values
	public ProbabilityGame() { //initialization
		window = new JFrame("Project X");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		window.setBounds((screen.width-d[0])/2,(screen.height-d[1])/2,d[0],d[1]);
		window.addWindowListener(new CloseWindow());
		
		canvas = new Canvas(cfg);
		canvas.setSize(window.getBounds().getSize());
		canvas.addMouseMotionListener(new MoveMouse());
		window.add(canvas, 0);
		window.setVisible(true); //show the window
		window.setResizable(false);
		
		content_d[0] = window.getSize().width;
		content_d[1] = window.getSize().height-window.getInsets().top-window.getInsets().bottom;
		background = createBufferedImage(content_d[0],content_d[1],false);
		canvas.createBufferStrategy(2);
		do {
			strategy = canvas.getBufferStrategy();
		} while (strategy == null);
		start(); //set running thread
		menuStart();
	}
	private class CloseWindow extends WindowAdapter { //perform window closing actions
		@Override
		public void windowClosing(final WindowEvent e) {
			running = false;
			System.out.println("Exited successfully.");
		}
	}
	private class MoveMouse extends MouseAdapter { //check for mouse events
		@Override
		public void mouseMoved(MouseEvent e) {
			//get move event data
			mouseMovePosition = e.getPoint();
			mouseHasMoved = true;
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			mouseClickPosition = e.getPoint();
			mouseHasClicked = true;
		}
	}
	public BufferedImage createBufferedImage(final int width, final int height, final boolean alpha) {
		return cfg.createCompatibleImage(width, height, alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
	}
	private Graphics2D getBuffer() { //return buffer for graphics
		if (g2d == null) try {
			g2d = (Graphics2D) strategy.getDrawGraphics();
		} catch (IllegalStateException e) {
			return null;
		}
		return g2d;
	}
	public void run() { //thread process to iterate
		b_g2d = (Graphics2D) background.getGraphics();
		main: while (running) {
			gameUpdate();
			do {
				Graphics2D bg = getBuffer(); //background
				if (!running) break main;
				render(b_g2d); //render to background graphics
				bg.drawImage(background, 0, 0, null);
			} while (!screenUpdate());
		}
		window.dispose(); //release system resources
	}
	public boolean screenUpdate() {
		g2d.dispose(); //release system resources
		g2d = null;
		try {
			strategy.show();
			Toolkit.getDefaultToolkit().sync();
			return (!strategy.contentsLost());
		} catch (NullPointerException e) {
			return true;
		} catch (IllegalStateException e) {
			return true;
		}
	}
	public void menuStart() { //start initial main menu; no rendering involved
		GameButton playNow = new GameButton("Play Now",true); //add play now button
		playNow.textColor.r = 170;
		playNow.textColor.g = 200;
		playNow.textColor.b = 230;
		playNow.textColor.a = 140;
		playNow.color.r = 120;
		playNow.color.g = 190;
		playNow.color.b = 200;
		playNow.color.a = (int)(playNow.textColor.a*.4);
		playNow.transform.width = 100;
		playNow.transform.height = 40;
		playNow.transform.x = (d[0]-100)/2;
		playNow.transform.y = (d[1]-40)/2 + 200;
		menuButtons.add(playNow);
		menuVisible = true; //set menu visible (important)
	}
	public void gameStart() { //start the game aspect; populate
		for (int i=0; i<10; i++) {
			Random rand = new Random();
			int rd = rand.nextInt(max_circle_n); //circle's radius
			GameCircle circ = new GameCircle(rd, 1+rand.nextInt(rd-1));
			circ.transform.x = rand.nextInt(d[0]);
			circ.transform.y = rand.nextInt(d[1]);
			circ.color.randomize();
			gameObjects.add(circ);
		}
		gameStarted = true;
		menuFadeTime = System.nanoTime();
	}
	public void gameUpdate() { //update game aspect, but don't render
		if (menuVisible) {
			//TODO: buttons
			
		}
		if (gameStarted) {
			for (GameCircle gc:gameObjects) { //iterate through all game circles
				//TODO: iteration
				
				//animation and events
				if (gc.event.mouseOverStarted != 0 //fade out animation
						&& (long)(System.nanoTime()-gc.event.mouseOverStarted)/msecond_n < 1200) {
					long delta = (System.nanoTime()-gc.event.mouseOverStarted)/1200;
					gc.color.a = 255 * (int)(1-Math.pow(delta, 3)); //opacity toggling
				} else {
					gc.event.mouseOverStarted = 0;
				}
			}
		}
		if (mouseHasMoved) {
			if (gameStarted)
				for (GameCircle gc:gameObjects) //perform a search; do stuff
					if (gc.visible && gc.event.mouseIsOver(mouseMovePosition)) {
						if (gc.event.mouseOverStarted == 0)
							gc.event.mouseOverStarted = System.nanoTime(); //log start time
					}
			
			mouseHasMoved = false;
		}
		if (mouseHasClicked) {
			//TODO: code
			if (menuVisible)
				for (GameButton gB:menuButtons) {
					if (gB.visible && gB.event.mouseIsOver(mouseClickPosition)) {
						if (gB.buttonText == "Play Now") {
							
						}
					}
				}
			mouseHasClicked = false;
		}
	}
	public void render(Graphics2D g) { //render everything to background graphics on canvas
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.black); //paint black background for effective antialiasing
		g.fillRect(0, 0, d[0], d[1]);
		if (menuVisible) { //render main menu
			g.setColor(new Color(230,245,255,250)); //render big title
			g.setFont(new Font("Consolas", 300, 60));
			g.drawString("Project X", (int)(d[0]-300)/2, 200);
			for (GameButton gB:menuButtons) { //iterate through menuButtons
				gB.render(g, true);
				gB.renderText(g, 9, 28, new Font("Arial",(int)gB.transform.width-10,(int)gB.transform.height-20));
			}
		}
		if (gameStarted)
			for (GameCircle gc:gameObjects) { //iterate through all circles on display
				g.setColor(gc.color.toColor());
				gc.render(g, true);
			}
	}
	public static void main(String[] args) {
		new ProbabilityGame();
	}
}
