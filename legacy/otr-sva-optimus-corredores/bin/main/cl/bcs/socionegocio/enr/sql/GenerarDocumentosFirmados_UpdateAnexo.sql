UPDATE "BSCL.Plataforma.BD::DOC.Anexo" 
SET "IdEstado" = :IdEstado 
,"FechaVigencia" = CURRENT_UTCTIMESTAMP 
WHERE "IdAnexo" IN (:IdAnexo) 
AND "IdDocumento" IN (:IdDocumento); 