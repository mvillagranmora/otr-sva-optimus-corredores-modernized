SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoMonedaEmision"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoMonedaEmision"
AS
WITH 
	"MonedaEmisionRF" AS (
		SELECT 
			 inst."IdInstrumento"
			,inst."IdMonedaEmision"
			,inst."IdEstado"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
		WHERE tipoins."IdClasificacionTipoInstrumento" = 'RF'
	),
	"MonedaEmisionIF" AS (
		SELECT 
			 inst."IdInstrumento"
			,MOEM."IdMonedaEmision"
			,inst."IdEstado"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
		CROSS JOIN (
				SELECT 'CLP' AS "IdMonedaEmision" FROM DUMMY UNION ALL
				SELECT 'USD' AS "IdMonedaEmision" FROM DUMMY UNION ALL
				SELECT 'EUR' AS "IdMonedaEmision" FROM DUMMY UNION ALL
				SELECT 'UF'  AS "IdMonedaEmision" FROM DUMMY
			) AS MOEM
		WHERE tipoins."IdClasificacionTipoInstrumento" = 'IF'
	)
SELECT *
FROM (
	SELECT "IdInstrumento", "IdMonedaEmision", "IdEstado" FROM "MonedaEmisionRF"
	UNION ALL
	SELECT "IdInstrumento", "IdMonedaEmision", "IdEstado" FROM "MonedaEmisionIF"
)				   
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoMonedaEmision" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO