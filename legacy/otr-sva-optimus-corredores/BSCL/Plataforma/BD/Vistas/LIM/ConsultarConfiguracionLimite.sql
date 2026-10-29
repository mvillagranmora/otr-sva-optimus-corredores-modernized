SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarConfiguracionLimite"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarConfiguracionLimite"
AS
SELECT
	CONF."IdEmpresa", 
    CONF."Codigo",                   
    CONF."Descripcion",
    CONF."Valor"
FROM "BSCL.Plataforma.BD::LIM.Configuracion" CONF;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarConfiguracionLimite" TO BCS_DESARROLLADOR WITH GRANT OPTION;