SELECT 
  "IdCondicionConceptoNormativo" as "IdCondicion",
  "IdTipoDatoEvento"
  "ValorVarchar",
  "OperadorFuncion"
FROM "BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo"
WHERE "IdConceptoNormativo" = :IdConceptoNormativo
