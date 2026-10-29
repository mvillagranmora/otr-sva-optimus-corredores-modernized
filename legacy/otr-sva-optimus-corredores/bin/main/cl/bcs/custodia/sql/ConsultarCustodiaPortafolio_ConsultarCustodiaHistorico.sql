SELECT	
	CLI."IdCliente"														AS "IdCliente"
	,CLI."IdPortafolio"													AS "IdPortafolio"
	,CLI."CodigoPropositoPortafolio"									AS "CodigoPortafolio"
	,CUS.RUT_CLI														AS "DocumentoIdentidad"
	,CLI."NombreCompleto"												AS "NombreCliente"
	,'Otros (No APV)'													AS "Producto" 
	,INS."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"
	,INS."IdTipoInstrumento"											AS "IdTipoInstrumento"
	,INS."NombreTipoInstrumento"										AS "NombreTipoInstrumento"
	,INS."IdEmisor"														AS "IdEmisor"
	,INS."IdInstrumento"												AS "IdInstrumento"
	,CUS.NEMO															AS "Nemotecnico"
	,INS."ValorClasificacionRiesgo"										AS "ClasificacionRiesgoInstrumento"
	,INS."NombreClasificacionTipoInstrumento"							AS "NombreClasificacionTipoInstrumento"
	,INS."RazonSocial"													AS "RazonSocialInstrumento"
	,CUS.COD_BOVEDA														AS "IdBoveda"
	,CUS.CAN_CUSTODIA  		+ 		-- CantidadCustodia
	 CUS.CAN_GARANTIA  		+  		-- CantidadGarantia
	 CUS.CAN_COM_PLAZO 		- 		-- CantidadSimulCom
	 CUS.CAN_VEN_PLAZO 		+ 		-- CantidadSimulVen
	 CUS.CAN_PRESTAMO 		- 		-- CantidadEmpresti
	 CUS.CAN_VENTA_CORTA			-- CantidadPrestamo										
	 																	AS "TotalPosicion"
	,CUS.CAN_CUSTODIA 													AS "CantidadCustodia"
	
	,CUS.MONTO_INI_AJUS 												AS "ValorPPPCustodia"
	,CUS.MONTO_INI_AJUS 												AS "MontoPPPCustodia" 
	
	,CUS.MONTO_VALORIZ 													AS "ValorMercadoCustodia"
	,CUS.MONTO_VALORIZ 													AS "MontoMercadoCustodia"
	,TO_DECIMAL(0)														AS "DiferenciaMercadoCustodia"
	,CUS.PRECIO_CIERRE 													AS "PrecioMercadoCustodia"
	,CUS.PRECIO_PROM   													AS "PPPCustodia"
	,CUS.MON_MORIGEN													AS "IdMonedaInstrReajuste"
	,CASE 
		WHEN CLI."IdCliente" 	= :p_IdClienteCuentaPropia  THEN 'CPRO' 
		WHEN IND."ValorIndicador" = 'S' 					THEN 'REL'
		ELSE 													 'TER' 
	END 																AS "Relacion"
	,'N'																AS "EsFungible"
	,CUS.CAN_GARANTIA 													AS "CantidadGarantia"
	,CUS.CAN_VENTA_CORTA												AS "CantidadPrestamo"
	,CUS.CAN_PRESTAMO													AS "CantidadEmpresti"
	,CASE 
		WHEN (CUS.PRECIO_CIERRE  <> 0 AND CUS.PRECIO_PROM <> 0) AND ((CUS.PRECIO_CIERRE - CUS.PRECIO_PROM) <> 0)THEN 
		(CUS.PRECIO_CIERRE - CUS.PRECIO_PROM)/(CUS.PRECIO_PROM) * 100
		ELSE 9999
	END 																AS "Rentabilidad"
	
	,CUS.CAN_VEN_PLAZO													AS "CantidadSimulVen"
	,CUS.CAN_COM_PLAZO													AS "CantidadSimulCom"
	,CUS.CAN_TRAM_COM 													AS "CantidadTransIng"
	,CUS.CAN_TRAM_VEN 													AS "CantidadTransEgr"
	,TO_DECIMAL(0)														AS "CantidadPactosV"
	,TO_DECIMAL(0)														AS "CantidadPactosC"
	

FROM 		"BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP
LEFT JOIN   "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"		AS CLI
ON (CLI."IdEmpresa" = EMP."IdEmpresa" )

