SELECT cn."IdConceptoNormativo", ccn."IdTipoDatoEvento", ccn."ValorVarchar", ccn."OperadorFuncion", ccn."IdEstado", ccn."IdCondicionConceptoNormativo", v."Descripcion"
FROM "BSCL.Contabilidad.BD::ITZ.ConceptoNormativo" cn 
INNER JOIN "BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo" ccn ON cn."IdConceptoNormativo" = ccn."IdConceptoNormativo" 
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" V ON ccn."ValorVarchar" = v."ValorVarchar" AND ccn."IdTipoDatoEvento" = v."IdTipoDatoEvento"
WHERE cn."IdConceptoNormativo" = :IdConceptoNormativo;