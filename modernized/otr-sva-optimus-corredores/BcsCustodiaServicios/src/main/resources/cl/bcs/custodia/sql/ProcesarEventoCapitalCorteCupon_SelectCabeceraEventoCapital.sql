WITH CarteraPago AS
(
		SELECT
			 "INGFechaPagoCorteCupon"
			,"INGNumeroCupon"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"INGIdMonedaPagoCupon"
			,"IdEmisor"
		    ,(CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
				WHEN "IdTipoInstrumento" = 'LTH' AND LOCATE("Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN "IdTipoInstrumento" = 'LTH' AND LOCATE("Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			  END) AS "TipoAccion"
			,"INGFechaCorteCupon" 
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
		WHERE 	"IdClasificacionTipoInstrumento" = 'RF'				
		AND		"INGFechaPagoCorteCupon"	     BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
		

		AND     "IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN "IdInstrumento" ELSE :IdInstrumento END)
		AND		"IdEstado" 	IN ( 'VIG','VCD')
		GROUP BY 			
				"INGNumeroCupon", 		
				"IdClasificacionTipoInstrumento", 
				"IdTipoInstrumento", 	
				"IdInstrumento", 
				"Nemotecnico",
				"INGIdMonedaPagoCupon", 	
				"INGFechaPagoCorteCupon",
				"IdEmisor",
				"INGFechaCorteCupon" 
		UNION 
		SELECT
			 "INGFechaPagoCorteCupon"
			,"INGNumeroCupon"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"INGIdMonedaPagoCupon"
			,"IdEmisor"
		    ,(CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
				WHEN "IdTipoInstrumento" = 'LTH' AND LOCATE("Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN "IdTipoInstrumento" = 'LTH' AND LOCATE("Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			  END) AS "TipoAccion"
			,"INGFechaCorteCupon" 
		FROM "BSCL.Custodia.BD::HIS.Cartera"
		WHERE 	"IdClasificacionTipoInstrumento" = 'RF'				
		AND		"INGFechaPagoCorteCupon"	     BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
		

		AND     "IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN "IdInstrumento" ELSE :IdInstrumento END)
		AND		"IdEstado" 	IN ( 'VIG','VCD')
		GROUP BY 			
				"INGNumeroCupon", 		
				"IdClasificacionTipoInstrumento", 
				"IdTipoInstrumento", 	
				"IdInstrumento", 
				"Nemotecnico",
				"INGIdMonedaPagoCupon", 	
				"INGFechaPagoCorteCupon",
				"IdEmisor",
				"INGFechaCorteCupon" 				
				
		UNION
		-- Busca los cupones que tienen pago, pero que estn en garantia
		SELECT 
			 GTA."INGFechaPagoCorteCupon"
			,GTA."INGNumeroCupon"
			,GTA."IdClasificacionTipoInstrumento"
			,GTA."IdTipoInstrumento"
			,GTA."IdInstrumento"
			,GTA."Nemotecnico"
			,GTA."INGIdMonedaPagoCupon"
			,GTA."IdEmisor"
		    ,(CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
				WHEN GTA."IdTipoInstrumento" = 'LTH' AND LOCATE(GTA."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN GTA."IdTipoInstrumento" = 'LTH' AND LOCATE(GTA."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			  END) AS "TipoAccion"
			,GTA."INGFechaCorteCupon"
		
		FROM       "BSCL.Custodia.BD::GTA.Garantia" AS GTA
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."IdCartera" = GTA."IdCarteraOrigen" AND CAR."Nemotecnico" = GTA."Nemotecnico"  AND CAR."INGNumeroCupon" = GTA."INGNumeroCupon")
		LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = CAR."IdCliente")
		LEFT JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = SOC."IdPersona")
		LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
		LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" POR	ON (POR."IdPortafolio" = CAR."IdPortafolio")
		
		WHERE 	CAR."IdClasificacionTipoInstrumento" = 'RF'
		AND		GTA."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
		AND     GTA."FechaLiquidacion" 		      <= :FechaCorteCuponDesde
		AND     GTA."FechaComprobante"         < GTA."INGFechaCorteCupon"
		AND     CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
		AND 	CAR."IdEstado" IN ('LIQ', 'RPA')   -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente

		UNION
		-- Busca los cupones que tienen pago, pero que estn en garantia historica
		SELECT 
			 GTA."INGFechaPagoCorteCupon"
			,GTA."INGNumeroCupon"
			,GTA."IdClasificacionTipoInstrumento"
			,GTA."IdTipoInstrumento"
			,GTA."IdInstrumento"
			,GTA."Nemotecnico"
			,GTA."INGIdMonedaPagoCupon"
			,GTA."IdEmisor"
		    ,(CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
				WHEN GTA."IdTipoInstrumento" = 'LTH' AND LOCATE(GTA."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN GTA."IdTipoInstrumento" = 'LTH' AND LOCATE(GTA."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			  END) AS "TipoAccion"
			,GTA."INGFechaCorteCupon"
		
		FROM       "BSCL.Custodia.BD::HIS.Garantia" AS GTA
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (GTA."IdCarteraOrigen" = CAR."IdCartera" AND CAR."Nemotecnico" = GTA."Nemotecnico")
		LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = CAR."IdCliente")
		LEFT JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = SOC."IdPersona")
		LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
		LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" POR	ON (POR."IdPortafolio" = CAR."IdPortafolio")
		
		WHERE 	CAR."IdClasificacionTipoInstrumento" = 'RF'
		AND		GTA."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
		AND     GTA."FechaLiquidacion" 		      <= :FechaCorteCuponDesde
		AND     CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
		AND 	CAR."IdEstado" IN ('LIQ', 'RPA')   -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente
		AND	    GTA."FechaRespaldo"				< GTA."INGFechaPagoCorteCupon" 
			UNION
		-- Busca los cupones que estan en nominal
		SELECT 
			  :FechaCorteCuponHasta                                        AS "INGFechaPagoCorteCupon"
			  ,CAST(0 AS BIGINT)                                            AS "Secuencia"
			  ,MOV."IdClasificacionTipoInstrumento"                            AS "IdClasificacionTipoInstrumento"
			  ,MOV."IdTipoInstrumento"                                         AS "IdTipoInstrumento"
			  ,MOV."IdInstrumento"                                             AS "IdInstrumento"
			  ,MOV."Nemotecnico"                                               AS "Nemotecnico"
			  ,MOV."IdMonedaInstrumento"                                       AS "INGIdMonedaPagoCupon"
			  ,MOV."IdEmisor"                                                  AS "IdEmisor"
			  ,'CORTE_CUPON'                                                   AS "TipoAccion"
			  ,:FechaCorteCuponHasta                                         AS "INGFechaCorteCupon"
			
		    FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV 
			LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS ECLI ON ECLI."IdInstrumento"=MOV."IdInstrumento" 
			LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"	
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
			LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = MOV."FolioComprobante" AND CMBTE."IdCliente" = MOV."IdCliente" AND MOV."FolioComprobante"!= 0
			WHERE (
		          (MOV."IdConcepto"='CMINRF' AND MOV."FechaLiquidacion" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta AND MOV."FechaOperacion" <:FechaCorteCuponHasta)
		        OR
		          (MOV."IdConcepto"='VNINRF' AND MOV."FechaLiquidacion" >:FechaCorteCuponHasta)
		          )
			AND MOV."IdEstado" = 'PEN'
			AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
			AND MOV."IdClasificacionTipoInstrumento" = 'RF'
			AND MOV."IdTipoOperacion" in ('COMRF','VENRF')
			AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') AND MOV."EsNominal" = 'S'
			AND MOV."TieneCorteDeCupon" = 'S'
			AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
			
		UNION 
		---Carteras negativas
		SELECT
			 CAR."FechaPagoCorteCupon" 	AS "INGFechaPagoCorteCupon"
			,CAR."NumeroCupon" 			AS "INGNumeroCupon"
			,TIP."IdClasificacionTipoInstrumento"
			,TIP."IdTipoInstrumento"
			,INS."IdInstrumento"
			,INS."Nemotecnico"
			,CAR."IdMonedaPagoCupon" 	AS "INGIdMonedaPagoCupon"
			,INS."IdEmisor"
		    ,(CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
				WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			  END) AS "TipoAccion"
			,"FechaCorteCupon"  AS "INGFechaCorteCupon" 
		FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" AS CAR
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CAR."IdInstrumento" = INS."IdInstrumento" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIP ON INS."IdTipoInstrumento"  = TIP."IdTipoInstrumento" 
		WHERE 	"IdClasificacionTipoInstrumento" = 'RF'				
		AND		CAR."FechaPagoCorteCupon"	     BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
		

		AND     INS."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN INS."IdInstrumento" ELSE :IdInstrumento END)
		AND		CAR."IdEstado" 	IN ('VIG','VCD')
		GROUP BY 			
				CAR."NumeroCupon", 		
				TIP."IdClasificacionTipoInstrumento", 
				TIP."IdTipoInstrumento", 	
				INS."IdInstrumento", 
				INS."Nemotecnico",
				CAR."IdMonedaPagoCupon", 	
				CAR."FechaPagoCorteCupon",
				INS."IdEmisor",
				CAR."FechaCorteCupon"
				
		UNION		
		-- Cartera negativa historica
		SELECT
			 CAR."FechaPagoCorteCupon" 	AS "INGFechaPagoCorteCupon"
			,CAR."NumeroCupon" 			AS "INGNumeroCupon"
			,TIP."IdClasificacionTipoInstrumento"
			,TIP."IdTipoInstrumento"
			,INS."IdInstrumento"
			,INS."Nemotecnico"
			,CAR."IdMonedaPagoCupon" 	AS "INGIdMonedaPagoCupon"
			,INS."IdEmisor"
		    ,(CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
				WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			  END) AS "TipoAccion"
			,"FechaCorteCupon"  AS "INGFechaCorteCupon" 
		FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" AS CAR
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CAR."IdInstrumento" = INS."IdInstrumento" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIP ON INS."IdTipoInstrumento"  = TIP."IdTipoInstrumento" 
		WHERE 	"IdClasificacionTipoInstrumento" = 'RF'				
		AND		CAR."FechaPagoCorteCupon"	     BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
		

		AND     INS."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN INS."IdInstrumento" ELSE :IdInstrumento END)
		AND		CAR."IdEstado" 	IN ('VIG','VCD')
		GROUP BY 			
				CAR."NumeroCupon", 		
				TIP."IdClasificacionTipoInstrumento", 
				TIP."IdTipoInstrumento", 	
				INS."IdInstrumento", 
				INS."Nemotecnico",
				CAR."IdMonedaPagoCupon", 	
				CAR."FechaPagoCorteCupon",
				INS."IdEmisor",
				CAR."FechaCorteCupon"
		), 
		CabeceraPagoActiva AS (
	        SELECT 
	        "INGFechaPagoCorteCupon"
			,"INGNumeroCupon"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"INGIdMonedaPagoCupon"
			,"IdEmisor"
			,"TipoAccion"
			,"INGFechaCorteCupon" 
	        ,ROW_NUMBER() OVER (PARTITION BY CAR."IdInstrumento" ORDER BY CAR."INGFechaPagoCorteCupon" DESC) AS rn
	        FROM CarteraPago AS CAR
	 
)
		--------------------------------------------------------------------------------------------------------------------------
