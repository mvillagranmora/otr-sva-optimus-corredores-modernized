package cl.bcs.loaderapp.concrete.socionegocio.relacionpersona;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;

public class RelacionPersonaDbLoader implements DbLoader {

	/*
    private final String SQL_ConsultarPersonaIup = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/relacionpersona/ConsultarPersonaIUP.sql");
    Log log = new Log("SOC.RELACIONPERSONA");

    public static class IupPersona {
        public Long IdPersona;
    }

    @Override
    public List<String> loadIntoDb(Connection conn, EntradaCargaGeneral entrada, List<Object> lines) {
        Long idUsuarioDefecto = Long.parseLong("1");
        Long idEmpresaDefecto = Long.parseLong("1");
        SQL sql = new SQL();

        try {
            IdentificarLlaves identificarLlaves = new IdentificarLlavesBuilder(conn).setTipoRelacion().setPaises().setCiudades().build();

            int linea = 1;
            for (int i = 0; i < lines.size(); i++) {
                RelacionPersona relacion = (RelacionPersona) lines.get(i);
                HashMap<String, Object> params = new HashMap<String, Object>();
                System.out.println(relacion);
                String estado = "";
                if (relacion.getIupCliente() != null && !relacion.getIupCliente().equals("")) {
                    params.put("DocumentoIdentidad", relacion.getIupCliente());
                    try {
                        // consultar si existe la persona a la cual se le
                        // ingresara un relacionado
                        List<IupPersona> iupPersonas = sql.query(conn, SQL_ConsultarPersonaIup, params, IupPersona.class);
                        if (iupPersonas != null && iupPersonas.size() == 1) {
                            CrearPersona spCrearPersona = new CrearPersona();
                            CrearPersona.EntradaServicioCrearPersona entradaSpCrearPersona = new CrearPersona.EntradaServicioCrearPersona();
                            List<Enrolamiento> enrolamientos = new ArrayList<Enrolamiento>();

                            Enrolamiento enro = new Enrolamiento();
                            enro.IdEnrolamiento = Long.parseLong("0");
                            enro.IdEmpresa = idEmpresaDefecto;
                            enro.IdUsuario = idUsuarioDefecto;
                            enro.IdTipoEntidad = "PERSONA";
                            enro.IdEntidadEnrolada = Long.parseLong("0");
                            enro.IdTipoDocumentoIdentidad = "IUP";
                            enro.DocumentoIdentidad = relacion.getIupRelacion();
                            enro.IdTipoDocumentoIdentidadAdicional = "";
                            enro.DocumentoIdentidadAdicional = "";
                            enro.Tipo = relacion.getTipoPersona();
                            enro.Nombres = relacion.getNombres();
                            enro.ApellidoPaterno = relacion.getApellidoPaterno();
                            enro.ApellidoMaterno = relacion.getApellidoMaterno();
                            enro.RazonSocial = relacion.getRazonSocial();
                            enro.IdPaisNacimiento = identificarLlaves.getPais(relacion.getPaisNacimientoConstitucion());
                            enro.FechaNacimiento = relacion.getFechaNacimientoConstitucion();
                            enro.Sexo = relacion.getGenero();
                            enro.EstadoCivil = relacion.getTipoEstadoCivil();
                            enro.IdCiudad = identificarLlaves.getCiudad(relacion.getCiudadDistrito());
                            enro.IdDivisionPais = Long.parseLong("0");
                            enro.IdTipoTelefono = "FIJO";
                            enro.Telefono = relacion.getTelefono();
                            enro.Email = relacion.getEmail();
                            enro.DireccionCalle = relacion.getCalle();
                            enro.DireccionNumero = relacion.getNumero();
                            enro.DireccionDepartamento = "";
                            enro.FechaCreacion = "0001-01-01";
                            enro.FechaModificacion = "0001-01-01";
                            enro.IdPersonaConyuge = Long.parseLong("0");
                            enro.RegimenConyugal = "";
                            enro.CLI_IdCategoriaInversionista = "";
                            enro.CLI_IdTipoCliente = "";
                            enro.CLI_IdEmpleadoEjecutivo = Long.parseLong("0");
                            enro.CLI_CodigoAdicionalHolding = "";
                            enro.CLI_IdClaseCliente = "";
                            enro.POR_IdPerfilComercial = "";
                            enro.EMP_CodigoEmpleado = "";
                            enro.EMP_CodigoTipoCargoEmpleado = "";
                            enro.EMP_IdEmpleadoSupervisor = Long.parseLong("0");
                            enro.EMP_IdSucursal = Long.parseLong("0");
                            enro.EMP_FechaInicioCargo = "0001-01-01";
                            enro.EMP_FechaTerminoCargo = "0001-01-01";
                            enro.CNT_CodigoInscripcion = "";
                            enro.CNT_Emisor = "";
                            enro.CNT_Depositos = "";
                            enro.CNT_Bolsa = "";
                            enro.CNT_Corredora = "";
                            enro.CNT_CamaraDeCompensacion = "";
                            enro.CNT_Banco = "";
                            enro.CNT_Boveda = "";
                            enro.PRO_IdAreaProveedor = Long.parseLong("0");
                            enro.EMP_CargoIndefinido = "";

                            enrolamientos.add(enro);

                            entradaSpCrearPersona.P_IDEMPRESA = idEmpresaDefecto;
                            entradaSpCrearPersona.P_IDUSUARIO = idUsuarioDefecto;
                            entradaSpCrearPersona.P_IDMODULO = "SOC";
                            entradaSpCrearPersona.P_CONSULTARENROLAMIENTO = enrolamientos;
                            CrearPersona.ServiceSalida spSalidaCrearpersona = spCrearPersona.logica(conn, entradaSpCrearPersona);
                            System.out.println("OUT:" + spSalidaCrearpersona);

                            if (spSalidaCrearpersona.P_OUT_ERRORESCONTADOR == 0 && spSalidaCrearpersona.P_OUT_ERRORESCONTADOR == 0) {
                                Long idPersonaRelacionada = spSalidaCrearpersona.P_OUT_REFID;
                                Long idSocioNegocioRelacionada = spSalidaCrearpersona.P_OUT_IDSOCIONEGOCIO;

                                CrearRelacionPersona spCrearRelacionPersona = new CrearRelacionPersona();
                                CrearRelacionPersona.ServiceEntrada servicioentrada = new CrearRelacionPersona.ServiceEntrada();

                                servicioentrada.P_IDEMPRESA = idEmpresaDefecto;
                                servicioentrada.P_IDUSUARIO = idUsuarioDefecto;
                                servicioentrada.P_IDMODULO = "SOC";
                                servicioentrada.P_USAREGLASNEGOCIO = "N";
                                servicioentrada.P_IDTIPORELACION = identificarLlaves.getTipoRelacion(relacion.getRelacion());
                                servicioentrada.P_IDPERSONAORIGEN = iupPersonas.get(0).IdPersona;
                                servicioentrada.P_IDPERSONADESTINO = idPersonaRelacionada;
                                servicioentrada.P_IDESTADO = "ACT";
                                servicioentrada.P_FECHAINICIORELACION = "2017-04-20";
                                servicioentrada.P_FECHAFINRELACION = "0001-01-01";
                                servicioentrada.P_PORCENTAJEPARTICIPACION = Long.parseLong("0");
                                servicioentrada.P_RETIRADOCUMENTOS = "";
                                servicioentrada.P_FINALIDADRELACION = "";
                                servicioentrada.P_CONDICIONESRELACION = "";
                                servicioentrada.P_IDGRADOVINCULACIONPARENTAL = Long.parseLong("0");

                                CrearRelacionPersona.ServiceSalida salidaRelacion = spCrearRelacionPersona.logica(conn, servicioentrada);
                                System.out.println(salidaRelacion);
                                if(salidaRelacion.P_OUT_ADVERTENCIASCONTADOR==0 && salidaRelacion.P_OUT_ERRORESCONTADOR==0){
                                    estado = "Relacion creada correctamente.";
                                }
                            } else {
                                estado = "Error al crear la persona relacionada.";
                            }
                            SQL.exec(conn, "COMMIT");
                        } else {
                            estado = "El IUP cliente no es valido";
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                log.write("LINEA[" + linea + "], IUPCLIENTE: " + relacion.getIupCliente() + ", IUPRELACION: " + relacion.getIupRelacion() + " ESTADO:[" + estado + "]");
                linea++;
            }
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return null;
    }
*/

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		// TODO Auto-generated method stub
		return null;
	}
}
