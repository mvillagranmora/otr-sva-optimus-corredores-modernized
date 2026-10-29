SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRF" AS

		SELECT
		    --top 10
		    TRA."IdTransaccion",
			CAST(TO_VARCHAR (UTCTOLOCAL(TRA."FechaOperacion",'America/Santiago','platform'),'YYYY-MM-DD:HH24:MI:SS.FF7') AS VARCHAR(26)) AS FEC_TRANS, --char](26) NOT NULL, *********
			LPAD(LEFT(TRA."Folio",8),8) AS FOLIO_TRANS, --char](8) NOT NULL, *********
			TRA."IdBolsa" AS IND_BOLSA, --char](3) NOT NULL,
			substr(TOPE."AccionOperacion",1,1) AS TIPO_OPERAC, --char](1) NOT NULL, C/V
			TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FECHA, --char](10) NOT NULL, *********
			TRA."Nemotecnico" AS NEMO, --char](16) NOT NULL,  *********
			coalesce (ASIG."CantidadAsignada", 0) AS CAN_ASIG_ACUM, --decimal](14, 2) NOT NULL,
			0 AS CAN_CP, --decimal](14, 2) NOT NULL,
			TRA."Cantidad" AS CANTIDAD, --decimal](14, 2) NOT NULL, *********
			COALESCE(CODCORRCONTRA."CodigoCorredora",'0') AS COD_CORR_CONTRA, --decimal](3, 0) NOT NULL, ********* 82
			case when TRA."CodigoOperadorCorredor" is null then CODCORR."CodigoCorredora" when LENGTH(TRIM(TRA."CodigoOperadorCorredor")) = 0 then CODCORR."CodigoCorredora" when TRA."CodigoOperadorCorredor" > 100 then TRA."CodigoOperadorCorredor" else CODCORR."CodigoCorredora" end AS COD_OPE_EXT, --decimal](3, 0) NOT NULL, ********* 42 PREGUNTAR ????
			'' AS EMISOR_NO_INS, --char](10) NOT NULL,
			TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FEC_DIG, --char](10) NOT NULL,
			'' AS IND_BOLSON, --char](1) NOT NULL,
			'N' AS IND_COMPROMISO, --char](1) NOT NULL,
			'N' AS IND_CTA_PRO, --char](1) NOT NULL,
			TRA."IndicadorDCV" AS IND_DCV, --char](1) NOT NULL,  ********* D
			CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END  AS IND_LIQUID, --char](2) NOT NULL, *********  PM
			'' AS IND_MODIF, --char](1) NOT NULL,
			CASE WHEN coalesce (left(TRA."IdTransaccionOd", 2), 0) = 0 then '' ELSE 'OD' END AS IND_OD, --char](2) NOT NULL, RECICLADA 
			'N' AS IND_RESERVADO, --char](1) NOT NULL,
			TRA."Monto" AS MONTO, --decimal](17, 2) NOT NULL, *********
			TRA."Plazo" AS PLAZO, --decimal](4, 0) NOT NULL, 
			0 AS PLAZO_CORA_PRTP, --decimal](2, 0) NOT NULL,
			TRA."Precio" AS PRECIO, --decimal](5, 2) NOT NULL, *********
			CASE WHEN coalesce (TRA."EsFueraRueda", 'S') = 'S' THEN 'N' ELSE 'S' END AS RUEDA, --char](2) NOT NULL, *********  T1
			'' AS TIPO_CALCULO, --char](2) NOT NULL,
			TRA."Tasa" AS TIR, --decimal](5, 2) NOT NULL, *********
			CASE WHEN TRA."CodigoOperadorCorredor" is null THEN 0 
				when LENGTH(TRIM(TRA."CodigoOperadorCorredor")) = 0 THEN 0
				WHEN TRA."CodigoOperadorCorredor" < 100 THEN TRA."CodigoOperadorCorredor" ELSE 0 END AS OPE_INT, --decimal](3, 0) NOT NULL, *********  3
			0 AS SEC_MOVTO_FAC, --decimal](8, 0) NOT NULL,
			'' AS FEC_VCTO_TP, --char](10) NOT NULL,
			'' AS IND_SIMUL, --char](2) NOT NULL,
			0 AS TASA_SIMUL, --decimal](7, 4) NOT NULL,
			'' AS FOLIO_SIMUL, --char](8) NOT NULL,
			0 AS CODCATIFRSID, --tinyint] NOT NULL,
			TRA."ValorParidad" AS PARIDAD_MORIGEN, --decimal](15, 6) NOT NULL,
			TRA."Monto" AS CAPTACION_MORIGEN, --decimal](18, 2) NOT NULL,
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
				  SELECT  ASI."IdTransaccion", SUM( ASI."CantidadAsignada") AS  "CantidadAsignada"
				  FROM "BSCL.Orden.BD::ORD.Asignacion" as ASI
				  GROUP BY ASI."IdTransaccion"
				  )
		          as ASIG ON (TRA."IdTransaccion" = ASIG."IdTransaccion")
		WHERE TRA."IdClasificacionTipoInstrumento" = 'RF' AND TRA."IdEstado" != 'ANU'
 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
