package br.com.ruralmanager.seguranca;

import br.com.ruralmanager.usuario.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final PropriedadesSeguranca props;

    public TokenService(JwtEncoder encoder, PropriedadesSeguranca props) {
        this.encoder = encoder;
        this.props = props;
    }

    public long segundosDeValidade() {
        return props.jwtExpiracaoMinutos() * 60;
    }

    public String emitir(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(SegurancaConfig.EMISSOR)
                .issuedAt(agora)
                .expiresAt(agora.plus(props.jwtExpiracaoMinutos(), ChronoUnit.MINUTES))
                .subject(String.valueOf(usuario.getId()))
                .claim("perfil", usuario.getPerfil().name());
        // Admin nao tem produtor; o claim simplesmente nao existe para ele.
        if (usuario.getProdutorId() != null) {
            claims.claim("pid", usuario.getProdutorId());
        }
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();
    }
}
