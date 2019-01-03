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
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Xtube extends GenericExtractor{
    
    public Xtube() { //this contructor is used for when you jus want to search
        
    }
    
    public Xtube(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Xtube(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Xtube(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    private Map<String,String> getQualities(String src) {
        String[] pair = CommonUtils.getBracket(src,src.indexOf("\"sources\":")).split(",");
        
        Map<String, String> qualities = new HashMap<>();
        for(int i = 0; i < pair.length; i++) {
            String[] temp = pair[i].split("\"");
            qualities.put(temp[1], CommonUtils.eraseChar(temp[3], '\\'));
        }
        return qualities;
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        
	String title = Jsoup.parse(page.select("h1").get(0).toString()).body().text();
	Map<String,String> quality = getQualities(page.toString());
        
        //CommonUtils.showQualityDialog(quality);
        
        String video = null;
        if (quality.containsKey("720"))
            video = quality.get("720");
        else if(quality.containsKey("480"))
            video = quality.get("480");
        else if (quality.containsKey("360"))
            video = quality.get("360");
        else if (quality.containsKey("240"))
            video = quality.get("240");
        else video = quality.get((String)quality.values().toArray()[0]);
        
        super.downloadVideo(video,title,s);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
	return Jsoup.parse(page.select("h1").get(0).toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(),page.toString().indexOf("poster",page.toString().indexOf("mediaDefintion")) + 9,'\"'), '\\');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,4))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,4),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,4));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Xtube";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException{
    	str = str.trim(); str = str.replaceAll(" ", "-");
    	String searchUrl = "https://www.xtube.com/search/video/"+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("li.deleteListElement.col-xs-24.col-s-12.col-xl-6.col10-xxl-2");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("img").get(0).attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,6))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,6),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
        	String link = "http://www.xtube.com" + li.get(i).select("a").get(0).attr("href");
        	String name = li.get(i).select("h3").text();
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,6)));
        	break;
        }
        
        return v;
    }
}