-- CARTERAS QUE GENERAN EVENTO CAPITAL CORTE CUPON  (Excluye letras hipotecarias cuyo nemo contiene "*" o "&")

SELECT
	 'CC'													AS "IdTipoEventoCapital"
	,CAR."INGNumeroCupon"									AS "Secuencia"
	,CONCAT('CORTE CUPON ' , "Nemotecnico")					AS "Descripcion"
	,'S'													AS "EsInscrito"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"										AS "Nemo"
	,TO_DECIMAL(0,26,8)										AS "Factor"
	,TO_DECIMAL(0,26,8)										AS "Valor"
	,CAR."INGIdMonedaPagoCupon"								AS "IdMoneda"
	,TO_BIGINT(0)											AS "IdInstrumentoNuevo"
	,''														AS "NemoNuevo"
	,TO_DECIMAL(0,26,8)										AS "FactorNuevo"
	,CAR."INGFechaCorteCupon"								AS "FechaActualizacion"
	,CAR."INGFechaCorteCupon"								AS "FechaLimite"
	,CAR."INGFechaPagoCorteCupon"							AS "FechaPago"
	,TO_BIGINT(0)											AS "NumeroDividendo"
	,'P'													AS "IdTipoDividendo"
	,TO_DECIMAL(0,26,8)										AS "PrecioAnterior"
	, CURRENT_UTCTIMESTAMP									AS "FechaHoraIngresoRegistro"
	, CURRENT_UTCTIMESTAMP									AS "FechaHoraUltimaActualizacion"
	,'C'													AS "TipoIngreso"
	,'VIG'													AS "IdEstado"
	,'SIS'													AS "Origen"
	,CAR."IdEmisor"
    ,CAR."TipoAccion"    
