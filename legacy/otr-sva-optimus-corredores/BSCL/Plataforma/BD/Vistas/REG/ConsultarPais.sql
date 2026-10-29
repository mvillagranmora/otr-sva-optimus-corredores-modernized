SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" AS
	SELECT 
		PAIS."IdPais" , 
		PAIS."NombrePais", 
		PAIS."CodigoPais",
		PAIS."UsaDivisionPais",
		PAIS."NombreDivisionPais",
		PAIS."EsOcde",
		PAIS."EsPaisUs"
	FROM "BSCL.Plataforma.BD::REG.Pais" AS PAIS;
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
