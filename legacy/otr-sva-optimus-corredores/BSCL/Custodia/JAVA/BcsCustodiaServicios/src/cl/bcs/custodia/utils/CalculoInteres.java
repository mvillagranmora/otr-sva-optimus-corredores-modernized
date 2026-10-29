package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;

import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CalculoInteres {
	
	public static Log log = new Log("CUS.CalculoInteres"); 
	
	public static class CalculoInteresEntrada extends BcsPojo {
		public Long 		p_IdEmpresa;
		public String		p_SoloDiasHabiles;
		public String 		p_FechaDesde;
		public String 		p_FechaHasta;
		public BigDecimal 	p_Monto;
		public String 		p_IdMoneda;
		public BigDecimal 	p_Tasa;
		
		public CalculoInteresEntrada() {
			this.p_IdEmpresa = 1L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public BigDecimal CalculoInteres (Connection con, CalculoInteresEntrada entrada) throws Exception {
		BigDecimal Interes = new BigDecimal(0);
		
		//Obtener Plazo
		InObtenerPlazo inObtenerPlazo = new InObtenerPlazo();
		
		FechasIn f1 = new FechasIn();
		f1.SoloDiasHabiles 	= entrada.p_SoloDiasHabiles;
		f1.FechaDesde 		= entrada.p_FechaDesde;
		f1.FechaHasta 		= entrada.p_FechaHasta;
		f1.IdInterno 		= 0L;
		inObtenerPlazo.p_Fechas.add(f1);
		ObtenerPlazo obtenerPlazo = new ObtenerPlazo();
		OutObtenerPlazo salidaObtenerPlazo;
		

		if (Fecha.toDate(entrada.p_FechaDesde).compareTo(Fecha.toDate(entrada.p_FechaHasta)) < 0) {
			salidaObtenerPlazo = obtenerPlazo.logica(con, inObtenerPlazo);
			
			Long Plazo = salidaObtenerPlazo.p_out_Fechas.get(0).Plazo;
			Long Base = "CLP".equals(entrada.p_IdMoneda) ? 30L : 360L;
			
			Interes = (entrada.p_Monto.multiply((entrada.p_Tasa.divide(new BigDecimal(100), MathContext.DECIMAL128)), MathContext.DECIMAL128)
										.multiply( new BigDecimal(Plazo), MathContext.DECIMAL128)
								 ).divide(new BigDecimal(Base), MathContext.DECIMAL128);
		} else {
			Interes = new BigDecimal(0);
		}

		return Interes;
	}

}
