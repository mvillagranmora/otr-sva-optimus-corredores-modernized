SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
AS 
SELECT
	sby."IdInstrumento" 					AS "IdInstrumentoSubyacente",
	sby."FolioSubyacente",
	sby."IdEmisor" 							AS "IdEmisorSubyacente",
	sby."Nemotecnico"						AS "NemoSubyacente",
	sby."NemoSVS" 							AS "NemoSVSSubyacente",
	sby."IdTipoInstrumento"					AS "IdTipoInstrumentoSubyacente",
	sbyi."Nombre" 							AS "NombreTipoInstrumentoSubyacente",
	sby."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumentoSubyacente",
	IFNULL(asig."CantidadAsignada",0) 		AS "CantidadAsignada",
	IFNULL(asig."CantidadPorAsignar",0) 	AS "CantidadPorAsignar",
	sby."IdMonedaInstrumento" 				AS "IdMonedaSubyacente",
	sby."IdTipoReajusteSubyacente", 
	sby."IdSubyacente",
	sby."IdMonedaEmision" 					AS "IdMonedaEmisionSubyacente",
	Mercado."Nombre" 						AS "NombreMercado",
	
	Emisor."NombreCompleto" 				AS "NombreEmisorSubyacente",
	prt."IdBoveda" 							AS "IdBovedaSubyacente",
	prt."IdCartera",
	prt."IdEmpresa",
	prt."FolioCartera" 						AS "FolioPrestamo",
	prt."FolioCarteraRelacionada",
	prt."IdEstado",
	Estado."Nombre" 						AS "NombreEstado",
	prt."IdMonedaSistema",
	prt."IdMonedaInstrumento",
	prt."IdOperacion",
	prt."IdTipoOperacion",
	TipoOperacion."Nombre" 					AS "NombreTipoOperacion",
	prt."IdBoveda",
	prt."IdTipoReajuste",
	prt."IdTipoProducto",
	prt."IdSucursal",
	prt."IdAgente",
	prt."IdBolsa",
	Bolsa."Nombre" 							AS "NombreBolsa",
	prt."IdConcepto",
	Concepto."Nombre" 						AS "NombreConcepto",
	prt."IdCondicionLiquidacion",
	CondicionLiquidacion."Nombre" 			AS "NombreCondicionLiquidacion",
	prt."IdModulo",
	prt."IdCliente",
	Cli."DocumentoIdentidad",
	prt."IdPortafolio",
	Port."CodigoPortafolio",
	CONCAT(CONCAT(Port."CodigoPortafolio", ' - '), Port."Proposito") AS "DescripcionPortafolio",
	Cli."NombreCompleto" 					AS "RazonSocial",
	prt."Nemotecnico" 						AS "NemotecnicoPrestamo",
	prt."NemoSVS",
	prt."IdInstrumento" 					AS "IdInstrumentoPrestamo", 
	prt."IdClasificacionTipoInstrumento",
	prt."IdTipoInstrumento",
	TipoInstrumento."Nombre" 				AS "TipoInstrumento",
	prt."IdEmisor",
	prt."FechaOperacion" 					AS "FechaPrestamo",
	prt."IdTipoComprobante",
	prt."FolioComprobante",
	prt."FechaComprobante",
	prt."Cantidad",
	sby."CorteMinimo",
	prt."PrecioCompra" 						AS "PrecioInstrumento",
	prt."MontoCompra" 						AS "MontoInstrumento",
	prt."TasaCompra" 						AS "TasaPrestamo",
	prt."ValorRescate",
	prt."FechaVencimientoOperacion" 		AS "FechaVencimientoPrestamo",
	prt."FechaEmisionInstrumento",
	sby."FechaVencimientoInstrumento",
	prt."PlazoEnDiasOperacion",
	prt."EsFungible",
	sby."EsFungible" 						AS "EsFungibleSubyacente",
	prt."CodigoIFRS",
	prt."FechaIngresoRegistro",
	prt."FechaModificacionRegistro",
	prt."TasaAgente",
	prt."FechaLiquidacion",
	prt."IdTipoPrestamo",
	 CASE WHEN  tpres."Nemotecnico"  = 'FINES GENERALES' and  prt."TipoCustodia" = 'S'  THEN 'FINES GENERALES (CUSTODIA)' 
	 else  tpres."Nemotecnico"  END
	 										AS "NombreTipoPrestamo",
	tpres."MinimoDiasAnticipo",
	tpres."MaximoDiasPrestamo",
	tpres."EsDiaHabilPlazoPrestamo",
	tpres."EsDiaHabilParaCalculoInteres",
	prt."UsaGarantiaEspecifica",
	prt."FolioExternoReferencia" 			AS "FolioEntidad",
	prt."CobraPrimaAlInicio",
	prt."IdTipoCanalIngreso",
	prt."INGGlosa",
	prt."INGFechaOperacion",
	prt."INGValorRescate",
	prt."TasaCompra" 						AS "INGTasaPrestamo",
	prt."PrecioAPlazo" 						AS "MontoPrimaOriginal",
	prt."INGPrecioAPlazo" 					AS "INGMontoPrima",
	prt."INGMontoAPlazo" 					AS "INGMontoTotalPrestamo",
	prt."INGPrecioCompra"	 				AS "INGPrecioInstrumento",
	sby."INGTasaSubyacente"	 				AS "INGTasaInstrumento",
	prt."INGFechaLiquidacion" 				AS "INGFechaLiquidacionPrestamo",
	prt."INGMontoATasaCompraActual" 		AS "INGMontoInstrumento",
	prt."INGFechaPrecioTasaMercadoActual",
	prt."INGPrecioMercadoActual" 			AS "INGPrecioMercadoInstrumento",
	prt."INGTasaMercadoActual",
	prt."INGMontoATasaMercadoActual" 		AS "INGMontoMercadoInstrumento",
	prt."EGRCantidad" 						AS "EGRCantidadPrestamo",
	prt."EGRFechaLiquidacion"				AS "EGRFechaLiquidacionPrestamo",
	prt."EGRFechaOperacion",
	prt."INGFechaValorizacionAnterior",
	prt."INGFechaValorizacionActual",
	prt."ValorTipoCambioCierre",
	prt."INGPrecioMercadoAnterior",
	prt."INGMontoPrecioPromedioCompra",
	prt."INGMontoATasaMercadoAnterior",
	prt."INGMontoATasaMercadoProximo",
	prt."INGMontoATasaCompraAnterior",
	prt."INGMontoInteresCompraProximo",
	prt."INGMontoInteresCorridoCompraActual",
	isuby."TipoValorizacion",
	prt."IdInstrumento",
    prt."FolioSistemaExterno",
    prt."EstadoAnticipo",
    prt."ExigirGarantia",
    prt."EstadoLiquidacion",
    prt."TipoCustodia",
    CondicionLiquidacion."Codigo" AS "CodigoCondicionLiquidacion",
    tpres."MinimoDiasPrestamo",
	
	prt."INGCantidad",
    prt."INGMontoATasaCompraActual",
    prt."INGMontoAPlazo",
    prt."INGMontoCompraActualMonedaInstrumento",
    prt."INGMontoAPlazoMonedaInstrumento",
	CASE WHEN prt."PrecioSucio" = 0 THEN prt."PrecioCompra" ELSE prt."PrecioSucio" END AS "PrecioCompraInicial"
