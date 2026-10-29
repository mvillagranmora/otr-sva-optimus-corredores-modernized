WITH v_OperacYCoberturas 
AS (
	SELECT 
			 GAR."NombreCliente"
			,GAR."DocumentoIdentidad"
			,GAR."IdCliente"
			,GAR."IdPortafolio"
			,GAR."PropositoPortafolio"
			,GAR."FolioCarteraGarantizada"
			,GAR."IdTipoInstrumentoGarantizado"                 -- Ej.: SIM
			,GAR."IdInstrumentoCarteraGarantizada"              -- Ej.: 999 (SIMVEN)
			,GAR."NemoGarantizado" 								-- Ej.: SIMULTANE VENTA
			,GAR."IdContraparteGarantia"                        -- Entidad Destino 
			,GAR."RazonSocialContraparte"  						-- Entidad Destino
			,GAR."EsEspecifica"                                 -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)			
			,COALESCE(GAR."IdOperacionOrigenGarantia",0) AS "IdOperacionOrigenGarantia"	    -- Folio de la operacion(orden) que originó la garantía (solo para específicas)
			,GAR."MontoCoberturaEntidad" 
			,GAR."MontoCoberturaCorredora"
			,GAR."IdBolsaOperacionOrigen" as "IdBolsa"
			
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle" AS GAR
		WHERE GAR."IdCliente"	 					= (CASE WHEN COALESCE(:p_IdCliente,0) <> 0 THEN :p_IdCliente ELSE GAR."IdCliente" END)
		AND	  GAR."IdPortafolio" 					= (CASE WHEN COALESCE(:p_IdPortafolio,0) <> 0 THEN :p_IdPortafolio ELSE GAR."IdPortafolio" END)		
		AND	  GAR."IdTipoInstrumentoGarantizado"    = (CASE WHEN COALESCE(:p_IdTipoInstrumentoGarantizado,'') <> '' THEN  :p_IdTipoInstrumentoGarantizado ELSE GAR."IdTipoInstrumentoGarantizado" END)
		AND	  GAR."IdInstrumentoCarteraGarantizada" = (CASE WHEN COALESCE(:p_IdInstrumentoCarteraGarantizada,0) <> 0 THEN :p_IdInstrumentoCarteraGarantizada ELSE GAR."IdInstrumentoCarteraGarantizada" END)
		AND	  GAR."IdContraparteGarantia"           = (CASE WHEN COALESCE(:p_IdContraparteGarantia,0) <> 0 THEN  :p_IdContraparteGarantia ELSE GAR."IdContraparteGarantia" END)
		AND   GAR."IdEstado"						= 'VIG'
	
), v_CoberturaGarantiaDetalle AS (
	SELECT 
			 MAX(GAR."NombreCliente") 																AS "NombreCliente"
			,MAX(GAR."DocumentoIdentidad") 															AS "DocumentoIdentidad"
			,GAR."IdCliente"
			,GAR."IdPortafolio"
			,GAR."PropositoPortafolio"
			,GAR."IdTipoInstrumentoGarantizado"                 -- Ej.: SIM
			,GAR."IdInstrumentoCarteraGarantizada"              -- Ej.: 999 (SIMVEN)
			,MAX(GAR."NemoGarantizado") 															AS "NemoGarantizado"	-- Ej.: SIMULTANE VENTA
			,GAR."IdContraparteGarantia"                        -- Entidad Destino 
			,GAR."FolioCarteraGarantizada"
			,MAX(GAR."RazonSocialContraparte") 														AS "RazonSocialContraparte" -- Entidad Destino
			,GAR."EsEspecifica"         									 -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)
--			,CASE WHEN GAR."EsEspecifica" = 'S' THEN GAR."IdOperacionOrigenGarantia" ELSE 0 END 	AS "IdOperacionOrigenGarantia"  -- Folio de la operacion(orden) que originó la garantía (solo para específicas)
			,GAR."IdOperacionOrigenGarantia"														AS "IdOperacionOrigenGarantia"
			,cast(SUM(GAR."MontoCoberturaEntidad") as DECIMAL(26,8))   								AS "MontoCoberturaEntidad"
			,cast(SUM(GAR."MontoCoberturaCorredora") as DECIMAL(26,8)) 								AS "MontoCoberturaCorredora"		 
			,MAX(GAR."IdBolsa") 																	as "IdBolsa"
			
		FROM v_OperacYCoberturas AS GAR
		GROUP BY
			GAR."IdCliente",
			GAR."IdPortafolio",
			GAR."PropositoPortafolio",
			GAR."IdTipoInstrumentoGarantizado",
			GAR."IdInstrumentoCarteraGarantizada",
			GAR."IdContraparteGarantia",
			GAR."EsEspecifica",
