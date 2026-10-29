SELECT 
    ConsultarCartera."IdCartera"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    CASE WHEN   "IdTipoInstrumento" = 'SIM' THEN 'CARTERASIM'  
         ELSE   'CARTERARV' END AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    ConsultarCartera."FolioCartera"                     AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."FechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarCartera."FechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."FechaOperacion")  AS "Periodo",
    ABS(ConsultarCartera."INGMontoATasaMercadoActual")  AS "Monto",
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",                    
    ConsultarCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE "IdEmpresa" = :p_IdEmpresa
    AND "FechaOperacion" <= :p_FechaContable 
    AND "EGRCantidad" = 0
    AND "IdClasificacionTipoInstrumento" = 'RV'
    AND (ConsultarCartera."IdTipoInstrumento" NOT IN ('PRT','EMP','PAC', 'FWD','RRV','RRF') OR (ConsultarCartera."IdTipoInstrumento" = 'SIM' AND ConsultarCartera."IdTipoOperacion" = 'VENSIM'))
    AND "DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
    AND "IdEstado" = 'VIG'
UNION ALL
SELECT 
    ConsultarGarantias."IdGarantia"                     AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    'CARTERAGTA'                                        AS "IdTipoMovimiento",
    ConsultarGarantias."IdEmpresa"                      AS "IdEmpresa",                                     
    ConsultarGarantias."FolioGarantia"                  AS "Numero",
    ConsultarGarantias."FolioGarantia"                  AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
                
    ConsultarGarantias."DocumentoIdentidad"             AS "DocumentoIdentidad",  
    ConsultarGarantias."IdPortafolio"                   AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarGarantias."FechaOperacion"))        AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarGarantias."FechaOperacion"),2) ||'-'|| YEAR(ConsultarGarantias."FechaOperacion")  AS "Periodo",
    ABS(ConsultarGarantias."INGMontoATasaMercadoActual") AS "Monto",
    "IdMonedaSistema"                                   AS "IdMoneda",                                     
    ConsultarGarantias."IdEstado"                       AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" ConsultarGarantias
WHERE "IdEmpresa" = :p_IdEmpresa
AND "IdClasificacionTipoInstrumento" = 'RV'
AND "FechaOperacion" <= :p_FechaContable 
AND "EGRCantidad" = 0
AND "DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
AND "NemoGarantizado" != 'SIMULTANEA VENTA'
AND ConsultarGarantias."IdEstado" = 'VIG';