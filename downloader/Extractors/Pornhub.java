/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.downloadedMedia;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PrivateVideoException;
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.GameTime;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import static java.lang.Thread.sleep;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Pornhub extends GenericQueryExtractor{
    
    public Pornhub() { //this contructor is used for when you jus want to query
        
    }
    
    public Pornhub(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Pornhub(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Pornhub(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private void downloadPic(String link, OperationStream s) throws MalformedURLException {
        long stop = 0; String name = CommonUtils.getPicName(link);
        do {
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(name));
            stop = CommonUtils.saveFile(link,CommonUtils.clean(name),MainApp.preferences.getPictureFolder(),s);
            try {
                sleep(4000);
            } catch (InterruptedException ex) {
                System.out.println("Failed to pause");
            }
        }while(stop != -2); //retry download if failed
    }
    
    private void getPic(String link, OperationStream s) throws MalformedURLException, IOException {
        Document page = Jsoup.parse(Jsoup.connect(link).userAgent(CommonUtils.pcClient).get().html());
        String img = "";
            Element div = page.getElementById("photoImageSection");
            if (div == null) { div = page.getElementById("gifImageSection"); img = div.select("div.centerImage").attr("data-gif"); }
            else img = div.select("div.centerImage").select("a").select("img").attr("src");
        downloadPic(img,s);
    }
    
    private Map<String,String> getQualities(String src) {
        int from = 0, occur = 0;
        
        Map<String,String> qualities = new HashMap<String, String>();
        while((occur = src.indexOf("quality",from)) != -1) {
            qualities.put(CommonUtils.getLink(src, occur+10, '\"'),CommonUtils.eraseChar(CommonUtils.getUrl(src,occur), '\\'));
            if (qualities.get(CommonUtils.getLink(src, occur+10, '\"')).length() == 0)
                qualities.remove(qualities.get(CommonUtils.getLink(src, occur+10, '\"')));
            from = occur + 1;
        }
        
        return qualities;
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException,SocketTimeoutException,UncheckedIOException, GenericDownloaderException, Exception{
        if (s != null) s.startTiming();
        Document page;
        
        if (isPhoto(url)) {
            getPic(url,s);
            GameTime took = s.endOperation();
            if (s != null) s.addProgress("Took "+took.getTime()+" to download");
            MainApp.createNotification("Download Success","Finished Downloading "+getVideoName());
            File saved = new File(MainApp.preferences.getPictureFolder() + File.separator + CommonUtils.clean(getVideoName()));
            MainApp.downloadHistoryList.add(new downloadedMedia(videoName,videoThumb,saved,name()));
        } else if (isAlbum(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
            Elements items = page.select("li.photoAlbumListContainer");
            for(int i = 0; i < items.size(); i++) {
                String subLink = "https://www.pornhub.com" + items.get(i).select("a").attr("href");
                try {getPic(subLink,s);} catch(UncheckedIOException e) {i--; /*retry link*/}
            }
            GameTime took = s.endOperation();
            if (s != null) s.addProgress("Took "+took.getTime()+" to download");
            MainApp.createNotification("Download Success","Finished Downloading Album"+getVideoName());
            File saved = new File(MainApp.preferences.getPictureFolder() + File.separator + CommonUtils.clean(getVideoName()));
            MainApp.downloadHistoryList.add(new downloadedMedia(videoName,videoThumb,saved,name()));
        } else { //must be a video
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
            verify(page);
            //Element video = page.getElementById("videoShow"); video.attr("data-default");
            Elements titleSpan = page.select("span.inlineFree");
            String title = Jsoup.parse(titleSpan.toString()).body().text(); //pull out text from span
            String rawQualities = CommonUtils.getLink(page.toString(), page.toString().indexOf(":",page.toString().indexOf("mediaDefinitions")) + 4, ']');
            Map<String,String> quality = getQualities(rawQualities);
            String video = null;
            if(quality.containsKey("480"))
                video = quality.get("480");
            else if (quality.containsKey("720"))
                video = quality.get("720");
            else if (quality.containsKey("360"))
                video = quality.get("360");
            else video = quality.get("240");
                
            super.downloadVideo(video,title,s);
        }
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if(Jsoup.parse(page.select("title").toString()).text().equals("Page Not Found"))
            throw new PageNotFoundException(page.location());
        if (!page.select("div.privateContainer").isEmpty())
                throw new PrivateVideoException();
            Element e;
            if ((e = page.getElementById("messageWrapper")) != null)
                if (Jsoup.parse(e.select("p").toString()).body().text().equals("This video was deleted by uploader."))
                    throw new VideoDeletedException();
    }

    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://pornhub.com/video/search?search="+search;
        
        GenericQuery thequery = new GenericQuery();
        Document page = getPage(searchUrl,false);
                
	Elements searchResults = page.select("ul.videos.search-video-thumbs").select("li");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://pornhub.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            try {verify(getPage("https://pornhub.com"+searchResults.get(i).select("a").attr("href"),false));} catch (GenericDownloaderException e) {continue;}
            thequery.addLink("https://pornhub.com"+searchResults.get(i).select("a").attr("href"));
            String thumbLink = searchResults.get(i).select("a").select("img").attr("data-mediumthumb");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,4))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,4),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,4)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(searchResults.get(i).select("a").attr("title"));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException{ 
        Vector<File> thumbs = new Vector<>();
        
        try {
            String html;
            if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,true))) //check to see if page was downloaded previous 
                html = CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,true));
            else {
                html = Jsoup.connect(url).followRedirects(true).cookie("trial-step1-modal-shown", "null").userAgent(CommonUtils.mobileClient).get().html(); //not found so download it
                CommonUtils.savePage(html, url, true);
            }
            Document page = Jsoup.parse(html);
            //System.out.println(page);
            Element preview = page.getElementById("thumbDisplay");
            Elements previewImg = preview.select("img");

            Iterator<Element> img = previewImg.iterator();
            while(img.hasNext()) {
                String thumb = img.next().attr("data-src");
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,5))) //if file not already in cache download it
                    CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,5),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,5)));
            }
        } catch (NullPointerException e) {
            return thumbs; //return parse(url); //possible it went to interstitial? instead
        }
        return thumbs;
    }
    
    private static boolean isAlbum(String url) {
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if (url.matches("https://www.pornhub.com/album/[\\S]*") || url.matches("https://pornhub.com/album/[\\S]*"))
            return true;
        else return false;
    }
    
    private static boolean isPhoto(String url) {
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if (url.matches("https://www.pornhub.com/photo/[\\S]*") || url.matches("https://pornhub.com/photo/[\\S]*"))
            return true;
        if (url.matches("https://www.pornhub.com/gif/[\\S]*") || url.matches("https://pornhub.com/gif/[\\S]*"))
            return true;
        else return false;
    }
    
    private static boolean isGif(String url) {
        if (url.matches("https://www.pornhub.com/gif/[\\S]*") || url.matches("https://pornhub.com/gif/[\\S]*"))
            return true;
        else return false;
    }
    
     private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
         Document page;
        if (isAlbum(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
            String subUrl = "https://www.pornhub.com" + page.select("li.photoAlbumListContainer").get(0).select("a").attr("href");
            Document subpage = Jsoup.parse(Jsoup.connect(subUrl).userAgent(CommonUtils.pcClient).get().html());
            String img = subpage.getElementById("photoImageSection").select("div.centerImage").select("a").select("img").attr("src");
            return "Album " + CommonUtils.getPicName(img);
        } else if (isPhoto(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
            String img = "";
            Element div = page.getElementById("photoImageSection");
            if (div == null) { div = page.getElementById("gifImageSection"); img = div.select("div.centerImage").attr("data-gif"); }
            else img = div.select("div.centerImage").select("a").select("img").attr("src");
            return CommonUtils.getPicName(img);
        } else {
            if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,true))) //check to see if page was downloaded previous
               page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,true)));
            else {
                String html = Jsoup.connect(url).userAgent(CommonUtils.mobileClient).get().html();
                page = Jsoup.parse(html);
               CommonUtils.savePage(html, url, true);
            }
            verify(page);
           Elements titleSpan = page.select("span.inlineFree");
           String title = Jsoup.parse(titleSpan.toString()).body().text(); //pull out text in span
           return title;
        }
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        Document page;
        if (isAlbum(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
            String subUrl = "https://www.pornhub.com" + page.select("li.photoAlbumListContainer").get(0).select("a").attr("href");
            Document subpage = Jsoup.parse(Jsoup.connect(subUrl).userAgent(CommonUtils.pcClient).get().html());
            String img = subpage.getElementById("photoImageSection").select("div.centerImage").select("a").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(img))) //if file not already in cache download it
                CommonUtils.saveFile(img,CommonUtils.getThumbName(img),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(img));
        } else if (isPhoto(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
            String img = "";
            Element div = page.getElementById("photoImageSection");
            if (div == null) { div = page.getElementById("gifImageSection"); img = div.select("div.centerImage").attr("data-gif"); }
            else img = div.select("div.centerImage").select("a").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(img))) //if file not already in cache download it
                CommonUtils.saveFile(img,CommonUtils.getThumbName(img),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(img));
        } else {
            if (CommonUtils.checkImageCache(CommonUtils.getCacheName(url,true))) //check to see if page was downloaded previous
                page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,true)));
             else {
                 String html = Jsoup.connect(url).userAgent(CommonUtils.mobileClient).get().html();
                 page = Jsoup.parse(html);
                CommonUtils.savePage(html, url, true);
             }
            verify(page);
            Elements metas = page.select("meta");
            String thumb = "";
            
            Iterator<Element> i = metas.iterator();
            while(i.hasNext()) {
                Element temp = i.next();
                if (temp.attr("property").equals("og:image")) {
                    thumb = temp.attr("content"); break;
                }
            }

            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,5))) //if file not already in cache download it
                CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,5),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,5));
        }
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Pornhub";
    }
}