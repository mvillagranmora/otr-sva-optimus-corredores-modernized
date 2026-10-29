SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocioPortafolioDesbloqueo";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocioPortafolioDesbloqueo" 
AS
SELECT 
"IdTipoBloqueo",
"NombreTipoBloqueo"
FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
WHERE "IdTipoEntidad" = 'PORTAFOLIO' 
AND "IdEstado" = 'ACT'
AND "IdAccion" = 'ACT';
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocioPortafolioDesbloqueo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO