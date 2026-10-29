SELECT 
    ORD."IdMonedaOrden",
    ORD."IdOrden",
    ORD."IdTipoInstrumento",
    ORD."DocumentoIdentidad",
    ORD."NombreCompleto" AS "Cliente",
    ORD."Instrumento" AS "Instrumento",
    ORD."NombreClasificacionTipoInstrumento" AS "Mercado",
    ORD."NombreTipoInstrumento" AS "Producto",
    ORD."NombreTipoOperacion" AS "Operacion",
    ORD."CantidadOrden" AS "Cantidad",
    ORD."PrecioOrden" AS "Precio",
    ORD."IdEstadoOrden",
    CASE WHEN ORD."EstadoOrdenAsignacion" = 'SIN ASIGNAR' THEN ORD."EstadoOrden" ELSE ORD."EstadoOrdenAsignacion" END AS "EstadoOrden",
    TO_NVARCHAR(ORD."FechaOperacionOrden",'DD-MM-YYYY') AS "FechaIngreso",
    TO_NVARCHAR(ORD."FechaConfirmacionPlazo",'DD-MM-YYYY') AS "FechaConfirmacionPlazo" ,
    --CASE WHEN TO_NVARCHAR(ORD."FechaConfirmacion",'DD-MM-YYYY') = '01-01-0001' THEN '' ELSE ORD."FechaConfirmacion" END AS "FechaConfirmacion",
    ORD."FechaConfirmacion",
    CASE WHEN COALESCE(ORD."IdEstadoConfirmacion",'') = '' THEN 'NCF' ELSE ORD."IdEstadoConfirmacion" END AS "IdEstadoConfirmacion",
    CASE WHEN COALESCE(ORD."EstadoConfirmacion",'') = '' THEN 'NO CONFIRMADA' ELSE ORD."EstadoConfirmacion" END AS "EstadoConfirmacion",
    ORD."InstrumentoNegociacion",
    ORD."IdMonedaNegociacion",
    ORD."IdCondicionDeLiquidacion" 
FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" as ORD 
--WHERE ORD."FechaConfirmacionPlazo" >= '2016-06-12'AND ORD."IdTipoInstrumento" IN ('ACN', 'ACX', 'SIM')  AND (ORD."IdTipoInstrumento" = COALESCE(null,ORD."IdTipoInstrumento"))
WHERE ORD."FechaConfirmacionPlazo" >= :FechaConsulta AND ORD."IdTipoInstrumento" IN (:Productos) AND (ORD."IdTipoInstrumento" = COALESCE(:IdTipoInstrumento,ORD."IdTipoInstrumento"))
AND (ORD."IdEstadoConfirmacion" = COALESCE(:EstadoConfirmacion,ORD."IdEstadoConfirmacion")) AND ORD."IdEmpresa" = :IdEmpresa
ORDER BY "FechaConfirmacionPlazo" ASC ;
--{FechaConsulta=2019-06-12, Productos=[ACN, ACX, SIM], IdEmpresa=1, IdTipoInstrumento=, EstadoConfirmacion=}