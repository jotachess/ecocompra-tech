package com.ecocompra.tech.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authentication;
    private final JwtEncoder encoder;
    private final long ttl;
    private final UsuarioRepository usuarios;
    public AuthController(AuthenticationManager authentication, JwtEncoder encoder,
            @Value("${security.jwt.ttl-seconds:1800}") long ttl, UsuarioRepository usuarios) {
        this.authentication = authentication; this.encoder = encoder; this.ttl = ttl;
        this.usuarios = usuarios;
    }
    public record LoginRequest(@NotBlank @Size(max=80) String username,
                               @NotBlank @Size(max=72) String password) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresIn,
                                String username, List<String> roles) {}
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var principal = authentication.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var roles = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)).toList();
        Instant now = Instant.now();
        Usuario usuario = usuarios.findByUsername(principal.getName()).orElseThrow();
        var claims = JwtClaimsSet.builder().issuer("ecocompra-tech").subject(principal.getName())
                .audience(List.of("ecocompra-api")).issuedAt(now).expiresAt(now.plusSeconds(ttl))
                .claim("roles", roles).claim("uid", usuario.getId())
                .claim("version", usuario.getTokenVersion()).build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new LoginResponse(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(),
                "Bearer", ttl, principal.getName(), roles);
    }
    @GetMapping("/me")
    public Map<String,Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("username", jwt.getSubject(), "roles", jwt.getClaimAsStringList("roles"));
    }
}
