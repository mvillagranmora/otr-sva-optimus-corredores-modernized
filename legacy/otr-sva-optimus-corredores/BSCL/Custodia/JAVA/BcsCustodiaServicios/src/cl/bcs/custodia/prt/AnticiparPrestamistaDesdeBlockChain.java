package cl.bcs.custodia.prt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import java.math.BigDecimal;

import cl.bcs.custodia.prt.AnticiparEmprestito.AnticiparEmprestito_Entrada;
import cl.bcs.custodia.prt.AnticiparEmprestito.AnticiparEmprestito_Entrada_DatosEmprestito;
import cl.bcs.custodia.prt.AnticiparEmprestito.AnticiparEmprestito_Salida;
import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain.Usuario;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.lendingClients;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamistaAnticipado;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnticiparPrestamistaDesdeBlockChain implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarPrestamoDesdeBlockChain.class);
	
	private final String SQL_SELECT_EMPRESTITOS			= loadResourceToString("cl/bcs/custodia/sql/AnticiparPrestamistaDesdeBlockChain_SelectEmprestitos.sql");
	private final String SQL_SELECT_USUARIO				= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestitoDesdeBlockChain_SelectUsuario.sql");
	
	public static class AnticiparPrestamistaDesdeBlockChainEntrada extends BcsPojo {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String 						p_FechaAnticipo;
		public List<PrestamistaAnticipado>	p_PrestamistasAnticipados;
	}
	
	public static class AnticiparPrestamistaDesdeBlockChainSalida extends BcsPojo {
		public List<MensajeSalida>			P_OUT_MENSAJES			;
		public Long							P_OUT_ERRORESCONTADOR	;
		
		public AnticiparPrestamistaDesdeBlockChainSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR	= 0L;
		}
	}
	
	public static class Emprestito extends BcsPojo {
		public String		FolioSistemaExterno;
		public BigDecimal	INGCantidad;
		public String		DocumentoIdentidad;
		public Long			FolioEmprestito;
		public String		FechaEmprestito;
		public BigDecimal	PrecioInstrumento;
		public BigDecimal	INGTasaEmprestito;
		public String		IdCondicionLiquidacion;
		public String		FechaLiquidacion;
		public String		FechaVencimientoEmprestito;
		public String		EsDiaHabilPlazoPrestamo;
		public Long			FolioPrestamo;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public AnticiparPrestamistaDesdeBlockChainSalida logica( Connection conn, AnticiparPrestamistaDesdeBlockChainEntrada in ) throws Exception {
		
		AnticiparPrestamistaDesdeBlockChainSalida salida = new AnticiparPrestamistaDesdeBlockChainSalida();
		
		if ( in.p_PrestamistasAnticipados.size() == 0 ) return salida;
		
		String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		SQL sql 								= new SQL();
		HashMap<String,Object> hparam 			= new HashMap<String,Object>();
		
		hparam.put( "p_IdEmpresa"				, in.p_IdEmpresa );
		hparam.put( "p_FoliosSistemaExterno"	, in.p_PrestamistasAnticipados.stream().map( prestamista_anticipado -> prestamista_anticipado.FolioSistemaExterno ).collect( Collectors.toList() ) );
		List<Emprestito> emprestitos	= sql.query( conn, SQL_SELECT_EMPRESTITOS, hparam, Emprestito.class );
		hparam.clear();
		
		hparam.put( "p_IdEmpresa", in.p_IdEmpresa );
		hparam.put( "p_IdUsuario", in.p_IdUsuario );
		List<Usuario> usuarios = sql.query( conn, SQL_SELECT_USUARIO, hparam, Usuario.class );
		hparam.clear();
		Long lngIdEmpleado = usuarios.get(0).IdEntidad;
		
		ConsultarEmpresa consultarEmpresaService 	= new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long IdPaisEmpresa = empresa.IdPais;
		
		for ( Emprestito emprestito : emprestitos ) {
			
			BigDecimal cantidadAnticipar = new BigDecimal( "0" );
			
			List<PrestamistaAnticipado> prestamoPrestamistaAnticipado	= in.p_PrestamistasAnticipados.stream()
					.filter( prestamista_anticipado -> prestamista_anticipado.FolioSistemaExterno.equals( emprestito.FolioSistemaExterno ) )
					.collect( Collectors.toList() );
			List<lendingClients> emprestitoPrestamo 					= prestamoPrestamistaAnticipado.get( 0 ).Prestamistas.stream()
					.filter( prestamista -> prestamista.LendingClientRUT.equals( emprestito.DocumentoIdentidad ) )
					.collect( Collectors.toList() );
			
			if ( emprestitoPrestamo.size() > 0 ) {
				cantidadAnticipar	= emprestito.INGCantidad.subtract( new BigDecimal( emprestitoPrestamo.get( 0 ).LendingClientQty ) );
			} else {
				cantidadAnticipar	= emprestito.INGCantidad;
			}
			
			AnticiparEmprestito anticiparEmprestito 					= new AnticiparEmprestito();
			AnticiparEmprestito_Entrada anticiparEmprestitoEntrada 		= new AnticiparEmprestito_Entrada();
			AnticiparEmprestito_Salida anticiparEmprestitoSalida		= new AnticiparEmprestito_Salida();
			AnticiparEmprestito_Entrada_DatosEmprestito datosEmprestito = new AnticiparEmprestito_Entrada_DatosEmprestito();
			CrearPrestamoDesdeBlockChain crearPrestamoDesdeBlockChain	= new CrearPrestamoDesdeBlockChain();
			
			datosEmprestito.FolioEmprestito		= emprestito.FolioEmprestito;
			datosEmprestito.FechaEmprestito		= emprestito.FechaEmprestito;
			datosEmprestito.PrecioInstrumento	= emprestito.PrecioInstrumento;
			datosEmprestito.Cantidad			= cantidadAnticipar;
			datosEmprestito.MontoInstrumento	= crearPrestamoDesdeBlockChain.Monto( conn, in.p_IdEmpresa, in.p_IdUsuario, cantidadAnticipar, "ACN", emprestito.PrecioInstrumento );
			datosEmprestito.TasaPremio			= emprestito.INGTasaEmprestito;
			
			String strPlazo = crearPrestamoDesdeBlockChain.Plazo( conn, in.p_IdEmpresa, in.p_IdUsuario, usaFechaLiquidacionEnVencimiento.equalsIgnoreCase( ConstantesBCK.SI ) ? emprestito.FechaLiquidacion : emprestito.FechaEmprestito, emprestito.FechaVencimientoEmprestito, IdPaisEmpresa, emprestito.IdCondicionLiquidacion, "S", emprestito.EsDiaHabilPlazoPrestamo );
			
			datosEmprestito.MontoAPlazo				= crearPrestamoDesdeBlockChain.calcularMontoPlazo( datosEmprestito.MontoInstrumento, strPlazo , datosEmprestito.TasaPremio.toString() );
			datosEmprestito.MontoPremio				= datosEmprestito.MontoAPlazo.subtract( datosEmprestito.MontoInstrumento );
			datosEmprestito.FechaAnticipo			= in.p_FechaAnticipo;
			datosEmprestito.PlazoAlVencimiento		= Integer.valueOf( strPlazo );
			datosEmprestito.IdPais					= IdPaisEmpresa;
			datosEmprestito.IdMonedaLiquidacion		= ConstantesBCK.MONEDA;
			datosEmprestito.IdCondicionLiquidacion	= emprestito.IdCondicionLiquidacion;
			datosEmprestito.FechaLiquidacion		= emprestito.FechaLiquidacion;
			datosEmprestito.DevolverCustodia		= "S";
			datosEmprestito.IdEmpleadoDigitador		= lngIdEmpleado;
			datosEmprestito.FolioPrestamo			= emprestito.FolioPrestamo;
			
			anticiparEmprestitoEntrada.p_IdEmpresa					= in.p_IdEmpresa;
			anticiparEmprestitoEntrada.p_IdUsuario					= in.p_IdUsuario;
			anticiparEmprestitoEntrada.p_IdModulo					= in.p_IdModulo;
			anticiparEmprestitoEntrada.p_UsaReglasNegocio			= "S";
			anticiparEmprestitoEntrada.p_IdPais						= IdPaisEmpresa;
			anticiparEmprestitoEntrada.p_RebajaPrestamo				= "S";
			anticiparEmprestitoEntrada.p_AnticiparPrestamoAsociado	= "N";
			anticiparEmprestitoEntrada.p_FechaAnticipo				= in.p_FechaAnticipo;
			anticiparEmprestitoEntrada.p_DatosEmprestitos.add( datosEmprestito );
			
			anticiparEmprestitoSalida = anticiparEmprestito.logica( conn, anticiparEmprestitoEntrada );
			
			salida.P_OUT_ERRORESCONTADOR += anticiparEmprestitoSalida.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll( 
					anticiparEmprestitoSalida.P_OUT_MENSAJES.stream()
					.map( mensaje_salida -> new MensajeSalida( emprestito.FolioEmprestito, mensaje_salida.tipo, mensaje_salida.codigo, mensaje_salida.descripcion ) )
					.collect( Collectors.toList() )
					);
		}
		
		return salida;
	}

}
