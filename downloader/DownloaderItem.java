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
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.SocketException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author christopher
 */
public class DownloaderItem {
    private String url, videoName;
    private Pane item;
    private ObservableList<Node> paneElems;
    private GenericExtractor extractor;
    private Site.Type type;
    private video v = null;
    private File thumbFile;
    
    public void release() {
        item = null;
        paneElems = null;
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
    
    private GenericExtractor getExtractor() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (null == type)
            return null;
        else switch (type) {
            case spankbang:
                return new SpankBang(url);
            case pornhub:
                return new Pornhub(url);
            case xhamster:
                return new Xhamster(url); 
            case xvideos:
                return new Xvideos(url);
            case xnxx:
                return new Xvideos(url); //xnxx shares the same setup so they use the extractor
            case youporn:
                return new Youporn(url);
            case redtube:
                return new Redtube(url);
            case thumbzilla:
                return new Thumbzilla(url);
            case shesfreaky:
                return new Shesfreaky(url);
            case instagram:
                return new Instagram(url);
            case yourporn:
                return new Yourporn(url);
            case bigtits:
                return new Bigtits(url);
            case pornhd:
                return new Pornhd(url);
            case vporn:
                return new Vporn(url);
            case ghettotube:
                return new Ghettotube(url);
            case tube8:
                return new Tube8(url);
            case watchenga:
                return new Watchenga(url);
            case youjizz:
                return new Youjizz(url);
            case xtube:
                return new Xtube(url);
            case spankwire:
                return new Spankwire(url);
            case justporno:
                return new Justporno(url);
            case bigbootytube:
                return new Bigbootytube(url);
            case befuck:
                return new Befuck(url); 
            case dailymotion:
                return new Dailymotion(url);
            case vimeo:
                return new Vimeo(url);
            case cumlouder:
                return new Cumlouder(url);
            case ruleporn:
                return new Ruleporn(url);
            default:
                return null;
        } 
    }
    
    private ImageView getThumbnail(ImageView view) {
        if (v == null) {
            try {
                extractor = getExtractor();
                if(extractor == null) return null;
                thumbFile = extractor.getThumb();
                videoName = extractor.getVideoName();
                FileInputStream fis = new FileInputStream(thumbFile);
                Image image = new Image(fis);
                if (fis != null) fis.close();
                view.setImage(image);
            } catch (FileNotFoundException e) {
                MainApp.createMessageDialog("Failed to load thumbnail code:2\n"+url);
                System.out.println("Failed to load thumbnail code:2");
                return null;
            } catch (UncheckedIOException e) {
                e.printStackTrace();
                MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
                System.out.println("Failed to download thumbnail");
                return null;
            }catch(SocketTimeoutException e) {
                e.printStackTrace();
                MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
                System.out.println("Failed to download thumbnail");
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
                System.out.println("Failed to download thumbnail");
                return null;
            }catch (GenericDownloaderException e) {
                MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
                System.out.println("Failed to download thumbnail");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                MainApp.createMessageDialog("Failed to download thumbnail: "+e.getMessage()+"\n"+url);
                System.out.println("Failed to download thumbnail");
                return null;
            }
        } else {
            try {
                videoName = v.getName();
                thumbFile = v.getThumbnail();
                FileInputStream fis = new FileInputStream(v.getThumbnail());
                Image image = new Image(fis);
                if (fis != null) fis.close();
                view.setImage(image);
            } catch (FileNotFoundException e) {
                MainApp.createMessageDialog("Failed to load thumbnail may have be deleted\n"+url);
                System.out.println("Failed to load thumbnail");
                return null;
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to load thumbnail"+"\n"+url);
                System.out.println("Failed to load thumbnail");
                return null;
            } catch (Exception e) {
               MainApp.createMessageDialog("Failed to download thumbnail"+"\n"+url);
                System.out.println("Failed to download thumbnail");
                return null;
            }
        }
        view.setFitHeight(77);
        view.setFitWidth(77);
        return view;
    }
    
    private Label getName(Label temp) {
        temp.setText(videoName);
        return temp;
    }
    
    private Button getButton(Button downloadBtn) {
        downloadBtn.setOnAction(event -> {downloadThis();});
        downloadBtn.setDisable(false);
        return downloadBtn;
    }
    
    private JFXButton getCloseBtn(JFXButton close) {
        close.setOnAction(event -> {clearThis();});
        close.setDisable(false);
        return close;
    }
    
    private void getSaveBtn(Button btn) {
        btn.setOnAction(event -> {
            try {
                DataIO.saveVideo(new video(url,videoName,thumbFile));
                MainApp.createMessageDialog("Media saved");
                MainApp.settings.videoUpdate();
            } catch (IOException e) {
                MainApp.createMessageDialog("Failed to save video for later");
            }
        });
    }
    
