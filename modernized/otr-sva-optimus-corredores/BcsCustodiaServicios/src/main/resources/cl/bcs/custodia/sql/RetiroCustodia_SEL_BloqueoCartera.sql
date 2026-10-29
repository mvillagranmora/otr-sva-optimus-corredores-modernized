SELECT 
	"IdCartera" 
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdEmpresa"		= :IdEmpresa
AND ("IdCartera" 		IN (:IdCustodia) 	OR 1 = :FiltroCustodiaValido)
AND ("IdPortafolio" 	IN (:IdPortafolio)	OR 1 = :FiltroPortafolioValido)
AND ("IdInstrumento"	IN (:IdInstrumento)	OR 1 = :FiltroInstrumentoValido)
AND ("FolioCartera"     IN (:FolioCartera)  OR 1 = :FiltroFolioCarteraValido)
AND ("IdRegimenFondoMutuo" IN (:IdRegimenFondoMutuo)  OR 1 = :FiltroRegimenValido)
AND ("IdEstado"         IN (:IdEstado)      OR 1 = :FiltroEstadoValido)
FOR UPDATE