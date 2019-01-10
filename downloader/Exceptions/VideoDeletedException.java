/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Exceptions;

/**
 *
 * @author christopher
 */
public class VideoDeletedException extends GenericDownloaderException{
    
    public VideoDeletedException() {
        super("Video deleted");
    }
    
    public VideoDeletedException(String video) {
        super(video);
    }
}
