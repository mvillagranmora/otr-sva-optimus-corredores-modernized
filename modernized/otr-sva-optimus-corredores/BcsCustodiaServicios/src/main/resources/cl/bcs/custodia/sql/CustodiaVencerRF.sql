/********************************************************** 
Proposito	: Script para obtener las carteras para vencer
autor		: JG
fecha		: 01/02/2017
***********************************************************/

select
	c."IdCartera",
	c."FolioCartera",
	c."Nemotecnico",
	c."IdMonedaEmision" AS "EsPagaderoEn",
	c."IdClasificacionTipoInstrumento",
	c."IdTipoInstrumento",
	c."IdInstrumento",
	c."INGMontoATasaCompraActual",
	c."INGMontoCompraActualMonedaInstrumento",
	c."IdPortafolio",
	c."IdMonedaInstrumento",
	c."IdTipoProducto", 
	c."IdTipoOperacion", -- "VCDTIT"
	c."IdOperacion",
	coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion", 
	s."IdPersona",
	s."IdCliente",
	--
	c."PrecioAPlazo",
	c."MontoAPlazo",
	c."FechaComprobante",
	c."IdMonedaSistema", --se usa como IdMonedaOperacion
	c."IdCondicionLiquidacion", -- se está usando T0
	c."INGCantidad", --usado como Cantidad?
	c."INGValorRescate",
	c."ValorRescate",
	c."INGTasaCompra", --es la tasa
	c."PrecioCompra",
	c."INGPrecioCompra",
	'' as "AccionOperacion", --lo usa la simultanea
	c."INGMontoAPlazo",
	c."TieneCorteDeCupon"
	--* MSH
	,c."IdTipoReajuste"
	,c."IdMonedaEmision"
	,c."EsFungible"
	,c."FechaVencimientoInstrumento"
from "BSCL.Custodia.BD::CUS.Cartera" c
join "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
left join "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = c."IdPortafolio" and ip."CodigoTipoIndicador" = 'GEN_OCCINV'
where c."IdEstado" = 'VIG'  and c."IdTipoInstrumento" != 'PAC' and
c."IdClasificacionTipoInstrumento" = 'RF' and
c."FechaVencimientoInstrumento" > '0001-01-01' and
c."FechaVencimientoInstrumento"<=:Fecha and c."IdEmpresa" = :IdEmpresa
-- for update of c."IdEstado"
