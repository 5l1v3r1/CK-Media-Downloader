/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Youjizz extends GenericExtractor{
    public Youjizz() { //this contructor is used for when you jus want to search
        
    }
    
    public Youjizz(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Youjizz(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Youjizz(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private Map<String, String> getQualities(String src, String s) {
        int happen, from = 0;
        
        Map<String, String> qualities = new HashMap<>();
        while((happen = src.indexOf(s,from)) != -1) {
            if(!qualities.containsKey(CommonUtils.getLink(src,happen+10,'\"')))
                qualities.put(CommonUtils.getLink(src,happen+10,'\"'), "https:"+CommonUtils.eraseChar(CommonUtils.getLink(src,src.indexOf("filename",happen)+11,'\"'), '\\'));
            from = happen +1;
        }
        return qualities;
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
        String title = Jsoup.parse(page.select("title").toString()).text();
        Map<String, String> quality = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("var encodings")),"quality");
        String video;
        
        if(quality.containsKey("480"))
            video = quality.get("480");
        else if(quality.containsKey("720"))
            video = quality.get("720");
        else if(quality.containsKey("1080"))
            video = quality.get("1080");
        else if(quality.containsKey("360"))
            video = quality.get("360");
        else if(quality.containsKey("288"))
            video = quality.get("288");
        else video = quality.get((String)quality.keySet().toArray()[0]);
        
        super.downloadVideo(video, title, s);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
         Document page = getPage(url,false);

	return Jsoup.parse(page.select("title").toString()).text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);
        
        String thumb = "https://"+CommonUtils.getLink(page.toString(),page.toString().indexOf("posterImage: '//") + 16, '\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Youjizz";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
