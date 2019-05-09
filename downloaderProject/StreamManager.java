/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import ChrisPackage.GameTime;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 *
 * @author christopher
 */
public class StreamManager {
    private final Pane root;
    private final Button play, stop, rewind15, skip15, skipMin, rewindMin; 
    private final Slider slide;
    private MediaPlayer player;
    private final int SKIP = 15, FASTSKIP = 60;
    
    public StreamManager(Pane p) {
        this.root = p;
        play = (Button)root.lookup("#toogle");
        stop = (Button)root.lookup("#stop");
        rewind15 = (Button)root.lookup("#rewind15");
        skip15 = (Button)root.lookup("#skip15");
        rewindMin = (Button)root.lookup("#rewindMin");
        skipMin = (Button)root.lookup("#skipMin");
        slide = (Slider)root.lookup("#progress");
        slide.setMin(0.0);
        enableButtons(true);
    }
    
    private void setHeader(String s) {
        Platform.runLater(() -> {
            ((Label)root.lookup("#title")).setText("Stream - "+s);
        });
    }
    
    private void resetHeader() {
        Platform.runLater(() -> {
            ((Label)root.lookup("#title")).setText("Stream");
        });
    }
    
    private void updateTime(long current, long total) {
        GameTime c = new GameTime(), t = new GameTime();
        c.addSec(current); t.addSec(total);
        int min = t.getLength() < 2 ? 2 : t.getLength();
        String progress = current == -1 ? "" : c.getTimeFormat(min) + " / " + t.getTimeFormat(min);
        
        Platform.runLater(() -> {
            ((Label)root.lookup("#time")).setText(progress);
        });
    }
    
    private void changeStatus(String s) {
        Platform.runLater(() -> {
            ((Label)root.lookup("#status")).setText(s);
        });
    }
    
    private void enableButtons(boolean enable) {
        play.setDisable(enable);
        stop.setDisable(enable);
        skip15.setDisable(enable);
        skipMin.setDisable(enable);
        rewind15.setDisable(enable);
        rewindMin.setDisable(enable);
    }
    
    public void setMedia(String url, String name) throws MalformedURLException, URISyntaxException, IOException {
        if (player != null)
            player.dispose();
        player = new MediaPlayer(new Media(new URL(url.replace("https","http")).toURI().toString()));
        ((MediaView)root.lookup("#video")).setMediaPlayer(player);
        
        configurePlayer();
        
        play.setGraphic(getIcon("/icons/icons8-pause-48.png"));
        player.play();
        
        setHeader(name);
        Platform.runLater(() -> {
            MainApp.displayPane(MainApp.STREAMPANE);
        });
    }
    
    private void configurePlayer() {
        changeStatus("Preparing");
        
        player.setOnReady(() -> {
            slide.setValue(0.0);
            slide.setMax(player.getTotalDuration().toSeconds());
            changeStatus("Ready");
        });
        
        player.setOnEndOfMedia(() -> {
            changeStatus("Done");
            play.setGraphic(getIcon("/icons/icons8-play-48.png"));
            slide.setValue(0.0);
            player.seek(Duration.ZERO);
            player.pause();
            changeStatus("Done");
        });
        
        player.setOnStopped(() -> {changeStatus("");});
        player.setOnStalled(() -> {changeStatus("Stalled");});
        player.setOnError(() -> {
            changeStatus("Error");
            player.dispose(); player = null;
            ((MediaView)root.lookup("#video")).setMediaPlayer(null);
            enableButtons(true);
            resetHeader();
        });
        player.setOnHalted(() -> {changeStatus("Halted");});
        player.setOnPlaying(() -> {
            changeStatus("Streaming");
            play.setGraphic(getIcon("/icons/icons8-pause-48.png"));
        });
        player.setOnPaused(() -> {
            changeStatus("Paused");
            play.setGraphic(getIcon("/icons/icons8-play-48.png"));
        });
        
        player.currentTimeProperty().addListener((ObservableValue<? extends Duration> ov, Duration t, Duration current) -> {
            slide.setValue(current.toSeconds());
            if (player != null)
                updateTime((long)current.toSeconds(), (long)player.getTotalDuration().toSeconds());
            else updateTime(-1,-1);
        });
        
        slide.setOnMousePressed((MouseEvent) -> {
            if(player != null)
                player.seek(Duration.seconds(slide.getValue()));
        });
        
        slide.setOnMouseClicked((MouseEvent) -> {
            if(player != null) {
                player.seek(Duration.seconds(slide.getValue()));
                play.setGraphic(getIcon("/icons/icons8-pause-48.png"));
            }
        });
        
        enableButtons(false);
        play.setOnAction((ActionEvent) -> {
            tooglePlay();
        });
        stop.setOnAction((ActionEvent) -> {
            stop();
        });
        skip15.setOnAction((ActionEvent) -> {
            player.seek(Duration.seconds(slide.getValue()).add(Duration.seconds(SKIP)));
        });
        rewind15.setOnAction((ActionEvent) -> {
            player.seek(Duration.seconds(slide.getValue()).subtract(Duration.seconds(SKIP)));
        });
        skipMin.setOnAction((ActionEvent) -> {
            player.seek(Duration.seconds(slide.getValue()).add(Duration.seconds(FASTSKIP)));
        });
        rewindMin.setOnAction((ActionEvent) -> {
            player.seek(Duration.seconds(slide.getValue()).subtract(Duration.seconds(FASTSKIP)));
        });
    }
    
    private ImageView getIcon(String path) {
        Image image = new Image(System.class.getResourceAsStream(path));
        ImageView icon = new ImageView();
        icon.setImage(image);
        icon.setFitHeight(30);
        icon.setFitWidth(30);
        return icon;
    }
    
    public void stop() {
        changeStatus("");
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
            ((MediaView)root.lookup("#video")).setMediaPlayer(null);
        }
        enableButtons(true);
        resetHeader();
        updateTime(-1,-1);
        MainApp.displayPane(MainApp.DOWNLOADPANE);
    }
    
    public void tooglePlay() {
        if(null == player.getStatus()) {
        } else switch (player.getStatus()) {
            case PLAYING:
                player.pause();
                break;
            case PAUSED:
                player.play();
                break;
            default:
                break;
        }
    }
}
