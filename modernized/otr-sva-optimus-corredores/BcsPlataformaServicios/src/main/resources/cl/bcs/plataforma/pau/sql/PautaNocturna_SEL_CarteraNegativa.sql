/********************************************************** 
Proposito	: Script recuperar carteras negativas para pauta nocturna
autor		: Gat-Blac
fecha		: 12/09/2023
***********************************************************/


SELECT 
"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdProcesamientoValorizaCustodiaMasiva",
CR."IdCarteraNegativa" 		as "IdTablaOrigen",
'NEGATIVA' 			as "TablaOrigen",
'PENDIENTE' 		as "EstadoProcesamiento",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then cr."FechaOperacion"  					else null end as "PQ_FechaOperacion",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then 'S'									else null end as "PQ_ValorizaACompra",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then TI."IdClasificacionTipoInstrumento"  	else null end as "PQ_IdClasificacionTipoInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then TI."IdTipoInstrumento" 				else null end as "PQ_IdTipoInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."IdInstrumento" 					else null end as "PQ_IdInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."Nemotecnico"  						else null end as "PQ_Nemotecnico",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."IdMonedaInstrumento"  				else null end as "PQ_IdMoneda",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."Cantidad" 							else null end as "PQ_Cantidad",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."PrecioOperacion"  					else null end as "PQ_Precio",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then cast(0 as BIGINT)     					else null end as "PQ_Tamano",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then 'CARTERA' 								else null end as "PQ_UsoPrecio",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then null									else null end as "PQ_TipoCartera", --se calcula en valorizarCustodia
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then 'NORMATIVO' 							else null end as "PQ_IdTipoCriterioTasa",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	THEN I."FechaVencimiento"  					else null end as "PQ_FechaVencimientoOperacion",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then cr."TasaOperacion" 					else null end as "PQ_Tasa",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then cast(0 as BIGINT) 						else null end as "PQ_Plazo",
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_PlazoResidual", 	-- Consultar donde sacar!
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_Base",				-- Consultar donde sacar!
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."IdCliente" 						else null end as "PQ_IdCliente",		
case when TI."IdClasificacionTipoInstrumento" in('RV','FM')	then CR."IdPortafolio"						else null end as "PQ_IdPortafolio",		
-- RF/IF
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."FechaOperacion" 					else null end as "WS_FechaOperacion",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then 'S'					 				else null end as "WS_ValorizaACompra",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then 'T'									else null end as "WS_ValorizaPor",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then TI."IdClasificacionTipoInstrumento"	else null end as "WS_IdClasificacionTipoInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then TI."IdTipoInstrumento" 				else null end as "WS_IdTipoInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdInstrumento" 					else null end as "WS_IdInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."Nemotecnico"	 					else null end as "WS_Nemotecnico",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdEmisor"  						else null end as "WS_IdEmisor",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then i."FechaEmision" 						else null end as "WS_FechaEmision",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."FechaVencimientoOperacion"  		else null end as "WS_FechaVencimiento",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdTipoReajuste" 					else null end as "WS_IdTipoReajuste",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."Cantidad"  						else null end as "WS_Cantidad",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."ValorRescate" 						else null end as "WS_ValorRescate",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."PrecioOperacion" 					else null end as "WS_PrecioCompra",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."TasaOperacion" 					else null end as "WS_TasaCompra",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_PrecioSubyacente",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TasaSubyacente",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_PrecioGarantia",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TasaGarantia",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF') then cast(0 as DECIMAL(26,8))     							        else null end as "WS_TasaTIF",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then 'CARTERA' 								else null end as "WS_UsoPrecio",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TipoCartera",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then 'NORMATIVO'				 			else null end as "WS_IdTipoCriterioTasa",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then 'T' 									else null end as "WS_TipoValorizacion",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then NULL										else null end as "WS_TieneCorteDeCupon",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdCliente" 						else null end as "WS_IdCliente",		
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdPortafolio"						else null end as "WS_IdPortafolio",	
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."MontoOperacionMonedaInstrumento"	 else null end as "WS_BK_INGMontoCompraActualMonedaInstrumento",	
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."MontoOperacionMonedaInstrumento" 			 else null end as "WS_BK_INGMontoATasaCompraActual",	
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."MontoMercadoMonedaInstrumento"  else null end as "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."MontoMercadoMonedaInstrumento" 		 else null end as "WS_BK_INGMontoATasaMercadoActual",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdTipoReajuste"  					     else null end as "WS_IdTipoReajuste",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdMonedaInstrumento"  					 else null end as "WS_IdMonedaInstrumento",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."MontoOperacionMonedaInstrumento" 		 else null end as "WS_MontoCompra",
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then cast(0 as BIGINT) 	 			 						 else null end as "WS_PlazoEnDias", --PREGUNTAR
case when TI."IdClasificacionTipoInstrumento" in('RF','IF')	then CR."IdMonedaEmision" 		 				 else null end as "WS_IdMonedaEmision"
FROM  "BSCL.Custodia.BD::CUS.CarteraNegativa" CR
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS I ON I."IdInstrumento" = cr."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON TI."IdTipoInstrumento" = I."IdTipoInstrumento" 
where CR."IdEmpresa" = :IdEmpresa and CR."IdEstado" = 'VIG' and ti."IdClasificacionTipoInstrumento" in (:IdClasificacionTipoInstrumento)
AND CR."IdCliente" = case when ifnull(:IdCliente,0)=0 then CR."IdCliente" else :IdCliente end
AND CR."IdPortafolio" = case when ifnull(:IdPortafolio,0)=0 then CR."IdPortafolio" else :IdPortafolio end
AND ti."IdTipoInstrumento" NOT in ('CER','ZER','XER','PAC','SIM');
