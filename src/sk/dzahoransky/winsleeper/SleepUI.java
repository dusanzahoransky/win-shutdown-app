package sk.dzahoransky.winsleeper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
		createCountDownPanel();

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

	private void createCountDownPanel() {
		countDownPanel = new JPanel();
		countDownDisplay = new JLabel();
		countDownDisplay.setFont(new Font("Verdana", Font.PLAIN, 20));
		countDownPanel.add(countDownDisplay);
	}

	private void startCountDown() {
		Optional.of(getTime()).ifPresent((time)->{
			timer.start(time, getAction());
			timerOptionsPanel.setVisible(false);
			window.remove(timerOptionsPanel);
			window.add(countDownPanel, BorderLayout.CENTER);
			window.validate();
		});
	}

	private Integer getTime() {
		Integer time = null;
		ButtonModel timeSelection = timeOptions.getSelection();
		if (timeSelection != null) {
			time = Integer.parseInt(timeSelection.getActionCommand());
		} else if (custom.getInputVerifier().verify(custom)) {
			time = Integer.parseInt(custom.getText());
		}
		return time;
	}

	private SleepTimer.Action getAction() {
		return SleepTimer.Action.valueOf(shutDownOptions.getSelection().getActionCommand());
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
		bStart.addActionListener((e)-> startCountDown());
		p.add(bStart, BorderLayout.WEST);
		
		JButton bStop = new JButton("Stop");
		bStop.addActionListener((e) -> stopCountDown());
		p.add(bStop, BorderLayout.EAST);

		JPanel pShutDownOpt = new JPanel();
		pShutDownOpt.setLayout(new FlowLayout(FlowLayout.CENTER));
		shutDownOptions = new ButtonGroup();
		
		Stream.of(SleepTimer.Action.values()).forEach((a)->{
			JRadioButton rb = new JRadioButton(a.name());
			rb.setActionCommand(a.name());
			if(a==SleepTimer.Action.Shutdown)rb.setSelected(true);
			shutDownOptions.add(rb);
			pShutDownOpt.add(rb);
		});

		p.add(pShutDownOpt, BorderLayout.CENTER);

		return p;
	}

	private JPanel createTimerOptionsPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 7));
		timeOptions = new ButtonGroup();

		Stream.of("30","45","60","75","90","120").forEach((option)->{
			JRadioButton rb = new JRadioButton(option);
			rb.setActionCommand(option);
			if(option.equals("45"))rb.setSelected(true);
			timeOptions.add(rb);
			p.add(rb);
		});

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
	public void refresh(long timeLeftSecs) {
		if(timeLeftSecs>60){
			countDownDisplay.setText("Time left: " + TimeUnit.SECONDS.toMinutes(timeLeftSecs) + " min.");
		}else{
			countDownDisplay.setText("Time left: " + timeLeftSecs + " sec.");
		}
	}
}

interface IDisplay {
	public void refresh(long timeLeft);
}