--			(CASE WHEN GAR."EsEspecifica" = 'S' THEN GAR."IdOperacionOrigenGarantia" ELSE 0 END),
			GAR."IdOperacionOrigenGarantia",
			GAR."FolioCarteraGarantizada"	
), v_ExigibilidadGarantiaOperaciones AS(
	SELECT 	G."IdCliente"	
			,G."IdPortafolio"
			,G."IdTipoInstrumentoGarantizado"   -- SIM
			,G."IdInstrumentoGarantizado"       -- 2249 (VENSIM)  
			,G."IdContraparteGarantia"          -- Entidad Destino	
		    --,G."EsEspecifica"
	--					    ,(CASE WHEN G."EsEspecifica" = 'S' THEN G."IdOperacionOrigenGarantia" ELSE 0 END) AS "IdOperacionOrigenGarantia"  -- Folio de la operacion(orden) que originó la garantía (solo para específicas)
			,G."IdOperacionOrigenGarantia"
			,SUM(G."ExigibilidadEntidad") AS "MontoExigibilidadEntidad"
			,SUM(G."ExigibilidadCorredora") AS "MontoExigibilidadCorredora"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones" G
		WHERE   G."IdCliente" 					= (CASE WHEN COALESCE(:p_IdCliente,0) <> 0 THEN :p_IdCliente ELSE G."IdCliente" END)
			AND G."IdPortafolio" 					= (CASE WHEN COALESCE(:p_IdPortafolio,0) <> 0 THEN :p_IdPortafolio ELSE G."IdPortafolio" END)
			AND G."IdTipoInstrumentoGarantizado"	= (CASE WHEN COALESCE(:p_IdTipoInstrumentoGarantizado,'') <> '' THEN :p_IdTipoInstrumentoGarantizado ELSE G."IdTipoInstrumentoGarantizado" END)
			AND G."IdInstrumentoGarantizado" 		= (CASE WHEN COALESCE(:p_IdInstrumentoCarteraGarantizada,0) <> 0 THEN :p_IdInstrumentoCarteraGarantizada ELSE G."IdInstrumentoGarantizado" END)
			AND G."IdContraparteGarantia" 		= (CASE WHEN COALESCE(:p_IdContraparteGarantia,0) <> 0 THEN :p_IdContraparteGarantia ELSE G."IdContraparteGarantia" END)
		GROUP BY G."IdCliente", G."IdPortafolio", G."IdTipoInstrumentoGarantizado",
			     G."IdInstrumentoGarantizado", G."IdContraparteGarantia", --G."EsEspecifica",						     
	--						     (CASE WHEN G."EsEspecifica" = 'S' THEN G."IdOperacionOrigenGarantia" ELSE 0 END)
				 G."IdOperacionOrigenGarantia"
) 
SELECT 
	CGD."IdCliente",
			CGD."IdPortafolio",
			CGD."PropositoPortafolio",
			CGD."NombreCliente",
			CGD."DocumentoIdentidad",
			CGD."IdTipoInstrumentoGarantizado",             -- Ej.: SIM
			CGD."IdInstrumentoCarteraGarantizada",          -- Ej.: 999 (SIMVEN)
			CGD."NemoGarantizado",    					    -- Ej.: SIMULTANE VENTA
			CGD."IdContraparteGarantia",					-- Entidad Destino (Ej.: Bolsa Comercio de Santiago)
			CGD."RazonSocialContraparte",                   -- Entidad Destino (Ej.: Bolsa Comercio de Santiago)
			CGD."EsEspecifica",                             -- Especifica(Reportes/Vta. Corta) -> Cada operación tiene sus propias garantias) -- No específicas(Simultáneas)
			CGD."IdOperacionOrigenGarantia",                -- Id de la operación asociada a la garantía(cuen es especifica)
			cast(CGD."MontoCoberturaEntidad" as DECIMAL(26,8)) as "MontoCoberturaEntidad",
			cast(CGD."MontoCoberturaCorredora" as DECIMAL(26,8)) as "MontoCoberturaCorredora",
			cast(EXI."MontoExigibilidadEntidad" as DECIMAL(26,8)) as "MontoExigibilidadEntidad",
			cast(EXI."MontoExigibilidadCorredora" as DECIMAL(26,8)) as "MontoExigibilidadCorredora",
			CASE WHEN (IFNULL(CGD."MontoCoberturaEntidad",0) - EXI."MontoExigibilidadEntidad") < 0 THEN 'DESCUBIERTO'
				ELSE 'CUBIERTO'
			END AS "EstadoCoberturaEntidad",			 
			CASE WHEN (IFNULL(CGD."MontoCoberturaCorredora",0) - EXI."MontoExigibilidadCorredora") < 0 THEN 'DESCUBIERTO'
				ELSE 'CUBIERTO'
			END AS "EstadoCoberturaCorredora",
			--Esto aplica solo para reportes, es posible que se requiera agregar nuevas consultas para otras operaciones si requiere.(PERU)
			ORD."IdMonedaLiquidacionOrden"as "IdMonedaLiquidacionOperacion"
			

FROM v_CoberturaGarantiaDetalle CGD  
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON (
	ORD."IdOrden" = CGD."IdOperacionOrigenGarantia"
)
LEFT JOIN v_ExigibilidadGarantiaOperaciones EXI ON (
		CGD."IdCliente" 						= EXI."IdCliente"
	AND CGD."IdPortafolio" 						= EXI."IdPortafolio"
	AND CGD."IdTipoInstrumentoGarantizado" 		= EXI."IdTipoInstrumentoGarantizado"
	AND CGD."IdInstrumentoCarteraGarantizada" 	= EXI."IdInstrumentoGarantizado"
	AND CGD."IdContraparteGarantia" 			= EXI."IdContraparteGarantia"
  --AND CGD."EsEspecifica" 						= EXI."EsEspecifica"
--	AND CGD."IdOperacionOrigenGarantia" 		= (CASE WHEN CGD."EsEspecifica" = 'S' THEN EXI."IdOperacionOrigenGarantia" ELSE 0 END)
	AND CGD."IdOperacionOrigenGarantia" 		= EXI."IdOperacionOrigenGarantia"
)