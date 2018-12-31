/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.video;
import static downloader.Extractors.GenericExtractor.configureUrl;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Bigbootytube extends GenericQueryExtractor{
    
    public Bigbootytube() { //this contructor is used for when you jus want to query
        
    }
    
    public Bigbootytube(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Bigbootytube(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Bigbootytube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
        String video = CommonUtils.getLink(page.toString(),page.toString().indexOf("video_url:")+12,'\'');
        String title = Jsoup.parse(page.select("h1").toString()).body().text();
        
        super.downloadVideo(video,title,s);
    }
    
    int stop;
    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        
        search = search.replaceAll(" ", "+");
        String searchUrl = "http://www.bigbootytube.xxx/search/?q="+search;
        
        GenericQuery thequery = new GenericQuery();
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("div.list-thumbs").get(0).select("li").select("div.thumb").select("div.thumb-content").select("a.thumb-img");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) {continue;}
            thequery.addLink(searchResults.get(i).attr("href"));
            thequery.addThumbnail(downloadThumb(thequery.getLink(i)));
            String thumbBase = CommonUtils.getLink(searchResults.get(i).toString(), searchResults.get(i).toString().indexOf("onmouseover=\"KT_rotationStart(this, \'")+37, '\'');
            stop = Integer.parseInt(CommonUtils.getLink(searchResults.get(i).toString(), searchResults.get(i).toString().indexOf("onmouseover=\"KT_rotationStart(this, \'")+37+thumbBase.length()+3,')'));
            thequery.addPreview(parse(thumbBase));
            System.out.println("thumbbase: "+thumbBase);
            thequery.addName(downloadVideoName(thequery.getLink(i)));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException{ 
        Vector<File> thumbs = new Vector<>();
        
        for(int i = 1; i <= stop; i++) {
            String thumbLink = url+String.valueOf(i)+".jpg";
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,4))) //if file not already in cache download it
                CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,4),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,4)));
        }
        
        return thumbs;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);
	
	return Jsoup.parse(page.select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        String thumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("preview_url:")+14,'\'');
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,3))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,3),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,3));
    }

    @Override
    protected void setExtractorName() {
        extractorName = "Bigbootytube";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "http://www.bigbootytube.xxx/search/?q="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("div.list-thumbs").get(0).select("li").select("div.thumb").select("div.thumb-content").select("a.thumb-img");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) {continue;}
            try {
                v = new video(searchResults.get(i).attr("href"),downloadVideoName(searchResults.get(i).attr("href")),downloadThumb(searchResults.get(i).attr("href")));
            } catch(Exception e) { v = null; continue;}
            break; //if u made it this far u already have a vaild video
        }
        return v;        
    }
}
