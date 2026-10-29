/********************************************************** 
Proposito	: Script para obtener las carteras para vencer
autor		: JG
fecha		: 01/02/2017
***********************************************************/
SELECT DISTINCT
	c."IdCartera",
	c."FolioPrestamo" AS "FolioCartera",
	c."NemoSubyacente" AS "Nemotecnico",
	c."IdMonedaInstrumento" AS "EsPagaderoEn", --VALIDARS
	c."IdClasificacionTipoInstrumento",
	c."IdTipoInstrumento",	
	c."IdInstrumento",
	c."INGMontoTotalPrestamo" AS "INGMontoATasaCompraActual",
	c."INGMontoTotalPrestamo" AS "INGMontoCompraActualMonedaInstrumento",
	c."IdPortafolio",
	c."IdMonedaInstrumento",
	c."IdTipoProducto", 
	'VENPRT' AS "IdTipoOperacion",
	COALESCE(ip."ValorIndicador",'N') as "OperaContraCuentaInversion", 
	s."IdPersona",
	s."IdCliente",
	'VENPRT' AS "IdConcepto",
	'FEE' AS "IdTipoComprobante",
	c."IdBolsa",
	--
	c."MontoPrimaOriginal" AS "PrecioAPlazo",
	c."INGMontoTotalPrestamo" AS "MontoAPlazo",
	c."INGFechaOperacion" AS "FechaComprobante", --validar
	c."IdMonedaSistema", --se usa como IdMonedaOperacion
	c."IdCondicionLiquidacion", -- se esta usando T0
	c."INGCantidad" AS  "INGCantidad", --usado como Cantidad?
	c."INGValorRescate" AS  "INGValorRescate",
	c."ValorRescate",
	c."INGTasaPrestamo" AS  "INGTasaCompra", --es la tasa
	c."INGMontoPrima" AS  "PrecioCompra",
	c."INGMontoPrima" AS  "INGPrecioCompra",
	'' AS "AccionOperacion", --lo usa la simultanea
	c."INGMontoTotalPrestamo" AS "INGMontoAPlazo",
	TipPre."CobraPrimaAlInicio",
	c."INGMontoPrima",   -- Monto prima de Prestamos(se usa para enviar a facturar)
	c."CantidadAsignada",  --Para saber cuantos titulos debe devolver
	c."CantidadPorAsignar", --Para saber si el prestamo esta asignado total o parcial
	c."IdInstrumentoSubyacente", 
	c."IdTipoPrestamo",
	c."FechaVencimientoPrestamo",
	c."FechaLiquidacion" AS "FechaLiquidacionDate",
	c."INGPrecioInstrumento",
	c."INGTasaInstrumento"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" c
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = c."IdPortafolio" and ip."CodigoTipoIndicador" = 'GEN_OCCINV'
LEFT JOIN  "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TipPre ON (TipPre."IdTipoPrestamo" = c."IdTipoPrestamo")
WHERE c."IdEstado" = 'VIG' 
AND   c."IdEmpresa" = :p_IdEmpresa
AND   c."FolioPrestamo" IN (:p_FolioPrestamo)
