/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author christopher
 */
public class Account implements Externalizable{
    private static long versionId = 1;
    private String login, password; //encrypt password
    
    public Account() {
        this("","");
    }
    
    public Account(String login) {
        this(login,"");
    }
    
    public Account(String login, String password) {
        this.login = login;
        setPassword(password); //encrypt and set
    }
    
    public void setPassword(String p) {
        
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getPassword() {
        return password; // return unencrypted version
    }
    
    public String getLogin() {
        return login;
    }

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(versionId);
        out.writeObject(login);
        out.writeObject(password);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        versionId = (long)in.readObject();
        login = (String)in.readObject();
        password = (String)in.readObject();
    }
}
