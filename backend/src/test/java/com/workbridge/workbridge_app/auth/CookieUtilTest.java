package com.workbridge.workbridge_app.auth;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.workbridge.workbridge_app.auth.util.CookieUtil;
import com.workbridge.workbridge_app.auth.util.CookieUtil.TokenType;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class CookieUtilTest {

    @InjectMocks
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(cookieUtil, "isSecureCookie", true); // manually set secure flag
    }

    @Test
    void setTokenCookie_ShouldAddSecureHttpOnlyCookie() {
        cookieUtil.setTokenCookie(response, "access_token", "jwt-value", TokenType.ACCESS);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertEquals("access_token", cookie.getName());
        assertEquals("jwt-value", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals(TokenType.ACCESS.getMaxAge(), cookie.getMaxAge());
    }

    @Test
    void clearCookie_ShouldSetCookieWithNullValueAndZeroMaxAge() {
        cookieUtil.clearCookie(response, "refresh_token");

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void extractTokenFromCookie_ShouldReturnTokenIfPresent() {
        Cookie[] cookies = { new Cookie("access_token", "jwt-123") };
        when(request.getCookies()).thenReturn(cookies);

        String result = cookieUtil.extractTokenFromCookie(request, "access_token");

        assertEquals("jwt-123", result);
    }

    @Test
    void extractTokenFromCookie_ShouldReturnNullIfCookieNotPresent() {
        Cookie[] cookies = { new Cookie("other", "val") };
        when(request.getCookies()).thenReturn(cookies);

        String result = cookieUtil.extractTokenFromCookie(request, "access_token");

        assertNull(result);
    }

    @Test
    void extractTokenFromCookie_ShouldReturnNullIfCookiesNull() {
        when(request.getCookies()).thenReturn(null);

        String result = cookieUtil.extractTokenFromCookie(request, "access_token");

        assertNull(result);
    }
}

