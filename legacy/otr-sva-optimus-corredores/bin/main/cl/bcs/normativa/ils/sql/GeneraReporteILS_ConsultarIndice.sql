SELECT 	NOR_ilsIndice."IdIndice", NOR_ilsIndice."IdEmpresa", NOR_ilsIndice."Nombre", NOR_ilsIndice."NombreCorto", NOR_ilsIndice."Tipo",
	NOR_ilsIndiceConfiguracion."IdConcepto"/*,NOR_ilsConceptoConfiguracion."IdConceptoGrupo"*/,NOR_ilsIndiceConfiguracion."Posicion",NOR_ilsIndiceConfiguracion."Factor",NOR_ilsIndiceConfiguracion."Operacion"
FROM "BSCL.Normativa.BD::ILS.Indice" AS NOR_ilsIndice  
	INNER JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS NOR_ilsIndiceConfiguracion ON (NOR_ilsIndice."IdIndice" = NOR_ilsIndiceConfiguracion."IdIndice")
	/*INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS NOR_ilsConceptoConfiguracion ON 
		NOR_ilsConceptoConfiguracion."IdConcepto" 	= NOR_ilsIndiceConfiguracion."IdConcepto" */
WHERE NOR_ilsIndice."IdIndice" IN (:p_IdIndice) AND NOR_ilsIndice."Estado" = 'VIG' AND NOR_ilsIndiceConfiguracion."Estado" = 'VIG'


--SELECT * FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" 