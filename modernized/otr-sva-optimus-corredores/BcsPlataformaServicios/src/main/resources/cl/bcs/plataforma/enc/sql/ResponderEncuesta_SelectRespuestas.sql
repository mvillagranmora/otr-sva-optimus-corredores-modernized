SELECT R."IdEmpresa", R."IdDefinicionRespuesta", R."IdDefinicionPregunta", R."IdUsuarioCreacion", R."FechaIngresoRegistro", R."TextoRespuesta", R."Puntaje"
FROM "BSCL.Plataforma.BD::ENC.DefinicionRespuesta" R
LEFT JOIN "BSCL.Plataforma.BD::ENC.DefinicionPregunta" P ON R."IdDefinicionPregunta" = P."IdDefinicionPregunta"
WHERE P."IdDefinicionEncuesta" = :IdDefinicionEncuesta and P."Borrado" = 'N' and R."Borrado" ='N' 
