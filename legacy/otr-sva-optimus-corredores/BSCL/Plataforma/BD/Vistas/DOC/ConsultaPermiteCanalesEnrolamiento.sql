
SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultaPermiteCanalesEnrolamiento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultaPermiteCanalesEnrolamiento"  
AS
SELECT CanalDoc."IdTipoCanalIngreso" as "IdTipoCanal", CanalDoc."IdTipoDocumento" , TipoCanal."Nombre" 
FROM "BSCL.Plataforma.BD::DOC.CanalesEnrolamientoDocumento" as CanalDoc
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TipoCanal ON  CanalDoc."IdTipoCanalIngreso"  = TipoCanal."IdTipoCanalIngreso" ;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultaPermiteCanalesEnrolamiento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;