SELECT 	NOR_ilsConcepto."IdConcepto",NOR_ilsConcepto."Nombre",NOR_ilsConcepto."NombreCorto",NOR_ilsConceptoConfiguracion."IdConceptoGrupo",NOR_ilsConceptoConfiguracion."IdItem",NOR_ilsConceptoConfiguracion."Operacion",NOR_ilsConceptoConfiguracion."Factor"
	,NOR_ilsIndiceConfiguracion."IdIndice",NOR_ilsIndiceConfiguracion."Posicion"
FROM "BSCL.Normativa.BD::ILS.Concepto" AS NOR_ilsConcepto
	INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS NOR_ilsConceptoConfiguracion ON 
		NOR_ilsConceptoConfiguracion."IdConcepto" 	= NOR_ilsConcepto."IdConcepto"
	INNER  JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS NOR_ilsIndiceConfiguracion ON
		NOR_ilsConceptoConfiguracion."IdConcepto" 	= NOR_ilsIndiceConfiguracion."IdConcepto"
WHERE NOR_ilsConcepto."IdConcepto"	IN (:p_IdConcepto) AND  NOR_ilsIndiceConfiguracion."IdIndice"	IN (:p_IdIndice) AND NOR_ilsConcepto."Estado" ='VIG';

--SELECT * FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion"
--SELECT * FROM "BSCL.Normativa.BD::ILS.IndiceConfiguracion"