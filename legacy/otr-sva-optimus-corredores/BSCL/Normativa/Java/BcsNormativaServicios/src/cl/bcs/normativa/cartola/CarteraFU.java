package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraFU implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraFU");
	
	private final String SQL_SELECT_CARTERA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraFU_SelectCartera.sql");
	
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");

	public static class SalidaCarteraFU{
		public Long 		IdCliente;
		public Long 		IdPortafolio;		
		public Long 		IdCartera;
		public String 		DocumentoIdentidad;
		public String		CodigoPortafolio;
		public String 		IdTipoInstrumento;
		public Long       	IdInstrumento;		
		public String 		Nemotecnico;
		public String 		FechaVencimiento;
		public String 		Fecha;
		public String 		InteresAbierto;
		public BigDecimal 	CantidadContratos;
		public Long			TamanoContratos;
		public BigDecimal 	PrecioAnterior;
		public BigDecimal 	PrecioBase;
		public BigDecimal 	Resultado;
		public BigDecimal 	Saldo;
		public String 		Mercado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	private static final Logger logger = LogManager.getLogger(CarteraFU.class);
	private static final SimpleDateFormat FORMATO_YYYYMMDD     = new SimpleDateFormat("yyyy-MM-dd");
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::CarteraFU");
		
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		
		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			String IdMonedaSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");	
			
			List<Portafolio> portafolios = entrada.p_Datos.stream()
					.map(dato->{
						Portafolio portafolio = new Portafolio();
						portafolio.IdPortafolio = dato.IdPortafolio;
						portafolio.IdTrabajo = idTrabajo;
						return portafolio;
					})
					.collect(Collectors.toList());
			HashMap<String,Object> params = new HashMap<>();
			params.put("FechaSistema",fechaSistema);
			params.put("IdTrabajo",idTrabajo);
			
			sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portafolios, Portafolio.class);
			List<SalidaCarteraFU> salidaCarteraFU = sql.query(con, SQL_SELECT_CARTERA, params, SalidaCarteraFU.class);

			EntregarPreciosTasas entregarPreciosTasas = new EntregarPreciosTasas();
			EntregarPreciosTasas_Entrada entradaEntregarPreciosTasas = new EntregarPreciosTasas_Entrada();
			
			entradaEntregarPreciosTasas.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEntregarPreciosTasas.p_IdUsuario = entrada.p_IdUsuario;
			entradaEntregarPreciosTasas.p_IdModulo = entrada.p_IdModulo;
			entradaEntregarPreciosTasas.p_IdPais = idPaisEmpresa;
			
			
			for(SalidaCarteraFU precio : salidaCarteraFU){
				EntregarPreciosTasas_Entrada_DatosInstrumentos tasa = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
				tasa.IdRegistro = precio.IdCartera;
				tasa.TipoCartera = "0";
				tasa.IdUso = "CARTERA";
				tasa.IdTipoCriterioTasa = "0";
				tasa.IdClasificacionTipoInstrumento = "DV";
				tasa.IdTipoInstrumento = precio.IdTipoInstrumento;
				tasa.IdInstrumento = precio.IdInstrumento;
				tasa.Nemotecnico = precio.Nemotecnico;
				tasa.IdMoneda = IdMonedaSistema;
				tasa.FechaConsulta = new java.sql.Date(FORMATO_YYYYMMDD.parse(fechaSistema).getTime());
				tasa.PlazoResidual = 0L;
				entradaEntregarPreciosTasas.p_DatosInstrumentos.add(tasa);
			}

			EntregarPreciosTasas_Salida salidaPrecio = entregarPreciosTasas.logica(con, entradaEntregarPreciosTasas);
			
			HashMap<Long,Object> hPrecios = Utils.List2Hash(salidaPrecio.P_OUT_PRECIOSTASAS, "IdRegistro");
			
			List<SalidaCarteraFU> salidas = new ArrayList<SalidaCarteraFU>();		
			
		    if (!salidaCarteraFU.isEmpty()) {				    
			    for(SalidaCarteraFU salidaCarteraFut :  salidaCarteraFU) {
			    	EntregarPreciosTasas_Salida_PreciosTasas tasa = (EntregarPreciosTasas_Salida_PreciosTasas) hPrecios.get(salidaCarteraFut.IdCartera);
					BigDecimal precio = tasa != null && tasa.Precio !=null ? tasa.Precio : BigDecimal.ZERO;
			    	BigDecimal tamanoContratos = new BigDecimal(salidaCarteraFut.TamanoContratos);
			    	salidaCarteraFut.PrecioBase			= precio;
			    	salidaCarteraFut.Resultado			= salidaCarteraFut.CantidadContratos.multiply(tamanoContratos, MathContext.DECIMAL128).multiply(precio.subtract(salidaCarteraFut.PrecioAnterior));
			    	salidas.add(salidaCarteraFut);				    	
				}				    
		    }		
			
			List<HashMap<String,Object>> resultadoCarteraFU = Utils.List2ListHashGenericObject(salidas);
			
			out.P_OUT_SALIDA.put("CarteraFU", resultadoCarteraFU);
			System.out.println(resultadoCarteraFU);
		} catch (Exception e) {
			logger.debug("ERROR::ConsultarActivoPasivoCustodiaPortafolio", e);
			throw e;
		} finally {
			Portafolio portafolio = new Portafolio();
			portafolio.IdTrabajo = idTrabajo;
			List<Portafolio> portafolios = Arrays.asList(portafolio);
			sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, portafolios, Portafolio.class);
		}
		
		
		logger.info("FIN::CarteraFU");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2020-03-01");
		Objin.p_FechaHasta = Fecha.toDate("2020-03-20");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 1451920L;
		Objin.p_Datos.add(dato);
		CarteraFU  demo= new CarteraFU();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
