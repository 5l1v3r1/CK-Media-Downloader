/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import ChrisPackage.GameTime;
import com.jfoenix.controls.JFXButton;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javax.imageio.ImageIO;

/**
 *
 * @author christopher
 */
public class DownloaderItem {
    private String url, albumName;
    private Pane root;
    private GenericExtractor extractor;
    private video v = null;
    private boolean loaded;
    private Vector<String> downloadNames;
    private Vector<MediaQuality> downloadLinks;
    
    public void release() {
        url = null;
        root = null;
        extractor = null;
        v = null;
        downloadLinks = null;
        albumName = null;
        downloadLinks = downloadLinks = null;
    }
    
    public String getName() {
        if(v != null)
            return v.getName();
        else return extractor.getVideoName();
    }
    
    public String getSite() {
        if (extractor != null)
            return extractor.getClass().getSimpleName();
        else return "";
    }
    
    public video getSide() throws GenericDownloaderException {
        try {
            if (extractor != null)
                return extractor.similar();
            else return null;
        } catch (UnsupportedOperationException | IOException e) {return null;}
    }
    
    public Vector<String> getKeywords() throws GenericDownloaderException {
        try {
            if (extractor != null)
                return extractor.getKeywords();
            else return null;
        } catch (UnsupportedOperationException | IOException e) {return null;}
    }
    
    public Vector<String> getStars() throws GenericDownloaderException {
        try {
            if (extractor != null)
                return extractor.getStars();
            else return null;
        } catch (UnsupportedOperationException | IOException e) {return null;}
    }
    
