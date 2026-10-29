package cl.bcs.loaderapp.concrete.posiciones.garantia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.posiciones.Garantia;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GarantiaDbLoader implements DbLoader {
	
	public String SQL_INSERT_GARANTIA = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/garantias/Garantias_IngresarGarantia.sql");
	
	public String SQL_CONSULTAR_NOMBRE_CONTRAPARTE = "SELECT \"IdContraparte\", \"NombreCompleto\" FROM BSCL.\"BSCL.Custodia.BD::GTA.EntidadDestinoGarantia\" EDG\r\n" + 
													 "INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" CP " +
													 "ON  CP.\"IdPersona\" = EDG.\"IdPersona\"" + 
													 "WHERE EDG.\"IdPersona\" = :IdPersona;";
	public String SQL_CONSULTA_PORTAFOLIO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" CCP INNER JOIN \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" DI ON CCP.\"IdPersona\" = DI.\"IdPersona\" WHERE DI.\"DocumentoIdentidad\" = :DocumentoIdentidadCliente AND DI.\"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CCP.\"CodigoPortafolio\" = :CodigoPortafolio;";
	public String SQL_CONSULTA_AGENTE = "SELECT \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" WHERE \"CodigoEmpleado\" = :CodigoAgente;";
	
	public String SQL_CONSULTA_INSTRUMENTO =    "SELECT I.\"IdInstrumento\", I.\"Nemotecnico\", I.\"IdTipoInstrumento\", TI.\"IdClasificacionTipoInstrumento\" \r\n" + 
												"FROM \"BSCL.Plataforma.BD::INS.Instrumento\" I\r\n" + 
												"INNER JOIN \"BSCL.Plataforma.BD::INS.TipoInstrumento\" TI ON  TI.\"IdTipoInstrumento\" = I.\"IdTipoInstrumento\"\r\n" + 
												"WHERE I.\"Nemotecnico\" = :Nemotecnico;";
	public static class Instrumento{
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
	}
	public static class Agente{
		public Long IdEmpleadoEjecutivo;
	}
	public static class Portafolio{
		public Long IdPortafolio;
	}
	public static class Contraparte{
		public Long IdContraparte;
		public String NombreCompleto;
	}
	
	public static class EntidadGarantia{
		public Long IdGarantia;
		public String FolioGarantia;
		public String FolioCarteraOrigen;
		public String IdCarteraOrigen;
		public String FolioCarteraGarantizada;
		
		//public String NemotecnicoCarteraGarantizada;
		public Long IdInstrumentoCarteraGarantizada;
		
		public String EsEspecifica;
		
		public Long IdContraparteGarantia;
		public String RazonSocialContraparteGarantia;
		
		public String CuentaContraparteGarantia;
		public String IdOrigenGarantia;
		public String IdEspecieGarantia;
		public String FechaIngresoRegistro;
		public String FechaModificacionRegistro;
		public String PrecioCompraOperacionOrigen;
		public String TasaCompraOperacionOrigen;
		public String IdEmpresa;
		public String FechaOperacion;
		public String FechaVencimientoOperacion;
		public String PlazoEnDiasOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdCuentaCustodioPortafolio;
		public String IdEstado;
		public String IdTipoCanalIngreso;
		public String IdMonedaSistema;
		public String IdMonedaAdicional;
		public String FechaFactorMonedaAdicional;
		public String ValorFactorMonedaAdicional;
		public String IdConcepto;
		public String IdTipoComprobante;
		public String FolioComprobante;
		public String FechaComprobante;
		public String FolioExternoReferencia;
		public String IdSucursal;
		public Long IdAgente;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public String IdUsuario;
		public String IdModulo;
		
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdInstrumentoAnterior;

		public String IdEmisor;
		public String NemoSVS;
		public String IdTipoReajuste;
		public String EsPagaderoEn;
		public String FechaVencimientoInstrumento;
		public String EsFungible;
		public String CodigoIFRS;
		public String IdMonedaInstrumento;
		public String TIFAfectoBeneficio;
		public String TIFTasa;
		public String ISIN;
		public String FechaFactorMonedaInstrumento;
		public String ValorFactorMonedaInstrumento;
		public String Cantidad;
		public String CorteMinimo;
		public String ValorRescate;
		public String PrecioGarantia;
		public String TasaGarantia;
		public String MontoGarantia;
		public int IdCarga;
		
		public String MontoGarantiaMonedaGarantia;
		public String MontoGarantiaMonedaAdicional;
		public String INGFechaOperacion;
		public String INGIdCondicionLiquidacion;
		public String INGFechaLiquidacion;
		public String INGCantidad;
		public String INGValorRescate;
		public String INGPrecioGarantia;
		public String INGTasaGarantia;
		public String INGMontoATasaGarantiaActual;
		public String INGMontoGarantiaActualMonedaGarantia;
		public String INGMontoGarantiaActualMonedaAdicional;
		public String INGPrecioMercadoActual;
		public String INGTasaMercadoActual;
		public String INGMontoATasaMercadoActual;
		public String INGMontoMercadoActualMonedaGarantia;
		public String INGMontoMercadoActualMonedaAdicional;
		public String INGMontoATasaCompraActual;
		public String INGMontoCompraActualMonedaGarantia;
		public String INGMontoCompraActualMonedaAdicional;
		public String INGTasaCorredora;
		public String INGFechaTasaCorredora;
		public String INGMontoATasaCorredora;
		public String INGMontoATasaCorredoraMonedaGarantia;
		public String INGMontoATasaCorredoraMonedaAdicional;
		public String INGFechaCorteCupon;
		public String INGNumeroCupon;
		public String INGIdMonedaPagoCupon;
		public String INGMontoCorteCupon;
		public String INGFechaPagoCorteCupon;
		public String EGRFechaOperacion;
		public String EGRIdTipoOperacion;
		public String EGRIdOperacion;
		public String EGRIdMovimientoCarteraEnTransito;
		public String EGRIdTipoCanalIngreso;
		public String EGRIdCondicionLiquidacion;
		public String EGRFechaLiquidacion;
		public String EGRIdTipoComprobante;
		public String EGRFolioComprobante;
		public String EGRFechaComprobante;
		public String EGRFolioExternoReferencia;
		public String EGRIdConcepto;
		public String EGRIdSucursal;
		public Long EGRIdAgente;
		public String EGRCantidad;
		public String EGRValorRescate;
		public String EGRPrecio;
		public String EGRTasa;
		public String EGRMonto;
		public String EGRMontoMonedaGarantia;
		public String EGRMontoMonedaAdicional;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		
		try (Connection conn = jsJB.getConnection()){
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(conn)
			        .setPaises()
			        .setInstrumentos()
			        .build();
                
			int linea = 0;
			List<Garantia> garantiasCSV = (List<Garantia>) (Object) lines;
			List<Long> idsGarantias = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdGarantia", (long) garantiasCSV.size());
			
			for(Garantia garantia: garantiasCSV){
				try {
					System.out.println(garantia);
					
					EntidadGarantia gar = new EntidadGarantia();
					gar.IdGarantia = idsGarantias.get(linea);
					gar.FolioGarantia = garantia.getFolioGarantia();
					gar.FolioCarteraOrigen = garantia.getFolioCarteraOrigen();
					gar.IdCarteraOrigen = garantia.getIdCarteraOrigen();
					gar.FolioCarteraGarantizada = garantia.getFolioCarteraGarantizada();
					gar.IdInstrumentoCarteraGarantizada = llaves.getInstrumentos(garantia.getNemotecnicoCarteraGarantizada());//obtener con nemo
					gar.EsEspecifica = garantia.getEsEspecifica();
					
					//obtener datos contraparte
					ConsultarDocumentoIdentidadCliente consultarDocumentoIdentidad = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio salidaSocioNegocioContraparte = consultarDocumentoIdentidad.logica(conn, garantia.getDocumentoIdentidadContraparteGarantia(), garantia.getTipoDocuementoIdentidadContraparteGarantia(), garantia.getTipoPersonaContraparteGarantia(), idEmpresaDefecto);
					HashMap<String, Object> params = new HashMap<>();
					gar.IdContraparteGarantia = salidaSocioNegocioContraparte.IdContraparte;
					params.put("IdPersona", salidaSocioNegocioContraparte.IdContraparte);
					List<Contraparte> contrapartes = sql.query(conn, SQL_CONSULTAR_NOMBRE_CONTRAPARTE, params, Contraparte.class);
					gar.RazonSocialContraparteGarantia = (contrapartes!=null && contrapartes.size()==1) ? contrapartes.get(0).NombreCompleto : "";
					
					gar.CuentaContraparteGarantia = garantia.getCuentaContraparteGarantia();
					gar.IdOrigenGarantia = garantia.getIdOrigenGarantia();
					gar.IdEspecieGarantia = garantia.getIdEspecieGarantia();
					gar.FechaIngresoRegistro = garantia.getFechaIngresoRegistro();
					gar.FechaModificacionRegistro = garantia.getFechaModificacionRegistro();
					gar.PrecioCompraOperacionOrigen = garantia.getPrecioCompraOperacionOrigen();
					gar.TasaCompraOperacionOrigen = garantia.getTasaCompraOperacionOrigen();
					gar.IdEmpresa = garantia.getIdEmpresa();
					gar.FechaOperacion = garantia.getFechaOperacion();
					gar.FechaVencimientoOperacion = garantia.getFechaVencimientoOperacion();
					gar.PlazoEnDiasOperacion = garantia.getPlazoEnDiasOperacion();
					
					//obtener id cliente
					SalidaSocioNegocio salidaSocioNegocio = consultarDocumentoIdentidad.logica(conn, garantia.getDocumentoIdentidadCliente(), garantia.getTipoDocuementoIdentidad(), garantia.getTipoPersona(), idEmpresaDefecto);
					gar.IdCliente = salidaSocioNegocio.IdCliente;//obtener con el documento identidad
					//obtener id portafolio
					params = new HashMap<>();
					params.put("DocumentoIdentidadCliente", garantia.getDocumentoIdentidadCliente());
					params.put("TipoDocumentoIdentidad", garantia.getTipoDocuementoIdentidad());
					params.put("CodigoPortafolio", garantia.getCodigoPortafolio());
					List<Portafolio> portafolios = sql.query(conn, SQL_CONSULTA_PORTAFOLIO, params, Portafolio.class);
					gar.IdPortafolio = portafolios.get(0).IdPortafolio;//obtener con el idcliente + codigo portafolio
					
					gar.IdCuentaCustodioPortafolio = garantia.getIdCuentaCustodioPortafolio();
					gar.IdEstado = garantia.getIdEstado();
					gar.IdTipoCanalIngreso = garantia.getIdTipoCanalIngreso();
					gar.IdMonedaSistema = garantia.getIdMonedaSistema();
					gar.IdMonedaAdicional = garantia.getIdMonedaAdicional();
					gar.FechaFactorMonedaAdicional = garantia.getFechaFactorMonedaAdicional();
					gar.ValorFactorMonedaAdicional = garantia.getValorFactorMonedaAdicional();
					gar.IdConcepto = garantia.getIdConcepto();
					gar.IdTipoComprobante = garantia.getIdTipoComprobante();
					gar.FolioComprobante = garantia.getFolioComprobante();
					gar.FechaComprobante = garantia.getFechaComprobante();
					gar.FolioExternoReferencia = garantia.getFolioExternoReferencia();
					gar.IdSucursal = garantia.getIdSucursal();
					
					//obtener con el codigoAgente
					params = new HashMap<>();
					if(!garantia.getCodigoAgente().equals("")) {
						params.put("CodigoAgente", garantia.getCodigoAgente());
						List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
						gar.IdAgente = agentes.get(0).IdEmpleadoEjecutivo;
							
					}else {
						gar.IdAgente = (long) 0;
					}
					
					gar.IdCondicionLiquidacion = garantia.getIdCondicionLiquidacion();
					gar.FechaLiquidacion = garantia.getFechaLiquidacion();
					gar.IdUsuario = garantia.getIdUsuario();
					gar.IdModulo = "CUS";
					
					//obtener instrumento con el nemo
					params = new HashMap<>();
					params.put("Nemotecnico", garantia.getNemotecnico());
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					gar.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
					gar.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
					gar.IdInstrumento = instrumentos.get(0).IdInstrumento;//obtener con el nemo
					
					//obtener instrumento anterior con el nemo anterior
					params = new HashMap<>();
					params.put("Nemotecnico", garantia.getNemotecnicoAnterior());
					List<Instrumento> instrumentosAnterior = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					gar.IdInstrumentoAnterior = instrumentosAnterior.get(0).IdInstrumento;
					
					gar.IdEmisor = garantia.getIdEmisor();
					gar.NemoSVS = garantia.getNemoSVS();
					gar.IdTipoReajuste = garantia.getIdTipoReajuste();
					gar.EsPagaderoEn = garantia.getEsPagaderoEn();
					
					gar.FechaVencimientoInstrumento = garantia.getFechaVencimientoInstrumento();
					gar.EsFungible = garantia.getEsFungible();
					gar.CodigoIFRS = garantia.getCodigoIFRS();
					gar.IdMonedaInstrumento = garantia.getIdMonedaInstrumento();
					gar.TIFAfectoBeneficio = garantia.getTIFAfectoBeneficio();
					gar.TIFTasa = garantia.getTIFTasa();
					gar.ISIN = garantia.getISIN();
					gar.FechaFactorMonedaInstrumento = garantia.getFechaFactorMonedaInstrumento();
					gar.ValorFactorMonedaInstrumento = garantia.getValorFactorMonedaInstrumento();
					gar.Cantidad = garantia.getCantidad();
					gar.CorteMinimo = garantia.getCorteMinimo();
					gar.ValorRescate = garantia.getValorRescate();
					gar.PrecioGarantia = garantia.getPrecioGarantia();
					gar.TasaGarantia = garantia.getTasaGarantia();
					gar.MontoGarantia = garantia.getMontoGarantia();
					gar.IdCarga = 66;
					gar.MontoGarantiaMonedaGarantia = garantia.getMontoGarantiaMonedaInstrumento();
					gar.MontoGarantiaMonedaAdicional = garantia.getMontoGarantiaMonedaAdicional();
					gar.INGFechaOperacion = garantia.getINGFechaOperacion();
					gar.INGIdCondicionLiquidacion = garantia.getINGIdCondicionLiquidacion();
					gar.INGFechaLiquidacion = garantia.getINGFechaLiquidacion();
					gar.INGCantidad = garantia.getINGCantidad();
					gar.INGValorRescate = garantia.getINGValorRescate();
					gar.INGPrecioGarantia = garantia.getINGPrecioGarantia();
					gar.INGTasaGarantia = garantia.getINGTasaGarantia();
					gar.INGMontoATasaGarantiaActual = garantia.getINGMontoATasaGarantiaActual();
					gar.INGMontoGarantiaActualMonedaGarantia = garantia.getINGMontoGarantiaActualMonedaInstrumento();
					gar.INGMontoGarantiaActualMonedaAdicional = garantia.getINGMontoGarantiaActualMonedaAdicional();
					gar.INGPrecioMercadoActual = garantia.getINGPrecioMercadoActual();
					gar.INGTasaMercadoActual = garantia.getINGTasaMercadoActual();
					gar.INGMontoATasaMercadoActual = garantia.getINGMontoATasaMercadoActual();
					gar.INGMontoMercadoActualMonedaGarantia = garantia.getINGMontoMercadoActualMonedaInstrumento();
					gar.INGMontoMercadoActualMonedaAdicional = garantia.getINGMontoMercadoActualMonedaAdicional();
					gar.INGMontoATasaCompraActual = garantia.getINGMontoATasaCompraActual();
					gar.INGMontoCompraActualMonedaGarantia = garantia.getINGMontoCompraActualMonedaInstrumento();
					gar.INGMontoCompraActualMonedaAdicional = garantia.getINGMontoCompraActualMonedaAdicional();
					gar.INGTasaCorredora = garantia.getINGTasaCorredora();
					gar.INGFechaTasaCorredora = garantia.getINGFechaTasaCorredora();
					gar.INGMontoATasaCorredora = garantia.getINGMontoATasaCorredora();
					gar.INGMontoATasaCorredoraMonedaGarantia = garantia.getINGMontoATasaCorredoraMonedaInstrumento();
					gar.INGMontoATasaCorredoraMonedaAdicional = garantia.getINGMontoATasaCorredoraMonedaAdicional();
					gar.INGFechaCorteCupon = garantia.getINGFechaCorteCupon();
					gar.INGNumeroCupon = garantia.getINGNumeroCupon();
					gar.INGIdMonedaPagoCupon = garantia.getINGIdMonedaPagoCupon();
					gar.INGMontoCorteCupon = garantia.getINGMontoCorteCupon();
					gar.INGFechaPagoCorteCupon = garantia.getINGFechaPagoCorteCupon();
					gar.EGRFechaOperacion = garantia.getEGRFechaOperacion();
					gar.EGRIdTipoOperacion = garantia.getEGRIdTipoOperacion();
					gar.EGRIdOperacion = garantia.getEGRIdOperacion();
					gar.EGRIdMovimientoCarteraEnTransito = garantia.getEGRIdMovimientoCarteraEnTransito();
					gar.EGRIdTipoCanalIngreso = garantia.getEGRIdTipoCanalIngreso();
					gar.EGRIdCondicionLiquidacion = garantia.getEGRIdCondicionLiquidacion();
					gar.EGRFechaLiquidacion = garantia.getEGRFechaLiquidacion();
					gar.EGRIdTipoComprobante = garantia.getEGRIdTipoComprobante();
					gar.EGRFolioComprobante = garantia.getEGRFolioComprobante();
					gar.EGRFechaComprobante = garantia.getEGRFechaComprobante();
					gar.EGRFolioExternoReferencia = garantia.getEGRFolioExternoReferencia();
					gar.EGRIdConcepto = garantia.getEGRIdConcepto();
					gar.EGRIdSucursal = garantia.getEGRIdSucursal();
					
					//obtener con el codigoAgente EGR
					params = new HashMap<>();
					if(!garantia.getEGRCodigoAgente().equals("")) {
						params.put("CodigoAgente", garantia.getEGRCodigoAgente());
						List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
						gar.EGRIdAgente = agentes.get(0).IdEmpleadoEjecutivo;
							
					}else {
						gar.EGRIdAgente = (long) 0;
					}

					gar.EGRCantidad = garantia.getEGRCantidad();
					gar.EGRValorRescate = garantia.getEGRValorRescate();
					gar.EGRPrecio = garantia.getEGRPrecio();
					gar.EGRTasa = garantia.getEGRTasa();
					gar.EGRMonto = garantia.getEGRMonto();
					gar.EGRMontoMonedaGarantia = garantia.getEGRMontoMonedaInstrumento();
					gar.EGRMontoMonedaAdicional = garantia.getEGRMontoMonedaAdicional();
					
					int[] res = sql.batch(conn, SQL_INSERT_GARANTIA, Arrays.asList(gar), EntidadGarantia.class);
					for (int i : res) {
						if(i==1){
							salida.add(linea + " - GARANTIA A DOCUMENTO IDENTIDAD: " + garantia.getDocumentoIdentidadCliente() + ", INGRESADA CORRECTAMENTE");
						}else{
							salida.add(linea + " - GARANTIA A DOCUMENTO IDENTIDAD: " + garantia.getDocumentoIdentidadCliente() + ", NO INGRESADA");
						}
					}
			
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - GARANTIA A DOCUMENTO IDENTIDAD: " + garantia.getDocumentoIdentidadCliente() + ", NO INGRESADO");
				}
			}
			linea++;
		} catch (SQLException e1) {
			e1.printStackTrace();
			salida.add("ERROR AL OBTENER LLAVES");
		}
		
		return salida;

	}

}
