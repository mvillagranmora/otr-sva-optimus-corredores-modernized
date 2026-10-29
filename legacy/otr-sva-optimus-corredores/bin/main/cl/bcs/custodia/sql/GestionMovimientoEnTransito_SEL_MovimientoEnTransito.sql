/********************************************************** 
Proposito	: Script para la recuperar informacion en la tabla MovimientoCarteraEnTransito
autor		: NRM
fecha		: 16/05/2017
***********************************************************/

WITH
	MovimientosEnTransito AS (
		SELECT  
			"IdMovimientoCarteraEnTransito","IdMovimientoCarteraEnTransitoRelacionado","FechaIngresoRegistro","FechaCertificacionRegistro","FechaAnulacionRegistro","IdUsuarioIngreso",
			"IdUsuarioCertificacion","IdUsuarioAnulacion","FechaOperacion","FechaOperacionOriginal","FechaVencimientoOperacion","PlazoEnDiasOperacion","FolioCustodia","IdTipoComprobante","FolioComprobante",
			"EsRegistroGeneradoPorSistema","MueveCustodia","PermiteCertificacionPorUsuario","OrigenCustodia","IdCustodiaOrigen","IdTipoOperacionOrigen","FolioCarteraGarantizada",
			"IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia","CuentaContraparteGarantia","IdOrigenGarantia",
			"IdEspecieGarantia",MvtoCarTran."IdEmpresa","IdPais","EsNominal","FolioExternoReferencia",MvtoCarTran."IdCliente",MvtoCarTran."IdPortafolio","IdCuentaCustodioPortafolio","IdClienteRelacionado","IdPortafolioRelacionado",
			"IdCuentaCustodioPortafolioRelacionado",MvtoCarTran."IdEstado","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","CodigoIFRS",
			"IdConcepto","IdTipoOperacion","IdOperacion","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","Glosa",
			"IdUsuario","IdModulo","TipoCarteraCorredor","TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste","IdClasificacionTipoInstrumento",
			"IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","IdMonedaEmision","FechaEmisionInstrumento","FechaVencimientoInstrumento",
			"EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda","ISIN","FechaFactorMonedaInstrumento",
			"ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioAPlazo","PrecioSucio","PrecioCompra","TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional",
			MvtoCarTran."IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo","EsVentaCorta", "MontoAPlazo","MontoAPlazoMonedaInstrumento","IdTipoProducto","IdRegimenFondoMutuo","FechaOrigenCompra",
			COALESCE(ConsCli."NombreCompleto", ConsCli."RazonSocial") AS "NombreCliente", COALESCE(ConsPort."CodigoPortafolio", '') AS "CodigoPortafolio","IdMovimientoComprobante",ConsCli."DocumentoIdentidad",
			MvtoCarTran."TasaTIR"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MvtoCarTran
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsCli
		    ON ConsCli."IdCliente" = MvtoCarTran."IdCliente"
		  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConsPort
		    ON ConsPort."IdPortafolio" = MvtoCarTran."IdPortafolio"
		WHERE "IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito)
		UNION ALL 
		SELECT  
			"IdMovimientoCarteraEnTransito","IdMovimientoCarteraEnTransitoRelacionado","FechaIngresoRegistro","FechaCertificacionRegistro","FechaAnulacionRegistro","IdUsuarioIngreso",
			"IdUsuarioCertificacion","IdUsuarioAnulacion","FechaOperacion","FechaOperacionOriginal","FechaVencimientoOperacion","PlazoEnDiasOperacion","FolioCustodia","IdTipoComprobante","FolioComprobante",
			"EsRegistroGeneradoPorSistema","MueveCustodia","PermiteCertificacionPorUsuario","OrigenCustodia","IdCustodiaOrigen","IdTipoOperacionOrigen","FolioCarteraGarantizada",
			"IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia","CuentaContraparteGarantia","IdOrigenGarantia",
			"IdEspecieGarantia",MvtoCarTran."IdEmpresa","IdPais","EsNominal","FolioExternoReferencia",MvtoCarTran."IdCliente",MvtoCarTran."IdPortafolio","IdCuentaCustodioPortafolio","IdClienteRelacionado","IdPortafolioRelacionado",
			"IdCuentaCustodioPortafolioRelacionado",MvtoCarTran."IdEstado","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","CodigoIFRS",
			"IdConcepto","IdTipoOperacion","IdOperacion","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","Glosa",
			"IdUsuario","IdModulo","TipoCarteraCorredor","TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste","IdClasificacionTipoInstrumento",
			"IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","IdMonedaEmision","FechaEmisionInstrumento","FechaVencimientoInstrumento",
			"EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda","ISIN","FechaFactorMonedaInstrumento",
			"ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioAPlazo","PrecioSucio","PrecioCompra","TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional",
			MvtoCarTran."IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo","EsVentaCorta", "MontoAPlazo","MontoAPlazoMonedaInstrumento","IdTipoProducto","IdRegimenFondoMutuo","FechaOrigenCompra",
			COALESCE(ConsCli."NombreCompleto", ConsCli."RazonSocial") AS "NombreCliente", COALESCE(ConsPort."CodigoPortafolio", '') AS "CodigoPortafolio","IdMovimientoComprobante",ConsCli."DocumentoIdentidad",
			MvtoCarTran."TasaTIR"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MvtoCarTran
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsCli
		    ON ConsCli."IdCliente" = MvtoCarTran."IdCliente"
		  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConsPort
		    ON ConsPort."IdPortafolio" = MvtoCarTran."IdPortafolio"
		WHERE "IdMovimientoCarteraEnTransitoRelacionado" IN (:IdMovimientoCarteraEnTransito)
	)
