SELECT "IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais"
FROM "BSCL.Plataforma.BD::REG.DiaFeriado"
WHERE "IdPais" IN (:IdPais)
  AND "IdEstado" = 'ACT';