FROM "BSCL.Custodia.BD::CUS.Cartera" as prt
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" ultMov on ultMov."IdEmpresa" = prt."IdEmpresa" and ultMov."FolioCartera" = prt."FolioCartera" and prt."IdCartera" = ultMov."IdCartera"
INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" as tpres ON (prt."IdInstrumento" = tpres."IdInstrumentoPrestamo")
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (prt."FolioCartera" = ultSub."FolioCarteraRelacionada")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamo" as asig on asig."FolioCarteraPrestamo" = prt."FolioCartera"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as sbyi ON sbyi."IdTipoInstrumento" = sby."IdTipoInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = prt."IdCliente" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = prt."IdPortafolio" and Port."IdCliente" = prt."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON prt."IdEstado" = Estado."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON prt."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON prt."IdBolsa" = Bolsa."IdBolsa" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON prt."IdConcepto" = Concepto."IdConcepto" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON prt."EGRIdConcepto" = ConceptoEGR."IdConcepto" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON prt."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = prt."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as Mercado ON Mercado."IdClasificacionTipoInstrumento" = sby."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" as Emisor ON Emisor."IdEmisor" = sby."IdEmisor"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as isuby ON isuby."IdInstrumento" = sby."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" precios on precios."IdInstrumento" = isuby."IdInstrumento" and precios."Fecha" = prt."FechaOperacion"  and precios."IdBolsa" = prt."IdBolsa"
WHERE prt."IdTipoInstrumento" in ('PRT','PRF') AND prt."IdEstado" = 'VIG'
WITH READ ONLY;
GO 

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

