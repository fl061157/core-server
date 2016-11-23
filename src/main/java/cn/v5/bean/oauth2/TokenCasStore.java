package cn.v5.bean.oauth2;

import cn.v5.entity.oauth.AccessToken;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.IndexCondition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by haoWang on 2016/7/16.
 */
public class TokenCasStore implements TokenStore, TokenHandler {
    private static final Log LOG = LogFactory.getLog(TokenCasStore.class);

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    private PersistenceManager manager;


    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    //y
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken oAuth2AccessToken = null;
        String key = authenticationKeyGenerator.extractKey(authentication);
        try {
            AccessToken accessToken = manager.indexedQuery(AccessToken.class, new IndexCondition("authentication_id", key)).getFirst();
            oAuth2AccessToken = deserializeAccessToken(accessToken.getTokenInfo());
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to find access token for authentication " + authentication, e);
            }
        }
        if (oAuth2AccessToken != null
                && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(oAuth2AccessToken.getValue())))) {
            removeAccessToken(oAuth2AccessToken.getValue());
            // Keep the store consistent (maybe the same user is represented by this authentication but the details have
            // changed)
            storeAccessToken(oAuth2AccessToken, authentication);
        }
        return oAuth2AccessToken;
    }

    //y
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        if (readAccessToken(token.getValue()) != null) {
            removeAccessToken(token.getValue());
        }
        AccessToken accessToken = new AccessToken();
        accessToken.setTokenId(extractTokenKey(token.getValue()));
        accessToken.setTokenInfo(serializeAccessToken(token));
        accessToken.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
        accessToken.setUserName(authentication.isClientOnly() ? null : authentication.getName());
        accessToken.setClientId(authentication.getOAuth2Request().getClientId());
        accessToken.setAuthentication(serializeAuthentication(authentication));
        manager.insert(accessToken);
    }


    //y
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        OAuth2AccessToken oAuth2AccessToken = null;
        AccessToken accessToken = manager.find(AccessToken.class, extractTokenKey(tokenValue));
        try {
            oAuth2AccessToken = deserializeAccessToken(accessToken.getTokenInfo());
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            removeAccessToken(tokenValue);
        }
        return oAuth2AccessToken;
    }

    //y
    public void removeAccessToken(OAuth2AccessToken token) {
        removeAccessToken(token.getValue());
    }

    //y
    public void removeAccessToken(String tokenValue) {
        manager.deleteById(AccessToken.class, extractTokenKey(tokenValue));
    }

    //y
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    //y
    public OAuth2Authentication readAuthentication(String token) {
        OAuth2Authentication authentication = null;
        try {
            AccessToken accessToken = manager.find(AccessToken.class, extractTokenKey(token));
            authentication = deserializeAuthentication(accessToken.getAuthentication());
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            removeAccessToken(token);
        }
        return authentication;
    }

    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {

    }

    public OAuth2RefreshToken readRefreshToken(String token) {
        OAuth2RefreshToken refreshToken = null;
        return refreshToken;
    }

    public void removeRefreshToken(OAuth2RefreshToken token) {
        removeRefreshToken(token.getValue());
    }

    public void removeRefreshToken(String token) {
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String value) {
        OAuth2Authentication authentication = null;
        return authentication;
    }

    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    public void removeAccessTokenUsingRefreshToken(String refreshToken) {

    }

    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
//        List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
//
//        try {
//            accessTokens = jdbcTemplate.query(selectAccessTokensFromClientIdSql, new SafeAccessTokenRowMapper(),
//                    clientId);
//        } catch (EmptyResultDataAccessException e) {
//            if (LOG.isInfoEnabled()) {
//                LOG.info("Failed to find access token for clientId " + clientId);
//            }
//        }
//        accessTokens = removeNulls(accessTokens);
//
//        return accessTokens;
        return null;
    }

//    public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
//        List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
//
//        try {
//            accessTokens = jdbcTemplate.query(selectAccessTokensFromUserNameSql, new SafeAccessTokenRowMapper(),
//                    userName);
//        } catch (EmptyResultDataAccessException e) {
//            if (LOG.isInfoEnabled())
//                LOG.info("Failed to find access token for userName " + userName);
//        }
//        accessTokens = removeNulls(accessTokens);
//
//        return accessTokens;
//    }

    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        List<OAuth2AccessToken> oAuth2AccessTokens;
        List<AccessToken> accessToken = manager.indexedQuery(AccessToken.class, new IndexCondition("client_id", clientId)).get();
        oAuth2AccessTokens = accessToken.stream().map(t -> deserializeAccessToken(t.getTokenInfo())).collect(Collectors.toList());

        oAuth2AccessTokens = removeNulls(oAuth2AccessTokens);

        return oAuth2AccessTokens;
    }

    private List<OAuth2AccessToken> removeNulls(List<OAuth2AccessToken> accessTokens) {
        List<OAuth2AccessToken> tokens = new ArrayList<OAuth2AccessToken>();
        for (OAuth2AccessToken token : accessTokens) {
            if (token != null) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    protected String extractTokenKey(String value) {
        if (value == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }
    }

    public void setManager(PersistenceManager manager) {
        this.manager = manager;
    }


    protected byte[] serializeAccessToken(OAuth2AccessToken token) {
        return SerializationUtils.serialize(token);
    }


    protected byte[] serializeAuthentication(OAuth2Authentication authentication) {
        return SerializationUtils.serialize(authentication);
    }

    protected OAuth2AccessToken deserializeAccessToken(byte[] token) {
        return SerializationUtils.deserialize(token);
    }


    protected OAuth2Authentication deserializeAuthentication(byte[] authentication) {
        return SerializationUtils.deserialize(authentication);
    }


    @Override
    public void invalidToken(String clientId) {
        try {
            List<AccessToken> accessTokens = manager.indexedQuery(AccessToken.class, new IndexCondition("client_id", clientId)).get();
            accessTokens.forEach(k -> manager.delete(k));
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            //ignore empty result exception.
        }
    }

    @Override
    public String getAppId(String tokenValue) {
        AccessToken accessToken = manager.find(AccessToken.class, extractTokenKey(tokenValue));
        return accessToken.getClientId();
    }
}
