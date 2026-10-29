SELECT "ValorOrigen", "IdEstado", "ValorDestino", TRIM("ValorOrigen") ||'_'||TRIM("IdEstado") AS "Hash"
FROM "BSCL.Normativa.BD::DOC.Homologacion"
WHERE "Esquema" = :Esquema;