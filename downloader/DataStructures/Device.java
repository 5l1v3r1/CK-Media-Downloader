/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.Serializable;

/**
 *
 * @author christopher
 */
public class Device implements Serializable{
    private static final long serialVersionUID = 3205872440977757998L;
    String title = null, ip = null;
    
    public Device(String title, String ip) {
       this.title = title;
       this.ip = ip;
    }
    
    public void name(String title) {
        this.title = title;
    }
    
    public String name() {
        return this.title;
    }
    
    public void address(String ip) {
        this.ip = ip;
    }
    
    public String address() {
        return this.ip;
    }
    
    public boolean is(String name) {
        return name.equals(title);
    }
    
    public boolean is(Device d) {
        if (this.title.equals(d.title))
            return this.address().equals(d.address());
        else return false;
    }
    
    @Override
    public String toString() {
        return title;
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof Device) {
            Device temp = (Device)o;
            return ((this.title.equals(temp.title)) && (this.ip.equals(temp.ip)));
        } else return false;
    }
}