    public boolean searchLink() {
       paneElems = item.getChildren();
       ImageView image =(ImageView)item.lookup("#thumb");
       Label name = (Label)item.lookup("#downloadName");
       Button down = (Button)item.lookup("#downloadBtn");
       Button saveLater = (Button)item.lookup("#save");
       JFXButton close = (JFXButton)item.lookup("#cancel");

       image = getThumbnail(image);
       if (image == null) return false; //either link not supported or network error
       down = getButton(down);
       getCloseBtn(close);
       getSaveBtn(saveLater);
       
       final ImageView finalImage = image;
       final Label finalName = name;
       final Button finalButton = down, save = saveLater;
       Platform.runLater(new Runnable() {
           @Override public void run() {
                int thumb = paneElems.indexOf((ImageView)item.lookup("#thumb"));
                paneElems.set(thumb, finalImage);
                int itemname = paneElems.indexOf((Label)item.lookup("#downloadName"));
                paneElems.set(itemname, getName(finalName));
                int itembtn = paneElems.indexOf((Button)item.lookup("#downloadBtn"));
                paneElems.set(itembtn, finalButton);
                int saveBtn = paneElems.indexOf((Button)item.lookup("#save"));
                paneElems.set(saveBtn, save);
                System.out.println("Found");
        }}); //post results to ui
       return true; //if u made it this far process must be sucessful
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
    
    private void disableButton(Pane p) {
        ObservableList<Node> childs = p.getChildren();
        for(Node n : childs) {
            if(n.getId().equals("downloadBtn"))
                n.setDisable(true);
        }
    }
    
    private void enableButton() {      
        ObservableList<Node> childs = item.getChildren();
        for(Node n : childs)
            if(n.getId().equals("downloadBtn"))
                n.setDisable(false);
    }

    private ImageView getIcon(String path) {
        InputStream in;
        in = System.class.getResourceAsStream(path);
        Image image = new Image(in);
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
       item[0].setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(url), new StringSelection(MainApp.username));
            }
       });
       item[1].setGraphic(getIcon("/icons/icons8-open-in-browser-40.png"));
       item[1].setOnAction(new EventHandler() {
           @Override
            public void handle(Event t) {
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
       item[2].setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                clearThis();
            }
       });
       item[3].setGraphic(getIcon("/icons/icons8-cancel-40.png"));
       item[3].setOnAction(new EventHandler() {
           @Override 
           public void handle(Event t) {
               MainApp.dm.removeAll();
           }
       });
       item[4].setGraphic(getIcon("/icons/icons8-export-40.png"));
       item[4].setOnAction(new EventHandler() {
           @Override 
           public void handle(Event t) {
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
       item = loader.load();
       disableButton(item);
       item.setPadding(new Insets(3,3,3,3));
       final ContextMenu contextMenu = initContextMenu();
       item.setOnMouseClicked(new EventHandler() {
           @Override
           public void handle(Event t) {
               if (((MouseEvent)t).getButton().equals(MouseButton.SECONDARY))
                    contextMenu.show(item,((MouseEvent)t).getScreenX(),((MouseEvent)t).getScreenY());
           }
       });
       return item;
    }
    
    public void downloadThis() {
        MainApp.dm.startDownload(this);
    }
    
    private void clearThis() {
        MainApp.dm.removeDownload(this);
        setDone();
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
    }
    
    public void start() {
        disableButton(item);
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
                setDone();
            }catch(SocketTimeoutException e) {
                displayStatus("Took too long to download page");
                setDone();
            }catch (IOException e) {
                displayStatus(e.getMessage());
                setDone();
            } catch (GenericDownloaderException e) {
                displayStatus(e.getMessage());
                setDone();
            } catch(Exception e){
                displayStatus(e.getMessage());
                setDone();
            } finally { 
                System.out.println("done");
            }
            setDone();
        }
    }
    
    private void displayStatus(String msg) {
        Platform.runLater(new Runnable() {
           public void run() {
               for(int i = 0; i < paneElems.size(); i++) {
                   if (paneElems.get(i) instanceof Label) {
                        Label stat = (Label)paneElems.get(i);
                        stat.setText(msg+" "+videoName);
                        paneElems.set(i,stat);
                        break;
                   }
               }
           }
        });
    }
    
    private void updateProgressBar(final float progress) {
        Platform.runLater(new Runnable() {
           public void run() {
               for(int i = 0; i < paneElems.size(); i++) {
                   if (paneElems.get(i) instanceof ProgressBar) {
                        ProgressBar stat = (ProgressBar)paneElems.get(i);
                        stat.setProgress(progress);
                        paneElems.set(i,stat);
                        break;
                   }
               }
           }
        });
    }
}
