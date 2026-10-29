SELECT 
Detalle."IdTipoOrigenFondo"
,Detalle."NombreTipoOrigenFondo" AS "TipoOrigenFondo"
,CAST(Detalle."Valor" AS VARCHAR(50)) AS "Valor"
,Detalle."Otros"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos" AS Detalle
WHERE Detalle."IdEmpresa" = :p_IdEmpresa 
AND Detalle."IdCliente" = :p_IdCliente 
AND Detalle."IdDeclaracionOrigenFondos" = :p_IdDeclaracionOrigenFondos