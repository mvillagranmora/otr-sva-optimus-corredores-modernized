SELECT "IdEmpresa", "IdDefinicionEncuesta", "IdDefinicionPregunta", "IdUsuarioCreacion", "FechaIngresoRegistro", "TextoPregunta"
FROM "BSCL.Plataforma.BD::ENC.DefinicionPregunta"
WHERE "IdDefinicionEncuesta" = :IdDefinicionEncuesta and "Borrado" = 'N' 
