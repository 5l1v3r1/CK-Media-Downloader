/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.NotSupportedException;
import downloaderProject.MainApp;
import downloaderProject.StreamManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import org.jsoup.UncheckedIOException;

/**
 *
 * @author christopher
 */
public class DownloadManager {
    private List<DownloaderItem> downloadItems = new ArrayList<>();//list of downloader items that create panes
    private final ExecutorService downloadThreads;
    private final ExecutorService sideJobs;
    //private final int threads = 3;
    private final int sideThreads = 4;
    private StreamManager streamer;
    private ListView downloadsView;
    
    public void release() {
        try {
            downloadThreads.shutdown();
            downloadThreads.awaitTermination(2, TimeUnit.SECONDS);
            downloadThreads.shutdownNow();
            sideJobs.shutdown();
            sideJobs.awaitTermination(2, TimeUnit.SECONDS);
            sideJobs.shutdownNow();
            removeAll();
            downloadsView.getItems().clear();
            downloadItems = null;
        } catch (InterruptedException ex) {
            CommonUtils.log("Failed to stop download threads",this);
        }
    }
    
    public DownloadManager() {
        downloadThreads = Executors.newCachedThreadPool(); //threads for downloading are set to limit # of simultaneous downloads
        sideJobs = Executors.newFixedThreadPool(sideThreads); //fixed amount mineial tasks
    }
    
    public void setDownloadList(ListView<Pane> d) {
        this.downloadsView = d;
    }
    
    public void setStreamer(StreamManager s) {
        this.streamer = s;
    }
    
    public void changeTheme(boolean enable) {
        for(int i = 0; i < downloadsView.getItems().size(); i++) {
            if (((Pane)downloadsView.getItems().get(i)).getStylesheets() != null) ((Pane)downloadsView.getItems().get(i)).getStylesheets().clear();
            if(enable)
                ((Pane)downloadsView.getItems().get(i)).getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else ((Pane)downloadsView.getItems().get(i)).getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
        
        if (downloadsView != null) {
            if (downloadsView.getStylesheets() != null) downloadsView.getStylesheets().clear();
            if (enable)
                downloadsView.getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else downloadsView.getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
        }
    }
    
    private synchronized void addDownload(DownloaderItem d) {
        Platform.runLater(() -> {
            if(!isDup(d)) {
                display(d);
                sideJobs.execute(new search(d));
                System.gc();
            }
        });
    }
    
    public synchronized void addDownload(String link) {
        DownloaderItem download = new DownloaderItem();
        download.setLink(link); download.setVideo(null);
        addDownload(download);
    }
    
    public synchronized void addDownload(String link, video v) {
        DownloaderItem download = new DownloaderItem();
        download.setLink(link); download.setVideo(v);
        addDownload(download);
    }
    
    public synchronized void addDownload(String page, Site.Page type) {
        DownloaderItem download = new DownloaderItem();
        download.setPage(page); download.setPageType(type);
        addDownload(download);
    }
    
    private boolean isDup(DownloaderItem d) {
        for(int i = 0; i < downloadItems.size(); i++)
            if (ExtractorList.similar(downloadItems.get(i).getLink(),d.getLink()))
                return true;
        return false;
    }
    
    private void display(DownloaderItem d) {
        try {
            downloadItems.add(d);
            downloadsView.getItems().add(d.createItem());
        } catch (IOException e) {
            MainApp.createMessageDialog("An internal error occurred: 1");
        }
    }
    
    public void startDownload(DownloaderItem who) {
        downloadThreads.execute(new manager(who));
    }
    
    public void removeDownload(DownloaderItem which) {
        Platform.runLater(() -> {
            if (downloadItems != null) {
                int i = downloadItems.indexOf(which);
                if (i != -1) {
                    downloadItems.get(i).setDone();
                    downloadItems.remove(i);
                    downloadsView.getItems().remove(i);
                }
            }
        });
    }
    
    public void removeAll() {
        for(DownloaderItem d: downloadItems) {
            removeDownload(d);
            d.release();
        }
    }
    
    public void exportAll() {
        DirectoryChooser save = new DirectoryChooser();
        save.setTitle("Select a folder export links to");
        if((MainApp.settings.preferences.getImportFolder() != null) && (MainApp.settings.preferences.getImportFolder().exists() && (MainApp.settings.preferences.getImportFolder().isDirectory())))
            save.setInitialDirectory(MainApp.settings.preferences.getImportFolder());
        File selected = save.showDialog(null);
        if(selected != null) {
            try {
                Formatter writter = new Formatter(new File(selected.getAbsolutePath() + File.separator + "urls.txt"));
                for(int i = 0; i < downloadItems.size(); i++)
                    writter.format("%s\n",downloadItems.get(i).getLink());
                writter.flush();
                writter.close();
                MainApp.createMessageDialog("Links exported successfully");
            } catch (FileNotFoundException e) {
                MainApp.createMessageDialog("File not found");
            }
        }
    }
    
    public void play(DownloaderItem d) {
        String streamLink = null;
        try {
            streamLink = d.getStreamLink();
            if(streamLink == null || streamLink.isEmpty())
                MainApp.createMessageDialog("Error with stream link");
            else
                streamer.setMedia(streamLink);
        } catch (GenericDownloaderException | UncheckedIOException e) {
            CommonUtils.log(e.getMessage(),this);
            MainApp.createMessageDialog(e.getMessage());
        } catch(MalformedURLException | URISyntaxException e) {
           CommonUtils.log(streamLink+" failed",this);
           MainApp.createMessageDialog(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private class search implements Runnable {
        DownloaderItem d;
        
        public search(DownloaderItem d) {
            this.d = d;
        }
        
        @Override public void run() {
            CommonUtils.log("Searching link",this);
            try {
                if(!d.searchLink()) {
                    //MainApp.createMessageDialog("Error with: "+d.getLink());
                    removeDownload(d);
                } else { 
                    //wasnt loaded
                    try {
                        if (!d.wasLoaded()) {
                            int stop = new Random().nextInt(3) + 1;
                            for(int i = 0; i < stop; i++) {
                                MainApp.log(d.getName(),d.getSite());
                                MainApp.log(d.getSide());
                            }
                        }
                    } catch (GenericDownloaderException e) {
                        CommonUtils.log("Couldnt load side &&|| search",this);
                        CommonUtils.log(e.getMessage(),this);
                    } catch (Exception e) {
                        CommonUtils.log(e.getMessage(),this);
                        //dont remove download if it has it only because of search || side
                    }
                }
            } catch (GenericDownloaderException | IOException e) {
                if (e instanceof NotSupportedException)
                    CommonUtils.log(((NotSupportedException)e).getMessage()+" "+((NotSupportedException)e).getUrl());
                else MainApp.createMessageDialog(e.getMessage()+ " "+ d.getLink());
                removeDownload(d);
            } catch (Exception e) {
                MainApp.createMessageDialog(e.getMessage()+ " "+ d.getLink());
                removeDownload(d);
                e.printStackTrace();
            }
        }
    }
    
    private class manager implements Runnable{
        DownloaderItem item;
        
        public manager(DownloaderItem what) {
            this.item = what;
        }
        
        @Override public void run() {
            item.start();
        }
    }
}
