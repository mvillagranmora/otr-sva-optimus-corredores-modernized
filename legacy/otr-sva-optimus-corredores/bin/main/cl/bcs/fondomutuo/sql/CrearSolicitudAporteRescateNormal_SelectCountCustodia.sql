SELECT CARTERA."IdPortafolio" || '_' || SERIE."IdSerieFondo" AS "Id", COUNT(1) As "Cantidad" FROM "BSCL.Custodia.BD::CUS.Cartera" CARTERA

INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON CARTERA."IdInstrumento" = SERIE."IdInstrumento"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REGIMEN ON REGIMEN."IdRegimenFondoMutuo" = CARTERA."IdRegimenFondoMutuo"

WHERE 	CARTERA."IdEstado" 		= 'VIG' 
AND 	REGIMEN."GrupoRegimen" 	= 'DC' 
AND 	SERIE."IdSerieFondo" 	IN (:IdSerieFondo)
AND 	CARTERA."IdPortafolio"	IN (:IdPortafolio)

GROUP BY CARTERA."IdPortafolio"
		,SERIE."IdSerieFondo"