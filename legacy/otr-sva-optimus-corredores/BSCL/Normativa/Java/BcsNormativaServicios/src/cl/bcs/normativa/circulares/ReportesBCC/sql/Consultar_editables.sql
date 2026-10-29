SELECT "Nombre"
FROM "BSCL.Normativa.BD::DOC.Campo"
WHERE "Esquema" = :Esquema 
    AND "Version" = :Version
    AND "Grupo" LIKE '%2%'
    AND "Editable" = 'S';