package game;

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
	}
	public final class GameObjectColor {
		public int r = 255;
		public int g = 255;
		public int b = 255;
		public int a = 255;
		public void randomize() {
			Random rand = new Random();
			r = rand.nextInt(255);
			g = rand.nextInt(255);
			b = rand.nextInt(255);
			a = rand.nextInt(255);
		}
	}
	public final GameObjectTransform transform = new GameObjectTransform();
	public final GameObjectColor color = new GameObjectColor();
	final String type;
	public boolean visible = true;
	public GameObject(String gameObjectType) {
		type = gameObjectType;
	}
}
