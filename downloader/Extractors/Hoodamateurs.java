/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
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
public class Hoodamateurs extends GenericExtractor {
    private static final byte SKIP = 5;
    
    public Hoodamateurs() { //this contructor is used for when you jus want to search
        
    }

    public Hoodamateurs(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(changeHttp(configureUrl(url))),downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Hoodamateurs(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Hoodamateurs(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(getPage(url,false,true)),videoName);

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getH1Title(getPage(url,false));
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        String thumbLink = addHost(getPage(url,false).select("video").attr("poster"), "www.hoodamateurs.com");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(url,false));
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("a").forEach(a -> {
           if (a.attr("href").matches("/tag/[\\S]+/"))
               words.add(a.text());
        });
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?hoodamateurs[.]com/(?<id>[\\d]+)(?:/[\\S]+)?/?";
        //http://www.hoodamateurs.com/17101/green-hair-goddess-sucks-huge-black-cock/
    }
}
