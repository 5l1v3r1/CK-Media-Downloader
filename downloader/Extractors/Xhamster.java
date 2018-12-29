/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Xhamster extends GenericQueryExtractor{
    
    public Xhamster() { //this contructor is used for when you jus want to query
        
    }
    
    public Xhamster(String url)throws IOException, SocketTimeoutException, UncheckedIOException , Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, boolean mobile) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, File thumb, String videoName){
        super(convertUrl(url),thumb,videoName);
    }

    @Override
    public void getVideo(OperationStream s) throws IOException,SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
 
	String video = page.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
	String name = Jsoup.parse(page.select("h1.entity-info-container__title").toString()).body().text();

        super.downloadVideo(video,name,s);
    }

    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://xhamster.com/search?q="+search;
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("div.thumb-list__item.video-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            thequery.addLink(searchResults.get(i).select("a").attr("href"));
            String thumbLink = searchResults.get(i).select("a").select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("div.video-thumb-info").select("a").toString()).body().text());
	}
        return thequery;
    }
    
     //get preview thumbnails
    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException {
        Vector<File> thumbs = new Vector<File>();
        
        Document page = getPage(url,true);
        
        Elements previewImg = page.select("div.item").select("img"); //div containing imgs tags
			
        Iterator<Element> img = previewImg.iterator();
        while(img.hasNext()) { //loop through all img tags
            String thumb = img.next().attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,5))) //if file not already in cache download it
                CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,5),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,5)));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        return Jsoup.parse(page.select("h1.entity-info-container__title").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,true);
        
	String thumb = page.select("header").select("img").attr("src");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,5))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,5),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,5));
    }
    
    private static String convertUrl(String url) {
        url = url.replace("https://m", "https://www");
        if (url.matches("[\\S]*.m.xhamster.com/videos/[\\S]*"))
            return url.replaceFirst(".m", "");
        else return url;
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Xhamster";
    }
}
