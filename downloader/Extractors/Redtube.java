/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.video;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Redtube extends GenericQueryExtractor{
    public Redtube() { //this contructor is used for when you jus want to query
        
    }
    
    public Redtube(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Redtube(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Redtube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override
    public void getVideo(OperationStream s) throws IOException,SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
        else {
            String html = Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html();
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
        } //if not found in cache download it
		
	String title = page.select("meta").get(6).attr("content");
	int mediaIndex = page.toString().indexOf("mediaDefinition:");
		
	String defaultVideo = CommonUtils.getBracket(page.toString(),mediaIndex);
	String videoLink = CommonUtils.eraseChar(CommonUtils.getLink(defaultVideo,defaultVideo.indexOf("videoUrl")+11,'"'),'\\');
        
        super.downloadVideo(videoLink,title,s);
    }
    
    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.redtube.com/?search=/"+search+"/";
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
		
	Elements searchResults = page.getElementById("search_results_block").select("li.videoblock_list");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage("https://www.redtube.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String link = "https://www.redtube.com"+searchResults.get(i).select("a").attr("href");
            String title = Jsoup.parse(searchResults.get(i).select("div.video_title").toString()).body().text();
            String thumb = searchResults.get(i).select("span.video_thumb_wrap").select("img").attr("data-thumb_url");
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,5))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,5),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,5)));
            thequery.addPreview(parse(link));
            thequery.addName(title);
	}
        return thequery;
    }
    
    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Vector<File> thumbs = new Vector<File>();
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
        else {
            String html = Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html();
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
        } //if not found in cache download it
        
	int thumbIndex = page.toString().indexOf("thumbs:");
	String definition = CommonUtils.getBracket(page.toString(),thumbIndex, 1);
	String mainLink = CommonUtils.eraseChar(CommonUtils.getLink(definition,definition.indexOf("urlPattern:")+13,'"'),'\\');
	String temp = CommonUtils.getBracket(definition,definition.indexOf("urlPattern:"));
	
	int max = Integer.parseInt(temp.substring(1, temp.length()-1));
	int width = CommonUtils.getThumbs(definition,definition.indexOf("thumbWidth"),',');
	int height = CommonUtils.getThumbs(definition,definition.indexOf("thumbHeight"),',');
	for(int i = 0; i <= max; i++) {
            String link = CommonUtils.replaceIndex(mainLink,i,String.valueOf(max));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,5)))
                CommonUtils.saveFile(link, CommonUtils.getThumbName(link,5), MainApp.imageCache);
            File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,5));
            Vector<File> split = CommonUtils.splitImage(grid, 5, 5, 0, 0);
            for(int j = 0; j < split.size(); j++)
                thumbs.add(split.get(j));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);

	return page.select("meta").get(6).attr("content");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        int posterIndex = page.toString().indexOf("poster:");
        String thumb = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(), posterIndex+9, '"'),'\\');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,7))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,7),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,7));
    }    
    
    @Override
    protected void setExtractorName() {
        extractorName = "Redtube";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.redtube.com/?search=/"+str+"/";
        
        Document page = getPage(searchUrl,false); video v = null;
		
	Elements searchResults = page.getElementById("search_results_block").select("li.videoblock_list");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage("https://www.redtube.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String thumb = searchResults.get(i).select("span.video_thumb_wrap").select("img").attr("data-thumb_url");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,5))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,5),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            v = new video("https://www.redtube.com"+searchResults.get(i).select("a").attr("href"),Jsoup.parse(searchResults.get(i).select("div.video_title").toString()).body().text(),new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,5)));
            break; //if u made it this far u already have a vaild video
        }
        return v;
    }
}
