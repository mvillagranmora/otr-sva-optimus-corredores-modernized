SELECT
"IdCliente" AS "IdClienteCtaPropiaMesaRF",
"IdPortafolio" AS "IdPortafolioCtaPropiaMesaRF"
FROM "BSCL.Mesa.BD::MES.Mesa"
WHERE "IdTipoProducto" = 'MERF'
AND "PorDefecto" = 'S';