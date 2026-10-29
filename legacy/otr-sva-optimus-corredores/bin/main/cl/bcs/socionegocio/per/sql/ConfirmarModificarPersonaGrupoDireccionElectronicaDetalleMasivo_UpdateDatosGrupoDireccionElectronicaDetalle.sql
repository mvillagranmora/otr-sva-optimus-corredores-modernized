UPDATE GDD
SET GDD."IdTipoDestinatario"     = COALESCE(:IdTipoDestinatario, GDD."IdTipoDestinatario")
  , GDD."IdEstado"               = COALESCE(:IdEstado, GDD."IdEstado")
  , GDD."IdEstadoAutorizacion"   = COALESCE(:IdEstadoAutorizacion, GDD."IdEstadoAutorizacion")
  , GDD."Borrado"                = CASE WHEN :IdEstado = 'INA' THEN 'S' ELSE 'N' END
  , GDD."FechaModificacion"      = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" GDD
WHERE GDD."IdGrupoDireccionDetalle" = :IdGrupoDireccionDetalle;