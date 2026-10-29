WITH v_TabEventosPaso AS (
    SELECT
        TO_BIGINT(:p_IdTipoCentralizacion)    AS "IdTipoCentralizacion"
         ,"IdCartera"                        AS  "IdMovimiento"                              
        ,COALESCE("INGCantidad",0) AS "INGCantidad"
        ,"IdMonedaInstrumento"
        ,"Nemotecnico"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND ("FechaVencimientoOperacion" > :p_FechaContable AND "EGRCantidad" = 0) 
        AND "IdClasificacionTipoInstrumento" = 'RV' 
        AND "IdTipoInstrumento" = 'SIM'
        AND "IdEstado" = 'VIG'
    UNION ALL
    SELECT
        TO_BIGINT(:p_IdTipoCentralizacion)    AS "IdTipoCentralizacion"
        ,"IdCartera"                        AS  "IdMovimiento"                              
        ,COALESCE("INGCantidad",0) AS "INGCantidad"
        ,"IdMonedaInstrumento"
        ,"Nemotecnico"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "EGRFechaOperacion" = :p_FechaContable 
        AND "IdClasificacionTipoInstrumento" = 'RV' 
        AND "IdTipoInstrumento" = 'SIM'
        AND "IdEstado" IN ('VCD','ANT')

)
SELECT
    "IdMovimiento"
    ,"IdTipoCentralizacion"
    ,'MONTO_DETERIORO' AS "IdEvento"
    ,'Monto Deterioro' AS "Nombre"
    ,COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
    		(DET."PorcentajeDeterioro"/100) * v_TabEventosPaso."INGCantidad"
    		,MF."MontoDecimales"
    		,MF."MontoTrucaRedondea"),0) AS "MontoEvento"
    ,"IdMonedaInstrumento"              AS "CodigoMoneda"
FROM v_TabEventosPaso
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = v_TabEventosPaso."Nemotecnico")
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = INS."IdEmisor" AND DET."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
	 		 ON MF."IdMoneda" 			= v_TabEventosPaso."IdMonedaInstrumento" 
	 		AND MF."IdUsoMonedaFormato" = 'GEN'
	 		AND MF."IdTipoInstrumento" 	= ''
	 		AND MF."IdTipoOperacion" 	= ''
WHERE DET."FlagDeterioro" = 'S' AND DET."PorcentajeDeterioro" > 0;