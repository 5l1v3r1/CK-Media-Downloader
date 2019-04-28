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
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Eporner extends GenericExtractor{
    private static final int SKIP = 6;
    
    public Eporner() { //this contructor is used for when you jus want to search
        //https://eporner.com/hd-porn/IF2uT0kcojN/Blonde-Lass-Delicate-Hands/
    }

    public Eporner(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Eporner(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Eporner(String url, File thumb, String videoName){
        super(url,thumb,videoName); 
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        Elements tr = page.getElementById("hd-porn-dload").select("table").select("tr");
        Map<String,String> qualities = new HashMap<>();
        
        for(int i = 0; i < tr.size(); i++)
            qualities.put(tr.get(i).select("td").select("strong").text().replace(":",""), "https://www.eporner.com" + tr.get(i).select("td").select("a").attr("href"));
            //qualities.put(tr.get(i).select("td").select("span").text().replace(":",""), "https://s13-n5-nl-cdn.eporner.com/142123122312/5c42773c13880" + tr.get(i).select("td").select("a").attr("href"));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        return page.getElementById("undervideomenu").select("h1").text();
    }
    
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        Document page = getPage(url,false);
        String thumb = getMetaImage(page, true);
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www.)?eporner.com/hd-porn/(?<id>[\\S]+)/[\\S]+/"; 
    }
}
