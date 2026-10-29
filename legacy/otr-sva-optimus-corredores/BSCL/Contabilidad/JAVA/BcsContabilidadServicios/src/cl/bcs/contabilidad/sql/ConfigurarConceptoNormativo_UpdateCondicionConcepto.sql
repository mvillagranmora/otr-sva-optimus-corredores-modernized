UPDATE "BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo"
SET 
  "IdConceptoNormativo" = :IdConceptoNormativo,
  "IdTipoDatoEvento"    = :IdTipoDatoEvento,
  "ValorVarchar"        = :ValorVarchar,
  "OperadorFuncion"     = :OperadorFuncion
WHERE "IdCondicionConceptoNormativo" = :IdCondicion
