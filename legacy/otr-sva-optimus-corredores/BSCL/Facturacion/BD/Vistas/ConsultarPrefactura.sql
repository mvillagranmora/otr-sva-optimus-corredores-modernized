SET SCHEMA "BSCL";

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPrefactura";

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPrefactura" AS

SELECT
    COM."IdCliente",
    COM."DocumentoIdentidad",
    COM."NombreLargoCliente",
    POR."IdPortafolio",
    POR."CodigoPortafolio",
    COM."FechaComprobante",
    CASE 
        WHEN SUM(CASE WHEN COM."IdEstadoComprobante" = 'FAC' THEN 1 ELSE 0 END) > 0 
         AND SUM(CASE WHEN COM."IdEstadoComprobante" = 'ENV' THEN 1 ELSE 0 END) > 0 
        THEN 'FAC_ENV'
        WHEN SUM(CASE WHEN COM."IdEstadoComprobante" = 'FAC' THEN 1 ELSE 0 END) > 0 
        THEN 'FAC'
        ELSE 'ENV'
    END AS "IdEstadoComprobante",
    COUNT(0) AS "Cantidad",
    SUM(CASE WHEN COM."IdEstadoComprobante" = 'FAC' THEN 1 ELSE 0 END) AS "CantidadFAC",
    SUM(CASE WHEN COM."IdEstadoComprobante" = 'ENV' THEN 1 ELSE 0 END) AS "CantidadENV"
FROM
    "BSCL.Facturacion.BD::FAC.Comprobante" COM
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR 
    ON POR."IdPortafolio" = COM."IdPortafolio"
WHERE COM."IdEstadoComprobante" != 'ANU'
GROUP BY
    COM."IdCliente",
    COM."DocumentoIdentidad",
    COM."NombreLargoCliente",
    POR."IdPortafolio",
    POR."CodigoPortafolio",
    COM."FechaComprobante";


GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarPrefactura" TO BCS_DESARROLLADOR WITH GRANT OPTION;
