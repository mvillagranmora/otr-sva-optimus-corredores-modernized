package cl.bcs.custodia.prt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.custodia.prt.ModificarPrestamo.ModificarPrestamo_Entrada;
import cl.bcs.custodia.prt.ModificarPrestamo.ModificarPrestamo_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoModificado;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPrestamoDesdeBlockChain implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarPrestamoDesdeBlockChain.class);
	
	private final String SQL_SELECT_PRESTAMO				= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamoDesdeBlockChain_SelectPrestamo.sql");
	private final String SQL_SELECT_IDCLIENTE	 			= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectPersonaCliente.sql");
	private final String SQL_SELECT_CLIENTE_CUSTODIA		= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectClienteCustodia.sql");
	
	public static class ModificarPrestamoDesdeBlockChainEntrada extends BcsPojo {
		public Long									p_IdEmpresa;
		public Long									p_IdUsuario;
		public String								p_FechaOperacion;
		public List<PrestamoModificado>				p_PrestamosModificado;
	}
	
	public static class ModificarPrestamoDesdeBlockChainSalida extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public ModificarPrestamoDesdeBlockChainSalida() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<>();
		}
	}
	
	public static class Prestamo extends BcsPojo {
		public Long			IdCartera;
		public Long			IdSubyacente;
		public Long 		FolioPrestamo;
		public BigDecimal	TasaPrestamo;
		public String 		FechaVencimientoPrestamo;
		public String 		FolioSistemaExterno;
		public String		IdTipoPrestamo;
		public BigDecimal	INGPrecioInstrumento;
		public BigDecimal 	INGCantidad;
		public String		FechaLiquidacion;
		public String		INGFechaOperacion;
		public String		IdCondicionLiquidacion;
		public String 		EsDiaHabilPlazoPrestamo;
	}
	
	public static class Cliente extends BcsPojo {
		public String	DocumentoIdentidad;
		public Long 	IdCliente;
	}
	
	public static class ClienteCustodia extends BcsPojo {
		public String 	TieneContrato;
		public Long 	IdCliente;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ModificarPrestamoDesdeBlockChainSalida logica( Connection conn, ModificarPrestamoDesdeBlockChainEntrada in ) throws Exception {
		
		ModificarPrestamoDesdeBlockChainSalida salida = new ModificarPrestamoDesdeBlockChainSalida();
		
		if ( in.p_PrestamosModificado.size() == 0 ) return salida;
		
		SQL sql 									= new SQL();
		HashMap<String,Object> parametrosQuery 		= new HashMap<String,Object>();
		HashMap<String,Object> hPrestamos 			= new HashMap<String,Object>();
		HashMap<String,Object> hClientesOptimus 	= new HashMap<String,Object>();
		HashMap<Long,Object> hClientesCustodia 		= new HashMap<Long,Object>();
		String usaFechaLiquidacionEnVencimiento		= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		parametrosQuery.put( "p_IdEmpresa"				, in.p_IdEmpresa );
		parametrosQuery.put( "p_FoliosSistemaExterno"	, in.p_PrestamosModificado.stream().map( prestamo_modificado -> prestamo_modificado.laDetails.LAID ).collect( Collectors.toList() ) );
		List<Prestamo> prestamos = sql.query( conn, SQL_SELECT_PRESTAMO, parametrosQuery, Prestamo.class );
		parametrosQuery.clear();
		hPrestamos = Utils.List2Hash( prestamos, "FolioSistemaExterno" );
		
		parametrosQuery.put( "p_DocumentoIdentidad"	, in.p_PrestamosModificado.stream().map( prestamo_modificado -> prestamo_modificado.borrowingClients.BorrowingClientRUT ).collect( Collectors.toList() ) );
		parametrosQuery.put( "p_IdEmpresa"			, in.p_IdEmpresa );
		List<Cliente> clientes = sql.query( conn, SQL_SELECT_IDCLIENTE, parametrosQuery, Cliente.class );
		parametrosQuery.clear();
		hClientesOptimus = Utils.List2Hash( clientes, "DocumentoIdentidad" );
		
		parametrosQuery.put( "p_IdCliente", 	clientes.stream().map( cliente -> cliente.IdCliente ).collect( Collectors.toList() ) );
		List<ClienteCustodia> clientesCustodia = sql.query(conn, SQL_SELECT_CLIENTE_CUSTODIA, parametrosQuery, ClienteCustodia.class);
		parametrosQuery.clear();
		hClientesCustodia = Utils.List2Hash( clientesCustodia, "IdCliente" );
		
		
		for ( PrestamoModificado prestamoModificado : in.p_PrestamosModificado ) {
			
			Boolean clienteOperaCustodiaExterna = false;
			Prestamo prestamoOptimus 	= ( Prestamo ) hPrestamos.get( prestamoModificado.laDetails.LAID );
			Cliente clienteOptimus 		= ( Cliente ) hClientesOptimus.get( prestamoModificado.borrowingClients.BorrowingClientRUT );
			
			if ( clienteOptimus == null ) {
				salida.P_OUT_ERRORESCONTADOR += 1L;
				salida.P_OUT_MENSAJES.add(  new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "BC_CLIENTE_NO_EXISTE", new HashMap<>(), conn, in.p_IdEmpresa ) );
				continue;
			}
			
			ClienteCustodia clienteCustodia		= ( ClienteCustodia ) hClientesCustodia.get( clienteOptimus.IdCliente );
			if ( clienteCustodia != null ) {
				clienteOperaCustodiaExterna = clienteCustodia.TieneContrato.equalsIgnoreCase(ConstantesBCK.NO) ? true : false;
			}
			
			if ( prestamoOptimus == null ) {
				salida.P_OUT_ERRORESCONTADOR += 1L;
				salida.P_OUT_MENSAJES.add(  new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "BC_PRESTAMO_NO_EXISTE", new HashMap<>(), conn, in.p_IdEmpresa ) );
				continue;
			}
			
			if ( new BigDecimal( prestamoModificado.laDetails.Yield ).compareTo( prestamoOptimus.TasaPrestamo ) == 0
					&& ( prestamoModificado.laDetails.MaturityDate.equals( prestamoOptimus.FechaVencimientoPrestamo ) 
							&& ( prestamoOptimus.IdTipoPrestamo.equalsIgnoreCase("PRTADM") || (prestamoOptimus.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") && clienteOperaCustodiaExterna) ) ) ) {
				continue;
			}
			
			CrearPrestamoDesdeBlockChain crearPrestamoDesdeBlockChain 	= new CrearPrestamoDesdeBlockChain();
			ModificarPrestamo modificarPrestamo 						= new ModificarPrestamo();
			ModificarPrestamo_Entrada modificarPrestamoEntrada 			= new ModificarPrestamo_Entrada();
			ModificarPrestamo_Salida modificarPrestamoSalida 			= new ModificarPrestamo_Salida();
			
			modificarPrestamoEntrada.p_IdEmpresa 			= in.p_IdEmpresa;
			modificarPrestamoEntrada.p_IdUsuario			= in.p_IdUsuario;
			modificarPrestamoEntrada.p_IdModulo				= "CUS";
			modificarPrestamoEntrada.p_IdCartera			= prestamoOptimus.IdCartera;
			modificarPrestamoEntrada.p_IdSubyacente			= prestamoOptimus.IdSubyacente;
			modificarPrestamoEntrada.p_FechaVencimiento		= prestamoModificado.laDetails.MaturityDate;
			modificarPrestamoEntrada.p_PrecioInstrumento	= prestamoOptimus.INGPrecioInstrumento;
			modificarPrestamoEntrada.p_TasaInstrumento		= new BigDecimal( prestamoModificado.laDetails.Yield );
			modificarPrestamoEntrada.p_Monto 				= crearPrestamoDesdeBlockChain.Monto(conn, in.p_IdEmpresa, in.p_IdUsuario, prestamoOptimus.INGCantidad, "ACN", prestamoOptimus.INGPrecioInstrumento);
			modificarPrestamoEntrada.p_MontoMdaInstr 		= modificarPrestamoEntrada.p_Monto;
			
			String strPlazo = crearPrestamoDesdeBlockChain.Plazo( conn, in.p_IdEmpresa, in.p_IdUsuario, usaFechaLiquidacionEnVencimiento.equalsIgnoreCase( ConstantesBCK.SI ) ? prestamoOptimus.FechaLiquidacion : prestamoOptimus.INGFechaOperacion, prestamoModificado.laDetails.MaturityDate, in.p_IdEmpresa, prestamoOptimus.IdCondicionLiquidacion, "S", prestamoOptimus.EsDiaHabilPlazoPrestamo );
			
			modificarPrestamoEntrada.p_MontoAPlazo 			= crearPrestamoDesdeBlockChain.calcularMontoPlazo( modificarPrestamoEntrada.p_Monto, strPlazo, prestamoModificado.laDetails.Yield );
			modificarPrestamoEntrada.p_MontoPrima 			= modificarPrestamoEntrada.p_MontoAPlazo.subtract( modificarPrestamoEntrada.p_Monto ); 
			modificarPrestamoEntrada.p_TasaPrima 			= new BigDecimal( prestamoModificado.laDetails.Yield );
			modificarPrestamoEntrada.p_PlazoEnDias 			= Long.valueOf( strPlazo );
			modificarPrestamoEntrada.p_InteresDiario 		= new BigDecimal( Math.ceil( ( modificarPrestamoEntrada.p_Monto.doubleValue() * ( modificarPrestamoEntrada.p_TasaPrima.doubleValue() / 100 ) ) / 30 ) );
			modificarPrestamoEntrada.p_InteresAcumulado		= calcularInteresAcumulado( modificarPrestamoEntrada.p_InteresDiario, prestamoOptimus.INGFechaOperacion, in.p_FechaOperacion );
			modificarPrestamoEntrada.p_FolioSistemaExterno	= prestamoModificado.laDetails.LAID;
			
			modificarPrestamoSalida = modificarPrestamo.logica( conn, modificarPrestamoEntrada );
			
			salida.P_OUT_ERRORESCONTADOR += modificarPrestamoSalida.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll( 
					modificarPrestamoSalida.P_OUT_MENSAJES.stream()
					.map( mensaje -> new MensajeSalida( prestamoOptimus.FolioPrestamo, mensaje.tipo, mensaje.codigo, mensaje.descripcion ) )
					.collect( Collectors.toList() ) 
					);
		}
		
		
		return salida;
	}
	
	public BigDecimal calcularInteresAcumulado( BigDecimal interesDiario, String fechaDesde, String fechaHasta ) {
		
		SimpleDateFormat dateFormat 		= new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			
			Date fechaInicial   = dateFormat.parse(fechaDesde); 
			Date fechaFinal		= dateFormat.parse(fechaHasta);
			
			if (fechaInicial.after(fechaFinal)) {
				return new BigDecimal( "-1" );
			} else {
				double dias = Math.ceil( ( fechaFinal.getTime() - fechaInicial.getTime() ) / (1000 * 60 * 60 * 24 ) );
				return new BigDecimal( Math.ceil( dias * interesDiario.doubleValue() ) );
			}
			
		}
		catch ( Exception e ) {
			return new BigDecimal( "0" );
		}
	}

}
