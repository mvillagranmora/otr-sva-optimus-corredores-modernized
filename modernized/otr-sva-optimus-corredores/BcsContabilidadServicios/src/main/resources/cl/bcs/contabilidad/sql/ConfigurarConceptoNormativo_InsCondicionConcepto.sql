INSERT INTO "BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo"
(
  "IdCondicionConceptoNormativo",
  "IdConceptoNormativo",
  "IdTipoDatoEvento",
  "ValorVarchar",
  "OperadorFuncion",
  "IdEstado"
)
VALUES (
  "BSCL.Contabilidad.BD::ITZ.SeqCondicionConceptoNormativo".NEXTVAL,
  :IdConceptoNormativo,
  :IdTipoDatoEvento,
  :ValorVarchar,
  :OperadorFuncion,
  'ACT'
)
