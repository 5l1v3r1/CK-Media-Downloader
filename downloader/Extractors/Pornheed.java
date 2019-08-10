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
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import org.jsoup.UncheckedIOException;

/**
 *
 * @author christopher
 */
public class Pornheed extends GenericExtractor{
    private static final byte SKIP = 1;
    
    public Pornheed() { //this contructor is used for when you jus want to search
        
    }

    public Pornheed(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(changeHttp(configureUrl(url))),downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Pornheed(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Pornheed(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        String embed = getPage(url,false,true).getElementById("first").select("iframe").attr("src");
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(getPage(embed,false,true)),videoName);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("h1").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        String embed = getPage(url,false).getElementById("first").select("iframe").attr("src");
        
	String thumbLink = getPage(embed,false,true).select("video").attr("poster");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override public GameTime getDuration() {
        return null;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?pornheed[.]com/video/(?<id>[\\d]+)/[\\S]+";
        //http://www.pornheed.com/video/3385161/tattooed-babe-harlow-harrison-fucks-for-more-money
    }
}
