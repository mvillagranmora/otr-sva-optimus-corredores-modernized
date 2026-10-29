/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 18/12/2017
***********************************************************/

SELECT 
	"IdProcesoMovimientoCustodia",
	-----------------------------------------------------------
	"IdGenerarMovimientoCustodia","EstaCertificando","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
	"FolioOperacionRelacionada",
	--GARANTIAS
	"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
	"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
	--OPERACION
	"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso",
	"IdPortafolioIngreso","IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro",
	"IdCuentaCustodioPortafolioRetiro","IdTipoProducto","IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional",
	"ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante",
	"FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion",
	"FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario",
	"IdModulo","TipoCarteraCorredor",
	--FUERON CREADOS PARA FONDOS MUTUOS
	"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
	--INSTRUMENTO
	"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
	"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda",
	"IdBovedaEsIncluyente",
	--CANTIDADES Y MONTOS
	"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra",
	"MontoCompra","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo",
	"MontoAPlazoMonedaInstrumento","MontoAPlazoMonedaAdicional",
 	--INSTRUMENTO RELACIONADO
	"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado",
	"IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado",
	"FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
	"CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado","TamanoRelacionado",
 	--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
	"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado",
	"CorteMinimoInstrumentoRelacionado","ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado",
	"TasaCompraInstrumentoRelacionado","MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado",
	"MontoCompraMonedaAdicionalRelacionado","PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado",
	"MontoAPlazoMonedaInstrumentoRelacionado","MontoAPlazoMonedaAdicionalRelacionado",
	--SE UTILIZA EN OSA, LO INGRESADO EN CARTERA PASA A ESTAR EN GARANTIA
	"MueveGarantiaAutomatica",
	--RF Y FORWARD
	"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
	"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
	--FORWARD
	"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
	"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
	"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
	"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador",
	"EsPuntos","TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo",
	"MontoARecibirAnticipo","TipoCambioObservado",
	--Necesarios para la carga desde una fuente externa, por ejemplo un archivo
	"IdCarga","IdSecuenciaCarteraMigracion","CodigoPeriodo"
FROM "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaDatosEntrada"
WHERE "IdProcesoMovimientoCustodia"	= :IdProcesoMovimientoCustodia	
AND   "IdGenerarMovimientoCustodia" = :IdGenerarMovimientoCustodia;