package cl.bcs.tesoreria.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Family B sequence adapter. Only the statement it builds is checked: it is HANA SQL (sequence access on DUMMY) that this module cannot run on
 * H2 (see the Javadoc of the class: HANA syntax unverified).
 */
class SecuenciasJdbcTest {

    @Test
    void construyeElAccesoNextvalDeLaSecuencia() {
        assertThat(SecuenciasJdbc.sentencia("BSCL.Tesoreria.BD.Secuencias::IdMandatoPAC"))
                .isEqualTo("SELECT \"BSCL.Tesoreria.BD.Secuencias::IdMandatoPAC\".NEXTVAL FROM DUMMY");
    }

    @Test
    void rechazaNombresQueNoSonIdentificadores() {
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia("x\".NEXTVAL FROM DUMMY; DROP TABLE t; --")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia("a b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