SELECT DISTINCT 
	"IdMovimientoCarteraEnTransito","IdMovimientoCarteraEnTransitoRelacionado","FechaIngresoRegistro","FechaCertificacionRegistro","FechaAnulacionRegistro","IdUsuarioIngreso",
	"IdUsuarioCertificacion","IdUsuarioAnulacion","FechaOperacion","FechaOperacionOriginal","FechaVencimientoOperacion","PlazoEnDiasOperacion","FolioCustodia","IdTipoComprobante","FolioComprobante",
	"EsRegistroGeneradoPorSistema","MueveCustodia","PermiteCertificacionPorUsuario","OrigenCustodia","IdCustodiaOrigen","IdTipoOperacionOrigen","FolioCarteraGarantizada",
	"IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia","CuentaContraparteGarantia","IdOrigenGarantia",
	"IdEspecieGarantia","IdEmpresa","IdPais","EsNominal","FolioExternoReferencia","IdCliente","IdPortafolio","IdCuentaCustodioPortafolio","IdClienteRelacionado","IdPortafolioRelacionado",
	"IdCuentaCustodioPortafolioRelacionado","IdEstado","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","CodigoIFRS",
	"IdConcepto","IdTipoOperacion","IdOperacion","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","Glosa",
	"IdUsuario","IdModulo","TipoCarteraCorredor","TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste","IdClasificacionTipoInstrumento",
	"IdTipoInstrumento","IdInstrumento","IdInstrumentoAnterior","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","IdMonedaEmision","FechaEmisionInstrumento","FechaVencimientoInstrumento",
	"EsFungible","ValorizaPor","TipoValorizacion","TieneCorteDeCupon","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda","ISIN","FechaFactorMonedaInstrumento",
	"ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioAPlazo","PrecioSucio","PrecioCompra","TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional",
	"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo","EsVentaCorta", "MontoAPlazo","MontoAPlazoMonedaInstrumento","IdTipoProducto","IdRegimenFondoMutuo","FechaOrigenCompra", "NombreCliente", "CodigoPortafolio","DocumentoIdentidad",
	"TasaTIR"
FROM MovimientosEnTransito
ORDER BY "IdMovimientoCarteraEnTransito";
