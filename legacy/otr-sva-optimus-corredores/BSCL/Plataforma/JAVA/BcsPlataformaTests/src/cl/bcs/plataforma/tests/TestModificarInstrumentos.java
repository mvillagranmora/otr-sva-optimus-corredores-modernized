package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.ins.CrearInstrumentos;
import cl.bcs.plataforma.ins.CrearInstrumentos.CrearInstrumentoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.CrearInstrumentoSalida;
import cl.bcs.plataforma.ins.CrearInstrumentos.InstrumentoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.ValorRiesgoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.BolsaEntrada;
import cl.bcs.plataforma.ins.ModificarInstrumentos;
import cl.bcs.plataforma.ins.ModificarInstrumentos.ModificarInstrumentoEntrada;
import cl.bcs.plataforma.ins.ModificarInstrumentos.ModificarInstrumentoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;

class TestModificarInstrumentos {
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");
	String sql = "SELECT \"IdInstrumento\", \"Nemotecnico\", \"ISIN\", \"CorteMinimo\", \"EstaInscrito\", \"EsDivisible\", \"RazonSocial\", \"FechaEmision\", \"FechaVencimiento\", \"IdInstrumentoRelacionado\", \"IdEmisor\", \"IdTipoReajuste\", \"Reajustable\", \"IdMoneda\", \"IdTipoInstrumento\", \"IdEstado\", CAST (\"DecimalesPrecio\" AS INTEGER), \"Tamano\", \"FechaInicioVigencia\", \"IndArt104\", \"TasaFiscal\", \"TipoValorizacion\", \"ValorizaPor\", \"Precio\", \"Tasa\", \"TieneCupon\", \"NumeroCuotasCirculacionMesActual\", \"NumeroAccionesCirculacion\", \"IndicadorLiquidaDCV\", \"IdPaisEmisor\", \"FrecuenciaCupon\", \"Serie\", \"PrecioReferencia\", \"IndicadorBolson\", \"FechaReferencia\", \"NumeroCupones\", \"CodigoRubro\", CAST (\"PresenciaBursatilAjustada\" AS INTEGER), \"ModalidadLiquidacion\", \"PrecioStrike\", \"LargoPlazo\", \"EsPutEsCall\", \"FactorExposicion\", \"OrigenInstrumento\", \"EstiloOpcion\", \"PactoAndino\", \"FormadorMercado\", \"PorcentajeCastigo\", \"FechaHastaAcreditado\", \"InstrumentoAcreditado\", \"RelacionadoGrupo\", \"ModalidadEjercicio\", \"TipoRenta\", \"TipoBonoIRF\", \"IdMonedaEmision\", \"IdCarga\", \"NumeroAmortizaciones\", \"Tera\", \"IdOrdenSerie\", \"IdTipoInstrumentoOrigen\", \"Rentabilidad7Dias\", \"Rentabilidad30Dias\", \"Rentabilidad52Sem\", \"DocumentoIdentidadFondo\", \"AfectoArt107\", \"DividendoOtraMoneda\", \"EsAutorizado\", \"AfectoArt104\", \"CobroComisionesCustodia\", \"IdInstrumentoRaiz\", \"IdCondicionLiquidacion\", \"EsTranzable\", \"EsInstrumentoCalificado\" FROM \"BSCL.Plataforma.BD::INS.Instrumento\";";
	String sql2 = "SELECT \"IdBolsaInstrumento\", \"IdInstrumento\", \"IdBolsa\" FROM \"BSCL.Plataforma.BD::INS.BolsaInstrumento\" WHERE \"IdInstrumento\" = :p_idInstrumento;";

