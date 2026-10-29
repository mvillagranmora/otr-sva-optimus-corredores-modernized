SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" AS

SELECT
	ED."IdEstadoTasa",
	ED."IdEstado",
	ED."NombreEntidad",
	ED."Descripcion"
FROM "BSCL.Plataforma.BD::TAS.EstadoTasa" AS ED;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
