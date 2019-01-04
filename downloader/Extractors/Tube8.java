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
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Tube8 extends GenericQueryExtractor{
	private static final int skip = 4;
    
    public Tube8() { //this contructor is used for when you jus want to query
        
    }
    
    public Tube8(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Tube8(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Tube8(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.tube8.com/searches.html?q=/"+search+"/";
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
		
	Elements searchResults = page.select("div.video_box");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage(searchResults.get(i).select("p.video_title").select("a").attr("href"))) continue; //test to avoid error 404
            String link = searchResults.get(i).select("p.video_title").select("a").attr("href");
            String title = searchResults.get(i).select("p.video_title").select("a").attr("title");
            String thumb = searchResults.get(i).select("div.videoThumbsWrapper").select("img").attr("data-thumb");
            System.out.println("thumb: "+thumb);
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip)));
            thequery.addPreview(parse(link));
            thequery.addName(title);
            long size; try { size = getSize(link); } catch (GenericDownloaderException | IOException e) {size = -1;}
            thequery.addSize(size);
	}
        return thequery;
    }

    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Vector<File> thumbs = new Vector<File>();
        
        Document page = getPage(url,false);
        
        String mainLink = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(),page.toString().indexOf("timeline_preview_url",page.toString().indexOf("var flashvars"))+23,'"'),'\\');
        String temp = CommonUtils.getBracket(page.toString(),page.toString().indexOf("timeline_preview_url",page.toString().indexOf("var flashvars")));
        int max = Integer.parseInt(temp.substring(1, temp.length()-1));
        
        for(int i = 0; i <= max; i++) {
            String link = CommonUtils.replaceIndex(mainLink,i,String.valueOf(max));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,skip+1)))
                CommonUtils.saveFile(link, CommonUtils.getThumbName(link,skip+1), MainApp.imageCache);
            File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,skip+1));
            Vector<File> split = CommonUtils.splitImage(grid, 5, 5, 0, 0);
            for(int j = 0; j < split.size(); j++)
                thumbs.add(split.get(j));
        }
        return thumbs;
    }
    
    private static Map<String, String> getQualities(String raw, String s) {
        int happen, from = 0;
        
        Map<String, String> qualities = new HashMap<>();
        while((happen = raw.indexOf(s,from)) != -1) {
            qualities.put(CommonUtils.getLink(raw,happen + 10,'\"'), CommonUtils.eraseChar(CommonUtils.getUrl(raw,happen),'\\'));
            from = happen +1;
        }
        return qualities;
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        if (s != null) s.startTiming();
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
        
        String title = Jsoup.parse(page.select("h1").toString()).body().text();
        Map<String, String> quality= getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("mediaDefinition",page.toString().indexOf("var flashvars"))),"quality");
        String video;
        
        if (quality.containsKey("720"))
            video = quality.get("720");
        else if(quality.containsKey("480"))
            video = quality.get("480"); 
        else if (quality.containsKey("1080"))
            video = quality.get("1080"); 
        else if(quality.containsKey("240"))
            video = quality.get("240");    
        else if(quality.containsKey("180"))
            video = quality.get("180");    
        else 
            video = quality.get((String)quality.keySet().toArray()[0]);
        super.downloadVideo(video, title, s);       
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);

	return Jsoup.parse(page.select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(), page.toString().indexOf("image_url",page.toString().indexOf("var flashvars")) + 12,'\"'), '\\');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip));
    }  
    
    @Override
    protected void setExtractorName() {
        extractorName = "Tube8";
    }

    @Override
    public video similar() throws IOException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.gridList.videosList").select("div.video_box");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = li.get(i).select("a").get(0).attr("href");
            String title = li.get(i).select("p.video_title").select("a").text();
                try {v = new video(link,title,downloadThumb(link),getSize(link));}catch(Exception e) {continue;}
                break;
            }
        return v;
    }

    @Override
    public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.tube8.com/searches.html?q=/"+str+"/";
        
        Document page = getPage(searchUrl,false); video v = null;
		
	Elements searchResults = page.select("div.video_box");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage(searchResults.get(i).select("p.video_title").select("a").attr("href"))) continue; //test to avoid error 404
            String link = searchResults.get(i).select("p.video_title").select("a").attr("href");
            String title = searchResults.get(i).select("p.video_title").select("a").attr("title");
            String thumb = searchResults.get(i).select("div.videoThumbsWrapper").select("img").attr("data-thumb");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            try { v = new video(link,title,new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip)),getSize(link)); } catch (GenericDownloaderException | IOException e) {}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    private static long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = Jsoup.parse(Jsoup.connect(link).get().html());
        
        Map<String, String> quality= getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("mediaDefinition",page.toString().indexOf("var flashvars"))),"quality");
        String video;
        
        if (quality.containsKey("720"))
            video = quality.get("720");
        else if(quality.containsKey("480"))
            video = quality.get("480"); 
        else if (quality.containsKey("1080"))
            video = quality.get("1080"); 
        else if(quality.containsKey("240"))
            video = quality.get("240");    
        else if(quality.containsKey("180"))
            video = quality.get("180");    
        else 
            video = quality.get((String)quality.keySet().toArray()[0]);
        
        return CommonUtils.getContentSize(video);
    }
    
    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
}
