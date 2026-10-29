/********************************************************** 
Proposito	COALESCE(: Script para la selección de instrumentos de la tabla cartera a valorizar
autor		COALESCE(: NRM
fecha		COALESCE(: 27/10/2017
***********************************************************/


SELECT 
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdGenerarMovimientoCustodia",
	dt."IdCartera" AS "IdCustodia",
	dt."FolioCartera" AS "FolioCustodia",
	dt."IdCliente",
	dt."IdPortafolio",
	
	dt."FechaOperacion",
	dt."FechaLiquidacion",
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
	dt."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
	dt."IdTipoReajuste" AS "IdTipoReajusteInstrumento",
	dt."IdEmisor" AS "Emisor",
	
	dt."INGCantidad"  AS "Cantidad",
	dt."INGValorRescate" AS "ValorRescate",
	dt."INGMontoCompraActualMonedaInstrumento"  AS "MontoCompra",
	dt."INGMontoATasaCompraActual"	AS "MontoCompraMonedaSistema",	
		
	dt."INGPrecioCompra" AS "PrecioCompra", 
	dt."INGTasaCompra" AS "TasaCompra",	
	null as "PrecioSubyacente", 
	null AS "TasaSubyacente", 
	null AS "PrecioGarantia", 
	null AS "TasaGarantia"	
FROM "BSCL.Custodia.BD::CUS.Cartera" dt
JOIN "BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Carteras" as c on 
	(dt."FolioCartera" 						= coalesce(c."FolioCartera",dt."FolioCartera") and
	dt."IdCliente" 							= coalesce(c."IdCliente",dt."IdCliente") and
	dt."IdPortafolio" 						= coalesce(c."IdPortafolio",dt."IdPortafolio"))
JOIN "BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Instrumentos" as ins on 
	(dt."IdClasificacionTipoInstrumento" 	= coalesce(ins."IdClasificacionTipoInstrumento",dt."IdClasificacionTipoInstrumento") and
	dt."IdTipoInstrumento" 					= coalesce(ins."IdTipoInstrumento",dt."IdTipoInstrumento") and
	dt."IdInstrumento"						= coalesce(ins."IdInstrumento",dt."IdInstrumento") and
	dt."IdEmisor" 							= coalesce(ins."IdEmisor",dt."IdEmisor") and
	dt."FechaVencimientoInstrumento" 		= coalesce(ins."FechaVencimiento",dt."FechaVencimientoInstrumento") and
	dt."IdTipoReajuste" 					= coalesce(ins."IdTipoReajuste",dt."IdTipoReajuste") and 
	dt."EsFungible" 						= coalesce(ins."EsFungible",dt."EsFungible") and 
	dt."IdBoveda" 							= coalesce(ins."IdBoveda",dt."IdBoveda"))
WHERE dt."IdEmpresa" = :p_IdEmpresa 
AND dt."IdEstado" = 'VIG' 
AND dt."IdClasificacionTipoInstrumento" in ('RV','RF','IF','RE','FM','DV');
