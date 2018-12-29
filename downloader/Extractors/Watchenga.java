/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Watchenga extends GenericExtractor{
    
    public Watchenga(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Watchenga(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Watchenga(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
        
        String title = Jsoup.parse(page.select("h1").toString()).body().text();
        String video = page.select("video").select("source").attr("src");
        
        super.downloadVideo(video, title, s);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);

	return Jsoup.parse(page.select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = page.select("video").attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,1))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,1),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,1));
    }  
    
    @Override
    protected void setExtractorName() {
        extractorName = "Watchenga";
    }
}
