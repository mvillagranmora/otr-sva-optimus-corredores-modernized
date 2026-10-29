package cl.bcs.loaderapp.concrete.posiciones.garantia;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.posiciones.Garantia;

public class GarantiaFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {

		List<Object> garantias = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

            for (CSVRecord record : records) {
            	Garantia garantia = new Garantia();
            	garantia.setFolioGarantia(record.get("FolioGarantia"));
            	garantia.setFolioCarteraOrigen(record.get("FolioCarteraOrigen"));
            	garantia.setIdCarteraOrigen(record.get("IdCarteraOrigen"));
            	garantia.setFolioCarteraGarantizada(record.get("FolioCarteraGarantizada"));
            	garantia.setNemotecnicoCarteraGarantizada(record.get("NemotecnicoCarteraGarantizada"));
            	garantia.setEsEspecifica(record.get("EsEspecifica"));
            	garantia.setTipoPersonaContraparteGarantia(record.get("TipoPersonaContraparteGarantia"));
            	garantia.setTipoDocuementoIdentidadContraparteGarantia(record.get("TipoDocuementoIdentidadContraparteGarantia"));
            	garantia.setDocumentoIdentidadContraparteGarantia(record.get("DocumentoIdentidadContraparteGarantia"));
            	garantia.setCuentaContraparteGarantia(record.get("CuentaContraparteGarantia"));
            	garantia.setIdOrigenGarantia(record.get("IdOrigenGarantia"));
            	garantia.setIdEspecieGarantia(record.get("IdEspecieGarantia"));
            	garantia.setFechaIngresoRegistro(record.get("FechaIngresoRegistro"));
            	garantia.setFechaModificacionRegistro(record.get("FechaModificacionRegistro"));
            	garantia.setPrecioCompraOperacionOrigen(record.get("PrecioCompraOperacionOrigen"));
            	garantia.setTasaCompraOperacionOrigen(record.get("TasaCompraOperacionOrigen"));
            	garantia.setIdEmpresa(record.get("IdEmpresa"));
            	garantia.setFechaOperacion(record.get("FechaOperacion"));
            	garantia.setFechaVencimientoOperacion(record.get("FechaVencimientoOperacion"));
            	garantia.setPlazoEnDiasOperacion(record.get("PlazoEnDiasOperacion"));
            	garantia.setTipoPersona(record.get("TipoPersona"));
            	garantia.setTipoDocuementoIdentidad(record.get("TipoDocuementoIdentidad"));
            	garantia.setDocumentoIdentidadCliente(record.get("DocumentoIdentidadCliente"));
            	garantia.setCodigoPortafolio(record.get("CodigoPortafolio"));
            	garantia.setIdCuentaCustodioPortafolio(record.get("IdCuentaCustodioPortafolio"));
            	garantia.setIdEstado(record.get("IdEstado"));
            	garantia.setIdTipoCanalIngreso(record.get("IdTipoCanalIngreso"));
            	garantia.setIdMonedaSistema(record.get("IdMonedaSistema"));
            	garantia.setIdMonedaAdicional(record.get("IdMonedaAdicional"));
            	garantia.setFechaFactorMonedaAdicional(record.get("FechaFactorMonedaAdicional"));
            	garantia.setValorFactorMonedaAdicional(record.get("ValorFactorMonedaAdicional"));
            	garantia.setIdConcepto(record.get("IdConcepto"));
            	garantia.setIdTipoComprobante(record.get("IdTipoComprobante"));
            	garantia.setFolioComprobante(record.get("FolioComprobante"));
            	garantia.setFechaComprobante(record.get("FechaComprobante"));
            	garantia.setFolioExternoReferencia(record.get("FolioExternoReferencia"));
            	garantia.setIdSucursal(record.get("IdSucursal"));
            	garantia.setCodigoAgente(record.get("CodigoAgente"));
            	garantia.setIdCondicionLiquidacion(record.get("IdCondicionLiquidacion"));
            	garantia.setFechaLiquidacion(record.get("FechaLiquidacion"));
            	garantia.setIdUsuario(record.get("IdUsuario"));
            	garantia.setIdMovimientoCarteraEnTransito(record.get("IdMovimientoCarteraEnTransito"));
            	garantia.setNemotecnicoAnterior(record.get("NemotecnicoAnterior"));
            	garantia.setNemotecnico(record.get("Nemotecnico"));
            	garantia.setIdEmisor(record.get("IdEmisor"));
            	garantia.setNemoSVS(record.get("NemoSVS"));
            	garantia.setIdTipoReajuste(record.get("IdTipoReajuste"));
            	garantia.setEsPagaderoEn(record.get("EsPagaderoEn"));
            	garantia.setFechaVencimientoInstrumento(record.get("FechaVencimientoInstrumento"));
            	garantia.setEsFungible(record.get("EsFungible"));
            	garantia.setCodigoIFRS(record.get("CodigoIFRS"));
            	garantia.setIdMonedaInstrumento(record.get("IdMonedaInstrumento"));
            	garantia.setTIFAfectoBeneficio(record.get("TIFAfectoBeneficio"));
            	garantia.setTIFTasa(record.get("TIFTasa"));
            	garantia.setISIN(record.get("ISIN"));
            	garantia.setFechaFactorMonedaInstrumento(record.get("FechaFactorMonedaInstrumento"));
            	garantia.setValorFactorMonedaInstrumento(record.get("ValorFactorMonedaInstrumento"));
            	garantia.setCantidad(record.get("Cantidad"));
            	garantia.setCorteMinimo(record.get("CorteMinimo"));
            	garantia.setValorRescate(record.get("ValorRescate"));
            	garantia.setPrecioGarantia(record.get("PrecioGarantia"));
            	garantia.setTasaGarantia(record.get("TasaGarantia"));
            	garantia.setMontoGarantia(record.get("MontoGarantia"));
            	garantia.setMontoGarantiaMonedaInstrumento(record.get("MontoGarantiaMonedaInstrumento"));
            	garantia.setMontoGarantiaMonedaAdicional(record.get("MontoGarantiaMonedaAdicional"));
            	garantia.setINGFechaOperacion(record.get("INGFechaOperacion"));
            	garantia.setINGIdCondicionLiquidacion(record.get("INGIdCondicionLiquidacion"));
            	garantia.setINGFechaLiquidacion(record.get("INGFechaLiquidacion"));
            	garantia.setINGCantidad(record.get("INGCantidad"));
            	garantia.setINGValorRescate(record.get("INGValorRescate"));
            	garantia.setINGPrecioGarantia(record.get("INGPrecioGarantia"));
            	garantia.setINGTasaGarantia(record.get("INGTasaGarantia"));
            	garantia.setINGMontoATasaGarantiaActual(record.get("INGMontoATasaGarantiaActual"));
            	garantia.setINGMontoGarantiaActualMonedaInstrumento(record.get("INGMontoGarantiaActualMonedaInstrumento"));
            	garantia.setINGMontoGarantiaActualMonedaAdicional(record.get("INGMontoGarantiaActualMonedaAdicional"));
            	garantia.setINGPrecioMercadoActual(record.get("INGPrecioMercadoActual"));
            	garantia.setINGTasaMercadoActual(record.get("INGTasaMercadoActual"));
            	garantia.setINGMontoATasaMercadoActual(record.get("INGMontoATasaMercadoActual"));
            	garantia.setINGMontoMercadoActualMonedaInstrumento(record.get("INGMontoMercadoActualMonedaInstrumento"));
            	garantia.setINGMontoMercadoActualMonedaAdicional(record.get("INGMontoMercadoActualMonedaAdicional"));
            	garantia.setINGMontoATasaCompraActual(record.get("INGMontoATasaCompraActual"));
            	garantia.setINGMontoCompraActualMonedaInstrumento(record.get("INGMontoCompraActualMonedaInstrumento"));
            	garantia.setINGMontoCompraActualMonedaAdicional(record.get("INGMontoCompraActualMonedaAdicional"));
            	garantia.setINGTasaCorredora(record.get("INGTasaCorredora"));
            	garantia.setINGFechaTasaCorredora(record.get("INGFechaTasaCorredora"));
            	garantia.setINGMontoATasaCorredora(record.get("INGMontoATasaCorredora"));
            	garantia.setINGMontoATasaCorredoraMonedaInstrumento(record.get("INGMontoATasaCorredoraMonedaInstrumento"));
            	garantia.setINGMontoATasaCorredoraMonedaAdicional(record.get("INGMontoATasaCorredoraMonedaAdicional"));
            	garantia.setINGFechaCorteCupon(record.get("INGFechaCorteCupon"));
            	garantia.setINGNumeroCupon(record.get("INGNumeroCupon"));
            	garantia.setINGIdMonedaPagoCupon(record.get("INGIdMonedaPagoCupon"));
            	garantia.setINGMontoCorteCupon(record.get("INGMontoCorteCupon"));
            	garantia.setINGFechaPagoCorteCupon(record.get("INGFechaPagoCorteCupon"));
            	garantia.setEGRFechaOperacion(record.get("EGRFechaOperacion"));
            	garantia.setEGRIdTipoOperacion(record.get("EGRIdTipoOperacion"));
            	garantia.setEGRIdOperacion(record.get("EGRIdOperacion"));
            	garantia.setEGRIdMovimientoCarteraEnTransito(record.get("EGRIdMovimientoCarteraEnTransito"));
            	garantia.setEGRIdTipoCanalIngreso(record.get("EGRIdTipoCanalIngreso"));
            	garantia.setEGRIdCondicionLiquidacion(record.get("EGRIdCondicionLiquidacion"));
            	garantia.setEGRFechaLiquidacion(record.get("EGRFechaLiquidacion"));
            	garantia.setEGRIdTipoComprobante(record.get("EGRIdTipoComprobante"));
            	garantia.setEGRFolioComprobante(record.get("EGRFolioComprobante"));
            	garantia.setEGRFechaComprobante(record.get("EGRFechaComprobante"));
            	garantia.setEGRFolioExternoReferencia(record.get("EGRFolioExternoReferencia"));
            	garantia.setEGRIdConcepto(record.get("EGRIdConcepto"));
            	garantia.setEGRIdSucursal(record.get("EGRIdSucursal"));
            	garantia.setEGRCodigoAgente(record.get("EGRCodigoAgente"));
            	garantia.setEGRCantidad(record.get("EGRCantidad"));
            	garantia.setEGRValorRescate(record.get("EGRValorRescate"));
            	garantia.setEGRPrecio(record.get("EGRPrecio"));
            	garantia.setEGRTasa(record.get("EGRTasa"));
            	garantia.setEGRMonto(record.get("EGRMonto"));
            	garantia.setEGRMontoMonedaInstrumento(record.get("EGRMontoMonedaInstrumento"));
            	garantia.setEGRMontoMonedaAdicional(record.get("EGRMontoMonedaAdicional"));
            	garantias.add(garantia);
            }
            
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return garantias;
	}

}
