/********************************************************** 
Proposito	: Script recuperar carteras para pauta nocturna
autor		: Gat-Blac
fecha		: 25/05/2020
***********************************************************/


select
"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdProcesamientoValorizaCustodiaMasiva",
dt."IdCartera"		as "IdTablaOrigen",
'CARTERA' 			as "TablaOrigen",
'PENDIENTE' 		as "EstadoProcesamiento",
dt."FechaOperacion" 					as "PQ_FechaOperacion",
'S'									as "PQ_ValorizaACompra",
dt."IdClasificacionTipoInstrumento" 	as "PQ_IdClasificacionTipoInstrumento",
dt."IdTipoInstrumento" 				as "PQ_IdTipoInstrumento",
dt."IdInstrumento" 					as "PQ_IdInstrumento",
dt."Nemotecnico" 						as "PQ_Nemotecnico",
dt."IdMonedaInstrumento" 				as "PQ_IdMoneda",
dt."INGCantidad"						as "PQ_Cantidad",
dt."INGPrecioCompra" 					as "PQ_Precio",
dt."Tamano" 							as "PQ_Tamano",
'CARTERA' 								as "PQ_UsoPrecio",
null									as "PQ_TipoCartera", --se calcula en valorizarCustodia
'NORMATIVO' 							as "PQ_IdTipoCriterioTasa",
dt."FechaVencimientoInstrumento" 		as "PQ_FechaVencimientoOperacion",
dt."INGTasaCompra"						as "PQ_Tasa",
dt."PlazoEnDiasOperacion" 				as "PQ_Plazo",
null 									as "PQ_PlazoResidual", 	-- Consultar donde sacar!
null 									as "PQ_Base",				-- Consultar donde sacar!
dt."IdCliente" 						as "PQ_IdCliente",		
dt."IdPortafolio"						as "PQ_IdPortafolio",		

dt."FechaOperacion" 					as "WS_FechaOperacion",
'S'					 				as "WS_ValorizaACompra",
coalesce(dt."ValorizaPor",'T')			as "WS_ValorizaPor",
dt."IdClasificacionTipoInstrumento"	as "WS_IdClasificacionTipoInstrumento",
dt."IdTipoInstrumento" 				as "WS_IdTipoInstrumento",
dt."IdInstrumento" 					as "WS_IdInstrumento",
dt."Nemotecnico"	 					as "WS_Nemotecnico",
dt."IdEmisor" 							as "WS_IdEmisor",
dt."FechaEmisionInstrumento"			as "WS_FechaEmision",
dt."FechaVencimientoInstrumento" 		as "WS_FechaVencimiento",
dt."IdTipoReajuste" 					as "WS_IdTipoReajuste",
dt."INGCantidad" 						as "WS_Cantidad",
dt."INGValorRescate"					as "WS_ValorRescate",
dt."INGPrecioCompra"					as "WS_PrecioCompra",
dt."INGTasaCompra"						as "WS_TasaCompra",
null 									as "WS_PrecioSubyacente",
null 									as "WS_TasaSubyacente",
null 									as "WS_PrecioGarantia",
null 									as "WS_TasaGarantia",
dt."TIFTasa" 							as "WS_TasaTIF",
'CARTERA' 								as "WS_UsoPrecio",
null 									as "WS_TipoCartera",
'NORMATIVO'				 			as "WS_IdTipoCriterioTasa",
dt."TipoValorizacion" 					as "WS_TipoValorizacion",
dt."TieneCorteDeCupon"					as "WS_TieneCorteDeCupon",
dt."IdCliente" 						as "WS_IdCliente",		
dt."IdPortafolio"						as "WS_IdPortafolio",	
dt."INGMontoCompraActualMonedaInstrumento"	 as "WS_BK_INGMontoCompraActualMonedaInstrumento",	
dt."INGMontoATasaCompraActual"				 as "WS_BK_INGMontoATasaCompraActual",	
dt."INGMontoMercadoActualMonedaInstrumento" as "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
dt."INGMontoATasaMercadoActual"			 as "WS_BK_INGMontoATasaMercadoActual",
dt."IdTipoReajuste" 					     as "WS_IdTipoReajuste",
dt."IdMonedaInstrumento" 					 as "WS_IdMonedaInstrumento",
dt."MontoCompra"		 					 as "WS_MontoCompra",
dt."PlazoEnDiasOperacion"		 			 as "WS_PlazoEnDias",
dt."IdMonedaEmision"		 				 as "WS_IdMonedaEmision"


from "BSCL.Custodia.BD::CUS.Cartera" dt
where dt."IdEmpresa" = :IdEmpresa and dt."IdEstado" = 'VIG' and dt."IdClasificacionTipoInstrumento" in (:IdClasificacionTipoInstrumento)
AND dt."IdCliente" = case when ifnull(:IdCliente,0)=0 then dt."IdCliente" else :IdCliente end
AND dt."IdPortafolio" = case when ifnull(:IdPortafolio,0)=0 then dt."IdPortafolio" else :IdPortafolio end
AND dt."IdTipoInstrumento" in ('IOA', 'IOE', 'IOI', 'IOD','IOB', 'IOC', 'IOU', 'IOM', 'IOF');