LEFT JOIN "BSCL.Sebra.BD::MIG.TBCTCURV"										AS CUS
ON (TRIM(CUS.RUT_CLI) 	  = TRIM(CLI."DocumentoIdentidad") AND 
	TRIM(CUS.SEC_RUT_CLI) = TRIM(CLI."CodigoPortafolio"))

LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"			AS INS
ON (TRIM(CUS.NEMO) = INS."Nemotecnico")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IND
ON IND."IdCliente" =  CLI."IdCliente" AND IND."CodigoTipoIndicador"  = 'RELCORR'			

WHERE
	(EMP."IdEmpresa"						= 	:p_IdEmpresa)																	AND		
	(COD_BOVEDA				    			!= 	'TOT')																			AND 
	(CLI."IdCliente"						= 	:p_IdCliente						OR IFNULL(:p_IdCliente,0)		= 0) 		AND
	(CLI."IdPortafolio" 					=   :p_IdPortafolio						OR IFNULL(:p_IdPortafolio,0)	= 0)		AND
	(COD_PER 								= 	:p_CodPer)																		AND
	(INS."IdInstrumento"					=   :p_IdInstrumento 					OR :p_IdInstrumento				= 0)		AND
	(INS."IdClasificacionTipoInstrumento"	= 	:p_IdClasificacionTipoInstrumento	OR :p_IdClasificacionTipoInstrumento = '')	AND
	(INS."IdTipoInstrumento"				= 	:p_IdTipoInstrumento				OR :p_IdTipoInstrumento 		= '')		AND
	(IFNULL(:p_IdProductoAgrupador,0)		=   0)
	
	

UNION ALL

-- RENTA FIJA
SELECT	
	 CLI."IdCliente"													AS "IdCliente"
	,CLI."IdPortafolio"													AS "IdPortafolio"
	,CLI."CodigoPropositoPortafolio"									AS "CodigoPortafolio"
	,CUS.RUT_CLI														AS "DocumentoIdentidad"
	,CLI."NombreCompleto"												AS "NombreCliente"
	,'Otros (No APV)'													AS "Producto"
	,INS."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"
	,INS."IdTipoInstrumento"											AS "IdTipoInstrumento"
	,INS."NombreTipoInstrumento"										AS "NombreTipoInstrumento"
	,INS."IdEmisor"														AS "IdEmisor"
	,INS."IdInstrumento"												AS "IdInstrumento"
	,CUS.NEMO															AS "Nemotecnico"
	,INS."ValorClasificacionRiesgo"										AS "ClasificacionRiesgoInstrumento"
	,INS."NombreClasificacionTipoInstrumento"							AS "NombreClasificacionTipoInstrumento"
	,INS."RazonSocial"													AS "RazonSocialInstrumento"
	,CUS.COD_BOVEDA														AS "IdBoveda"
	,CUS.CAN_CUSTODIA  + 	-- CantidadCustodia
	 CUS.CAN_GARANTIA  + 	-- CantidadGarantia 
	 CUS.CAN_COM_PLAZO -	-- CantidadPactosV
	 CUS.CAN_VEN_PLAZO  	-- CantidadPactosC
	 	 																AS "TotalPosicion"
	
	,CUS.CAN_CUSTODIA 													AS "CantidadCustodia"
	,CUS.MONTO_INI_AJUS													AS "ValorPPPCustodia"
	,CUS.MONTO_INI_AJUS													AS "MontoPPPCustodia"
	
	,CUS.MONTO_VALORIZ 													AS "ValorMercadoCustodia"
	,CUS.MONTO_VALORIZ 													AS "MontoMercadoCustodia"
	,TO_DECIMAL(0)														AS "DiferenciaMercadoCustodia"	
	
	,CUS.PRECIO_MED_CIE 												AS "PrecioMercadoCustodia"
	,CUS.PRECIO_PROM   													AS "PPPCustodia"
	,INS."IdTipoReajuste"												AS "IdMonedaInstrReajuste"
	,CASE 
		WHEN CLI."IdCliente" 	= :p_IdClienteCuentaPropia  THEN 'CPRO' 
		WHEN IND."ValorIndicador" = 'S' 					THEN 'REL'
		ELSE 													 'TER' 
	END 																AS "Relacion"
	,'N'																AS "EsFungible"
	,CUS.CAN_GARANTIA 													AS "CantidadGarantia"
	,0																	AS "CantidadPrestamo" 
	,TO_DECIMAL(0)														AS "CantidadEmpresti"
	,CASE 
		WHEN (CUS.PRECIO_MED_CIE <> 0 AND CUS.PRECIO_PROM <> 0) AND ((CUS.PRECIO_MED_CIE  - CUS.PRECIO_PROM) <> 0) THEN 
		(CUS.PRECIO_MED_CIE  - CUS.PRECIO_PROM)/(CUS.PRECIO_PROM) * 100
		ELSE 9999
	END 																AS "Rentabilidad"
	,0																	AS "CantidadSimulVen"
	,0																	AS "CantidadSimulCom"
	,CUS.CAN_TRAMITE_COM												AS "CantidadTransIng"
	,CUS.CAN_TRAMITE_VEN												AS "CantidadTransEgr"
	
	,TO_DECIMAL(CUS.CAN_COM_PLAZO)										AS "CantidadPactosV"
	,TO_DECIMAL(CUS.CAN_VEN_PLAZO)										AS "CantidadPactosC"
	
