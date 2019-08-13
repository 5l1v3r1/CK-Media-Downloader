package downloader.DataStructures;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class MediaDefinition implements Iterable<Map<String, MediaQuality>>{
    private String albumName;
    private final Vector<Map<String, MediaQuality>> threads; //threads of qualities
    private final Vector<String> threadName; //each quality will have the same name
    //each thread will have a unique name
    //threads occur from albums

    public MediaDefinition() {
        albumName = null;
        threads = new Vector<>();
        threadName = new Vector<>();
    }
	
    public void setAlbumName(String name) {
        albumName = name;
    }
	
    public String getAlbumName() {
        return albumName;
    }
	
    public void addThread(Map<String,MediaQuality> t, String n) {
        threads.add(t);
        threadName.add(n);
    }
    
    public String getThreadName(int i) {
        return threadName.get(i);
    }

    public boolean isSingleThread() {
        return threads.size() == 1;
    }
	
    public boolean isAlbum() {
        return albumName != null;
    }

    @Override public Iterator<Map<String, MediaQuality>> iterator() {
        Iterator<Map<String, MediaQuality>> it = new Iterator<Map<String, MediaQuality>>() {
	    private int currentIndex = 0;
	
	    @Override public boolean hasNext() {
	      	return currentIndex < threads.size() && threads.get(currentIndex) != null;
	    }
	
            @Override public Map<String, MediaQuality> next() {
	      	return threads.get(currentIndex++);
	    }
	
	    @Override public void remove() {
	      	throw new UnsupportedOperationException();
	    }
                
            public int current() {
                return currentIndex-1;
            }
        };
        return it;
    }
}