FROM CabeceraPagoActiva AS CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EC
	ON 	(			CAR."IdInstrumento" 	   = EC."IdInstrumento"
			AND		CAR."IdTipoInstrumento"    = EC."IdTipoInstrumento"
			AND		CAR."INGFechaPagoCorteCupon" = EC."FechaPago"
			AND		EC."IdTipoEventoCapital"   = 'CC'                        
			AND		EC."IdEstado"             <> 'ANU'
		)
WHERE CAR."TipoAccion"        = 'CORTE_CUPON' AND RN=1
AND    EC."IdTipoInstrumento"  IS NULL

UNION ALL

-------------------------------------------------------------
-- CARTERAS QUE GENERAN CORTE CUPON (Letras hipotecarias cuyo nemo contiene "*" o "&")

SELECT
	 'CC'													AS "IdTipoEventoCapital"
	,CAR."INGNumeroCupon"									AS "Secuencia"	
	,CONCAT('CAMBIO DE NEMO' , "Nemotecnico")				AS "Descripcion"
	,'S'													AS "EsInscrito"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"										AS "Nemo"
	,TO_DECIMAL(0,26,8)										AS "Factor"
	,TO_DECIMAL(0,26,8)										AS "Valor"
	,CAR."INGIdMonedaPagoCupon"								AS "IdMoneda"
	,TO_BIGINT(0)											AS "IdInstrumentoNuevo"
	,''														AS "NemoNuevo"
	,TO_DECIMAL(0,26,8)										AS "FactorNuevo"
	,CAR."INGFechaCorteCupon"								AS "FechaActualizacion"
	,CAR."INGFechaCorteCupon"								AS "FechaLimite"
	,CAR."INGFechaPagoCorteCupon"				            AS "FechaPago"
	,TO_BIGINT(0)											AS "NumeroDividendo"
	,'P'													AS "IdTipoDividendo"
	,TO_DECIMAL(0,26,8)										AS "PrecioAnterior"
	, CURRENT_UTCTIMESTAMP									AS "FechaHoraIngresoRegistro"
	, CURRENT_UTCTIMESTAMP									AS "FechaHoraUltimaActualizacion"
	,'C'													AS "TipoIngreso"
	,'VIG'													AS "IdEstado"
	,'SIS'													AS "Origen"
	,CAR."IdEmisor"
	,CAR."TipoAccion"	      
FROM CabeceraPagoActiva AS CAR
WHERE CAR."TipoAccion" = 'CAMBIO_NEMO' AND RN=1;