/********************************************************** 
Proposito	: Script para obtener las carteras para vencer
autor		: JG
fecha		: 01/02/2017
***********************************************************/
select
	c."IdCartera",
	c."FolioPrestamo" as "FolioCartera",
	c."NemoSubyacente" as "Nemotecnico",
	c."IdMonedaInstrumento" as "EsPagaderoEn", --VALIDARS
	c."IdClasificacionTipoInstrumento",
	c."IdTipoInstrumento",	
	c."IdInstrumento",
	c."MontoInstrumento" as "INGMontoATasaCompraActual",
	c."MontoInstrumento" as "INGMontoCompraActualMonedaInstrumento",
	c."IdPortafolio",
	c."IdMonedaInstrumento",
	c."IdTipoProducto", 
	'VENPRT' AS "IdTipoOperacion",
	coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion", 
	s."IdPersona",
	s."IdCliente",
	'VENPRT' AS "IdConcepto",
	'FEE' AS "IdTipoComprobante",
	--
	c."PrecioInstrumento" as "PrecioAPlazo",
	c."MontoInstrumento"  as "MontoAPlazo",
	c."INGFechaOperacion" as "FechaComprobante", --validar
	c."IdMonedaSistema", --se usa como IdMonedaOperacion
	c."IdCondicionLiquidacion", -- se está usando T0
	c."INGCantidad" as "INGCantidad", --usado como Cantidad?
	c."INGValorRescate" as "INGValorRescate",
	c."ValorRescate",
	c."INGTasaPrestamo" as "INGTasaCompra", --es la tasa
	c."PrecioInstrumento" as "PrecioCompra",
	c."INGPrecioInstrumento" as "INGPrecioCompra",
	'' as "AccionOperacion", --lo usa la simultanea
	c."INGMontoTotalPrestamo" AS "INGMontoAPlazo",
	--c."CobraPrimaAlInicio", -- no se guarda bien en cartera
	TipPre."CobraPrimaAlInicio",
	c."INGMontoPrima"   -- Monto prima de Prestamos(se usa para enviar a facturar)
	
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" c
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = c."IdPortafolio" and ip."CodigoTipoIndicador" = 'GEN_OCCINV'
LEFT JOIN  "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TipPre ON (TipPre."IdTipoPrestamo" = c."IdTipoPrestamo")
WHERE c."IdEstado" = 'VIG' 
AND   c."IdEmpresa" = :IdEmpresa
AND   c."FechaVencimientoPrestamo" > '0001-01-01' and c."FechaVencimientoPrestamo"<=:Fecha
AND 	c."FolioPrestamo" = 20686394