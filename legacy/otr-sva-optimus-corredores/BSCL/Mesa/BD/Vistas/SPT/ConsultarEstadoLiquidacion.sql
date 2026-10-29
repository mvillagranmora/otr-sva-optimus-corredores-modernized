SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarEstadoLiquidacion";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarEstadoLiquidacion"
AS 
	SELECT	MES.*,
			PLA."Nombre" ,
			MES."IdEstado" || ' - ' || PLA."Nombre" AS "NombreEstado"
	FROM	"BSCL.Mesa.BD::MES.EstadoOperacion" MES
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" PLA 
			ON MES."IdEstado" = PLA."IdEstado"
	WHERE	"NombreEntidad" = 'LIQUIDACION';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarEstadoLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
