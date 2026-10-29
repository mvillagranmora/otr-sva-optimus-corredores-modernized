SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoMonedaInstrumento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoMonedaInstrumento"
AS
WITH 
	"MonedaInstrumento" AS (
		SELECT 
			 inst."IdInstrumento"
			,inst."IdMoneda"
			,inst."IdEstado"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
		WHERE tipoins."IdClasificacionTipoInstrumento" in ('RV','RF')
	),
	"MonedaInstrumentoIF" AS (
		SELECT 
			 inst."IdInstrumento"
			,MOEM."IdMoneda"
			,inst."IdEstado"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
		CROSS JOIN (
				SELECT 'CLP' AS "IdMoneda" FROM DUMMY UNION ALL
				SELECT 'USD' AS "IdMoneda" FROM DUMMY UNION ALL
				SELECT 'EUR' AS "IdMoneda" FROM DUMMY UNION ALL
				SELECT 'UF'  AS "IdMoneda" FROM DUMMY
			) AS MOEM
		WHERE tipoins."IdClasificacionTipoInstrumento" = 'IF'
	)
SELECT *
FROM (
	SELECT "IdInstrumento", "IdMoneda", "IdEstado" FROM "MonedaInstrumento"
	UNION ALL
	SELECT "IdInstrumento", "IdMoneda", "IdEstado" FROM "MonedaInstrumentoIF"
)				   
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoMonedaEmision" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO