// GENERATED DOUBLE of the legacy cl.bcs.plataforma.tas.EntregarPreciosTasas by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.tas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;


import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class EntregarPreciosTasas implements JavaCall{
	
	// Log
	static final Logger logger = LogManager.getLogger(EntregarPreciosTasas.class.getName());	
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class EntregarPreciosTasas_Entrada 
	{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdPais;
		public List<EntregarPreciosTasas_Entrada_DatosInstrumentos> p_DatosInstrumentos;
		
		public EntregarPreciosTasas_Entrada() 
		{			
			p_DatosInstrumentos = new ArrayList<EntregarPreciosTasas_Entrada_DatosInstrumentos>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	 
	public static class EntregarPreciosTasas_Entrada_DatosInstrumentos 
	{
		public Long 		 IdRegistro;
		public String 		 TipoCartera;   		// --> PROPIA: CARTERA PROPIA  /  TERCEROS: CUENTA TERCEROS / "0": TODOS 
		public String 		 IdUso; 				// --> CARTERA / GARANTIA / SUBYACENTE / ORDEN / PRECIOMEDIO / SUSOSA
		public String 		 IdTipoCriterioTasa;  	// --> COMERCIAL /  NORMATIVO  / "0": TODOS
		public String 		 IdClasificacionTipoInstrumento; //--> RV, RF, IF
		public String 		 IdTipoInstrumento; 	// -->  ACN, ACX, BBC,.. / "0": TODOS
		public Long 		 IdInstrumento;
		public String 		 Nemotecnico;
		public String 		 IdMoneda;
		public java.sql.Date FechaConsulta;
		public Long 		 PlazoResidual;
		public String		 NemoSVS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	
	public static class EntregarPreciosTasas_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<EntregarPreciosTasas_Salida_PreciosTasas> 	P_OUT_PRECIOSTASAS;
		
		
		public EntregarPreciosTasas_Salida() 
		{				
			P_OUT_MENSAJES		= new ArrayList<MensajeSalida>();
			P_OUT_PRECIOSTASAS	= new ArrayList<EntregarPreciosTasas_Salida_PreciosTasas>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntregarPreciosTasas_Salida_PreciosTasas {
		public Long 				IdRegistro;
		public String 				IdClasificacionTipoInstrumento;
		public String 				IdTipoInstrumento;
		public Long 				IdInstrumento;
		public String 				Nemotecnico;
		public String 				IdTipoTasa;
		public BigDecimal 			Tasa;
		public String 				IdTipoPrecio;
		public BigDecimal 			Precio;
		public String 				CodigoFuente;
		public String 				MensajeFuente;
		public java.sql.Timestamp 	FechaFuente;
		public String 				MensajeError;
		public String 				IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	
	// MSH para Instrumento-Plazo RF/IF
	public static class InstrumentoPlazo {
		public Long Correlativo;
		public String IdTipoInstrumento;
		public String IdTipoReajuste;
		public Long   Plazo;

		public String toString() {
			return Utils.printJson(this, false);
		}

		@Override
		public boolean equals(Object o) {
			InstrumentoPlazo f = (InstrumentoPlazo)o;
			// MSH
			if ( this.IdTipoInstrumento != null && f.IdTipoInstrumento != null &&
				 this.IdTipoReajuste != null && f.IdTipoReajuste != null &&
				 this.Plazo != null && f.Plazo != null ) {
				return this.IdTipoInstrumento.equals(f.IdTipoInstrumento) &&
					   this.IdTipoReajuste.equals(f.IdTipoReajuste) &&
					   this.Plazo.equals(f.Plazo);
			} else {
				return false;
			}
		}
	}
	
	public static class InstrumentoFecha {
		public String Fecha;
		public Long Correlativo;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		@Override
		public boolean equals(Object o) {
			InstrumentoFecha f = (InstrumentoFecha)o;
			return this.Fecha.equals(f.Fecha) && this.IdInstrumento.equals(f.IdInstrumento);
		}
	}
	
	public static class InstrumentoFechaEntidad {
		public String Fecha;
		public String FechaDesde;
		public Long Correlativo;
		public Long IdInstrumento;
		public String Entidad;
		public String TipoRegistro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		@Override
		public boolean equals(Object o) {
			InstrumentoFechaEntidad f = (InstrumentoFechaEntidad)o;
			return this.Fecha.equals(f.Fecha) && this.IdInstrumento.equals(f.IdInstrumento) &&
					this.TipoRegistro.equals(f.TipoRegistro) && this.Entidad.equals(f.Entidad);
		}
	}
	
	public static class Fuente {
		public String Codigo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoTasasPublicadas {
		public Long Correlativo;
		public String IdTipoTasa;
		public String IdTipoPrecio;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Date FechaDesde;
		public Date FechaHasta;
		public String CodigoFuente;
		public Long Plazo;
		public Date FechaActual;
		public Date FechaPlazo;
		public String IdMoneda;
		
		@Override
		public boolean equals(Object o) {
			InstrumentoTasasPublicadas f = (InstrumentoTasasPublicadas)o;
			return this.IdTipoTasa.equals(f.IdTipoTasa) && this.IdTipoPrecio.equals(f.IdTipoPrecio) && 
					this.IdInstrumento.equals(f.IdInstrumento) && this.Nemotecnico.equals(f.Nemotecnico) &&
					this.FechaDesde.compareTo(f.FechaDesde) == 0 && this.FechaHasta.compareTo(f.FechaHasta) == 0 &&
					this.CodigoFuente.equals(f.CodigoFuente) && (this.IdMoneda != null && f.IdMoneda != null && this.IdMoneda.equals(f.IdMoneda));
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public InstrumentoTasasPublicadas () {
			this.Plazo=0L;
			this.IdMoneda = "";
		}
	}
	
	// Precios para Tasas Publicadas
	public static class PreciosTasasPublicadas
	{
		public Long 				IdInstrumento;
		public String 				Nemotecnico; 
		public String				IdTipoInstrumento;
		public String				IdClasificacionTipoInstrumento;
		public String				CodigoFuente;
		public Long 				IdPrecioTasa;		
		public String 				IdTipoTasa;		
		public BigDecimal			Tasa;
		public BigDecimal			TasaEstimada;		
		public String 				IdTipoPrecio; 
		public BigDecimal 			Precio;
		public java.sql.Timestamp	FechaSolicitud;
		public Long 				Plazo;
		public java.sql.Date		FechaDesde;
		public java.sql.Date		FechaHasta;
		public String				IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Precios para CodigoFuente = "TIPOINSTRUMENTO"
	public static class PreciosTasasTipoInstrumento
	{
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public BigDecimal			Tasa;
		public java.sql.Date		Fecha;
		public java.sql.Date		FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// MSH "TIPOINSTRUMENTO x PLAZO
	public static class PreciosTasasInstrumentoPlazo
	{
		public String 				IdTipoInstrumento;
		public String 				IdTipoReajuste;
		public Long					Plazo;		
		public BigDecimal 			Precio;
		public BigDecimal			Tasa;
		public java.sql.Timestamp	FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Precios para CodigoFuente = "VPG"
	public static class VectorPreciosGarantias
	{
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public java.sql.Date		Fecha;
		public java.sql.Timestamp   FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioEntidadVector
	{
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public java.sql.Date		Fecha;
		public java.sql.Date		FechaEncontrada;
		public String 				TipoRegistro;
		public String				Entidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Precios para CodigoFuente = "PRECIOSAGF"
	public static class PreciosMercadoAGF 
	{
		public Long 				IdInstrumento;
		public BigDecimal 			ValorCuota;
		public java.sql.Date		FechaEncontrada;
		public java.sql.Date		Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Precios para CodigoFuente = "PRECIOSBCS"
	public static class PreciosMercadoBcs 
	{
		public Long 				IdInstrumento;
		public Date					Fecha;
		public Date					FechaEncontrada;
		public BigDecimal 			PrecioCierre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Precios para CodigoFuente = "PRECIOMEDIO"
	public static class PreciosMedioBcs 
	{	
		public Long 				IdInstrumento;
		public BigDecimal 			PrecioMedio;
		public java.sql.Date		Fecha;
		public java.sql.Date		FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Precios para CodigoFuente = "INSTRUMENTO"
	public static class PreciosTasasInstrumento 
	{	
		public Long 				IdInstrumento;		
		public BigDecimal 			Precio;	
		public BigDecimal 			Tasa;	
	}	

	public static class Criterio 
	{	
		public Long 		 IdCriterioSeleccionTasas;		
		public Long 		 Prioridad;
		public String 		 TipoCartera;
		public String 		 IdUso;
		public String 		 IdTipoCriterioTasa;
		public String 		 IdClasificacionTipoInstrumento;
		public String 		 IdTipoInstrumento;
		public Long 		 DiasAntiguedad;		
		public String 		 CodigoFuente;
		public String 		 IdTipoTasa;
		public String 		 IdTipoPrecio;
		public String 		 Interpolar;   // S/N
		public String 		 Nemotecnico;   // S/N
		public Long 		 IdEmpresa;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	public static class Instrumento_Proceso 
	{
		public Long 		 IdRegistro;		
		public String 		 TipoCartera;   // "PROPIA": CARTERA PROPIA  /  "TERCEROS": CUENTA TERCEROS  / "0": TODOS 
		public String 		 IdUso;
		public String 		 IdTipoCriterioTasa;
		public String 		 IdClasificacionTipoInstrumento;
		public String 		 IdTipoInstrumento;
		public Long 		 IdInstrumento;
		public String 		 Nemotecnico;
		public String 		 NemoSVS;
		public String 		 IdMoneda;
		public java.sql.Date FechaConsulta;
		public Long 		 PlazoResidual;
		public Long 		 IdInstrumentoRaiz;
		public List<Criterio> criterios = new ArrayList<>();
		
		//si esta el criterio, significa que lo visitamos. Si el valor esta null significa que no lo encontramos
		public HashMap<Criterio,EntregarPreciosTasas_Salida_PreciosTasas> precioPorCriterio = new HashMap<>();
		
		//Para validaciones
		public long 		 errores 		  = 0;
		public long 		 advertencias 	  = 0;
		public long 		 autorizaciones   = 0;
		public long 		 ok 			  = 0;
		
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}
	
	public static class Validacion {
		public String Tipo;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class InfoInstrumento 
	{	
		public Long 				IdInstrumento;
		public Long 				IdInstrumentoRaiz;
		public java.sql.Date 		FechaConsulta;
		public BigDecimal 			TasaIntradayBase360;		
		public BigDecimal 			AperturaBCS;	
		public BigDecimal 			PrecioSemilla;
		public BigDecimal 			PrecioT1;
		public String				IdCondicionLiquidacion;
		public String				Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("EntregarPreciosTasas", "call", new Object[]{conn, jin}, JsonObject.class); }
	
	public void limpiarInstrumentos(List<Instrumento_Proceso> instrumentos, List<Instrumento_Proceso> instrumentosConErrores) { throw new UnsupportedOperationException("oracle double EntregarPreciosTasas.limpiarInstrumentos: not part of the double"); }
	
	public boolean faltanInstrumentos(List<Instrumento_Proceso> instrumentosProceso, List<String> fuentesRequeridas) { throw new UnsupportedOperationException("oracle double EntregarPreciosTasas.faltanInstrumentos: not part of the double"); }
	
	public List<String> calcularFuentesRequeridas(List<Instrumento_Proceso> instrumentosProceso) { throw new UnsupportedOperationException("oracle double EntregarPreciosTasas.calcularFuentesRequeridas: not part of the double"); }
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	@Trace
	public EntregarPreciosTasas_Salida logica(Connection con, EntregarPreciosTasas_Entrada entrada) throws Exception { return cl.bcs.oracle.Doubles.answer("EntregarPreciosTasas", "logica", new Object[]{con, entrada}, EntregarPreciosTasas_Salida.class); }
	
	public Date SumarRestar(Date fecha , int dias) { throw new UnsupportedOperationException("oracle double EntregarPreciosTasas.SumarRestar: not part of the double"); }
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	
	public static void main(String[] args) throws Exception 
	{ throw new UnsupportedOperationException("oracle double EntregarPreciosTasas.main: not part of the double"); }
}
