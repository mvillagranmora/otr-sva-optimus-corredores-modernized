SELECT 
    COM."IdComprobante", 
    COM."IdRespuestaDTE", 
    COM."IdPortafolio",
    COM."FechaComprobante", 
    COM."IdEstadoComprobante", 
    COM."NombreLargoCliente",
    POR."CodigoPortafolio"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" COM
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
    ON COM."IdPortafolio" = POR."IdPortafolio"
WHERE CAST(COM."FechaComprobante" AS DATE) = :FechaConsulta
AND COM."IdEstadoComprobante" IN ('FAC', 'ENV')