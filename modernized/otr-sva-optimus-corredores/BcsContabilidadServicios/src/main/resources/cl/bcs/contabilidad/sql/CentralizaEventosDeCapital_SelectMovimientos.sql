SELECT
    TO_BIGINT(:p_IdTipoCentralizacion)        AS "IdTipoCentralizacion",
    eventocapital."IdEventoCapitalCliente"    AS "IdMovimiento",
    eventocapital."IdTipoEventoCapital" 	  AS "IdTipoMovimiento",
    cargoabono."IdEmpresa"                    AS "IdEmpresa",                                     
    eventocapital."IdEventoCapitalCliente"    AS "Numero",
    eventocapital."IdEventoCapitalCliente"    AS "Linea",
    YEAR("FechaSistema") ||'-'|| RIGHT('0' || MONTH("FechaSistema"),2) ||'-'|| RIGHT('0' || DAYOFMONTH("FechaSistema"),2) AS "Fecha",
    documentoIdentidad."DocumentoIdentidad"           	AS "DocumentoIdentidad",  
    cuentainversion."IdPortafolio"                 		AS "IdPortafolio",
    TO_BIGINT(YEAR("FechaSistema"))     		AS "Ejercicio",
    RIGHT('0' || MONTH("FechaSistema"),2) ||'-'|| YEAR("FechaSistema")  AS "Periodo",
    ABS(cargoabono."Monto")                  			 AS "Monto",
    cuentainversion."IdMoneda",
    cargoabono."IdEstado"                     			AS "Estado"           
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS eventocapital ON eventocapital."IdEventoCapitalCliente" = cargoabono."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdTipoProducto" = 'EVCA'
AND cargoabono."IdEstado" = 'CRT'
AND eventocapital."IdTipoEventoCapital" NOT IN ('CC', 'SL', 'DA')
AND cargoabono."IdModulo"='CUS'
AND cargoabono."IdEmpresa" = :p_IdEmpresa;