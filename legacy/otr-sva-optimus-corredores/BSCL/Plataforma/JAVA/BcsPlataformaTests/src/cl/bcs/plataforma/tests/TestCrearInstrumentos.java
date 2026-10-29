package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.ins.CrearInstrumentos;
import cl.bcs.plataforma.ins.CrearInstrumentos.CrearInstrumentoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.CrearInstrumentoSalida;
import cl.bcs.plataforma.ins.CrearInstrumentos.InstrumentoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.ValorRiesgoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.BolsaEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestCrearInstrumentos {
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");	
	
    @Test
    void testCrearInstrumentoFFM() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearInstrumentos demo = new CrearInstrumentos();
			
    		List<InstrumentoEntrada> instrumentosEntrada = new ArrayList<InstrumentoEntrada>();
    		InstrumentoEntrada instrumentoEntrada = new InstrumentoEntrada();
    		instrumentoEntrada.IdInterno = 0L;
    		instrumentoEntrada.Nemotecnico = "nemoPruebaCrear";
    		instrumentoEntrada.ISIN = "";
    		instrumentoEntrada.CorteMinimo = BigDecimal.ZERO;
    		instrumentoEntrada.EstaInscrito = "N";
    		instrumentoEntrada.InstrumentoAcreditado = "N";
    		instrumentoEntrada.RazonSocial = "SURA ESTRUCTURADO UF VIII CHILE A";
    		instrumentoEntrada.FechaEmision = new java.sql.Date(FORMATO_FECHA.parse("2010-01-15").getTime());
    		instrumentoEntrada.FechaVencimiento = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.IdEmisor = "SURA";
    		instrumentoEntrada.IdTipoReajuste = "";
    		instrumentoEntrada.IdMoneda = "CLP";
    		instrumentoEntrada.IdTipoInstrumento = "FFM";
    		instrumentoEntrada.IdEstado = "ACT";
    		instrumentoEntrada.Tamano = 0L;
    		instrumentoEntrada.Precio = BigDecimal.ZERO;
    		instrumentoEntrada.PrecioReferencia = BigDecimal.ZERO;
    		instrumentoEntrada.FechaReferencia =  new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.FrecuenciaCupon = BigDecimal.ZERO;
    		instrumentoEntrada.NumeroCupones = 0;
    		instrumentoEntrada.TipoBonoIRF = "";
    		instrumentoEntrada.OrigenInstrumento = "";
    		instrumentoEntrada.TipoRenta = "";
    		instrumentoEntrada.RelacionadoGrupo = "N";
    		instrumentoEntrada.FormadorMercado = "N";
    		instrumentoEntrada.PactoAndino = "N";
    		instrumentoEntrada.FactorExposicion = BigDecimal.ZERO;
    		instrumentoEntrada.LargoPlazo = "N";
    		instrumentoEntrada.IdInstrumentoRelacionado = 0L;
    		instrumentoEntrada.EsPutEsCall = "";
    		instrumentoEntrada.PrecioStrike = BigDecimal.ZERO;
    		instrumentoEntrada.PorcentajeCastigo = BigDecimal.ZERO;
    		instrumentoEntrada.FechaHastaAcreditado = null;
    		instrumentoEntrada.Tasa = null;
    		instrumentoEntrada.AfectoArt104 = "N";
    		instrumentoEntrada.AfectoArt107 = "N";
    		instrumentoEntrada.CobroComisionesCustodia = "S";
    		instrumentoEntrada.IdPaisEmisor = 44L;
    		instrumentoEntrada.Serie = "A";
    		instrumentosEntrada.add(instrumentoEntrada);
    		
    		List<ValorRiesgoEntrada> valoresRiesgo =  new ArrayList<ValorRiesgoEntrada>();
    		List<BolsaEntrada> bolsas =  new ArrayList<BolsaEntrada>();
    		BolsaEntrada bolsaEntrada = new BolsaEntrada();
    		bolsaEntrada.IdInterno = 0L;
    		bolsaEntrada.IdBolsa = "BCS";
    		bolsaEntrada.IdBolsaInstrumento = 0L;
    		bolsaEntrada.IdInstrumento = 0L;
    		bolsas.add(bolsaEntrada);
    		
    		CrearInstrumentoEntrada entrada = new CrearInstrumentoEntrada();
    		entrada.p_IdEmpresa = 0L;
    		entrada.p_IdUsuario = 0L;
    		entrada.p_IdModulo = "PLA";
    		entrada.p_SimularOperacion = "N";
    		entrada.p_UsaReglasNegocio = "N";
    		entrada.p_Instrumentos = instrumentosEntrada;
    		entrada.p_ValoresRiesgo = valoresRiesgo;
    		entrada.p_Bolsas = bolsas;

			CrearInstrumentoSalida salida = demo.logica(conn, entrada);
					
			assertEquals("UI_INSTRUMENTO_INGRESADO", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
    
    @Test
    void testCrearInstrumentoACN() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearInstrumentos demo = new CrearInstrumentos();
			
    		List<InstrumentoEntrada> instrumentosEntrada = new ArrayList<InstrumentoEntrada>();
    		InstrumentoEntrada instrumentoEntrada = new InstrumentoEntrada();
    		instrumentoEntrada.IdInterno = 0L;
    		instrumentoEntrada.Nemotecnico = "GRANJAMARPruebaCrear";
    		instrumentoEntrada.ISIN = "";
    		instrumentoEntrada.CorteMinimo = BigDecimal.ZERO;
    		instrumentoEntrada.EstaInscrito = "N";
    		instrumentoEntrada.InstrumentoAcreditado = "N";
    		instrumentoEntrada.RazonSocial = "GRANJA MARINA DE CAMARONES S.A.";
    		instrumentoEntrada.FechaEmision = new java.sql.Date(FORMATO_FECHA.parse("2010-01-15").getTime());
    		instrumentoEntrada.FechaVencimiento = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.IdEmisor = "SURA";
    		instrumentoEntrada.IdTipoReajuste = "";
    		instrumentoEntrada.IdMoneda = "CLP";
    		instrumentoEntrada.IdTipoInstrumento = "ACN";
    		instrumentoEntrada.IdEstado = "ACT";
    		instrumentoEntrada.Tamano = 0L;
    		instrumentoEntrada.Precio = BigDecimal.ZERO;
    		instrumentoEntrada.PrecioReferencia = BigDecimal.ZERO;
    		instrumentoEntrada.FechaReferencia =  new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.FrecuenciaCupon = BigDecimal.ZERO;
    		instrumentoEntrada.NumeroCupones = 0;
    		instrumentoEntrada.TipoBonoIRF = "";
    		instrumentoEntrada.OrigenInstrumento = "";
    		instrumentoEntrada.TipoRenta = "";
    		instrumentoEntrada.RelacionadoGrupo = "N";
    		instrumentoEntrada.FormadorMercado = "N";
    		instrumentoEntrada.PactoAndino = "N";
    		instrumentoEntrada.FactorExposicion = BigDecimal.ZERO;
    		instrumentoEntrada.LargoPlazo = "N";
    		instrumentoEntrada.IdInstrumentoRelacionado = 0L;
    		instrumentoEntrada.EsPutEsCall = "";
    		instrumentoEntrada.PrecioStrike = BigDecimal.ZERO;
    		instrumentoEntrada.PorcentajeCastigo = BigDecimal.ZERO;
    		instrumentoEntrada.FechaHastaAcreditado = null;
    		instrumentoEntrada.Tasa = null;
    		instrumentoEntrada.AfectoArt104 = "N";
    		instrumentoEntrada.AfectoArt107 = "N";
    		instrumentoEntrada.CobroComisionesCustodia = "S";
    		instrumentoEntrada.IdPaisEmisor = 44L;
    		instrumentoEntrada.Serie = "";
    		instrumentosEntrada.add(instrumentoEntrada);
    		
    		List<ValorRiesgoEntrada> valoresRiesgo =  new ArrayList<ValorRiesgoEntrada>();
    		List<BolsaEntrada> bolsas =  new ArrayList<BolsaEntrada>();
    		BolsaEntrada bolsaEntrada = new BolsaEntrada();
    		bolsaEntrada.IdInterno = 0L;
    		bolsaEntrada.IdBolsa = "BCS";
    		bolsaEntrada.IdBolsaInstrumento = 0L;
    		bolsaEntrada.IdInstrumento = 0L;
    		bolsas.add(bolsaEntrada);
    		
    		CrearInstrumentoEntrada entrada = new CrearInstrumentoEntrada();
    		entrada.p_IdEmpresa = 0L;
    		entrada.p_IdUsuario = 0L;
    		entrada.p_IdModulo = "PLA";
    		entrada.p_SimularOperacion = "N";
    		entrada.p_UsaReglasNegocio = "N";
    		entrada.p_Instrumentos = instrumentosEntrada;
    		entrada.p_ValoresRiesgo = valoresRiesgo;
    		entrada.p_Bolsas = bolsas;

			CrearInstrumentoSalida salida = demo.logica(conn, entrada);
					
			assertEquals("UI_INSTRUMENTO_INGRESADO", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
    
    @Test
    void testCrearInstrumentoRFBEM() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearInstrumentos demo = new CrearInstrumentos();
			
    		List<InstrumentoEntrada> instrumentosEntrada = new ArrayList<InstrumentoEntrada>();
    		InstrumentoEntrada instrumentoEntrada = new InstrumentoEntrada();
    		instrumentoEntrada.IdInterno = 0L;
    		instrumentoEntrada.Nemotecnico = "BLATM-GPruebaCrear";
    		instrumentoEntrada.ISIN = "";
    		instrumentoEntrada.CorteMinimo = BigDecimal.ZERO;
    		instrumentoEntrada.EstaInscrito = "N";
    		instrumentoEntrada.InstrumentoAcreditado = "N";
    		instrumentoEntrada.RazonSocial = "LATAM Airlines2 Group SERIE G";
    		instrumentoEntrada.FechaEmision = new java.sql.Date(FORMATO_FECHA.parse("2010-01-15").getTime());
    		instrumentoEntrada.FechaVencimiento = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.IdEmisor = "LTM";
    		instrumentoEntrada.IdTipoReajuste = "";
    		instrumentoEntrada.IdMoneda = "CLP";
    		instrumentoEntrada.IdTipoInstrumento = "BEM";
    		instrumentoEntrada.IdEstado = "ACT";
    		instrumentoEntrada.Tamano = 0L;
    		instrumentoEntrada.Precio = BigDecimal.ZERO;
    		instrumentoEntrada.PrecioReferencia = BigDecimal.ZERO;
    		instrumentoEntrada.FechaReferencia =  new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.FrecuenciaCupon = BigDecimal.ZERO;
    		instrumentoEntrada.NumeroCupones = 0;
    		instrumentoEntrada.TipoBonoIRF = "";
    		instrumentoEntrada.OrigenInstrumento = "";
    		instrumentoEntrada.TipoRenta = "";
    		instrumentoEntrada.RelacionadoGrupo = "N";
    		instrumentoEntrada.FormadorMercado = "N";
    		instrumentoEntrada.PactoAndino = "N";
    		instrumentoEntrada.FactorExposicion = BigDecimal.ZERO;
    		instrumentoEntrada.LargoPlazo = "N";
    		instrumentoEntrada.IdInstrumentoRelacionado = 0L;
    		instrumentoEntrada.EsPutEsCall = "";
    		instrumentoEntrada.PrecioStrike = BigDecimal.ZERO;
    		instrumentoEntrada.PorcentajeCastigo = BigDecimal.ZERO;
    		instrumentoEntrada.FechaHastaAcreditado = null;
    		instrumentoEntrada.Tasa = null;
    		instrumentoEntrada.AfectoArt104 = "N";
    		instrumentoEntrada.AfectoArt107 = "N";
    		instrumentoEntrada.CobroComisionesCustodia = "S";
    		instrumentoEntrada.IdPaisEmisor = 44L;
    		instrumentoEntrada.Serie = "BLATM-G";
    		instrumentoEntrada.ValorizaPor = "T";
    		instrumentosEntrada.add(instrumentoEntrada);
    		
    		List<ValorRiesgoEntrada> valoresRiesgo =  new ArrayList<ValorRiesgoEntrada>();
    		List<BolsaEntrada> bolsas =  new ArrayList<BolsaEntrada>();
    		BolsaEntrada bolsaEntrada = new BolsaEntrada();
    		bolsaEntrada.IdInterno = 0L;
    		bolsaEntrada.IdBolsa = "BCS";
    		bolsaEntrada.IdBolsaInstrumento = 0L;
    		bolsaEntrada.IdInstrumento = 0L;
    		bolsas.add(bolsaEntrada);
    		
    		CrearInstrumentoEntrada entrada = new CrearInstrumentoEntrada();
    		entrada.p_IdEmpresa = 0L;
    		entrada.p_IdUsuario = 0L;
    		entrada.p_IdModulo = "PLA";
    		entrada.p_SimularOperacion = "N";
    		entrada.p_UsaReglasNegocio = "N";
    		entrada.p_Instrumentos = instrumentosEntrada;
    		entrada.p_ValoresRiesgo = valoresRiesgo;
    		entrada.p_Bolsas = bolsas;

			CrearInstrumentoSalida salida = demo.logica(conn, entrada);
					
			assertEquals("UI_INSTRUMENTO_INGRESADO", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
    
    @Test
    void testCrearInstrumentoETX() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearInstrumentos demo = new CrearInstrumentos();
			
    		List<InstrumentoEntrada> instrumentosEntrada = new ArrayList<InstrumentoEntrada>();
    		InstrumentoEntrada instrumentoEntrada = new InstrumentoEntrada();
    		instrumentoEntrada.IdInterno = 0L;
    		instrumentoEntrada.Nemotecnico = "GERJPruebaCrear";
    		instrumentoEntrada.ISIN = "";
    		instrumentoEntrada.CorteMinimo = BigDecimal.ZERO;
    		instrumentoEntrada.EstaInscrito = "N";
    		instrumentoEntrada.InstrumentoAcreditado = "N";
    		instrumentoEntrada.RazonSocial = "MARKET VECTORS GERMANY SMALL-CAP ETF2";
    		instrumentoEntrada.FechaEmision = new java.sql.Date(FORMATO_FECHA.parse("2010-01-15").getTime());
    		instrumentoEntrada.FechaVencimiento = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.IdEmisor = "GERJ";
    		instrumentoEntrada.IdTipoReajuste = "";
    		instrumentoEntrada.IdMoneda = "USD";
    		instrumentoEntrada.IdTipoInstrumento = "ETX";
    		instrumentoEntrada.IdEstado = "ACT";
    		instrumentoEntrada.Tamano = 0L;
    		instrumentoEntrada.Precio = BigDecimal.ZERO;
    		instrumentoEntrada.PrecioReferencia = BigDecimal.ZERO;
    		instrumentoEntrada.FechaReferencia =  new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		instrumentoEntrada.FrecuenciaCupon = BigDecimal.ZERO;
    		instrumentoEntrada.NumeroCupones = 0;
    		instrumentoEntrada.TipoBonoIRF = "";
    		instrumentoEntrada.OrigenInstrumento = "";
    		instrumentoEntrada.TipoRenta = "";
    		instrumentoEntrada.RelacionadoGrupo = "N";
    		instrumentoEntrada.FormadorMercado = "N";
    		instrumentoEntrada.PactoAndino = "N";
    		instrumentoEntrada.FactorExposicion = BigDecimal.ZERO;
    		instrumentoEntrada.LargoPlazo = "N";
    		instrumentoEntrada.IdInstrumentoRelacionado = 0L;
    		instrumentoEntrada.EsPutEsCall = "";
    		instrumentoEntrada.PrecioStrike = BigDecimal.ZERO;
    		instrumentoEntrada.PorcentajeCastigo = BigDecimal.ZERO;
    		instrumentoEntrada.FechaHastaAcreditado = null;
    		instrumentoEntrada.Tasa = null;
    		instrumentoEntrada.AfectoArt104 = "N";
    		instrumentoEntrada.AfectoArt107 = "N";
    		instrumentoEntrada.CobroComisionesCustodia = "S";
    		instrumentoEntrada.IdPaisEmisor = 0L;
    		instrumentoEntrada.Serie = "";
    		instrumentosEntrada.add(instrumentoEntrada);
    		
    		List<BolsaEntrada> bolsas =  new ArrayList<BolsaEntrada>();
    		BolsaEntrada bolsaEntrada = new BolsaEntrada();
    		bolsaEntrada.IdInterno = 0L;
    		bolsaEntrada.IdBolsa = "BCS";
    		bolsaEntrada.IdBolsaInstrumento = 0L;
    		bolsaEntrada.IdInstrumento = 0L;
    		bolsas.add(bolsaEntrada);
    		
    		List<ValorRiesgoEntrada> valoresRiesgo =  new ArrayList<ValorRiesgoEntrada>();
    		ValorRiesgoEntrada valorRiesgoEntrada = new ValorRiesgoEntrada();
    		valorRiesgoEntrada.IdInterno = 0L;
    		valorRiesgoEntrada.IdEntidadClasificadoraRiesgo = "BCS";
    		valorRiesgoEntrada.ValorClasificacionRiesgo = "A";
    		valorRiesgoEntrada.IdInstrumento = 0L;
    		
    		CrearInstrumentoEntrada entrada = new CrearInstrumentoEntrada();
    		entrada.p_IdEmpresa = 0L;
    		entrada.p_IdUsuario = 0L;
    		entrada.p_IdModulo = "PLA";
    		entrada.p_SimularOperacion = "N";
    		entrada.p_UsaReglasNegocio = "N";
    		entrada.p_Instrumentos = instrumentosEntrada;
    		entrada.p_ValoresRiesgo = valoresRiesgo;
    		entrada.p_Bolsas = bolsas;

			CrearInstrumentoSalida salida = demo.logica(conn, entrada);
					
			assertEquals("UI_INSTRUMENTO_INGRESADO", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
}