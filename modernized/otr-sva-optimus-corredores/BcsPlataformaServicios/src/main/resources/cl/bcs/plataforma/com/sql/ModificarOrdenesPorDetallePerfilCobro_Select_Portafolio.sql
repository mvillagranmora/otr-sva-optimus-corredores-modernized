SELECT 
"IdPortafolio",
"VALCOB"."IdTipoProducto",
"VALCOB"."IdCobro"
FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros"AS "DETALLE"
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" AS "VALCOB" ON "VALCOB"."IdValorCobro" = "DETALLE"."IdValorCobro"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON  "PORT"."IdPerfilCobrosExtra" ="DETALLE"."IdPerfilCobros"
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
