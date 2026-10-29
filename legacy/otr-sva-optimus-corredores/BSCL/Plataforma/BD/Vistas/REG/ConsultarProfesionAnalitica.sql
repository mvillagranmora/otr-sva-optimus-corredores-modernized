SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesionAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesionAnalitica" AS
SELECT
	"IdProfesion",
	"Nombre",
	"IdPais",
	"IdEstado"
FROM "BSCL.Plataforma.BD::REG.Profesion";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;