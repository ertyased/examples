package info.kgeorgiy.ja.shchetinin.rmi;

import java.io.Serializable;


public class LocalPerson implements Person, Serializable {
    private final String name;
    private final String surname;
    private final String passport;

    public LocalPerson(String name, String surname, String passport) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSurname() {
        return surname;
    }
    @Override
    public String getPassport() {
        return passport;
    }
}
