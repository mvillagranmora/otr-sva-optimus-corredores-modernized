SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTBIF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTBIF" AS


		SELECT 		     
			TRA."IdTransaccion",
		   	CAST(TO_VARCHAR (UTCTOLOCAL(TRA."FechaOperacion",'America/Santiago','platform'),'YYYY-MM-DD:HH24:MI:SS.FF7') AS VARCHAR(26)) AS FEC_TRANS, --char](26) NOT NULL, *********
			LPAD(LEFT(TRA."Folio",8),8) AS FOLIO_TRANS, --char](8) NOT NULL, *********
			TRA."IdBolsa" AS IND_BOLSA, --char](3) NOT NULL,
		 	COALESCE( substr(TOPE."AccionOperacion",1,1),'') AS TIPO_OPERAC, --char](1) NOT NULL, C/V
		    TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FECHA, --char](10) NOT NULL, *********
		    TRA."IdEmisor"  AS EMISOR, -- CHAR 10 Arreglar destino 
		    CASE WHEN left(TRA."IdTipoReajuste",2) = 'CL'THEN 'NR'ELSE left(TRA."IdTipoReajuste",2) END  AS TIPO_REAJ, -- CHAR 2
		    TRA."FechaVencimiento"  AS FEC_VCTO,  --  char    10
		    COALESCE(CODCORRCONTRA."CodigoCorredora",'0') AS COD_CORR_CONTRA, --decimal](3, 0) NOT NULL, ********* 82
			case when TRA."CodigoOperadorCorredor" is null then CODCORR."CodigoCorredora" when LENGTH(TRIM(TRA."CodigoOperadorCorredor")) = 0 then CODCORR."CodigoCorredora" when TRA."CodigoOperadorCorredor" > 100 then TRA."CodigoOperadorCorredor" else CODCORR."CodigoCorredora" end AS COD_OPE_EXT, --decimal](3, 0) NOT NULL, ********* 42 PREGUNTAR ????
		    TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FEC_DIG, --char](10) NOT NULL, *********
		    TRA."FechaEmision"  AS FEC_EMI,  --   char    10
		    'N'  AS IND_ASIGNAC,   -- char    1
		    'N'  AS IND_COMPROMISO, --char](1) NOT NULL,
		    'N'  AS IND_CTA_PRO, --char](1) NOT NULL, 
			TRA."IndicadorDCV" AS IND_DCV, --char](1) NOT NULL,  ********* D
		    'N'  AS IND_LAMINA, --   char    1
			CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END  AS IND_LIQUID, --char](2) NOT NULL, *********  PM
		    '' AS IND_LUGAR_EMI,    --char    1
		  	'' AS IND_MADUREZ,     -- char    1
			'' AS IND_MODIF, --char](1) NOT NULL,
			CASE WHEN coalesce (left(TRA."IdTransaccionOd", 2), 0) = 0 then '' ELSE 'OD' END AS IND_OD, --char](2) NOT NULL, RECICLADA 
			'N' AS IND_RESERVADO, --char](1) NOT NULL,
			CAST(TRA."Monto" AS DECIMAL(17,2)) AS MONTO, --decimal](17, 2) NOT NULL, *********
		    CAST(TRA."Monto" AS DECIMAL(17,2)) MONTO_CP,  --decimal 9 
			TRA."Nemotecnico" AS NEMO, --char](16) NOT NULL,  ********* REVISAR
		    CASE WHEN TRA."Plazo" > 9999 THEN 9999 ELSE TRA."Plazo" END   AS PLAZO, --decimal](4, 0) NOT NULL, 
		    CASE WHEN coalesce (TRA."EsFueraRueda", 'S') = 'S' THEN 'N' ELSE 'S' END AS RUEDA, --char](2) NOT NULL, *********  T1
		    CAST(TRA."Tasa" AS DECIMAL(10,6)) AS TASA, --   decimal    5
		    CAST(CASE WHEN TRA."ValorRescate"  > 99999999999 THEN -99999999999 ELSE TRA."ValorRescate" END AS DECIMAL(15,4)) AS VAL_RESC, --    decimal    9
		    
		    CAST(COALESCE(CASE WHEN ASIG."valorRescateAsignado"  > 99999999999 THEN -99999999999 ELSE ASIG."valorRescateAsignado" END,0) AS DECIMAL(15,4)) AS VAL_RESC_ASIG, --    decimal    9
		    
		    --coalesce(ASIG."valorRescateAsignado", 0)  AS VAL_RESC_ASIG, --   decimal    9
		    CASE WHEN TRA."CodigoOperadorCorredor" is null THEN 0 
				when LENGTH(TRIM(TRA."CodigoOperadorCorredor")) = 0 THEN 0
				WHEN TRA."CodigoOperadorCorredor" < 100 THEN TRA."CodigoOperadorCorredor" ELSE 0 END AS OPE_INT, --   decimal    5
		     0 AS SEC_MOVTO_FAC, --decimal](8, 0) NOT NULL,
		    TRA."EsFungible" AS IND_FUNGIBILIDAD,    -- char    1
		    '' AS FEC_VCTO_TP, --char](10) NOT NULL,
		    'N' AS IND_SIMUL, --char](2) NOT NULL,
			 0 AS TASA_SIMUL, --decimal](7, 4) NOT NULL,
			'' AS FOLIO_SIMUL, --char](8) NOT NULL,
		     CAST(TRA."Tasa" AS DECIMAL(12,6)) AS TASA_EMISION, --   decimal    9
		     0 AS SPREAD_TASA_EMISION, --   decimal    9
		     '' AS MODALIDAD_PAGO,  --  char    1
		     '' AS  PERIODICIDAD_PAGO,    --char    1
		     TRA."FechaEmision" AS FECHA_EMISION,  --  char    10
		
		     0 AS PRECIO,    --decimal    5
		     TRA."ValorParidad" AS PARIDAD_MORIGEN, --decimal](15, 6) NOT NULL,
			 TRA."Monto" AS CAPTACION_MORIGEN, --decimal](18, 2) NOT NULL,
		
		     0 AS CODCATIFRSID, --tinyint] NOT NULL,
		     coalesce (TRA."IdMonedaEmision", 'CLP') AS MON_MORIGEN, --char](3) NOT NULL,
			 '' AS FCREOF_COM, --char](26) NOT NULL,
			 '' AS FCREOF_VEN, --char](26) NOT NULL,
		     0 AS CAN_COM, --decimal](9, 0) NOT NULL,
		     0 AS PRECIO_COM, --decimal](15, 4) NOT NULL,
			'' AS BOLSA_COM, --char](3) NOT NULL,
		     0 AS CAN_VEN, --decimal](9, 0) NOT NULL,
		     0 AS PRECIO_VEN, --decimal](15, 4) NOT NULL,
			 '' AS BOLSA_VEN --char](3) NOT NULL,
		
		FROM "BSCL.Orden.BD::ORD.Transaccion"  TRA 
		LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  TOPE."IdTipoOperacion" = TRA."IdTipoOperacion" 
		LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" AS CORRES ON CORRES."IdCorredorCorresponsal" = TRA."IdCorredorCorresponsal"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" CORRCONTRA ON TRA."IdCorredorContraparte" = CORRCONTRA."IdCorredora"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CODCORRCONTRA ON CODCORRCONTRA."IdCorredora" = CORRCONTRA."IdCorredora" AND CODCORRCONTRA."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" CORR ON TRA."IdCorredorLocal" = CORR."IdCorredora"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CODCORR ON CODCORR."IdCorredora" = CORR."IdCorredora" AND CODCORR."IdBolsa" = TRA."IdBolsa"
		
		LEFT JOIN (
				  SELECT  ASI."IdTransaccion", SUM( ASI."ValorRescateAsignado") AS  "valorRescateAsignado"
				  FROM "BSCL.Orden.BD::ORD.Asignacion" as ASI
				  GROUP BY ASI."IdTransaccion"
				  )
		          as ASIG ON (TRA."IdTransaccion" = ASIG."IdTransaccion")
		
		WHERE TRA."IdClasificacionTipoInstrumento" = 'IF' AND TRA."IdEstado" != 'ANU'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLTBIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
