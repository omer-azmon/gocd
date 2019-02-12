/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.server.service;

import com.thoughtworks.go.config.exceptions.NotAuthorizedException;
import com.thoughtworks.go.domain.AccessToken;
import com.thoughtworks.go.server.dao.AccessTokenDao;
import com.thoughtworks.go.server.domain.Username;
import com.thoughtworks.go.server.service.result.HttpLocalizedOperationResult;
import com.thoughtworks.go.util.Clock;
import com.thoughtworks.go.util.TestingClock;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static com.thoughtworks.go.helper.AccessTokenMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class AccessTokenServiceTest {
    @Mock
    private AccessTokenDao accessTokenDao;
    @Mock
    private SecurityService securityService;
    private AccessTokenService accessTokenService;
    private HttpLocalizedOperationResult result;
    private String username;
    private String authConfigId;
    private Clock clock = new TestingClock();

    @BeforeEach
    void setUp() {
        initMocks(this);
        accessTokenService = new AccessTokenService(accessTokenDao, clock, securityService);
        result = new HttpLocalizedOperationResult();

        username = "Bob";
        authConfigId = "auth-config-1";
    }

    @Test
    void shouldMakeACallToSQLDaoForAccessTokenCreation() throws Exception {
        String longerDescription = RandomStringUtils.randomAlphanumeric(1024).toUpperCase();
        accessTokenService.create(longerDescription, username, authConfigId);

        assertThat(result.isSuccessful()).isTrue();

        verify(accessTokenDao, times(1)).saveOrUpdate(any(AccessToken.class));
    }

    @Test
    void shouldMakeACallToSQLDaoForFetchingAccessToken() {
        long tokenId = 42;
        when(accessTokenDao.load(42)).thenReturn(randomAccessTokenForUser(username));

        accessTokenService.find(tokenId, username);
        verify(accessTokenDao, times(1)).load(42);
        verifyNoMoreInteractions(accessTokenDao);
    }

    @Test
    void shouldVerifyAccessTokenBelongsToUser() {
        long tokenId = 42;
        when(accessTokenDao.load(42)).thenReturn(randomAccessTokenForUser(username));

        accessTokenService.find(tokenId, username);
        verify(accessTokenDao, times(1)).load(42);
        verifyNoMoreInteractions(accessTokenDao);
    }

    @Test
    void shouldVerifyAccessTokenBelongsToAdminUser() {
        long tokenId = 42;
        when(accessTokenDao.load(42)).thenReturn(randomAccessTokenForUser(username));
        when(securityService.isUserAdmin(new Username("root"))).thenReturn(true);

        accessTokenService.find(tokenId, "root");
        verify(accessTokenDao, times(1)).load(42);
        verifyNoMoreInteractions(accessTokenDao);
    }

    @Test
    void shouldBailIfUserDoesNotOwnToken() {
        long tokenId = 42;
        when(accessTokenDao.load(42)).thenReturn(randomAccessTokenForUser(username));
        when(securityService.isUserAdmin(new Username("hacker"))).thenReturn(false);

        assertThatCode(() -> accessTokenService.find(tokenId, "hacker"))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("You performed an unauthorized operation!");
        verify(accessTokenDao, times(1)).load(42);
        verifyNoMoreInteractions(accessTokenDao);
    }

    @Test
    void shouldMakeACallToSQLDaoForFetchingAllAccessTokensBelongingToAUser() {
        accessTokenService.findAllTokensForUser(username);

        verify(accessTokenDao, times(1)).findAllTokensForUser(username);
        verifyNoMoreInteractions(accessTokenDao);
    }

    @Test
    void hashToken_shouldHashTheProvidedString() throws Exception {
        String tokenValue = "token1";
        String saltValue = "salt1";
        String hashed = AccessToken.digestToken(tokenValue, saltValue);

        SecretKey key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(new PBEKeySpec(tokenValue.toCharArray(), saltValue.getBytes(), 4096, 256));

        assertThat(hashed).isEqualTo(Hex.encodeHexString(key.getEncoded()));
    }

    @Test
    void hashToken_shouldGenerateTheSameHashValueForTheSameInputString() throws Exception {
        String tokenValue = "new-token";
        String saltValue = "new-salt";
        String hashed1 = AccessToken.digestToken(tokenValue, saltValue);
        String hashed2 = AccessToken.digestToken(tokenValue, saltValue);

        assertThat(hashed1).isEqualTo(hashed2);
    }
}
