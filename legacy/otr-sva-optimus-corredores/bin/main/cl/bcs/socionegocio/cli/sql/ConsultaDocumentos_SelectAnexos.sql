SELECT DOC."IdDocumento"
,ANX."IdAnexo"
,ANX."NombreArchivoAdjunto"
,ANX."FormatoArchivoAdjunto"
,ANX."ArchivoAdjunto"
FROM "BSCL.Plataforma.BD::DOC.Documento" DOC
LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX 
ON ANX."IdDocumento" = DOC."IdDocumento"
WHERE DOC."IdEmpresa" = :IdEmpresa
AND DOC."IdDocumento" in (:IdDocumentos) AND ANX."IdAnexo" in (:IdAnexos)