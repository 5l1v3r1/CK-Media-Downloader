/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.downloadedMedia;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import static downloader.Extractors.GenericExtractor.configureUrl;
import static downloader.Extractors.GenericExtractor.getPage;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Pornpics extends GenericExtractor{
    private static final int skip = 2;

    public Pornpics() { //this contructor is used for when you jus want to search
        
    }

    public Pornpics(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Pornpics(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Pornpics(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private void downloadPic(String link, OperationStream s) throws MalformedURLException {
        long stop = 0; String name = CommonUtils.getThumbName(link,skip);
        do {
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(name));
            stop = CommonUtils.saveFile(link,CommonUtils.clean(name),MainApp.settings.preferences.getPictureFolder().getAbsolutePath()+File.separator+videoName,s);
        }while(stop != -2); //retry download if failed
    }

    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        if (s != null) s.startTiming();

        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        Elements a = page.select("a.rel-link");
        for(Element item :a)
            downloadPic(item.attr("href"),s);
        GameTime took = s.endOperation();
        if (s != null) s.addProgress("Took "+took.getTime()+" to download");
        MainApp.createNotification("Download Success","Finished Downloading Album"+videoName);
        File saved = new File(MainApp.settings.preferences.getPictureFolder() + File.separator + this.videoName);
        MainApp.downloadHistoryList.add(new downloadedMedia(videoName,videoThumb,saved,name()));
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        Document page = getPage(url,false);
        return page.select("title").text().replace(" - PornPics.com","");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        String thumbLink = page.select("a.rel-link").get(0).attr("href");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,skip));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Pornpics";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        long total = 0;
        Document page = getPage(url,false);
        Elements a = page.select("a.rel-link");
        for(Element item :a)
            total += CommonUtils.getContentSize(item.attr("href"));
        return total;
    }
}
