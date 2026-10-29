SELECT 
carteras."IdPortafolio"
,carteras."IdInstrumento"
,carteras."IdRegimenFondoMutuo"
, SUM(COALESCE(carteras."INGCantidad",0)) AS "CantidadDisponiblePortafolio"
FROM "BSCL.Custodia.BD::CUS.Cartera" carteras
WHERE 
carteras."IdPortafolio" IN (:IdPortafolio)
AND carteras."IdInstrumento" IN (:IdInstrumento)
AND carteras."IdEmpresa" = :IdEmpresa
AND   carteras."IdClasificacionTipoInstrumento" = 'FM'
AND carteras."IdEstado" = 'VIG'
GROUP BY carteras."IdPortafolio",carteras."IdInstrumento",carteras."IdRegimenFondoMutuo"