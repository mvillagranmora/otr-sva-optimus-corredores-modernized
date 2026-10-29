UPDATE "BSCL.SocioNegocio.BD::PER.GrupoDireccion"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = IFNULL(:IdEstado, "IdEstado"),
    "Borrado" = IFNULL(:Borrado, "Borrado")
WHERE "IdGrupoDireccion" = :IdGrupoDireccion
