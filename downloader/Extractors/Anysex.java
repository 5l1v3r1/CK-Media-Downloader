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
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Anysex extends GenericExtractor{
    private static final int skip = 4;
    
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
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
     
        Map<String,String> qualities = new HashMap<>();
        String video = page.select("video").select("source").attr("src");
	
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
        String thumbLink = null; 
	for(Element meta :page.select("meta"))
            if (meta.attr("property").equals("og:image"))
                thumbLink = meta.attr("content");
	if (thumbLink == null)
            for(Element meta :page.select("meta"))
                if (meta.attr("itemprop").equals("og:image"))
                    thumbLink = meta.attr("content");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,skip));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Anysex";
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
        String searchUrl = "https://anysex.com/search/?q="+str.replaceAll(" ", "+");
	Document page = Jsoup.parse(Jsoup.connect(searchUrl).userAgent(CommonUtils.PCCLIENT).get().html());

	Elements lis = page.select("ul.box").select("li.item"); video v = null;
	for(Element li: lis) {
            if (li.select("a").isEmpty()) continue;
            if (!CommonUtils.testPage(li.select("a").attr("href"))) continue; //test to avoid error 404
            String link = li.select("a").attr("href");
            try { v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link)); } catch (Exception e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    public long getSize(String url) throws IOException, GenericDownloaderException {
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
     
        String video = page.select("video").select("source").attr("src");
        return CommonUtils.getContentSize(video);
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getVideo().iterator().next().get("single"));
    }
}
