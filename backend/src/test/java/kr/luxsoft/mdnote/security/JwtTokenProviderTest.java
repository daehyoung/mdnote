package kr.luxsoft.mdnote.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setup() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "testSecretKeyWhichIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", 3600000L);
        jwtTokenProvider.init();
    }

    @Test
    public void testCreateAndValidateToken() {
        String token = jwtTokenProvider.createToken("testuser", "USER");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    public void testGetAuthentication() {
        String token = jwtTokenProvider.createToken("testuser", "USER");
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        
        assertNotNull(auth);
        assertEquals("testuser", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void testTamperedToken() {
        String token = jwtTokenProvider.createToken("testuser", "USER");
        String tamperedToken = token + "tamper";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    public void testInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalidToken"));
    }
}
