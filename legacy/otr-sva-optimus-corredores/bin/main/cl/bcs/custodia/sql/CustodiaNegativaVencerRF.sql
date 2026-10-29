

select
	c."IdCarteraNegativa",
	c."FolioCustodia",
	c."FolioComprobante",
	c."Nemotecnico",
	c."IdMonedaEmision" AS "EsPagaderoEn",
	tins."IdClasificacionTipoInstrumento",
	ins."IdTipoInstrumento",
	c."IdInstrumento", 
	--c."INGMontoATasaCompraActual",
	--c."INGMontoCompraActualMonedaInstrumento",
	c."IdPortafolio",
	c."IdMonedaInstrumento",
	c."IdTipoProducto", 
	c."IdTipoOperacion", -- "VCDTIT"
	c."IdOperacion",
	coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion", 
	s."IdPersona",
	s."IdCliente",
	--
	c."PrecioOperacion",
	c."MontoOperacionMonedaInstrumento",
	comp."FechaComprobante",
	c."IdMonedaInstrumento", --se usa como IdMonedaOperacion
	'T0' AS "IdCondicionLiquidacion", -- se está usando T0
	c."Cantidad", --usado como Cantidad?
	c."ValorRescate",
	c."ValorRescate",
	c."TasaOperacion", --es la tasa de la operacion
	c."PrecioOperacion",
	c."PrecioOperacion",
	'' as "AccionOperacion", --lo usa la simultanea
	c."MontoOperacionMonedaInstrumento",
	--c."TieneCorteDeCupon"
	CASE  WHEN c."MontoCorteCupon" > 0 THEN 'S'
		ELSE 'N' END AS "TieneCotroCupon"--* MSH
	,c."IdTipoReajuste"
	,c."IdMonedaEmision"
	,c."EsFungible"
	,ins."FechaVencimiento"
	,'S' as "esCarteraNegtiva"
from "BSCL.Custodia.BD::CUS.CarteraNegativa" c
JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON ins."IdInstrumento" =c."IdInstrumento" 
JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins ON tins."IdTipoInstrumento" = ins."IdTipoInstrumento" 
join "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" comp ON c."FolioComprobante" = comp."FolioComprobante" 
left join "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = c."IdPortafolio" and ip."CodigoTipoIndicador" = 'GEN_OCCINV'
where c."IdEstado" = 'VIG'  and ins."IdTipoInstrumento" != 'PAC' and
tins."IdClasificacionTipoInstrumento" = 'RF' and
ins."FechaVencimiento" > '0001-01-01' and
ins."FechaVencimiento"<=:Fecha and c."IdEmpresa" = :IdEmpresa

