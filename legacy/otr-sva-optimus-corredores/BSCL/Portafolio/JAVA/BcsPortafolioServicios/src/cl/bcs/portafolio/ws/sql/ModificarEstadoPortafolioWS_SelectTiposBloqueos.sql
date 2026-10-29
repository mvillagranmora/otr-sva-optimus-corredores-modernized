SELECT 
"IdTipoBloqueo"
,"IdEstado"
,"IdAccion"
FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
WHERE "IdTipoEntidad" = 'PORTAFOLIO' 
AND "IdEstado" = 'ACT' 
AND "IdTipoBloqueo" = :IdTipo;