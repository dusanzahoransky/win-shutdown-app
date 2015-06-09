package sk.dzahoransky.winsleeper;
import java.io.IOException;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SleepTimer {
	
	public static enum Action{Sleep, Hibernate, Shutdown};
	
	private IDisplay display;
	
	private ScheduledExecutorService executor;
	private Delayed shutdownTimer;
	
	public SleepTimer(IDisplay display) {
		this.display = display;
	}
	
	public void stop() {
		if(executor!=null) executor.shutdownNow();
		System.out.println("Tasks canceled");
	}

	public void start(int time, Action action) {
		executor = Executors.newScheduledThreadPool(1);
		shutdownTimer = executor.schedule(()->execActionCommand(action), time, TimeUnit.MINUTES);
		executor.scheduleAtFixedRate(()->refreshDisplay(shutdownTimer.getDelay(TimeUnit.SECONDS)), 0, 1, TimeUnit.MINUTES);
		executor.scheduleAtFixedRate(()->refreshDisplay(shutdownTimer.getDelay(TimeUnit.SECONDS)), TimeUnit.MINUTES.toSeconds(time-1), 1, TimeUnit.SECONDS);
		
		System.out.println(String.format("Tasks scheduled (action=%s, time=%smin)", action, time));
	}

	private void execActionCommand(Action action) {
		String cmd = null;
		
		switch (action) {
		case Sleep:
			cmd = "powercfg -hibernate off && rundll32.exe powrprof.dll,SetSuspendState 0,1,0";
			break;
		case Hibernate:
			cmd = "powercfg -hibernate on && rundll32.exe PowrProf.dll,SetSuspendState";
			break;
		case Shutdown:
			cmd = "Shutdown.exe -s -t 00";
			break;
		}
		System.out.println("Executing " + action + "\r\n" + cmd);
		
		try {
			Runtime.getRuntime().exec("cmd /K" + cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void refreshDisplay(long timeLeftSecs){
		display.refresh(timeLeftSecs);
	}
}
