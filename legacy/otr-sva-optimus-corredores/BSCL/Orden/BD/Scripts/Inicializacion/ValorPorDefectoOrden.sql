set schema "BSCL";

DELETE FROM "BSCL.Orden.BD::ORD.ValorPorDefectoOrden";

INSERT INTO "BSCL.Orden.BD::ORD.ValorPorDefectoOrden"("IdInterno","IdTipoPrecio","IdPorcentajeVisibilidad","IdFormaCustodia")
SELECT 0 AS "IdInterno",'M' AS "IdTipoPrecio",1 AS "IdPorcentajeVisibilidad",'CUS' AS "IdFormaCustodia" FROM DUMMY;
