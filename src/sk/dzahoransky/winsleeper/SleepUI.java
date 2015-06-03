package sk.dzahoransky.winsleeper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SleepUI implements IDisplay {

	private ButtonGroup timeOptions;
	private JTextField custom;
	private SleepTimer timer;
	private JPanel timerOptionsPanel;
	private JPanel countDownPanel;
	private JFrame window;
	private ButtonGroup shutDownOptions;
	private JLabel countDownDisplay;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SleepUI ui = new SleepUI();
		ui.timer = new SleepTimer(ui);
	}

	public SleepUI() {
		window = new JFrame("WinSleeper - windows shutdown timer.");
		window.setSize(new Dimension(380, 100));

		window.setLayout(new BorderLayout());

		timerOptionsPanel = createTimerOptionsPanel();
		window.add(timerOptionsPanel, BorderLayout.CENTER);
		window.add(createButtonsPanel(), BorderLayout.SOUTH);
		countDownPanel = createCountDownPanel();

		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private JPanel createCountDownPanel() {
		JPanel p = new JPanel();
		countDownDisplay = new JLabel();
		countDownDisplay.setFont(new Font("Verdana", Font.PLAIN, 20));
		p.add(countDownDisplay);
		return p;
	}

	private void startCountDown() {
		Long time = getTime();
		if (time == null) {
			return;
		}
		timer.start(time, getAction());
		timerOptionsPanel.setVisible(false);
		window.remove(timerOptionsPanel);
		window.add(countDownPanel, BorderLayout.CENTER);
		window.validate();
	}

	private Long getTime() {
		Long time = null;
		ButtonModel timeSelection = timeOptions.getSelection();
		if (timeSelection != null) {
			time = Long.parseLong(timeSelection.getActionCommand()) * SleepTimer.MinMilis;
		} else if (custom.getInputVerifier().verify(custom)) {
			time = Long.parseLong(custom.getText()) * SleepTimer.MinMilis;
		}
		return time;
	}

	private SleepTimer.Action getAction() {
		ButtonModel selection = shutDownOptions.getSelection();
		return SleepTimer.Action.valueOf(selection.getActionCommand());
	}

	private void stopCountDown() {
		timer.stop();
		window.remove(countDownPanel);
		window.add(timerOptionsPanel, BorderLayout.CENTER);
		timerOptionsPanel.setVisible(true);
		timerOptionsPanel.validate();
		window.validate();
	}

	private JPanel createButtonsPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JButton bStart = new JButton("Start");
		bStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startCountDown();
			}

		});
		JButton bStop = new JButton("Stop");
		bStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCountDown();
			}
		});
		p.add(bStart, BorderLayout.WEST);
		p.add(bStop, BorderLayout.EAST);

		JPanel pShutDownOpt = new JPanel();
		pShutDownOpt.setLayout(new FlowLayout(FlowLayout.CENTER));

		JRadioButton sleep = new JRadioButton("Sleep");
		sleep.setActionCommand("Sleep");
		sleep.setSelected(true);
		JRadioButton hib = new JRadioButton("Hibernate");
		hib.setActionCommand("Hibernate");
		JRadioButton shutdown = new JRadioButton("Shutdown");
		shutdown.setActionCommand("Shutdown");

		shutDownOptions = new ButtonGroup();
		shutDownOptions.add(sleep);
		shutDownOptions.add(hib);
		shutDownOptions.add(shutdown);

		pShutDownOpt.add(sleep);
		pShutDownOpt.add(hib);
		pShutDownOpt.add(shutdown);

		p.add(pShutDownOpt, BorderLayout.CENTER);

		return p;
	}

	private JPanel createTimerOptionsPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 7));

		JRadioButton r30 = new JRadioButton("30");
		r30.setActionCommand("30");
		JRadioButton r45 = new JRadioButton("45");
		r45.setActionCommand("45");
		JRadioButton r60 = new JRadioButton("60");
		r60.setActionCommand("60");
		JRadioButton r75def = new JRadioButton("75");
		r75def.setActionCommand("75");
		r75def.setSelected(true);
		JRadioButton r90 = new JRadioButton("90");
		r90.setActionCommand("90");
		JRadioButton r120 = new JRadioButton("120");
		r120.setActionCommand("120");

		timeOptions = new ButtonGroup();
		timeOptions.add(r30);
		timeOptions.add(r45);
		timeOptions.add(r60);
		timeOptions.add(r75def);
		timeOptions.add(r90);
		timeOptions.add(r120);

		p.add(r30);
		p.add(r45);
		p.add(r60);
		p.add(r75def);
		p.add(r90);
		p.add(r120);

		custom = new JTextField("240");
		custom.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				int val = -1;
				try {
					val = Integer.parseInt(custom.getText());
				} catch (NumberFormatException e) {
					return false;
				}
				if (val < 1) {
					return false;
				}
				return true;
			}
		});
		custom.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onUpdate();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onUpdate();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onUpdate();
			}
			private void onUpdate() {
				if (custom.getInputVerifier().verify(custom)) {
					timeOptions.clearSelection();
				}
			}
		});
		p.add(custom);
		return p;
	}

	@Override
	public void refresh(long currentTime) {
		countDownDisplay.setText("Time left: " + currentTime / SleepTimer.MinMilis + " min.");
	}
}

interface IDisplay {
	public void refresh(long currentTime);
}