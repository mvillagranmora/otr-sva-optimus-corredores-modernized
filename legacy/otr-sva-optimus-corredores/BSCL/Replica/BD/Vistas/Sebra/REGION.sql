SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.REGION"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.REGION" AS

	SELECT 
	DISTINCT
		DP."IdClasificacionDivisionPais" AS "REG_ID",-- INT 
		CAST(DP."NombreClasificacionDivisionPais" AS VARCHAR(100)) AS "REG_NOMBRE"

	FROM "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" DP
	JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON (PAIS."IdPais" = 44 AND 
					"TipoClasificacionDivisionPais" = 'Región' )
	--DP."IdClasificacionDivisionPais" < 15


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.REGION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
