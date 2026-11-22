//Fridge.java
//Solo 1-day hackathon freezer mini-game in Java Swing

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;
import javax.sound.sampled.*;

public class Fridge
{
	public Fridge()
	{
	}

	public static void main(String[] args)
	{
		Fridge frd = new Fridge();
		frd.run(); //launch app
		frd.startMusic(); //start background music
	}

	public void run()
	{
		JFrame frame = new JFrame("Freezer Chill!");
		frame.setSize(1000, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 50);

		GameData gamdat = new GameData(); //shared game state
		GameHolder gh = new GameHolder(gamdat); //card screen container
		frame.getContentPane().add(gh);
		frame.setVisible(true);
		frame.setResizable(false); //keep null layout coords fixed
	}
	
	public void startMusic()
	{
		File soundFile;

		try
		{
			soundFile = new File("winterpiano.wav");
			AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);
			Clip soundclip = AudioSystem.getClip();
			soundclip.open(audio);
			soundclip.start(); //start sound
			soundclip.loop(Clip.LOOP_CONTINUOUSLY); //so sound keeps looping
			Thread.sleep(5000);
		}
		catch(Exception except)
		{
			except.printStackTrace();
		}
	}

	//styled button helper
	static JButton makeBtn(String text, Color bg, ActionListener al)
	{
		JButton b = new JButton(text);
		b.setFont(new Font("Monospaced", Font.BOLD, 17));
		b.setBackground(bg);
		b.setForeground(Color.WHITE);
		b.addActionListener(al);
		return b;
	}

	//draw stick + gradient red popsicle
	static void drawPopsicle(Graphics2D g2, int cx, int topY, int w, int h)
	{
		g2.setColor(new Color(215, 160, 95)); //stick color
		g2.fillRoundRect(cx - w / 8, topY + h - 4, w / 4, h / 2 + 6, 6, 6);

		GradientPaint popPaint = new GradientPaint(cx, topY, new Color(250, 65, 85), cx, topY + h, new Color(195, 15, 35));
		g2.setPaint(popPaint); //red gradient fill
		g2.fillRoundRect(cx - w / 2, topY, w, h, 34, 34);
	}
}

class GameData
{
	private int focusSeconds; //FV: study timer seconds left
	private boolean isDoorOpen; //FV: freezer door open flag
	private String[] messages; //FV: positive message pool
	private String[] habits; //FV: extra study habits not in text area

	public GameData()
	{
		focusSeconds = 25 * 60; //25 mins in seconds
		isDoorOpen = false;

		messages = new String[] {
			"You are doing great, keep going one step at a time!",
			"Cool down, take a deep breath, and trust your brain!",
			"Small progress today still counts as a major win!",
			"You've got way more grit than you give yourself credit for!",
			"Stay sweet, stay steady, and finish this block strong!",
			"Every hard problem you stick with makes the next one easier!",
			"You don't have to be perfect today—just keep moving forward!",
			"Give yourself some credit for showing up and putting in effort!",
			"Even a slow pace is still progress in the right direction!",
			"Take a sip of water, reset your shoulders, and keep shining!",
			"You have handled tough days before and you can handle this one too!",
			"Curiosity beats perfection every single time, keep exploring!"};

		//extra habits outside the main text area
		habits = new String[] {
			"Write a simple explanation like you are teaching a middle schooler.",
			"Try a practice problem before reading the solution guide.",
			"Spend 3 minutes writing everything you remember on a blank sheet.",
			"Highlight formulas in one color and definitions in another.",
			"Start with the easiest problem to build quick momentum.",
			"Switch to a quiet desk or library corner for a fresh start.",
			"Keep a list of missed questions and revisit them before test day."};
	}

	public boolean isDoorOpen()
	{
		return isDoorOpen;
	}

	public void setDoorOpen(boolean openIn)
	{
		isDoorOpen = openIn;
	}

	public int getFocusSeconds()
	{
		return focusSeconds;
	}

	public void setFocusSeconds(int secIn)
	{
		focusSeconds = secIn;
	}

	public String getRandomMessage()
	{
		int idx = (int)(Math.random() * messages.length); //random array index
		return messages[idx];
	}

	public String getRandomHabit()
	{
		int idx = (int)(Math.random() * habits.length); //random array index
		return habits[idx];
	}
}

class GameHolder extends JPanel
{
	private CardLayout cards; //FV: screen switcher
	private FreezerPanel freezerPanel; //FV: freezer screen ref

