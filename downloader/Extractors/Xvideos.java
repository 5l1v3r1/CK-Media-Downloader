/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Xvideos extends GenericQueryExtractor{
    
    public Xvideos() { //this contructor is used for when you jus want to query
        
    }
    
    public Xvideos(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Xvideos(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Xvideos(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
        verify(page);
        
        int use = 5;
        for(int i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        
        super.downloadVideo(stats.get(2),stats.get(0),s);
    }
    
    private static void verify(Document page) throws VideoDeletedException {
        if(Jsoup.parse(page.select("title").toString()).text().equals("Video deleted - XVIDEOS.COM"))
            throw new VideoDeletedException();
    }

    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.xvideos.com/?k="+search;
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("div.mozaique").select("div.thumb-block");
	for(int i = 0; i < searchResults.size(); i++)  {
            //if has /models/ it is link to model page
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/models/")) continue;
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/pornstars/")) continue;
            //if  has /channels/ it is a link to channel page
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/channels/")) continue;
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/verified/videos")) continue;
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/pornstar-channels/")) continue;
            if (!CommonUtils.testPage("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href"))) continue; //test to avoid error 404
            try {verify(page);} catch (GenericDownloaderException e) { continue;}
            String thumbLink = searchResults.get(i).select("div.thumb").select("a").select("img").attr("data-src");
            if (thumbLink.contains("THUMBNUM")) continue;
            thequery.addLink("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href"));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,2))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,2),MainApp.imageCache) != -2)
                    throw new IOException("Error downloading file");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,2)));
            thequery.addPreview(parse("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href")));
            thequery.addName(downloadVideoName("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href")));
	}
        return thequery;
    }
    
    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
       Document page = getPage(url,false);
        int use = 5;
        for(int i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(stats.get(5),1))) //if file not already in cache download it
            CommonUtils.saveFile(stats.get(5), CommonUtils.getThumbName(stats.get(5),1),MainApp.imageCache);
        
        File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(stats.get(5),1));
        return CommonUtils.splitImage(grid, 5, 6, 25, 50);
    }
    
    private static String grab(String s, int fromWhere) {
	StringBuilder pure = new StringBuilder();
            for(int i = fromWhere; i < s.length(); i++) {
		if (s.charAt(i) == '\'')
                    break;
            pure.append(s.charAt(i));
	}
	return pure.toString();
    }
    
    private static Vector<String> getStats(String raw) {
        Vector<String> stats = new Vector<>();
        
        stats.add(grab(raw,raw.indexOf("setVideoTitle")+15));
	stats.add(grab(raw,raw.indexOf("setVideoUrlLow")+16));
	stats.add(grab(raw,raw.indexOf("setVideoUrlHigh")+17));
	stats.add(grab(raw,raw.indexOf("setThumbUrl")+13));
	stats.add(grab(raw,raw.indexOf("setThumbUrl169")+16));
	stats.add(grab(raw,raw.indexOf("setThumbSlide")+15));
	stats.add(grab(raw,raw.indexOf("setThumbSlideBig")+18));
        
	return stats;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        
        verify(page);
        
        int use = 5;
        for(int i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());

	return stats.get(0);
    } 

    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, HttpStatusException, Exception{
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
        else {
            String html;
            try {
                html = Jsoup.connect(url).get().html();
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404)
                    throw new PageNotFoundException("Video may be deleted as it is");
                else throw e;
            }
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
        } //if not found in cache download it
        
        verify(page);
	int use = 5;
        for(int i = 0; i < page.select("script").size(); i++) {
            if (page.select("script").get(i).toString().contains("var html5player = new HTML5Player('html5video',")) {
                use = i; break;
            }
        }
        Vector<String> stats = getStats(page.select("script").get(use).toString());
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(stats.get(4),2))) //if file not already in cache download it
            CommonUtils.saveFile(stats.get(4),CommonUtils.getThumbName(stats.get(4),2),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(stats.get(4),2));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Xvideos";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.xvideos.com/?k="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("div.mozaique").select("div.thumb-block");
	for(int i = 0; i < searchResults.size(); i++)  {
            //if has /models/ it is link to model page
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/models/")) continue;
            //if  has /channels/ it is a link to channel page
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/channels/")) continue;
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/verified/videos")) continue;
            if (searchResults.get(i).select("div.thumb").select("a").attr("href").contains("/pornstar-channels/")) continue;
            if (!CommonUtils.testPage("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href"))) continue; //test to avoid error 404
            try {verify(page);} catch (GenericDownloaderException e) { continue;}
            String thumbLink = searchResults.get(i).select("div.thumb").select("a").select("img").attr("data-src");
            if (thumbLink.contains("THUMBNUM")) continue;
            
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,2))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,2),MainApp.imageCache) != -2)
                    throw new IOException("Error downloading file");
            try {
                v = new video("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href"),downloadVideoName("https://xvideos.com"+searchResults.get(i).select("div.thumb").select("a").attr("href")),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,2)));
            } catch(Exception e) { v = null; continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
}
