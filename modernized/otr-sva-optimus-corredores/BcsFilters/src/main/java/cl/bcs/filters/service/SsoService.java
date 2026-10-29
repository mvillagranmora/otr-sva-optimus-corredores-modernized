package cl.bcs.filters.service;

import cl.bcs.filters.domain.Usuario;
import cl.bcs.filters.port.out.SesionRepository;
import cl.bcs.filters.port.out.SsoStateRepository;
import cl.bcs.filters.port.out.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Handles SSO (OAuth2/OIDC) login flow.
 *
 * Security fixes applied:
 *   RULE-046 — CSRF state is a random UUID per flow, not a hardcoded constant.
 *              Callback validates the state against the stored value and rejects mismatches.
 *   RULE-045 — The SSO login path now validates FechaVigencia (account expiry).
 *              Legacy only checked FechaVigencia in the local login path.
 */
@Service
public class SsoService {

    private static final Logger log = LoggerFactory.getLogger(SsoService.class);

    private final SsoStateRepository ssoStateRepository;
    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;

    public SsoService(SsoStateRepository ssoStateRepository,
                      UsuarioRepository usuarioRepository,
                      SesionRepository sesionRepository) {
        this.ssoStateRepository = ssoStateRepository;
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
    }

    /**
     * Generates a random CSRF state token and stores it for later validation.
     *
     * RULE-046 FIX: state = UUID.randomUUID().toString() instead of a hardcoded string.
     *
     * @return the state parameter to embed in the identity-provider redirect URL
     */
    public String initSso() {
        // RULE-046 FIX: random UUID — not a hardcoded "fee7e77..." constant.
        String state = UUID.randomUUID().toString();
        ssoStateRepository.storeState(state);
        log.debug("SSO init: generated state={}", state);
        return state;
    }

    /**
     * Handles the identity-provider callback after successful authentication.
     *
     * RULE-046 FIX: the received {@code state} is validated against the stored value.
     *               Mismatching or replayed states are rejected with an exception.
     * RULE-045 FIX: account expiry (FechaVigencia) is checked in the SSO path
     *               (previously only checked during local password login).
     *
     * @param code           OAuth2 authorisation code (opaque; the IdP exchange is out of scope)
     * @param state          CSRF state received from the IdP redirect
     * @param codigoUsuario  user identity resolved from the IdP token/claim
     * @return the new platform session token
     */
    @Transactional
    public String handleCallback(String code, String state, String codigoUsuario) {
        // RULE-046 FIX: validate and consume the stored state — reject replays or forgeries.
        if (!ssoStateRepository.validateAndConsumeState(state)) {
            log.warn("SSO callback rejected — invalid or replayed state for user={}", codigoUsuario);
            throw new IllegalStateException("Invalid SSO state — possible CSRF attack");
        }

        Usuario usuario = usuarioRepository.findByCodigo(codigoUsuario)
                .orElseThrow(() -> new BadCredentialsException("SSO user not found"));

        // RULE-018: only ACT estado can log in (also enforced in SSO path)
        if (!"ACT".equals(usuario.estado())) {
            throw new DisabledException("Account is not active");
        }

        // RULE-045 FIX: validate FechaVigencia in the SSO path.
        // Legacy only performed this check in the local login path.
        if (usuario.fechaVigencia() != null && usuario.fechaVigencia().isBefore(LocalDate.now())) {
            log.warn("SSO login denied — account expired: user={} vigencia={}",
                    codigoUsuario, usuario.fechaVigencia());
            throw new AccountExpiredException("Account has expired");
        }

        return sesionRepository.create(usuario.idUsuario(), "WEB");
    }
}
