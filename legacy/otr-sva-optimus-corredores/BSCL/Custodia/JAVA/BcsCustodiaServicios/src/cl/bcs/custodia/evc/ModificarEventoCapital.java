package cl.bcs.custodia.evc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;

public class ModificarEventoCapital implements JavaCall{
	private final Log log = new Log("CUS.ModificarEventoCapital");
	private final String SQL_SELECT_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapital_SelectEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapital_UpdateEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE =  loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapital_SelectEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapital_UpdateEventoCapitalCliente.sql");
	private final String SQL_UPDATE_CARTERA_FECHA_PAGO_CC = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapital_UpdateCarteraFechaPagoCC.sql");

	public static class EventoCapital_IN {
		public Long p_IdEmpresa;
	    public Long	p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<Evento> p_EventoCapital;
	    	    
	    public EventoCapital_IN() {
			this.p_EventoCapital = new ArrayList<Evento>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCapital_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public EventoCapital_OUT() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Evento {
		public Long 	p_IdEventoCapital;
		public Long		p_IdEmpresa;
		public String 	p_IdTipoEventoCapital;
		public Long 	p_Secuencia;
		public String 	p_Descripcion;
		public String 	p_EsInscrito;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;  
		public Long 	p_IdInstrumento;
		public String  	p_Nemo;
		public BigDecimal	p_Factor;
		public BigDecimal	p_Impuesto;
		public String 		p_IdMoneda;
		public String		p_IdMonedaLiquidacion;
		public BigDecimal	p_Paridad;
		public Long 		p_IdInstrumentoNuevo;
		public String 		p_NemoNuevo;
		public BigDecimal	p_FactorNuevo;
		public String 		p_FechaActualizacion;     
		public String 		p_FechaLimite;     
		public String 		p_FechaPago;     
		public Long 		p_NumeroDividendo;
		public String 		p_IdTipoDividendo;
		public BigDecimal	p_PrecioAnterior;
		public String 		p_FechaHoraIngresoRegistro;     
		public String 		p_FechaHoraUltimaActualizacion;     
		public String 		p_TipoIngreso;
		public String 		p_IdEstado;
		public String 		p_IdEmisor;
		public String 		p_IdEmisorNuevo;
		public BigDecimal	p_Monto;
		public BigDecimal	p_Cuotas;
		public String			p_IdBoveda;
		public BigDecimal	p_ImpuestoDivExtranjero;
		public BigDecimal	p_GastoTransferencia;
		//--------------------
		public BigDecimal	p_Valor;
		public Long			p_IdUsuario;
		public String		p_Origen;
		public String		p_EsISFUT;
		public BigDecimal	p_TasaEfectiva;
		
		public String		p_SecuenciaRelacionada = "";
		public String		p_SuscripcionComplementaria = "N";
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoExiste {
		public Long 		 IdEventoCapital;
		public BigDecimal	 Factor;
		public String		 IdEstado;
		public String        IdTipoEventoCapital;
		public java.sql.Date FechaPago;
		public java.sql.Date FechaLimite;
		public Long 		 NumeroDividendo;
		public String 		 Descripcion;
		public String 		 EsInscrito;
		public BigDecimal 	 Impuesto;
		public BigDecimal 	 FactorNuevo;
		public String 		 IdMoneda;
	    public String 		 IdMonedaLiquidacion;
	    public BigDecimal 	 Paridad;
	    public String 		 IdBoveda;
	   	public String 		 ISFUT;
	    public BigDecimal 	 TasaEfectiva;
	    public BigDecimal	 ImpuestoDivExtranjero;
	    public BigDecimal	 GastoTransferencia;
	    public Long 		 Secuencia;
			    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    public boolean equals(EventoExiste aux) {
	    	if(this.TasaEfectiva==null) this.TasaEfectiva = BigDecimal.ZERO;
			if ( ( "".equalsIgnoreCase(this.ISFUT) || this.ISFUT == null ) && ( "N".equalsIgnoreCase(aux.ISFUT) || "".equalsIgnoreCase(aux.ISFUT) ) ) {
				this.ISFUT = "N";
				aux.ISFUT = "N";
			}
			if( (this.IdBoveda == null || "".equalsIgnoreCase(this.IdBoveda)) && "".equalsIgnoreCase(aux.IdBoveda) ) {
				this.IdBoveda = "";
			}
	    	
	        if (this == aux) {
	            return true; // Son la misma instancia
	        }
	        if (aux == null || getClass() != aux.getClass()) {
	            return false; // Comparación de clases o con null
	        }
	        if (!Objects.equals(this.IdEventoCapital, aux.IdEventoCapital)) {
	            return false; // Comparación de IdEventoCapital
	        }
	        // Comparación de BigDecimal usando compareTo en lugar de equals
	        if (!compareBigDecimal(this.Factor, aux.Factor)) {
	            return false;
	        }
	        if (!compareBigDecimal(this.FactorNuevo, aux.FactorNuevo)) {
	            return false;
	        }
	        if (!compareBigDecimal(this.Impuesto, aux.Impuesto)) {
	            return false;
	        }
	        if (!compareBigDecimal(this.Paridad, aux.Paridad)) {
	            return false;
	        }
	        if (!compareBigDecimal(this.TasaEfectiva, aux.TasaEfectiva)) {
	            return false;
	        }
	        if (!compareBigDecimal(this.ImpuestoDivExtranjero, aux.ImpuestoDivExtranjero)) {
	        	return false;
	        }
	        if (!compareBigDecimal(this.GastoTransferencia, aux.GastoTransferencia)) {
	        	return false;
	        }

	        // Comparación de fechas usando equals
	        if (!Objects.equals(this.FechaPago, aux.FechaPago)) {
	            return false;
	        }
	        if (!Objects.equals(this.FechaLimite, aux.FechaLimite)) {
	            return false;
	        }

	        // Comparación de cadenas y otros campos con equals
	        return Objects.equals(this.IdEstado, aux.IdEstado)
	                && Objects.equals(this.IdTipoEventoCapital, aux.IdTipoEventoCapital)
	                && Objects.equals(this.NumeroDividendo, aux.NumeroDividendo)
	                && Objects.equals(this.Descripcion, aux.Descripcion)
	                && Objects.equals(this.EsInscrito, aux.EsInscrito)
	                && Objects.equals(this.IdMoneda, aux.IdMoneda)
	                && Objects.equals(this.IdMonedaLiquidacion, aux.IdMonedaLiquidacion)
	                && Objects.equals(this.IdBoveda, aux.IdBoveda)
	                && Objects.equals(this.ISFUT, aux.ISFUT);
	    }
	    
	    private boolean compareBigDecimal(BigDecimal bd1, BigDecimal bd2) {
	        if (bd1 == null || bd2 == null) {
	            return bd1 == null && bd2 == null; // Ambos son null o ninguno es null
	        }
	        return bd1.setScale(8, RoundingMode.HALF_UP).compareTo(bd2.setScale(8, RoundingMode.HALF_UP)) == 0;
	    }
	}
	
	public static class CarteraExistente {
		public Long       IdInstrumento;
		public String     FechaPago;
		public String     FechaPagoOld;
			    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Instrumento {
		public Long 		IdInstrumento;
		public String		Nemotecnico;
		public String		IdTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public String		IdEstado;
		public String		CallConfirmado;
		public String		CuponeraVariable;
		public Date			FechaPagoCupon;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente {
		public Long			IdEventoCapitalCliente;
		public Long			IdEventoCapital;
		public String		IdTipoEventoCapital;
		public Long			Secuencia;
		public Long			IdSocioNegocio;
		public Long			IdPersona;
		public String		TipoSocioNegocio;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		IdTipoDocumentoIdentidad;
		public String		DocumentoIdentidad;
		public String		NombreCliente;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public Long			IdInstrumento;
		public String			Nemo;
		public Long				IdInstrumentoNuevo;
		public String			NemoNuevo;
		public java.sql.Date	FechaCierre;
		public java.sql.Date	FechaPago;
		public BigDecimal		CantidadDerecho;
		public String			TipoCartera;
		public BigDecimal	CantidadNueva;
		public BigDecimal	MontoCapital;
		public BigDecimal	MontoInteres;
		public BigDecimal	Total;
		public String		IdMoneda;
		public Long			SecuenciaCupon;
		public String		CodigoUsuarioAgente;
		public String		NombreAgente;
		public String		CodigoSucursal;
		public Timestamp	FechaHoraIngresoRegistro;
		public Timestamp		FechaHoraUltimaActualizacion;
		public Long			IdUsuario;
		public String		IdEstado;
		public String		IdBoveda;
		public String		IdBolsa;
		public String		IdConceptoGarantia;
		public String		IdTipoOperacionGarantia;
		public BigDecimal	MontoReajuste;
		public BigDecimal	Precio;
		public BigDecimal	PrecioNuevo;
		public Long			NumeroDividendo;
		public String		Origen;
		public BigDecimal	Paridad;
		public Long			IdCartera;
		public Long			FolioCartera;
		public Long			IdEmpleadoEjecutivoPortafolio;
		public String		EstadoNotificacion;
		public Long			IdEventoCapitalClienteFormaDePago;
		public String		TipoCuentaBoveda;
		public String		NumeroCuentaBoveda;
		public String		PagaClienteCorredor;
		public String		IdTipoReajuste;
		public BigDecimal	INGMontoInteresCorteCupon;
		public BigDecimal	Amortizacion;
		public Long			IdInstrumentoAnterior;
		public String		AfectoaClearing;
		public String		ProcesadoClearing;
		public Long			IdCorredorContraparte;
		public Long			IdComprobanteTesoreria;
		public String		TipoOperacion;
		public String		CodigoCorredorContraparte;
		public BigDecimal	MontoBeneficioArchivo;
		public BigDecimal	DiferenciaMontoBeneficio;
		public BigDecimal	CantidadDerechoArchivo;
		public BigDecimal	AccionesDisponiblesArchivo;
		public BigDecimal	DiferenciaCantidadDerecho;
		public BigDecimal	DiferenciaAccionesCantidadNueva;
		public BigDecimal			MontoInteresArchivo;
		public BigDecimal			MontoCapitalArchivo;
		public BigDecimal			DiferenciaMontoInteres;
		public BigDecimal			DiferenciaMontoCapital;
		public String		TipoCarteraNormativa;
		public String		IdTipoCuentaCustodio;
		public Long			IdMovimientoComprobanteFac;
		public Long			IdCuentaCustodioPortafolio;
		public BigDecimal	MontoRetencion;
		public BigDecimal	DiferenciaMontoRetencion;
		public BigDecimal	MontoRetencionArchivo;
		public String		IdConcepto;
		public Long			IdContraparteGaratia;
		public String		p_EsISFUT;
		public BigDecimal	p_TasaEfectiva;
		public String		p_SecuenciaRelacionada = "";
		public String		p_SuscripcionComplementaria = "N";
		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EventoCapital_OUT logica (Connection conn, EventoCapital_IN in) throws Exception {
 		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		log.write("INICIO::CUS.ModificarEventoCapital");
		EventoCapital_OUT out = new EventoCapital_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Long> idEventoCapital = new ArrayList<>();
		List<Long> Secuencia = new ArrayList<>();
		List<EventoExiste> eventoExistente = new ArrayList<>();
		Map<String, Object> eventoExistenteMap = new HashMap<>();
		List<EventoCapitalCliente> eventosCapitalClienteExistente = new ArrayList<>();
		
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(conn,in.p_IdEmpresa);
		for (Evento ev: in.p_EventoCapital){
			idEventoCapital.add(ev.p_IdEventoCapital);
			Secuencia.add(ev.p_Secuencia);
			
		}
		
		if (idEventoCapital.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			params.put("p_IdEventoCapital", idEventoCapital);
			params.put("p_Secuencia", Secuencia);
			eventoExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoExiste.class);
			params.clear();
			params.put("p_IdEventoCapital", idEventoCapital);
			params.put("p_Secuencia", Secuencia);
			eventosCapitalClienteExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoCapitalCliente.class);
			eventoExistenteMap = Utils.List2Hash(eventoExistente, "IdEventoCapital");
		}
		
		final List<EventoExiste> eventoExistenteFinal = eventoExistente;

		Set<Long> idsOriginales = in.p_EventoCapital.stream()
		    .map(e -> e.p_IdEventoCapital)
		    .filter(Objects::nonNull)
		    .collect(Collectors.toSet());

		List<Evento> nuevaListaEventos = in.p_EventoCapital.stream()
		    .flatMap(eventoOriginal -> {
		        List<Evento> clones = new ArrayList<>();
		        clones.add(eventoOriginal);

		        List<Evento> clonadosConId = eventoExistenteFinal.stream()
		            .filter(existe -> Objects.equals(existe.Secuencia, eventoOriginal.p_Secuencia))
		            .filter(existe -> !idsOriginales.contains(existe.IdEventoCapital)) // <-- evita duplicados
		            .map(existe -> {
		                Evento clon = copiarEvento(eventoOriginal);
		                clon.p_IdEventoCapital = existe.IdEventoCapital;
		                return clon;
		            })
		            .collect(Collectors.toList());

		        clones.addAll(clonadosConId);
		        return clones.stream();
		    })
		    .collect(Collectors.toList());




		//::Validaciones
		{		
			try {
				List<EventoExiste>listaEventos = new ArrayList<>();
				List<Integer> indicesAEliminar = new ArrayList<>(); 
				int index = 0;
				for(Evento evCap:in.p_EventoCapital) {
					EventoExiste eventoNuevo = new EventoExiste();
					eventoNuevo.IdEventoCapital = evCap.p_IdEventoCapital;
					eventoNuevo.Descripcion = evCap.p_Descripcion;
					eventoNuevo.EsInscrito = evCap.p_EsInscrito;
					eventoNuevo.Factor = evCap.p_Factor;
					eventoNuevo.FactorNuevo = evCap.p_FactorNuevo;
					eventoNuevo.FechaLimite = Fecha.toDate(evCap.p_FechaLimite);
					eventoNuevo.FechaPago = Fecha.toDate(evCap.p_FechaPago);
					eventoNuevo.IdBoveda = evCap.p_IdBoveda;
					eventoNuevo.IdEstado = evCap.p_IdEstado;
					eventoNuevo.IdMoneda = evCap.p_IdMoneda;
					eventoNuevo.IdMonedaLiquidacion = evCap.p_IdMonedaLiquidacion;
					eventoNuevo.Impuesto = evCap.p_Impuesto;
					eventoNuevo.ISFUT = evCap.p_EsISFUT;
					eventoNuevo.NumeroDividendo = evCap.p_NumeroDividendo;
					eventoNuevo.Paridad = evCap.p_Paridad;
					eventoNuevo.TasaEfectiva = evCap.p_TasaEfectiva;
					eventoNuevo.IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
					eventoNuevo.ImpuestoDivExtranjero = evCap.p_ImpuestoDivExtranjero;
					eventoNuevo.GastoTransferencia = evCap.p_GastoTransferencia;
					listaEventos.add(eventoNuevo);
					EventoExiste EventoExiste = (EventoExiste) eventoExistenteMap.get(evCap.p_IdEventoCapital);
					if(listaEventos.get(index).equals(EventoExiste)) {
			            int indice = in.p_EventoCapital.indexOf(evCap); // Obtener el índice
			            indicesAEliminar.add(indice); // Agregar el índice a la lista de índices a eliminar
			            mapMsg.clear();
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_IDENTICOS",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						//return out;
					}
					if (EventoExiste.IdTipoEventoCapital.equalsIgnoreCase("DV") && (evCap.p_IdMoneda.equalsIgnoreCase("PEN") || evCap.p_IdMoneda.equalsIgnoreCase("COP")) && !evCap.p_IdMonedaLiquidacion.equalsIgnoreCase("USD")){
						int indice = in.p_EventoCapital.indexOf(evCap);
						indicesAEliminar.add(indice);
						mapMsg.clear();
						mapMsg.put("Moneda", evCap.p_IdMoneda.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MONEDA_LIQ_EVENTO_DV", mapMsg, conn, in.p_IdEmpresa));
						Id++;
						out.P_OUT_ERRORESCONTADOR++;
					}
					if (evCap.p_ImpuestoDivExtranjero != null && (evCap.p_ImpuestoDivExtranjero.compareTo(BigDecimal.ZERO) < 0 || evCap.p_ImpuestoDivExtranjero.compareTo(new BigDecimal(100)) > 0)){
						int indice = in.p_EventoCapital.indexOf(evCap);
						indicesAEliminar.add(indice);
						mapMsg.clear();
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERR_VALOR_IMP_DV_EXTRANJERO", mapMsg, conn, in.p_IdEmpresa));
						Id++;
						out.P_OUT_ERRORESCONTADOR++;
					}
					if (evCap.p_GastoTransferencia.compareTo(BigDecimal.ZERO) < 0){
						int indice = in.p_EventoCapital.indexOf(evCap);
						indicesAEliminar.add(indice);
						mapMsg.clear();
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERR_VALOR_GASTO_TRANSFERENCIA", mapMsg, conn, in.p_IdEmpresa));
						Id++;
						out.P_OUT_ERRORESCONTADOR++;
					}
					index++;
				}
			    for (int i = indicesAEliminar.size() - 1; i >= 0; i--) {
			        int indice = indicesAEliminar.get(i);
			        in.p_EventoCapital.remove(indice);
			    }
			}catch(Exception e) {
				e.printStackTrace();
			}		
		}
		{
			List<Evento> datosUpdate = new ArrayList<>();
			List<CarteraExistente> carteraUpdate = new ArrayList<>();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			Long IdCabeceraLog =  in.p_IdUsuario;
		    int i = 0;
			for (Evento evCap: nuevaListaEventos){
				Evento inEv = new Evento();
				inEv.p_IdEventoCapital = evCap.p_IdEventoCapital;
//				inEv.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
				inEv.p_Descripcion		= evCap.p_Descripcion;
				inEv.p_Cuotas 				= evCap.p_Cuotas != null ? evCap.p_Cuotas : BigDecimal.ZERO;
				inEv.p_EsInscrito		= evCap.p_EsInscrito;
				inEv.p_Factor				= evCap.p_Factor;
			    inEv.p_Impuesto				= evCap.p_Impuesto;
			    inEv.p_IdMoneda				= evCap.p_IdMoneda;
			    inEv.p_IdMonedaLiquidacion	= evCap.p_IdMonedaLiquidacion;
			    inEv.p_Paridad				= evCap.p_Paridad;
			    inEv.p_FactorNuevo			= evCap.p_FactorNuevo;
			    inEv.p_FechaLimite			= evCap.p_FechaLimite;
			    inEv.p_FechaPago			= evCap.p_FechaPago;
			    inEv.p_NumeroDividendo		= evCap.p_NumeroDividendo;
			    inEv.p_FechaHoraUltimaActualizacion	= fechaHoraSistema;
			    inEv.p_IdBoveda = evCap.p_IdBoveda;
			    inEv.p_IdEstado				= ((EventoExiste) eventoExistenteMap.get(evCap.p_IdEventoCapital)).Factor.compareTo(evCap.p_Factor) != 0 &&  !((EventoExiste) eventoExistenteMap.get(evCap.p_IdEventoCapital)).IdEstado.equalsIgnoreCase("VIG")?"MOD":((EventoExiste) eventoExistenteMap.get(evCap.p_IdEventoCapital)).IdEstado;//evCap.p_IdEstado;
			    inEv.p_EsISFUT				= evCap.p_EsISFUT != null ? evCap.p_EsISFUT:"N"; 
			    inEv.p_TasaEfectiva			= evCap.p_TasaEfectiva != null ? evCap.p_TasaEfectiva:BigDecimal.ZERO; 
			    inEv.p_SecuenciaRelacionada = evCap.p_SecuenciaRelacionada;
			    inEv.p_SuscripcionComplementaria = evCap.p_SuscripcionComplementaria;
			    inEv.p_ImpuestoDivExtranjero = evCap.p_ImpuestoDivExtranjero != null? evCap.p_ImpuestoDivExtranjero: BigDecimal.ZERO;
			    inEv.p_GastoTransferencia = evCap.p_GastoTransferencia != null? evCap.p_GastoTransferencia: BigDecimal.ZERO;
			    datosUpdate.add(inEv);
			    
			    if ("CC".equalsIgnoreCase(((EventoExiste) eventoExistenteMap.get(evCap.p_IdEventoCapital)).IdTipoEventoCapital)) {
			    	CarteraExistente carteraExistente = new CarteraExistente();
			    	carteraExistente.IdInstrumento = evCap.p_IdInstrumento;
			    	carteraExistente.FechaPago =  inEv.p_FechaPago;
			    	carteraExistente.FechaPagoOld = Fecha.toStringDate(((EventoExiste) eventoExistenteMap.get(evCap.p_IdEventoCapital)).FechaPago);
			    	carteraUpdate.add(carteraExistente);
			    }

			    mapMsg.clear();
			    mapMsg.put("p_NumeroDividendo", evCap.p_NumeroDividendo.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_MODIFICADO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				
				//parametros log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				log.IdCabeceraLog	= evCap.p_IdEventoCapital; 
				log.IdTipoLog		= "MODIFICACION"; 
				log.IdModulo		= in.p_IdModulo; 
				log.IdUsuario		= in.p_IdUsuario; 
				log.IdEmpresa		= in.p_IdEmpresa; 
				log.FechaSistemaLog	= fechaSistema; 
				log.IdUsuarioAtendido	= in.p_IdUsuario; 
				log.IdCliente			= 0L; 
				log.EstadoCliente		= ""; 
				log.NombreCliente		= ""; 
				log.IdTipoDocumentoIdentidad	= ""; 
				log.IdDocumentoIdentidad	= ""; 
				log.IdPortafolio			= 0L; 
				log.EstadoPortafolio		= ""; 
				log.IdTipoRegistro			= "MODIFICAREVENTOCAPITAL"; 
				log.IdRegistro				= evCap.p_Secuencia.toString(); 
				log.EstadoRegistro			= ""; 
				log.IdTipoOperacion			= ""; 
				log.Folio					= 0L; 
				log.Cantidad				= BigDecimal.ZERO; 
				log.PrecioTasa				= BigDecimal.ZERO; 
				log.Monto					= BigDecimal.ZERO; 
				log.IdMonedaOrigen			= ""; 
				log.IdMonedaLiquidacion		= ""; 
				log.TipoCambio				= ""; 
				log.Nemo					= evCap.p_Nemo; 
				log.IdProceso				= 0L; 
				log.EstadoProceso			= ""; 
				log.NombreProceso			= "cl.bcs.custodia.evc.ModificarEventoCapital";

				//Agrega el detalle de la modificacion si tiene campos diferentes al modificar el evento capital, Se agrego para el cargar desde bursatil para la pauta y manual)
				try {
				    Evento eventoMod = datosUpdate.get(i);
				    Field[] camposMod = eventoMod.getClass().getDeclaredFields();
				    Object eventoEx=eventoExistenteMap.get(eventoMod.p_IdEventoCapital);
				    for(Field campo:camposMod) {
				    	campo.setAccessible(true);
				    	Object nombreCampo = campo.getName();
				    	Object valor = campo.get(eventoMod);
						if (!"p_IdEmpresa".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdUsuario".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_Origen".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_Secuencia".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdClasificacionTipoInstrumento".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdTipoInstrumento".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdInstrumento".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_Nemo".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdInstrumentoNuevo".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_NemoNuevo".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_FechaActualizacion".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdTipoDividendo".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_PrecioAnterior".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_FechaHoraIngresoRegistro".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_FechaHoraUltimaActualizacion".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_TipoIngreso".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdEmisor".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_IdEmisorNuevo".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_Monto".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_Cuotas".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_Valor".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_EsISFUT".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_SecuenciaRelacionada".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_SuscripcionComplementaria".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_ImpuestoDivExtranjero".equalsIgnoreCase(nombreCampo.toString())
								&& !"p_GastoTransferencia".equalsIgnoreCase(nombreCampo.toString())) {
							Field campoAntiguo = eventoEx.getClass()
									.getDeclaredField(nombreCampo.toString().substring(2));
							campoAntiguo.setAccessible(true);
							Object valorAntiguo = campoAntiguo.get(eventoEx);
							if (valor != null && !nombreCampo.equals("p_IdEventoCapital")
									&& !valorAntiguo.equals(valor)) {
								InAgregarLog.DetalleLog detalleLog = new InAgregarLog.DetalleLog();
								detalleLog.ValorAntiguo = valorAntiguo.toString();
								detalleLog.CampoRegistro = nombreCampo.toString().substring(2);
								detalleLog.ValorNuevo = valor.toString();
								detalleLog.IdCabeceraLog = evCap.p_IdEventoCapital;
								detalleLog.IdTrabajo = 0L;
								detalleLog.SubRegistro = "EVENTOCAPITAL";
								entradaAgregarLog.p_LogDetalle.add(detalleLog);
							}
						}
				    }				    
				    i++;
				} catch (Exception e) {
				    e.printStackTrace();
				}
				logList.add(log);
				
			}
			HashMap<Long, String> FechaPagoHash = new HashMap<>();
			HashMap<Long, String> FechaCierreHash = new HashMap<>();
			HashMap<Long, BigDecimal> PrecioNuevoHash = new HashMap<>();
			
			for (Evento evento : in.p_EventoCapital) {
				FechaPagoHash.put(evento.p_Secuencia, evento.p_FechaPago);
				FechaCierreHash.put(evento.p_Secuencia, evento.p_FechaLimite);
				PrecioNuevoHash.put(evento.p_Secuencia, evento.p_FactorNuevo);
	        }
			
			List<EventoCapitalCliente> evCapCliUpadte = new ArrayList<>();
			for(EventoCapitalCliente evCapCli : eventosCapitalClienteExistente) {
				EventoCapitalCliente inEvCapCli = new EventoCapitalCliente();
				inEvCapCli.IdEventoCapital = evCapCli.IdEventoCapital;
				inEvCapCli.IdEventoCapitalCliente = evCapCli.IdEventoCapitalCliente;
				inEvCapCli.Secuencia = evCapCli.Secuencia;
				inEvCapCli.FechaPago = Date.valueOf(FechaPagoHash.get(inEvCapCli.Secuencia));
				inEvCapCli.FechaCierre = Date.valueOf(FechaCierreHash.get(inEvCapCli.Secuencia));
				inEvCapCli.FechaHoraIngresoRegistro = evCapCli.FechaHoraIngresoRegistro;
				inEvCapCli.FechaHoraUltimaActualizacion = Timestamp.valueOf(fechaHoraSistema);
				if (evCapCli.IdEstado.equals("ENV") || evCapCli.IdEstado.equals("ANU") || !evCapCli.IdTipoEventoCapital.equals("SU")) {
					inEvCapCli.Precio = evCapCli.Precio;
				}else {
					inEvCapCli.Precio = PrecioNuevoHash.get(inEvCapCli.Secuencia);
				}
				if ((!evCapCli.IdEstado.equals("ENV") && !evCapCli.IdEstado.equals("ANU")) || !evCapCli.IdTipoEventoCapital.equals("SU")) {
					evCapCliUpadte.add(inEvCapCli);
					
					mapMsg.clear();
				    mapMsg.put("idEventoCapitalCliente", inEvCapCli.IdEventoCapitalCliente.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_CLIENTE_MODIFICADO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}

			
			_sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, evCapCliUpadte, EventoCapitalCliente.class);
			
			
			entradaAgregarLog.p_Log = logList;
			_sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL, datosUpdate, Evento.class);
			
			if (carteraUpdate.size() > 0) {
				_sql.batch(conn, SQL_UPDATE_CARTERA_FECHA_PAGO_CC, carteraUpdate, CarteraExistente.class);
			}
				
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		log.write("FIN::CUS.ModificarEventoCapital");
		return out;
	};
	
	private static Evento copiarEvento(Evento original) {
	    Evento copia = new Evento();
	    copia.p_IdEventoCapital = original.p_IdEventoCapital;
	    copia.p_IdEmpresa = original.p_IdEmpresa;
	    copia.p_IdTipoEventoCapital = original.p_IdTipoEventoCapital;
	    copia.p_Secuencia = original.p_Secuencia;
	    copia.p_Descripcion = original.p_Descripcion;
	    copia.p_EsInscrito = original.p_EsInscrito;
	    copia.p_IdClasificacionTipoInstrumento = original.p_IdClasificacionTipoInstrumento;
	    copia.p_IdTipoInstrumento = original.p_IdTipoInstrumento;
	    copia.p_IdInstrumento = original.p_IdInstrumento;
	    copia.p_Nemo = original.p_Nemo;
	    copia.p_Factor = original.p_Factor;
	    copia.p_Impuesto = original.p_Impuesto;
	    copia.p_IdMoneda = original.p_IdMoneda;
	    copia.p_IdMonedaLiquidacion = original.p_IdMonedaLiquidacion;
	    copia.p_Paridad = original.p_Paridad;
	    copia.p_IdInstrumentoNuevo = original.p_IdInstrumentoNuevo;
	    copia.p_NemoNuevo = original.p_NemoNuevo;
	    copia.p_FactorNuevo = original.p_FactorNuevo;
	    copia.p_FechaActualizacion = original.p_FechaActualizacion;
	    copia.p_FechaLimite = original.p_FechaLimite;
	    copia.p_FechaPago = original.p_FechaPago;
	    copia.p_NumeroDividendo = original.p_NumeroDividendo;
	    copia.p_IdTipoDividendo = original.p_IdTipoDividendo;
	    copia.p_PrecioAnterior = original.p_PrecioAnterior;
	    copia.p_FechaHoraIngresoRegistro = original.p_FechaHoraIngresoRegistro;
	    copia.p_FechaHoraUltimaActualizacion = original.p_FechaHoraUltimaActualizacion;
	    copia.p_TipoIngreso = original.p_TipoIngreso;
	    copia.p_IdEstado = original.p_IdEstado;
	    copia.p_IdEmisor = original.p_IdEmisor;
	    copia.p_IdEmisorNuevo = original.p_IdEmisorNuevo;
	    copia.p_Monto = original.p_Monto;
	    copia.p_Cuotas = original.p_Cuotas;
	    copia.p_IdBoveda = original.p_IdBoveda;
	    copia.p_ImpuestoDivExtranjero = original.p_ImpuestoDivExtranjero;
	    copia.p_GastoTransferencia = original.p_GastoTransferencia;
	    copia.p_Valor = original.p_Valor;
	    copia.p_IdUsuario = original.p_IdUsuario;
	    copia.p_Origen = original.p_Origen;
	    copia.p_EsISFUT = original.p_EsISFUT;
	    copia.p_TasaEfectiva = original.p_TasaEfectiva;
	    copia.p_SecuenciaRelacionada = original.p_SecuenciaRelacionada;
	    copia.p_SuscripcionComplementaria = original.p_SuscripcionComplementaria;
	    return copia;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EventoCapital_IN jin = gson.fromJson(in.toString(), EventoCapital_IN.class);
		EventoCapital_OUT out = logica(conn, jin);
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
			conn = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
			//datos entrada
			//String json = "{\"p_IdEmpresa\": 1,\"p_IdUsuario\": 9,\"p_IdModulo\": \"CUS\",\"p_EventoCapital\":[{\"p_IdTipoEventoCapital\":\"DA\",\"p_Secuencia\":22,\"p_Descripcion\":\"PJJKJK\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":308,\"p_IdEmisor\":\"CCU\",\"p_Nemo\":\"CCU\",\"p_Factor\":1.466,\"p_Impuesto\":0,\"p_FactorNuevo\":1,\"p_IdMoneda\":\"CLP\",\"p_IdMonedaLiquidacion\":\"CLP\",\"p_Paridad\":0,\"p_FechaActualizacion\":\"2018-07-31\",\"p_FechaLimite\":\"2018-07-31\",\"p_FechaPago\":\"2018-09-28\",\"p_NumeroDividendo\":115621,\"p_EsInscrito\":\"N\",\"p_IdInstrumentoNuevo\":0,\"p_IdEmisorNuevo\":\"\",\"p_NemoNuevo\":0,\"p_IdEventoCapital\":364837}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdTipoEventoCapital\":\"\",\"p_Secuencia\":100001239,\"p_Descripcion\":\"PRUEBA ED 3\",\"p_EsInscrito\":\"S\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":1569,\"p_Nemo\":\"NORTEGRAN\",\"p_Factor\":0.567,\"p_Impuesto\":0,\"p_IdMoneda\":\"CLP\",\"p_IdMonedaLiquidacion\":\"CLP\",\"p_Paridad\":1,\"p_IdInstrumentoNuevo\":0,\"p_NemoNuevo\":0,\"p_FactorNuevo\":34,\"p_FechaActualizacion\":\"2025-10-03\",\"p_FechaLimite\":\"2025-09-15\",\"p_FechaPago\":\"2025-10-03\",\"p_NumeroDividendo\":0,\"p_IdTipoDividendo\":\"\",\"p_PrecioAnterior\":0,\"p_FechaHoraIngresoRegistro\":\"1900-01-01\",\"p_FechaHoraUltimaActualizacion\":\"1900-01-01\",\"p_TipoIngreso\":0,\"p_IdBoveda\":\"DCV\",\"p_IdEstado\":\"\",\"p_IdEmisor\":\"NORTEGRAN\",\"p_IdEmisorNuevo\":\"\",\"p_EsISFUT\":\"N\",\"p_ImpuestoDivExtranjero\":0,\"p_GastoTransferencia\":0,\"p_TasaEfectiva\":0,\"p_SecuenciaRelacionada\":\"\",\"p_SuscripcionComplementaria\":\"N\",\"p_IdEventoCapital\":51664}]}";
			
			EventoCapital_IN objIn = gson.fromJson(json,EventoCapital_IN.class);
			//mientras pruebo hare el rollback
			ModificarEventoCapital demo = new ModificarEventoCapital();
			
			EventoCapital_OUT ObjOut=demo.logica(conn, objIn);
				
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