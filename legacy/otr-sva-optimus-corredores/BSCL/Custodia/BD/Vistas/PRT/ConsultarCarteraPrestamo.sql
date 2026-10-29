SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarCarteraPrestamo";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarCarteraPrestamo"
AS 
SELECT
	sby."IdInstrumento" 		as "IdInstrumentoSubyacente",
	sby."IdEmisor" 				as "IdEmisorSubyacente",
	sby."Nemotecnico"			as "NemoSubyacente",
	sby."NemoSVS" 				as "NemoSVSSubyacente",
	sby."IdTipoInstrumento"		as "IdTipoInstrumentoSubyacente",
	sby."EsFungible" 			as "EsFungibleSubyacente",
--	sby."NombreTipoInstrumento" as "NombreTipoInstrumentoSubyacente",
	sby."IdClasificacionTipoInstrumento"		as "IdClasificacionTipoInstrumentoSubyacente",
	IFNULL(asig."CantidadAsignada",0) as "CantidadAsignada",
	IFNULL(asig."CantidadPorAsignar",0) as "CantidadPorAsignar",
	sby."IdMonedaInstrumento" as "IdMonedaSubyacente",
	sby."IdTipoReajusteSubyacente", 
	prt."IdBoveda" as "IdBovedaSubyacente",


	 prt."IdCartera",
	 prt."IdEmpresa",
	 prt."FolioCartera" as "FolioPrestamo",
	 prt."FolioCarteraRelacionada",
	 prt."IdEstado",
	 prt."IdMonedaSistema",
	 prt."IdMonedaInstrumento",
	 prt."IdOperacion",
	 prt."IdTipoOperacion",
	 TipOpe."Nombre" AS "NombreTipoOperacion",
	 prt."IdBoveda",
	 prt."IdTipoReajuste",
	 prt."IdTipoProducto",
	 prt."IdSucursal",
	 prt."IdAgente",
	 prt."IdBolsa",
	 prt."IdConcepto",
	 Concepto."Nombre" AS "NombreConcepto",
	 prt."IdCondicionLiquidacion",
	 prt."IdModulo",
	 prt."IdCliente",
	 Cli."DocumentoIdentidad",
	 prt."IdPortafolio",
	 Port."CodigoPortafolio",
	 Cli."RazonSocial",
	 prt."Nemotecnico" as "NemotecnicoPrestamo",
	 prt."NemoSVS",
	 prt."IdInstrumento" as "IdInstrumentoPrestamo", 
	 prt."IdClasificacionTipoInstrumento",
	 prt."IdTipoInstrumento",
	 prt."IdEmisor",
	 prt."FechaOperacion" as "FechaPrestamo",
	 prt."IdTipoComprobante",
	 prt."FolioComprobante",
	 prt."FechaComprobante",
	 prt."Cantidad",
	 prt."CorteMinimo",
	 prt."PrecioCompra" as "PrecioInstrumento",
	 prt."MontoCompra" as "MontoInstrumento",
	 prt."TasaCompra" as "TasaPrestamo",
	 prt."ValorRescate",
	 prt."FechaVencimientoOperacion" as "FechaVencimientoPrestamo",
	 prt."FechaEmisionInstrumento",
	 prt."FechaVencimientoInstrumento",
	 prt."PlazoEnDiasOperacion",
	 prt."EsFungible",
	 prt."CodigoIFRS",
	 prt."FechaIngresoRegistro",
	 prt."FechaModificacionRegistro",
	 prt."TasaAgente",
	 prt."FechaLiquidacion",
	 prt."IdTipoPrestamo",
	 tpres."Nemotecnico" as "NombreTipoPrestamo",
	 tpres."MinimoDiasAnticipo",
	 tpres."MaximoDiasPrestamo",
	 tpres."EsDiaHabilPlazoPrestamo",
	 tpres."EsDiaHabilParaCalculoInteres",
	 prt."UsaGarantiaEspecifica",
	 prt."FolioExternoReferencia" AS "FolioEntidad",
	 prt."CobraPrimaAlInicio",
	 prt."IdTipoCanalIngreso",
	 prt."INGGlosa",
	 prt."INGFechaOperacion",
	 prt."INGFechaLiquidacion",
	 prt."INGCantidad",
	 prt."INGValorRescate",
	 prt."INGPrecioAPlazo" 					as "INGMontoPrima",
	 prt."INGMontoAPlazo" 					as "INGMontoTotalPrestamo",
	 prt."INGPrecioCompra"		 			as "INGPrecioInstrumento",
	 prt."INGTasaCompra" 					as "INGTasaPrestamo",
	 prt."INGMontoATasaCompraActual" 		as "INGMontoInstrumento",
	 prt."INGFechaPrecioTasaMercadoActual",
	 prt."INGPrecioMercadoActual" 			as "INGPrecioMercadoInstrumento",
	 prt."INGTasaMercadoActual",
	 prt."INGMontoATasaMercadoActual" 		as "INGMontoMercadoInstrumento",
	 prt."EGRCantidad",
	 prt."EGRFechaLiquidacion",
	 prt."EGRFechaOperacion",
	 prt."EGRIdTipoComprobante",
	 prt."EGRFolioComprobante",
	 prt."EGRFechaComprobante",
	 prt."INGPrecioPromedioCompra",
	 prt."INGMontoPrecioPromedioCompra",
	 prt."IdCuentaCustodioPortafolio"
FROM "BSCL.Custodia.BD::CUS.Cartera" as prt
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (prt."FolioCartera" = ultSub."FolioCarteraRelacionada")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente") 
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON (Port."IdPortafolio" = prt."IdPortafolio")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = prt."IdEmpresa" and Cli."IdCliente" = Port."IdCliente")
	LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamo" as asig on (asig."FolioCarteraPrestamo" = prt."FolioCartera")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = prt."IdTipoOperacion")
	LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" as tpres on (tpres."IdInstrumentoPrestamo" = prt."IdInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (prt."IdConcepto" = Concepto."IdConcepto")
WHERE prt."IdTipoInstrumento" IN ('PRT','PRF')
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarCarteraPrestamo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
GO