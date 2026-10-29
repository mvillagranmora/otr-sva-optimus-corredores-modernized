SELECT "IdTipoDatoEvento", "ValorVarchar"
FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor"
WHERE "Estado" = 'V'
  AND "IdTipoDatoEvento" IN (:tipos)
  AND "ValorVarchar" IN (:valores)
