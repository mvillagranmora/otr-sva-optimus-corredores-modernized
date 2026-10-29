WITH 
    Ordenes as (
        SELECT "IdOrden", "Cantidad", "NombreCompleto", "IdEstadoOrden"
        FROM "BSCL.Orden.BD::ORD.Orden" 
        WHERE "IdOrden" = :IdOrden AND "OrdenVigente" = 'S' AND "IdEstadoOrden" <> 'RZD' AND "IdEstadoOrden" <> 'ANU' AND "IdEmpresa" = :IdEmpresa
    ) 
SELECT O."IdOrden", 
CASE 
    WHEN O."Cantidad" = COALESCE(AO."TotalAsignacion",-1) THEN 'EJE'
    WHEN (COALESCE(AO."TotalAsignacion",-1) < O."Cantidad" AND COALESCE(AO."TotalAsignacion",-1) > 0) THEN 'EJP'
    ELSE 'OFE'
END AS "IdEstado","TotalAsignacion"
FROM Ordenes O
LEFT JOIN (
    SELECT O."IdOrden", SUM("CantidadAsignada") AS "TotalAsignacion" 
    FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" CA
    LEFT JOIN Ordenes O ON CA."IdOrden" = O."IdOrden"
    WHERE CA."IdEstadoAsignacion" != 'ANU'
    GROUP BY O."IdOrden"
) AO ON O."IdOrden" = AO."IdOrden";