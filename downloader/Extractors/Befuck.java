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
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Befuck extends GenericExtractor{
    private static final int SKIP = 4;
    
    public Befuck() { //this contructor is used for when you jus want to search
        
    }
        
    public Befuck(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Befuck(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Befuck(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(page),videoName);
        
        return media;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
	
	return Jsoup.parse(page.select("div.desc").select("span").get(0).toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        String thumb = page.select("video").attr("poster");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override protected void setExtractorName() {
        extractorName = "Befuck";
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "https://befuck.com/search/"+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("figure");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("img").attr("data-src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = li.get(i).select("a").attr("href");
        	String name = li.get(i).select("figcaption").text();
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link));
        	break;
        }
        
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String,String> q = getDefaultVideo(page);
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
    
    @Override public String getId(String link) {
        Pattern p = Pattern.compile("https://(www.)?befuck.com/videos/([\\d]+)/[\\S]+");
        Matcher m = p.matcher(link);
        return m.find() ? m.group(2) : "";
    }

    @Override public String getId() {
        return getId(url);
    }
}
