package downloaderProject;

public class executeStopWatch {
	private long start, stop, elapsed;
	
	executeStopWatch() {
		start = stop = elapsed = 0;
	}
	
	public void start() {
		start = System.nanoTime();
	}
	
	public void stop() {
		if (start != 0) {
			stop = System.nanoTime();
			elapsed = (stop - start) / 1000000;
		}
	}
	
	public long getElapsed() {
		return elapsed;
	}
	
	public GameTime getTime() {
		GameTime t = new GameTime();
		t.addSec((int)elapsed / 1000);
		return t;
	}
}
