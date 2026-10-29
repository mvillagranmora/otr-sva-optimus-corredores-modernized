package cl.bcs.fondomutuo.tesoreria;

import java.util.List;

/**
 * The four reads of the legacy {@code ProcesarIngresoEgresoTesoreria} (FM-TES-02..05): one SQL file of the module and the three SQL the legacy class
 * keeps IN LINE ({@code :40, :42, :45}; C-W0A-5). All SELECT, on the connection of the unit of work. The row classes keep the legacy names and fields
 * ({@code ProcesarIngresoEgresoTesoreria.Empleado / PreferenciaPagoCorredor / PreferenciaPagoPortafolio}, {@code pojo.ModalidadPago}).
 */
public interface TesoreriaDatos {

    /** {@code SQL_SELECT_Empleado} (in line, {@code ACC.Usuario}). */
    List<Empleado> empleados(Long idUsuario);

    /** {@code ComunesSolicitudes_SelectModalidadPago.sql} ({@code PAG.MedioDePagoYModalidadDePago}). */
    List<ModalidadPago> modalidadesPago();

    /** {@code SQL_SELECT_PreferenciaPagoPortafolio} (in line, view {@code ConsultarPreferenciaPagoPortafolio}, {@code IN} list). */
    List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio(List<Long> idsPreferencia);

    /** {@code SQL_SELECT_PreferenciaPagoCorredor} (in line, {@code TES.PreferenciaPagoCorredor} with fixed filters). */
    List<PreferenciaPagoCorredor> preferenciasPagoCorredor(Long idEmpresa);

    /** Legacy {@code ProcesarIngresoEgresoTesoreria.Empleado}. */
    class Empleado {
        public Long IdEmpleado;
    }

    /** Legacy {@code cl.bcs.fondomutuo.pojo.ModalidadPago}. */
    class ModalidadPago {
        public String IdMedioDePago;
        public String IdModalidadDePago;
    }

    /** Legacy {@code ProcesarIngresoEgresoTesoreria.PreferenciaPagoCorredor}. */
    class PreferenciaPagoCorredor {
        public String IdTipoMovimiento;
        public String IdConcepto;
        public String IdMedioDePago;
        public Long IdCuentaBanco;
        public String IdMoneda;
    }

    /** Legacy {@code ProcesarIngresoEgresoTesoreria.PreferenciaPagoPortafolio}. */
    class PreferenciaPagoPortafolio {
        public Long IdPreferenciaPagoPortafolio;
        public Long IdCuentaBancaria;
        public Long IdBancoCuentaBancaria;
        public String NumeroCuentaBancaria;
        public String IdTipoCuentaBancaria;
        public String NombreTipoCuentaBancaria;
    }
}
