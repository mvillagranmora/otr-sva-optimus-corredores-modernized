/********************************************************** 
Proposito	: Script recuperar carteras para pauta nocturna
autor		: Gat-Blac
fecha		: 22/08/2017
***********************************************************/


select  

"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdProcesamientoValorizaCustodiaMasiva",
dt."IdCartera"		as "IdTablaOrigen",
'CARTERA' 			as "TablaOrigen",
'PENDIENTE' 		as "EstadoProcesamiento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."FechaOperacion" 					else null end as "PQ_FechaOperacion",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'S'									else null end as "PQ_ValorizaACompra",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdClasificacionTipoInstrumento" 	else null end as "PQ_IdClasificacionTipoInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdTipoInstrumento" 				else null end as "PQ_IdTipoInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdInstrumento" 					else null end as "PQ_IdInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."Nemotecnico" 						else null end as "PQ_Nemotecnico",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdMonedaInstrumento" 				else null end as "PQ_IdMoneda",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGCantidad"						else null end as "PQ_Cantidad",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGPrecioCompra" 					else null end as "PQ_Precio",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."Tamano" 							else null end as "PQ_Tamano",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'CARTERA' 								else null end as "PQ_UsoPrecio",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null									else null end as "PQ_TipoCartera", --se calcula en valorizarCustodia
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then 'NORMATIVO' 							else null end as "PQ_IdTipoCriterioTasa",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."FechaVencimientoInstrumento" 		else null end as "PQ_FechaVencimientoOperacion",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."INGTasaCompra"						else null end as "PQ_Tasa",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."PlazoEnDiasOperacion" 				else null end as "PQ_Plazo",
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_PlazoResidual", 	-- Consultar donde sacar!
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then null 									else null end as "PQ_Base",				-- Consultar donde sacar!
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdCliente" 						else null end as "PQ_IdCliente",		
case when dt."IdClasificacionTipoInstrumento" in('RV','FM')	then dt."IdPortafolio"						else null end as "PQ_IdPortafolio",		
-- RF/IF
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."FechaOperacion" 					else null end as "WS_FechaOperacion",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'S'					 				else null end as "WS_ValorizaACompra",
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
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGValorRescate"					else null end as "WS_ValorRescate",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGPrecioCompra"					else null end as "WS_PrecioCompra",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGTasaCompra"						else null end as "WS_TasaCompra",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_PrecioSubyacente",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TasaSubyacente",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_PrecioGarantia",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TasaGarantia",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF') then dt."TIFTasa" 							else null end as "WS_TasaTIF",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'CARTERA' 								else null end as "WS_UsoPrecio",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then null 									else null end as "WS_TipoCartera",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then 'NORMATIVO'				 			else null end as "WS_IdTipoCriterioTasa",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TipoValorizacion" 					else null end as "WS_TipoValorizacion",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."TieneCorteDeCupon"					else null end as "WS_TieneCorteDeCupon",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdCliente" 						else null end as "WS_IdCliente",		
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdPortafolio"						else null end as "WS_IdPortafolio",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoCompraActualMonedaInstrumento"	 else null end as "WS_BK_INGMontoCompraActualMonedaInstrumento",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoATasaCompraActual"				 else null end as "WS_BK_INGMontoATasaCompraActual",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoMercadoActualMonedaInstrumento" else null end as "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."INGMontoATasaMercadoActual"			 else null end as "WS_BK_INGMontoATasaMercadoActual",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdTipoReajuste" 					     else null end as "WS_IdTipoReajuste",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdMonedaInstrumento" 					 else null end as "WS_IdMonedaInstrumento",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."MontoCompra"		 					 else null end as "WS_MontoCompra",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."PlazoEnDiasOperacion"		 			 else null end as "WS_PlazoEnDias",
case when dt."IdClasificacionTipoInstrumento" in('RF','IF')	then dt."IdMonedaEmision"		 				 else null end as "WS_IdMonedaEmision",
INS."Tasa" AS "WS_TasaEmisionInstrumento",
INS."FrecuenciaCupon" AS "WS_FrecuenciaCupon"
from "BSCL.Custodia.BD::CUS.Cartera" dt
LEFT JOIN  "BSCL.Plataforma.BD::INS.Instrumento" INS ON (dt."IdInstrumento" = INS."IdInstrumento")
where  
(
    :Historica = 'S'
    AND dt."IdCartera" IN (
       	select "IdCartera" AS "IdCartera"
	FROM "BSCL.Custodia.BD::HIS.Cartera" 		AS CAR 
	WHERE CAR."IdEmpresa"						= 			:IdEmpresa
	AND CAR."IdEstado" 						 	= 			'VIG'
	AND CAR."IdClasificacionTipoInstrumento" 	IN ('RF','IF')
	AND CAR."IdCliente" 						= 			:IdCliente
	AND  CAR."Nemotecnico" 						not like 	'PACTO%'
	AND CAR."FechaRespaldo"						= 			:Fecha
	
	UNION all
	
	select G."IdCarteraOrigen" AS "IdCartera"
	FROM    "BSCL.Custodia.BD::HIS.Garantia" 	AS 	G
	LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" 	AS 	CAR  		ON CAR."IdCartera" 			= G."IdCarteraOrigen" 
	WHERE G."IdEmpresa"							= :IdEmpresa
	AND G."IdEstado" 							= 'VIG'
	AND G."IdClasificacionTipoInstrumento" 		IN ('RF','IF')
	AND G."IdCliente" 							= :IdCliente
	AND CAR."IdTipoOperacion" 					NOT IN ('COMPAC', 'VENPAC')
	AND G."FechaRespaldo"						= :Fecha
	AND CAR."FechaRespaldo"  					= :Fecha
	
	UNION
	
	select RES."IdCarteraCustodia" AS "IdCartera"
	FROM    "BSCL.Custodia.BD::HIS.Subyacente" 	AS SUBY
	LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 	AS RES 		ON 	RES."FolioSubyacente"  = SUBY."FolioSubyacente"
	INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" 	AS 	PAC 		ON 	(PAC."FolioCartera" 	= SUBY."FolioCarteraRelacionada"  )
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" 	AS 	CAR 		ON 	(CAR."IdCartera" 		= SUBY."IdCarteraRetirada" )		-- Cabecera del pacto
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	AND PAC."Nemotecnico" 						= 'PACTO VENTA'
	AND SUBY."IdEstado" 						IN ('VIG' , 'RPA')
	AND (SUBY."IdClasificacionTipoInstrumento" 	<> 'IF' OR SUBY."INGValorRescate" <> 0)
	AND SUBY."IdClasificacionTipoInstrumento" 	IN ('RF','IF')
	AND (CAR."TipoCarteraCorredor" 				<> 'CC' 
	AND CAR."IdCliente" 						= :IdCliente)  
	AND SUBY."FechaRespaldo" 					= :Fecha
	AND PAC."FechaRespaldo"  					= :Fecha
    )
)
OR 
(
    :Historica <> 'S'
    AND dt."IdCartera" IN (
        	select "IdCartera" AS "IdCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera" 		AS CAR 
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	AND CAR."IdEstado" 						 	= 'VIG'
	AND CAR."IdClasificacionTipoInstrumento" 	IN ('RF','IF')
	AND CAR."IdCliente" 						= :IdCliente
	AND  CAR."Nemotecnico" 						not like 'PACTO%'
	
	
	UNION 
	
	select G."IdCarteraOrigen" AS "IdCartera"
	FROM    "BSCL.Custodia.BD::GTA.Garantia" 	AS 	G
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 	AS CAR ON (CAR."IdCartera" = G."IdCarteraOrigen")
	WHERE G."IdEmpresa"							= 		:IdEmpresa
	AND G."IdEstado" 							= 		'VIG'
	AND G."IdClasificacionTipoInstrumento" 		IN 		('RF','IF')
	AND G."IdCliente" 							= 		:IdCliente
	AND CAR."IdTipoOperacion" 					NOT IN 	('COMPAC', 'VENPAC')
	
	UNION
	
	select RES."IdCarteraCustodia" AS "IdCartera"
	FROM    "BSCL.Custodia.BD::CUS.Subyacente" 	AS SUBY
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 	AS PAC ON (PAC."FolioCartera" = SUBY."FolioCarteraRelacionada"  )
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 	AS CAR ON (CAR."IdCartera" = SUBY."IdCarteraRetirada" )
	LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 	AS RES ON RES."FolioSubyacente"  = SUBY."FolioSubyacente"
	WHERE CAR."IdEmpresa"						= :IdEmpresa
	AND PAC."Nemotecnico" 						= 'PACTO VENTA'
	AND SUBY."IdEstado" 						IN ('VIG' , 'RPA')
	AND (SUBY."IdClasificacionTipoInstrumento" 	<> 'IF' OR SUBY."INGValorRescate" <> 0)
	AND SUBY."IdClasificacionTipoInstrumento" 	IN ('RF','IF')
	AND CAR."TipoCarteraCorredor" 				<> 'CC' 
	AND CAR."IdCliente" 						= :IdCliente 
    )
)

 
AND dt."IdEmpresa" = :IdEmpresa