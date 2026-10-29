SELECT 
TD."IdTipoDocumento"
, TD."NumeroFirmasMinimas"
, TD."NumeroFirmasMaximas"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TD 
WHERE TD."IdTipoDocumento" = :IdTipoDocumento;