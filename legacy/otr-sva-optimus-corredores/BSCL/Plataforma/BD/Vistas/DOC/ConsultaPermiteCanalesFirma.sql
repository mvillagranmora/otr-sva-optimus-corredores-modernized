
SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultaPermiteCanalesFirma"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultaPermiteCanalesFirma"  
AS
SELECT CanalDoc."IdTipoCanalIngreso", CanalDoc."IdTipoDocumento" , TipoCanal."Nombre" 
FROM "BSCL.Plataforma.BD::DOC.CanalesDocumento" as CanalDoc
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TipoCanal ON  CanalDoc."IdTipoCanalIngreso"  = TipoCanal."IdTipoCanalIngreso" ;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultaPermiteCanalesFirma" TO  BCS_DESARROLLADOR WITH GRANT OPTION;