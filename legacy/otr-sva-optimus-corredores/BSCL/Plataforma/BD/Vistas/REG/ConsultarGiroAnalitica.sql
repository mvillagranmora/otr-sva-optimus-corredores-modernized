SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiroAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiroAnalitica" AS
SELECT
	"IdGiro",
	"IdPais",
	"Nombre",
	"Codigo"
FROM "BSCL.Plataforma.BD::REG.Giro";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiroAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
