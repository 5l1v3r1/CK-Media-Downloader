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
public class PrivateVideoException extends GenericDownloaderException{
    
    public PrivateVideoException(String video) {
        super(video+" is private");
    }
    
    public PrivateVideoException() {
        super("Video is private");
    }
}
