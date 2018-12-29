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
import org.jsoup.nodes.Element;

/**
 *
 * @author christopher
 */
public class Bigtits extends GenericExtractor{
    
    public Bigtits(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Bigtits(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Bigtits(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
        Element div = page.getElementById("playerCont");
	String video = CommonUtils.getLink(div.toString(), div.toString().indexOf("<source src=")+13, '\"');
        String temp = Jsoup.parse(page.select("div.vid_title").select("h1").toString()).body().text();
        String title = temp.substring(temp.lastIndexOf('>')+2);
        
        super.downloadVideo(video,title,s);
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
	String temp = Jsoup.parse(page.select("div.vid_title").select("h1").toString()).body().text();
	
	return temp.substring(temp.lastIndexOf('>')+2);
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        Element div = page.getElementById("playerCont");
        String thumb = CommonUtils.getLink(div.toString(),div.toString().indexOf("poster:")+9,'\"');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,2))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,2),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,2));
    }

    @Override
    protected void setExtractorName() {
        extractorName = "Bigtits";
    }
}