	public GameHolder(GameData gamdat)
	{
		cards = new CardLayout();
		setLayout(cards);

		freezerPanel = new FreezerPanel(this, gamdat);
		PopsiclePanel popsiclePanel = new PopsiclePanel(this, gamdat);
		IceCreamPanel iceCreamPanel = new IceCreamPanel(this, gamdat);

		add(freezerPanel, "freezer");
		add(popsiclePanel, "popsicle");
		add(iceCreamPanel, "icecream");
	}

	public void showCard(String name)
	{
		cards.show(this, name);
		if (name.equals("freezer"))
		{
			freezerPanel.refreshState(); //sync door state on return
		}
	}
}

class FreezerPanel extends JPanel implements MouseListener, ActionListener
{
	private GameHolder holder; //FV: card container
	private GameData gamdat; //FV: shared state
	private float doorSwing; //FV: 0.0 closed to 1.0 open
	private int tick; //FV: animation frame counter
	private JButton closeBtn; //FV: closes freezer door
	private Timer animTimer; //FV: ~30fps animation timer
	private float[] sx; //FV: snow x positions
	private float[] sy; //FV: snow y positions
	private float[] spd; //FV: snow fall speeds

	public FreezerPanel(GameHolder holderIn, GameData gamdatIn)
	{
		holder = holderIn;
		gamdat = gamdatIn;
		doorSwing = 0.0f;
		tick = 0;

		setLayout(null);
		addMouseListener(this);

		sx = new float[36];
		sy = new float[36];
		spd = new float[36];
		for (int i = 0; i < 36; i++)
		{
			sx[i] = (float)(Math.random() * 1000);
			sy[i] = (float)(Math.random() * 540);
			spd[i] = 0.5f + (float)(Math.random() * 1.3);
		}

		closeBtn = Fridge.makeBtn("CLOSE DOOR", new Color(65, 95, 130), this);
		closeBtn.setBounds(755, 270, 180, 46);
		closeBtn.setVisible(false);
		add(closeBtn);

		animTimer = new Timer(33, this); //~30fps
		animTimer.start();
	}

	public void refreshState()
	{
		if (gamdat.isDoorOpen())
		{
			doorSwing = 1.0f;
		}
		else
		{
			doorSwing = 0.0f;
		}
		closeBtn.setVisible(gamdat.isDoorOpen());
		repaint();
	}

	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource() == animTimer)
		{
			tick++;

			//ease doorSwing toward target
			float target = 0.0f;
			if (gamdat.isDoorOpen())
			{
				target = 1.0f;
			}

			if (doorSwing < target)
			{
				doorSwing += Math.min(target - doorSwing, 0.12f);
			}
			else if (doorSwing > target)
			{
				doorSwing -= Math.min(doorSwing - target, 0.12f);
			}

			//fall and sway snow
			for (int i = 0; i < 36; i++)
			{
				sy[i] += spd[i];
				sx[i] += (float)Math.sin(tick * 0.05 + sy[i] * 0.02) * 0.45f; //to make snow sway
				if (sy[i] > 540) //wrap to top
				{
					sy[i] = 55;
					sx[i] = (float)(Math.random() * 1000);
				}
			}

			repaint(); //so changes are visble
		}
		else if ("CLOSE DOOR".equals(evt.getActionCommand()))
		{
			gamdat.setDoorOpen(false);
			closeBtn.setVisible(false);
			repaint();
		}
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //smooth shapes

		Color handleColor = new Color(55, 82, 112);

		GradientPaint wallPaint = new GradientPaint(0, 0, new Color(205, 236, 250), 0, 600, new Color(165, 212, 238));
		g2.setPaint(wallPaint);
		g2.fillRect(0, 0, 1000, 600);

		g2.setColor(handleColor);
		g2.setFont(new Font("Monospaced", Font.BOLD, 34));
		g2.drawString("THE FREEZER", 380, 62);

		//freezer interior & shelf
		g2.setColor(new Color(150, 205, 238));
		g2.fillRoundRect(280, 95, 440, 420, 16, 16);
		g2.setStroke(new BasicStroke(3f));
		g2.setColor(handleColor);
		g2.drawRoundRect(280, 95, 440, 420, 16, 16);
		g2.fillRect(285, 300, 430, 10);

		//red popsicle
		Fridge.drawPopsicle(g2, 390, 135, 80, 95);
		g2.setColor(handleColor);
		g2.setStroke(new BasicStroke(2.5f));
		g2.drawRoundRect(350, 135, 80, 95, 35, 35);

		//neapolitan stripes (choc / vanilla / strawberry)
		g2.setColor(new Color(105, 55, 30));
		g2.fillRect(510, 350, 140, 35);
		g2.setColor(new Color(255, 248, 230));
		g2.fillRect(510, 385, 140, 35);
		g2.setColor(new Color(245, 130, 165));
		g2.fillRect(510, 420, 140, 35);
		g2.setColor(handleColor);
		g2.drawRect(510, 350, 140, 105);

		//narrow door as it swings open
		if (doorSwing < 0.98f)
		{
			int w = Math.max(48, (int)(400 * (1.0f - 0.78f * doorSwing)));
			g2.setColor(new Color(110, 178, 222));
			g2.fillRoundRect(300, 95, w, 420, 16, 16);
			g2.setStroke(new BasicStroke(3f));
			g2.setColor(handleColor);
			g2.drawRoundRect(300, 95, w, 420, 16, 16);
			g2.fillRoundRect(300 + w - 36, 255, 16, 95, 8, 8); //door handle
		}

		//semi-transparent snow
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
		g2.setColor(Color.WHITE);
		for (int i = 0; i < 36; i++)
		{
			g2.fillOval((int)sx[i], (int)sy[i], 5, 5);
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); //reset alpha

		g2.setColor(handleColor);
		g2.setFont(new Font("Monospaced", Font.BOLD, 19));
		if (gamdat.isDoorOpen())
		{
			g2.drawString("Click the red popsicle or Neapolitan ice cream!", 245, 555);
		}
		else
		{
			g2.drawString("Click the freezer door to swing it open!", 295, 555);
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();

		if (!gamdat.isDoorOpen())
		{
			if (x >= 300 && x <= 700 && y >= 95 && y <= 515) //door hitbox
			{
				gamdat.setDoorOpen(true);
				closeBtn.setVisible(true);
				repaint();
			}
		}
		else
		{
			if (x >= 350 && x <= 430 && y >= 135 && y <= 280) //popsicle hitbox
				holder.showCard("popsicle");
			else if (x >= 510 && x <= 650 && y >= 350 && y <= 455) //ice cream hitbox
				holder.showCard("icecream");
		}
	}

	public void mousePressed(MouseEvent e){}

	public void mouseReleased(MouseEvent e){}

	public void mouseEntered(MouseEvent e){}

	public void mouseExited(MouseEvent e){}
}

