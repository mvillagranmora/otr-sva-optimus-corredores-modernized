SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPaisAnalitica";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPaisAnalitica" AS
SELECT
	"IdClasificacionDivisionPais",
	"IdPais",
	"NombreClasificacionDivisionPais",
	"TipoClasificacionDivisionPais"
FROM "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarClasificacionDivisionPaisAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;