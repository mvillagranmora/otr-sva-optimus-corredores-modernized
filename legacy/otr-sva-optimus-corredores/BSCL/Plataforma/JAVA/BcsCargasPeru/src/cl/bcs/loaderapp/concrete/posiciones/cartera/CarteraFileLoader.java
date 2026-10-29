package cl.bcs.loaderapp.concrete.posiciones.cartera;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.posiciones.Posicion;
import cl.bcs.loaderapp.utils.LoaderUtils;

public class CarteraFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		List<Object> posiciones = new ArrayList<>(); 
		try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).withNullString("NULL").parse(in);
            SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd-mm-yyyy");
            SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-mm-dd");
            for (CSVRecord record : records) {
            	Posicion posicion = new Posicion();
            	posicion.IdInterno = (long) 1;
            	posicion.DocumentoIdentidad = (record.isMapped("RUTCLIENTE")) ? record.get("RUTCLIENTE") : "";
            	posicion.Portafolio = (record.isMapped("PORTAFOLIO")) ? record.get("PORTAFOLIO") : "";
            	posicion.CondicionLiquidacion = (record.isMapped("CONDICIONLIQUIDACION")) ? record.get("CONDICIONLIQUIDACION") : "";
            	posicion.Boveda = (record.isMapped("BOVEDA")) ? record.get("BOVEDA") : "";
            	posicion.CuentaCustodio = (record.isMapped("RUTCLIENTE")) ? record.get("RUTCLIENTE") : "";//CUENTACUSTODIO
            	posicion.Concepto = (record.isMapped("CONCEPTO")) ? record.get("CONCEPTO") : "";
            	posicion.Nemotecnico = (record.isMapped("NEMO")) ? record.get("NEMO") : "";
            	posicion.Emisor = (record.isMapped("EMISOR")) ? record.get("EMISOR") : "";
            	posicion.Reajuste = (record.isMapped("REAJUSTE") && record.get("REAJUSTE")!=null) ? record.get("REAJUSTE") : "";
            	posicion.FechaVencimiento = yyyymmdd.format(ddmmyyyy.parse((record.isMapped("FECHAVENCIMIENTO") && record.get("FECHAVENCIMIENTO")!=null) ? record.get("FECHAVENCIMIENTO") : "01-01-1900"));
            	posicion.Cantidad = LoaderUtils.analizarNumero((record.isMapped("CANTIDAD")) ? record.get("CANTIDAD") : "0");
            	posicion.Precio = LoaderUtils.analizarNumero((record.isMapped("PRECIO") && record.get("PRECIO")!=null && !record.get("PRECIO").equalsIgnoreCase("")) ? record.get("PRECIO") : "0");
            	posicion.Tasa = LoaderUtils.analizarNumero((record.isMapped("TASA") && record.get("TASA")!=null) ? record.get("TASA") : "0");
            	posicion.Monto = LoaderUtils.analizarNumero((record.isMapped("MONTO")) ? record.get("MONTO") : "0");
            	posicion.ValorRescate = LoaderUtils.analizarNumero((record.isMapped("VALORRESCATE")) ? record.get("VALORRESCATE") : "0");
            	posicion.Glosa = (record.isMapped("GLOSA")) ? record.get("GLOSA") : "";
            	posicion.NemoGarantizado = (record.isMapped("NEMOGARANTIZADO")) ? record.get("NEMOGARANTIZADO") : "";
            	posicion.Moneda = (record.isMapped("MONEDA")) ? record.get("MONEDA") : "";
            	posicion.Fungible = (record.isMapped("FUNGIBLE")) ? record.get("FUNGIBLE") : "";
            	posicion.Bolsa = (record.isMapped("BOLSA")) ? record.get("BOLSA") : "";
            	posicion.CorrelativoReporte = LoaderUtils.analizarNumero( (record.isMapped("CORRELATIVOREPORTE")) ? record.get("CORRELATIVOREPORTE") : "0").longValue();
            	posicion.TipoOperacion = (record.isMapped("TIPOOPERACION")) ? record.get("TIPOOPERACION") : "";
            	
            	posicion.Plazo = LoaderUtils.analizarNumero( (record.isMapped("PLAZO")) ? record.get("PLAZO") : "0").longValue();
            	posicion.FechaLiquidacion = yyyymmdd.format(ddmmyyyy.parse( (record.isMapped("FECHALIQUIDACION")) ? record.get("FECHALIQUIDACION") : "01-01-1900" ));
            	posicion.FechaOperacion = yyyymmdd.format(ddmmyyyy.parse( (record.isMapped("FECHAOPERACION")) ? record.get("FECHAOPERACION") : "01-01-1900" ));
            	posicion.TipoCambio = LoaderUtils.analizarNumero( (record.isMapped("TIPOCAMBIO")) ? record.get("TIPOCAMBIO") : "0");
            	posicion.MontoContado = LoaderUtils.analizarNumero( (record.isMapped("MONTOCONTADO")) ? record.get("MONTOCONTADO") : "0");
            	posicion.MontoPlazo = LoaderUtils.analizarNumero( (record.isMapped("MONTOPLAZO")) ? record.get("MONTOPLAZO") : "0");
            	posicion.EsNominal = (record.isMapped("ESNOMINAL")) ? record.get("ESNOMINAL") : "";
            	posicion.ReferenciaReportado = (record.isMapped("REFERENCIAREPORTADO") && record.get("REFERENCIAREPORTADO")!=null) ? record.get("REFERENCIAREPORTADO") : "";
            	//REPORTES
            	posicion.TipoOperacion = (record.isMapped("TIPO")) ? record.get("TIPO") : "";
            	posicion.ClasificacionInstrumento = (record.isMapped("CLASIFICACION")) ? record.get("CLASIFICACION") : "";
            	posicion.AsociadoCorelativo = LoaderUtils.analizarNumero( (record.isMapped("ASOCIADOCORRELATIVO") && record.get("ASOCIADOCORRELATIVO")!=null) ? record.get("ASOCIADOCORRELATIVO") : "0").longValue();
            	if(record.isMapped("ASOCIADOCORRELATIVO"))
            		posicion.TipoOperacion = ((posicion.TipoOperacion.equals("COMPRA")) ? "COMR" : "VENR") + posicion.ClasificacionInstrumento;
            	//POSICION OK
            	//GARANTIAS OK
            	
            	posiciones.add(posicion);
            }
            
        } catch (IOException | ParseException e) {
			e.printStackTrace();
		}    
		return posiciones;
	}

}
