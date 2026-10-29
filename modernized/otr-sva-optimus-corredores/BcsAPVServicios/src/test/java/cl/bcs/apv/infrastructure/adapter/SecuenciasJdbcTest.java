package cl.bcs.apv.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Sequence adapter. Only the statement it builds is checked: it is HANA SQL (sequence access on DUMMY) that this module cannot run on
 * H2 (see the Javadoc of the class: HANA syntax unverified).
 */
class SecuenciasJdbcTest {

    @Test
    void construyeElAccesoNextvalDeLaSecuencia() {
        assertThat(SecuenciasJdbc.sentencia("BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV"))
                .isEqualTo("SELECT \"BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV\".NEXTVAL FROM DUMMY");
    }

    @Test
    void rechazaNombresQueNoSonIdentificadores() {
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia("x\".NEXTVAL FROM DUMMY; DROP TABLE t; --")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia("a b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SecuenciasJdbc.sentencia(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
