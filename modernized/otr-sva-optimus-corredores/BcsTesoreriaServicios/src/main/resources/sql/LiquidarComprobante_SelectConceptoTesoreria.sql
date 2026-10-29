SELECT
	ConceptoTesoreria."IdConcepto"			AS "IdConcepto"
	, ConceptoTesoreria."NoValidaSaldoCaja"	AS "IndicadorNoValidaSaldoCaja"
FROM "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ConceptoTesoreria
WHERE ConceptoTesoreria."IdConcepto" IN (:ConceptoIds);