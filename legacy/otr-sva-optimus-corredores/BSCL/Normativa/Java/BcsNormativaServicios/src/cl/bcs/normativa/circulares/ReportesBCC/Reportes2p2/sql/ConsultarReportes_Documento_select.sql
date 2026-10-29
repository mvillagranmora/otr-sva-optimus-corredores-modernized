SELECT "IdDocumento"
  FROM BSCL."BSCL.Normativa.BD::DOC.Documento" 
 WHERE ("Esquema" = :esquema) 
  AND ("Version" = :version);