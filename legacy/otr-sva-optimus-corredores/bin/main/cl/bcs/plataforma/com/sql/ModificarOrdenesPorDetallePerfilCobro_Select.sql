SELECT 
"DETALLE"."IdDetallePerfilCobros" ,
"DETALLE"."IdMoneda" ,
"VALCOB"."IdCobro" ,
"VALCOB"."ValorCobro" ,
"VALCOB"."ValorTopeMinimoOrden" ,
"ORDENN"."Cantidad",
"ORDENN"."PrecioOrden",
"ORDENN"."PrecioMercado",
"ORDENN"."MontoLocal",
"ORDENN"."MontoOrden",
"ORDENN"."MontoLiquidacion",
"ORDENN"."PorcentajeCobros",
"COBORDEN"."ValorCobroPerfil",
"COBORDEN"."ValorCobroAplicado",
"COBORDEN"."MontoCobroCalculado" ,
"COBORDEN"."MontoCobroCalculadoLocal" ,
"COBORDEN"."MontoCobroCalculadoLiquidacion" ,
"COBORDEN"."MontoMinimo" ,
"COBORDEN"."CodigoCobroClasificacion" ,
"COBORDEN"."ValorCobroFinal" ,
"COBORDEN"."IdMoneda" AS "IdMonedaOrden",
"ORDENN"."IdOrden",
"COBORDEN"."IdCobroOrden",
"DETALLE"."Borrado",
"VALCOB"."IdTipoProducto"
FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros" AS "DETALLE"
INNER JOIN "BSCL.Plataforma.BD::COM.ValorCobro" AS "VALCOB" ON "VALCOB"."IdValorCobro" = "DETALLE"."IdValorCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" AS "VAL" ON "VAL"."IdPerfilCobros" = "DETALLE"."IdPerfilCobros"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON  "PORT"."IdPerfilCobrosExtra" ="DETALLE"."IdPerfilCobros"
INNER JOIN "BSCL.Orden.BD::ORD.Orden" AS ORDENN ON "ORDENN"."IdPortafolioCliente"= "PORT"."IdPortafolio" 
INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden"AS "COBORDEN" ON "COBORDEN"."IdOrden" ="ORDENN"."IdOrden" 
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
AND "VALCOB"."IdCobro" IN ('COVA','COFI')
AND "ORDENN"."IdOrden" NOT IN (SELECT "IdOrden"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante"
WHERE "EstadoFacturacion" ='FAC')
ORDER BY  "ORDENN"."IdOrden"
;