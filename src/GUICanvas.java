//gui code for the probability game
package game;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUICanvas {
	private JFrame window;
	private String wn_name = "Window 1";
	private int[] d = {800,800}; //dimensions
	public Canvas canvas; //game's drawing canvas
	public boolean isRunning = true; //running variable
	public GUICanvas(String window_name, Canvas cnv, int... dimensions) throws Exception { //initialization
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		wn_name = window_name;
		window = new JFrame(wn_name);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		for (int i=0; i<dimensions.length && i<d.length; i++) 
			d[i] = dimensions[i];
		window.setBounds((screen.width-d[0])/2,(screen.height-d[1])/2,d[0],d[1]); //center
		canvas = cnv;
		canvas.setSize(window.getBounds().getSize());
		window.getContentPane().add(canvas);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isRunning = false;
				System.out.println("Successfully ended.");
				System.exit(0);
			}
		});
		window.setResizable(false);
		window.setVisible(true); //show the window
		System.out.println("Successfully started GUICanvas instantiation.");
	}
}
