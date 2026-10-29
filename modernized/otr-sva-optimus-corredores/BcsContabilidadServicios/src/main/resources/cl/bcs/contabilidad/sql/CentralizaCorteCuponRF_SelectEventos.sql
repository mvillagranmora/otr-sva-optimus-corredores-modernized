SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,cargoabono."IdCargoAbonoMovimiento"  AS "IdMovimiento"
	,CASE 
		WHEN eventocapital."Total" > 0 THEN 'MONTOCUPONCOBRADO' 
        ELSE 'MONTOCUPONPAGADO'  END 
        AS "IdEvento"
	,CASE 
		WHEN eventocapital."Total" > 0 THEN 'MONTO CUPON COBRADO' 
        ELSE 'MONTO CUPON PAGADO'  END 
        AS "Nombre"
    ,COALESCE(cargoabono."Monto",0) AS "MontoEvento", 
    eventocapital."IdMoneda" AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto" IN ('CICCMI','CICCAM','CIECCC')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" IN ('CC')
AND cargoabono."IdEmpresa" = :p_IdEmpresa


UNION

SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,cargoabono."IdCargoAbonoMovimiento"  AS "IdMovimiento"
	,CASE 
		WHEN eventocapital."Total" > 0 THEN 'MONTOSORTEOCOBRADO' 
        ELSE 'MONTOSORTEOPAGADO'  END 
        AS "IdEvento"
	,CASE 
		WHEN eventocapital."Total" > 0 THEN 'MONTO SORTEO COBRADO' 
        ELSE 'MONTO SORTEO PAGADO'  END 
        AS "Nombre"
    ,COALESCE(cargoabono."Monto",0) AS "MontoEvento", 
    eventocapital."IdMoneda" AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto" IN ('CIECSL')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" IN ('SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa

UNION

SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,MIN(cargoabono."IdCargoAbonoMovimiento")   AS "IdMovimiento"
	,'MONTOINTERES' AS "IdEvento"
	,'MONTO INTERES' AS "Nombre"
    ,COALESCE(eventocapital."MontoInteres",0) AS "MontoEvento", 
    eventocapital."IdMoneda" AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto"  IN ('CICCMI','CICCAM','CIECCC','CIECSL')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital"IN ('CC','SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
GROUP BY cargoabono."IdOperacion", eventocapital."MontoInteres", eventocapital."IdMoneda"

UNION

SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,MIN(cargoabono."IdCargoAbonoMovimiento")  AS "IdMovimiento"
	,'MONTOAMORTIZACION' AS "IdEvento"
	,'MONTO AMORTIZACION' AS "Nombre"
    ,COALESCE(eventocapital."MontoCapital",0) AS "MontoEvento", 
    eventocapital."IdMoneda" AS "CodigoMoneda"

FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto"  IN ('CICCMI','CICCAM','CIECCC','CIECSL')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" IN ('CC','SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa
GROUP BY cargoabono."IdOperacion", eventocapital."MontoCapital", eventocapital."IdMoneda"

UNION 

SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,cargoabono."IdCargoAbonoMovimiento" AS "IdMovimiento"
	,'MONTOCAPITAL' AS "IdEvento"
	,'MONTO CAPITAL' AS "Nombre"
    ,COALESCE(eventocapital."MontoCapital",0) AS "MontoEvento", 
    eventocapital."IdMoneda" AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto" IN ('CIECCC','CIECSL')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" IN ('CC','SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa

UNION 

SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,cargoabono."IdCargoAbonoMovimiento"  AS "IdMovimiento"
	,'RET104' AS "IdEvento"
	,'RETENCION ART. 104' AS "Nombre"
    ,COALESCE(cargoabono."Monto",0) AS "MontoEvento", 
    eventocapital."IdMoneda" AS "CodigoMoneda"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto" IN ('RET104')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" IN ('CC','SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa;