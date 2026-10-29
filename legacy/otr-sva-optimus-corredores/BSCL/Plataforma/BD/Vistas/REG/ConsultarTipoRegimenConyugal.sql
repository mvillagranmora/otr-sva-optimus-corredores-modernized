SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoRegimenConyugal"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoRegimenConyugal"  
AS
SELECT "TipoRegimenConyugal_$0"."IdTipoRegimenConyugal" , "TipoRegimenConyugal_$0"."Nombre" , "Pais_$1"."IdPais" , "Pais_$1"."CodigoPais" FROM  ( "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" AS "TipoRegimenConyugal_$0" LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$1" ON  ( "Pais_$1"."IdPais" = "TipoRegimenConyugal_$0"."IdPais" )  );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoRegimenConyugal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 