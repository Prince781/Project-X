/***************************************
 * Probability Game - Project X
 * ------------------------------------
 * An academic exploration into 
 * mathematical probability of user
 * decision, using a visual game
 * consisting of successive circle 
 * reductions.
 * ------------------------------------
 * 2013 P. Ferro & D. Casazz
****************************************/
package com;

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
	private boolean gameStarted = false;
	private long gameFadeInTime = 0;
	private class GameCircle extends GameObject {
		private class GameAttrs {
			public int radius;
			public int savedRadius = 0;
			public int u_radius;
			public boolean revealed = false;
			public void shrink() {
				if (savedRadius == 0 || transform.r/10 == savedRadius-1)
					savedRadius = radius;
				radius--;
				shrinkTime = System.nanoTime();
			}
			public long shrinkTime = 0; //for animation
		}
		GameAttrs game = new GameAttrs();
		public GameCircle(int radius, int u_radius, String ...attrs) {
			super("circle", attrs);
			game.radius = radius;
			transform.r = radius*10; //set realistic radius
			game.u_radius = u_radius; //set unknown black circle radius
		}
	}
	private ArrayList<GameCircle> gameObjects = new ArrayList<GameCircle>();
	private int max_circle_n = 10; //maximum circle radius
	private int defusedCircles = 0; //number of defused circles
	private int failedCircles = 0; //number of failed circles
	private int numGameCircles = 9; //number of game circles
	private int gameScore = 0;
	private long levelTimeout = 90*(long)Math.pow(10, 9); //time (s) to complete level
	private long origLevelTimeout = levelTimeout;
	private long levelStarted; //time at which the level has started
	private class GameText {
		public Font font;
		public int x;
		public int y;
		public int a = 255; //alpha opacity
		public String text;
		public long animTime = 0;
		public GameText(String renderedText, String fontName, int posX, int posY, int width, int height) {
			font = new Font(fontName, width, height);
			x = posX;
			y = posY;
			text = renderedText;
		}
	}
	private GameText gameScoreText = new GameText("Score: ","Arial",10,20,100,14);
	private GameText levelTimeText = new GameText("Time: ", "Arial",d[0]-10,20,100,14);
	private GameText gameOverNotif = new GameText("Game Over","Consolas",d[0]/2,400,300,26);
	private GameText maxURadiusText = new GameText("Largest Radius Underneath: ","Arial",10,d[1]-50,100,14);
	private int gameMaxURadius = 0; //maximum unknown radius
	private boolean gameOver = false;
	private double gameOverallOpacity = 0.0;
	//game values
	//main menu values
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
	int menuOverallOpacity = 255; //for menu fading animation
	//main menu values
	//other values
	private final long msecond_n = (long)Math.pow(10, 6); //divide by this to get milliseconds
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
		canvas.addMouseMotionListener(new MouseAction());
		canvas.addMouseListener(new MouseAction());
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
	private class MouseAction extends MouseAdapter { //check for mouse events
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
		if (gameStarted) return;
		for (int i=0; i<numGameCircles; i++) {
			Random rand = new Random();
			int rd = 2+rand.nextInt(max_circle_n-2); //circle's radius
			GameCircle circ = new GameCircle(rd, 1+rand.nextInt(rd-1));
			circ.transform.x = max_circle_n*10+rand.nextInt(d[0]-2*max_circle_n*10);
			circ.transform.y = max_circle_n*(int)Math.floor(Math.sqrt(i-1))
					+rand.nextInt(d[1]-3*max_circle_n*5);
			circ.color.randomize();
			circ.color.a = 100;
			gameObjects.add(circ);
		}
		if (menuVisible) {
			menuFadeTime = System.nanoTime();
			//save menu item color states
			for (GameButton gB:menuButtons)
				gB.color.saveColor();
		}
		gameOverNotif.a = 0; //alter game over notification opacity
	}
	public void gameUpdate() { //update game aspect, but don't render
		if (menuFadeTime != 0 && menuVisible) { //fade away the menu
			if ((long)(System.nanoTime()-menuFadeTime)/msecond_n < 1000) {
				double delta = (double)(System.nanoTime()-menuFadeTime)/msecond_n/1000;
				menuOverallOpacity = (int)(255 * (1-Math.pow(delta, 3)));
				for (GameButton gB:menuButtons) //iterate through all menu buttons and their colors
					gB.color.a = (int)(gB.color.savedColor().getAlpha() * (1-Math.pow(delta, 3)));
			} else {
				menuVisible = false;
				gameStarted = true;
				gameFadeInTime = System.nanoTime();
			}
		} else if (menuVisible) 
			for (GameButton gB:menuButtons) { //iterate through all menu buttons
				if (gB.event.mouseOverStarted != 0 //fade in/out button animation
					&& (long)(System.nanoTime()-gB.event.mouseOverStarted)/msecond_n < 700) {
					gB.event.mouseOutStarted = 0;
					double delta = (double)(System.nanoTime()-gB.event.mouseOverStarted)/msecond_n/700;
					gB.color.a = gB.color.savedColor().getAlpha() + (int)((80-gB.color.savedColor().getAlpha()) * Math.pow(delta, 3)); //opacity fading
				} else gB.event.mouseOverStarted = 0;
				if (gB.event.mouseOutStarted != 0 //mouse out event
					&& (long)(System.nanoTime()-gB.event.mouseOutStarted)/msecond_n < 700) {
					gB.event.mouseOverStarted = 0;
					double delta = (double)(System.nanoTime()-gB.event.mouseOutStarted)/msecond_n/700;
					gB.color.a = gB.color.savedColor().getAlpha() + (int)((56-gB.color.savedColor().getAlpha()) * Math.pow(delta, 3));
				} else gB.event.mouseOutStarted = 0;
			}
		if (gameOver) {
			//levelTimeout = 0;
			if (gameOverNotif.animTime == 0 && gameOverNotif.a == 0)
				gameOverNotif.animTime = System.nanoTime();
			else if (gameOverNotif.animTime != 0
					&& (long)(System.nanoTime()-gameOverNotif.animTime)/msecond_n < 700) {
				double delta = (double)((System.nanoTime()-gameOverNotif.animTime)/msecond_n)/700;
				gameOverNotif.a = (int)(255*Math.pow(delta, 3));
			} else gameOverNotif.animTime = 0;
		}
		if (gameStarted) {
			if (!gameOver) {
				if (levelStarted != 0)
					levelTimeout = origLevelTimeout-(System.nanoTime()-levelStarted);
				if (gameFadeInTime != 0 && (long)(System.nanoTime()-gameFadeInTime)/msecond_n < 1000) {
					double delta = (double)((System.nanoTime()-gameFadeInTime)/msecond_n)/1000;
					gameOverallOpacity = Math.pow(delta, 3);
				} else if ((long)(System.nanoTime()-gameFadeInTime)/msecond_n >= 1000 && levelStarted==0) {
					gameOverallOpacity = 1.0;
					levelStarted = System.nanoTime();
					System.out.println("A new game has started");
				} else gameFadeInTime = 0;
			}
			for (GameCircle gc:gameObjects) { //iterate through all game circles
				//animation and events
				if (gc.event.mouseOverStarted != 0 //fade out animation
						&& (long)(System.nanoTime()-gc.event.mouseOverStarted)/msecond_n < 700) {
					gc.event.mouseOutStarted =0;
					double delta = (double)((System.nanoTime()-gc.event.mouseOverStarted)/msecond_n)/700;
					gc.color.a = gc.color.savedColor().getAlpha() + (int)((200-gc.color.savedColor().getAlpha()) * Math.pow(delta, 3));
				} else gc.event.mouseOverStarted = 0;
				if (gc.event.mouseOutStarted != 0 
						&& (long)(System.nanoTime()-gc.event.mouseOutStarted)/msecond_n < 700) {
					gc.event.mouseOverStarted = 0;
					double delta = (double)((System.nanoTime()-gc.event.mouseOutStarted)/msecond_n)/700;
					gc.color.a = gc.color.savedColor().getAlpha() + (int)((100-gc.color.savedColor().getAlpha()) * Math.pow(delta, 3));
					
				} else gc.event.mouseOutStarted = 0;
				if (gc.game.shrinkTime != 0 && (long)(System.nanoTime()-gc.game.shrinkTime)/msecond_n < 700) {
					double delta = (double)((System.nanoTime()-gc.game.shrinkTime)/msecond_n)/700;
					gc.transform.r = gc.game.savedRadius*10 + (int)((gc.game.radius*10-gc.game.savedRadius*10)*Math.pow(delta, 3));
				} else if (gc.game.shrinkTime != 0) {
					gc.transform.r = gc.game.radius*10;
					gc.game.shrinkTime = 0;
				}
			}
			if (!gameOver) {
				defusedCircles = 0; //reset number of defused circles
				failedCircles = 0; //reset number of failed circles
				gameMaxURadius = 0; //reset maximum unknown radius
				for (GameCircle gc:gameObjects) {
					defusedCircles += (gc.game.radius==gc.game.u_radius ? 1 : 0);
					failedCircles += (gc.game.radius<gc.game.u_radius ? 1 : 0);
					gameMaxURadius = gc.game.u_radius;
					for (GameCircle gc2:gameObjects)
						if (gc2.game.u_radius > gameMaxURadius)
							gameMaxURadius = gc2.game.u_radius;
				}
				if (levelTimeout <= 0 || failedCircles > 0)
					gameOver = true;
			}
		}
		if (mouseHasMoved) {
			if (menuVisible)
				for (GameButton gB:menuButtons) { //perform menuButton search
					if (gB.visible 
							&& gB.event.mouseIsOver(mouseMovePosition)
							&& gB.event.mouseOverStarted == 0) {
						gB.event.mouseOverStarted = System.nanoTime(); //log start time
						gB.color.saveColor(); //save object color at this point
					} else if (gB.visible 
							&& !gB.event.mouseIsOver(mouseMovePosition)
							&& gB.event.mouseOutStarted == 0) {
						gB.event.mouseOutStarted = System.nanoTime();
						gB.color.saveColor(); //save object color
					}
				}
			else if (gameStarted && !gameOver)
				for (GameCircle gc:gameObjects) { //perform a search; do stuff
					if (gc.visible 
							&& gc.event.mouseIsOver(mouseMovePosition) 
							&& gc.event.mouseOverStarted == 0) {
						gc.event.mouseOverStarted = System.nanoTime(); //log start time
						gc.color.saveColor(); //save object color
					} else if (gc.visible 
							&& !gc.event.mouseIsOver(mouseMovePosition) 
							&& gc.event.mouseOutStarted == 0) {
						gc.event.mouseOutStarted = System.nanoTime();
						gc.color.saveColor(); //save object color
					}
				}
			mouseHasMoved = false;
		}
		if (mouseHasClicked) {
			if (menuVisible && menuFadeTime == 0)
				for (GameButton gB:menuButtons)
					if (gB.visible && gB.event.mouseIsOver(mouseClickPosition))
						if (gB.buttonText == "Play Now")
							gameStart(); //start the game
			if (gameStarted && !gameOver) {
				for (GameCircle gc:gameObjects) {
					if (gc.visible && gc.event.mouseIsOver(mouseClickPosition)
							&& !gc.game.revealed) {
						gameScore += gc.game.radius;
						gc.game.shrink();
						gc.game.revealed = (gc.game.radius<gc.game.u_radius);
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
			g.setColor(new Color(230,245,255,menuOverallOpacity)); //render big title
			g.setFont(new Font("Consolas", 300, 60));
			g.drawString("Project X", (int)(d[0]-300)/2, 200);
			for (GameButton gB:menuButtons) { //iterate through menuButtons
				gB.render(g, true);
				gB.renderText(g, 9, 28, new Font("Arial",(int)gB.transform.width-10,(int)gB.transform.height-20));
			}
		} else if (gameStarted) {
			//render score text
			g.setColor(new Color(255,255,255,(int)(255*gameOverallOpacity)));
			g.setFont(gameScoreText.font);
			g.drawString(gameScoreText.text+gameScore, gameScoreText.x, gameScoreText.y);
			//render time text
			g.setFont(levelTimeText.font);
			int newLevelTime = (int)(levelTimeout/1000000000);
			if (newLevelTime < 10) g.setColor(new Color(255,40,40,(int)(255*gameOverallOpacity)));
			else g.setColor(new Color(255,255,255,(int)(255*gameOverallOpacity)));
			g.drawString(levelTimeText.text+newLevelTime+" s", levelTimeText.x-g.getFontMetrics(levelTimeText.font).stringWidth(levelTimeText.text+newLevelTime+" s"), levelTimeText.y);
			//render max u_radius text
			g.setFont(maxURadiusText.font);
			g.setColor(new Color(255,255,255,(int)(255*gameOverallOpacity)));
			g.drawString(maxURadiusText.text+gameMaxURadius, maxURadiusText.x, maxURadiusText.y);
			for (GameCircle gc:gameObjects) { //iterate through all circles on display
				int saveColor = gc.color.a;
				gc.color.a = (int)(saveColor*gameOverallOpacity);
				gc.render(g, true);
				gc.color.a = saveColor;
				if (gc.game.revealed) {
					g.setColor(gc.color.toColor().darker().darker());
					g.fillArc((int)(gc.transform.x-gc.game.u_radius*5), 
							(int)(gc.transform.y-gc.game.u_radius*5), 
							gc.game.u_radius*10, 
							gc.game.u_radius*10, 
							0, 360);
				}
				Font fnt = new Font("Arial", (int)gc.game.radius*10, (int)(20*gc.game.radius/4));
				g.setFont(fnt);
				g.setColor(new Color(255,255,255,(int)(255*gameOverallOpacity)));
				g.drawString(""+gc.game.radius, (int)gc.transform.x-g.getFontMetrics(fnt).stringWidth(""+gc.game.radius)/2, (int)(gc.transform.y+g.getFontMetrics(fnt).stringWidth(""+gc.game.radius)/1.5));
			}
			if (gameOver) { //render game over notification
				g.setColor(new Color(255,255,255,(int)(gameOverNotif.a*gameOverallOpacity)));
				g.setFont(gameOverNotif.font);
				g.drawString(gameOverNotif.text, gameOverNotif.x-g.getFontMetrics(gameOverNotif.font).stringWidth(gameOverNotif.text)/2, gameOverNotif.y);
			}
		}
	}
	public static void main(String[] args) {
		new ProbabilityGame();
	}
}
