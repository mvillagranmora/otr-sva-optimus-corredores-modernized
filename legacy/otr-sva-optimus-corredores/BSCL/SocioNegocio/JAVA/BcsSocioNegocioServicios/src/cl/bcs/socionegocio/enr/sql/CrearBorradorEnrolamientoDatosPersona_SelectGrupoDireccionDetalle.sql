SELECT 
"IdGrupoDireccionDetalle"
,"IdDireccionElectronica"
,"IdTipoDestinatario"
,"IdGrupoDireccion"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle"
WHERE "IdGrupoDireccion" IN (:IdGrupoDireccion) AND "Borrado" = 'N';