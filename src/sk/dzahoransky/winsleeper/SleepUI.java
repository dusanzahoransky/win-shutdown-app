package sk.dzahoransky.winsleeper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
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

import sk.dzahoransky.winsleeper.SleepUI.Action;

public class SleepUI implements IDisplay, IActionProvider {
	
	public static enum Action{Sleep, Hibernate, Shutdown};

	private ButtonGroup preddefinedTimeOptions;
	private JTextField customTime;
	private Scheduler scheduler;
	private JPanel timerOptionsPanel;
	private JPanel countDownPanel;
	private JFrame window;
	private ButtonGroup shutDownOptions;
	private JLabel countDownDisplay;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		//construct and cross connect display and scheduler 
		SleepUI ui = new SleepUI();
		ui.scheduler = new Scheduler(ui);
	}

	public SleepUI() {
		window = new JFrame("Windows shutdown scheduler.");
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
		Integer countdownTime = getSelectedTime();
		if(countdownTime == null) return; 
		
		scheduler.start(countdownTime, this);
		
		//hide selection panel and display countdown
		timerOptionsPanel.setVisible(false);
		window.remove(timerOptionsPanel);
		window.add(countDownPanel, BorderLayout.CENTER);
		window.validate();
	}

	private Integer getSelectedTime() {
		if (preddefinedTimeOptions.getSelection() != null) {
			return Integer.parseInt(preddefinedTimeOptions.getSelection().getActionCommand());
		} 
		if (customTime.getInputVerifier().verify(customTime)) {
			return Integer.parseInt(customTime.getText());
		}
		return null;	//custom time, not a number
	}

	private void stopCountDown() {
		scheduler.stop();
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
				
		Arrays.stream(Action.values()).forEach( a -> {
			JRadioButton rb = new JRadioButton(a.name());
			rb.setActionCommand(a.name());
			if(a==Action.Shutdown)rb.setSelected(true);
			shutDownOptions.add(rb);
			pShutDownOpt.add(rb);
		});

		p.add(pShutDownOpt, BorderLayout.CENTER);

		return p;
	}

	/**
	 * Init and bind to the panel timeout options.
	 * @return
	 */
	private JPanel createTimerOptionsPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 7));
		preddefinedTimeOptions = new ButtonGroup();

		Stream.of("30","45","60","75","90","120").forEach( option -> {
			JRadioButton rb = new JRadioButton(option);
			rb.setActionCommand(option);
			if("45".equals(option))rb.setSelected(true);
			preddefinedTimeOptions.add(rb);
			p.add(rb);
		});

		customTime = new JTextField("240");
		customTime.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				try {
					int val = Integer.parseInt(customTime.getText());
					if (val < 1) return false;
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		});
		customTime.getDocument().addDocumentListener(new DocumentListener() {
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
				if (customTime.getInputVerifier().verify(customTime)) {
					preddefinedTimeOptions.clearSelection();
				}
			}
		});
		p.add(customTime);
		return p;
	}
	
	@Override
	public Action getAction() {
		return Action.valueOf(shutDownOptions.getSelection().getActionCommand());
	}

	@Override
	public void refresh(String textToShow) {
		countDownDisplay.setText(textToShow);
	}
}

interface IDisplay {
	public void refresh(String textToShow);
}
interface IActionProvider {
	public Action getAction();
}