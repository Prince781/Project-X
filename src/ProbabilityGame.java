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

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;

public class ProbabilityGame extends Thread {
	private GUICanvas gui;
	private long lastTime = 0;
	public void run() { //thread runner
		if (gui.isRunning) {
			System.out.println("Running...");
		}
	}
	public void clearGraphics() {
		
	}
	public ProbabilityGame() throws Exception {
		gui = new GUICanvas("Probability Game", new Canvas() {
			@Override
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.black);
				g.clearRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
				g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
				g.setColor(new Color(124,250,130));
				g.drawRect(100, 300, 40, 40);
				g.setColor(Color.gray);
				g.drawArc(100, 100, 300, 300, 0, 360);
				
			}
		});
	}
	public static void main(String[] args) throws Exception {
		ProbabilityGame g = new ProbabilityGame();
	}
}
