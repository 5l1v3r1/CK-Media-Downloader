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
public class Vodlocker extends GenericExtractor{
    
    public Vodlocker(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Vodlocker(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Vodlocker(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        if (s != null) s.startTiming();
        Document page =  Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        
        String video = page.select("video").select("source").attr("src");
        String title = downloadVideoName(url);
        System.out.println(video);
        super.downloadVideo(video,title,s);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        //<meta property="og:title" content="Incredibles 2" />
        
        Elements metas = page.select("meta"); String name = null;
        for(int i = 0; i < metas.size(); i++) {
            if (metas.get(i).attr("property").equals("og:title")) {
                name = metas.get(i).attr("content");
                break;
            }
        }
        
	return name;
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        
        String thumb = null;
        Elements metas = page.select("meta");
        for(int i = 0; i < metas.size(); i++) {
            if(metas.get(i).attr("property").equals("og:image"))
                thumb = metas.get(i).attr("content");
        }
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb));
    }

    @Override
    protected void setExtractorName() {
        extractorName = "Vodlocker";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
