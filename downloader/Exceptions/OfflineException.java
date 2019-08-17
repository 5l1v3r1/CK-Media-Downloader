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
public class OfflineException extends GenericDownloaderException {
    
    public OfflineException(String user) {
        super(user+" is currently offline");
    }
    
    public OfflineException(String user, boolean t) {
        super(user);
    }
}
