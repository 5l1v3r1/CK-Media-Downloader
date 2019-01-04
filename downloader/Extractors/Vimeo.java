/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Vimeo extends GenericExtractor{
    
    public Vimeo() { //this contructor is used for when you jus want to search
        
    }
    
    public Vimeo(String url)throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Vimeo(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Vimeo(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String,String> getQualities(String src) {
        Map<String, String> qualities = new HashMap<>();
        
        try {
            JSONArray progressiveArray = (JSONArray)new JSONParser().parse(src);
            for(int i = 0; i < progressiveArray.size(); i++) {
                JSONObject quality = (JSONObject)progressiveArray.get(i);
                if(!quality.get("mime").equals("video/mp4")) continue;
                qualities.put((String)quality.get("quality"), (String)quality.get("url"));
            }
        } catch (ParseException e) {
            System.out.println("error parsing "+url);
        }
        return qualities;
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        verify(page); String newUrl = "https://player.vimeo.com/video/"+url.split("/")[url.split("/").length -1];
        page = Jsoup.parse(Jsoup.connect(newUrl).userAgent(CommonUtils.PCCLIENT).get().html());
        verify(page);
        Map<String, String> qualities = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("progressive")));
        int max = 144;
        Iterator<String> i = qualities.keySet().iterator();
        while(i.hasNext()) {
            String str = i.next();
            int temp = Integer.parseInt(str.substring(0,str.length()-1));
            if(temp > max)
                max = temp;
        }
        
	String video = qualities.get(String.valueOf(max)+"p");
        
        super.downloadVideo(video,videoName,s);
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        String title = Jsoup.parse(page.select("title").get(0).toString()).text();
        
        if(title.equals("VimeUhOh") || !page.select("h1.exception_title iris_header").isEmpty())
            throw new PageNotFoundException();
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        verify(page);
        String title = Jsoup.parse(page.select("title").get(0).toString()).text();        
	return title.replace(" on Vimeo","");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        String newUrl = "https://player.vimeo.com/video/"+url.split("/")[url.split("/").length -1];
         Document page = getPage(newUrl,false);
        verify(page); String thumb = null;
        try {
            String thumbs = CommonUtils.getBracket(page.toString(), page.toString().indexOf("thumbs\":{"));
            JSONObject thumbObject = (JSONObject)new JSONParser().parse(thumbs);
            thumb = (String)thumbObject.get("base");
        } catch (ParseException e) {
            System.out.println("error parsing for thumb "+url);
            throw e;
        }
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,2))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,2),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,2));
    }
    
    private static String convertUrl(String url) {
        return url.replace("player.vimeo.com/video/", "vimeo.com/");
    }

    @Override
    protected void setExtractorName() {
        extractorName = "Vimeo";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    	/*str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "https://www.vimeo.com/search?q="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("div.iris_p_infinite__item span-1");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("div.iris_thumbnail.iris_thumbnail.iris_thumbnail--16-9").select("img").attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,2))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,2),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = li.get(i).select("a.iris_video-vital__overlay.iris_link-box.iris_annotation-box.iris_chip-box").attr("href");
        	String name = li.get(i).select("span.iris_link.iris_link--gray-2").text();
                System.out.println("link = "+link+"\nname = "+name);
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,2)));
        	break;
        }
        
        return v;*/
    }

    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        verify(page); String newUrl = "https://player.vimeo.com/video/"+url.split("/")[url.split("/").length -1];
        page = Jsoup.parse(Jsoup.connect(newUrl).userAgent(CommonUtils.PCCLIENT).get().html());
        verify(page);
        Map<String, String> qualities = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("progressive")));
        int max = 144;
        Iterator<String> i = qualities.keySet().iterator();
        while(i.hasNext()) {
            String str = i.next();
            int temp = Integer.parseInt(str.substring(0,str.length()-1));
            if(temp > max)
                max = temp;
        }
        
	String video = qualities.get(String.valueOf(max)+"p");
        return CommonUtils.getContentSize(video);
    }
}
