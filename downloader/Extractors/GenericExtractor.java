/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.downloadedMedia;
import downloaderProject.GameTime;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import static java.lang.Thread.sleep;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public abstract class GenericExtractor implements Trackable {
    protected File videoThumb;
    protected String videoName, url;
    protected String extractorName;

    GenericExtractor(String url, File thumb, String videoName) {
       this();
       this.videoThumb = thumb;
       this.videoName = videoName;
       this.url = configureUrl(url);
    }
    
    GenericExtractor() { //this contructor is used for when you jus want to query / search
        setExtractorName();
    }
    
    public String name() {
       return extractorName; 
    }
    
    protected abstract void setExtractorName();
    
    protected static Document getPage(String url, boolean mobile) throws FileNotFoundException, IOException {
        Document page;
         if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile)));
        else { String html;
             if (mobile)
                html = Jsoup.connect(url).userAgent(CommonUtils.mobileClient).get().html();
             else html = Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html();
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, mobile);
        } //if not found in cache download it
         return page;
    }
    
    public abstract void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception;
    
    protected void downloadVideo(String video, String title, OperationStream s) throws MalformedURLException {
        long stop = 0;
        do {
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(title+".mp4"));
            stop = CommonUtils.saveFile(video,CommonUtils.clean(title+".mp4"),MainApp.settings.preferences.getVideoFolder(),s);
            try {
                sleep(4000);
            } catch (InterruptedException ex) {
                System.out.println("Failed to pause");
            }
        }while(stop != -2); //retry download if failed
        GameTime took = s.endOperation();
        if (s != null) s.addProgress("Took "+took.getTime()+" to download");
        MainApp.createNotification("Download Success","Finished Downloading "+title);
        File saved = new File(MainApp.settings.preferences.getVideoFolder() + File.separator + CommonUtils.clean(title+".mp4"));
        MainApp.downloadHistoryList.add(new downloadedMedia(videoName,videoThumb,saved,name()));
    }
    
    public String getVideoName() {
        return this.videoName;
    }
    
    public File getThumb() {
        return this.videoThumb;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public static String configureUrl(String link) {
        if ((!link.startsWith("https://")) && (!link.startsWith("http://"))) return "https://" + link;
        else return link;
    }
}
