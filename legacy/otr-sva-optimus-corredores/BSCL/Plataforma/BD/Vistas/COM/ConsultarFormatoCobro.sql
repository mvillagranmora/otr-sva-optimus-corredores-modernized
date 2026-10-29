SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarFormatoCobro"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarFormatoCobro" AS
SELECT
	  "IdFormatoCobro",
	  "Nombre",
	  "IdEstado"
FROM "BSCL.Plataforma.BD::COM.FormatoCobro";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarFormatoCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 