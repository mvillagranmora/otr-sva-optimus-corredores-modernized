SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaComuna";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaComuna" as
SELECT
 DP."IdDivisionPais" AS com_id,-- INT  
 DP."IdClasificacion1" AS reg_id,-- INT 
 PAIS."CodigoPais" AS pai_iso,-- CHAR(2)  
 DP."NombreDivisionPais" AS com_nombre,-- VARCHAR(30)  
 CIUDAD."NombreCiudad" AS com_ciudad,-- VARCHAR(50)  
 '' AS com_postal,-- VARCHAR(5) 
 '' AS com_cod_svs-- INT  
FROM "BSCL.Plataforma.BD::REG.DivisionPais" DP
JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD ON CIUDAD."IdPais" = DP."IdPais" AND CIUDAD."IdCiudad" = DP."IdCiudad" 
JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON DP."IdPais" = PAIS."IdPais" 
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaComuna" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO