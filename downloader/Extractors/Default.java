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
import downloader.Exceptions.NotSupportedException;
import downloader.Exceptions.PageNotFoundException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Default extends GenericExtractor {
    private static final byte SKIP = 1;
    
    public Default() {
        
    }

    public Default(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Default(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Default(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        CommonUtils.log("Getting video with default extractor", this);
        MediaDefinition media = new MediaDefinition();
        try {
            Map<String,String> v = getDefaultVideo(page);
            if (v == null || v.isEmpty()) 
                throw new PageNotFoundException("video not found");
            else if (v.containsKey("single")) {
                if (v.get("single") == null || v.get("single").matches("https?://"))
                    throw new PageNotFoundException("video not found");
            }
            media.addThread(v,videoName);
        } catch (NullPointerException e) {
            throw new PageNotFoundException("video not found");
        }

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        try {
            Document page = getPage(url,false);
            String title = getH1Title(page);
            return (title == null) || (title.length() < 1) ? getTitle(page) : page.select("title").text();
        } catch (UnknownHostException e) {
           throw new NotSupportedException("Couldnt determine video name: ",url); 
        }
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        try {
            Document page = getPage(url,false);
            verify(page);
            String thumbLink = getVideoPoster(page);
            thumbLink = thumbLink == null || thumbLink.isEmpty() ? getMetaImage(page) : thumbLink;
            thumbLink = thumbLink.startsWith("//") ? "http:" + thumbLink : thumbLink;
            thumbLink = !thumbLink.startsWith("http") && thumbLink.startsWith("/") ? "http://" +getDomain(url) + thumbLink : thumbLink;
            thumbLink = !thumbLink.startsWith("http") && !thumbLink.startsWith("/") ? "http://" +getDomain(url) + "/" + thumbLink : thumbLink;

            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
        } catch (UnknownHostException | NullPointerException e) {
           throw new NotSupportedException("Couldnt determine thumb: ",url); 
        }
    }
    
    private static void verify(Document page) throws IOException, GenericDownloaderException {
        CommonUtils.log("Checking for a video with default extractor", "Default");
        if (page.select("video").isEmpty())
            throw new PageNotFoundException("Could not find a video");
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(url,false));
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?<id>[\\S]+)";
    }
}
