SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoEstadoCivil"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoEstadoCivil"  
AS
SELECT "TipoEstadoCivil_$0"."IdTipoEstadoCivil" , "TipoEstadoCivil_$0"."Nombre" , "Pais_$1"."IdPais" , "Pais_$1"."CodigoPais" FROM  ( "BSCL.Plataforma.BD::REG.TipoEstadoCivil" AS "TipoEstadoCivil_$0" LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$1" ON  ( "Pais_$1"."IdPais" = "TipoEstadoCivil_$0"."IdPais" )  );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoEstadoCivil" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 