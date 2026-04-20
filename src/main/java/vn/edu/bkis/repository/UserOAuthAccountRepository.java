package vn.edu.bkis.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.bkis.model.AuthProvider;
import vn.edu.bkis.model.UserOAuthAccount;

@Repository
public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, Long> {

    /**
     * Find an SSO account mapping by provider and subject identifier.
     *
     * @param provider the OAuth2 provider used for login
     * @param providerUserId the provider-side unique user identifier
     * @return matching mapping when the SSO account was already linked
     */
    Optional<UserOAuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
