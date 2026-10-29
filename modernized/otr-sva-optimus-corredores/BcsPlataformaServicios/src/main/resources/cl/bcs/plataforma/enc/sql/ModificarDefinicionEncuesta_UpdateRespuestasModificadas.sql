UPDATE "BSCL.Plataforma.BD::ENC.DefinicionRespuesta"
SET "Puntaje" = COALESCE(:Puntaje,"Puntaje"), "TextoRespuesta" = COALESCE(:TextoRespuesta,"TextoRespuesta")
WHERE "IdDefinicionRespuesta" = :IdDefinicionRespuesta;
