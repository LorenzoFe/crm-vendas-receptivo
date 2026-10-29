package com.ibrowse.crm.discador;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vonix")
public record VonixProperties(
        boolean habilitado,
        String url,
        String token,
        String filaId,
        String retry,
        long intervaloSincronizacaoMs,
        int maxTentativasEnvio) {

    public boolean tokenConfigurado() {
        return token != null && !token.isBlank() && !token.startsWith("COLE_AQUI");
    }

    public boolean urlConfigurada() {
        return url != null && !url.isBlank();
    }

    public boolean filaConfigurada() {
        return filaId != null && !filaId.isBlank();
    }

    public boolean pronto() {
        return habilitado && urlConfigurada() && tokenConfigurado() && filaConfigurada();
    }
}
