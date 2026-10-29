SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMTASASCURVA"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMTASASCURVA" AS

SELECT * FROM (
	SELECT 
		RANK() OVER (PARTITION BY PT."IdTipoTasa",FU."Codigo",LEFT(PT."FechaCreacion",10),PT."IdMoneda",PT."IdClasificacionTipoInstrumento",PT."Plazo",PT."Nemotecnico" ORDER BY PT."IdPrecioTasa" DESC) AS RANK,
		COALESCE (CAST(PT."IdPrecioTasa" AS VARCHAR), '') 			        		                AS IDPRECIOTASA,
		RPAD(COALESCE(PT."IdTipoTasa", ''),10)     		                AS TIPOTASA, 
		RPAD(COALESCE(FU."Codigo", ''),10)         		                AS FUENTE, 
		CAST(COALESCE(PT."FechaCreacion",'1900-01-01') AS CHAR (10))                AS FECHA,
		CAST(COALESCE(CASE PT."IdMoneda"
			WHEN '' THEN 0
			WHEN 'BRL' THEN 5
			WHEN 'CAD' THEN 6
			WHEN 'USD' THEN 13
			WHEN 'SOL' THEN 24
			WHEN 'AUD' THEN 36
			WHEN 'RMB' THEN 48
			WHEN 'DKK' THEN 51
			WHEN 'MDP' THEN 68
			WHEN 'JPY' THEN 72
			WHEN 'CHF' THEN 82
			WHEN 'NOK' THEN 96
			WHEN 'NZD' THEN 97
			WHEN 'GBP' THEN 102
			WHEN 'SEK' THEN 113
			WHEN 'HKD' THEN 127
			WHEN 'MXN' THEN 132
			WHEN 'IC' THEN 133
			WHEN 'EUR' THEN 142
			WHEN 'PCL' THEN 212
			WHEN 'UTM' THEN 503
			WHEN 'DI' THEN 988
			WHEN 'DO' THEN 994
			WHEN 'DA' THEN 995
			WHEN 'UF' THEN 998
			WHEN 'CLP' THEN 999
			WHEN 'COP' THEN 170
			WHEN 'PEN' THEN 604
			ELSE 0
		END, 0) AS INT)                                                             AS MONEDA,
		RPAD(COALESCE(PT."IdClasificacionTipoInstrumento", ''),10)	    AS MERCADO,
		CAST(COALESCE(PT."Plazo", 0) AS INT)					                    AS PERIODO,
		CAST(COALESCE(CASE 
			WHEN PT."FechaPublicacion" = '0001-01-01' THEN '1900-01-01' ELSE PT."FechaPublicacion" 
			END,'1900-01-01') AS CHAR(10))	            AS FEC_VCTO,
		CAST(COALESCE(PT."Tasa", 0)  AS DECIMAL(20,10))						        AS TASA,
		0 											                                AS TASAMID,
		0                                                                           AS TASABID,
		0 											                                AS TASAASK,
		0 											                                AS TASAVPAR,
		CAST(COALESCE(PT."TasaEstimada", 0) AS DECIMAL(20,10))					    AS TASAEST,
		0 											                                AS TASAESTMIN,
		0 											                                AS TASAESTMAX,
		CAST(COALESCE(PT."NombreArchivo",'') AS CHAR(40))		                    AS ARCHIVO,
		CAST(COALESCE(US."CodigoUsuario", '') AS CHAR(20))		                    AS UsrUltMod,
		COALESCE(PT."FechaModificacion",'1900-01-01')	                            AS FecUltMod,
		CAST('R'AS CHAR(1))		                                                    AS EstadoCarga,
		CAST(0 AS DECIMAL(17,2))                                                    AS MONTO,
		RPAD(COALESCE(PT."Nemotecnico",''),16)                             AS NEMO
		
	FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" PT 
	LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" US ON PT."IdUsuarioModificacion" = US."IdUsuario"
	LEFT JOIN  "BSCL.Plataforma.BD::TAS.Fuente" FU ON PT."IdFuente" = FU."IdFuente"
	WHERE PT."IdTipoTasa" LIKE 'CURVA'
) WHERE RANK = 1

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMTASASCURVA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
