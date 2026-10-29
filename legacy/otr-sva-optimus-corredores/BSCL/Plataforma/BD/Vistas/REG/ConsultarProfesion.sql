SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesion"  
AS
SELECT "Profesion_$0"."IdProfesion" , "Profesion_$0"."Codigo" , "Profesion_$0"."Nombre" , "Pais_$1"."IdPais" , "Pais_$1"."CodigoPais" FROM  ( "BSCL.Plataforma.BD::REG.Profesion" AS "Profesion_$0" LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$1" ON  ( "Pais_$1"."IdPais" = "Profesion_$0"."IdPais" )  );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 