SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRV" AS

SELECT 
		    TRA."IdTransaccion",
			LEFT(CASE COALESCE( hpr."ValorOrigen", TRA."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						ELSE COALESCE( hpr."ValorOrigen", TRA."IdTipoInstrumento") END,2)  AS MERCADO, --2 
			
		    CAST(TO_VARCHAR (UTCTOLOCAL(TRA."FechaOperacion",'America/Santiago','platform'),'YYYY-MM-DD:HH24:MI:SS.FF7') AS VARCHAR(26)) AS FEC_TRANS, --char](26) NOT NULL, ********* 
		    LPAD(LEFT(TRA."Folio",8),8) AS FOLIO_TRANS, --char](8) NOT NULL, *********
			TRA."IdBolsa" AS IND_BOLSA, --char](3) NOT NULL,
			COALESCE(substr(TOPE."AccionOperacion",1,1),'') AS TIPO_OPERAC, --char](1) NOT NULL, C/V
			TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FECHA, --char](10) NOT NULL, *********
			LEFT(CASE WHEN TRA."IdTipoInstrumento" = 'SIM' THEN TRA."NemotecnicoSubyacente" ELSE TRA."Nemotecnico" END,16) AS NEMO, --char](16) NOT NULL,  ********* REVISAR
		    0 AS SEC_TRASPASO, --DECIMAL 8
		    coalesce (ASIG."CantidadAsignada", 0) AS CAN_ASIG_ACUM, -- DECIMAL 12 
		    0 AS CAN_CP, -- DECIMAL 12
		    TRA."Cantidad" AS CANTIDAD, --decimal](14, 2) NOT NULL, *********
		    COALESCE(CODCORRCONTRA."CodigoCorredora",'0') AS COD_CORR_CONTRA, --decimal](3, 0) NOT NULL, ********* 82
			case 
				when TRA."CodigoOperadorCorredor" is null then COALESCE(CODCORR."CodigoCorredora",0)
				when LENGTH(TRIM(TRA."CodigoOperadorCorredor")) = 0 then COALESCE(CODCORR."CodigoCorredora",0)
				when TRA."IdSistemaBursatil" = 'BEC' then COALESCE(CODCORR."CodigoCorredora",0)
				when TRA."CodigoOperadorCorredor" > 100 then TRA."CodigoOperadorCorredor" 
				else COALESCE(CODCORR."CodigoCorredora",0) end AS COD_OPE_EXT, --decimal](3, 0) NOT NULL, ********* 
		    '' AS EMISOR_NO_INS, -- CHAR 10
		    TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FEC_DIG, --char](10) NOT NULL,0   x fecha operacion 
		    TO_VARCHAR (TRA."FechaVencimiento",'YYYY-MM-DD') AS FEC_VCTO_TP, --char](10) NOT NULL, x vencimiento 
			COALESCE(LPAD(LEFT(FO_SIM."Folio",8),8),'') AS FOLIO_SIMUL, --char](8) NOT NULL, x relacion 0
		    'N'  AS IND_CTA_PRO, --char](1) NOT NULL,   
		    TRA."IndicadorDCV" AS IND_DCV, --char](1) NOT NULL,  ********* D
		     CASE WHEN TRA."EsAPlazo" = 'S' THEN 'TP' 
		     ELSE
		     	CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END 
		     END AS IND_LIQUID, --char](2) NOT NULL, *********  PM
		   '' AS IND_MODIF, --char](1) NOT NULL,
		   CASE WHEN coalesce (left(TRA."IdTransaccionOd", 2), 0) = 0 then '' ELSE 'OD' END AS IND_OD, --char](2) NOT NULL,
		   'N' AS IND_RESERVADO, --char](1) NOT NULL,
		   CASE WHEN TRA."EsAPlazo" = 'S' THEN 'TS' ELSE '' END AS IND_SIMUL, --char](2) NOT NULL,  x esAplazo
		   CAST(TRA."Monto" AS DECIMAL(17,2)) AS MONTO, --decimal](17, 2) NOT NULL, *********
		   TRA."Precio" AS PRECIO,    --decimal    5
		   0 as REDONDEL, --decimal 1
		   CASE WHEN coalesce (TRA."EsFueraRueda", 'S') = 'S' THEN 'N' ELSE 'S' END AS RUEDA, --char](2) NOT NULL, *********  T1
		   CASE WHEN TRA."Tasa" > 999 THEN 999 ELSE TRA."Tasa" END AS TASA, --   decimal    5
		   CASE WHEN TRA."CodigoOperadorCorredor" is null THEN 0 
				when LENGTH(TRIM(TRA."CodigoOperadorCorredor")) = 0 THEN 0
				when TRA."IdSistemaBursatil" = 'BEC' then 0
				WHEN TRA."CodigoOperadorCorredor" < 100 THEN TRA."CodigoOperadorCorredor" ELSE 0 END AS OPE_INT, --   decimal    5
		   '' AS IND_RUTEO, -- char 1  x como se que un palo es de ruteo
		   LEFT(COALESCE(CASE WHEN substr(TOPE."AccionOperacion",1,1) = 'C' THEN TRA."MarcaNegociacion" ELSE '' END,''),COALESCE(conf."Valor",16)) AS CLIENTE_COM, --char 16
		   LEFT(COALESCE(CASE WHEN substr(TOPE."AccionOperacion",1,1) = 'V' THEN TRA."MarcaNegociacion" ELSE '' END,''),COALESCE(conf."Valor",16))  AS CLIENTE_VEN, -- char 16
		   COALESCE(CASE WHEN substr(TOPE."AccionOperacion",1,1) = 'C' then COALESCE(TO_VARCHAR (UTCTOLOCAL(TRA."FechaOperacion",'America/Santiago','platform'),'YYYY-MM-DD:HH24:MI:SS.FF3'),'0001-01-01 00:00:00') ELSE '' end,'') AS FCREOF_COM, --char](26) NOT NULL,
		   COALESCE(CASE WHEN substr(TOPE."AccionOperacion",1,1) = 'V' then COALESCE(TO_VARCHAR (UTCTOLOCAL(TRA."FechaOperacion",'America/Santiago','platform'),'YYYY-MM-DD:HH24:MI:SS.FF3'),'0001-01-01 00:00:00') ELSE '' end,'') AS FCREOF_VEN, --char](26) NOT NULL,
		   0 AS CAN_COM, --decimal](9, 0) NOT NULL,
		   0 AS PRECIO_COM, --decimal](15, 4) NOT NULL,
		   '' AS BOLSA_COM, --char](3) NOT NULL,
		   0 AS CAN_VEN, --decimal](9, 0) NOT NULL,
		   0 AS PRECIO_VEN, --decimal](15, 4) NOT NULL,
		   '' AS BOLSA_VEN, --char](3) NOT NULL,
		  0  AS USUARIO_SOL,	--DECIMAL	7
		   'N' AS IND_PREPAGO,	--CHAR	1
		   0 AS SEC_MOVTO_FAC, --decimal](8, 0) NOT NULL,
		   '' AS USERNAME_RESERVA,	--CHAR	20
		   '' AS IND_AGRES,	--CHAR	1  x  preguntar 
		    0 AS CODCATIFRSID, --tinyint] NOT NULL,
		   '' AS HORA_EX,	--CHAR	26
		   '' AS FOLIO_EX,	--CHAR	32
		   TRA."IdMoneda" AS MONEDA,	--CHAR	3  x moneda de origen 
		   0 AS MONTO_ORI,	--DECIMAL	20 monto de la moneda de origen
		   TRA."ValorParidad" AS PARIDAD, -- DECIMA 15   x paridad de la moneda 
		   0 AS COD_CORRESP,	--INTEGER	10  x idcodigocorresponsal 
		   '' AS CORR_ORIGEN, -- CHAR 25 
		   0  AS TCAMBIO_USD,	--DECIMAL	18  x cambio en dolar
		   0  AS MONTO_USD,	--DECIMAL	17  x monton en dolar 
		   '' AS IND_OD_AUTO,	--CHAR	1 preguntar 
		   TRA."IndicadorOrdenDirecta" AS IND_ORDEN_DIRECTA,	--CHAR	2
		   TRA."OrderIdNegociacion" AS ORDER_ID_HT,	--VARCHAR	32 id de la orden ht ?
		   
		   COALESCE(CAST(TRA."PrecioRaiz" AS DECIMAL(17,6)), 0.0) AS "PRECIO_CONVERTIDO",
		   COALESCE(CAST(TRA."MontoRaiz" AS DECIMAL(17,2)), 0.0) AS "MONTO_CONVERTIDO",
		   COALESCE(CAST(TRA."IdMonedaRaiz" AS CHAR(3)), '') AS "MONEDA_CONVERTIDA",
		   0.0 AS "PARIDAD_CONVERTIDA",
		   COALESCE(CAST(INS."Nemotecnico" AS CHAR(16)), '') AS "NEMO_NEGOCIACION"

		   
		
		FROM "BSCL.Orden.BD::ORD.Transaccion"  TRA 
		LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion") 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = TRA."IdTipoOperacion") 
		LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" AS CORRES ON (CORRES."IdCorredorCorresponsal" = TRA."IdCorredorCorresponsal")
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
		                                                                   AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
		                                                                   AND hpr."ValorDestino" = TRA."IdTipoInstrumento"    AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET'))
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS FO_SIM on (TRA."IdTransaccion" = FO_SIM."IdTransaccionPlazo")
		LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" CORRCONTRA ON TRA."IdCorredorContraparte" = CORRCONTRA."IdCorredora"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CODCORRCONTRA ON CODCORRCONTRA."IdCorredora" = CORRCONTRA."IdCorredora" AND CODCORRCONTRA."IdBolsa" = TRA."IdBolsa"
		
		LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" CORR ON TRA."IdCorredorLocal" = CORR."IdCorredora"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CODCORR ON CODCORR."IdCorredora" = CORR."IdCorredora" AND CODCORR."IdBolsa" = TRA."IdBolsa"
		
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON TRA."IdInstrumentoNegociacion" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Replica.BD::ConfiguracionReplica" conf on conf. "CodigoParametro" = 'TBPLTBRV_LargoClientId'
		LEFT JOIN (
				  SELECT  ASI."IdTransaccion", SUM( ASI."CantidadAsignada") AS  "CantidadAsignada"
				  FROM "BSCL.Orden.BD::ORD.Asignacion" as ASI
				  WHERE ASI."IdEstadoAsignacion" != 'ANU'
				  GROUP BY ASI."IdTransaccion"
				  )
		          as ASIG ON (TRA."IdTransaccion" = ASIG."IdTransaccion")
		WHERE TRA."IdClasificacionTipoInstrumento" = 'RV' AND TRA."IdEstado" != 'ANU'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
