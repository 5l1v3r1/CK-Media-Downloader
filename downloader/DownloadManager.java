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
    private final ExecutorService downloadThreads, sideJobs;
    //private final int threads = 3;
    private final int sideThreads = 4, TIMES = 1;
    private StreamManager streamer;
    private ListView<Pane> downloadsView;
    
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
            streamer.stop();
            streamer = null;
            CommonUtils.log("Attempted to release download Manager",this);
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
    
    public void reserve(int i) {
        ((ArrayList)downloadItems).ensureCapacity(i);
    }
    
    public void changeTheme(boolean enable) {
        for(int i = 0; i < downloadsView.getItems().size(); i++) {
            if ((downloadsView.getItems().get(i)).getStylesheets() != null) (downloadsView.getItems().get(i)).getStylesheets().clear();
            if(enable)
                (downloadsView.getItems().get(i)).getStylesheets().add(MainApp.class.getResource("layouts/darkPane.css").toExternalForm());
            else (downloadsView.getItems().get(i)).getStylesheets().add(MainApp.class.getResource("layouts/normal.css").toExternalForm());
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
                    downloadItems.remove(i);
                    downloadsView.getItems().remove(i);
                }
            }
        });
    }
    
    public void removeAll() {
        downloadItems.forEach((d) -> {
            removeDownload(d);
        });
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
                streamer.setMedia(streamLink, d.getName());
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
        private DownloaderItem d;
        
        public search(DownloaderItem d) {
            this.d = d;
        }
        
        @Override public void run() {
            CommonUtils.log("Searching link: "+d.getLink(),this);
            try {
                if(!d.searchLink()) {
                    //MainApp.createMessageDialog("Error with: "+d.getLink());
                    removeDownload(d);
                } else { //wasnt loaded
                    try {
                        if (!d.wasLoaded()) {
                            int stop = new Random().nextInt(TIMES) + 1;
                            for(int i = 0; i < stop; i++) {
                                MainApp.log(d.getName(),d.getSite()); //generate suggestion from search algo with provided info
                                int result, count = 8;
                                do { //add similar video as suggestion
                                    if(!MainApp.active) break;
                                    result = MainApp.log(d.getSide());
                                    if (result == 2)
                                        count = count > 3 ? 3 : count;
                                    else if (result == 1)
                                        count = count > 2 ? 2 : count;
                                    CommonUtils.log("result: "+result, this);
                                    CommonUtils.log("times "+count, this);
                                    if (count-- < 0) break;
                                } while(result != 0); //if video was already there repeat
                                if(!MainApp.active) break;
                            }
                        }
                    } catch (GenericDownloaderException e) {
                        CommonUtils.log("Couldnt load side &&|| search",this);
                        CommonUtils.log(e.getMessage(),this);
                    } catch (Exception e) {
                        CommonUtils.log(e.getMessage(), "search:innerExeption");
                        //dont remove download if it has it only because of search || side
                    }
                }
            } catch (GenericDownloaderException | IOException e) {
                if (e instanceof NotSupportedException)
                    CommonUtils.log(((NotSupportedException)e).getMessage()+" "+((NotSupportedException)e).getUrl(),this);
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
