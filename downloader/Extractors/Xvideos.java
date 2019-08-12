/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Xvideos extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 2, SCRIPT = 5;
    
    public Xvideos() { //this contructor is used for when you jus want to query
        
    }
    
    public Xvideos(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this.url = adjustUrl(url);
        this.videoThumb = downloadThumb(configureUrl(this.url));
        this.videoName = downloadVideoName(configureUrl(this.url));
    }
    
    public Xvideos(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this.url = adjustUrl(url);
        this.videoThumb = thumb;
        this.videoName = downloadVideoName(configureUrl(this.url));
    }
    
    public Xvideos(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static String adjustUrl(String link) {
        if (link.matches("https?://(?:www[.])?myfreeblack[.]com/porn/([\\d]+)(?:/[\\S]+)?"))
            return "https://www.xvideos.com/video"+getId(link, "myfreeblack[.]com/porn/(?<id>[\\d]+)(?:/[\\S]+)")+"/s";
        else
            return link;
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        verify(page);
        
        int use = SCRIPT;
        for(byte i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        
        Map<String,String> qualities = new HashMap<>();
        qualities.put("high",stats.get(2)); qualities.put("low",stats.get(1));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
    private static void verify(Document page) throws VideoDeletedException {
        if(Jsoup.parse(page.select("title").toString()).text().equals("Video deleted - XVIDEOS.COM"))
            throw new VideoDeletedException();
    }

    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        search = search.trim().replaceAll(" ", "+"); 
        String searchUrl = "https://www.xvideos.com/?k="+search;
        GenericQuery thequery = new GenericQuery();
        
	Elements searchResults = getPage(searchUrl,false).select("div.mozaique").select("div.thumb-block");
	for(int i = 0; i < searchResults.size(); i++)  {
            String link = addHost(searchResults.get(i).select("div.thumb").select("a").attr("href"),"xvideos.com");
            if (!link.matches(getValidRegex())) continue;
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            String thumbLink = searchResults.get(i).select("div.thumb").select("a").select("img").attr("data-src");
            if (thumbLink.contains("THUMBNUM")) continue;
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Error downloading file");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)));
            thequery.addPreview(parse(link));
            thequery.addName(downloadVideoName(link));
            long size; try { size = getSize(link); } catch (GenericDownloaderException | IOException e) {size = -1;}
            thequery.addSize(size);
            thequery.addDuration(getDuration(link).toString());
	}
        return thequery;
    }
    
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
       Document page = getPage(url,false);
        int use = SCRIPT;
        for(byte i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(stats.get(SCRIPT),SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(stats.get(SCRIPT), CommonUtils.getThumbName(stats.get(SCRIPT),SKIP),MainApp.imageCache);
        
        File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(stats.get(SCRIPT),SKIP));
        return CommonUtils.splitImage(grid, 5, 6, 25, 50);
    }
    
    private static String grab(String s, int fromWhere) {
	StringBuilder pure = new StringBuilder();
            for(int i = fromWhere; i < s.length(); i++) {
		if (s.charAt(i) == '\'')
                    break;
            pure.append(s.charAt(i));
	}
	return pure.toString();
    }
    
    private static Vector<String> getStats(String raw) {
        Vector<String> stats = new Vector<>();
        
        stats.add(grab(raw,raw.indexOf("setVideoTitle")+15));
	stats.add(grab(raw,raw.indexOf("setVideoUrlLow")+16));
	stats.add(grab(raw,raw.indexOf("setVideoUrlHigh")+17));
	stats.add(grab(raw,raw.indexOf("setThumbUrl")+13));
	stats.add(grab(raw,raw.indexOf("setThumbUrl169")+16));
	stats.add(grab(raw,raw.indexOf("setThumbSlide")+15));
	stats.add(grab(raw,raw.indexOf("setThumbSlideBig")+18));
        
	return stats;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        verify(page);
        
        int use = SCRIPT;
        for(int i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());

	return stats.get(0);
    } 

    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, HttpStatusException, Exception{
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
        else {
            String html;
            try {
                html = Jsoup.connect(url).get().html();
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404)
                    throw new PageNotFoundException("Video may be deleted as it is not found");
                else throw e;
            }
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
        } //if not found in cache download it
        
        verify(page);
	int use = SCRIPT;
        for(byte i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(stats.get(4),SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(stats.get(4),CommonUtils.getThumbName(stats.get(4),SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(stats.get(4),SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException{
        if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        String array = page.toString().substring(page.toString().indexOf("var video_related")+18,page.toString().indexOf("];")+1);
        try {
            JSONArray j = (JSONArray)new JSONParser().parse(array);
            Random randomNum = new Random(); int count = 0; boolean got = j.isEmpty();
            while(!got) {
                if (count > j.size()) break;
                int i = randomNum.nextInt(j.size()); count++;
                JSONObject item = (JSONObject)j.get(i);
                String thumb = (String)item.get("i");
                String link = addHost((String)item.get("u"),"xvideos.com");
                if (!link.matches(getValidRegex())) continue;
                String title = CommonUtils.getThumbName(link).replaceAll("_", " ");
                if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                        continue;//throw new IOException("Error downloading file");
                try { v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link), getDuration(link).toString()); } catch(Exception e) {continue;}
                break;
            }
        } catch (ParseException e) {
            CommonUtils.log("parse exception: "+e.getMessage(),this);
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.xvideos.com/?k="+str.trim().replaceAll(" ", "+");
        
	Elements searchResults = getPage(searchUrl,false).select("div.mozaique").select("div.thumb-block");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
        
	while (count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            /*"/models/" || "/channels/" ||  "/verified/videos" || "/pornstar-channels/"*/
            String link = addHost(searchResults.get(i).select("div.thumb").select("a").attr("href"),"xvideos.com");
            if (!link.matches(getValidRegex())) continue;
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) { continue;}
            String thumbLink = searchResults.get(i).select("div.thumb").select("a").select("img").attr("data-src");
            if (thumbLink.contains("THUMBNUM")) continue;
            
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Error downloading file");
            try {
                v = new video(link,downloadVideoName(link),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link),getDuration(link).toString());
            } catch(Exception e) { v = null; continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        verify(page);
        
        int use = SCRIPT;
        for(byte i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        
        Map<String,String> qualities = new HashMap<>();
        qualities.put("high",stats.get(2)); qualities.put("low",stats.get(1));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(link,false));
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url,false).select("div.video-metadata.video-tags-list.ordered-label-list.cropped").select("a").forEach(a -> {
           if (a.attr("href").matches("/tags/[\\S]+"))
               words.add(a.text());
        });
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?((?:xvideos)|(?:xnxx))[.]com/video-?(?<id>[\\S]+)/[\\S]+|"
                + "https?://(?:www[.])?myfreeblack[.]com/porn/(?<id2>[\\d]+)(?:/[\\S]+)"; 
        //https://www.xvideos.com/video36203901/she_suck_it_like_she_missed_it_
        //https://www.myfreeblack.com/porn/39133526/MaseratiXXX-huge-tits-dangling-and-jiggling
    }
}
