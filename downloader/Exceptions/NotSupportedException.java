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
public class NotSupportedException extends GenericDownloaderException {
    private final String url;
    
    public NotSupportedException(String msg, String url) {
        super(msg);
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
}
