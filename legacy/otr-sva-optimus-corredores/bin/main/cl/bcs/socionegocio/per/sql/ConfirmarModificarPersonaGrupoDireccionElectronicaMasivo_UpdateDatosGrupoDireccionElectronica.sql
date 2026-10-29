UPDATE GD
SET GD."Grupo"                = COALESCE(:Grupo, GD."Grupo")
  , GD."Comentario"           = COALESCE(:Comentario, GD."Comentario")
  , GD."IdEstado"             = COALESCE(:IdEstado, GD."IdEstado")
  , GD."IdEstadoAutorizacion" = COALESCE(:IdEstadoAutorizacion, GD."IdEstadoAutorizacion")
  , GD."FechaModificacion"    = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion" GD
WHERE GD."IdGrupoDireccion" = :IdGrupoDireccion;