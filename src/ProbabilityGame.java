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
	private long lastClick = 0;
	private long second_n = (long)Math.pow(10, 9);
	private boolean mouseHasMoved = false;
	private Point mousePos;
	private ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	private boolean gameStarted = false;
	//game values
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
		gameStart();
	}
	private class CloseWindow extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent e) {
			running = false;
			System.out.println("Exited successfully.");
			//System.exit(0);
		}
	}
	private class MoveMouse extends MouseAdapter {
		@Override
		public void mouseMoved(MouseEvent e) {
			//get move event data
			mousePos = e.getPoint();
			mouseHasMoved = true;
		}
	}
	public BufferedImage createBufferedImage(final int width, final int height, final boolean alpha) {
		return cfg.createCompatibleImage(width, height, alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
	}
	private Graphics2D getBuffer() {
		if (g2d == null) try {
			g2d = (Graphics2D) strategy.getDrawGraphics();
		} catch (IllegalStateException e) {
			return null;
		}
		return g2d;
	}
	public void run() { //thread process
		b_g2d = (Graphics2D) background.getGraphics();
		main: while (running) {
			long frameInitialUpdateTime = System.nanoTime();
			gameUpdate();
			do {
				Graphics2D bg = getBuffer(); //background
				if (!running) break main;
				render(b_g2d); //render to background graphics
				bg.drawImage(background, 0, 0, null);
			} while (!screenUpdate());
			long frameFinalUpdateTime = System.nanoTime();
			//System.out.println("Frame update took "+((double)(frameFinalUpdateTime-frameInitialUpdateTime)/second_n)+" s.");
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
	public void gameStart() { //start the game aspect
		//for (int i=0; i<30; i++) {
			Random rand = new Random();
			GameObject circ = new GameObject("circle");
			circ.transform.x = rand.nextInt(d[0]);
			circ.transform.y = rand.nextInt(d[1]);
			circ.transform.width = 40;
			circ.transform.height = 40;
			circ.transform.r = rand.nextInt(25);
			circ.color.randomize();
			gameObjects.add(circ);
		//}
	}
	public void gameUpdate() { //update game aspect
		if (mouseHasMoved) {
			mouseHasMoved = false;
			
		}
	}
	public void render(Graphics2D g) { //render to background graphics on canvas
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.black); //paint black background for effective antialiasing
		g.fillRect(0, 0, d[0], d[1]);
		for (GameObject gameObject:gameObjects) {
			switch (gameObject.type) {
			case "circle": //render a circle
				g.setColor(new Color(gameObject.color.r,gameObject.color.g,gameObject.color.b,gameObject.color.a));
				g.fillArc((int)gameObject.transform.x, (int)gameObject.transform.y, (int)gameObject.transform.width, (int)gameObject.transform.height, 0, 360);
				gameObject.color.randomize();
				break;
			}
		}
	}
	public static void main(String[] args) {
		new ProbabilityGame();
	}
}
