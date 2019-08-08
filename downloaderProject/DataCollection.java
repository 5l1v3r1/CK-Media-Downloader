package downloaderProject;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;
import ChrisPackage.Star;
import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Extractors.Searchable;
import downloader.Extractors.GenericExtractor;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DataCollection implements Externalizable {
    private static final long serialVersionUID = 1L, VERSION = 2;
    private transient Vector<String> starList, /*dictionary,*/ ignoreWords;
    private Map<String, Integer> keywords, frequentStars, frequentSites;
    private Queue<video> videoQueue; 
	
    public DataCollection() {
        loadLibs();
    }
	
    public DataCollection(boolean yes) {
        this();
        init();
    }
        
    private void init() {
        if (keywords == null) keywords = new HashMap<>();
        if (frequentStars == null) frequentStars = new HashMap<>();		
        if (videoQueue == null) videoQueue = new LinkedList<>();
        if (frequentSites == null) frequentSites = new HashMap<>();
    }
	
    private void loadLibs() {
        if (starList == null) {
            starList = DataIO.loadStarList(); 
            Collections.sort(starList, String.CASE_INSENSITIVE_ORDER);
        }
        //dictionary = DataIO.loadDictionary(); Collections.sort(dictionary);
        try { 
            if (ignoreWords == null) {
                ignoreWords = DataIO.loadIgnoreWords(); 
                ignoreWords.add("a"); ignoreWords.add("the"); ignoreWords.add("an");
                Collections.sort(ignoreWords, String.CASE_INSENSITIVE_ORDER);
            }
        }catch (FileNotFoundException e) {}
    }
        
    public Vector<File> getExempt() { 
        //for clearing cache and avoiding suggested video cache
        Vector<File> list = new Vector<>();
        video[] v = new video[videoQueue.size()];
        videoQueue.toArray(v);
        for(video l: v) {
            if (l == null) continue;
            list.addAll(l.getDependencies());
        }
        return list;
    }
    
    public boolean hasNext() {
        return !videoQueue.isEmpty();
    }
	
    public video next() {
        return videoQueue.poll();
    }

    public int suggestions() {
        return videoQueue.size();
    }
	
    public byte addSuggestion(video v) {
        if (v == null)
            return 1;
        else if (!videoQueue.contains(v)) {
            videoQueue.add(v);
            return 0;
        } else 
            return 2;
    }
	
    public void add(String mediaName, String site) throws GenericDownloaderException {
        //if (keywords == null) init();
        loadLibs();
        Vector<String> words = searchStars(mediaName.split(" ")); addSite(site);
        //pull out articles, pronouns, conjuctions and prepositions
        //and ensure the string is actually a word (just in case some video name is ujljkhvjh ljh lj)
        //i dare ya to search that, it was actually the name of one but thats not a helpful search keyword
        words = parse(words); //pull out ignore words 
        //add remaining nouns, adjectives, adverbs, verbs and interjections as adjectives
        for(String s:words) addKeyword(s);
        generateSuggestion();
    }
    
    public void add(Vector<String> metaData) throws GenericDownloaderException {
        if (metaData != null && !metaData.isEmpty()) {
            loadLibs();
            metaData = parse(metaData);
            for(String s:metaData) addKeyword(s);
        }
        generateSuggestion();
    }
	
    private Vector<String> searchStars(String[] words) {
        Vector<String> pure = new Vector<>();
        //search for 3 word star name
        if (words.length > 2) {
            for(int i = 0; i < words.length - 2; i++) {
                if((words[i] != null) && (words[i+1] != null) && (words[i+2] != null)) {
                    if (parseStar(words[i]+ " " + words[i+1] + " " + words[i+2])) {
                        words[i] = null; words[i+1] = null; words[i+2] = null;
                    }
                }
            }
        }
        //search for 2 word star name
        if (words.length > 1) {
            for(int i = 0; i < words.length - 1; i++) {
                if((words[i] != null) && (words[i+1] != null)) {
                    if (parseStar(words[i]+ " " + words[i+1])) {
                        words[i] = null; words[i+1] = null;
                    }
                }
            }
        }
        //search for 1 word star name
        if (words.length > 0) {
            for(int i = 0; i < words.length; i++)
                if(words[i] != null)
                    if (parseStar(words[i]))
                        words[i] = null;
        }
        pure.ensureCapacity(words.length);
        for(String s: words)
            if (s != null)
                pure.add(s);
        return pure;
    }
	
    private boolean parseStar(String name) {
        //if known star add to frequency
        if (Collections.binarySearch(starList, name, String.CASE_INSENSITIVE_ORDER) > -1) {
            addStar(name);
            return true;
        } else return false;
    }
	
    private Vector<String> parse(Vector<String> words) {
        Vector<String> pure = new Vector<>();
        for(String s:words) {
            if (Collections.binarySearch(ignoreWords, s, String.CASE_INSENSITIVE_ORDER) < 0)
            //if (Collections.binarySearch(dictionary, s, String.CASE_INSENSITIVE_ORDER) < 0)
                if (isValid(s))
                    pure.add(s);
        }
        return pure;
    }
	
    public void addStar(String name) {
        if (name != null && !name.isEmpty()) {
            if (frequentStars.containsKey(name))
                frequentStars.put(name, frequentStars.get(name) + 1);
            else
                frequentStars.put(name, 1);
        }
    }
	
    private void addKeyword(String word) {
        if (word.matches(".+\\d+_n.+")) return;
        if (word.matches("\\d+")) return;
        word = CommonUtils.filter(word);
        if (keywords.containsKey(word))
            keywords.put(word, keywords.get(word) + 1);
        else
            keywords.put(word, 1);
    }
	
    private void addSite(String site) {
        if (site != null && !site.isEmpty()) {
            if (frequentSites.containsKey(site))
                frequentSites.put(site, frequentSites.get(site) + 1);
            else
                frequentSites.put(site, 1);
        }
    }
	
    public void generateSuggestion() throws GenericDownloaderException {
        final Map<String, Integer> keywordChart = keywords.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        final Map<String, Integer> StarChart = frequentStars.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Iterator<String> i = keywordChart.keySet().iterator();
        Iterator<String> j = StarChart.keySet().iterator();
        if (!i.hasNext() && !j.hasNext()) {
            CommonUtils.log("No data captured",this);
            return;
        }
        if (i.hasNext() && j.hasNext()) {
            String top = i.next();
            CommonUtils.log("top: "+keywords.get(top)+" it: "+top,this);
            String topStar = j.next();
            CommonUtils.log("topstar: "+frequentStars.get(topStar)+" it: "+topStar,this);
            if ((keywords.get(top) > 1) && (frequentStars.get(topStar) > 1)) {
                if (new Random().nextInt(2) == 0)
                    generateSearch(keywordChart,StarChart);
                else generateSearch(keywordChart); //dont always search with star because u can
            } else if (frequentStars.get(topStar) > 1)
                search(StarChart.keySet().iterator().next()); //search top star
            else if (keywords.get(top) > 1) {
                generateSearch(keywordChart);
            }
        } else if (i.hasNext()) {
            String top = i.next(); 
            CommonUtils.log("top: "+keywords.get(top)+" it: "+top,this);			
            if (keywords.get(top) > 1)
                generateSearch(keywordChart);
        } else if(j.hasNext()) {
            String topStar = j.next();
            CommonUtils.log("topstar: "+frequentStars.get(topStar)+" it: "+topStar,this);
            if (frequentStars.get(topStar) > 1)
                search(StarChart.keySet().iterator().next()); //search top star
        }
    }
	
    private void generateSearch(Map<String,Integer> kwords, Map<String,Integer> stars) throws GenericDownloaderException {
        Random randomNum = new Random();
        int max = randomNum.nextInt(4); //generate 1 - 4 words
        int starIndex = stars.keySet().size() > 3 ? randomNum.nextInt(stars.keySet().size() / 3) : 0; //generate from top stars (depending on size of list)
		
        byte count = 0; Iterator<String> i = kwords.keySet().iterator();
        StringBuilder words = new StringBuilder();
        while(i.hasNext()) {
            if (count >= max + 1) break;
            words.append(" "+i.next());
            count++;
        }
                
        Iterator<String> j = stars.keySet().iterator();
        String star = ""; count = 0;
        while(j.hasNext()) { //keep going until u find the chosen top star
            //but break if u dont have that many
            if (count >= starIndex + 1) break;
            star = j.next(); count++;
        }
        search(star+words.toString()); //search top star with random number of available keywords
    }
        
    private void generateSearch(Map<String,Integer> kwords) throws GenericDownloaderException {
        Random randomNum = new Random();
        int max = randomNum.nextInt(4) + 1; //generate 1 - 4
        CommonUtils.log("key size: "+kwords.keySet().size(),this);
        byte count = 0; Iterator<String> i = kwords.keySet().iterator();
        StringBuilder words = new StringBuilder();
        while(i.hasNext()) { //generate at least 4 words to search with
            if (count >= max + 1) break;
            words.append(i.next()+" ");
            count++;
        }
        search(words.toString().trim()); //search with random number of available keywords
    }
        
    private void search(String searchStr) throws GenericDownloaderException {
        final Map<String, Integer> siteChart = frequentSites.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Searchable x; Iterator<String> i = siteChart.keySet().iterator();
        do {
            x = getExtractor(i.next());
        }while(x == null && i.hasNext());
        if (x != null) {
            CommonUtils.log("Searching: "+x.getClass().getSimpleName(),this);
	    CommonUtils.log("search: "+searchStr,this);
            try {
                byte retry = 8, result;
                do {
                    result = addSuggestion(x.search(searchStr));
                    if (result == 2)
                        retry = retry > 3 ? 3 : retry;
                    if (result == 1) break;
                }while(result != 0 && retry-- > 0);
            }catch(IOException | UnsupportedOperationException e) {}
	}
    }
        
    private Searchable getExtractor(String type) {
        try {
            Class<?> c = Class.forName("downloader.Extractors."+type);
            Constructor<?> cons = c.getConstructor();
            GenericExtractor x = (GenericExtractor)cons.newInstance();
            return x instanceof Searchable ? (Searchable)x : null;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
           return null;
        }
        /*Pornhd //not implemented //complex
        Dailymotion //not implemented
        Vimeo //not implemented
        Pornheed //not implemented*/
    }
        
    private static boolean isValid(String str) {
        if (str == null) return false;
        else if(str.length() < 1) return false;
        else {
            int count = 0;
            for(int i = 0; i < str.length(); i++)
                if (!Character.isLetterOrDigit(str.charAt(i)))
                    count++;
            if (count == str.length()) return false; //all characters were neither alphabetic nor digits
                
            count = 0;
            for(int i = 0; i < str.length(); i++)
                if (Character.isDigit(str.charAt(i)) || !Character.isLetter(str.charAt(i))) //ik this is redundant
                    count++;
            if (count == str.length()) return false; //all characters were either numbers or non alphabet chars
        }
        return true; //you made it this far ... valid
    }
        
    private String getMap(Map<String,Integer> m) {
        StringBuilder s = new StringBuilder();
        Iterator<String> i = m.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)).keySet().iterator();
        boolean first = true;
        while(i.hasNext()) {
            String temp = i.next();
            if (!first)
                s.append(",{");
            else {
                s.append("{");
                first = false;
            }
            s.append(CommonUtils.addAttr(temp,m.get(temp))+"}");
        }
        return s.toString();
    }
        
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{"+CommonUtils.addAttr("Version",VERSION)+","+CommonUtils.addAttr("videoQueue",videoQueue.size())+","+CommonUtils.addAttr("keyword size",keywords.size())+",\"Keywords\":[");
        json.append(getMap(keywords)).append("],\"FrequentStars\":[").append(getMap(frequentStars));
        json.append("],\"FrequentSites\":[").append(getMap(frequentSites)).append("]}");
        return json.toString();
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long id = (long)in.readObject();
        if (id == 1) {
            keywords = (HashMap<String, Integer>)in.readObject();
            Map<Star,Integer> star = (HashMap<Star, Integer>)in.readObject();
            Iterator<Star> i = star.keySet().iterator();
            frequentStars = new HashMap<>();
            while(i.hasNext()) {
                Star next = i.next();
                frequentStars.put(next.getName(),star.get(next));
            }
            videoQueue = (LinkedList<video>)in.readObject();
            frequentSites = (HashMap<String, Integer>)in.readObject();
        } else if (id == 2) {
            keywords = (HashMap<String, Integer>)in.readObject();
            frequentStars = (HashMap<String, Integer>)in.readObject();
            videoQueue = (LinkedList<video>)in.readObject();
            frequentSites = (HashMap<String, Integer>)in.readObject();
        }
    }

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(VERSION);
        out.writeObject(keywords);
        out.writeObject(frequentStars);
        out.writeObject(videoQueue);
        out.writeObject(frequentSites);
    }
}
