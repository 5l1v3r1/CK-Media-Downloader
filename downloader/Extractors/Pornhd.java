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
public class Pornhd extends GenericExtractor{
    private static final int SKIP = 3;
    
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
        Document page = getPage(url,false,true);

	String[] rawData = CommonUtils.getBracket(page.toString(), page.toString().indexOf("sources: {")).split("\"");
	Map<String,String> qualities = new HashMap<>(); MediaDefinition media = new MediaDefinition();
	for(int i = 0; i < rawData.length; i++) {
            if (i == 0) continue;
            qualities.put(rawData[i], "http://pornhd.com"+CommonUtils.eraseChar(rawData[i+2],'\\'));
            i+=3;
	}
        media.addThread(qualities, videoName);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
       Document page = getPage(url,false);
        
        String title = page.select("div.section-title").select("h1").toString();
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
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("ul.thumbs").select("li");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = "https://www.pornhd.com" + li.get(i).select("a.thumb.videoThumb.popTrigger").attr("href");
            String title = li.get(i).select("a.title").text();
            try {v = new video(link,title,downloadThumb(link),getSize(link)); } catch(Exception e) {continue;}
            break;
        }
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);

	String[] rawData = CommonUtils.getBracket(page.toString(), page.toString().indexOf("sources: {")).split("\"");
	Map<String,String> qualities = new HashMap<>(); MediaDefinition media = new MediaDefinition();
	for(int i = 0; i < rawData.length; i++) {
            if (i == 0) continue;
            qualities.put(rawData[i], "http://pornhd.com"+CommonUtils.eraseChar(rawData[i+2],'\\'));
            i+=3;
	}
        media.addThread(qualities, videoName); //video name is not used so doesnt matter
        return getSize(media);
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?pornhd[.]com/videos/(?<id>[\\d]+)/[\\S]+"; 
    }
}
