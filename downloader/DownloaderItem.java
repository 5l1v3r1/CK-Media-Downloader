/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import ChrisPackage.GameTime;
import com.jfoenix.controls.JFXButton;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.downloadedMedia;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Extractors.GenericExtractor;
import downloader.Extractors.Playlist;
import downloaderProject.DataIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.SocketException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javax.imageio.ImageIO;
import org.jsoup.Jsoup;

/**
 *
 * @author christopher
 */
public class DownloaderItem {
    private String url, videoName;
    private Pane root;
    private GenericExtractor extractor;
    private Site.Type type;
    private Site.Page pageType;
    private video v = null;
    private File thumbFile;
    private boolean loaded;
    private Vector<String> downloadLinks, downloadNames;
    private String albumName, page;
    private long size = -1;
    
    public void release() {
        root = null;
        extractor = null;
        thumbFile = null;
        v = null;
        downloadLinks = null;
        page = null;
        albumName = null;
    }
    
    public String getName() {
        return videoName;
    }
    
    public String getSite() {
        if (extractor != null)
            return extractor.name();
        else return null;
    }
    
    public video getSide() throws GenericDownloaderException {
        try {
            if (extractor != null)
                return extractor.similar();
            else return null;
        } catch (UnsupportedOperationException | IOException e) {return null;}
    }
    
