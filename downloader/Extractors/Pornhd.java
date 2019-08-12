/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Pornhd extends GenericExtractor implements Searchable{
    private static final byte SKIP = 3;
    
    public Pornhd() { //this contructor is used for when you jus want to search
        
    }
    
    public Pornhd(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Pornhd(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Pornhd(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPageCookie(url,false,true);

	String[] rawData = getId(page.toString(),"sources: \\{(?<id>.+?)\\}").split("\"");
	Map<String,String> qualities = new HashMap<>(); MediaDefinition media = new MediaDefinition();
	for(int i = 0; i < rawData.length; i++) {
            if (i == 0) continue;
            qualities.put(rawData[i], addHost(CommonUtils.eraseChar(rawData[i+2],'\\'),"pornhd.com"));
            i+=3;
	}
        media.addThread(qualities, videoName);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        String title = getPage(url,false).select("div.section-title").select("h1").toString();
	return title.substring(4,title.indexOf("<",4)-1);
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        String thumb = getMetaImage(getPage(url,false));//String thumb = page.select("video.video-js.vjs-big-play-centered").attr("poster").replace(".webp", ".jpg");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        
        Elements li = getPageCookie(url,false).select("ul.thumbs").select("li");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty(); video v = null;
        while(!got) {
            if (count > li.size()) break;
            byte i = (byte)randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).select("a.thumb.videoThumb.popTrigger").attr("href"),"pornhd.com");
            String title = li.get(i).select("a.title").text();
            try {v = new video(link,title,downloadThumb(link),getSize(link),"----"); } catch(Exception e) {continue;}
            break;
        }
        return v;
    }
    
    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.pornhd.com/search?search="+str.trim().replaceAll(" ", "%20");
        
	Elements searchResults = getPageCookie(searchUrl,false).select("a.thumb.videoThumb.popTrigger"); 
        int count = searchResults.size(); Random rand = new Random(); video v = null;

	while(count-- > 0) {
            byte i = (byte)rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).select("a.thumb").attr("href"),"pornhd.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            String thumbLink = searchResults.get(i).select("img").attr("src"); //src for pc
            if (!thumbLink.matches("https?://cdn-pics.pornhd.com/\\S+.jpg"))
                thumbLink = searchResults.get(i).select("img").attr("data-original");
            if (!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            try {
                long size = getSize(link);
                v = new video(link,downloadVideoName(link),new File(MainApp.imageCache+File.separator+CommonUtils.parseName(thumbLink,".jpg")),size, "----");
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPageCookie(link,false,true);

	String[] rawData = getId(page.toString(),"sources: \\{(?<id>.+?)\\}").split("\"");
	Map<String,String> qualities = new HashMap<>(); MediaDefinition media = new MediaDefinition();
	for(int i = 0; i < rawData.length; i++) {
            if (i == 0) continue;
            qualities.put(rawData[i], addHost(CommonUtils.eraseChar(rawData[i+2],'\\'),"pornhd.com"));
            i+=3;
	}
        media.addThread(qualities, videoName); //video name is not used so doesnt matter
        
        return getSize(media, CommonUtils.StringCookies(cookieJar));
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?pornhd[.]com/videos/(?<id>[\\d]+)(/[\\S]+)?/?";
        //https://www.pornhd.com/videos/154232/latina-ella-knox-massive1-boobs-almost-suffocate-a-bloke-hd-porn-video
    }
}
