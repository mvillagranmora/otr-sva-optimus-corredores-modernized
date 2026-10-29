SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad"  
AS
SELECT 
	CIUDAD."IdCiudad" , 
	CIUDAD."NombreCiudad" ,  
	(  ( CIUDAD."NombreCiudad" || ' - ' )  || PAIS."NombrePais" )  AS "NombreCiudadPais" , 
	PAIS."IdPais" , 
	PAIS."NombrePais" , 
	PAIS."CodigoPais",
	CASE WHEN CLASIF1."IdDivisionPais" <> 0 THEN  CLASIF1."IdDivisionPais" ELSE 0 END AS "IdDivisionPais",
	CASE WHEN CLASIF1."IdDivisionPais" <> 0 THEN  CLASIF1."NombreDivisionPaisClasificacion1" ELSE (CIUDAD."NombreCiudad" || ' - ' ) || PAIS."NombrePais" END AS "NombreCiudadDivisionPais"
FROM  "BSCL.Plataforma.BD::REG.Ciudad" AS CIUDAD
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON (PAIS."IdPais" = CIUDAD."IdPais")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" AS CLASIF1 ON (PAIS."IdPais" = CLASIF1."IdPais" AND CIUDAD."IdCiudad" = CLASIF1."IdCiudad");
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 