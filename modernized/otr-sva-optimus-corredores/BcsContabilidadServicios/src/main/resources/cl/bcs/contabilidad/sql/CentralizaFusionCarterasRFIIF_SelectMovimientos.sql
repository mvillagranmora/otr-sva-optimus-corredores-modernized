SELECT 
    ConsultarFusionCartera."IdCartera"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                        AS "IdTipoCentralizacion",
    'FUSION'                                                  AS "IdTipoMovimiento",
    ConsultarFusionCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarFusionCartera."FolioCartera"                     AS "Numero",
    ConsultarFusionCartera."FolioCartera"                     AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarFusionCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarFusionCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarFusionCartera."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarFusionCartera."INGFechaOperacion"),2) ||'-'|| YEAR(ConsultarFusionCartera."INGFechaOperacion")  AS "Periodo",
    CarteraFusionada."DiferenciaInversion"  AS "Monto",
    ConsultarFusionCartera."IdMonedaSistema"  AS "IdMoneda",                                     
    ConsultarFusionCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarFusionCartera
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadas" CarteraFusionada ON CarteraFusionada."FolioCartera" = ConsultarFusionCartera."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::HIS.FusionCartera" DatoFusion ON DatoFusion."IdCarteraResultante" = ConsultarFusionCartera."IdCartera"

WHERE ConsultarFusionCartera."IdEmpresa" = :p_IdEmpresa
AND (
    ("INGFechaOperacion" <= :p_FechaContable AND "EGRCantidad" = 0) OR("INGFechaOperacion" <= :p_FechaContable AND "EGRCantidad" <> 0 AND "INGFechaOperacion" > :p_FechaContable)
    )
AND ConsultarFusionCartera."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
AND CarteraFusionada."DiferenciaInversion" <> 0
AND ConsultarFusionCartera."IdEstado" = 'VIG';