class PopsiclePanel extends JPanel implements ActionListener
{
	private GameHolder holder; //FV: card container
	private GameData gamdat; //FV: shared state
	private JLabel msgLabel; //FV: positive message text

	public PopsiclePanel(GameHolder holderIn, GameData gamdatIn)
	{
		holder = holderIn;
		gamdat = gamdatIn;

		setLayout(new BorderLayout(12, 12));
		setBackground(new Color(255, 226, 232));

		JLabel title = new JLabel("Red Popsicle Positivity", SwingConstants.CENTER);
		title.setFont(new Font("Monospaced", Font.BOLD, 32));
		title.setForeground(new Color(220, 40, 60));
		title.setBorder(BorderFactory.createEmptyBorder(18, 10, 8, 10));
		add(title, BorderLayout.NORTH);

		JPanel center = new JPanel(new BorderLayout())
		{
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Fridge.drawPopsicle(g2, getWidth() / 2, 22, 68, 78); //centered popsicle
			}
		};
		center.setOpaque(false);

		msgLabel = new JLabel("Press MESSAGE for a positive boost!", SwingConstants.CENTER);
		msgLabel.setFont(new Font("Monospaced", Font.BOLD, 21));
		msgLabel.setBorder(BorderFactory.createEmptyBorder(130, 35, 15, 35));
		center.add(msgLabel, BorderLayout.CENTER);
		add(center, BorderLayout.CENTER);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		south.setBackground(getBackground());
		south.add(Fridge.makeBtn("MESSAGE", new Color(220, 40, 60), this));
		south.add(Fridge.makeBtn("EXIT", new Color(75, 80, 95), this));
		add(south, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent evt)
	{
		String cmd = evt.getActionCommand();
		if ("MESSAGE".equals(cmd))
		{
			msgLabel.setText(gamdat.getRandomMessage());
		}
		else if ("EXIT".equals(cmd))
		{
			holder.showCard("freezer");
		}
	}
}

class IceCreamPanel extends JPanel implements ActionListener
{
	private GameHolder holder; //FV: card container
	private GameData gamdat; //FV: shared state
	private JLabel habitLabel; //FV: generated habit text
	private JLabel timerLabel; //FV: study clock readout
	private Timer focusTimer; //FV: 1-second countdown timer
	private boolean running; //FV: timer active flag

	public IceCreamPanel(GameHolder holderIn, GameData gamdatIn)
	{
		holder = holderIn;
		gamdat = gamdatIn;
		running = false;

		setLayout(new BorderLayout(10, 10));
		setBackground(new Color(255, 243, 234));

		JPanel north = new JPanel(new BorderLayout(6, 6));
		north.setOpaque(false);
		north.setBorder(BorderFactory.createEmptyBorder(14, 20, 4, 20));

		JLabel title = new JLabel("Neapolitan Study Habits", SwingConstants.CENTER);
		title.setFont(new Font("Monospaced", Font.BOLD, 28));
		title.setForeground(new Color(105, 55, 30));
		north.add(title, BorderLayout.NORTH);

		//3-color neapolitan bar
		JPanel stripe = new JPanel()
		{
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				int w = getWidth() / 3;
				g.setColor(new Color(105, 55, 30));
				g.fillRect(0, 0, w, getHeight());
				g.setColor(new Color(255, 248, 230));
				g.fillRect(w, 0, w, getHeight());
				g.setColor(new Color(245, 130, 165));
				g.fillRect(w * 2, 0, getWidth() - w * 2, getHeight()); //fill remaining width
			}
		};
		stripe.setPreferredSize(new Dimension(900, 12));
		north.add(stripe, BorderLayout.CENTER);

		timerLabel = new JLabel(formatTime(gamdat.getFocusSeconds()), SwingConstants.CENTER);
		timerLabel.setFont(new Font("Monospaced", Font.BOLD, 19));
		timerLabel.setForeground(new Color(150, 35, 65));
		north.add(timerLabel, BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);

		JPanel center = new JPanel(new BorderLayout(8, 8));
		center.setOpaque(false);
		center.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

		String habitText = " - Chocolate = Focus: Study in 25-minute blocks with zero phone distractions.\n"
				+ " - Vanilla = Concepts: Review key concepts and summarize in your own words.\n"
				+ " - Strawberry = Recall: Quiz yourself without notes and explain it out loud.\n"
				+ " - Space It Out: Short daily sessions beat last-minute cramming.\n"
				+ " - Breaks!: Step away, stretch, and drink water between study blocks.\n\n"
				+ "EXTRA FLAVORS (TIPS):\n"
				+ " - Cookie Dough = Break it Up (Chunks): Break big assignments into smaller tasks you can quickly check off.\n"
				+ " - Pistachio = Examples: Work through a brand new example problem from scratch without looking at steps.\n"
				+ " - Rocky Road = Obstacles: If you're stuck for more than 5 minutes, move on to another task and come back later.\n"
				+ " - Sherbet = SLEEP!!!!: Protect your sleep schedule so that you can focus during study sessions!\n"
				+ " - Mint Chip = Review: Go through your key takeaways before bed so your brain locks it in!\n";

		JTextArea area = new JTextArea(habitText);
		area.setFont(new Font("Monospaced", Font.BOLD, 17));
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		center.add(new JScrollPane(area), BorderLayout.CENTER);

		habitLabel = new JLabel("Press GENERATE HABIT for a study habit tip!", SwingConstants.CENTER);
		habitLabel.setFont(new Font("Monospaced", Font.BOLD, 19));
		center.add(habitLabel, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
		south.setBackground(getBackground());
		south.add(Fridge.makeBtn("GENERATE HABIT", new Color(185, 75, 110), this));
		south.add(Fridge.makeBtn("START/PAUSE STUDY", new Color(105, 55, 30), this));
		south.add(Fridge.makeBtn("EXIT", new Color(75, 80, 95), this));
		add(south, BorderLayout.SOUTH);

		focusTimer = new Timer(1000, this); //1-second tick
	}

	private String formatTime(int s)
	{
		int mins = s / 60;
		int secs = s % 60;
		return String.format("Focus Clock: %02d:%02d", mins, secs); //MM:SS format
	}

	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource() == focusTimer)
		{
			int s = gamdat.getFocusSeconds();
			if (s > 0)
			{
				s--;
				gamdat.setFocusSeconds(s);
				timerLabel.setText(formatTime(s));
			}
			else
			{
				focusTimer.stop();
				running = false;
				timerLabel.setText("Focus Clock: DONE!");
			}
		}
		else if ("GENERATE HABIT".equals(evt.getActionCommand()))
		{
			habitLabel.setText(gamdat.getRandomHabit());
		}
		else if ("START/PAUSE STUDY".equals(evt.getActionCommand()))
		{
			if (running)
			{
				focusTimer.stop();
				running = false;
			}
			else
			{
				focusTimer.start();
				running = true;
			}
		}
		else if ("EXIT".equals(evt.getActionCommand()))
		{
			holder.showCard("freezer");
		}
	}
}