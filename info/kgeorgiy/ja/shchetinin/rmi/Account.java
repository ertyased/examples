package info.kgeorgiy.ja.shchetinin.rmi;

import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money in the account. */
    int getAmount() throws RemoteException;

    /** Sets amount of money in the account. */
    void setAmount(int amount) throws RemoteException;
    Person getPerson(String passport, boolean type);
}
