SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPais";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPais" AS
SELECT
	"IdClasificacionDivisionPais"
   ,"NombreClasificacionDivisionPais"
   ,"TipoClasificacionDivisionPais"
   ,"IdPais"
   ,"Codigo"
FROM "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais";
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPais" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO