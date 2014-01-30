package com;

import java.awt.*;
import java.util.*;

public class GameObject {
	public final class GameObjectTransform {
		public double x = 0;
		public double y = 0;
		public double r = 0;
		public double width = 0;
		public double height = 0;
		public double distanceFrom(double x2, double y2) {
			return Math.sqrt(Math.pow(x2-x,2)+Math.pow(y2-y,2));
		}
		public void translate(double deltaX, double deltaY) {
			x+=deltaX;
			y+=deltaY;
		}
		public Rectangle toRectangle() {
			return new Rectangle((int)x,(int)y,(int)width,(int)height);
		}
	}
	public final class GameObjectColor {
		public int r = 255;
		public int g = 255;
		public int b = 255;
		public int a = 255;
		private int savedR = r;
		private int savedG = g;
		private int savedB = b;
		private int savedA = a;
		public Color toColor() {
			return new Color(r,g,b,a);
		}
		public void randomize() {
			Random rand = new Random();
			r = rand.nextInt(255);
			g = rand.nextInt(255);
			b = rand.nextInt(255);
			a = rand.nextInt(255);
		}
		public GameObjectColor setColor(int...clr) {
			if (clr.length==1) r = clr[0];
			if (clr.length==2) g = clr[1];
			if (clr.length==3) b = clr[2];
			if (clr.length==4) a = clr[3];
			return this;
		}
		public GameObjectColor saveColor() {
			savedR = r;
			savedG = g;
			savedB = b;
			savedA = a;
			return this;
		}
		public Color savedColor() {
			return new Color(savedR, savedG, savedB, savedA);
		}
	}
	public final class GameObjectEvent {
		public long mouseOverStarted = 0;
		public long mouseOutStarted = 0;
		public long mouseClickStarted = 0;
		public boolean mouseWasOver = false;
		public boolean mouseIsOver(Point p) {
			mouseWasOver = (type=="circle"?transform.distanceFrom(p.x, p.y)<=transform.r:((p.x-transform.x<=transform.width&&p.x-transform.x>=0)&&(p.y-transform.y<=transform.height&&p.y-transform.y>=0)));
			return mouseWasOver;
		}
	}
	public final GameObjectTransform transform = new GameObjectTransform();
	public final GameObjectColor color = new GameObjectColor();
	public final GameObjectEvent event = new GameObjectEvent();
	public String[] attributes;
	public final String type;
	public boolean visible = true;
	public void render(Graphics2D g, boolean fill) { //simplify drawing process
		g.setColor(color.toColor());
		switch (type) {
		case "rect":
			if (fill) g.fillRect((int)transform.x, (int)transform.y, (int)transform.width, (int)transform.height);
			else g.drawRect((int)transform.x, (int)transform.y, (int)transform.width, (int)transform.height);
			break;
		case "circle":
			if (fill) g.fillArc((int)transform.x-(int)transform.r/2, (int)transform.y-(int)transform.r/2, (int)transform.r, (int)transform.r, 0, 360);
			else g.drawArc((int)transform.x-(int)transform.r/2, (int)transform.y-(int)transform.r/2, (int)transform.r, (int)transform.r, 0, 360);
			break;
		case "roundedRect":
			if (fill) g.fillRoundRect((int)transform.x, (int)transform.y, (int)transform.width, (int)transform.height, 10, 10);
			else g.drawRoundRect((int)transform.x, (int)transform.y, (int)transform.width, (int)transform.height, 10, 10);
			break;
		}
	}
	public GameObject(String gameObjectType, String ...attrs) {
		type = gameObjectType;
		attributes = new String[attrs.length];
		for (int a=0; a<attrs.length; a++) attributes[a] = attrs[a];
	}
}