FROM 		"BSCL.Plataforma.BD::PLA.Empresa" 							AS EMP
LEFT JOIN   "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"	AS CLI
ON (CLI."IdEmpresa" = EMP."IdEmpresa" )

LEFT JOIN "BSCL.Sebra.BD::MIG.TBCTCURF"									AS CUS
ON (TRIM(CUS.RUT_CLI) 	  = TRIM(CLI."DocumentoIdentidad") AND 
	TRIM(CUS.SEC_RUT_CLI) = TRIM(CLI."CodigoPortafolio"))

LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"		AS INS
ON (TRIM(CUS.NEMO) = INS."Nemotecnico")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IND
ON IND."IdCliente" =  CLI."IdCliente" AND IND."CodigoTipoIndicador"  = 'RELCORR'


WHERE
	(EMP."IdEmpresa"						= 	:p_IdEmpresa)																	AND		
	(COD_BOVEDA				    			!= 	'TOT')																			AND 
	(CLI."IdCliente"						= 	:p_IdCliente						OR IFNULL(:p_IdCliente,0)		= 0) 		AND
	(CLI."IdPortafolio" 					=   :p_IdPortafolio						OR IFNULL(:p_IdPortafolio,0)	= 0)		AND
	(COD_PER 								= 	:p_CodPer)																		AND
	(INS."IdInstrumento"					=   :p_IdInstrumento 					OR :p_IdInstrumento				= 0)		AND
	(INS."IdClasificacionTipoInstrumento"	= 	:p_IdClasificacionTipoInstrumento	OR :p_IdClasificacionTipoInstrumento = '')	AND
	(INS."IdTipoInstrumento"				= 	:p_IdTipoInstrumento				OR :p_IdTipoInstrumento 		= '')		AND
	(IFNULL(:p_IdProductoAgrupador,0)		=   0)


	
UNION ALL

-- FONDOS MUTUOS
SELECT	
	 CLI."IdCliente"													AS "IdCliente"
	,CLI."IdPortafolio"													AS "IdPortafolio"
	,CLI."CodigoPropositoPortafolio"									AS "CodigoPortafolio"
	,CUS.RUT_CLI														AS "DocumentoIdentidad"
	,CLI."NombreCompleto"												AS "NombreCliente"
	,'Otros (No APV)'													AS "Producto"
	,INS."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"
	,INS."IdTipoInstrumento"											AS "IdTipoInstrumento"
	,INS."NombreTipoInstrumento"										AS "NombreTipoInstrumento"
	,INS."IdEmisor"														AS "IdEmisor"
	,INS."IdInstrumento"												AS "IdInstrumento"
	,CUS.NEMO															AS "Nemotecnico"
	,INS."ValorClasificacionRiesgo"										AS "ClasificacionRiesgoInstrumento"
	,INS."NombreClasificacionTipoInstrumento"							AS "NombreClasificacionTipoInstrumento"
	,INS."RazonSocial"													AS "RazonSocialInstrumento"
	,CUS.COD_BOVEDA														AS "IdBoveda"
	,CUS.CAN_CUSTODIA   + 
	 CUS.CAN_GARANTIA 	
	 																	AS "TotalPosicion"	
	,CUS.CAN_CUSTODIA 													AS "CantidadCustodia"
	,CUS.MONTO_INI_AJUS													AS "ValorPPPCustodia"
	,CUS.MONTO_INI_AJUS													AS "MontoPPPCustodia"
	,CUS.MONTO_VALORIZ 													AS "ValorMercadoCustodia"
	,CUS.MONTO_VALORIZ 													AS "MontoMercadoCustodia"
	,TO_DECIMAL(0)														AS "DiferenciaMercadoCustodia"
	,CUS.PRECIO_CIERRE													AS "PrecioMercadoCustodia"
	,CUS.PRECIO_PROM   													AS "PPPCustodia"
	,INS."IdMoneda"														AS "IdMonedaInstrReajuste"
	,CASE 
		WHEN CLI."IdCliente" 	= :p_IdClienteCuentaPropia  THEN 'CPRO' 
		WHEN IND."ValorIndicador" = 'S' 					THEN 'REL'
		ELSE 													 'TER' 
	END 																AS "Relacion"
	,'N'																AS "EsFungible"
	,CUS.CAN_GARANTIA 													AS "CantidadGarantia"	
	,0																	AS "CantidadPrestamo"	
	,TO_DECIMAL(0)														AS "CantidadEmpresti"
	
	,CASE 
		WHEN (CUS.PRECIO_CIERRE  <> 0 AND CUS.PRECIO_PROM <> 0) AND ((CUS.PRECIO_CIERRE - CUS.PRECIO_PROM) <> 0) THEN 
		(CUS.PRECIO_CIERRE - CUS.PRECIO_PROM)/(CUS.PRECIO_PROM) * 100
		ELSE 9999
	END 																AS "Rentabilidad"	
	,0																	AS "CantidadSimulVen"
	,0																	AS "CantidadSimulCom"	
	,0 																	AS "CantidadTransIng"
	,0				 													AS "CantidadTransEgr"
	,TO_DECIMAL(0)														AS "CantidadPactosV"
	,TO_DECIMAL(0)														AS "CantidadPactosC"
	
