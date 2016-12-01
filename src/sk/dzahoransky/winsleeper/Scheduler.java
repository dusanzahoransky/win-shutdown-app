package sk.dzahoransky.winsleeper;
import java.io.IOException;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import sk.dzahoransky.winsleeper.SleepUI.Action;

public class Scheduler {
	
	private IDisplay display;
	
	private ScheduledExecutorService executor;
	private Delayed shutdownTimer;
	
	public Scheduler(IDisplay display) {
		this.display = display;
	}
	
	public void stop() {
		if(executor!=null) executor.shutdownNow();
		System.out.println("Tasks canceled");
	}

	public void start(int countdownMins, IActionProvider actionProvider) {
		
		stop();	//better to stop any trigger shutdown, Threads might not get eaten by GC
		
		executor = Executors.newScheduledThreadPool(3);
		
		//shutdown command executor
		shutdownTimer = executor.schedule(()->execActionCommand(actionProvider), countdownMins, TimeUnit.MINUTES);
		
		//display refresh every sec. last minute
		executor.scheduleAtFixedRate(()-> {
			refreshCountdown(shutdownTimer.getDelay(TimeUnit.SECONDS));
		} ,0, 1, TimeUnit.MINUTES);
		
		//display refresh every min.
		executor.scheduleAtFixedRate(()->{
			refreshCountdown(shutdownTimer.getDelay(TimeUnit.SECONDS));	
		}, TimeUnit.MINUTES.toSeconds(countdownMins-1), 1, TimeUnit.SECONDS);
		
		System.out.println(String.format("It's on, %s min left!", countdownMins));
	}

	private void execActionCommand(IActionProvider actionProvider) {
		String cmd = null;
		
		Action action = actionProvider.getAction();
		
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
		System.out.println("Executing " + action + ": " + cmd);
		
		try {
			Runtime.getRuntime().exec("cmd /K" + cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);	//can't do much with it...
		} finally {
			stop();
			display.refresh("Just sleep already!");
		}
	}
	
	/**
	 * Refresh countdown display, show seconds if less than a minute
	 * 
	 * @param timeToGo
	 */
	private void refreshCountdown(long timeToGo){
		if(timeToGo < 0) return;
		
		if(timeToGo > 60) {
			display.refresh(String.format("Time left %d %s.", TimeUnit.SECONDS.toMinutes(timeToGo), "min"));
		} else {
			display.refresh(String.format("Time left %d %s.", timeToGo, "sec"));
		}
	}
}
