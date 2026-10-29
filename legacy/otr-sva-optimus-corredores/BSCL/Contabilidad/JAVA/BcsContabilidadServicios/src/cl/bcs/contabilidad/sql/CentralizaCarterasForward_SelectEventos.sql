SELECT DISTINCT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_PRINCIPAL' AS "IdEvento",'Monto Moneda Principal' AS "Nombre", ABS("INGMontoMonedaPrincipal") AS "MontoEvento", "IdMonedaPrincipal" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" <= :FechaContable AND "IdEstado" = 'VIG'
UNION
SELECT DISTINCT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_SECUNDARIA' AS "IdEvento",'Monto Moneda Secundaria' AS "Nombre", ABS("INGMontoMonedaSecundaria") AS "MontoEvento", "IdMonedaSecundaria" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" <= :FechaContable AND "IdEstado" = 'VIG'
UNION
SELECT DISTINCT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_PRINCIPAL' AS "IdEvento",'Monto Moneda Principal' AS "Nombre", ABS("EGRMontoMonedaPrincipal") AS "MontoEvento", "IdMonedaPrincipal" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "EGRFechaOperacion" = :FechaContable AND "IdEstado" IN ('VCD','ANT')
UNION
SELECT DISTINCT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_SECUNDARIA' AS "IdEvento",'Monto Moneda Secundaria' AS "Nombre", ABS("EGRMontoMonedaSecundaria") AS "MontoEvento", "IdMonedaSecundaria" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "EGRFechaOperacion" = :FechaContable AND "IdEstado" IN ('VCD','ANT')