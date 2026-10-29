SELECT DISTINCT 
"IdPortafolio" ,
"IdCobro",
"VALCOB"."IdTipoProducto",
"VALCOB"."IdValorCobro",
"PERF"."IdPerfilCobros" ,
"VALCOB"."ValorCobro" ,
"VALCOB"."ValorTopeMinimoOrden" ,
"VALCOB"."IdMoneda"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" 
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" AS "PERF" ON "PERF"."IdPerfilCobros" = "PORT"."IdPerfilCobros" 
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" AS "PERFDT" ON "PERFDT"."IdPerfilCobros"  = "PORT"."IdPerfilCobros"  
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" AS "VALCOB" ON "VALCOB"."IdValorCobro" = "PERFDT"."IdValorCobro"
WHERE "IdPortafolio" = :IdPortafolio
AND "IdCobro" = :IdCobro 
AND "VALCOB"."IdTipoProducto" = :IdTipoProducto
AND "PERFDT"."Borrado"= 'N'
;