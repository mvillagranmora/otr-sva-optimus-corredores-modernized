WITH v_TabEventosPaso AS(
    SELECT
             "IdCartera"                                         AS  "IdMovimiento"
            ,TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion"
            ,"INGMontoInteresCompraProximo"
            ,"INGMontoReajusteCompraProximo"            
            ,CASE ifnull("IdMonedaInstrumento",'')
                WHEN '' THEN "IdMonedaSistema"
                ELSE "IdMonedaInstrumento"
                END  AS "IdMonedaInstrumento" 
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    WHERE "IdEmpresa" = :p_IdEmpresa
        AND (
            ("INGFechaOperacion" <= :p_FechaContable AND "EGRCantidad" = 0) OR("INGFechaOperacion" <= :p_FechaContable AND "EGRCantidad" <> 0 AND "INGFechaOperacion" > :p_FechaContable)
            )
        AND "IdClasificacionTipoInstrumento" = 'RF'
        AND "IdTipoInstrumento"              <> 'PACTO'
        AND "DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
)

SELECT   
"IdMovimiento"                     	AS "IdMovimiento"
        ,"IdTipoCentralizacion"
        ,'MONTO_INTERES'          	AS "IdEvento"
        ,'Monto Interes'      		AS "Nombre"
        ,"INGMontoInteresCompraProximo"       AS "MontoEvento"
        ,"IdMonedaInstrumento"              AS "CodigoMoneda"
FROM v_TabEventosPaso
UNION 
SELECT "IdMovimiento","IdTipoCentralizacion",'MONTO_REAJUSTE'    			,'Monto Reajuste'  					,"INGMontoReajusteCompraProximo"    ,"IdMonedaInstrumento" FROM v_TabEventosPaso;
