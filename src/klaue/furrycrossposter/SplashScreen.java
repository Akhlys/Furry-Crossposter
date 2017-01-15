package klaue.furrycrossposter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * Simple splash screen. Would be enough to call java with -splash:bla.jpg, but on multi-monitor-systems
 * the position is undefined and on linux, it centers between both screens which sucks
 * @author klaue
 *
 */
public class SplashScreen extends JWindow implements Runnable {
	private static final long serialVersionUID = 8942731281697603765L;
	BufferedImage img = null;
	boolean isPerPixelTranslucencySupported = false;
	SplashPanel panel = null;
	
	public SplashScreen() {
		try {
			img = ImageIO.read(SplashScreen.class.getResourceAsStream("/splash.png"));
			System.out.println("Splash image by Rainbow Boa - https://www.furaffinity.net/user/rainbow-boa/");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		isPerPixelTranslucencySupported = gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT);
		if (isPerPixelTranslucencySupported) {
	        setBackground(new Color(0, 255, 0, 0));
		}
		
		panel = new SplashPanel(isPerPixelTranslucencySupported, img);
		add(panel);
        pack();
		setSize(img.getWidth(), img.getHeight()); 
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
	}

	@Override
	public void run() {
		if (img != null) {
			setVisible(true);
		}
	}
	
	public void fadeOut(int fadeSteps, int delay) throws InterruptedException {
		float step = panel.getPaintTransparency() / fadeSteps;

		// stop one shy of finish, as float is not designed to be accurate and it could actually result in a value below
		// 0.9f - 0.1f = 0.79999995 XD
		for (int i = 0; i < fadeSteps - 1; ++i) {
			panel.setPaintTransparency(panel.getPaintTransparency() - step);
			//SwingUtilities.getWindowAncestor( this ).repaint();
			//this.revalidate();
			this.repaint();
			Thread.sleep(delay);
		}
	}
	
	public void stop() {
		if (img != null) {
			// it looks nicer when it stays just a bit longer
			try {
				if (isPerPixelTranslucencySupported) {
					// do not go under 70 or it flickers
					fadeOut(5, 100);
				} else {
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				// not really important, just ignore
			}
			setVisible(false);
			dispose();
		}
	}
}

class SplashPanel extends JPanel {
	private static final long serialVersionUID = -2912298738877068088L;
	boolean isPerPixelTranslucencySupported = false;
	BufferedImage img = null;
	float paintTransparency = 1;
	
	public SplashPanel(boolean isPerPixelTranslucencySupported, BufferedImage img) {
		this.isPerPixelTranslucencySupported = isPerPixelTranslucencySupported;
		this.img = img;
		if (isPerPixelTranslucencySupported) {
	        setBackground(new Color(0, 255, 0, 0));
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (img == null) return;
		super.paintComponent(g);
		
		if (g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, paintTransparency));
		}
		g.drawImage(img, 0, 0, null);
	}

	public float getPaintTransparency() {
		return paintTransparency;
	}

	public void setPaintTransparency(float paintTransparency) {
		this.paintTransparency = paintTransparency;
	}
}
