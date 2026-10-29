SELECT DISTINCT 
"PORT"."IdPortafolio" ,
"DETALLE"."IdDetallePerfilCobros" ,
"DETALLE"."IdMoneda" ,
"COBORDEN"."IdCobro" AS "IdCobrocoborden",
"ORDENN"."IdOrden",
"VALCOB"."IdCobro" ,
"COBORDEN"."ValorCobroPerfil",
"ASIG"."IdAsignacion",
"ASIG"."MontoAsignado",
"ASIG"."MontoAsignadoLocal",
"ASIG"."MontoAsignadoLiquidacion",
"COBASIG"."ValorCobro" ,
"COBASIG"."ValorCobroAplicado" ,
"COBASIG"."MontoCobroCalculado" ,
"COBASIG"."MontoCobroCalculadoLocal" ,
"COBASIG"."MontoCobroCalculadoLiquidacion" ,
"COBORDEN"."CodigoCobroClasificacion" ,
"COBASIG"."IdCobroAsignacion" ,
"COBASIG"."CodigoCobroClasificacion",
"COBASIG"."IdCobro" AS "IdCobroAsig",
"DETALLE"."Borrado",
"VALCOB"."IdTipoProducto",
 "COBORDEN"."MontoMinimo" ,
 "COBORDEN"."MontoCobroCalculado" AS "MontoCobroCalculadoOrd" 
FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros" AS "DETALLE"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON  "PORT"."IdPerfilCobrosExtra" ="DETALLE"."IdPerfilCobros"
INNER JOIN "BSCL.Orden.BD::ORD.Orden" AS ORDENN ON "ORDENN"."IdPortafolioCliente"= "PORT"."IdPortafolio" 
INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden"AS "COBORDEN" ON "COBORDEN"."IdOrden" ="ORDENN"."IdOrden" 
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" AS "ASIG" ON "ASIG"."IdOrden" = "ORDENN"."IdOrden"
INNER JOIN "BSCL.Orden.BD::ORD.CobroAsignacion" AS "COBASIG" ON "COBASIG"."IdAsignacion"  = "ASIG"."IdAsignacion"
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" AS "VALCOB" ON "VALCOB"."IdValorCobro" = "DETALLE"."IdValorCobro"
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
AND "VALCOB"."IdCobro" IN ('COVA','COFI')
AND ("COBORDEN"."CodigoCobroClasificacion" = 'COMISION' OR "COBASIG"."CodigoCobroClasificacion" = 'COMISION')
AND "ORDENN"."IdOrden" NOT IN (SELECT "IdOrden"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante"
WHERE "EstadoFacturacion" ='FAC')
ORDER BY  "ORDENN"."IdOrden" ;
