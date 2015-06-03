package sk.dzahoransky.winsleeper;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class SleepTimer {
	
	public static final long MinMilis =  TimeUnit.MINUTES.toMillis(1);
	public static enum Action{Sleep, Hibernate, Shutdown};

	private Timer timerThread = new Timer("Shutdown timer", false);
	
	private Ticker ticker;
	private Action action;
	
	private IDisplay display;
	
	public SleepTimer(IDisplay display) {
		this.display = display;
	}
	
	public void stop() {
		stopAndclearTasks();
		System.out.println("Tasks canceled");
	}

	public void start(long timeInMilis, Action action) {
		stopAndclearTasks();
		
		ticker = new Ticker(timeInMilis);
		
		timerThread.scheduleAtFixedRate(ticker, 0, MinMilis);
		this.action = action;
		
		System.out.println(String.format("Tasks scheduled (action=%s, time=%smin)", action, timeInMilis / MinMilis));
	}

	public void execActionCommand() {
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
	
	private class Ticker extends TimerTask{
		int tick;
		long totalTime;
		public Ticker(long totalTime) {
			this.totalTime = totalTime;
		}
		@Override
		public void run() {
			long remTime = totalTime - tick * MinMilis;
			System.out.println("Executing refresh (tick="+tick+"), remaining time " + remTime + " milis.");
			
			display.refresh(remTime);
			
			if(remTime == 0){
				cancel();
				execActionCommand();
			}
			tick++;
		}
	}
	
	private void stopAndclearTasks(){
		if(ticker != null){
			ticker.cancel();
			ticker = null;
		}
		timerThread.purge();
	}
}
