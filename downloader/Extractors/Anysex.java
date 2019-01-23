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
import java.util.HashMap;
import java.util.Map;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Anysex extends GenericExtractor{
    private static final int SKIP = 4;
    
    public Anysex() { //this contructor is used for when you jus want to search
        
    }

    public Anysex(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Anysex(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Anysex(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        Map<String,String> qualities = new HashMap<>();
        String video = getDefaultVideo(page);
	
        qualities.put("single",video);
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("h1").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        String thumbLink = getMetaImage(page);
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Anysex";
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
        String searchUrl = "https://anysex.com/search/?q="+str.replaceAll(" ", "+");
	Document page = getPage(searchUrl,false);

	Elements lis = page.select("ul.box").select("li.item"); video v = null;
	for(Element li: lis) {
            if (li.select("a").isEmpty()) continue;
            if (!CommonUtils.testPage(li.select("a").attr("href"))) continue; //test to avoid error 404
            String link = "https://anysex.com" + li.select("a").attr("href");
            String video = getDefaultVideo(getPage(link,false,true));
            try { v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(video)); } catch (Exception e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    public long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        return CommonUtils.getContentSize(getDefaultVideo(page));
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getVideo().iterator().next().get("single"));
    }
}