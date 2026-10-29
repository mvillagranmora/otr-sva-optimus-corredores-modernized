/********************************************************** 
Proposito	: Script para la selección de instrumentos desde la tabla garantia a valorizar
autor		: NRM
fecha		: 20/05/2017
***********************************************************/

SELECT
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdGenerarMovimientoCustodia",
	dt."IdGarantia" AS "IdCustodia",
	dt."FolioGarantia" AS "FolioCustodia",
	dt."IdCliente",
	dt."IdPortafolio",
		
	dt."FechaOperacion",
	dt."FechaVencimientoOperacion",
	dt."PlazoEnDiasOperacion",				
	dt."IdClasificacionTipoInstrumento", 
	dt."IdTipoInstrumento", 
	dt."IdInstrumento", 
	dt."Nemotecnico", 
	dt."FechaVencimientoInstrumento",
	dt."FechaEmisionInstrumento",
	dt."TieneCorteDeCupon",
	dt."IdMonedaInstrumento",		
	dt."IdTipoReajusteInstrumento",
	dt."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
	dt."IdEmisor" AS "Emisor",
	
	dt."INGCantidad"  AS "Cantidad",
	dt."INGValorRescate" AS "ValorRescate",
	
	dt."INGPrecioCompra" AS "PrecioCompra", 
	dt."INGTasaCompra" AS "TasaCompra",		
	dt."INGMontoCompraActualMonedaInstrumento"  AS "MontoCompra",	
	dt."MontoCompraMonedaSistema",	
	0.0 as "PrecioSubyacente", 
	0.0 AS "TasaSubyacente", 
	dt."INGPrecioGarantia" AS "PrecioGarantia", 
	dt."INGTasaGarantia" AS "TasaGarantia"
	
FROM (
	SELECT DISTINCT GAR."IdGarantia",GAR."FolioGarantia",CAR."FolioCartera",CAR."IdCliente",CAR."IdPortafolio"
			,CAR."FechaOperacion"
			,CAR."PlazoEnDiasOperacion"
			,CAR."IdBoveda"
			,CAR."IdEmpresa"
			,CAR."FechaVencimientoOperacion"		
			,GAR."IdClasificacionTipoInstrumento",GAR."IdTipoInstrumento",GAR."IdInstrumento" ,GAR."Nemotecnico" , GAR."IdEmisor", GAR."EsFungible"
			,GAR."FechaVencimientoInstrumento",GAR."FechaEmisionInstrumento",GAR."TieneCorteDeCupon",GAR."IdMonedaInstrumento",GAR."IdEstado"
			,GAR."IdTipoReajuste" AS "IdTipoReajusteInstrumento",GAR."IdMonedaEmision"
			,GAR."INGCantidad",GAR."INGValorRescate",CAR."INGPrecioCompra",CAR."INGTasaCompra",GAR."INGPrecioGarantia",GAR."INGTasaGarantia"
			,GAR."INGMontoCompraActualMonedaInstrumento",GAR."INGMontoGarantiaActualMonedaInstrumento"
			,GAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
	FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (GAR."FolioCarteraOrigen" = CAR."FolioCartera")
) AS dt	
join "BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Carteras" as c on 
	(dt."FolioGarantia" 		= coalesce(c."FolioGarantia",dt."FolioGarantia") and
	dt."FolioCartera" 			= coalesce(c."FolioCartera",dt."FolioCartera") and
    dt."IdCliente" 				= coalesce(c."IdCliente",dt."IdCliente") and
	dt."IdPortafolio" 			= coalesce(c."IdPortafolio",dt."IdPortafolio")
)
join "BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Instrumentos" as ins on 
	(dt."IdClasificacionTipoInstrumento" 	= coalesce(ins."IdClasificacionTipoInstrumento",dt."IdClasificacionTipoInstrumento") and
	dt."IdTipoInstrumento" 					= coalesce(ins."IdTipoInstrumento",dt."IdTipoInstrumento") and
	dt."IdInstrumento" 						= coalesce(ins."IdInstrumento",dt."IdInstrumento") and
	dt."IdEmisor"							= coalesce(ins."IdEmisor",dt."IdEmisor") and
	dt."FechaVencimientoInstrumento" 		= coalesce(ins."FechaVencimiento",dt."FechaVencimientoInstrumento") and
	dt."IdTipoReajusteInstrumento" 			= coalesce(ins."IdTipoReajuste",dt."IdTipoReajusteInstrumento") and 
	dt."EsFungible" 						= coalesce(ins."EsFungible",dt."EsFungible") and
	dt."IdBoveda" 							= coalesce(ins."IdBoveda",dt."IdBoveda"))	
where dt."IdEmpresa"	= :p_IdEmpresa
and dt."IdEstado" 		= 'VIG' 
and dt."IdClasificacionTipoInstrumento" in ('RV','FM','RF','IF','RE');
