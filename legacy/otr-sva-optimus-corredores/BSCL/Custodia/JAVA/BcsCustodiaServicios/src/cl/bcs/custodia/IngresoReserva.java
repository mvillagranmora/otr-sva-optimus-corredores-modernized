package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoReserva implements JavaCall{

	private static final Logger logger = LogManager.getLogger(IngresoReserva.class);
	
	private final String SQL_SELECT_CARTERA_RESERVADA = loadResourceToString("cl/bcs/custodia/sql/CustodiaConsultarCarteraReservada.sql");
	private final String SQL_SELECT_RESERVA = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_SelectReserva.sql");
	private final String SQL_INSERT_RESERVA = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_InsertReserva.sql");
	private final String SQL_DELETE_RESERVA = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_DeleteReserva.sql");
	private final String SQL_UPDATE_RESERVA = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_UpdateReserva.sql");
	
	private final String SQL_INSERT_FOLIOS_CONSULTA  = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_InsertFolioConsulta.sql");
	private final String SQL_DELETE_FOLIOS_CONSULTA  = loadResourceToString("cl/bcs/custodia/sql/IngresoReserva_DeleteFolioConsulta.sql");
	
	public static class ReservaObj_IN extends BcsPojo {
		public Long IdOperacion;//			BIGINT
		public Long IdRegistro;//			BIGINT
		public Long IdReserva;//				BIGINT
		public String IdConcepto;//			NVARCHAR(6)	-- INGRES / RETRES / INGDET / RETDET / LIMDET
		public Long FolioOperacion;//		BIGINT
		public Long FolioCartera;// 			BIGINT 
		public Long FolioCarteraCustodia;// 	BIGINT     									
		public Long IdCartera;// 			BIGINT 
		public Long IdCarteraCustodia;// 	BIGINT
		public String IdTipoProducto;//		NVARCHAR(4)
		public BigDecimal Cantidad;// 				DECIMAL(26,8)
		public BigDecimal Tasa;// 					DECIMAL(26,8)
		public BigDecimal TasaMercado;//          DECIMAL(26,8)
		public BigDecimal Precio;//				DECIMAL(26,8)
		public BigDecimal PrecioMercado;//        DECIMAL(26,8)
		public BigDecimal ValorRescate;//			DECIMAL(26,8)
		public BigDecimal MontoValorizado;//		DECIMAL(26,8)
		public BigDecimal MontoLiquidacion;//		DECIMAL(26,8)
		public BigDecimal MontoLiquidacionOperacion;//		DECIMAL(26,8)
		public String IdClasificacionTipoInstrumento;// NVARCHAR(3)
		public String IdTipoInstrumento;//		NVARCHAR(3)
		public Long IdInstrumento;//			BIGINT
		public String Nemotecnico;// NVARCHAR(40)
		public String IdMoneda;//NVARCHAR(3)
		public String IdTipoReajuste;//		NVARCHAR(3)
		public String IdMonedaEmisionInstrumento;//NVARCHAR(3)
		public String IdMonedaLiquidacionInstrumento;//NVARCHAR(3)
		public String IdMonedaLiquidacionOperacion;//NVARCHAR(3)
		public String IdEmisor;//				NVARCHAR(40)
		public BigDecimal CorteMinimo;//			DECIMAL(26,8)
		public String FechaVencimiento;//		DATE
		public String EsFungible;//			NVARCHAR(1)
		public String Glosa;//					NVARCHAR(260)
		public String IdBolsa;//				NVARCHAR(3)
		public String IdBoveda;//			NVARCHAR(3)	
		public String CodigoIFRS;//			NVARCHAR(40)
		public String FechaOperacion;
		
		public BigDecimal MontoValorizadoReserva ;//		DECIMAL(26,8)
		public BigDecimal MontoFinalCarteraMonedaSistema;
		public BigDecimal MontoFinalCarteraMonedaInstrumento;
		
		public BigDecimal INGMontoAPlazoDetalle;
		public BigDecimal INGMontoAPlazoMonedaInstrumentoDetalle;

	}
	
	public static class IngresoReserva_IN extends BcsPojo {
		public Long p_IdEmpresa;// 				BIGINT,
		public Long p_IdUsuario;// 				BIGINT,
		public String p_IdModulo;//				NVARCHAR(3),	-- CUS / MES
		public String p_simularOperacion;//		nvarchar(1),	-- S/N se usa para simular operacion (algunos casos) 
		public String p_UsaReglasNegocio;//	 	nvarchar(1),	-- SN se usa en caso de servicios anidados.
		public Long p_IdPais;// 				BIGINT,
		public List<ReservaObj_IN> p_TblCarteraReserva;
		
		public IngresoReserva_IN() {
			this.p_TblCarteraReserva = new ArrayList<ReservaObj_IN>();
		}

	}
	
	public static class ReservaObj_OUT extends BcsPojo {
		public Long IdRegistro;//		BIGINT
		public Long IdReserva;//			BIGINT
		public Long IdOperacion;//		BIGINT
		public String Estado;
	}
	
	public static class IngresoReserva_OUT extends BcsPojo {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ReservaObj_OUT>		P_OUT_RESERVA;
		
		public IngresoReserva_OUT() {
			this.P_OUT_RESERVA 		= new ArrayList<ReservaObj_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}

	}
	
	public static class CarteraReservada extends BcsPojo {
		public Long IdMesa;
        public Long FolioCartera;
        public String Nemotecnico;
        public String IdEmisor;
        public String IdMoneda;
        public String IdTipoReajuste;
        public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
        public BigDecimal Cantidad;
        public Long IdEmpresa;
        public Long IdCliente;
        public Long IdPortafolio;
        public Date INGFechaOperacion;
        public Date FechaLiquidacion;
        public Long IdInstrumento;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String TipoInstrumento;
        public BigDecimal CorteMinimo;
        public BigDecimal PrecioCompra;
        public BigDecimal TasaCompra;
        public BigDecimal MontoCompra;
        public BigDecimal MontoLiquidacion;
        public BigDecimal MontoLiquidacionOperacion;
        public BigDecimal INGPrecioMercadoActual;
        public BigDecimal INGTasaMercadoActual;
        public BigDecimal INGMontoATasaMercadoActual;
        public Date FechaVencimientoInstrumento;
        public Date FechaVencimientoOperacion;
        public String EsFungible;
        public String CodigoIFRS;
        public String IdMonedaInstrumento;
        public BigDecimal CantidadTotalReservada;
        public BigDecimal CantidadReservadaCus;
        public Long IdUsrReserva;
        public String CodUsrReserva;
        public BigDecimal CantidadReservadaMes;
	}
	
		
	public static class Reserva extends BcsPojo {
		public Long IdReserva;
		public Long IdRegistro;
		public Long IdOperacion;
		public Long FolioCarteraOrigen;
		public Long FolioCarteraCustodia;
		public Long IdCarteraOrigen;
		public Long IdCarteraCustodia;
		public Long FolioOperacion;
	    public String IdEstado;
	    public Long IdUsuarioCreacion;
	    public String IdTipoProducto;
	    public String IdConcepto;
	    public BigDecimal Tasa;
	    public BigDecimal Precio;
	    public BigDecimal MontoValorizado;
	    public BigDecimal MontoLiquidacion;
	    public BigDecimal MontoLiquidacionOperacion;
	    public String IdClasificacionTipoInstrumento;
	    public Long IdInstrumento;
	    public String Nemotecnico;
	    public String IdTipoInstrumento;
	    public String IdMoneda;
	    public String IdTipoReajuste;
	    public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
	    public String IdEmisor;
	    public Date FechaVencimiento;
	    public String EsFungible;
	    public String IdBolsa;
	    public String IdBoveda;
	    public String CodigoIFRS;
	    public BigDecimal CorteMinimo;
	    public BigDecimal CantidadInicial;
	    public BigDecimal ValorRescateInicial;
	    public BigDecimal MontoValorizadoReserva;
	    public Date FechaVencimientoReserva;
	    public Date FechaVencimientoProducto;
	    public Date FechaCreacion;
	    public Long IngIdUsuario;
	    public BigDecimal IngCantidad;
	    public BigDecimal IngValorRescate;
	    public BigDecimal IngMontoValorizado;
	    public Date IngFecha;
	    public Long EgrIdUsuario;
	    public BigDecimal EgrCantidad;
	    public BigDecimal EgrValorRescate;
	    public BigDecimal EgrMontoValorizado;
	    public Date EgrFecha;
	    public Long SecuenciaCompromisoMigracion;
	    public Long IdCarga;
	    public BigDecimal PrecioMercado;
	    public BigDecimal TasaMercado;
	    public BigDecimal INGMontoAPlazo;
	    public BigDecimal INGMontoAPlazoMonedaInstrumento;
		public Integer SecMovCliIngreso;
		public Integer SecMovCorIngreso;
		public Integer SecMovCliLiquidacion;
		public Integer SecMovCorLiquidacion;
	    
	    public Reserva() {
	    	this.IdCarga=0L;
	    }
	}
	
	public static class FoliosConsultaPaso extends BcsPojo {
		public Long IdTrabajo;
		public Long FolioCartera;	
		public Long IdReserva;
		
		public FoliosConsultaPaso() {
			this.IdTrabajo = 0L;
			this.FolioCartera = 0L;
			this.IdReserva = 0L;
		}
	}	
	
	public IngresoReserva_OUT logica (Connection conn, IngresoReserva_IN in) throws Exception {
		
		Long init = System.currentTimeMillis();
		BigDecimal CantidadDisponible=BigDecimal.ZERO;
		logger.info("Inicio servicio CUS.IngresoReserva");
		
		SQL _sql = new SQL();
		IngresoReserva_OUT out = new IngresoReserva_OUT();
		Long Id = 0L;

		HashMap<String,String> mapMsg = new HashMap<>();
		

		HashMap<String, Object> params = new HashMap<>();
		Long countFolioCarteras = 0L;
		
		List<FoliosConsultaPaso> lstFoliosConsultaPaso = new ArrayList<FoliosConsultaPaso>();
		
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		
		try {
			
			for (ReservaObj_IN res: in.p_TblCarteraReserva)
			{
				FoliosConsultaPaso objFolioConsultaPaso = new FoliosConsultaPaso();
				objFolioConsultaPaso.IdTrabajo = idTrabajo;
				
				if (res.FolioCartera.compareTo(0L) > 0)
				{
					objFolioConsultaPaso.FolioCartera = res.FolioCartera;
					countFolioCarteras++;
				}
				if (res.IdReserva.compareTo(0L) > 0)
				{	
					objFolioConsultaPaso.IdReserva = res.IdReserva;
				}
				lstFoliosConsultaPaso.add(objFolioConsultaPaso);
				
			}
				
			if (lstFoliosConsultaPaso.size() > 0)
			{
				_sql.batch(conn, SQL_INSERT_FOLIOS_CONSULTA, lstFoliosConsultaPaso, FoliosConsultaPaso.class); //Inserta folios de comsulta en tabla temporal
			}

			
			HashMap<Long, Object> carteraReservadaHash = new HashMap<Long, Object>(); 
			if (countFolioCarteras > 0){
				HashMap<String, Object> parametrosCarteraReservada = new HashMap<String, Object>(2);
				parametrosCarteraReservada.put("IdTrabajo", idTrabajo);
				List<CarteraReservada> carteraReservada = _sql.query(conn, SQL_SELECT_CARTERA_RESERVADA, parametrosCarteraReservada, CarteraReservada.class);
				
				carteraReservadaHash = Utils.List2Hash(carteraReservada, "FolioCartera");
			}
						
			params = new HashMap<>();
			params.put("TipoConsulta", "ReservaId");
			params.put("IdTrabajo", idTrabajo);
			List<Reserva> datosReservaId = _sql.query(conn, SQL_SELECT_RESERVA, params, Reserva.class);
			
			params = new HashMap<>();
			params.put("TipoConsulta", "ReservaFolio");
			params.put("IdTrabajo", idTrabajo);
			List<Reserva> datosReservaFolio = _sql.query(conn, SQL_SELECT_RESERVA, params, Reserva.class);
			
			HashMap<Long, Object> datosReservaIdHash = Utils.List2Hash(datosReservaId, "IdReserva");
				
			//::Validaciones
			for (ReservaObj_IN resIn: in.p_TblCarteraReserva){
				Boolean EliminaReserva = false;
				if ((resIn.IdConcepto.equalsIgnoreCase("INGRES") 
						&& resIn.IdReserva.compareTo(0L) > 0 
						&& resIn.Cantidad.equals(BigDecimal.ZERO)) 
						|| (resIn.IdConcepto.equalsIgnoreCase("RETRES"))){
					EliminaReserva =true;
				}else {
					EliminaReserva =false;
				}
				
				Reserva tblReserva = (Reserva)datosReservaIdHash.get(resIn.IdReserva);
				if (resIn.IdReserva.equals(0L) && resIn.Cantidad.compareTo(BigDecimal.ZERO) > 0){ //son nominales
					break;
				}				
				if (!resIn.IdReserva.equals(0L) && tblReserva == null && EliminaReserva ==false){
					//no existe la reserva
					mapMsg.put("IdReserva", resIn.IdReserva.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_RESERVA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				CarteraReservada tblcarteraReservada = (CarteraReservada) carteraReservadaHash.get(resIn.FolioCartera);
				
				if (!resIn.FolioCartera.equals(0L) && tblcarteraReservada == null && EliminaReserva ==false){
					// No Existe la cartera
					mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_FOLIO_CARTERA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (resIn.IdConcepto.equals("RETRES") && resIn.IdTipoProducto.equals("CUST") && tblcarteraReservada.IdUsrReserva != null && !tblcarteraReservada.IdUsrReserva.equals(in.p_IdUsuario)){
					// No Existe la cartera
					mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_FOLIO_CARTERA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			
				
				if (tblReserva != null && tblcarteraReservada != null){
					CantidadDisponible = tblcarteraReservada.Cantidad.add(resIn.Cantidad);
					
					if (resIn.EsFungible.equalsIgnoreCase("N")){
						
						if (resIn.Cantidad.compareTo(tblcarteraReservada.Cantidad) > 0 && (resIn.IdConcepto.equalsIgnoreCase("INGRES") &&  resIn.IdTipoProducto.equalsIgnoreCase("CUST"))){
							//RESERVA_SUPERA_CANTIDAD_DISPONIBLE
						
							mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
							mapMsg.put("Reservado", tblReserva.CantidadInicial.setScale(0).toString());
							mapMsg.put("CantidadDisponible", CantidadDisponible.setScale(0).toString());
							
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"RESERVA_SUPERA_CANTIDAD_DISPONIBLE",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					
						
						if (resIn.Cantidad.compareTo(tblcarteraReservada.Cantidad.add(tblReserva.CantidadInicial)) > 0 && (resIn.IdConcepto.equalsIgnoreCase("INGRES") &&  resIn.IdTipoProducto.equalsIgnoreCase("MERF") && resIn.FolioCartera.compareTo(0L) > 0)){
							//RESERVA_SUPERA_CANTIDAD_DISPONIBLE
							mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
							mapMsg.put("Reservado", tblReserva.CantidadInicial.setScale(0).toString());
							mapMsg.put("CantidadDisponible", CantidadDisponible.setScale(0).toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"RESERVA_SUPERA_CANTIDAD_DISPONIBLE",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					
						if (resIn.Cantidad.compareTo(tblcarteraReservada.CantidadReservadaCus) > 0 && resIn.IdConcepto.equalsIgnoreCase("RETRES") && resIn.IdTipoProducto.equalsIgnoreCase("CUST") && (tblcarteraReservada.IdUsrReserva == null || tblcarteraReservada.IdUsrReserva.equals(in.p_IdUsuario))){
							//LIBERACION_SUPERA_CANTIDAD_RESERVADA
							mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
							mapMsg.put("Reservado", tblReserva.CantidadInicial.setScale(0).toString());
							mapMsg.put("CantidadDisponible", CantidadDisponible.setScale(0).toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"LIBERACION_SUPERA_CANTIDAD_RESERVADA",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					}
					else {
						
						if (resIn.ValorRescate.compareTo(tblcarteraReservada.CantidadTotalReservada) > 0 && (resIn.IdConcepto.equalsIgnoreCase("INGRES") &&  resIn.IdTipoProducto.equalsIgnoreCase("CUST"))){
							//RESERVA_SUPERA_CANTIDAD_DISPONIBLE
							mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
							mapMsg.put("Reservado", tblReserva.CantidadInicial.setScale(0).toString());
							mapMsg.put("CantidadDisponible", CantidadDisponible.setScale(0).toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"RESERVA_SUPERA_CANTIDAD_DISPONIBLE",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					
						if (resIn.ValorRescate.compareTo(tblcarteraReservada.Cantidad.add(tblReserva.ValorRescateInicial)) > 0 && (resIn.IdConcepto.equalsIgnoreCase("INGRES") &&  resIn.IdTipoProducto.equalsIgnoreCase("MERF") && resIn.FolioCartera.compareTo(0L) > 0)){
							//RESERVA_SUPERA_CANTIDAD_DISPONIBLE
							mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
							mapMsg.put("Reservado", resIn.ValorRescate.setScale(0).toString());
							mapMsg.put("CantidadDisponible", tblcarteraReservada.Cantidad.add(tblReserva.ValorRescateInicial).setScale(0).toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"RESERVA_SUPERA_CANTIDAD_DISPONIBLE",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					
						if (resIn.ValorRescate.compareTo(tblcarteraReservada.CantidadReservadaCus) > 0 && resIn.IdConcepto.equalsIgnoreCase("RETRES") && resIn.IdTipoProducto.equalsIgnoreCase("CUST") && (tblcarteraReservada.IdUsrReserva == null || tblcarteraReservada.IdUsrReserva.equals(in.p_IdUsuario))){
							//LIBERACION_SUPERA_CANTIDAD_RESERVADA
							mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
							mapMsg.put("Reservado", tblReserva.CantidadInicial.setScale(0).toString());
							mapMsg.put("CantidadDisponible", CantidadDisponible.setScale(0).toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"LIBERACION_SUPERA_CANTIDAD_RESERVADA",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					}
				}
				
				if (resIn.CorteMinimo.compareTo(BigDecimal.ZERO) > 0 && 
						(resIn.EsFungible.equalsIgnoreCase("N") && resIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") &&  resIn.ValorRescate.remainder(resIn.CorteMinimo).compareTo(BigDecimal.ZERO) > 0) ||
						((resIn.EsFungible.equalsIgnoreCase("S") && resIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) &&  resIn.ValorRescate.remainder(resIn.CorteMinimo).compareTo(BigDecimal.ZERO) > 0)){
					//CORTE_MINIMO_INVALIDO
					mapMsg.put("FolioCartera", resIn.FolioCartera.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CORTE_MINIMO_INVALIDO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
			
			//datos para ingreso de reserva
			List<Reserva> datosReservaInsert = new ArrayList<>();
			List<Reserva> datosReservaDelete = new ArrayList<>();
			List<Reserva> datosReservaUpdate = new ArrayList<>();
			
			for (ReservaObj_IN res: in.p_TblCarteraReserva){
				if (res.IdConcepto.equalsIgnoreCase("INGRES") 
						&& res.IdReserva.equals(0L) 
						&& res.Cantidad.compareTo(BigDecimal.ZERO) > 0){
					//nuevas reservas
					Reserva rs = new Reserva();
					rs = copyPojoEntradaReserva(res);
					
					rs.IdReserva = 0L;//secuenciaReserva.get(IdSecuenciaReserva);
					rs.FolioCarteraOrigen = res.FolioCartera; 
					rs.IdCarteraOrigen = res.IdCartera; 
					rs.IdCarteraCustodia = res.IdCarteraCustodia;
					rs.IdEstado = "VIG"; 
					rs.IdUsuarioCreacion = in.p_IdUsuario; 
					rs.IdMonedaEmisionInstrumento = res.IdMonedaEmisionInstrumento;
					rs.IdMonedaLiquidacionInstrumento = res.IdMonedaLiquidacionInstrumento;
					rs.IdMonedaLiquidacionOperacion = res.IdMonedaLiquidacionOperacion;
					//----
					rs.CantidadInicial = res.Cantidad;
					rs.ValorRescateInicial = res.ValorRescate;
					rs.MontoValorizadoReserva   = res.MontoValorizadoReserva == null ? BigDecimal.ZERO : res.MontoValorizadoReserva;
					rs.FechaVencimientoReserva = Fecha.toDate("0001-01-01"); 
					rs.FechaVencimientoProducto = Fecha.toDate("0001-01-01"); 
					rs.FechaCreacion = Fecha.toDate(res.FechaOperacion);
					rs.IngIdUsuario = in.p_IdUsuario; 
					rs.IngCantidad = res.Cantidad;
					rs.IngValorRescate = res.ValorRescate;
					rs.IngMontoValorizado = BigDecimal.ZERO; 
					rs.IngFecha = Fecha.toDate(res.FechaOperacion); 
					rs.EgrIdUsuario = 0L;
					rs.EgrCantidad = BigDecimal.ZERO;
					rs.EgrValorRescate = BigDecimal.ZERO;
					rs.EgrMontoValorizado = BigDecimal.ZERO;
					rs.EgrFecha = Fecha.toDate("0001-01-01");
					rs.INGMontoAPlazo = res.MontoFinalCarteraMonedaSistema;
					rs.INGMontoAPlazoMonedaInstrumento = res.MontoFinalCarteraMonedaInstrumento;
					
					datosReservaInsert.add(rs);
				}
				
				if ((res.IdConcepto.equalsIgnoreCase("INGRES") 
						&& res.IdReserva.compareTo(0L) > 0 
						&& res.Cantidad.equals(BigDecimal.ZERO)) 
						|| (res.IdConcepto.equalsIgnoreCase("RETRES"))){
					//se eliminan las reservas existentes con cantidad cero 
					Reserva rs = new Reserva();
					rs = copyPojoEntradaReserva(res);
					
					rs.IdReserva = res.IdReserva;
					rs.IdEstado = "DEL";
					datosReservaDelete.add(rs);
				}
				
				if (res.IdConcepto.equalsIgnoreCase("INGRES") 
						&& res.IdReserva.compareTo(0L) > 0 
						&& res.Cantidad.compareTo(BigDecimal.ZERO) > 0){
					Reserva rs = new Reserva();
					rs = copyPojoEntradaReserva(res);
					
					rs.IngCantidad              = res.Cantidad;
					rs.CantidadInicial          = res.Cantidad;
					rs.ValorRescateInicial      = res.ValorRescate;
					rs.MontoValorizadoReserva   = res.MontoValorizadoReserva;
					rs.IngValorRescate          = res.ValorRescate;
					rs.IngMontoValorizado       = res.MontoValorizado;
					rs.IdEstado 				= "VIG";
					rs.EgrIdUsuario				= 0L;//in.p_IdUsuario;
					rs.EgrCantidad				= BigDecimal.ZERO;
					rs.EgrMontoValorizado		= BigDecimal.ZERO;
					rs.EgrFecha					= Fecha.toDate("0001-01-01");
					rs.INGMontoAPlazo = res.INGMontoAPlazoDetalle;
					rs.INGMontoAPlazoMonedaInstrumento = res.INGMontoAPlazoMonedaInstrumentoDetalle;
					datosReservaUpdate.add(rs);
				}
				
				if (res.IdConcepto.equalsIgnoreCase("RETRES") &&  res.IdTipoProducto.equalsIgnoreCase("CUST")){
					Reserva tblReserva = (Reserva)datosReservaIdHash.get(res.IdReserva);
					
					if (tblReserva != null){ //retiro de reserva especifica (Por IdReserva)
						BigDecimal cantidadRestante = tblReserva.IngCantidad.subtract(res.Cantidad);
						
						if (cantidadRestante.equals(BigDecimal.ZERO)){
							Reserva rs = new Reserva();
							rs = copyPojoEntradaReserva(res);
							
							rs.IdEstado = "LIQ";
							rs.EgrIdUsuario = in.p_IdUsuario;
							rs.EgrCantidad = res.Cantidad;
							rs.EgrMontoValorizado = BigDecimal.ZERO;
							rs.EgrFecha = Fecha.toDate(res.FechaOperacion);
							datosReservaUpdate.add(rs);
						}
						else {
							Reserva rs = new Reserva();
							Reserva rsNew = new Reserva();
							rs    = copyPojoEntradaReserva(res);
							rsNew = copyPojoEntradaReserva(res);
							
							rs.IdEstado = "MOD";
							rs.EgrIdUsuario = in.p_IdUsuario;
							rs.EgrCantidad = res.Cantidad;
							rs.EgrMontoValorizado = BigDecimal.ZERO;
							rs.EgrFecha = Fecha.toDate(res.FechaOperacion);
							datosReservaUpdate.add(rs);
							
							rsNew.IdReserva = 0L;//secuenciaReserva.get(IdSecuenciaReserva);
							rsNew.FolioCarteraOrigen = res.FolioCartera; 
							rsNew.FolioCarteraCustodia = res.FolioCarteraCustodia;
							rsNew.IdCarteraOrigen = res.IdCartera; 
							rsNew.IdCarteraCustodia = res.IdCarteraCustodia;
							rsNew.IdEstado = "VIG"; 
							rsNew.IdUsuarioCreacion = in.p_IdUsuario;
							//----
							rsNew.IdClasificacionTipoInstrumento = res.IdClasificacionTipoInstrumento;
							rsNew.IdMonedaEmisionInstrumento = res.IdMonedaEmisionInstrumento;
							rsNew.IdMonedaLiquidacionInstrumento = res.IdMonedaLiquidacionInstrumento;
							rsNew.FechaVencimiento = Fecha.toDate(res.FechaVencimiento);
							//----
							rsNew.CantidadInicial = res.Cantidad;
							rsNew.ValorRescateInicial = res.ValorRescate;
							rsNew.MontoValorizadoReserva = BigDecimal.ZERO; 
							rsNew.MontoLiquidacion = res.MontoLiquidacion;
							rsNew.FechaVencimientoReserva = Fecha.toDate("0001-01-01"); 
							rsNew.FechaVencimientoProducto = Fecha.toDate("0001-01-01"); 
							rsNew.FechaCreacion = Fecha.toDate(res.FechaOperacion);
							rsNew.IngIdUsuario = in.p_IdUsuario; 
							rsNew.IngCantidad = cantidadRestante;//res.Cantidad;
							rsNew.IngValorRescate = res.ValorRescate;
							rsNew.IngMontoValorizado = BigDecimal.ZERO; 
							rsNew.IngFecha = Fecha.toDate(res.FechaOperacion); 
							rsNew.EgrIdUsuario = 0L;
							rsNew.EgrCantidad = BigDecimal.ZERO;
							rsNew.EgrValorRescate = BigDecimal.ZERO;
							rsNew.EgrMontoValorizado = BigDecimal.ZERO;
							rsNew.EgrFecha = Fecha.toDate("0001-01-01");
							datosReservaInsert.add(rsNew);
						}
					}
					else { //retiro para una catera en reserva  (Por FolioCartera)
						BigDecimal cantidadRestante = res.Cantidad;
						for (Reserva datosRes : datosReservaFolio){
							
							if (cantidadRestante.equals(BigDecimal.ZERO)){
								break;
							}
							
							if (res.FolioCartera.equals(datosRes.FolioCarteraOrigen)){
								
								if (cantidadRestante.compareTo(datosRes.IngCantidad) >= 0){
									Reserva rs = new Reserva();
									rs = copyPojoReserva(datosRes);
									
									rs.IdReserva = datosRes.IdReserva;
									rs.IdEstado = "LIQ";
									rs.IdBolsa = datosRes.IdBolsa;
									rs.EgrIdUsuario = in.p_IdUsuario;
									rs.EgrCantidad = datosRes.IngCantidad;
									rs.EgrMontoValorizado = BigDecimal.ZERO;
									rs.EgrFecha = Fecha.toDate(res.FechaOperacion);
									
									cantidadRestante = cantidadRestante.subtract(datosRes.IngCantidad);
									//actualizo el origen por si hay otro retiro para la misma cartera
									datosRes.IngCantidad = datosRes.IngCantidad.subtract(datosRes.IngCantidad);
									
									datosReservaUpdate.add(rs);
								}
								
								if (cantidadRestante.compareTo(datosRes.IngCantidad) < 0){
									Reserva rs = new Reserva();
									Reserva rsNew = new Reserva();
									
									rs = copyPojoReserva(datosRes);
									rsNew = copyPojoReserva(datosRes);
									
									rs.IdReserva = datosRes.IdReserva;
									rs.IdEstado = "MOD";
									rs.IdRegistro = res.IdRegistro;
									rs.IdBolsa = datosRes.IdBolsa;
									rs.EgrIdUsuario = in.p_IdUsuario;
									rs.EgrCantidad = cantidadRestante;//res.Cantidad;
									rs.EgrMontoValorizado = BigDecimal.ZERO;
									rs.EgrFecha = Fecha.toDate(res.FechaOperacion);
									
									//actualizo el origen por si hay otro retiro para la misma cartera
									datosRes.IngCantidad = datosRes.IngCantidad.subtract(cantidadRestante);
									
									datosReservaUpdate.add(rs);
									
									rsNew.IdReserva = 0L;//secuenciaReserva.get(IdSecuenciaReserva);
									rsNew.IdRegistro = res.IdRegistro;
									rsNew.IdOperacion = datosRes.IdOperacion;		
									rsNew.FolioCarteraOrigen = datosRes.FolioCarteraOrigen; 
									rsNew.FolioCarteraCustodia = datosRes.FolioCarteraCustodia;
									rsNew.IdCarteraOrigen = datosRes.IdCarteraOrigen; 
									rsNew.IdCarteraCustodia = datosRes.IdCarteraCustodia;
									rsNew.FolioOperacion = datosRes.FolioOperacion;	
									rsNew.IdEstado = "VIG"; 
									rsNew.IdUsuarioCreacion = in.p_IdUsuario; 
									rsNew.IdTipoProducto = datosRes.IdTipoProducto; 
									rsNew.IdConcepto = datosRes.IdConcepto;
									//----
									rsNew.Tasa = datosRes.Tasa;
									rsNew.TasaMercado = datosRes.TasaMercado;
									rsNew.Precio = datosRes.Precio;
									rsNew.PrecioMercado = datosRes.PrecioMercado;
									rsNew.MontoValorizado = datosRes.MontoValorizado;
									rsNew.MontoLiquidacion = datosRes.MontoLiquidacion;
									rsNew.MontoLiquidacionOperacion = datosRes.MontoLiquidacionOperacion;
									rsNew.IdClasificacionTipoInstrumento = datosRes.IdClasificacionTipoInstrumento;
									rsNew.IdInstrumento = datosRes.IdInstrumento;
									rsNew.Nemotecnico = datosRes.Nemotecnico;
									rsNew.IdTipoInstrumento = datosRes.IdTipoInstrumento;
									rsNew.IdTipoReajuste = datosRes.IdTipoReajuste;
									rsNew.IdMonedaEmisionInstrumento = res.IdMonedaEmisionInstrumento;
									rsNew.IdMonedaLiquidacionInstrumento = res.IdMonedaLiquidacionInstrumento;
									rsNew.IdMonedaLiquidacionOperacion = res.IdMonedaLiquidacionOperacion;
									rsNew.IdEmisor = datosRes.IdEmisor;
									rsNew.CorteMinimo = datosRes.CorteMinimo;	
									rsNew.FechaVencimiento = datosRes.FechaVencimiento;	
									rsNew.EsFungible = datosRes.EsFungible;
									rsNew.IdBolsa = datosRes.IdBolsa;		
									rsNew.IdBoveda = datosRes.IdBoveda;
									rsNew.CodigoIFRS = datosRes.CodigoIFRS;
									//----
									rsNew.CantidadInicial = datosRes.CantidadInicial;
									rsNew.ValorRescateInicial = datosRes.ValorRescateInicial;
									rsNew.MontoValorizadoReserva = BigDecimal.ZERO; 
									rsNew.FechaVencimientoReserva = Fecha.toDate("0001-01-01"); 
									rsNew.FechaVencimientoProducto = Fecha.toDate("0001-01-01"); 
									rsNew.FechaCreacion = Fecha.toDate(res.FechaOperacion);
									rsNew.IngIdUsuario = in.p_IdUsuario; 
									rsNew.IngCantidad = datosRes.IngCantidad;//res.Cantidad;
									rsNew.IngValorRescate = datosRes.IngValorRescate;
									rsNew.IngMontoValorizado = BigDecimal.ZERO; 
									rsNew.IngFecha = Fecha.toDate(res.FechaOperacion); 
									rsNew.EgrIdUsuario = 0L;
									rsNew.EgrCantidad = BigDecimal.ZERO;
									rsNew.EgrValorRescate = BigDecimal.ZERO;
									rsNew.EgrMontoValorizado = BigDecimal.ZERO;
									rsNew.EgrFecha = Fecha.toDate("0001-01-01");
									datosReservaInsert.add(rsNew);
									cantidadRestante = cantidadRestante.subtract(cantidadRestante);
								}
								
							}
						}
					}
				}
			}
			
			List<Long> secuenciaReserva = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::Reserva", Long.valueOf(datosReservaInsert.size()));//cambiar abajo para repasar datosReservaInsert

			List<Long> secuenciaReservaCliIngreso = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento", Long.valueOf(datosReservaInsert.size()));
			List<Long> secuenciaReservaCorrIngreso = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento", Long.valueOf(datosReservaInsert.size()));
			List<Long> secuenciaReservaCliLiquidacion = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento", Long.valueOf(datosReservaInsert.size()));
			List<Long> secuenciaReservaCorrLiquidacion = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento", Long.valueOf(datosReservaInsert.size()));

			for (int i = 0; i < datosReservaInsert.size(); i++){
				Reserva rs = datosReservaInsert.get(i);
				rs.IdReserva = secuenciaReserva.get(i).longValue();
				
				rs.SecMovCliIngreso = secuenciaReservaCliIngreso.get(i).intValue();
				rs.SecMovCorIngreso = secuenciaReservaCorrIngreso.get(i).intValue();
				rs.SecMovCliLiquidacion = secuenciaReservaCliLiquidacion.get(i).intValue();
				rs.SecMovCorLiquidacion = secuenciaReservaCorrLiquidacion.get(i).intValue();
				
				mapMsg.put("IdReserva", rs.IdReserva.toString());
				Id++;
				ReservaObj_OUT re = new ReservaObj_OUT();
				re.IdOperacion = rs.IdOperacion;
				re.IdRegistro = rs.IdRegistro;
				re.IdReserva = rs.IdReserva;
				re.Estado = rs.IdEstado;//"VIG";
				out.P_OUT_RESERVA.add(re);
			}
			
			for (Reserva rs: datosReservaUpdate){
				mapMsg.put("IdReserva", rs.IdReserva.toString());
				ReservaObj_OUT re = new ReservaObj_OUT();
				re.IdOperacion = rs.IdOperacion;
				re.IdRegistro = rs.IdRegistro;
				re.IdReserva = rs.IdReserva;
				re.Estado = rs.IdEstado;//"MOD";
				out.P_OUT_RESERVA.add(re);
			}
			
			for (Reserva rs: datosReservaDelete){
				mapMsg.put("IdReserva", rs.IdReserva.toString());
				ReservaObj_OUT re = new ReservaObj_OUT();
				re.IdOperacion = rs.IdOperacion;
				re.IdRegistro = rs.IdRegistro;
				re.IdReserva = rs.IdReserva;
				re.Estado = rs.IdEstado;//"DEL";
				out.P_OUT_RESERVA.add(re);
			}
			

			_sql.batch(conn, SQL_INSERT_RESERVA, datosReservaInsert, Reserva.class);
			_sql.batch(conn, SQL_DELETE_RESERVA, datosReservaDelete, Reserva.class);
			_sql.batch(conn, SQL_UPDATE_RESERVA, datosReservaUpdate, Reserva.class);
			
		} catch (Exception e) {
			throw e;
		} finally {
			
			if (lstFoliosConsultaPaso.size() > 0)
			{
				FoliosConsultaPaso objFolioConsultaPaso = new FoliosConsultaPaso();
				objFolioConsultaPaso.IdTrabajo = idTrabajo;
				_sql.batch(conn, SQL_DELETE_FOLIOS_CONSULTA, Arrays.asList(objFolioConsultaPaso), FoliosConsultaPaso.class);
			}
		}
			
		

		
		logger.info("Termino del servicioCUS.IngresoReservaa, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		
		return out;
	};
	
	
	private Reserva copyPojoEntradaReserva(ReservaObj_IN entrada) {
		
		Reserva salida = new Reserva();
		
		salida.IdOperacion = entrada.IdOperacion;
		salida.IdRegistro = entrada.IdRegistro;
		salida.IdReserva = entrada.IdReserva;
		salida.IdConcepto = entrada.IdConcepto;
		salida.FolioOperacion = entrada.FolioOperacion; 
		salida.FolioCarteraCustodia = entrada.FolioCarteraCustodia;
		salida.IdCarteraCustodia = entrada.IdCarteraCustodia;
		salida.IdTipoProducto = entrada.IdTipoProducto;
		salida.Tasa = entrada.Tasa;
		salida.TasaMercado = entrada.TasaMercado;
		salida.Precio = entrada.Precio;
		salida.PrecioMercado = entrada.PrecioMercado;
		salida.MontoValorizado = entrada.MontoValorizado;
		salida.MontoLiquidacion = entrada.MontoLiquidacion;
		salida.MontoLiquidacionOperacion = entrada.MontoLiquidacionOperacion;
		salida.IdClasificacionTipoInstrumento = entrada.IdClasificacionTipoInstrumento;
		salida.IdTipoInstrumento = entrada.IdTipoInstrumento;
		salida.IdInstrumento = entrada.IdInstrumento;
		salida.Nemotecnico = entrada.Nemotecnico;
		salida.IdMoneda = entrada.IdMoneda;
		salida.IdTipoReajuste = entrada.IdTipoReajuste;
		salida.IdMonedaEmisionInstrumento = entrada.IdMonedaEmisionInstrumento;
		salida.IdMonedaLiquidacionInstrumento = entrada.IdMonedaLiquidacionInstrumento;
		salida.IdMonedaLiquidacionOperacion = entrada.IdMonedaLiquidacionOperacion;
		salida.IdEmisor = entrada.IdEmisor;
		salida.CorteMinimo = entrada.CorteMinimo;
		salida.FechaVencimiento = Fecha.toDate(entrada.FechaVencimiento == null || "".equals(entrada.FechaVencimiento)?"0001-01-01":entrada.FechaVencimiento);
		salida.EsFungible = entrada.EsFungible;
		salida.IdBolsa = entrada.IdBolsa;
		salida.IdBoveda = entrada.IdBoveda;	
		salida.CodigoIFRS = entrada.CodigoIFRS == null ?"": entrada.CodigoIFRS;
		salida.MontoValorizadoReserva = entrada.MontoValorizadoReserva;
		
		return salida;
	}
	
	private Reserva copyPojoReserva(Reserva entrada) {
		
		Reserva salida = new Reserva();	

		salida.IdReserva = entrada.IdReserva;
		salida.IdRegistro = entrada.IdRegistro;
		salida.IdOperacion = entrada.IdOperacion;
		salida.FolioCarteraOrigen = entrada.FolioCarteraOrigen;
		salida.FolioCarteraCustodia = entrada.FolioCarteraCustodia;
		salida.IdCarteraOrigen = entrada.IdCarteraOrigen;
		salida.IdCarteraCustodia = entrada.IdCarteraCustodia;
		salida.FolioOperacion = entrada.FolioOperacion;
		salida.IdEstado = entrada.IdEstado;
		salida.IdUsuarioCreacion = entrada.IdUsuarioCreacion;
		salida.IdTipoProducto = entrada.IdTipoProducto;
		salida.IdConcepto = entrada.IdConcepto;
		salida.Tasa = entrada.Tasa;
		salida.Precio = entrada.Precio;
		salida.MontoValorizado = entrada.MontoValorizado;
		salida.MontoLiquidacion = entrada.MontoLiquidacion;
		salida.MontoLiquidacionOperacion = entrada.MontoLiquidacionOperacion;
		salida.IdClasificacionTipoInstrumento = entrada.IdClasificacionTipoInstrumento;
		salida.IdInstrumento = entrada.IdInstrumento;
		salida.Nemotecnico = entrada.Nemotecnico;
		salida.IdTipoInstrumento = entrada.IdTipoInstrumento;
		salida.IdMoneda = entrada.IdMoneda;
		salida.IdTipoReajuste = entrada.IdTipoReajuste;
		salida.IdMonedaEmisionInstrumento = entrada.IdMonedaEmisionInstrumento;
		salida.IdMonedaLiquidacionInstrumento = entrada.IdMonedaLiquidacionInstrumento;
		salida.IdMonedaLiquidacionOperacion = entrada.IdMonedaLiquidacionOperacion;
		salida.IdEmisor = entrada.IdEmisor;
		salida.FechaVencimiento = entrada.FechaVencimiento;
		salida.EsFungible = entrada.EsFungible;
		salida.IdBolsa = entrada.IdBolsa;
		salida.IdBoveda = entrada.IdBoveda;
		salida.CodigoIFRS = entrada.CodigoIFRS;
		salida.CorteMinimo = entrada.CorteMinimo;
		salida.CantidadInicial = entrada.CantidadInicial;
		salida.ValorRescateInicial = entrada.ValorRescateInicial;
		salida.MontoValorizadoReserva = entrada.MontoValorizadoReserva;
		salida.FechaVencimientoReserva = entrada.FechaVencimientoReserva;
		salida.FechaVencimientoProducto = entrada.FechaVencimientoProducto;
		salida.FechaCreacion = entrada.FechaCreacion;
		salida.IngIdUsuario = entrada.IngIdUsuario;
		salida.IngCantidad = entrada.IngCantidad;
		salida.IngValorRescate = entrada.IngValorRescate;
		salida.IngMontoValorizado = entrada.IngMontoValorizado;
		salida.IngFecha = entrada.IngFecha;
		salida.EgrIdUsuario = entrada.EgrIdUsuario;
		salida.EgrCantidad = entrada.EgrCantidad;
		salida.EgrValorRescate = entrada.EgrValorRescate;
		salida.EgrMontoValorizado = entrada.EgrMontoValorizado;
		salida.EgrFecha = entrada.EgrFecha;
		salida.SecuenciaCompromisoMigracion = entrada.SecuenciaCompromisoMigracion;
		salida.IdCarga = entrada.IdCarga;
		salida.PrecioMercado = entrada.PrecioMercado;
		salida.TasaMercado = entrada.TasaMercado; 
 
		return salida;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		IngresoReserva_IN jin = gson.fromJson(in.toString(), IngresoReserva_IN.class);
		IngresoReserva_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"N\",\"p_simularOperacion\":\"N\",\"p_IdPais\":44,\"p_FechaOperacion\":\"2018-01-30\",\"p_TblCarteraReserva\":[{\"IdOperacion\":0,\"IdRegistro\":1,\"IdReserva\":0,\"IdConcepto\":\"INGRES\",\"FolioOperacion\":0,\"FolioCartera\":1435363,\"FolioCarteraCustodia\":0,\"IdTipoProducto\":\"CUST\",\"Cantidad\":200,\"Tasa\":0,\"TasaMercado\":0,\"Precio\":0,\"PrecioMercado\":0,\"ValorRescate\":0,\"MontoValorizado\":0,\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoInstrumento\":\"\",\"IdInstrumento\":0,\"IdTipoReajuste\":\"\",\"IdEmisor\":\"\",\"CorteMinimo\":500,\"FechaVencimiento\":\"\",\"EsFungible\":\"\",\"Glosa\":\"\",\"IdBolsa\":\"\",\"IdBoveda\":\"\",\"CodigoIFRS\":\"\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"N\",\"p_simularOperacion\":\"N\",\"p_IdPais\":44,\"p_FechaOperacion\":\"2018-01-30\",\"p_TblCarteraReserva\":[{\"IdOperacion\":0,\"IdRegistro\":1,\"IdReserva\":0,\"IdConcepto\":\"INGRES\",\"FolioOperacion\":0,\"FolioCartera\":1435363,\"FolioCarteraCustodia\":0,\"IdTipoProducto\":\"CUST\",\"Cantidad\":100,\"Tasa\":0,\"TasaMercado\":0,\"Precio\":0,\"PrecioMercado\":0,\"ValorRescate\":0,\"MontoValorizado\":0,\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoInstrumento\":\"\",\"IdInstrumento\":0,\"IdTipoReajuste\":\"\",\"IdEmisor\":\"\",\"CorteMinimo\":500,\"FechaVencimiento\":\"\",\"EsFungible\":\"\",\"Glosa\":\"\",\"IdBolsa\":\"\",\"IdBoveda\":\"\",\"CodigoIFRS\":\"\"}]}";
			String json = "{{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"MRF\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPais\":44,\"p_TblCarteraReserva\":[{\"IdOperacion\":4140,\"IdRegistro\":0,\"IdReserva\":5218,\"IdConcepto\":\"RETRES\",\"FolioOperacion\":0,\"FolioCartera\":20699188,\"FolioCarteraCustodia\":20699188,\"IdCartera\":1537654,\"IdCarteraCustodia\":1537654,\"IdTipoProducto\":\"MERF\",\"Cantidad\":0,\"Tasa\":0,\"TasaMercado\":0,\"Precio\":0,\"PrecioMercado\":0,\"ValorRescate\":0,\"MontoValorizado\":0,\"MontoLiquidacion\":10680716,\"MontoLiquidacionOperacion\":10680716,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BEM\",\"IdInstrumento\":262000,\"Nemotecnico\":\"BAGUA-H\",\"IdMoneda\":\"CLP\",\"IdTipoReajuste\":\"UF\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdMonedaLiquidacionInstrumento\":\"CLP\",\"IdMonedaLiquidacionOperacion\":\"CLP\",\"IdEmisor\":\"AGUAS\",\"CorteMinimo\":500.00000000,\"FechaVencimiento\":\"2029-04-01\",\"EsFungible\":\"N\",\"Glosa\":\"\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"CodigoIFRS\":\"T\",\"FechaOperacion\":\"2022-01-05\",\"MontoValorizadoReserva\":0}]}}";
			//  String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"N\",\"p_simularOperacion\":\"N\",\"p_IdPais\":44,\"p_FechaOperacion\":\"2018-01-30\",\"p_TblCarteraReserva\":[{\"IdOperacion\":0,\"IdRegistro\":1,\"IdReserva\":0,\"IdConcepto\":\"RETRES\",\"FolioOperacion\":0,\"FolioCartera\":1435363,\"FolioCarteraCustodia\":0,\"IdTipoProducto\":\"CUST\",\"Cantidad\":150,\"Tasa\":0,\"TasaMercado\":0,\"Precio\":0,\"PrecioMercado\":0,\"ValorRescate\":0,\"MontoValorizado\":0,\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoInstrumento\":\"\",\"IdInstrumento\":0,\"IdTipoReajuste\":\"\",\"IdEmisor\":\"\",\"CorteMinimo\":500,\"FechaVencimiento\":\"\",\"EsFungible\":\"\",\"Glosa\":\"\",\"IdBolsa\":\"\",\"IdBoveda\":\"\",\"CodigoIFRS\":\"\"}]}";
			IngresoReserva_IN objIn = gson.fromJson(json,IngresoReserva_IN.class);
			//mientras pruebo hare el rollback
			IngresoReserva demo = new IngresoReserva();
			
			IngresoReserva_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
