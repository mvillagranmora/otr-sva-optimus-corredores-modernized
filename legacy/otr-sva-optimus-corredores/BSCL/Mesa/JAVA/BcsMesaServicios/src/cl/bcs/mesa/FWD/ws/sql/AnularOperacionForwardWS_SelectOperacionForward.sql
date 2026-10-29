SELECT 
CARTERA."FolioCartera" AS "FolioOperacion"
,OPERACION."IdEstado"
,CARTERA."IdOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CARTERA
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPERACION ON (OPERACION."IdOperacion" = CARTERA."IdOperacion" AND OPERACION."IdTipoProducto" = CARTERA."IdTipoProducto")
WHERE CARTERA."IdTipoProducto" = 'MEFW' AND CARTERA."FolioCartera" IN (:FolioOperacion) AND OPERACION."IdEstado" != 'MOD' AND CARTERA."IdEstado" != 'RPA'
GROUP BY 
CARTERA."FolioCartera"
,OPERACION."IdEstado"
,CARTERA."IdOperacion"