/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author christopher
 */
public class OperationStream {
    private final executeStopWatch duration;
    private final ArrayBlockingQueue<String> progress;
    private final int buffSize = 50;
            
    public OperationStream() {
        duration = new executeStopWatch();
        progress = new ArrayBlockingQueue<>(buffSize);
    }
    
    public void startTiming() {
        duration.start();
    }
    
    public GameTime endOperation() {
        duration.stop();
        return duration.getTime();
    }
    
    public String getProgress() {
        try {
            return progress.take();
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    public void addProgress(String p) {
        try {
            if(p.contains("%"))
                p = parse(p);
            progress.put(p);
        } catch (InterruptedException e) {
            
        }
    }
    
    private String parse(String p) {
        StringBuilder num = new StringBuilder();
        for(int i = 0; i < p.length(); i++) {
            if (!Character.isDigit(p.charAt(i)))
                break;
            num.append(p.charAt(i));
        }
        return num.toString();
    }
}
