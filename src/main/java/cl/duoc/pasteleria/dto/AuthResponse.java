package cl.duoc.pasteleria.dto;

// Mantiene accessToken, tokenType y expiresIn de Clase 5 + el objeto usuario con su rol para React
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UsuarioResponse usuario
) {}