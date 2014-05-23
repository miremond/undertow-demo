package org.pac4j.undertow;

import io.undertow.security.idm.Account;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.pac4j.core.profile.CommonProfile;

public class ProfileWrapper implements Serializable {

    private static final long serialVersionUID = 8996323477801100456L;

    final private CommonProfile profile;
    final private Account account;

    public ProfileWrapper(final CommonProfile profile) {
        this.profile = profile;
        this.account = new ClientAccount(profile);
    }

    CommonProfile getProfile() {
        return this.profile;
    }

    Account getAccount() {
        return this.account;
    }

    @Override
    public String toString() {
        return profile.toString();
    }

    private class ClientAccount implements Account {

        private Principal principal;
        private Set<String> roles;

        public ClientAccount(final CommonProfile profile) {
            this.roles = new HashSet<String>(profile.getRoles());
            this.principal = new Principal() {
                @Override
                public String getName() {
                    return profile.getId();
                }
            };
        }

        @Override
        public Set<String> getRoles() {
            return this.roles;
        }

        @Override
        public Principal getPrincipal() {
            return this.principal;
        }
    }

}
