SELECT 
Detalle."IdDeclaracionOrigenFondos"
,COALESCE( Detalle."IdTipoOrigenFondo",'') AS "IdTipoOrigenFondo"
,COALESCE( Detalle."NombreTipoOrigenFondo",'') AS "TipoOrigenFondo"
,CAST(Detalle."Valor" AS VARCHAR(50)) AS "Valor"
,COALESCE( Detalle."Otros",'') AS "Otros"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos" AS Detalle
WHERE Detalle."IdEmpresa" = :p_IdEmpresa 
AND Detalle."IdCliente" IN (:p_IdClientes)