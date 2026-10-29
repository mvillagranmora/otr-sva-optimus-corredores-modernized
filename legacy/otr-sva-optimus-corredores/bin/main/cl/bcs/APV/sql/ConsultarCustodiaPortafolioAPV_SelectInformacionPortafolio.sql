SELECT
PA."NombreProductoAgrupador"
,PA."IdProductoAgrupador"
,RT."Codigo" AS "Regimen"
,POR."IdPortafolio"
,MA."IdRegimenTributario"
,MA."IdPlanTipoInversion"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MA ON POR."IdMatrizAgrupadorAPV" = MA."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RT ON MA."IdRegimenTributario" = RT."IdRegimenTributario"
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PA ON PA."IdProductoAgrupador" = MA."IdProductoAgrupador"
WHERE POR."IdPortafolio" IN (:IdPortafolio)
AND POR."IdEstado" <> 'INA';