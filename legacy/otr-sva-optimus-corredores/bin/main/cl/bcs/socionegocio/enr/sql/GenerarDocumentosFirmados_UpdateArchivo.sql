UPDATE "BSCL.Plataforma.BD::DOC.Anexo"
SET 
"ArchivoAdjunto" = :FilePart
,"IdEstado" = :Estado
,"NombreArchivoAdjunto" = :NombreArchivoAdjunto
,"FormatoArchivoAdjunto" = :FormatoArchivoAdjunto
WHERE "IdDocumento" = :IdDocumento AND "IdAnexo" = :IdAnexo;