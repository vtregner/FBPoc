package cz.csas.startup.FBPoc;

import android.app.Application;
import com.facebook.model.GraphUser;
import cz.csas.startup.FBPoc.model.Account;
import cz.csas.startup.FBPoc.model.Collection;
import cz.csas.startup.FBPoc.model.Payment;
import cz.csas.startup.FBPoc.utils.FontsOverride;

import java.util.List;
import java.util.Map;

/**
 * Created by cen29414 on 18.4.2014.
 */
public class Friends24Application extends Application {
    private List<GraphUser> selectedFrieds;
    private List<Account> accounts;
    private GraphUser fbUser;
    private boolean appLogged;
    private Map<Account, List<Payment>> payments;
    private Map<Account, List<Collection>> collections;
    private String authHeader;

    public List<GraphUser> getSelectedFrieds() {
        return selectedFrieds;
    }

    public void setSelectedFrieds(List<GraphUser> selectedFrieds) {
        this.selectedFrieds = selectedFrieds;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public GraphUser getFbUser() {
        return fbUser;
    }

    public void setFbUser(GraphUser fbUser) {
        this.fbUser = fbUser;
    }

    public boolean isAppLogged() {
        return appLogged;
    }

    public void setAppLogged(boolean appLogged) {
        this.appLogged = appLogged;
    }

    public Map<Account, List<Payment>> getPayments() {
        return payments;
    }

    public void setPayments(Map<Account, List<Payment>> payments) {
        this.payments = payments;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public Account getAccount(Long id) {
        if (accounts != null) {
            for (Account account : accounts) {
                if (account.getId().equals(id)) return account;
            }
        }
        return null;
    }

    public Map<Account, List<Collection>> getCollections() {
        return collections;
    }

    public void setCollections(Map<Account, List<Collection>> collections) {
        this.collections = collections;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Gotham-Light.otf");
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Gotham-Light.otf");
    }
}
