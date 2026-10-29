WITH v_TabEventosPaso AS(
    SELECT
            ConsultarFusionCartera."IdCartera"                       	AS "IdMovimiento"
            ,TO_BIGINT(:p_IdTipoCentralizacion)	AS "IdTipoCentralizacion"
            ,CASE WHEN CarteraFusionada."DiferenciaInversion" < 0 THEN ABS (CarteraFusionada."DiferenciaInversion") ELSE 0 END AS "DiferenciaNegativaFusion"
            ,CASE WHEN CarteraFusionada."DiferenciaInversion" > 0 THEN CarteraFusionada."DiferenciaInversion"  ELSE 0 END AS "DiferenciaPositivaFusion"
            ,ConsultarFusionCartera."IdMonedaSistema"
            ,ConsultarFusionCartera."IdMonedaInstrumento"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarFusionCartera 
    INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarterasFusionadas" CarteraFusionada ON CarteraFusionada."FolioCartera" = ConsultarFusionCartera."FolioCartera" 
    LEFT JOIN "BSCL.Custodia.BD::HIS.FusionCartera" DatoFusion ON DatoFusion."IdCarteraResultante" = ConsultarFusionCartera."IdCartera"
    WHERE ConsultarFusionCartera."IdEmpresa" = :p_IdEmpresa
        AND (
                (ConsultarFusionCartera."INGFechaOperacion" <= :p_FechaContable AND ConsultarFusionCartera."EGRCantidad" = 0) OR(ConsultarFusionCartera."INGFechaOperacion" <= :p_FechaContable AND ConsultarFusionCartera."EGRCantidad" <> 0 AND ConsultarFusionCartera."INGFechaOperacion" > :p_FechaContable)
        )   
        AND ConsultarFusionCartera."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
        AND CarteraFusionada."DiferenciaInversion" <> 0
        AND ConsultarFusionCartera."IdEstado" = 'VIG'
)
SELECT "IdMovimiento","IdTipoCentralizacion",'DIFERENCIA_NEGATIVA_FUSION' AS "IdEvento"	,'Diferencia Negativa por Fusion'	AS "Nombre","DiferenciaNegativaFusion"	AS "MontoEvento","IdMonedaSistema" AS "CodigoMoneda" 	FROM v_TabEventosPaso 
WHERE "DiferenciaNegativaFusion" <> 0 
UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'DIFERENCIA_POSITIVA_FUSION' AS "IdEvento"	,'Diferencia Positiva por Fusion'	AS "Nombre","DiferenciaPositivaFusion"	AS "MontoEvento","IdMonedaSistema" AS "CodigoMoneda"	FROM v_TabEventosPaso
WHERE "DiferenciaPositivaFusion" <> 0;
