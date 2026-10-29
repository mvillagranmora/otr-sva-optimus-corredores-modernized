SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro"
AS  
SELECT "Giro_$0"."IdGiro" , "Giro_$0"."Codigo" , "Giro_$0"."Nombre" , "Pais_$1"."IdPais" , "Pais_$1"."CodigoPais" FROM  ( "BSCL.Plataforma.BD::REG.Giro" AS "Giro_$0" LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$1" ON  ( "Pais_$1"."IdPais" = "Giro_$0"."IdPais" )  );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 