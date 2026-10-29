SELECT "IdTipoDatoEvento"
FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDato"
WHERE "IdTipoDatoEvento" IN (:tipos)
  AND "Estado" = 'V'
