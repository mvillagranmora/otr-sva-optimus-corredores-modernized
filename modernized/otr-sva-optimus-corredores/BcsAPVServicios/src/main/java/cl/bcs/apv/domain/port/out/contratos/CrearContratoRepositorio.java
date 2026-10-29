package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/**
 * Persistence port of the use case CrearContrato (legacy {@code CrearContratoSAAPV}): one method per legacy query. The rows keep the legacy
 * names (public fields named like the column labels: the adapter maps by label). {@code SQLException} is surfaced unchanged by the adapter (the
 * methods declare no checked exception, see {@code Sneaky}).
 */
public interface CrearContratoRepositorio {

    /** Legacy {@code SelectFolioContratoSAAPV}. */
    class FilaFolioContrato {
        public Long IdPersona;
        public Long IdCliente;
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
        public Long DiaDescuento;
        public Long IdMedioRecaudacionFormaPago;
        public Long IdPortafolio;
        public Long IdPortafolioContrato;
    }

    /** Legacy {@code SelectContratoSAAPV}. */
    class FilaContratoVigente {
        public Long IdPersona;
        public Long IdCliente;
        public String IdEstado;
        public String IdEstadoSolicitud;
        public String FolioContratoSAAPV;
        public Long IdPlan;
        public Long IdMedioRecaudacionFormaPago;
        public Long IdMedioRecaudacionTrabajador;
        public String Pago;
        public String CodigoPortafolio;
        public Long IdMatrizAgrupadorAPV;
        public Long IdProductoAgrupador;
        public Long IdRegimenTributario;
    }

    /** Legacy {@code CrearAporteSAAPV.SelectDatosAPV}. */
    class FilaDatosApv {
        public Long IdMatrizAgrupadorAPV;
        public Long IdRegimenTributario;
        public String Codigo;
        public String NombrePlanTipoInversion;
        public String CodigoAgrupador;
    }

    /** Legacy {@code CrearAporteSAAPV.SelectIdPortafolio}. */
    class FilaIdPortafolio {
        public Long IdPortafolio;
    }

    /** Legacy {@code ContratoSAAPVVigente}. */
    class FilaEstadoContrato {
        public String IdEstado;
    }

    /** Legacy {@code salidaResidenciaCliente}. */
    class FilaResidencia {
        public Long IdDivisionPais;
        public Long IdPais;
        public Long IdCiudad;
    }

    /** Legacy {@code SalidaRegimen}. */
    class FilaCodigoRegimen {
        public String Codigo;
    }

    /** Legacy {@code salidaParametrosFichaCliente}: mutable, the legacy overwrites it with the data of the contract. */
    class FilaFichaCliente {
        public Long IdPersona;
        public String DocumentoIdentidad;
        public String Nombres;
        public String ApellidoPaterno;
        public String ApellidoMaterno;
        public String FechaNacimiento;
        public String Direccion;
        public String NumeroDireccion;
        public String TelefonoFijo;
        public String TelefonoMovil;
        public String TelefonoLaboral;
        public Long IdPais;
        public String Pais;
        public Long IdRegion;
        public String Region;
        public Long IdComuna;
        public String Comuna;
        public Long IdCiudad;
        public String Ciudad;
        public String EstadoCivil;
        public String Genero;
        public String DireccionElectronica;
        public String IdCategoriaInversionista;
        public Long IdEjecutivo;
        public String IdPerfilComercial;
        public Long IdPersonaEmpleador;
        public String RazonSocialEmpleador;
        public String RutEmpleador;
        public Long IdPaisEmpleador;
        public String DireccionEmpleador;
        public String NumeroDireccionEmpleador;
        public Long IdCiudadEmpleador;
        public Long IdComunaEmpleador;
        public String TelefonoEmpleador;
    }

    /** Legacy {@code Instrumento}. */
    class FilaInstrumento {
        public Long IdInstrumento;
        public String Nemotecnico;
    }

    /** Legacy {@code EntidadRecaudadora}. */
    class FilaEntidadRecaudadora {
        public String DocumentoIdentidad;
        public Long IdEntidadRecaudadora;
        public String Nombre;
        public Long IdPersona;
    }

    /** Legacy {@code PerfilComercialAPV}. */
    class FilaPerfilComercial {
        public String IdPerfilComercialBase;
        public String IdPerfilCobros;
    }

    /** Legacy {@code ParamsContrato_Update}. */
    class ContratoACerrar {
        public Long IdContratoSAAPV;
    }

    List<FilaFolioContrato> consultarFolioContrato(Long idCliente, Long idContratoSAAPV);

    List<FilaContratoVigente> consultarContratosVigentesAprobados(Long idCliente);

    List<FilaDatosApv> consultarDatosApv(String regimenTributario, String producto, Long idPlan);

    List<FilaIdPortafolio> consultarIdPortafolio(Long idCliente, Long idMatrizAgrupadorApv, Long idRegimenTributario);

    List<FilaIdPortafolio> consultarPrimerIdPortafolio(Long idCliente);

    List<FilaEstadoContrato> consultarEstadoContrato(Long idContratoSAAPV);

    List<FilaResidencia> consultarResidencia(String documentoIdentidad);

    List<FilaCodigoRegimen> consultarCodigoRegimen(Long idRegimen);

    List<FilaFichaCliente> consultarFichaCliente(Long idCliente, Long idPortafolio);

    List<FilaInstrumento> consultarInstrumentos(List<String> nemotecnicos);

    List<FilaEntidadRecaudadora> consultarEntidadRecaudadora(String documentoIdentidad);

    List<FilaPerfilComercial> consultarPerfilComercial(String idPerfilComercialBase);

    /** Legacy batch {@code UPDATE ... SET IdEstado = 'CER'}. */
    void cerrarContratos(List<ContratoACerrar> contratos);
}
