package processpackage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessInterface extends Remote {
    public String send(String origen) throws RemoteException;

    public String sendOk(String from, String to) throws RemoteException;

}
