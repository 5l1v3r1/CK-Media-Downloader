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
import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Bigtits extends GenericExtractor{
    
    public Bigtits() { //this contructor is used for when you jus want to search
        
    }
    
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
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
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

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "http://www.bigtits.com/search?q="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("ul.videos").select("li");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("div.thumb_container").select("a").select("img").attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,2))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,2),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
        	String link = "http://www.bigtits.com" + li.get(i).select("div.thumb_container").select("a").attr("href");
        	String name = li.get(i).select("div.thumb_container").select("a").select("img").attr("title");
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,2)));
        	break;
        }
        
        return v;
    }
}
