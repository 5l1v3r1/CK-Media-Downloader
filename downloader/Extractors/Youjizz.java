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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Youjizz extends GenericExtractor implements Searchable{
    private static final int SKIP = 3;
	
    public Youjizz() { //this contructor is used for when you jus want to search
        
    }
    
    public Youjizz(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Youjizz(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Youjizz(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private static Map<String, String> getQualities(String src, String s) {
        int happen, from = 0;
        
        Map<String, String> qualities = new HashMap<>();
        while((happen = src.indexOf(s,from)) != -1) {
            if(!qualities.containsKey(CommonUtils.getLink(src,happen+10,'\"')))
                qualities.put(CommonUtils.getLink(src,happen+10,'\"'), "https:"+CommonUtils.eraseChar(CommonUtils.getLink(src,src.indexOf("filename",happen)+11,'\"'), '\\'));
            from = happen +1;
        }
        return qualities;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        Map<String, String> quality = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("var encodings")),"quality");
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);

	return Jsoup.parse(page.select("title").toString()).text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);
        
        String thumb = "https://"+CommonUtils.getLink(page.toString(),page.toString().indexOf("posterImage: '//") + 16, '\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("related").select("div.video-thumb");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).select("a").get(0).attr("href"),"www.youjizz.com");
            String thumb = li.get(i).select("a").get(0).select("img").attr("src");
            if (thumb.length() < 1) thumb = li.get(i).select("a").get(0).select("img").attr("data-original");
            thumb = "https:" + thumb;
            String title = li.get(i).select("div.video-title").select("a").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    continue;//throw new IOException("Failed to completely download page");
            try {v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link)); } catch (GenericDownloaderException | IOException e) {}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException{
    	str = str.trim(); str = str.replaceAll(" ", "-");
    	String searchUrl = "https://www.youjizz.com/search/"+str+"-1.html?";
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("div.video-thumb"); int count = li.size(); Random rand = new Random();
        
        while(count-- > 0) {
            int i = rand.nextInt(li.size());
            String thumbLink = "https:"+li.get(i).select("img").get(0).attr("src"); 
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = addHost(li.get(i).select("a").get(0).attr("href"),"www.youjizz.com");
            String name = li.get(i).select("div.video-title").select("a").text();
            if (link.isEmpty() || name.isEmpty()) continue;
            try {v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link)); } catch(GenericDownloaderException | IOException e) {}
            break;
        }
        return v;
    }

    public long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String, String> quality = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("var encodings")),"quality");
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        return getSize(media);
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?youjizz[.]com/videos/(?<id>[\\S]+)[.]html"; 
    }
}
