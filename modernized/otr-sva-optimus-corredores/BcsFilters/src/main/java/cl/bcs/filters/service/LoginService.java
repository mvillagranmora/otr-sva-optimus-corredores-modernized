package cl.bcs.filters.service;

import cl.bcs.filters.domain.Usuario;
import cl.bcs.filters.port.out.SesionRepository;
import cl.bcs.filters.port.out.ServicioRepository;
import cl.bcs.filters.port.out.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Handles local (username + password) login.
 *
 * Security fixes applied:
 *   RULE-018 — entities with estado BLO (or anything other than ACT) cannot log in.
 *              Legacy allowed BLO users through the authentication gate.
 *   RULE-040 — account is locked when {@code intentos >= maxIntentos} (off-by-one fix).
 *              Legacy used {@code intentos > maxIntentos}, locking only at attempt max+1.
 */
@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private static final int DEFAULT_MAX_INTENTOS = 3;

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final ServicioRepository servicioRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UsuarioRepository usuarioRepository,
                        SesionRepository sesionRepository,
                        ServicioRepository servicioRepository,
                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.servicioRepository = servicioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user with the given credentials and creates a session.
     *
     * @param codigoUsuario login name
     * @param clave         raw (un-hashed) password
     * @param canal         channel code (WEB, COR, …)
     * @return the new session token
     * @throws BadCredentialsException  if the user does not exist or the password is wrong
     * @throws DisabledException        if {@code estado != "ACT"} (RULE-018)
     * @throws LockedException          if failed-attempt count ≥ max (RULE-040)
     * @throws AccountExpiredException  if {@code fechaVigencia} is in the past
     */
    @Transactional
    public String login(String codigoUsuario, String clave, String canal) {
        Usuario usuario = usuarioRepository.findByCodigo(codigoUsuario)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // RULE-018 FIX: only ACT estado is allowed. Legacy let BLO users pass the check.
        if (!"ACT".equals(usuario.estado())) {
            log.warn("Login denied — estado={} for user={}", usuario.estado(), codigoUsuario);
            throw new DisabledException("Account is not active");
        }

        // Expiry check (also applied in SSO path — RULE-045)
        if (usuario.fechaVigencia() != null && usuario.fechaVigencia().isBefore(LocalDate.now())) {
            throw new AccountExpiredException("Account has expired");
        }

        // RULE-040 FIX: lock when intentos >= maxIntentos (was > maxIntentos in legacy).
        int maxIntentos = resolveMaxIntentos();
        if (usuario.intentos() >= maxIntentos) {
            log.warn("Login denied — intentos={} >= maxIntentos={} for user={}",
                    usuario.intentos(), maxIntentos, codigoUsuario);
            throw new LockedException("Account is locked due to too many failed attempts");
        }

        if (!passwordEncoder.matches(clave, usuario.clave())) {
            usuarioRepository.incrementarIntentos(codigoUsuario);
            throw new BadCredentialsException("Invalid credentials");
        }

        usuarioRepository.resetearIntentos(codigoUsuario);
        return sesionRepository.create(usuario.idUsuario(), canal);
    }

    private int resolveMaxIntentos() {
        return servicioRepository.getConfiguracion("PLA.MaxIntentosPassword")
                .map(v -> {
                    try { return Integer.parseInt(v.trim()); }
                    catch (NumberFormatException e) { return DEFAULT_MAX_INTENTOS; }
                })
                .orElse(DEFAULT_MAX_INTENTOS);
    }
}
