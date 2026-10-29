SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza" AS

	SELECT
		PLA."IdPlaza",
		PLA."Codigo",
		PLA."NombrePlaza",
		PLA."PlazoRetencionMismoBanco",
		PLA."PlazoRetencionOtroBanco",
		PLA."PlazoMaximoCobroMismoBanco",
		PLA."PlazoMaximoCobroOtroBanco",
		PLA."IdPais",
		PAI."NombrePais",
		PLA."IdCiudad",
		CIU."NombreCiudad",
		PLA."IdEstado"
	FROM  "BSCL.Plataforma.BD::BAN.Plaza" AS PLA
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON  (PAI."IdPais" = PLA."IdPais")
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON (CIU."IdCiudad" = PLA."IdCiudad");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

