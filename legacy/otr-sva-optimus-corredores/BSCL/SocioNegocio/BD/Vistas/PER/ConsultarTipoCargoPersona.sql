SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoCargoPersona"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoCargoPersona"  AS 
SELECT 
	TC."IdTipoCargoPersona", 
	TC."CodigoTipoCargoPersona", 
	TC."NombreTipoCargoPersona",
	TC."IdEmpresa",
	TC."UtilizaDetalle"  
FROM 
"BSCL.SocioNegocio.BD::PER.TipoCargoPersona" AS TC
WHERE TC."Borrado"='N';
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoCargoPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 