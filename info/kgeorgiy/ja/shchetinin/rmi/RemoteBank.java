package info.kgeorgiy.ja.shchetinin.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;

    private final ConcurrentMap<String, Account> accountById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> personByPassport = new ConcurrentHashMap<>();
    private final ConcurrentMap<Person, List<Account>> accountsByPerson = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

}
