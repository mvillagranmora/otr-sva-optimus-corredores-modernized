SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaRegion";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaRegion" as
SELECT 
 DISTINCT
 DP."IdClasificacion1" AS reg_id,-- INT 
 DP."IdClasificacion1" AS reg_nombre
FROM "BSCL.Plataforma.BD::REG.DivisionPais" DP
JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD ON CIUDAD."IdPais" = DP."IdPais" AND CIUDAD."IdCiudad" = DP."IdCiudad" 
JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON DP."IdPais" = PAIS."IdPais" 
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaRegion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO