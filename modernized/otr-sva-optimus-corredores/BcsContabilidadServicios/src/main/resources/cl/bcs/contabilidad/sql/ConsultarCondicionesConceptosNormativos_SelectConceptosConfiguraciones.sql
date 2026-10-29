SELECT 
	cn."IdConceptoNormativo", 
	cn."ConceptoNormativo", 
	ccn."IdTipoDatoEvento", 
	ccn."ValorVarchar", 
	ccn."OperadorFuncion", 
	ccn."IdCondicionConceptoNormativo"
FROM "BSCL.Contabilidad.BD::ITZ.ConceptoNormativo" cn 
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CondicionConceptoNormativo" ccn ON cn."IdConceptoNormativo" = ccn."IdConceptoNormativo";