    private GenericExtractor getExtractor(video v) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        return v == null ? ExtractorList.getExtractor(url) : ExtractorList.getExtractor(v.getLink(),v.getThumbnail(),v.getName());
    }
    
    private GenericExtractor getExtractor() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        return getExtractor(null);
    }
    
    private boolean setFromVideo(ImageView view) {
        try {
            FileInputStream fis = new FileInputStream(v.getThumbnail());
            Image image = new Image(fis);
            if (fis != null) fis.close();
            view.setImage(image); return true;
        } catch (FileNotFoundException e) {
            MainApp.createMessageDialog("Couldnt find thumbnail to load it may have be deleted\n"+url);
            CommonUtils.log("Failed to load thumbnail",this);
            return false;
        } catch (IOException e) {
            MainApp.createMessageDialog("Failed to load thumbnail\n"+url);
            CommonUtils.log("Failed to load thumbnail: "+e.getMessage(),this);
            return false;
        } catch (Exception e) {
            MainApp.createMessageDialog("Failed to download thumbnail\n"+url+"\n"+e.getMessage());
            CommonUtils.log("Failed to download thumbnail: "+e.getMessage(),this);
            return false;
        }
    }
    
    private boolean setFromExtractor(ImageView view) {
        try {
            if(extractor == null) return false; //couldnt find extractor for link
            File thumbFile = extractor.getThumb();
            if (extractor.getClass().getName().equals("downloader.Extractors.Instagram"))
                view.preserveRatioProperty().setValue(true);
            if (thumbFile == null) return true; //no thumb //this really shouldnt be allowed but....
            //if more than 10mb (probably a large gif) image stream will run out of memory
            if (thumbFile.length() < MainApp.BYTE * MainApp.BYTE * 10) { 
                FileInputStream fis = new FileInputStream(thumbFile);
                Image image = new Image(fis);
                view.setImage(image);
            } else { //so read it as a still image (if is gif)
                BufferedImage b = ImageIO.read(thumbFile);
                view.setImage(SwingFXUtils.toFXImage(b, null));
            }
            return true;
        } catch (FileNotFoundException e) {
            MainApp.createMessageDialog("(2)Couldnt get thumb from link: \n"+url);
            CommonUtils.log("Failed to load thumbnail code: 2",this);
            e.printStackTrace();
            return false;
        } catch (UncheckedIOException e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            CommonUtils.log("Failed to download thumbnail (network)",this);
            return false;
        }catch(SocketTimeoutException e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            CommonUtils.log("Failed to download thumbnail (connection timeout)",this);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            CommonUtils.log("Failed to download thumbnail (some IO: network)",this);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
            CommonUtils.log("Failed to download thumbnail: "+e.getMessage(),this);
            return false;
        }
    }
    
    private boolean getThumbnail() {
        ImageView view = (ImageView)root.lookup("#thumb");
        return v == null ? setFromExtractor(view) : setFromVideo(view);
    }
    
    private void setStreamButton() {
        Button streamBtn = (Button)root.lookup("#streamBtn");
        streamBtn.setOnAction(event -> {
            new Thread(() -> {
                MainApp.dm.play(this);
            }).start();
        });
        streamBtn.setDisable(false);
    }
    
    private void setName() {
        Platform.runLater(() -> {
            Label temp = (Label)root.lookup("#downloadName");
            temp.setText(v != null ? v.getName() : extractor.getVideoName());
        });
    }
    
    private void setButton() {
        Button downloadBtn = (Button)root.lookup("#downloadBtn");
        downloadBtn.setOnAction(e -> downloadThis());
        downloadBtn.setDisable(false);
        //if is live stream change button icon and text to record
        if ((v == null) && (extractor.isLive())) { //by this point if one null the other cant be
            downloadBtn.setGraphic(CommonUtils.getIcon("/icons/icons8-record-48.png", 25, 25));
            downloadBtn.setText("Record");
        }
    }
    
    private void setCloseBtn() {
        JFXButton close = (JFXButton)root.lookup("#cancel");
        //close.getStyleClass().clear();
        close.getStyleClass().add("jfx-cancel");
        close.setOnAction(event -> {clearThis();});
        close.setDisable(false);
    }
    
    private void setSaveBtn() {
        Button btn = (Button)root.lookup("#save");
        btn.setOnAction(event -> {
            try {
                long size;
                if (v == null)
                    extractor = extractor == null ? getExtractor() : extractor;
                
                if ((extractor != null) && (extractor.isLive()))
                    MainApp.createMessageDialog("Cant save a live stream for later");
                else { 
                    size = v == null ? extractor.getSize() : v.getSize();
                    String name = v == null ? extractor.getVideoName() : v.getName();
                    File thumb = v == null ? extractor.getThumb() : v.getThumbnail();
                    String duration;
                    if (v == null) {
                        GameTime g = extractor.getDuration();
                        duration = g == null ? "----" : g.toString();
                    } else {
                        if (v.getDuration() == null)
                            duration = "----";
                        else duration = v.getDuration().equals("00") ? "----" : v.getDuration();
                    }
                    DataIO.saveVideo(new video(url, name, thumb, size, duration));
                    MainApp.createMessageDialog("Media saved");
                    MainApp.settings.videoUpdate();
                }
            } catch (IOException | GenericDownloaderException e) {
                MainApp.createMessageDialog("Failed to save video for later");
            } catch (Exception e) {
                MainApp.createMessageDialog("Failed to save video for later");
                CommonUtils.log("Failed to save video for later: "+e.getMessage(), this);
            }
        });
    }
    
    public boolean searchLink() throws GenericDownloaderException, IOException {       
        try { 
            setIndeteminate(true);
            if (v == null) {
                loaded = false;
                extractor = getExtractor();
                if (extractor == null) throw new Exception("couldnt get extractor"); //unsupported link
                setSize();
           } else {
        	loaded = true;
        	extractor = getExtractor(v);
                setSize(v.getSize());
            }
        } catch (Exception e) {
           if (e instanceof GenericDownloaderException) throw (GenericDownloaderException)e;
           e.printStackTrace();
           CommonUtils.log(e.getMessage(),this); release();
           return false;
        }
       
        if (extractor instanceof Playlist) {
            if (((Playlist)extractor).isPlaylist())
                ((Playlist)extractor).getItems().forEach((s) -> MainApp.dm.addDownload(s));
        }
       
        if (!getThumbnail()) {release(); return false;} //either link not supported or network error
       
        done = false;
        setButton();
        setSaveBtn();
        setName();
        setCloseBtn();
        setStreamButton();
        setDuration();
       
        CommonUtils.log("Found",this);
        setIndeteminate(false);
        return true; //if u made it this far process must be successful
    }
    
    public boolean wasLoaded() {
    	return loaded;
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
        disableButton(false);
    }
    
    private void disableButton(boolean b) {
        if (root != null) {
            Button downloadBtn = (Button)root.lookup("#downloadBtn");
            if (extractor != null && extractor.isLive() && b)
                downloadBtn.setText("Stop");    
            else downloadBtn.setDisable(true);
        }
    }
    
    private void enableButton() {
        if (root != null) {
            Button downloadBtn = (Button)root.lookup("#downloadBtn");
            if (extractor != null && extractor.isLive()) 
                downloadBtn.setText("Record");
            downloadBtn.setDisable(false);
        }
    }
    
    private ContextMenu initContextMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("background");
        final int iconSize = 20;
        final MenuItem[] item = new MenuItem[6];
        item[0] = new MenuItem("copy page url");
        item[1] = new MenuItem("copy video url");
        item[2] = new MenuItem("open in browser");
        item[3] = new MenuItem("remove");
        item[4] = new MenuItem("remove all");
        item[5] = new MenuItem("export links to file");
        
       
        item[0].setGraphic(CommonUtils.getIcon("/icons/icons8-copy-link-48.png", iconSize, iconSize));
        item[0].setOnAction((ActionEvent) -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(url), new StringSelection(MainApp.username));
        });
        item[1].setGraphic(CommonUtils.getIcon("/icons/icons8-copy-link-48.png", iconSize, iconSize));
        item[1].setOnAction((ActionEvent) -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(new StringSelection(getStreamLink()), new StringSelection(MainApp.username));
            } catch (Exception e) {
                MainApp.createMessageDialog(e.getMessage());
            }
        });
        item[2].setGraphic(CommonUtils.getIcon("/icons/icons8-open-in-browser-40.png", iconSize, iconSize));
        item[2].setOnAction((ActionEvent) -> {
            if (Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(new URI(url));
                    } catch (URISyntaxException ex) {
                        CommonUtils.log("Bad Uri",this);
                    } catch (IOException ex) {
                        MainApp.createMessageDialog("Failed to load");
                    }
                }).start();    
            } else
                MainApp.createMessageDialog("Not supported");
        });
        item[3].setGraphic(CommonUtils.getIcon("/icons/icons8-cancel-40.png", iconSize, iconSize));
        item[3].setOnAction((ActionEvent) -> {
            clearThis();
        });
        item[4].setGraphic(CommonUtils.getIcon("/icons/icons8-cancel-40.png", iconSize, iconSize));
        item[4].setOnAction((ActionEvent) -> {
            MainApp.dm.removeAll();
        });
        item[5].setGraphic(CommonUtils.getIcon("/icons/icons8-export-40.png", iconSize, iconSize));
        item[5].setOnAction((ActionEvent) -> {
            MainApp.dm.exportAll();
        });
       
        menu.getItems().addAll(Arrays.asList(item));
       
        return menu;
    }  
    
    public Pane createItem() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(DownloaderItem.class.getResource("downloaderListItem.fxml"));
        root = loader.load();
        disableButton();
        root.setPadding(new Insets(3,3,3,3));
        final ContextMenu contextMenu = initContextMenu();
        root.setOnMouseClicked((MouseEvent t) -> {
            if (((MouseEvent)t).getButton().equals(MouseButton.SECONDARY))
                contextMenu.show(root,((MouseEvent)t).getScreenX(),((MouseEvent)t).getScreenY());
        });
        return root; //give manager a ref to the pane
    }
    
    private void downloadThis() {
        try {
            determineLink();
            if (downloadLinks != null)
                if (!downloadLinks.isEmpty())
                    MainApp.dm.startDownload(this);
        } catch (SocketException e) {
            displayStatus(e.getMessage());
        } catch(SocketTimeoutException e) {
            displayStatus("Took too long to download page");
        } catch (IOException e) {
            displayStatus(e.getMessage());
        } catch (GenericDownloaderException e) {
            displayStatus(e.getMessage());
            MainApp.createMessageDialog(e.getMessage());
        } catch (Exception e) {
            displayStatus(e.getMessage());
        }
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
        setDone(false);
    }
    
    public void setDone(boolean live) {
        done = true;
        updateSpeed(0);
        if (!live)
            updateEta("00:00:00:00");
    }
    
    public void start() {
        disableButton(true);
        boolean live = extractor.isLive();
        displayStatus(!live ? "Downloading" : "Recording");
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
                    updateEta(text.substring(2), live);
                else displayStatus(text);
            }
        }
        try { app.awaitTermination(2, TimeUnit.SECONDS); } catch(InterruptedException e) { CommonUtils.log(e.getMessage(), this);}
        app.shutdownNow();
        //displayStatus("Finished Downloading");
        enableButton();
    }
    
    public String getStreamLink() throws GenericDownloaderException, UncheckedIOException, Exception {
        if (extractor == null) extractor = getExtractor();
        Map<String, MediaQuality> m = extractor.getVideo().iterator().next();
        String highestQuality = m.keySet().size() == 1 ? m.keySet().iterator().next() : CommonUtils.getSortedFormats(m.keySet()).get(0);
        return m.get(highestQuality).getUrl();
    }
    
    private void determineLink() throws GenericDownloaderException, UncheckedIOException, Exception {
        if (extractor == null) extractor = getExtractor();
        MediaDefinition media = extractor.getVideo();
        downloadLinks = new Vector<>(); downloadNames = new Vector<>();
        if (!media.isSingleThread()) { //if more than one thread
            Map<MediaQuality, String> m = new HashMap<>(); //to hold media link + name
            Iterator<Map<String, MediaQuality>> i = media.iterator(); int j = 0;
            while(i.hasNext()) { //get qualities from threads
                Map<String, MediaQuality> temp = i.next();
                if (temp.keySet().size() > 1) { //if more than one quality avaliable
                   QualityDialog d = new QualityDialog();
                    String quality = d.display(temp, media.getThreadName(0), extractor.getCookies());
                    if (quality != null)
                        m.put(temp.get(quality), media.getThreadName(j++));
                } else //get the single avaliable quality
                    m.put(temp.get(temp.keySet().iterator().next()), media.getThreadName(j++));
            }
            Iterator<MediaQuality> k = m.keySet().iterator();
            downloadLinks.ensureCapacity(m.keySet().size());
            downloadNames.ensureCapacity(m.keySet().size());
            while(k.hasNext()) { //download threads with chosen qualities
                MediaQuality n = k.next();
                downloadLinks.add(n);
                downloadNames.add(m.get(n));
                albumName = media.getAlbumName();
            }
        } else {
            Map<String, MediaQuality> m = media.iterator().next();
            MediaQuality link = null;
            if (m.keySet().size() > 1) { //if more than one quality avaliable
                QualityDialog d = new QualityDialog();
                String quality = d.display(m,media.getThreadName(0), extractor.getCookies());
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

        @Override public void run() {
            try {
                stream.startTiming();
                for(int i = 0; i < downloadLinks.size(); i++)
                    download(downloadLinks.get(i),downloadNames.get(i),stream,albumName);
                GameTime took = stream.endOperation();
                stream.addProgress("Took "+took.getTime()+" to download");
            } catch(MalformedURLException e){
                displayStatus(e.getMessage());
                CommonUtils.log(e.getMessage(),this);
                e.printStackTrace();
            } catch (Exception e) {
                displayStatus(e.getMessage());
                CommonUtils.log(e.getMessage(), this);
                e.printStackTrace();
            } finally { 
                CommonUtils.log("done",this);
            }
            setDone(downloadLinks.get(0).isLive());
        }
    }
    
    private void download(MediaQuality q, String name, OperationStream s, String albumName) throws MalformedURLException {
        //if albumName == null not an album
        File saved = (CommonUtils.isStreamType(q.getType()) || q.isLive()) ? 
                downloadFFmpeg(q, name, s, albumName) : downloadLocal(q, name, s, albumName);
        if (saved != null)
            MainApp.downloadHistoryList.add(new downloadedMedia(CommonUtils.clean(name),extractor.getThumb(),saved,extractor.getClass().getSimpleName(), CommonUtils.getCurrentTimeStamp()));
    }
    
    private File downloadLocal(MediaQuality q, String name, OperationStream s, String albumName) throws MalformedURLException {
        if (!CommonUtils.hasExtension(name, q.getType()))
            name = name + "." + q.getType();
        name = CommonUtils.clean(CommonUtils.addId(name, extractor.getId()));
        File folder = CommonUtils.isImage(name) ? MainApp.settings.preferences.getPictureFolder() : MainApp.settings.preferences.getVideoFolder();
        
        long stop;
        int retries = 15; //approx a min if fails instant
        do {
            if (retries < 1) break;
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(name));
            stop = albumName != null ? CommonUtils.saveFile(q.getUrl(), name,folder+File.separator+albumName,s, extractor.getCookies()) : CommonUtils.saveFile(q.getUrl(), name, folder,s,  extractor.getCookies());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                CommonUtils.log("Failed to pause: "+ex.getMessage(),this);
            }
            retries--;
        }while(stop != -2); //retry download if failed
        MainApp.createNotification("Download Success","Finished Downloading "+name);
        return new File(folder + File.separator + (albumName != null ? albumName + File.separator : "") + name);
    }
    
    private File downloadFFmpeg(MediaQuality q, String name, OperationStream s, String albumName) throws MalformedURLException {
        //to download videos/livestreams coming from hls/m3u8 playlists
        File out = new File(MainApp.settings.preferences.getVideoFolder().getAbsolutePath() + (albumName != null ? File.separator + albumName : ""));
        
        FFmpeg ffmpeg = new FFmpeg();
        ffmpeg.setOutDir(out);
        ffmpeg.setOutput(CommonUtils.addId(CommonUtils.clean(name) + ".mp4", extractor.getId()));
        ffmpeg.setInput(q.getUrl());
        //ffmpeg.copy(q.getType().equals("mp4") || CommonUtils.isStreamType(q.getType()));
        
        try {
            int code; 
            if (q.isLive()) {
                Button d = (Button)root.lookup("#downloadBtn");
                d.setOnAction(e -> ffmpeg.stop());
                setIndeteminate(true);
                code = ffmpeg.run(s, true); //wait till finished or stop signal sent
                d.setOnAction(e -> downloadThis());
                setIndeteminate(false);
            } else code = ffmpeg.run(s); //wait till finished
            if (code == 0)
                MainApp.createNotification("Download Success","Finished Downloading "+name);
            else CommonUtils.log(name+" Finished with "+code+" as code", this);
            return new File(out + File.separator + name + ".mp4");
        } catch (IOException | InterruptedException e) {
            CommonUtils.log(e.getMessage(), this);
            return null;
        }
    }
    
    private void setSize(final long size) {
        Platform.runLater(() -> {
            ((Label)root.lookup("#size")).setText(MainApp.getSizeText(size));
        });
    }
    
    private void setSize() {
        long size;
        try {size = v == null ? extractor.getSize() : v.getSize(); }catch(IOException | GenericDownloaderException e) {size = -1;}
        final long s = size;
        Platform.runLater(() -> {
            ((Label)root.lookup("#size")).setText(MainApp.getSizeText(s));
        });
    }
    
    private void setDuration() {
        String duration;
        try {
            String g;
            if (v == null) {
                GameTime t = extractor.getDuration();
                g = t == null ? null : t.toString();
            } else 
                g = v.getDuration();
            duration = g == null || g.equals("00") ? "----" : g;
            duration = duration.length() < 3 ? duration + " secs" : duration;
        } catch (IOException | GenericDownloaderException e) {
            CommonUtils.log(e.getMessage(), this);
            duration = "----";
        }
        final String d = duration;
        Platform.runLater(() -> {
            ((Label)root.lookup("#duration")).setText("Duration "+d);
        });
    }
    
    private void displayStatus(String msg) {
        Platform.runLater(() -> {
            String vidName = v != null ? v.getName() : extractor.getVideoName();
            ((Label)root.lookup("#downloadName")).setText(msg+" "+vidName);
        });
    }
    
    private void updateSpeed(double speed) {
        Platform.runLater(() -> {
            if (root != null) {
                if (root.lookup("#speed") != null) {
                    if (speed > MainApp.BYTE)
                        ((Label)root.lookup("#speed")).setText(String.format("%.0f",speed/MainApp.BYTE)+" mb/s");
                    else ((Label)root.lookup("#speed")).setText(String.format("%.0f",speed)+" kb/s");
                }
            }
        });
    }
    
    boolean set = false;
    private void updateEta(String s, boolean live) {
        Platform.runLater(() -> {
            if (root != null) {
                if (!set) {
                    Image image = new Image(System.class.getResourceAsStream("/icons/icons8-submit-progress-480.png"));
                    ((ImageView)root.lookup("#timeIcon")).setImage(image);
                    set = true;
                }
                if (root.lookup("#duration") != null) {
                    if (!live)
                        ((Label)root.lookup("#duration")).setText("ETA "+s);
                    else ((Label)root.lookup("#duration")).setText("Recorded "+s);
                }
            }
        });
    }
    
    private void updateEta(String s) {
        updateEta(s, false);
    }
    
    private void updateProgressBar(final float progress) {
        Platform.runLater(() -> {
            if (root != null)
                if (root.lookup("#pBar") != null)
                    ((ProgressBar)root.lookup("#pBar")).setProgress(progress);
        });
    }
    
    private void setIndeteminate(boolean enable) {
        Platform.runLater(() -> {
            if (root != null)
                if (root.lookup("#pBar") != null)
                    if (enable)
                        ((ProgressBar)root.lookup("#pBar")).setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                    else ((ProgressBar)root.lookup("#pBar")).setProgress(0);
        });
    }
}