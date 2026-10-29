/********************************************************** 
Proposito	COALESCE(: Script para la selección de instrumentos de la tabla subyacente a valorizar
autor		COALESCE(: NRM
fecha		COALESCE(: 16/05/2017
***********************************************************/

SELECT
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdGenerarMovimientoCustodia",
	dt."IdSubyacente" AS "IdCustodia",
	dt."FolioSubyacente" AS "FolioCustodia",
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
	0  AS "MontoCompraMonedaSistema",		
	
	
	dt."INGPrecioSubyacente" as "PrecioSubyacente", 
	dt."INGTasaSubyacente" AS "TasaSubyacente", 
	0.0 AS "PrecioGarantia", 
	0.0 AS "TasaGarantia"
	
FROM (
	SELECT   SUB."IdSubyacente",SUB."FolioSubyacente",CAR."FolioCartera",CAR."IdCliente",CAR."IdPortafolio"
			,CAR."FechaOperacion"
			,CAR."PlazoEnDiasOperacion"
			,CAR."IdBoveda"
			,CAR."IdEmpresa"
			,CAR."FechaVencimientoOperacion"		
			,SUB."IdClasificacionTipoInstrumento",SUB."IdTipoInstrumento",SUB."IdInstrumento" ,SUB."Nemotecnico" , SUB."IdEmisor", SUB."EsFungible"
			,SUB."FechaVencimientoInstrumento",SUB."FechaEmisionInstrumento",SUB."TieneCorteDeCupon",SUB."IdMonedaInstrumento",SUB."IdEstado",SUB."IdMonedaEmision"
			,SUB."IdTipoReajuste" AS "IdTipoReajusteInstrumento"
			,SUB."INGCantidad",SUB."INGValorRescate",CAR."INGPrecioCompra",CAR."INGTasaCompra",SUB."INGPrecioSubyacente",SUB."INGTasaSubyacente"
			,SUB."INGMontoCompraActualMonedaInstrumento",SUB."INGMontoSubyacenteActualMonedaInstrumento"
	FROM "BSCL.Custodia.BD::CUS.Subyacente" AS SUB
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (SUB."FolioCarteraRelacionada" = CAR."FolioCartera")
) AS dt	
join "BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Carteras" as c on 
	(dt."FolioSubyacente" 		= coalesce(c."FolioSubyacente",dt."FolioSubyacente") and
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
