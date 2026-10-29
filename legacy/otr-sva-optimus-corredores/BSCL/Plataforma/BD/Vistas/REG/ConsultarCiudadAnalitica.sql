SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudadAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudadAnalitica" AS
SELECT
	p."NombrePais",
	c."NombreCiudad",
	cdp."NombreClasificacionDivisionPais",
	p."NombreDivisionPais" AS "NombreDivisionPais_1",
	dp."NombreDivisionPais" AS "NombreDivisionPais",
	p."CodigoPais",
	cdp."TipoClasificacionDivisionPais",
	c."IdCiudad",
	p."IdPais"
FROM "BSCL.Plataforma.BD::REG.Ciudad" c 
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" p ON p."IdPais" = c."IdPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" dp ON c."IdCiudad" = dp."IdCiudad"
LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" cdp ON dp."IdClasificacion1" = cdp."IdClasificacionDivisionPais";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudadAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;