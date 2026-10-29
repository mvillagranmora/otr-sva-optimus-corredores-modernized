UPDATE GD
SET GD."Borrado"           = COALESCE(:Borrado, GD."Borrado")
  , GD."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion" GD
WHERE GD."IdGrupoDireccion" IN (:IdEntidadPersonaEliminar);