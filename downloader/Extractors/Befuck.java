/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import static downloader.Extractors.GenericExtractor.configureUrl;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Befuck extends GenericExtractor{
    
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
    
    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        String video = page.select("video").select("source").attr("src");
        String title = Jsoup.parse(page.select("div.desc").select("span").get(0).toString()).body().text();
        
        super.downloadVideo(video,title,s);
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);
	
	return Jsoup.parse(page.select("div.desc").select("span").get(0).toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
       Document page = getPage(url,false);
        String thumb = page.select("video").attr("poster");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,4))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,4),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,4));
    }

    @Override
    protected void setExtractorName() {
        extractorName = "Befuck";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "https://befuck.com/search/"+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("figure");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("img").attr("data-src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,3))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,3),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = li.get(i).select("a").attr("href");
        	String name = li.get(i).select("figcaption").text();
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,3)));
        	break;
        }
        
        return v;
    }
}