FROM 		"BSCL.Plataforma.BD::PLA.Empresa" 							AS EMP
LEFT JOIN   "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"	AS CLI
ON (CLI."IdEmpresa" = EMP."IdEmpresa" )

LEFT JOIN "BSCL.Sebra.BD::MIG.TBCTCUFM"									AS CUS
ON (TRIM(CUS.RUT_CLI) 	  = TRIM(CLI."DocumentoIdentidad") AND 
	TRIM(CUS.SEC_RUT_CLI) = TRIM(CLI."CodigoPortafolio"))

LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"		AS INS
ON (TRIM(CUS.NEMO) = INS."Nemotecnico")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IND
ON IND."IdCliente" =  CLI."IdCliente" AND IND."CodigoTipoIndicador"  = 'RELCORR'


WHERE
	(EMP."IdEmpresa"						= 	:p_IdEmpresa)																	AND		
	(COD_BOVEDA				    			!= 	'TOT')																			AND 
	(CLI."IdCliente"						= 	:p_IdCliente						OR IFNULL(:p_IdCliente,0)		= 0) 		AND
	(CLI."IdPortafolio" 					=   :p_IdPortafolio						OR IFNULL(:p_IdPortafolio,0)	= 0)		AND
	(COD_PER 								= 	:p_CodPer)																		AND
	(INS."IdInstrumento"					=   :p_IdInstrumento 					OR :p_IdInstrumento				= 0)		AND
	(INS."IdClasificacionTipoInstrumento"	= 	:p_IdClasificacionTipoInstrumento	OR :p_IdClasificacionTipoInstrumento = '')	AND
	(INS."IdTipoInstrumento"				= 	:p_IdTipoInstrumento				OR :p_IdTipoInstrumento 		= '')		AND
	(IFNULL(:p_IdProductoAgrupador,0)		=   0)



UNION ALL