    private GenericExtractor getExtractor(video v) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
    	if ((null == type) && (null == pageType))
            return null;
        else if (v == null) {
            if (page != null)
                return ExtractorList.getExtractor(pageType,Jsoup.parse(page));
            else return ExtractorList.getExtractor(type,url);
        } else return ExtractorList.getExtractor(type,url,v.getThumbnail(),v.getName());
    }
    
    private GenericExtractor getExtractor() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        return getExtractor(null);
    }
    
    private boolean setFromVideo(ImageView view) {
        try {
            thumbFile = v.getThumbnail();
            FileInputStream fis = new FileInputStream(v.getThumbnail());
            Image image = new Image(fis);
            if (fis != null) fis.close();
            view.setImage(image); return true;
        } catch (FileNotFoundException e) {
            MainApp.createMessageDialog("Couldnt find thumbnail to load it may have be deleted\n"+url);
            System.out.println("Failed to load thumbnail");
            return false;
        } catch (IOException e) {
            MainApp.createMessageDialog("Failed to load thumbnail\n"+url);
            System.out.println("Failed to load thumbnail: "+e.getMessage());
            return false;
        } catch (Exception e) {
            MainApp.createMessageDialog("Failed to download thumbnail\n"+url+"\n"+e.getMessage());
            System.out.println("Failed to download thumbnail: "+e.getMessage());
            return false;
        }
    }
    
    private boolean setFromExtractor(ImageView view) {
        try {
            if(extractor == null) return false; //couldnt find extractor for link
            thumbFile = extractor.getThumb();
            if (thumbFile == null) return true; //no thumb //this really shouldnt be allowed but....
            //if more than 10mb (probably a large gif) image stream will run out of memory
            if (thumbFile.length() < 1024 * 1024 * 10) { 
                FileInputStream fis = new FileInputStream(thumbFile);
                Image image = new Image(fis);
                if (fis != null) fis.close();
                view.setImage(image);
            } else { //so read it as a still image (if is gif)
                BufferedImage b = ImageIO.read(thumbFile);
                view.setImage(SwingFXUtils.toFXImage(b, null));
            }
            return true;
        } catch (FileNotFoundException e) {
            MainApp.createMessageDialog("(2)Couldnt get thumb from link: \n"+url);
            System.out.println("Failed to load thumbnail code: 2");
            return false;
        } catch (UncheckedIOException e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            System.out.println("Failed to download thumbnail (network)");
            return false;
        }catch(SocketTimeoutException e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            System.out.println("Failed to download thumbnail (connection timeout)");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            System.out.println("Failed to download thumbnail (some IO: network)");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            System.out.println("Failed to download thumbnail: "+e.getMessage());
            return false;
        }
    }
    
    private boolean getThumbnail() {
        ImageView view = (ImageView)root.lookup("#thumb");
        if (type == Site.Type.instagram) view.preserveRatioProperty().setValue(true);
        if (v == null)
            return setFromExtractor(view);
        else
            return setFromVideo(view);
    }
    
    private void setName() {
        Platform.runLater(new Runnable() {
           @Override public void run() {
                Label temp = (Label)root.lookup("#downloadName");
                temp.setText(videoName);
           }
        });
    }
    
    private void setButton() {
        Button downloadBtn = (Button)root.lookup("#downloadBtn");
        downloadBtn.setOnAction(event -> {downloadThis();});
        downloadBtn.setDisable(false);
    }
    
    private void setCloseBtn() {
        JFXButton close = (JFXButton)root.lookup("#cancel");
        //close.getStyleClass().clear();
        close.getStyleClass().add("jfx-cancel");
        close.setOnAction(event -> {clearThis();});
        close.setDisable(false);
        System.out.println("set close");
    }
    
    private void setSaveBtn() {
        Button btn = (Button)root.lookup("#save");
        btn.setOnAction(event -> {
            try {
                if (size == -1) {try {if (extractor == null) extractor = getExtractor(); size = extractor.getSize();}catch(Exception e){size = -1;}}
                DataIO.saveVideo(new video(url,extractor.getVideoName(),extractor.getThumb(),size));
                MainApp.createMessageDialog("Media saved");
                MainApp.settings.videoUpdate();
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to save video for later");
            }
        });
    }
    
    public boolean searchLink() throws GenericDownloaderException, IOException {       
       try { 
           setIndeteminate(true);
           if (v == null) {
               loaded = false;
               extractor = getExtractor(); if (extractor == null) throw new Exception("couldnt get extractor"); //unsupported link
               videoName = extractor.getVideoName();
               setSize();
           } else {
        	   loaded = true;
        	   extractor = getExtractor(v);
               setSize(v.getSize());
               videoName = v.getName();
           }
       } catch (Exception e) {
           if (e instanceof GenericDownloaderException) throw (GenericDownloaderException)e;
           e.printStackTrace();
           System.out.println(e.getMessage()); release();
           return false;
       }
       
       if (extractor instanceof Playlist) {
            if (((Playlist)extractor).isPlaylist()) {
                Vector<String> links = ((Playlist)extractor).getItems();
                DownloaderItem download;
                for(int i = 0; i < links.size(); i++) {
                    download = new DownloaderItem();
                    download.setLink(links.get(i)); download.setType(Site.getUrlSite(links.get(i))); download.setVideo(null);
                    MainApp.dm.addDownload(download);
                }
            }
       }
       
       if (!getThumbnail()) {release(); return false;} //either link not supported or network error
       
       setButton();
       setSaveBtn();
       setName();
       setCloseBtn();
       
       System.out.println("Found");
       setIndeteminate(false);
       return true; //if u made it this far process must be successful
    }
    
    public boolean wasLoaded() {
    	return loaded;
    }
    
    public void setPage(String p) {
        this.page = p;
    }
    
    public void setPageType(Site.Page type) {
        this.pageType = type;
    }
    
    public void setType(Site.Type type) {
        this.type = type;
    }
    
    public void setLink(String url) {
        this.url = url;
    }
    
    public void setVideo(video v) {
        this.v = v;
    }
    
    public String getLink() {
        if (extractor != null)
            return extractor.getUrl();
        else return url;
    }
    
    private void disableButton() {
        if (root != null)
            ((Button)root.lookup("#downloadBtn")).setDisable(true);
    }
    
    private void enableButton() {
        if (root != null)
            ((Button)root.lookup("#downloadBtn")).setDisable(false);
    }

    private ImageView getIcon(String path) {
        Image image = new Image(System.class.getResourceAsStream(path));
        ImageView icon = new ImageView();
        icon.setImage(image);
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        return icon;
    }
    
    private ContextMenu initContextMenu() {
       ContextMenu menu = new ContextMenu();
       menu.getStyleClass().add("background");
       final MenuItem[] item = new MenuItem[5];
       item[0] = new MenuItem("copy url");
       item[1] = new MenuItem("open in browser");
       item[2] = new MenuItem("remove");
       item[3] = new MenuItem("remove all");
       item[4] = new MenuItem("export links to file");
       
       item[0].setGraphic(getIcon("/icons/icons8-copy-link-48.png"));
       item[0].setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(url), new StringSelection(MainApp.username));
            }
       });
       item[1].setGraphic(getIcon("/icons/icons8-open-in-browser-40.png"));
       item[1].setOnAction(new EventHandler<ActionEvent>() {
           @Override
            public void handle(ActionEvent t) {
                if (Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.browse(new URI(url));
                        } catch (URISyntaxException ex) {
                            System.out.println("Bad Uri");
                        } catch (IOException ex) {
                            MainApp.createMessageDialog("Failed to load");
                        }
                    }).start();    
                } else
                    MainApp.createMessageDialog("Not supported");
            }
       });
       item[2].setGraphic(getIcon("/icons/icons8-cancel-40.png"));
       item[2].setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                clearThis();
            }
       });
       item[3].setGraphic(getIcon("/icons/icons8-cancel-40.png"));
       item[3].setOnAction(new EventHandler<ActionEvent>() {
           @Override 
           public void handle(ActionEvent t) {
               MainApp.dm.removeAll();
           }
       });
       item[4].setGraphic(getIcon("/icons/icons8-export-40.png"));
       item[4].setOnAction(new EventHandler<ActionEvent>() {
           @Override 
           public void handle(ActionEvent t) {
               MainApp.dm.exportAll();
           }
       });
       
       for(int i = 0; i < item.length; i++)
            menu.getItems().add(item[i]);
       
       return menu;
    }  
    
    public Pane createItem() throws IOException {
       FXMLLoader loader = new FXMLLoader();
       loader.setLocation(DownloaderItem.class.getResource("downloaderListItem.fxml"));
       root = loader.load();
       disableButton();
       root.setPadding(new Insets(3,3,3,3));
       final ContextMenu contextMenu = initContextMenu();
       root.setOnMouseClicked(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent t) {
               if (((MouseEvent)t).getButton().equals(MouseButton.SECONDARY))
                    contextMenu.show(root,((MouseEvent)t).getScreenX(),((MouseEvent)t).getScreenY());
           }
       });
       return root; //give manager a ref to the pane
    }
    
    public void downloadThis() {
        try {
            determineLink();
        } catch (SocketException e) {
            displayStatus(e.getMessage());
        } catch(SocketTimeoutException e) {
            displayStatus("Took too long to download page");
        } catch (IOException e) {
            displayStatus(e.getMessage());
        } catch (GenericDownloaderException e) {
            displayStatus(e.getMessage());
        } catch (Exception e) {
            displayStatus(e.getMessage());
        }
        if (downloadLinks != null)
            if (!downloadLinks.isEmpty())
                MainApp.dm.startDownload(this);
    }
    
    private void clearThis() {
        MainApp.dm.removeDownload(this);
        setDone();
        release();
    }
    
    private boolean isPureDigit(String s) {
        if (s.length() < 1) return false;
        for(int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i)))
                return false;
        return true;
    }
    
    boolean done;
    private boolean isFinished() {
        return done;
    }
    
    public void setDone() {
        done = true;
        updateSpeed(0);
        updateEta("00:00:00:00");
    }
    
    public void start() {
        disableButton();
        displayStatus("Downloading");
        done = false;
        OperationStream stream = new OperationStream();
        ExecutorService app = Executors.newSingleThreadExecutor();
        app.execute(new download(stream)); app.shutdown();
        while (!isFinished()) {
            String text = stream.getProgress();
            if (text != null) {
                if (isPureDigit(text))
                    updateProgressBar((float)Integer.parseInt(text)/100);
                else if (text.startsWith("^^"))
                    updateSpeed(Double.parseDouble(text.substring(2)));
                else if (text.startsWith("**"))
                    updateEta(text.substring(2));
                else displayStatus(text);
            }
        } app.shutdownNow();
        //displayStatus("Finished Downloading");
        enableButton();
        app = null;
    }
    
    private void determineLink() throws GenericDownloaderException, UncheckedIOException, Exception {
        if (extractor == null) extractor = getExtractor();
        MediaDefinition media = extractor.getVideo();
        downloadLinks = new Vector<>(); downloadNames = new Vector<>();
        if (!media.isSingleThread()) { //if more than one thread
            Map<String, String> m = new HashMap<>(); //to hold media link + name
            Iterator<Map<String,String>> i = media.iterator(); int j = 0;
            while(i.hasNext()) { //get qualities from threads
                Map<String,String> temp = i.next();
                if (temp.keySet().size() > 1) { //if more than one quality avaliable
                   QualityDialog d = new QualityDialog();
                    String quality = d.display(temp, media.getThreadName(0));
                    if (quality != null)
                        m.put(m.get(quality), media.getThreadName(j++));
                } else //get the single avaliable quality
                    m.put(temp.get(temp.keySet().iterator().next()), media.getThreadName(j++));
            }
            Iterator<String> k = m.keySet().iterator();
            while(k.hasNext()) { //download threads with chosen qualities
                String tempLink = k.next();
                downloadLinks.add(tempLink);
                downloadNames.add(m.get(tempLink));
                albumName = media.getAlbumName();
            }
        } else {
            Map<String,String> m = media.iterator().next();
            String link = null;
            if (m.keySet().size() > 1) { //if more than one quality avaliable
                QualityDialog d = new QualityDialog();
                String quality = d.display(m,media.getThreadName(0));
                if (quality != null)
                    link = m.get(quality);
            } else //get the single avaliable quality
                link = m.get(m.keySet().iterator().next());
            if (link != null) {
                downloadLinks.add(link);
                downloadNames.add(media.getThreadName(0));
            }
        }
    }
    
    private class download implements Runnable{
        OperationStream stream;

        public download(OperationStream s) {
            stream = s;
        }

        @Override
        public void run() {
            try {
                stream.startTiming();
                for(int i = 0; i < downloadLinks.size(); i++)
                    download(downloadLinks.get(i),downloadNames.get(i),stream,albumName);
                GameTime took = stream.endOperation();
                stream.addProgress("Took "+took.getTime()+" to download");
            } catch(Exception e){
                displayStatus(e.getMessage());
                e.printStackTrace();
            } finally { 
                System.out.println("done");
            }
            setDone();
        }
    }
    
    private void download(String link, String name, OperationStream s, String albumName) throws MalformedURLException {
        //if albumName == null not an album
        if (!CommonUtils.isImage(name))
            if (!CommonUtils.hasExtension(name, "mp4"))
                name = name + ".mp4"; //assume video
        File folder;
        if (CommonUtils.isImage(name)) folder = MainApp.settings.preferences.getPictureFolder(); 
        else folder = MainApp.settings.preferences.getVideoFolder();
        
        long stop = 0;
        do {
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(name));
            if (albumName != null)
                stop = CommonUtils.saveFile(link,CommonUtils.clean(name),folder+File.separator+albumName,s);
            else stop = CommonUtils.saveFile(link,CommonUtils.clean(name),folder,s);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                System.out.println("Failed to pause");
            }
        }while(stop != -2); //retry download if failed
        MainApp.createNotification("Download Success","Finished Downloading "+name);
        File saved;
        if (albumName != null)
            saved = new File(folder + File.separator + albumName + File.separator + CommonUtils.clean(name));
        else saved = new File(folder + File.separator + CommonUtils.clean(name));
        MainApp.downloadHistoryList.add(new downloadedMedia(CommonUtils.clean(name),extractor.getThumb(),saved,extractor.name()));
    }
    
    private void setSize(final long size) {
        Platform.runLater(new Runnable() {
           public void run() { 
                ((Label)root.lookup("#size")).setText(MainApp.getSizeText(size));
           }
        });
    }
    
    private void setSize() {
        size = 0;
        try {size = extractor.getSize();}catch(IOException | GenericDownloaderException e) {size = -1;}
        final long s = size;
        Platform.runLater(new Runnable() {
           public void run() { 
                ((Label)root.lookup("#size")).setText(MainApp.getSizeText(s));
           }
        });
    }
    
    private void displayStatus(String msg) {
        Platform.runLater(new Runnable() {
           public void run() { 
                ((Label)root.lookup("#downloadName")).setText(msg+" "+videoName);
           }
        });
    }
    
    private void updateSpeed(double speed) {
        Platform.runLater(new Runnable() {
           @Override public void run() {
               if (root != null) {
                    if (root.lookup("#speed") != null) {
                         if (speed > 1000)
                              ((Label)root.lookup("#speed")).setText(String.format("%.0f",speed/1000)+" mb/s");
                         else ((Label)root.lookup("#speed")).setText(String.format("%.0f",speed)+" kb/s");
                    }
               }
           }
        });
    }
    
    private void updateEta(String s) {
        Platform.runLater(new Runnable() {
           @Override public void run() {
               if (root != null) {
                    if (root.lookup("#eta") != null)
                         ((Label)root.lookup("#eta")).setText("ETA "+s);
               }
           }
        });
    }
    
    private void updateProgressBar(final float progress) {
        Platform.runLater(new Runnable() {
           @Override public void run() {
               if (root != null) 
                   if (root.lookup("#pBar") != null)
                        ((ProgressBar)root.lookup("#pBar")).setProgress(progress);
           }
        });
    }
    
    private void setIndeteminate(boolean enable) {
        Platform.runLater(new Runnable() {
           @Override public void run() {
               if (root != null) 
                   if (root.lookup("#pBar") != null)
                       if (enable)
                            ((ProgressBar)root.lookup("#pBar")).setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                       else ((ProgressBar)root.lookup("#pBar")).setProgress(0);
           }
        });
    }
}