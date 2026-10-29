SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoContrato"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoContrato" AS
SELECT
	  "IdTipoContrato",
	  "Nombre",
	  "IdEstado"
FROM "BSCL.Plataforma.BD::COM.TipoContrato";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoContrato" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 