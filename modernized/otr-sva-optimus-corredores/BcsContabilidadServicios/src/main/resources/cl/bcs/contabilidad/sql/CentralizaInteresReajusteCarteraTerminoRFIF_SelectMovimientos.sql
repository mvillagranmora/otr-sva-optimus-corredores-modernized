SELECT 
    ConsultarCartera."IdCartera"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    'CARINTREAJ'                                      	AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    ConsultarCartera."FolioCartera"                     AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."INGFechaOperacion")  AS "Periodo",
    ABS(ConsultarCartera."INGMontoATasaMercadoActual")  AS "Monto",
    ConsultarCartera."IdMonedaSistema"  AS "IdMoneda",                                     
    ConsultarCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE "IdEmpresa" = :p_IdEmpresa
AND (
    ("INGFechaOperacion" <= :p_FechaContable AND "EGRCantidad" = 0) OR("INGFechaOperacion" <= :p_FechaContable AND "EGRCantidad" <> 0 AND "INGFechaOperacion" > :p_FechaContable)
    )
AND "IdClasificacionTipoInstrumento" = 'RF'
AND "IdTipoInstrumento"              <> 'PACTO'
AND ("INGMontoInteresCompraProximo" <> 0 OR "INGMontoReajusteCompraProximo" <> 0)
AND "DocumentoIdentidad" = :p_DocumentoIdentidadCorredora; 