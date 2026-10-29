with 
    Ordenes as (
        SELECT "IdOrden", "Cantidad", "NombreCompleto", "IdTipoVigenciaOrden"
        FROM "BSCL.Orden.BD::ORD.Orden" 
        WHERE "FechaVigencia" <= TO_DATE(:p_FechaVencimiento) AND "OrdenVigente" = 'S' AND "IdEstadoOrden" <> 'RZD' AND "IdEstadoOrden" <> 'ANU' AND "IdEmpresa" = :p_IdEmpresa
    ) 
SELECT O."IdOrden", 
CASE 
    WHEN O."Cantidad" = COALESCE(AO."TotalAsignacion",-1) THEN 'EJE'
    WHEN (COALESCE(AO."TotalAsignacion",-1) < O."Cantidad" AND COALESCE(AO."TotalAsignacion",-1) > 0) THEN 'EJP'
    ELSE 'VCD'
END AS "IdEstado",
O."IdTipoVigenciaOrden"
FROM Ordenes O
LEFT JOIN (
    SELECT O."IdOrden", SUM("CantidadAsignada") AS "TotalAsignacion" 
    FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" CA
    LEFT JOIN Ordenes O ON CA."IdOrden" = O."IdOrden"
    GROUP BY O."IdOrden"
) AO ON O."IdOrden" = AO."IdOrden";