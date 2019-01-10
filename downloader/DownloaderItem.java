/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import com.jfoenix.controls.JFXButton;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Extractors.*;
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
import java.net.SocketException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
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

/**
 *
 * @author christopher
 */
public class DownloaderItem {
    private String url, videoName;
    private Pane root;
    private GenericExtractor extractor;
    private Site.Type type;
    private video v = null;
    private File thumbFile;
    private boolean loaded;
    
    public void release() {
        root = null;
        extractor = null;
        thumbFile = null;
        v = null;
    }
    
    public String getName() {
        return videoName;
    }
    
    public String getSite() {
        if (extractor != null)
            return extractor.name();
        else return null;
    }
    
    public video getSide() {
        try {
            return extractor.similar();
        } catch (UnsupportedOperationException | IOException e) {return null;}
    }
    
    private GenericExtractor getExtractor(video v) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
    	if (null == type)
            return null;
        else switch (type) {
            case spankbang:
            	if (v == null)
            		return new SpankBang(url);
            	else return new SpankBang(url,v.getThumbnail(),v.getName());
            case pornhub:
            	if (v == null)
            		return new Pornhub(url);
            	else return new Pornhub(url,v.getThumbnail(),v.getName());
            case xhamster:
            	if (v == null)
            		return new Xhamster(url); 
            	else return new Xhamster(url,v.getThumbnail(),v.getName());
            case xvideos:
            	if (v == null)
            		return new Xvideos(url);
            	else return new Xvideos(url,v.getThumbnail(),v.getName());
            case xnxx:
            	if (v == null)
            		return new Xvideos(url); //xnxx shares the same setup so they use the extractor
            	else return new Xvideos(url,v.getThumbnail(),v.getName());
            case youporn:
            	if (v == null)
            		return new Youporn(url);
            	else return new Youporn(url,v.getThumbnail(),v.getName());
            case redtube:
            	if (v == null)
            		return new Redtube(url);
            	else return new Redtube(url,v.getThumbnail(),v.getName());
            case thumbzilla:
            	if (v == null)
            		return new Thumbzilla(url);
            	else return new Thumbzilla(url,v.getThumbnail(),v.getName());
            case shesfreaky:
            	if (v == null)
            		return new Shesfreaky(url);
            	else return new Shesfreaky(url,v.getThumbnail(),v.getName());
            case instagram:
            	if (v == null)
            		return new Instagram(url);
            	else return new Instagram(url,v.getThumbnail(),v.getName());
            case yourporn:
            	if (v == null)
            		return new Yourporn(url);
            	else return new Yourporn(url,v.getThumbnail(),v.getName());
            case bigtits:
            	if (v == null)
                	return new Bigtits(url);
            	else return new Bigtits(url,v.getThumbnail(),v.getName());
            case pornhd:
            	if (v == null)
            		return new Pornhd(url);
            	else return new Pornhd(url,v.getThumbnail(),v.getName());
            case vporn:
            	if (v == null)
            		return new Vporn(url);
            	else return new Vporn(url,v.getThumbnail(),v.getName());
            case ghettotube:
            	if (v == null)
            		return new Ghettotube(url);
            	else return new Ghettotube(url,v.getThumbnail(),v.getName());
            case tube8:
            	if (v == null)
            		return new Tube8(url);
            	else return new Tube8(url,v.getThumbnail(),v.getName());
            case watchenga:
            	if (v == null)
            		return new Watchenga(url);
            	else return new Watchenga(url,v.getThumbnail(),v.getName());
            case youjizz:
            	if (v == null)
            		return new Youjizz(url);
            	else return new Youjizz(url,v.getThumbnail(),v.getName());
            case xtube:
            	if (v == null)
            		return new Xtube(url);
            	else return new Xtube(url,v.getThumbnail(),v.getName());
            case spankwire:
            	if (v == null)
            		return new Spankwire(url);
            	else return new Spankwire(url,v.getThumbnail(),v.getName());
            case justporno:
            	if (v == null)
            		return new Justporno(url);
            	else return new Justporno(url,v.getThumbnail(),v.getName());
            case bigbootytube:
            	if (v == null)
            		return new Bigbootytube(url);
            	else return new Bigbootytube(url,v.getThumbnail(),v.getName());
            case befuck:
            	if (v == null)
            		return new Befuck(url); 
            	else return new Befuck(url,v.getThumbnail(),v.getName());
            case dailymotion:
            	if (v == null)
            		return new Dailymotion(url);
            	else return new Dailymotion(url,v.getThumbnail(),v.getName());
            case vimeo:
            	if (v == null)
            		return new Vimeo(url);
            	else return new Vimeo(url,v.getThumbnail(),v.getName());
            case cumlouder:
            	if (v == null)
            		return new Cumlouder(url);
            	else return new Cumlouder(url,v.getThumbnail(),v.getName());
            case ruleporn:
            	if (v == null)
            		return new Ruleporn(url);
            	else return new Ruleporn(url,v.getThumbnail(),v.getName());
            default:
                return null;
        }
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
                long size;
                try {if (extractor == null) extractor = getExtractor(); size = extractor.getSize();}catch(Exception e){size = -1;}
                DataIO.saveVideo(new video(url,extractor.getVideoName(),extractor.getThumb(),size));
                MainApp.createMessageDialog("Media saved");
                MainApp.settings.videoUpdate();
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to save video for later");
            }
        });
    }
    
    public boolean searchLink() throws GenericDownloaderException {       
       try { 
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
           System.out.println(e.getMessage()); release();
           return false;
       }
       
       if (!getThumbnail()) {release(); return false;} //either link not supported or network error
       
       setButton();
       setCloseBtn();
       setSaveBtn();
       setName();
       
       System.out.println("Found");
       return true; //if u made it this far process must be successful
    }
    
    public boolean wasLoaded() {
    	return loaded;
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
    
    private class download implements Runnable{
        OperationStream stream;

        public download(OperationStream s) {
            stream = s;
        }

        @Override
        public void run() {
            try {
                if (extractor == null) extractor = getExtractor();
                extractor.getVideo(stream);
            } catch (SocketException e) {
                displayStatus(e.getMessage());
            }catch(SocketTimeoutException e) {
                displayStatus("Took too long to download page");
            }catch (IOException e) {
                displayStatus(e.getMessage());
            } catch (GenericDownloaderException e) {
                displayStatus(e.getMessage());
            } catch(Exception e){
                displayStatus(e.getMessage());
                e.printStackTrace();
            } finally { 
                System.out.println("done");
            }
            setDone();
        }
    }
    
    private void setSize(final long size) {
        Platform.runLater(new Runnable() {
           public void run() { 
                ((Label)root.lookup("#size")).setText(MainApp.getSizeText(size));
           }
        });
    }
    
    private void setSize() {
        long size = 0;
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
           public void run() {
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
           public void run() {
               if (root != null) {
                    if (root.lookup("#eta") != null)
                         ((Label)root.lookup("#eta")).setText("ETA "+s);
               }
           }
        });
    }
    
    private void updateProgressBar(final float progress) {
        Platform.runLater(new Runnable() {
           public void run() {
               if (root != null) 
                   if (root.lookup("#pBar") != null)
                        ((ProgressBar)root.lookup("#pBar")).setProgress(progress);
           }
        });
    }
}
