SELECT
	TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,cargoabono."IdCargoAbonoMovimiento" AS "IdMovimiento"
	,CASE WHEN eventocapital."IdTipoEventoCapital" = 'CC' THEN 'CORTECUPON'
	 WHEN eventocapital."IdTipoEventoCapital" = 'SL' THEN 'SORTEOLETR' ELSE '' END  AS "IdTipoMovimiento"
	,cargoabono."IdEmpresa" AS "IdEmpresa"
	,cargoabono."IdOperacion"  AS "Numero"
	,TO_BIGINT(0) AS "Linea"
	,TO_NVARCHAR(YEAR(:p_FechaContable) ||'-'|| 
                RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| 
                RIGHT('0' || DAYOFMONTH(:p_FechaContable),2)) 
                                                        AS "Fecha"
    ,documentoIdentidad."DocumentoIdentidad" AS "DocumentoIdentidad"
    ,cuentainversion."IdPortafolio" AS "IdPortafolio"
    ,TO_BIGINT(0) AS "Ejercicio"
    ,'' AS "Periodo"
    ,cuentainversion."IdMoneda" AS "IdMoneda"
    ,cargoabono."Monto" AS "Monto"
    ,'' AS "Estado"

FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto" IN ('CICCMI','CICCAM','CIECCC','CIECSL','RET104')
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" IN ('CC','SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa;