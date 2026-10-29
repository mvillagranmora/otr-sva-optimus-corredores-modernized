/********************************************************** 
Proposito	: Obtener carteras de opciones
autor		: Gat-Blac
fecha		: 11/01/2018
***********************************************************/

SELECT
	c."IdCartera",	
	'CARTERA' AS "TablaAfecta",
	c."IdTipoComprobante" AS "IdTipoComprobanteBusqueda",
	c."FolioComprobante" AS "FolioComprobanteBusqueda", --preguntar
	'VENCIMIENTO' as "AccionOperacion",
	c."IdCliente",
	c."IdPortafolio",
	c."FechaOperacion",
	c."IdTipoOperacion",
	c."IdConcepto",
	c."IdCondicionLiquidacion",
	c."FechaLiquidacion" AS "FechaLiquidacionDate",
	c."IdTipoCanalIngreso",
	c."IdTipoComprobante",
	c."FolioComprobante",
	c."IdUsuario",
	c."IdSucursal",
	c."IdAgente",
	c."Glosa",
	c."IdInstrumento",
	--c."EsPagaderoEn",
	c."Cantidad",
	c."CorteMinimo",
	c."ValorRescate",
	c."INGPrecioCompra" AS "PrecioCompra", 
	c."INGTasaCompra" AS "TasaCompra", 
	c."MontoCompraMonedaInstrumento",
	c."INGMontoCompraActualMonedaInstrumento"
from "BSCL.Custodia.BD::CUS.Cartera" c
where c."IdEstado" = 'VIG' and c."IdTipoInstrumento" = 'OPC' and
c."IdClasificacionTipoInstrumento" = 'DV' and
c."FechaVencimientoInstrumento" > '0001-01-01' and
c."FechaVencimientoInstrumento"<=:Fecha and c."IdEmpresa" = :IdEmpresa and
c."IdCarga"=0