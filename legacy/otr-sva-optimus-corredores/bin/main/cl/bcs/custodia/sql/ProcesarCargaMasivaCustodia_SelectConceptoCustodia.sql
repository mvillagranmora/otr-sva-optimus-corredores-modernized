SELECT 
	 "IdConcepto"
	,"NombreEntidad"
	,"IdClasificacionTipoInstrumento"
	,"IdContraparte"
FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia"
WHERE "IdConcepto" IN (:IdConcepto)
  AND "NombreEntidad" = 'CUS.GARANTIA';