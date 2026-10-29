SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ENC.ConsultarPuntajesEncuesta"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ENC.ConsultarPuntajesEncuesta" 
AS 
SELECT d."IdEmpresa",d."IdCategoriaInversionista",d."PuntajeHasta",c."NombreCategoriaInversionista"
FROM "BSCL.Plataforma.BD::ENC.DefinicionPuntaje" d
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" c on d."IdCategoriaInversionista" = c."IdCategoriaInversionista" AND d."IdEmpresa" = c."IdEmpresa"


;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ENC.ConsultarPuntajesEncuesta" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


