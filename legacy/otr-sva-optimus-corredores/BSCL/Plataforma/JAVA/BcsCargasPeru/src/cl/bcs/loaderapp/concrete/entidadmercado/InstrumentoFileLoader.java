package cl.bcs.loaderapp.concrete.entidadmercado;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.instrumentos.Instrumento;

public class InstrumentoFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {

		List<Object> instrumentos = new ArrayList<Object>();
		 int count = 0;
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withNullString("").withIgnoreSurroundingSpaces(true).parse(in);
            
            for (CSVRecord record : records) {
            	Instrumento instrumento = new Instrumento();
            	instrumento.setLinea(count);
            	instrumento.setNemotecnico(record.get("Nemotecnico"));
            	instrumento.setISIN(record.get("ISIN"));
            	instrumento.setCorteMinimo(record.get("CorteMinimo"));
            	instrumento.setEstaInscrito(record.get("EstaInscrito"));
            	instrumento.setEsDivisible(record.get("EsDivisible"));
            	instrumento.setRazonSocial(record.get("RazonSocial"));
            	instrumento.setLista(record.get("Lista"));
            	instrumento.setFechaEmision(record.get("FechaEmision"));
            	instrumento.setFechaVencimiento(record.get("FechaVencimiento"));
            	instrumento.setIdInstrumentoRelacionado(record.get("IdInstrumentoRelacionado"));
            	instrumento.setIdEmisor(record.get("IdEmisor"));
            	instrumento.setReajustable(record.get("Reajustable"));
            	instrumento.setIdMoneda(record.get("IdMoneda"));
            	instrumento.setIdTipoInstrumento(record.get("IdTipoInstrumento"));
            	instrumento.setIdEstado(record.get("IdEstado"));
            	instrumento.setDecimalesPrecio(record.get("DecimalesPrecio"));
            	instrumento.setTamano(record.get("Tamano"));
            	instrumento.setFechaInicioVigencia(record.get("FechaInicioVigencia"));
            	instrumento.setIndArt104(record.get("IndArt104"));
            	instrumento.setTasaFiscal(record.get("TasaFiscal"));
            	instrumento.setIdEntidadClasificadoraRiesgo(record.get("IdEntidadClasificadoraRiesgo"));
            	instrumento.setValorClasificacionRiesgo(record.get("ValorClasificacionRiesgo"));
            	instrumento.setTipoValorizacion(record.get("TipoValorizacion"));
            	instrumento.setValorizaPor(record.get("ValorizaPor"));
            	instrumento.setPrecio(record.get("Precio"));
            	instrumento.setTasa(record.get("Tasa"));
            	instrumento.setTieneCupon(record.get("TieneCupon"));
            	instrumento.setNumeroCuotasCirculacionMesActual(record.get("NumeroCuotasCirculacionMesActual"));
            	instrumento.setNumeroAccionesCirculacion(record.get("NumeroAccionesCirculacion"));
            	instrumento.setIndicadorLiquidaDCV(record.get("IndicadorLiquidaDCV"));
            	instrumento.setIdPaisEmisor(record.get("IdPaisEmisor"));
            	instrumento.setFrecuenciaCupon(record.get("FrecuenciaCupon"));
            	instrumento.setSerie(record.get("Serie"));
            	instrumento.setPrecioReferencia(record.get("PrecioReferencia"));
            	instrumento.setIndicadorBolson(record.get("IndicadorBolson"));
            	instrumento.setFechaReferencia(record.get("FechaReferencia"));
            	instrumento.setNumeroCupones(record.get("NumeroCupones"));
            	instrumento.setCodigoRubro(record.get("CodigoRubro"));
            	
            	instrumento.setLista( record.get("Lista") );
            	instrumento.setFechaInicioVigencia( record.get("FechaInicioVigencia") );
            	instrumento.setIdEntidadClasificadoraRiesgo( record.get("IdEntidadClasificadoraRiesgo") );
            	instrumento.setValorClasificacionRiesgo( record.get("ValorClasificacionRiesgo") );
            	instrumento.setTipoValorizacion( record.get("TipoValorizacion") );
            	instrumento.setFechaDevengo( record.get("FechaDevengo") );
            	instrumento.setPeriodoPagoCupon( record.get("PeriodoPagoCupon") );
            	instrumento.setTipoCalculoInteresesDevengado( record.get("TipoCalculoInteresesDevengado") );
            	instrumento.setTipoCalculoInteresesCupon( record.get("TipoCalculoInteresesCupon") );
            	instrumento.setIndicadorCuponeraVariable( record.get("IndicadorCuponeraVariable") );
            	instrumento.setIndicadorValorizarendiaHabil( record.get("IndicadorValorizarendiaHabil") );
            	instrumento.setConversionDias( record.get("ConversionDias") );
            	instrumento.setAjustefindeMes( record.get("AjustefindeMes") );
            	instrumento.setIndicadorMercadoLocal( record.get("IndicadorMercadoLocal") );
            	instrumento.setTipodeReajuste( record.get("TipodeReajuste") );
            	instrumento.setCUSIP( record.get("CUSIP") );
            	instrumentos.add(instrumento);
            	count++;
            }
            
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return instrumentos;
	}

}
