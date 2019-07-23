    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PrivateVideoException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Xtube extends GenericExtractor implements Searchable{
    private static final byte SKIP = 6;
    
    public Xtube() { //this contructor is used for when you jus want to search
        
    }
    
    public Xtube(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Xtube(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Xtube(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    private static Map<String,String> getQualities(String src) {
        String from = !src.contains("\"sources\":") ? "sources:" : "\"sources\":";
        String[] pair = CommonUtils.getBracket(src,src.indexOf(from)).split(",");
        
        Map<String, String> qualities = new HashMap<>();
        for (String p : pair) {
            String[] temp = p.split(":\"");
            qualities.put(CommonUtils.getPureDigit(temp[0]), CommonUtils.eraseChar(temp[1], '\\').replace("\"","").replace("}",""));
        }
        return qualities;
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{        
	Map<String,String> quality = getQualities(getPage(url,false,true).toString());
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        
        return media;
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if(!page.select("div.rootFakePlayer").isEmpty())
            throw new PrivateVideoException(page.select("div.rootFakePlayer").select("p.msg").select("strong").text());
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        verify(page);
	return Jsoup.parse(page.select("h1").get(0).toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        verify(page);
        String from = !page.toString().contains("\"poster\":") ? "poster:" : "\"poster\":";
        int offset = !page.toString().contains("\"poster\":") ? 8 : 10;
        
        String thumb = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(),page.toString().indexOf(from) + offset,'\"'), '\\');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        video v = null;
        Elements li = getPage(url,false).select("div.cntPanel.relatedVideosPanel").select("ul.row.smallSpace.rowSpace").select("li");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).select("a").get(0).attr("href"),"www.xtube.com");
            try {verify(getPage(link,false));} catch (GenericDownloaderException ex) {continue;}
            String title = li.get(i).select("h3").text();
                
            try {v = new video(link,title,downloadThumb(link),getSize(link), getDuration(link).toString());}catch(Exception e) {continue;}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException{
    	String searchUrl = "https://www.xtube.com/search/video/"+str.trim().replaceAll(" ", "-");
        
        Elements li = getPage(searchUrl,false).select("li.deleteListElement.col-xs-24.col-s-12.col-xl-6.col10-xxl-2");
        int count = li.size(); Random rand = new Random(); video v = null;
        
        while(count-- > 0) {
            int i = rand.nextInt(li.size());
            String link = addHost(li.get(i).select("a").get(0).attr("href"),"www.xtube.com");
            try {verify(getPage(link,false));} catch (GenericDownloaderException ex) {continue;}
            String thumbLink = li.get(i).select("img").get(0).attr("src"); 
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String name = li.get(i).select("h3").text();
            if (link.isEmpty() || name.isEmpty()) continue;
            try { v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link), getDuration(link).toString()); } catch(GenericDownloaderException | IOException e)  {}
            break;
        }
        
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
	Map<String,String> quality = getQualities(getPage(link,false,true).toString());
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        GameTime g = new GameTime();
        g.addSec(Integer.parseInt(getId(getPage(link,false).toString(),".*duration:(?<id>[\\d]+).*")));
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        Document page =  getPage(url, false);
        page.select("div.categories").select("a").forEach(a -> words.add(a.text()));
        page.select("div.tags").select("a").forEach(a -> words.add(a.text()));
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?xtube[.]com/video-watch/(?<id>[\\S]+)"; 
    }
}
