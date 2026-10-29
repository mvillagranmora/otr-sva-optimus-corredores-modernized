SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPaisAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPaisAnalitica" AS  
SELECT 
	"IdDivisionPais",
	"IdPais",
	"NombreDivisionPais",
	"IdClasificacion2",
	"IdClasificacion1",
	"IdCiudad"
FROM "BSCL.Plataforma.BD::REG.DivisionPais";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPaisAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
