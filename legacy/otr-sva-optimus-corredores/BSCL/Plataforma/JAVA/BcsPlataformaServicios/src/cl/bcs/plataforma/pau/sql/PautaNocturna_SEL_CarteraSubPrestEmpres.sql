/********************************************************** 
Proposito	: Script recuperar carteras subyacentes de simultaneas para pauta nocturna
autor		: Gat-Blac
fecha		: 20/02/2024
***********************************************************/

SELECT
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL,
	dt."IdSubyacente"	as "IdTablaOrigen",
	'SUBYACENTE' 		as "TablaOrigen",
	'PENDIENTE' 		as "EstadoProcesamiento",
	dt."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento",
	-- RV/FM
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."FechaOperacionRelacionada"			else null END as "PQ_FechaOperacion",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'S'									else null end as "PQ_ValorizaACompra",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'S'									else null end as "PQ_ValorizaAMercado",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdClasificacionTipoInstrumento" 	else null end as "PQ_IdClasificacionTipoInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdTipoInstrumento" 				else null end as "PQ_IdTipoInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdInstrumento" 					else null end as "PQ_IdInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."Nemotecnico" 						else null end as "PQ_Nemotecnico",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdMonedaInstrumento" 				else null end as "PQ_IdMoneda",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGCantidad" 						else null end as "PQ_Cantidad",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGPrecioSubyacente" 				else null end as "PQ_Precio",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."Tamano" 							else null end as "PQ_Tamano",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'SUBYACENTE' 							else null end as "PQ_UsoPrecio",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null									else null end as "PQ_TipoCartera", --se calcula en valorizarCustodia
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'NORMATIVO' 							else null end as "PQ_IdTipoCriterioTasa",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."FechaVencimientoInstrumento" 		else null end as "PQ_FechaVencimientoOperacion",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGTasaSubyacente" 				else null end as "PQ_Tasa",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."PlazoEnDiasOperacionRelacionada" 	else null end as "PQ_Plazo",
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_PlazoResidual", 	-- Consultar donde sacar!
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_Base",				-- Consultar donde sacar!
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdClienteOperacionRelacionada" 	else null end as "PQ_IdCliente",		
	case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdPortafolioOperacionRelacionada"	else null end as "PQ_IdPortafolio",	
	-- RF/IF
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaOperacionRelacionada"			else null end as "WS_FechaOperacion",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'S'					 				else null end as "WS_ValorizaACompra",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'S'									else null end as "WS_ValorizaAMercado",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then coalesce(dt."ValorizaPor",'T')			else null end as "WS_ValorizaPor",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdClasificacionTipoInstrumento"	else null end as "WS_IdClasificacionTipoInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdTipoInstrumento" 				else null end as "WS_IdTipoInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdInstrumento" 					else null end as "WS_IdInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."Nemotecnico"	 					else null end as "WS_Nemotecnico",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdEmisor" 							else null end as "WS_IdEmisor",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaEmisionInstrumento"			else null end as "WS_FechaEmision",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaVencimientoInstrumento" 		else null end as "WS_FechaVencimiento",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdTipoReajuste" 					else null end as "WS_IdTipoReajuste",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGCantidad" 						else null end as "WS_Cantidad",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGValorRescate" 					else null end as "WS_ValorRescate",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."PrecioCompraOperacionRelacionada"	else null end as "WS_PrecioCompra", --RELAC
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TasaCompraOperacionRelacionada"	else null end as "WS_TasaCompra", 	--RELAC
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGPrecioSubyacente"				else null end as "WS_PrecioSubyacente",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGTasaSubyacente"					else null end as "WS_TasaSubyacente",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_PrecioGarantia",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TasaGarantia",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TIFTasa" 							else null end as "WS_TasaTIF",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'SUBYACENTE' 							else null end as "WS_UsoPrecio",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TipoCartera",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'NORMATIVO'		 					else null end as "WS_IdTipoCriterioTasa",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TipoValorizacion" 					else null end as "WS_TipoValorizacion",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TieneCorteDeCupon"					else null end as "WS_TieneCorteDeCupon",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdClienteOperacionRelacionada" 	else null end as "WS_IdCliente",		
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdPortafolioOperacionRelacionada"	else null end as "WS_IdPortafolio",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdMonedaInstrumento" 					 else null end as "WS_IdMonedaInstrumento",
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoCompraActualMonedaInstrumento"	 else null end as "WS_BK_INGMontoCompraActualMonedaInstrumento",	
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoATasaCompraActual"				 else null end as "WS_BK_INGMontoATasaCompraActual",	
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoMercadoActualMonedaInstrumento" else null end as "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
	case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoATasaMercadoActual"			 else null end as "WS_BK_INGMontoATasaMercadoActual"	
FROM (SELECT SUB.*, CAR."IdCliente" AS "IdClienteOperacionRelacionada",CAR."IdPortafolio" AS  "IdPortafolioOperacionRelacionada"
	,SUB."INGFechaOperacion" AS  "FechaOperacionRelacionada",CAR."PlazoEnDiasOperacion" AS "PlazoEnDiasOperacionRelacionada", CAR."IdEstado" AS "IdEstadoCartera"
	,CAR."IdBoveda" AS "IdBovedaOperacionRelacionada",CAR."IdEmpresa" AS"IdEmpresaOperacionRelacionada", CAR."IdCliente", CAR."IdPortafolio", car."IdTipoInstrumento" AS "InstrumentoCartera"
from "BSCL.Custodia.BD::CUS.Subyacente" AS SUB
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND CAR."IdEstado" = 'VIG')) AS dt
where dt."IdEmpresaOperacionRelacionada" = :IdEmpresa and dt."IdEstado" = 'VIG' AND dt."IdClasificacionTipoInstrumento" IN ('RV', 'RF')
AND dt."IdCliente" = case when ifnull(:IdCliente,0)=0 then dt."IdCliente" else :IdCliente end
AND dt."IdPortafolio" = CASE WHEN IFNULL(:IdPortafolio,0)=0 THEN dt."IdPortafolio" ELSE :IdPortafolio END
AND dt."FolioCarteraRelacionada" = CASE WHEN IFNULL(:FolioCarteraRelacionada,0)=0 THEN dt."FolioCarteraRelacionada" ELSE :FolioCarteraRelacionada END
AND dt."InstrumentoCartera" in ('PRT', 'EMP', 'PRF', 'ERF');


