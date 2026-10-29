SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisAnalitica" AS
SELECT
	"IdPais",
	"NombrePais",
	"CodigoPais"
FROM "BSCL.Plataforma.BD::REG.Pais";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;