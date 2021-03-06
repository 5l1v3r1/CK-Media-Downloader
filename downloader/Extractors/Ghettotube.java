/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Ghettotube extends GenericExtractor implements Searchable{
    private static final byte SKIP = 3;
    
    public Ghettotube() { //this contructor is used for when you jus want to search
        
    }
    
    public Ghettotube(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Ghettotube(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Ghettotube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String, MediaQuality> getQualities(String s) throws PageParseException {
        Map<String, MediaQuality> q = new HashMap<>();
        try {
            String src = s.substring(s.indexOf("sources: [{")+9,s.indexOf("}],")+2).replaceAll("file", "\"file\"").replaceAll("label", "\"label\"");
            JSONArray json = (JSONArray)new JSONParser().parse(src);
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject temp = i.next();
                q.put((String)temp.get("label"), new MediaQuality((String)temp.get("file")));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        Map<String, MediaQuality> qualities = getQualities(page.toString().substring(page.toString().indexOf("playerInstance.setup({")));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
	return getH1Title(getPage(url,false));
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Elements scripts = getPage(url,false).select("div.play").select("script");
        String thumb = CommonUtils.getLink(scripts.get(scripts.size()-1).toString(), scripts.get(scripts.size()-1).toString().indexOf("image:")+8, '\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException {
    	/*if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("related_videos").select("div.item");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
            System.out.println("trying");
        	if (count > li.size()) break; int range = li.size() > 6 ? 6 : li.size(); 
        	int i = randomNum.nextInt(range); count++;
        	String link = li.get(i).select("a.thumb-img").attr("href");
            String thumb = li.get(i).select("a.thumb-img").select("img").attr("src");
            String title = li.get(i).select("a.thumb-img").select("img").attr("alt");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)));
                break;
            }
        return v;*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "https://www.ghettotube.com/search/video/"+str.trim().replaceAll(" ", "+");
    	
        video v = null;
        Elements li = getPage(searchUrl,false).select("div.thumb-list").select("div.item");
        
        for(byte i = 0; i < li.size(); i++) {
            String thumbLink = li.get(i).select("img").attr("src"); 
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = li.get(i).select("a").attr("href");
            String name = li.get(i).select("h2").select("a").text();
            if (link.isEmpty() || name.isEmpty()) continue;
            Document linkPage =  Jsoup.parse(Jsoup.connect(link).userAgent(CommonUtils.PCCLIENT).get().html());
            Elements scripts = linkPage.select("div.play").select("script");
            String video = CommonUtils.getLink(scripts.get(scripts.size()-1).toString(), scripts.get(scripts.size()-1).toString().indexOf("file:")+7, '\"');
            v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),CommonUtils.getContentSize(video), getDuration(link).toString());
            break;
        }
        return v;
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        Elements spans = getPage(link, false).select("div.ubox-addedby").select("span");
        String t = "00";
        for(byte i = 0; i < spans.size(); i++) {
            if (spans.get(i).text().contains(":")) {
                t = spans.get(i).text();
                break;
            }
        }
        long secs = getSeconds(t);
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException{
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("a.watch").forEach(s -> words.add(s.text()));
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?ghettotube[.]com/video/[\\S]+-(?<id>\\S+)[.]html";
        //https://www.ghettotube.com/video/brunette-lady-selena-castro-with-big-bosoms-rubbing-and-banging-shaved-pussy-by-bbc-NzEr159WfhW.html
    }
}
