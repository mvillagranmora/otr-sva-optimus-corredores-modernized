package cl.bcs.loaderapp.concrete.socionegocio.persona;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Persona;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PersonaDbLoader implements DbLoader {
    private final String SQL_CreateInputTable = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/CreateInputClienteNomina.sql");
    private final String SQL_DropInputTable = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/DropInputClienteNomina.sql");
    private final String SQL_InsertInputTable = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/InsertInputClienteNomina.sql");
    private final String SQL_UpdatePersonaNomina = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/UpdatePersonaNomina.sql");
    private final String SQL_CrearDocumentoIdentidadAdicional = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/CrearDocumentoIdentidadAdicional.sql");
    private final String SQL_UpdateCliente = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/UpdateCliente.sql");
    private final String SQL_CrearCuentaBancaria = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/CrearCuentaBancaria.sql");
    private final String SQL_UpdatePortafolioCliente = LoaderUtils.loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/persona/UpdatePortafolioCliente.sql");
    Log log = new Log("SOC.CLIENTE");

    public static class Portafolio {
        public Long IdCliente;
        public String CodigoPortafolio;
        public String DescripcionPortafolio;
        public String CodigoProposito;
        public String AliasPortafolio;
        public String EstadoPortafolio;
        public String FechaBloqueo;
        public String CodigoTipoBloqueo;
        public String ComentarioBloqueo;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ClienteNomina {
        public String TIPODOCUMENTO;
        public String DOCUMENTOIDENTIDAD;
        public String TIPOPERSONA;
        public String NOMBRES;
        public String APELLIDOPATERNO;
        public String APELLIDOMATERNO;
        public Long PAISNACIMIENTO;
        public String FECHANACIMIENTO;
        public String EDOCIVIL;
        public String GENERO;
        public String RAZONSOCIAL;
        public String CALLE;
        public String NUMERO;
        public String COMPLEMENTO;
        public Long PAISRESIDENCIA;
        public Long CIUDAD;
        public String TIPOTELEFONO;
        public String TELEFONO;
        public String EMAIL;
        public Long EJECUTIVO;
        public String CATINV;
        public String PERCOMER;
        public String TIPOCLIENTE;
        public String MENSAJEVALIDACION;

        @Override
        public String toString() {
            return Utils.printJson(this, false); 
        }
    }

    public static class UpdatePersonaNomina {
        public Long IdCliente;
        public Long PaisNacionalidadPrincipal;
        public Long PaisNacionalidadSecundaria;
        public Long PaisResidenciaSecundaria;
        public Long Profesion;
        public Long GradoInstruccion;
        public Long CodigoGiro;
        public String FechaDefuncion;
        public String TipoCargoPersona;
        public Long AniosServicio;
        public Long IngresoPromedioMensual;
        public Long IdRangoPatrimonio;
        public String EsExtranjero;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class DocumentoIdentidadAdicional {
        public Long IdPersona;
        public String DocumentoIdentidadAdicional;
        public Long IdUsuario;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class UpdateCliente {
        public Long IdCliente;
        public Long IdSucursal;
        public String IdEstadoCliente;
        public String FechaBloqueo;
        public String IdTipoBloqueo;
        public String ComentarioBloqueo;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class CuentaBancaria {
        public Long IdBanco;
        public String IdTipoCuentaBancaria;
        public String IdMoneda;
        public String NumeroCuentaBancaria;
        public Long IdPersona;
        public String IdEstado;
        public String IdEstadoAutorizacion;
        public Long IdUsuarioCreador;
        public Long IdPlaza;
        public String IdTipoIdentificadorCuenta;
        public Long IdentificadorCuenta;
		
        @Override
		public String toString() {
			return Utils.printJson(this, false);
		}

    }

    public static class OutputClienteNomina {
        public Long IDPERSONA;
        public Long IDCLIENTE;
        public String TIPODOCUMENTO;
        public String DOCUMENTOIDENTIDAD;
        public String NOMBRES;
        public String APELLIDOPATERNO;
        public String APELLIDOMATERNO;
        public String MENSAJE;

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class Advertencias {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        @Override
        public String toString() {
        	return Utils.printJson(this, false);
        }
    }

    public static class Errores {
        public String Codigo;
        public String Descripcion;

        @Override
        public String toString() {
        	return Utils.printJson(this, false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
        Long idUsuarioDefecto = in.p_IdEmpresa;
        Long idEmpresaDefecto = in.p_IdUsuario;
        SQL sql = new SQL();
        try (Connection conn = jsJB.getConnection()){
            
            IdentificarLlaves identificarLlaves = new IdentificarLlavesBuilder(conn)
                    .setBancos()
                    .setCategoriasInversionista()
                    .setCiudades()
                    .setClasesCliente()
                    .setGiros()
                    .setGradosInstruccion()
                    .setIupEjecutivo()
                    .setPaises()
                    .setProfesiones()
                    .setSucursales()
                    .setTiposCargoPersona()
                    .setTiposCuentaBancaria()
                    .setPlazas()
                    .setTipoDocumentoIdentidad()
                    .build();

            List<Persona> personasCSV = (List<Persona>) (Object) lines;
            // preparar personas para llamada servicio CrearClienteNomina
            int linea = 1;
            for (Persona per : personasCSV) {
                HashMap<String, Persona> personaMap = new HashMap<String, Persona>();
                List<ClienteNomina> clientes = new ArrayList<ClienteNomina>();

                // log.write(per);
                ClienteNomina cliente = new ClienteNomina();
                cliente.TIPODOCUMENTO = "IUP";
                cliente.DOCUMENTOIDENTIDAD = per.getIUP();
                cliente.TIPOPERSONA = per.getTipoPersona();
                cliente.NOMBRES = per.getNombres().toUpperCase();
                cliente.APELLIDOPATERNO = per.getApellidoPaterno().toUpperCase();
                cliente.APELLIDOMATERNO = per.getApellidoMaterno().toUpperCase();
                cliente.PAISNACIMIENTO = identificarLlaves.getPais(per.getPaisNacimientoConstitucion());
                cliente.FECHANACIMIENTO = per.getFechaNacimientoConstitucion();
                cliente.EDOCIVIL = per.getTipoEstadoCivil();
                cliente.GENERO = per.getGenero();
                cliente.RAZONSOCIAL = per.getRazonSocial().toUpperCase();
                cliente.CALLE = per.getCalle();
                cliente.NUMERO = per.getNumero();
                cliente.COMPLEMENTO = per.getComplemento();
                cliente.PAISRESIDENCIA = identificarLlaves.getPais(per.getPaisResidenciaPrincipal());
                // no funciona
                cliente.CIUDAD = identificarLlaves.getCiudad(per.getCiudadDistrito());
                cliente.TIPOTELEFONO = "FIJO";
                cliente.TELEFONO = per.getTelefono();
                cliente.EMAIL = per.getEmail();
                cliente.EJECUTIVO = identificarLlaves.getIUPEjecutivo(per.getIUP_Ejecutivo());
                cliente.CATINV = identificarLlaves.getIdCategoriaInversionista(per.getNombreCortoCategoriaInversionista());
                cliente.PERCOMER = "ARRIESGADO";
                cliente.TIPOCLIENTE = per.getTipoCliente();
                
                cliente.MENSAJEVALIDACION = "";
                clientes.add(cliente);
                if (personaMap.get(cliente.DOCUMENTOIDENTIDAD) == null) {
                    personaMap.put(cliente.DOCUMENTOIDENTIDAD, per);
                }

                // crear tabla temporal de entrada
                SQL.exec(conn, SQL_CreateInputTable);
                // insertar personas en la tabla de entrada
                sql.batch(conn, SQL_InsertInputTable, clientes, ClienteNomina.class);
                // sql.exec(conn, "COMMIT");//debug
                // Parametros de entrada SP Crear Clientes Nomina
                HashMap<String, Object> parmsIN = new HashMap<String, Object>();
                parmsIN.put("P_IDEMPRESA", idEmpresaDefecto);
                parmsIN.put("P_IDUSUARIO", idUsuarioDefecto);
                parmsIN.put("P_IDMODULO", "SOC");

                parmsIN.put("P_CLIENTEBLOQUEADO", "N");
                parmsIN.put("P_IDCATEGORIAINVERSIONISTADEFECTO", "");
                parmsIN.put("P_IDPERFILCOMERCIALDEFECTO", "");
                parmsIN.put("P_IDTIPOCLIENTEDEFECTO", "");
                parmsIN.put("P_IDEMPLEADOEJECUTIVODEFECTO", 0);
                parmsIN.put("P_PERMITEPARAMETROSDEFAULT", "N");
                parmsIN.put("p_ARCHIVO", clientes);
                // Parametros salida SP
                HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
                parmsOUT.put("P_OUT_MENSAJE", OutputClienteNomina.class);
                parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
                parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", Advertencias.class);
                parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
                parmsOUT.put("P_OUT_ERRORESDETALLE", Errores.class);

                // Ejecuta el SP
                String queryCall = "CALL \"BSCL.SocioNegocio.BD.Servicios::CLI.CrearClienteNomina\"(:P_IDEMPRESA, :P_IDUSUARIO, :P_IDMODULO, :P_CLIENTEBLOQUEADO, :P_IDCATEGORIAINVERSIONISTADEFECTO, :P_IDPERFILCOMERCIALDEFECTO, :P_IDTIPOCLIENTEDEFECTO, :P_IDEMPLEADOEJECUTIVODEFECTO, :P_PERMITEPARAMETROSDEFAULT, #NOMINA_CLIENTES , $P_OUT_MENSAJE, $P_OUT_ADVERTENCIASCONTADOR, $P_OUT_ADVERTENCIASDETALLE, $P_OUT_ERRORESCONTADOR, $P_OUT_ERRORESDETALLE)";
                HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);
                List<OutputClienteNomina> resultado = (List<OutputClienteNomina>) rpt.get("P_OUT_MENSAJE");
                // log.write(rpt);

                for (OutputClienteNomina nomina : resultado) {
                    String estado = nomina.MENSAJE.toUpperCase();
                    if (estado.equals("INGRESADO")) {
                    	boolean errores = false;
                    	
                        List<UpdatePersonaNomina> listaActualizacionPersonas = new ArrayList<UpdatePersonaNomina>();
                        List<DocumentoIdentidadAdicional> listaCreacionDocumentosIdentidadAdicional = new ArrayList<DocumentoIdentidadAdicional>();
                        List<UpdateCliente> listaActualizacionClientes = new ArrayList<UpdateCliente>();
                        List<CuentaBancaria> listaCreacionCuentasBancarias = new ArrayList<CuentaBancaria>();
                        List<Portafolio> listaActualizacionPortafolios = new ArrayList<Portafolio>();

                        // actualizar
                        Persona persona = personaMap.get(nomina.DOCUMENTOIDENTIDAD);
                        UpdatePersonaNomina updatePersona = new UpdatePersonaNomina();
                        updatePersona.IdCliente = nomina.IDCLIENTE;
                        updatePersona.PaisNacionalidadPrincipal = identificarLlaves.getPais(persona.getPaisNacionalidadPrincipalCasaMatriz());
                        updatePersona.PaisNacionalidadSecundaria = identificarLlaves.getPais(persona.getPaisNacionalidadSecundario());
                        updatePersona.PaisResidenciaSecundaria = identificarLlaves.getPais(persona.getPaisResidenciaSecundario());
                        updatePersona.Profesion = identificarLlaves.getProfesion(persona.getProfesionActividad());
                        updatePersona.GradoInstruccion = identificarLlaves.getGradoIntruccion(persona.getGradoInstruccion());
                        updatePersona.CodigoGiro = identificarLlaves.getGiro(persona.getCodigoGiro());
                        updatePersona.FechaDefuncion = persona.getFechaDefuncion();
                        updatePersona.TipoCargoPersona = persona.getCodigoCargo();//// identificarLlaves.getTipoCargoPersona(persona.getCodigoCargo());
                        updatePersona.AniosServicio = Long.parseLong(persona.getAnioServicio());
                        updatePersona.IngresoPromedioMensual = Long.parseLong(persona.getIngresoPromedioMensual());
                        updatePersona.IdRangoPatrimonio = identificarLlaves.getRangoPatrimonio(persona.getIdRangoPatrimonio());
                        updatePersona.EsExtranjero = persona.getExtranjero();
                        // log.write(updatePersona);
                        listaActualizacionPersonas.add(updatePersona);

                        // Documento identidad adicional
                        if (persona.getRUT() != null && !persona.getRUT().equals("")) {
                            DocumentoIdentidadAdicional docAdicional = new DocumentoIdentidadAdicional();
                            docAdicional.IdPersona = nomina.IDPERSONA;
                            docAdicional.DocumentoIdentidadAdicional = persona.getRUT();
                            docAdicional.IdUsuario = idUsuarioDefecto;
                            // log.write(docAdicional);
                            listaCreacionDocumentosIdentidadAdicional.add(docAdicional);
                        }
                        // actualizar cliente
                        UpdateCliente updCliente = new UpdateCliente();
                        updCliente.ComentarioBloqueo = persona.getComentarioBloqueo();
                        updCliente.FechaBloqueo = persona.getFechaBloqueo();
                        updCliente.IdCliente = nomina.IDCLIENTE;
                        updCliente.IdEstadoCliente = persona.getEstadoCliente();
                        updCliente.IdSucursal = identificarLlaves.getSucursal(persona.getSucursalCliente());
                        updCliente.IdTipoBloqueo = persona.getCodigoTipoBloqueo();
                        // log.write(updCliente);
                        listaActualizacionClientes.add(updCliente);

                        // Crear Cuenta Bancaria
                        if (persona.getNumeroCuentaBancaria() != null) {
                            CuentaBancaria ctaBco = new CuentaBancaria();
                            ctaBco.IdBanco = identificarLlaves.getBanco(persona.getNombreBanco());
                            ctaBco.IdEstado = "ACT";
                            ctaBco.IdEstadoAutorizacion = "AUT";
                            ctaBco.IdMoneda = persona.getIdMoneda();
                            ctaBco.IdPersona = nomina.IDPERSONA;

                            ctaBco.IdTipoCuentaBancaria = identificarLlaves.getTipoCuentaBancaria(persona.getTipoCuenta());
                            ctaBco.IdUsuarioCreador = idUsuarioDefecto;
                            ctaBco.NumeroCuentaBancaria = persona.getNumeroCuentaBancaria();
                            
                            ctaBco.IdPlaza = identificarLlaves.getPlaza(persona.getPlazaDistrito());
                            if(ctaBco.IdPlaza==null){
                            	estado = "[[CUENTA BANCARIA: IdPlaza no es valida]]";
                            	errores = true;
                            }
                            ctaBco.IdTipoIdentificadorCuenta = identificarLlaves.getTipoDocumentoIdentidad(persona.getTipoIdentificadorCuenta());
                            ctaBco.IdentificadorCuenta = Long.parseLong(persona.getIdentificadorCuenta());
                            
                            // log.write(ctaBco);
                            listaCreacionCuentasBancarias.add(ctaBco);
                        }
                        // actualizar portafolio cliente
                        if (persona.getCodigoPortafolio() != null) {
                            Portafolio port = new Portafolio();
                            port.IdCliente = nomina.IDCLIENTE;
                            port.AliasPortafolio = persona.getAliasPortafolio();
                            port.CodigoPortafolio = persona.getCodigoPortafolio();
                            port.CodigoProposito = persona.getCodigoProposito();
                            port.CodigoTipoBloqueo = persona.getCodigoTipoBloqueo();
                            port.ComentarioBloqueo = persona.getComentarioBloqueoPortafolio();
                            port.DescripcionPortafolio = persona.getDescripcionPortafolio();
                            port.EstadoPortafolio = persona.getEstadoPortafolio();
                            port.FechaBloqueo = persona.getFechaBloqueoPortafolio();
                            listaActualizacionPortafolios.add(port);
                        }
                        // actualizar los datos de la linea entidad por entidad.
                        try {
                            sql.batch(conn, SQL_CrearDocumentoIdentidadAdicional, listaCreacionDocumentosIdentidadAdicional, DocumentoIdentidadAdicional.class);
                        } catch (SQLException sqlE) {
                        	 if (!errores) {
                        		 estado = "Error al crear documento identidad adicional";
                             } else {
                                 estado = estado + ", Error al crear documento identidad adicional";
                             }
                             errores = true;
                        }
                        try {
                            sql.batch(conn, SQL_UpdatePersonaNomina, listaActualizacionPersonas, UpdatePersonaNomina.class);
                        } catch (SQLException sqlE) {
                            if (!errores) {
                                estado = "Error al actualizar datos de la persona";
                            } else {
                                estado = estado + ", Error al actualizar datos de la persona";
                            }
                            errores = true;
                        }
                        try {
                            sql.batch(conn, SQL_CrearCuentaBancaria, listaCreacionCuentasBancarias, CuentaBancaria.class);
                        } catch (SQLException sqlE) {
                            if (!errores) {
                                estado = "Error al crear la cuenta bancaria";
                            } else {
                                estado = estado + ", Error al crear la cuenta bancaria";
                            }
                            errores = true;
                        }
                        try {
                            sql.batch(conn, SQL_UpdateCliente, listaActualizacionClientes, UpdateCliente.class);
                        } catch (SQLException sqlE) {
                            if (!errores) {
                                estado = "Error al actualizar datos del cliente";
                            } else {
                                estado = estado + ", Error al actualizar datos del cliente";
                            }
                            errores = true;
                        }
                        try {
                            sql.batch(conn, SQL_UpdatePortafolioCliente, listaActualizacionPortafolios, Portafolio.class);
                        } catch (SQLException sqlE) {
                            if (!errores) {
                                estado = "Error al actualizar datos del portafolio";
                            } else {
                                estado = estado + ", Error al actualizar datos del portafolio";
                            }
                        }

                    }
                    if (estado.equals("INGRESADO")) {
                        SQL.exec(conn, SQL_DropInputTable);
                        SQL.exec(conn, "COMMIT");
                    } else {
                        SQL.exec(conn, "ROLLBACK");
                    }
                    log.write("LINEA[" + linea + "], CLIENTE: " + nomina.DOCUMENTOIDENTIDAD + ", ESTADO:[" + estado + "]");
                    linea++;
                }
            }
        } catch (SQLException e) {
            log.write(e.getMessage(), e);
        }
        return Arrays.asList("OK");
    }

}
