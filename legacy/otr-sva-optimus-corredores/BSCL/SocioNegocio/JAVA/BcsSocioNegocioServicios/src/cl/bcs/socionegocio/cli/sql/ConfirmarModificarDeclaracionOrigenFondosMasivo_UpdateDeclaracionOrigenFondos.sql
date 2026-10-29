UPDATE DOF
SET "IdTipoOperacionOrigenFondos" = COALESCE(:IdTipoOperacionOrigenFondos, DOF."IdTipoOperacionOrigenFondos")
   , "FechaDeclaracion" = COALESCE(:FechaDeclaracion, DOF."FechaDeclaracion")
   , "Comentario" = COALESCE(:Comentario, DOF."Comentario")
   , "IdEstado" = COALESCE(:IdEstado, DOF."IdEstado")
   , "OperacionInscripcion" = COALESCE(:OperacionInscripcion, DOF."OperacionInscripcion")
   , "FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" DOF
WHERE DOF."IdDeclaracionOrigenFondos" = :IdDeclaracionOrigenFondos;