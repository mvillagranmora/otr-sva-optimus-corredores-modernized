SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_PRINCIPAL' AS "IdEvento",'Monto Moneda Principal' AS "Nombre", ABS("INGMontoMonedaPrincipal") AS "MontoEvento", "IdMonedaPrincipal" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" <= :FechaContable AND "FechaVencimientoOperacion" > :FechaContable AND "IdEstado" = 'VIG'
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_SECUNDARIA' AS "IdEvento",'Monto Moneda Secundaria' AS "Nombre", ABS("INGMontoMonedaSecundaria") AS "MontoEvento", "IdMonedaSecundaria" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" <= :FechaContable AND "FechaVencimientoOperacion" > :FechaContable AND "IdEstado" = 'VIG'
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'DIFERENCIAVPVP' AS "IdEvento",'Diferencia VA - VP' AS "Nombre", ABS("INGMontoValorRazonableActivo" - "INGMontoValorRazonablePasivo") AS "MontoEvento", :IdMonedaSistema AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' AND "INGFechaOperacion" <= :FechaContable AND "FechaVencimientoOperacion" > :FechaContable AND "IdEstado" = 'VIG'
