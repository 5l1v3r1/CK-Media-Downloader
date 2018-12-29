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
public class Cumlouder extends GenericExtractor{
    
    public Cumlouder(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Cumlouder(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Cumlouder(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
        String video = "";
        if (page.select("video").select("source") != null)
            video = "https:"+page.select("video").select("source").attr("src");
        else video = "https:"+page.select("video").attr("src");
        
        super.downloadVideo(video,this.videoName,s);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
       Document page = getPage(url,false);
        
        return Jsoup.parse(page.select("div.video-top").select("h1").text()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = page.select("video").get(0).attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,4))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,4),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,4));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Cumlouder";
    }
}
