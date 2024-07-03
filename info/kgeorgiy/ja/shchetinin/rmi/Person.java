package info.kgeorgiy.ja.shchetinin.rmi;

import java.rmi.RemoteException;

public interface Person {
    String getName() throws RemoteException;
    String getSurname() throws RemoteException;
    String getPassport() throws RemoteException;
}
