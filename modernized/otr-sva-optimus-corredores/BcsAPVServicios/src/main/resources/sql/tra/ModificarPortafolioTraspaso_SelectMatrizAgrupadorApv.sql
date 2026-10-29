SELECT "IdMatrizAgrupadorAPV" 
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"
WHERE "IdPlanTipoInversion" = :p_IdPlanTipoInversion 
AND "IdProductoAgrupador" = :p_IdProductoAgrupador 
AND "IdRegimenTributario" = :p_IdRegimenTributario
;