-- IIF
SELECT	
	 CLI."IdCliente"													AS "IdCliente"
	,CLI."IdPortafolio"													AS "IdPortafolio"
	,CLI."CodigoPropositoPortafolio"									AS "CodigoPortafolio"
	,CUS.RUT_CLI														AS "DocumentoIdentidad"
	,CLI."NombreCompleto"												AS "NombreCliente"
	,'Otros (No APV)'													AS "Producto"
	,INS."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"
	,INS."IdTipoInstrumento"											AS "IdTipoInstrumento"
	,INS."NombreTipoInstrumento"										AS "NombreTipoInstrumento"
	,CUS.EMISOR															AS "IdEmisor"
	,INS."IdInstrumento"												AS "IdInstrumento"
	,CUS.NEMO_SVS														AS "Nemotecnico"
	,INS."ValorClasificacionRiesgo"										AS "ClasificacionRiesgoInstrumento"
	,INS."NombreClasificacionTipoInstrumento"							AS "NombreClasificacionTipoInstrumento"
	,INS."RazonSocial"													AS "RazonSocialInstrumento"
	,CUS.COD_BOVEDA														AS "IdBoveda"
	,CASE 
		WHEN  
		CUS.IND_FUNGIBILIDAD = 'S' 
		THEN 
			1 * (CUS.CAN_ACTUAL * CUS.VAL_RESC)	 	
		ELSE 
			(CUS.CAN_ACTUAL + CUS.CAN_GARANTIA + CUS.CAN_COM_PLAZO) * CUS.VAL_RESC
	END																	AS "TotalPosicion"	
	
	
	
	,CUS.CAN_ACTUAL	* CUS.VAL_RESC										AS "CantidadCustodia"
	,CUS.MONTO_INI_AJUS													AS "ValorPPPCustodia"
	,CUS.MONTO_INI_AJUS													AS "MontoPPPCustodia"
	,CUS.MONTO_VAL_DISP 												AS "ValorMercadoCustodia"
	,CUS.MONTO_VAL_DISP 												AS "MontoMercadoCustodia"
	,TO_DECIMAL(0)														AS "DiferenciaMercadoCustodia"	
	, 0																	AS "PrecioMercadoCustodia"
	,CUS.TASA_PROM_COMP  												AS "PPPCustodia"
	,INS."IdTipoReajuste"												AS "IdMonedaInstrReajuste"
	,CASE 
		WHEN CLI."IdCliente" 	= :p_IdClienteCuentaPropia  THEN 'CPRO' 
		WHEN IND."ValorIndicador" = 'S' 					THEN 'REL'
		ELSE 													 'TER' 
	END 																AS "Relacion"
	,CUS.IND_FUNGIBILIDAD												AS "EsFungible"
	,CUS.CAN_GARANTIA 													AS "CantidadGarantia"
	,0				 													AS "CantidadPrestamo"
	,TO_DECIMAL(0)														AS "CantidadEmpresti"
	
	,CASE 
		WHEN (CUS.TASA_PROM_COMP  <> 0 AND CUS.TASA_MED_CIE <> 0) AND ((CUS.TASA_PROM_COMP - CUS.TASA_MED_CIE) <> 0) THEN 
		(CUS.TASA_PROM_COMP - CUS.TASA_MED_CIE)/(CUS.TASA_MED_CIE) * 100
		ELSE 9999
	END 																AS "Rentabilidad"		
	,0																	AS "CantidadSimulVen"
	,0																	AS "CantidadSimulCom"
	,CUS.CAN_TRAMITE_COM												AS "CantidadTransIng"
	,CUS.CAN_TRAMITE_VEN												AS "CantidadTransEgr"
	,CUS.CAN_COM_PLAZO	* 	CUS.VAL_RESC								AS "CantidadPactosV"	
	,0																	AS "CantidadPactosC"
		

	
	
FROM 		"BSCL.Plataforma.BD::PLA.Empresa" 							AS EMP
LEFT JOIN   "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"	AS CLI
ON (CLI."IdEmpresa" = EMP."IdEmpresa" )

LEFT JOIN "BSCL.Sebra.BD::MIG.TBCTCIIF"									AS CUS
ON (TRIM(CUS.RUT_CLI) 	  = TRIM(CLI."DocumentoIdentidad") AND 
	TRIM(CUS.SEC_RUT_CLI) = TRIM(CLI."CodigoPortafolio"))

LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"		AS INS
ON (TRIM(CUS.NEMO) = INS."Nemotecnico")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IND
ON IND."IdCliente" =  CLI."IdCliente" AND IND."CodigoTipoIndicador"  = 'RELCORR'


WHERE
	(EMP."IdEmpresa"						= 	:p_IdEmpresa)																	AND		
	(COD_BOVEDA				    			!= 	'TOT')																			AND 
	(CLI."IdCliente"						= 	:p_IdCliente						OR IFNULL(:p_IdCliente,0)		= 0) 		AND
	(CLI."IdPortafolio" 					=   :p_IdPortafolio						OR IFNULL(:p_IdPortafolio,0)	= 0)		AND
	(COD_PER 								= 	:p_CodPer)																		AND
	(INS."IdInstrumento"					=   :p_IdInstrumento 					OR :p_IdInstrumento				= 0)		AND
	(INS."IdClasificacionTipoInstrumento"	= 	:p_IdClasificacionTipoInstrumento	OR :p_IdClasificacionTipoInstrumento = '')	AND
	(INS."IdTipoInstrumento"				= 	:p_IdTipoInstrumento				OR :p_IdTipoInstrumento 		= '')		AND
	(IFNULL(:p_IdProductoAgrupador,0)		=   0)