    @Test
    void testCrearModificarInstrumento() throws Exception {
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
    		
    		//Modificar
			SQL _sql = new SQL();
    		List<cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada> c = _sql.query(conn, sql, new HashMap<>(), cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada.class);
    		
			ModificarInstrumentos demoMod = new ModificarInstrumentos();
				
			List<cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada> instrumentosEntradaMod = new ArrayList<cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada>();
			cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada instrumentoEntradaMod = new cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada();
			
			instrumentoEntradaMod.Nemotecnico = c.get(0).Nemotecnico;
			instrumentoEntradaMod.ISIN = c.get(0).ISIN;
			instrumentoEntradaMod.CorteMinimo = c.get(0).CorteMinimo;
			instrumentoEntradaMod.EstaInscrito = c.get(0).EstaInscrito;
			instrumentoEntradaMod.InstrumentoAcreditado = c.get(0).InstrumentoAcreditado;
			instrumentoEntradaMod.RazonSocial = "PRUEBA MOD";
			instrumentoEntradaMod.FechaEmision = c.get(0).FechaEmision;
			instrumentoEntradaMod.FechaVencimiento = c.get(0).FechaVencimiento;
			instrumentoEntradaMod.IdEmisor = c.get(0).IdEmisor;
			instrumentoEntradaMod.IdTipoReajuste = c.get(0).IdTipoReajuste;
			instrumentoEntradaMod.IdMoneda = c.get(0).IdMoneda;
			instrumentoEntradaMod.IdTipoInstrumento = c.get(0).IdTipoInstrumento;
			instrumentoEntradaMod.IdEstado = c.get(0).IdEstado;
			instrumentoEntradaMod.Tamano = c.get(0).Tamano;
			instrumentoEntradaMod.Precio = c.get(0).Precio;
			instrumentoEntradaMod.PrecioReferencia = c.get(0).PrecioReferencia;
    		instrumentoEntradaMod.FechaReferencia =  c.get(0).FechaReferencia;
    		instrumentoEntradaMod.FrecuenciaCupon = c.get(0).FrecuenciaCupon;
    		instrumentoEntradaMod.NumeroCupones = c.get(0).NumeroCupones;
    		instrumentoEntradaMod.TipoBonoIRF = c.get(0).TipoBonoIRF;
    		instrumentoEntradaMod.OrigenInstrumento = c.get(0).OrigenInstrumento;
    		instrumentoEntradaMod.TipoRenta = c.get(0).TipoRenta;
    		instrumentoEntradaMod.RelacionadoGrupo = c.get(0).RelacionadoGrupo;
    		instrumentoEntradaMod.FormadorMercado = c.get(0).FormadorMercado;
    		instrumentoEntradaMod.PactoAndino = c.get(0).PactoAndino;
    		instrumentoEntradaMod.FactorExposicion = c.get(0).FactorExposicion;
    		instrumentoEntradaMod.LargoPlazo = c.get(0).LargoPlazo;
    		instrumentoEntradaMod.IdInstrumentoRelacionado = c.get(0).IdInstrumentoRelacionado;
    		instrumentoEntradaMod.EsPutEsCall = c.get(0).EsPutEsCall;
    		instrumentoEntradaMod.PrecioStrike = c.get(0).PrecioStrike;
    		instrumentoEntradaMod.PorcentajeCastigo = c.get(0).PorcentajeCastigo;
    		instrumentoEntradaMod.FechaHastaAcreditado = c.get(0).FechaHastaAcreditado;
    		instrumentoEntradaMod.Tasa = c.get(0).Tasa;
    		instrumentoEntradaMod.AfectoArt104 = c.get(0).AfectoArt104;
    		instrumentoEntradaMod.AfectoArt107 = c.get(0).AfectoArt107;
    		instrumentoEntradaMod.CobroComisionesCustodia = c.get(0).CobroComisionesCustodia;
    		instrumentoEntradaMod.IdPaisEmisor = c.get(0).IdPaisEmisor;
    		instrumentoEntradaMod.Serie = c.get(0).Serie;
    		instrumentoEntradaMod.ValorizaPor = c.get(0).ValorizaPor;
    		instrumentoEntradaMod.IdInstrumento = c.get(0).IdInstrumento;
    		instrumentosEntradaMod.add(instrumentoEntradaMod);
			   		
    		List<cl.bcs.plataforma.ins.ModificarInstrumentos.ValorRiesgoEntrada> valoresRiesgoMod =  new ArrayList<cl.bcs.plataforma.ins.ModificarInstrumentos.ValorRiesgoEntrada>();
    		List<cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada> bolsasMod =  new ArrayList<cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada>();
			HashMap<String, Object> paramsIn = new HashMap<String, Object>();
			paramsIn.put("p_idInstrumento", c.get(0).IdInstrumento);
			
			cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada bolsaEntradaMod = new cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada();
			List<cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada> c2 = _sql.query(conn, sql2, paramsIn, cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada.class);
    		bolsaEntradaMod.IdBolsa =  c2.get(0).IdBolsa;
    		bolsaEntradaMod.IdBolsaInstrumento =  c2.get(0).IdBolsaInstrumento;
    		bolsaEntradaMod.IdInstrumento = c.get(0).IdInstrumento;
    		bolsasMod.add(bolsaEntradaMod);
    		    		
    		cl.bcs.plataforma.ins.ModificarInstrumentos.ModificarInstrumentoEntrada entradaMod = new cl.bcs.plataforma.ins.ModificarInstrumentos.ModificarInstrumentoEntrada();
			entradaMod.p_IdEmpresa = 0L;
			entradaMod.p_IdUsuario = 0L;
			entradaMod.p_IdModulo = "PLA";
			entradaMod.p_SimularOperacion = "N";
			entradaMod.p_UsaReglasNegocio = "N";
			entradaMod.p_Instrumentos = instrumentosEntradaMod;
			entradaMod.p_ValoresRiesgo = valoresRiesgoMod;
			entradaMod.p_Bolsas = bolsasMod;
			
			ModificarInstrumentoSalida salidaMod = demoMod.logica(conn, entradaMod);
			assertAll(
			() -> assertEquals("UI_INSTRUMENTO_INGRESADO", salida.P_OUT_MENSAJES.get(0).codigo),
			() -> assertEquals("UI_INSTRUMENTO_MODIFICADO", salidaMod.P_OUT_MENSAJES.get(0).codigo));
    	}
    }
}