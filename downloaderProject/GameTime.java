package downloaderProject;

public class GameTime {
	private int days, hrs, mins, secs;
	
	GameTime() {
		days = hrs = mins = secs = 0;
	}
	
	GameTime(int s) {
		if (!setTime(0,0,0,s))
			setTime(0,0,0,0);
	}
	
	GameTime(int s, int m) {
		if(!setTime(0,0,m,s))
			setTime(0,0,0,0);
	}
	
	GameTime(int s, int m, int h) {
		if(!setTime(0,h,m,s))
			setTime(0,0,0,0);
	}
	
	GameTime(int s, int m, int h, int d) {
		if(!setTime(d,h,m,s))
			setTime(0,0,0,0);
	}
	
	GameTime(GameTime t) {
		setTime(t.getDay(),t.getHr(),t.getMin(),t.getSec());
	}
	
	//set functions
	private boolean setTime(int d, int h, int m, int s) {
		if ((!setSec(s)) || (!setMin(m)) || (!setHr(h))) 
			return false; //failed to set the passed time
		else {
			setDay(d);
			return true;
		}  
	}
	
	private boolean setSec(int s) {
		if (s > 59) 
			return false;
		else  
			this.secs = s;
		return true;
	}
	
	private boolean setMin(int m) {
		if (m > 59)
			return false;
		else this.mins = m;
		return true;
		
	}
	
	private boolean setHr(int h) {
		if (h > 23) 
			return false;
		else this.hrs = h;
		return true;
	}
	
	private void setDay(int d) {
		this.days = d;
	}
	
	public void addSec(int amount) {
		if (amount >= 0) {
			for (int i=0; i < amount; i++) {
				secs++;//by adding one each allows me to only have to increment the next time by 1
				//instead of +=
				if (secs > 59) {
					secs = 0;
					mins++;
					if (mins > 59) 
					{
						hrs++;
						mins = 0;
						if (hrs > 23) {
							days++;
							hrs = 0;
						}
					}
				}
			}//how many seconds to add
		} else {
			for(int i = amount; i < 1; i++) {
				secs--;
				if (secs < 0) {
					if (mins > 0) {
						mins--;
						secs = 59;
					} else {
						if (hrs > 0) {
							hrs--;
							mins = 58;
							secs = 59;
						} else {
							if (days > 0) {
								days--;
								hrs = 22;
								mins = 58;
								secs = 59;
							} else 
								secs = 0;
						}
					}
				}
			}
		}
	}

	public void addMin(int amount) {
		if (amount >= 0) {
			for(int i=0; i < amount; i++) {
				mins++;
				if (mins > 59) {
					hrs++;
					mins = 0;
					if (hrs > 23) {
						days++;
						hrs = 0;
					}
				} 
			}
		} else {
			for(int i = amount; i < 1; i++) {
				mins--;
				if (mins < 0) {
					if (hrs > 0) {
						hrs--;
						mins = 59;
					} else {
						if (days > 0) {
							days--;
							hrs = 58;
							mins = 59;
						} else 
							mins = 0;
					}
				}
			}
		}
	}

	public void addHr(int amount) {
		if (amount >= 0) {
			for(int i=0; i < amount; i++) {
				hrs++;
				if (hrs > 23) {
					days++;
					hrs = 0;
				} 
			}
		} else {
			for(int i = amount; i < 1; i++) {
				hrs--;
				if (hrs < 0) {
					if (days > 0) {
						days--;
						hrs = 23;
					} else 
						hrs = 0;
				}
			}
		}
	}
	
	public void addDay(int amount) {
		days+=amount;
		if (days < 0)
			days = 0;
	}
	
	public void addTime(GameTime t) {
		this.addSec(t.getSec());
		this.addMin(t.getMin());
		this.addHr(t.getHr());
		this.addDay(t.getDay());
	}

	
	//getters
	public int getHr() {
		return this.hrs;
	}

	public int getSec() {
		return this.secs;
	}

	public int getDay() {
		return this.days;
	}

	public int getMin() {
		return this.mins;
	}
	
	public String getTime() {
            if (days == 0) {
                if (hrs == 0) {
                    if (mins == 0) 
                        return String.format("%d second(s)",secs);
                    else return String.format("%d minute(s) %d second(s)", mins,secs);
                } else 
                    return String.format("%d hour(s) %d minute(s) %d second(s)",hrs,mins,secs);
            } else return String.format("%d day(s) %d hour(s) %d minute(s) %d second(s)", days,hrs,mins,secs);
	}
	
	@Override 
	public String toString() {
		return  String.format("%d:%d:%d:%d", days,hrs,mins,secs);
	}
	
	public boolean equal(GameTime t) {
		if ((t.getSec() == this.secs) && (t.getMin() == this.mins) && (t.getHr() == this.hrs) && (t.getDay() == this.days))
			return true;
		else return false;
	}
	
	public boolean more(GameTime t) {
		if (t.convertToMins() > this.convertToMins())
			return true;
		else return false;
	}
	
	public double convertToHrs() {
		double d,m,s,h;
		
		d = (getDay() * 24);
		m = (getMin() / 60);
		s = ((getSec()/60)/60);
		h = getHr();
		
		h+=d+m+s;
		return h;
	}

	public double convertToMins() {
		double d,m,s,h;
		
		d = ((getDay() * 24) * 60);
		s = (getSec() / 60);
		h = (getHr() * 60);
		m = getMin();
		
		m+=d+h+s;
		return m;
	}
	
}
