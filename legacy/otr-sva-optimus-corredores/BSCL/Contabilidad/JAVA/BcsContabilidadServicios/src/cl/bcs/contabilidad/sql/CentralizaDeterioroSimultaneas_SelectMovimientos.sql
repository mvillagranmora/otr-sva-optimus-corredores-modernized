SELECT
	MOV."IdTipoCentralizacion",
    MOV."IdMovimiento",
	MOV."IdTipoMovimiento",
    MOV."IdEmpresa",                                     
    MOV."Numero",
    MOV."Linea",
    MOV."Fecha",
    MOV."DocumentoIdentidad",  
    MOV."IdPortafolio",
    MOV."Ejercicio",
    MOV."Periodo",
   COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
    		(DET."PorcentajeDeterioro"/100) * MOV."INGCantidad"
    		,MF."MontoDecimales"
    		,MF."MontoTrucaRedondea"),0) AS "Monto",
    MOV."IdMoneda",
    MOV."Estado"
FROM 
(SELECT
    TO_BIGINT(:p_IdTipoCentralizacion)                    AS "IdTipoCentralizacion",
    ConsultarCartera."IdCartera"                        AS "IdMovimiento",
    'DETSIM'                                           AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    ConsultarCartera."FolioCartera"                     AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| YEAR(:p_FechaContable)    AS "Periodo",
    COALESCE(ConsultarCartera."INGCantidad",0) AS "INGCantidad",
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",
    ConsultarCartera."IdEstado"                         AS "Estado",
    "Nemotecnico"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND ("FechaVencimientoOperacion" > :p_FechaContable AND "EGRCantidad" = 0) 
    AND "IdClasificacionTipoInstrumento" = 'RV' 
    AND "IdTipoInstrumento" = 'SIM'
    AND "IdEstado" = 'VIG'
    
UNION ALL

SELECT
    TO_BIGINT(:p_IdTipoCentralizacion)                    AS "IdTipoCentralizacion",
    ConsultarCartera."IdCartera"                        AS "IdMovimiento",
    'DETSIM'                                           AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    ConsultarCartera."FolioCartera"                     AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| YEAR(:p_FechaContable)    AS "Periodo",
    COALESCE(ConsultarCartera."INGCantidad",0) AS "INGCantidad",
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",
    ConsultarCartera."IdEstado"                         AS "Estado",
    "Nemotecnico"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND "EGRFechaOperacion" = :p_FechaContable 
    AND "IdClasificacionTipoInstrumento" = 'RV' 
    AND "IdTipoInstrumento" = 'SIM'
    AND "IdEstado" IN ('VCD','ANT')
) AS MOV
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = MOV."Nemotecnico")
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = INS."IdEmisor" AND DET."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
	 		 ON MF."IdMoneda" 			= MOV."IdMoneda" 
	 		AND MF."IdUsoMonedaFormato" = 'GEN'
	 		AND MF."IdTipoInstrumento" 	= ''
	 		AND MF."IdTipoOperacion" 	= ''
WHERE DET."FlagDeterioro" = 'S' AND DET."PorcentajeDeterioro" > 0;