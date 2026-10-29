SELECT 
GRUPO_DIRECCION_DETALLE."IdGrupoDireccionDetalle"
,GRUPO_DIRECCION_DETALLE."IdDireccionElectronica"
,GRUPO_DIRECCION_DETALLE."IdTipoDestinatario"
,GRUPO_DIRECCION_DETALLE."IdGrupoDireccion"
,GRUPO_DIRECCION."IdEnrolamiento"
,GRUPO_DIRECCION_DETALLE."IdGrupoDireccionPersona"
,GRUPO_DIRECCION_DETALLE."IdGrupoDireccionDetallePersona"
FROM "BSCL.SocioNegocio.BD::ENR.GrupoDireccionDetalle" AS GRUPO_DIRECCION_DETALLE
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.GrupoDireccion" AS GRUPO_DIRECCION ON GRUPO_DIRECCION."IdGrupoDireccion" = GRUPO_DIRECCION_DETALLE."IdGrupoDireccion" 
WHERE GRUPO_DIRECCION."IdEnrolamiento" IN (:IdEnrolamiento);
