/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Vporn extends GenericExtractor{
    
    public Vporn() { //this contructor is used for when you jus want to search
        
    }
    
    public Vporn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Vporn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Vporn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }


    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
	String title = Jsoup.parse(page.select("h1").toString()).body().text();
	Elements rawQualities = page.getElementById("vporn-video-player").select("source");
	Map<String,String> qualities = new HashMap<>();
		
	for(int i = 0; i < rawQualities.size(); i++)
            qualities.put(rawQualities.get(i).attr("label"),rawQualities.get(i).attr("src"));
        
        String video = null;
        if (qualities.containsKey("720p"))
            video = qualities.get("720p");
        else if (qualities.containsKey("480p"))
            video = qualities.get("480p");
        else if (qualities.containsKey("320p"))
            video = qualities.get("320p");
        else if (qualities.containsKey("240p"))
            video = qualities.get("240p");
        
        super.downloadVideo(video,title,s);
    }
    
     private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Document page = getPage(url,false);
        
        return Jsoup.parse(page.select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Document page = getPage(url,false);
        
        String thumb = page.getElementById("vporn-video-player").attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,3))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,3),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,3));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Vporn";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "https://www.vporn.com/search?q="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        //is not a an actually li but...
        Elements li = page.select("div.thumblist.videos").select("div.video");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("img.imgvideo").attr("src");
        	String link= li.get(i).select("a").attr("href");
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,2))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,2),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String name = li.get(i).select("div.thumb-info").select("span").get(0).text();
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,2)));
        	break;
        }
        
        return v;
    }
}
