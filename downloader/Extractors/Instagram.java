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
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import static java.lang.Thread.sleep;
import java.net.SocketTimeoutException;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Instagram extends GenericExtractor{
    
    public Instagram(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Instagram(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Instagram(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private void download(String url, OperationStream s, String type) throws IOException {
       long stop = 0; String name = CommonUtils.parseName(url,type); File folder;
       if (type.equals(".jpg"))
           folder = MainApp.settings.preferences.getPictureFolder();
       else folder = MainApp.settings.preferences.getVideoFolder();
        do {
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(name));
            stop = CommonUtils.saveFile(url,CommonUtils.clean(name),folder,s);
            try {
                sleep(4000);
            } catch (InterruptedException ex) {
                System.out.println("Failed to pause");
            }
        }while(stop != -2); //retry download if failed
        MainApp.createNotification("Download Success","Finished Downloading "+CommonUtils.clean(name));
        File saved = new File(folder + File.separator + CommonUtils.clean(name));
        if (type.equals(".jpg"))
            MainApp.downloadHistoryList.add(new downloadedMedia(videoName,saved,saved,name()));
        else MainApp.downloadHistoryList.add(new downloadedMedia(videoName,getThumb(),saved,name()));
    }

    @Override
    public void getVideo(OperationStream s) throws IOException,SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
	if (isVideo(page)) { //download video
            String videoLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++) {
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            }
            download(videoLink, s,".mp4");
        } else if(isProfilePage(page)) { //download profile pic
           String picLink = null;
           System.out.println("here3");
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++) {
                if(metas.get(i).attr("property").equals("og:image"))
                    picLink = metas.get(i).attr("content");
            }
            download(picLink, s,".jpg");
        } else { //download pic/s
            System.out.println("here4");
           int occur, from = 0; boolean first = true; int count = 0;
           while((occur = page.toString().indexOf("display_resources",from)) != -1) {
               if (first) {first = false; from = occur + 1; continue;}
               from = occur + 1; count++;
               String brack = CommonUtils.getSBracket(page.toString(),occur);
               download(parseBracket(brack),s,".jpg");
           } if (count < 1) { //if there really was jus one
               occur = page.toString().indexOf("display_resources",0);
               if(occur != -1) {
                    String brack = CommonUtils.getSBracket(page.toString(),occur);
                    download(parseBracket(brack),s,".jpg");
               } else  {
                   System.out.println("was -1");
                   String picLink = null;
                    Elements metas = page.select("meta");
                    for(int i = 0; i < metas.size(); i++) {
                        if(metas.get(i).attr("property").equals("og:image"))
                            picLink = metas.get(i).attr("content");
                    }
                    download(picLink, s,".jpg");
               }
           }
        }
        
        GameTime took = s.endOperation();
        if (s != null) s.addProgress("Took "+took.getTime()+" to download");
    }
    
    private static String getLinks(String src) {
        String[] tokens = src.split("\"");
        return tokens[3];
    }
    
    private static String parseBracket(String bracket) {
        int occur, from = 0;
        Vector<String> link = new Vector<>();
        while((occur = bracket.indexOf('{',from)) != -1) {
            String src = CommonUtils.getBracket(bracket,occur);
            from = occur+src.length();
            link.add(getLinks(src));
        }
        return link.get(link.size()-1); //highest quality is usually mentioned last
    }
    
    private static boolean isVideo(Document page) {
        Elements metas = page.select("meta");
        for(int i = 0; i < metas.size(); i++) {
            if (metas.get(i).attr("property").equals("og:video"))
                return true;
        }
        return false;
    }
    
    private static boolean isProfilePage(Document page) {
        return !page.toString().contains("display_resources");
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page;
         if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
         else {
             String html = Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
             page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
         }
        if (isVideo(page)) {
            String videoLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++) {
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            }
            return CommonUtils.parseName(videoLink, ".mp4");
        } else return downloadThumb(url).getName();
    }
    
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page;//https://www.instagram.com/p/Bky0zJyA4kY-2nbW7RQ6oN71vNFEu-2lawigG00/
         if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
         else {
             String html = Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
             page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
         }
        
        String thumbLink = null;
        Elements metas = page.select("meta");
        for(int i = 0; i < metas.size(); i++) {
            if(metas.get(i).attr("property").equals("og:image"))
                thumbLink = metas.get(i).attr("content");
        }
        if(!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.parseName(thumbLink,".jpg"));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Instagram";
    }

    @Override
    public video similar() {
        return null;
    }

    @Override
    public video search(String str) {
        return null;
    }
}
