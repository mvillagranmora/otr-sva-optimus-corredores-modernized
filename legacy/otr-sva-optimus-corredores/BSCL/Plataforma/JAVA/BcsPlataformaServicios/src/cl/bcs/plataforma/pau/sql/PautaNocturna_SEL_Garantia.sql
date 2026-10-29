/********************************************************** 
Proposito	: Script para recuperar garantías para pauta nocturna
autor		: Gat-Blac
fecha		: 24/08/2017
***********************************************************/

select
"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL,
dt."IdGarantia"		as "IdTablaOrigen",
'GARANTIA' 			as "TablaOrigen",
'PENDIENTE' 		as "EstadoProcesamiento",
dt."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
-- RV
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."FechaOperacion" 					else null end as "PQ_FechaOperacion",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'S'									else null end as "PQ_ValorizaACompra",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'S'					 				else null end as "PQ_ValorizaAMercado",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdClasificacionTipoInstrumento" 	else null end as "PQ_IdClasificacionTipoInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdTipoInstrumento" 				else null end as "PQ_IdTipoInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdInstrumento" 					else null end as "PQ_IdInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."Nemotecnico" 						else null end as "PQ_Nemotecnico",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdMonedaInstrumento" 				else null end as "PQ_IdMoneda",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdMonedaEmision" 					else null end as "PQ_IdMonedaEmision",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGCantidad" 						else null end as "PQ_Cantidad",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGPrecioGarantia" 				else null end as "PQ_Precio",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."Tamano" 							else null end as "PQ_Tamano",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'GARANTIA' 							else null end as "PQ_UsoPrecio",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null									else null end as "PQ_TipoCartera", --se calcula en valorizarCustodia
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'NORMATIVO' 							else null end as "PQ_IdTipoCriterioTasa",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."FechaVencimientoInstrumento" 		else null end as "PQ_FechaVencimientoOperacion",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGTasaGarantia"	 				else null end as "PQ_Tasa",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."PlazoEnDiasOperacion" 				else null end as "PQ_Plazo",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_PlazoResidual", 	-- Consultar donde sacar!
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_Base",				-- Consultar donde sacar!
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdCliente" 						else null end as "PQ_IdCliente",		
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdPortafolio"						else null end as "PQ_IdPortafolio",					
-- RF/IF
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaOperacion" 					else null END as "WS_FechaOperacion",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'S'					 				else null end as "WS_ValorizaACompra",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'S'									else null end as "WS_ValorizaAMercado",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then coalesce(dt."ValorizaPor",'T')			else null end as "WS_ValorizaPor",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdClasificacionTipoInstrumento"	else null end as "WS_IdClasificacionTipoInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdTipoInstrumento" 				else null end as "WS_IdTipoInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdInstrumento" 					else null end as "WS_IdInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."Nemotecnico"	 					else null end as "WS_Nemotecnico",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdMonedaInstrumento" 				else null end as "WS_IdMoneda",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdMonedaEmision" 					else null end as "WS_IdMonedaEmision",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdEmisor" 							else null end as "WS_IdEmisor",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaEmisionInstrumento"			else null end as "WS_FechaEmision",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaVencimientoInstrumento" 		else null end as "WS_FechaVencimiento",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdTipoReajuste" 					else null end as "WS_IdTipoReajuste",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGCantidad" 						else null end as "WS_Cantidad",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGValorRescate" 					else null end as "WS_ValorRescate",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."PrecioCompraOperacionOrigen"		else null end as "WS_PrecioCompra", --RELAC
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TasaCompraOperacionOrigen"			else null end as "WS_TasaCompra", 	--RELAC
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null									else null end as "WS_PrecioSubyacente",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null									else null end as "WS_TasaSubyacente",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGPrecioGarantia"					else null end as "WS_PrecioGarantia",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGTasaGarantia" 					else null end as "WS_TasaGarantia",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TIFTasa" 							else null end as "WS_TasaTIF",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'GARANTIA' 							else null end as "WS_UsoPrecio",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null									else null end as "WS_TipoCartera",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'NORMATIVO'		 					else null end as "WS_IdTipoCriterioTasa",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TipoValorizacion" 					else null end as "WS_TipoValorizacion",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TieneCorteDeCupon"					else null end as "WS_TieneCorteDeCupon",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdCliente" 						else null end as "WS_IdCliente",		
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdPortafolio"						else null end as "WS_IdPortafolio",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoCompraActualMonedaInstrumento"	 else null end as "WS_BK_INGMontoCompraActualMonedaInstrumento",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoATasaCompraActual"				 else null end as "WS_BK_INGMontoATasaCompraActual",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoMercadoActualMonedaInstrumento" else null end as "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoATasaMercadoActual"			 else null end as "WS_BK_INGMontoATasaMercadoActual"	
from "BSCL.Custodia.BD::GTA.Garantia" AS dt
WHERE dt."IdClasificacionTipoInstrumento" in ('RV','RF','IF','FM')
AND dt."IdEstado" = 'VIG'
AND dt."IdCliente" = case when ifnull(:IdCliente,0)=0 then dt."IdCliente" else :IdCliente end
AND dt."IdPortafolio" = case when ifnull(:IdPortafolio,0)=0 then dt."IdPortafolio" else :IdPortafolio end 
AND dt."IdEmpresa" = :IdEmpresa;
