package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    public static final long EXPIRES_IN_SECONDS = 900; // 15 minutos (igual que Clase 5)

    private final JwtEncoder encoder;

    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generarToken(Usuario usuario) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("pasteleria-api")
            .subject(usuario.getEmail())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
            .claim("rol", usuario.getRol().name())
            .claim("nombre", usuario.getNombre())
            .claim("userId", usuario.getId())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}