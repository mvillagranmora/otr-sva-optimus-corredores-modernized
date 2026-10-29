SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocioPortafolioBloqueo";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocioPortafolioBloqueo" 
AS
SELECT 
"IdTipoBloqueo",
"NombreTipoBloqueo"
FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
WHERE "IdTipoEntidad" = 'PORTAFOLIO' 
AND "IdEstado" = 'ACT'
AND "IdAccion" = 'BLO';
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocioPortafolioBloqueo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO