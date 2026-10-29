package cl.bcs.custodia.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class EventoCapital {
	public Long					IdEventoCapital;
	public Long 				IdEmpresa;
	public String				IdTipoEventoCapital;
	public Long 				Secuencia;
	public String				Descripcion;
	public String				EsInscrito;
	public String				IdClasificacionTipoInstrumento;
	public String				IdTipoInstrumento;
	public Long					IdInstrumento;
	public String				Nemo;
	public BigDecimal			Factor;
	public BigDecimal			Valor;
	public String				IdMoneda;
	public Long 				IdInstrumentoNuevo;
	public String				NemoNuevo;
	public BigDecimal			FactorNuevo;
	public java.sql.Date 		FechaActualizacion;
	public java.sql.Date 		FechaLimite;
	public java.sql.Date 		FechaPago;
	public Long					NumeroDividendo;
	public String				IdTipoDividendo;
	public BigDecimal			PrecioAnterior;
	public java.sql.Timestamp 	FechaHoraIngresoRegistro;
	public java.sql.Timestamp 	FechaHoraUltimaActualizacion;
	public String				TipoIngreso;
	public Long					IdUsuario;
	public String				IdEstado;
	public BigDecimal			CuentaDepositanteSorteoLetras;
	public BigDecimal			MonedaSorteoLetras;
	public String				GlosaMonedaSorteoLetras;
	public BigDecimal			EmisorPagadorSorteoLetras;
	public BigDecimal			EmisorSorteoLetras;
	public BigDecimal			InstrumentoSorteoLetras;
	public String				GlosaInstrumentoSorteoLetras;
	public BigDecimal			BloqueoSorteoLetras;
	public BigDecimal			SorteadoSorteoLetras;
	public BigDecimal			APagarSorteoLetras;
	public BigDecimal			APagarPesosSorteoLetras;
	public String				Origen;
	public String				IdEmisor;
	public String				IdMonedaLiquidacion;
	public BigDecimal			Paridad;
	public String				IdEmisorNuevo;
	public String				EstadoNotificacion;
	public Long					IdInstrumentoAnterior;
	public Long					Indice;
	public BigDecimal			Impuesto;
	public BigDecimal			CantidadCuotas;
	public BigDecimal			Monto;
	public String				ISIN;
	public String				TipoEventoCapital;
	//-------- GCBSXNEC-1: EMISION PAGADA 2DA VUELTA
	public String				SecuenciaRelacionada;
	public String				SuscripcionComplementaria;
	public BigDecimal			ImpuestoDivExtranjero;
	public BigDecimal			GastoTransferencia;
	
	public String toString() {
		return Utils.printJson(this, false);
	};
}
