--ESTOS SON EVENTOS EN MONEDA CLP
SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'MONTO_EVENTO_CAPITAL'                          AS "IdEvento",
    'Monto Evento de Capital'                       AS "Nombre", 
    cargoabono."Monto"               				AS "MontoEvento", 
    cuentainversion."IdMoneda"                		AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" = 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'MONTO_VENCIMIENTO_EVENTO_CAPITAL'              AS "IdEvento",
    'Monto Vencimiento Evento de Capital'           AS "Nombre", 
    ABS(eventocapital."APagarPesosSorteoLetras")    AS "MontoEvento", 
    cuentainversion."IdMoneda"                 		AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" eventocapital ON eventocapitalcliente."IdEventoCapital" = eventocapital."IdEventoCapital"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" = 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'MONTO_INTERES_EVENTO_CAPITAL'                  AS "IdEvento",
    'Monto Interes Evento de Capital'               AS "Nombre", 
    ABS(eventocapitalcliente."MontoInteres")        AS "MontoEvento", 
    cuentainversion."IdMoneda"                 AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" = 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'MONTO_REAJUSTE_EVENTO_CAPITAL'                    AS "IdEvento",
    'Monto Reajuste Evento de Capital'              AS "Nombre", 
    ABS(eventocapitalcliente."MontoReajuste")               AS "MontoEvento", 
    cuentainversion."IdMoneda"                 AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" = 'CLP'

--AGREGAR VALOR DE LOS MISMOS EVENTOS PERO PARA MONEDA != CLP
UNION
SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'VALOR_EVENTO_CAPITAL'                          AS "IdEvento",
    'Monto Evento de Capital'                       AS "Nombre", 
    ABS(cargoabono."Monto")              			AS "MontoEvento", 
    cuentainversion."IdMoneda"                 		AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" != 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'VALOR_VENCIMIENTO_EVENTO_CAPITAL'             	AS "IdEvento",
    'Monto Vencimiento Evento de Capital'           AS "Nombre", 
    ABS(eventocapital."APagarPesosSorteoLetras")    AS "MontoEvento", 
    cuentainversion."IdMoneda"                 		AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" eventocapital ON eventocapitalcliente."IdEventoCapital" = eventocapital."IdEventoCapital"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" != 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'VALOR_INTERES_EVENTO_CAPITAL'                     AS "IdEvento",
    'Monto Interes Evento de Capital'               AS "Nombre", 
    ABS(eventocapitalcliente."MontoInteres")               AS "MontoEvento", 
    cuentainversion."IdMoneda"                 AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" != 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'VALOR_REAJUSTE_EVENTO_CAPITAL'                    AS "IdEvento",
    'Monto Reajuste Evento de Capital'              AS "Nombre", 
    ABS(eventocapitalcliente."MontoReajuste")               AS "MontoEvento", 
    cuentainversion."IdMoneda"                 AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" != 'CLP'

UNION

SELECT 
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    eventocapitalcliente."IdEventoCapitalCliente"   AS "IdMovimiento",
    'VALOR_EVENTO_CAPITAL_USD(CLP)'                          AS "IdEvento",
    'Monto CLP(USD)'                       AS "Nombre", 
    round (ABS(cargoabono."Monto" * COALESCE( PTCDO."TipoCambioMercado",0)))              			AS "MontoEvento", 
    'CLP'                 		AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapitalcliente ON eventocapitalcliente."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO ON (PTCDO."IdMoneda" = 'DO' AND PTCDO."Fecha" =:p_FechaContable)
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapitalcliente."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
AND cuentainversion."IdMoneda" IN ('USD','DO');