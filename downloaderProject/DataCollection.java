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
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Extractors.*;
import java.io.File;
import java.io.FileNotFoundException;

public class DataCollection implements Externalizable{
	private static final long serialVersionUID = 1L;
	private static final long VERSION = 2;
	private transient Vector<String> starList, /*dictionary,*/ ignoreWords;
	private Map<String, Integer> keywords, frequentStars;
	private Map<String, Integer> frequentSites;
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
            starList = DataIO.loadStarList(); Collections.sort(starList);
            //dictionary = DataIO.loadDictionary(); Collections.sort(dictionary);
            try { ignoreWords = DataIO.loadIgnoreWords(); }catch (FileNotFoundException e) {}
            ignoreWords.add("a"); ignoreWords.add("the"); ignoreWords.add("an");
            Collections.sort(ignoreWords);
	}
        
        public Vector<File> getExempt() { 
           //for clearing cache and avoiding suggested video cache
           Vector<File> list = new Vector<>();
           video[] v = new video[videoQueue.size()];
           videoQueue.toArray(v);
            for(video l:v) {
                list.add(l.getThumbnail());
                for(int i = 0; i < l.getPreviewCount(); i++)
                    list.add(l.getPreview(i));
            }
            return list;
        }
	
	public video next() {
            return videoQueue.poll();
	}
	
	public int suggestions() {
            return videoQueue.size();
	}
	
	public void addSuggestion(video v) {
            if (v != null)
            	if (!videoQueue.contains(v)) videoQueue.add(v);
	}
	
	public void add(String mediaName, String site) throws GenericDownloaderException {
            //if (keywords == null) init();
            Vector<String> words = new Vector<>(); loadLibs();
            words = searchStars(mediaName.split(" ")); addSite(site);
            //pull out articles, pronouns, conjuctions and prepositions
            //and ensure the string is actually a word (just in case some video name is ujljkhvjh ljh lj)
            //i dare ya to search that, it was actually the name of one but thats not a helpful search keyword
            words = parse(words);
            //add remaining nouns, adjectives, adverbs, verbs and interjections as adjectives
            for(String s:words) addKeyword(s);
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
            for(int i = 0; i < words.length; i++)
                if (words[i] != null)
                pure.add(words[i]);
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
	
	private void addStar(String name) {
            //if (frequentStars.containsKey(name))
            if (frequentStars.containsKey(name))
                frequentStars.put(name, frequentStars.get(name) + 1);
            else
		frequentStars.put(name, 1);
	}
	
	private void addKeyword(String word) {
            if (keywords.containsKey(word))
                keywords.put(word, keywords.get(word) + 1);
            else
                keywords.put(word, 1);
	}
	
	private void addSite(String site) {
            if (frequentSites.containsKey(site))
            	frequentSites.put(site, frequentSites.get(site) + 1);
            else
		frequentSites.put(site, 1);
	}
	
	private void generateSuggestion() throws GenericDownloaderException {
            final Map<String, Integer> keywordChart = keywords.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            final Map<String, Integer> StarChart = frequentStars.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            Iterator<String> i = keywordChart.keySet().iterator();
            Iterator<String> j = StarChart.keySet().iterator();
            if (i.hasNext() && j.hasNext()) {
                String top = i.next();
                System.out.println("top: "+keywords.get(top)+" it: "+top);
                String topStar = j.next();
                System.out.println("topstar: "+frequentStars.get(topStar)+" it: "+topStar);
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
                System.out.println("top: "+keywords.get(top)+" it: "+top);			
                if (keywords.get(top) > 1)
                    generateSearch(keywordChart);
            } else if(j.hasNext()) {
		String topStar = j.next();
                System.out.println("topstar: "+frequentStars.get(topStar)+" it: "+topStar);
                if (frequentStars.get(topStar) > 1)
                    search(StarChart.keySet().iterator().next()); //search top star
            }
	}
	
	private void generateSearch(Map<String,Integer> kwords, Map<String,Integer> stars) throws GenericDownloaderException {
            Random randomNum = new Random();
            int max = randomNum.nextInt(8); //generate 1 - 8 words
            int starIndex = randomNum.nextInt(5); //generate from top 5 stars
		
            int count = 0; Iterator<String> i = kwords.keySet().iterator();
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
            int max = randomNum.nextInt(8); //generate 0 - 7
            System.out.println("key size: "+kwords.keySet().size());
            int count = 0; Iterator<String> i = kwords.keySet().iterator();
            StringBuilder words = new StringBuilder();
            while(i.hasNext()) {
                if (count >= max + 1) break;
                words.append(i.next()+" ");
                count++;
            }
            search(words.toString().trim()); //search with random number of available keywords
        }
        
	private void search(String searchStr) throws GenericDownloaderException {
            final Map<String, Integer> siteChart = frequentSites.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            GenericExtractor x = getExtractor(siteChart.keySet().iterator().next());
            System.out.println("search: "+searchStr);
            try {if (x != null) addSuggestion(x.search(searchStr));}catch(IOException e) {} catch(UnsupportedOperationException e){}
	}
        
        private GenericExtractor getExtractor(String type) {
            if (type.equals("Spankbang")) return new Spankbang();
            if (type.equals("Pornhub"))return new Pornhub();
            if (type.equals("Xhamster")) return new Xhamster(); 
            if (type.equals("Xvideos")) return new Xvideos();
            if (type.equals("Xnxx")) return new Xvideos(); //xnxx shares the same setup so they use the extractor
            if (type.equals("Youporn")) return new Youporn();
            if (type.equals("Redtube")) return new Redtube();
            if (type.equals("Thumbzilla")) return new Thumbzilla();
            if (type.equals("Shesfreaky")) return new Shesfreaky();
            if (type.equals("Yourporn")) return new Yourporn();
            if (type.equals("Bigtits")) return new Bigtits();
            if (type.equals("Pornhd")) return new Pornhd(); //not implemented //complex
            if (type.equals("Vporn")) return new Vporn();
            if (type.equals("Ghettotube")) return new Ghettotube();
            if (type.equals("Tube8")) return new Tube8();
            if (type.equals("Youjizz")) return new Youjizz();
            if (type.equals("Xtube")) return new Xtube(); 
            if (type.equals("Spankwire")) return new Spankwire();
            if (type.equals("Justporno")) return new Justporno();
            if (type.equals("Bigbootytube")) return new Bigbootytube();
            if (type.equals("Befuck")) return new Befuck();
            if (type.equals("Dailymotion")) return new Dailymotion(); //not implemented
            if (type.equals("Vimeo")) return new Vimeo(); //not implemented
            if (type.equals("Cumlouder")) return new Cumlouder();
            if (type.equals("Ruleporn")) return new Ruleporn();
            if (type.equals("Homemoviestube")) return new Homemoviestube();
            if (type.equals("Anysex")) return new Anysex();
            if (type.equals("Porn")) return new Porn();
            if (type.equals("Pornheed")) return new Pornheed(); //not implemented
            if (type.equals("Drtuber")) return new Drtuber(); //not implemented
            return null;
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
                try {videoQueue = (LinkedList<video>)in.readObject(); }catch (IOException e){ videoQueue = new LinkedList<video>();}
                frequentSites = (HashMap<String, Integer>)in.readObject();
            } else if (id == 2) {
                keywords = (HashMap<String, Integer>)in.readObject();
                frequentStars = (HashMap<String, Integer>)in.readObject();
                try {videoQueue = (LinkedList<video>)in.readObject(); }catch (IOException e){ videoQueue = new LinkedList<video>();}
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
