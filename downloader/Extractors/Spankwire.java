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
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Spankwire extends GenericQueryExtractor{
	private static final int skip = 5;
    
    public Spankwire() { //this contructor is used for when you jus want to query
        
    }
    
    public Spankwire(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Spankwire(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Spankwire(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        search = search.trim(); 
        search = search.replaceAll(" ", "%2B");
        String searchUrl = "https://spankwire.com/search/straight/keyword/"+search;
        
        GenericQuery thequery = new GenericQuery();
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("li.js-li-thumbs");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://spankwire.com"+searchResults.get(i).select("a").get(0).attr("href"))) continue; //test to avoid error 404
            thequery.addLink("https://spankwire.com"+searchResults.get(i).select("a").get(0).attr("href"));
            String thumbLink = "https:"+searchResults.get(i).select("img").attr("data-original");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("div.video_thumb_wrapper__thumb-wrapper__title_video").select("a").toString()).body().text());
            long size = -1; try { size = getSize("https://spankwire.com"+searchResults.get(i).select("a").get(0).attr("href")); } catch (GenericDownloaderException | IOException e) {}
            thequery.addSize(size);
	}
        return thequery;
    }

    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
         Vector<File> thumbs = new Vector<File>();
        
        Document page = getPage(url,false);
        
        String mainLink = CommonUtils.getLink(page.toString(),page.toString().indexOf("playerData.timeline_preview_url") + 35, '\'');
        String temp = CommonUtils.getBracket(page.toString(),page.toString().indexOf("timeline_preview_url"));
        int max = Integer.parseInt(temp.substring(1, temp.length()-1));
        
        for(int i = 0; i <= max; i++) {
            long result;
            String link = "https:"+CommonUtils.replaceIndex(mainLink,i,String.valueOf(max));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,skip)))
                result = CommonUtils.saveFile(link, CommonUtils.getThumbName(link,skip), MainApp.imageCache);
            else result = -2;
            if (result == -2) {
                File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,skip));
                Vector<File> split = CommonUtils.splitImage(grid, 5, 5, 0, 0);
                for(int j = 0; j < split.size(); j++)
                    thumbs.add(split.get(j));
            }
        }
        return thumbs;
    }
    
     private static Map<String,String> getQualities(String src) {
        int happen, from = 0;
        
        Map<String, String> qualities = new HashMap<>();
        while((happen = src.indexOf("playerData.cdnPath",from)) != -1) {
            if(src.charAt(src.indexOf('\'',happen)+1) == '\'') {
                from = happen + 1; continue;
            }
            qualities.put(CommonUtils.getLink(src, happen+18, ' '), "https:" + CommonUtils.eraseChar(CommonUtils.getLink(src,src.indexOf("'//", happen)+1,'\''), '\\'));
            from = happen + 1;
        }
        return qualities;
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
         if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        
	String title = Jsoup.parse(page.select("h1").get(0).toString()).body().text();
	Map<String,String> quality = getQualities(page.toString());

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
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException,Exception{
        Document page = getPage(url,false);
	return Jsoup.parse(page.select("h1").get(0).toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
       Document page = getPage(url,false);
        String thumb = "https:" + CommonUtils.getLink(page.toString(),page.toString().indexOf("'//",page.toString().indexOf("playerData.poster"))+1, '\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Spankwire";
    }

    @Override
    public video similar() throws IOException {
    	/*if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.sc-eInJlc.hJWEWF").select("div.sc-hmXxxW.fgVpNC");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	try {
	        	if (count > li.size()) break;
	        	int i = randomNum.nextInt(li.size()); count++;
	        	String link = "https://www.spankwire.com" + li.get(i).select("a").get(0).attr("href");
	            String title = li.get(i).select("a").get(1).select("span").text();
	            if (title.length() < 1) downloadVideoName(link);
	                v = new video(link,title,downloadThumb(link));
	                break;
        	} catch (UncheckedIOException | Exception e) {continue;} 
        }
        return v;*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "%2B");
        String searchUrl = "https://spankwire.com/search/straight/keyword/"+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("li.js-li-thumbs");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://spankwire.com"+searchResults.get(i).select("a").get(0).attr("href"))) continue; //test to avoid error 404
            String thumbLink = "https:"+searchResults.get(i).select("img").attr("data-original");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = "https://spankwire.com"+searchResults.get(i).select("a").get(0).attr("href");
            long size = -1; try { size = getSize(link); } catch (GenericDownloaderException | IOException e) {continue;}
            v = new video("https://spankwire.com"+searchResults.get(i).select("a").get(0).attr("href"),Jsoup.parse(searchResults.get(i).select("div.video_thumb_wrapper__thumb-wrapper__title_video").select("a").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)),size);
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private static long getSize(String link) throws IOException, GenericDownloaderException{
        Document page = Jsoup.parse(Jsoup.connect(link).userAgent(CommonUtils.PCCLIENT).get().html());
        
	Map<String,String> quality = getQualities(page.toString());

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
        return CommonUtils.getContentSize(video);
    }
    
    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
}
