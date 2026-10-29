SELECT 
	 MOVS."IdMovimiento"
    ,MOVS."IdTipoCentralizacion"
    ,MOVS."IdTipoMovimiento"
    ,MOVS."IdEmpresa"                                    
    ,MOVS."Numero"
    ,MOVS."Linea"
	,MOVS."Fecha"
	,MOVS."DocumentoIdentidad" 
	,MOVS."IdPortafolio"
    ,MOVS."Ejercicio"
    ,MOVS."Periodo"
    ,COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MOVS."Monto",MF."MontoDecimales",MF."MontoTrucaRedondea"), MOVS."Monto") AS "Monto"
   	,MOVS."IdMoneda"                                   
    ,MOVS."Estado"
FROM (
	SELECT 
    CAR."IdCartera"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    'DETMMVRF'                                         AS "IdTipoMovimiento",
    CAR."IdEmpresa"                        AS "IdEmpresa",                                     
    CAR."FolioCartera"                     AS "Numero",
    CAR."FolioCartera"                     AS "Linea",
    --          YEAR(CAR."INGFechaOperacion") ||'-'|| RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(CAR."INGFechaOperacion"),2) AS "Fecha",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    CP."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    CAR."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(CAR."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| YEAR(CAR."INGFechaOperacion")  AS "Periodo",
    CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
		THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGValorRescate",0) AS DECIMAL(26,8)))
		ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGCantidad",0) AS DECIMAL(26,8)))
	END AS "Monto",
    CASE WHEN CAR."IdMonedaInstrumento"  <> '' THEN CAR."IdMonedaInstrumento" ELSE CAR."IdMonedaSistema" END AS "IdMoneda",                                     
    CAR."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND   CAR."IdEstado" = 'VIG'
AND   CAR."IdTipoOperacion" IN ('COMRF','INGTIT')
AND   CAR."IdTipoProducto" IN( 'INRF','CUST') 
AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
AND   DET."FlagDeterioro" = 'S'
AND   DET."PorcentajeDeterioro" > 0
AND   CAR."FechaOperacion" <=:p_FechaContable
AND   CP."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora

UNION ALL 


SELECT 
    CAR."IdCartera"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    'DETMMVRF'                                         AS "IdTipoMovimiento",
    CAR."IdEmpresa"                        AS "IdEmpresa",                                     
    CAR."FolioCartera"                     AS "Numero",
    CAR."FolioCartera"                     AS "Linea",
    --          YEAR(CAR."INGFechaOperacion") ||'-'|| RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(CAR."INGFechaOperacion"),2) AS "Fecha",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    CP."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    CAR."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(CAR."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| YEAR(CAR."INGFechaOperacion")  AS "Periodo",
    CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
		THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGValorRescate",0) AS DECIMAL(26,8)))
		ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGCantidad",0) AS DECIMAL(26,8)))
	END AS "Monto",
    CASE WHEN CAR."IdMonedaInstrumento"  <> '' THEN CAR."IdMonedaInstrumento" ELSE CAR."IdMonedaSistema" END AS "IdMoneda",                                    
    CAR."IdEstado"                         AS "Estado"
FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva"
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CPO ON CPO."IdPortafolio" = OPE."IdPortafolioCorredor" 
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND   OPE."IdEstado" = 'VIG'
AND   CAR."IdEstado" = 'VIG'
AND   CAR."IdTipoOperacion" IN ('COMTER')
AND   CAR."IdTipoProducto" = 'MERF'
AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
AND   CAR."IdTipoInstrumento" != 'PAC'
AND   DET."FlagDeterioro" = 'S'
AND   DET."PorcentajeDeterioro" > 0
AND   CAR."FechaOperacion" <=:p_FechaContable
AND   CPO."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora


UNION ALL 
SELECT
    CAR."IdCartera"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    'DETMMVRF'                                         AS "IdTipoMovimiento",
    CAR."IdEmpresa"                        AS "IdEmpresa",                                     
    CAR."FolioCartera"                     AS "Numero",
    CAR."FolioCartera"                     AS "Linea",
    --          YEAR(CAR."INGFechaOperacion") ||'-'|| RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(CAR."INGFechaOperacion"),2) AS "Fecha",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    CP."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    CAR."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(CAR."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| YEAR(CAR."INGFechaOperacion")  AS "Periodo",
    CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
		THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGValorRescate",0) AS DECIMAL(26,8)))
		ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."INGCantidad",0) AS DECIMAL(26,8)))
	END AS "Monto",
    CASE WHEN CAR."IdMonedaInstrumento"  <> '' THEN CAR."IdMonedaInstrumento" ELSE CAR."IdMonedaSistema" END AS "IdMoneda",                                    
    CAR."IdEstado"                         AS "Estado"

FROM "BSCL.Mesa.BD::MES.Operacion" AS OPE  
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RES."IdReserva"
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CPO ON CPO."IdPortafolio" = OPE."IdPortafolioCorredor" 
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CAR."IdEmisor" AND DET."IdEstado" = 'ACT'
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND   OPE."IdEstado" = 'VIG'
AND   CAR."IdEstado" IN ('VIG','RPA')
AND   CAR."IdTipoOperacion" IN ('VENPAC')
AND   CAR."IdTipoProducto" = 'MERF'
AND   CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
AND   CAR."IdTipoInstrumento" != 'PAC'
AND   DET."FlagDeterioro" = 'S'
AND   DET."PorcentajeDeterioro" > 0
AND   CAR."FechaOperacion" <=:p_FechaContable
AND   CPO."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora

UNION ALL
	--Garantias
	
SELECT
    GAR."IdGarantia"                        AS "IdMovimiento",
    TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion",
    'DETMMVRF'                                         AS "IdTipoMovimiento",
    GAR."IdEmpresa"                        AS "IdEmpresa",                                     
    GAR."FolioCarteraOrigen"                     AS "Numero",
    GAR."FolioCarteraOrigen"                     AS "Linea",
    --          YEAR(CAR."INGFechaOperacion") ||'-'|| RIGHT('0' || MONTH(CAR."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(CAR."INGFechaOperacion"),2) AS "Fecha",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    CP."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    GAR."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(GAR."INGFechaOperacion"))          AS "Ejercicio",
    RIGHT('0' || MONTH(GAR."INGFechaOperacion"),2) ||'-'|| YEAR(GAR."INGFechaOperacion")  AS "Periodo",
    CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'IF'
		THEN ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(GAR."INGValorRescate",0) AS DECIMAL(26,8)))
		ELSE ABS(CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(GAR."INGCantidad",0) AS DECIMAL(26,8)))
	END AS "Monto",
	CASE WHEN GAR."IdMonedaInstrumento"  <> '' THEN GAR."IdMonedaInstrumento" ELSE GAR."IdMonedaSistema" END AS "IdMoneda",                                    
    GAR."IdEstado"                         AS "Estado"
FROM  "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"  AS GAR
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = GAR."IdPortafolio" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen" AND CAR."IdEstado" = 'VIG'
INNER JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = GAR."IdEmisor" AND DET."IdEstado" = 'ACT'
WHERE GAR."IdEmpresa" = :p_IdEmpresa
AND GAR."IdEstado"='VIG' 
AND GAR."IdClasificacionTipoInstrumento" in ('RF','IF')
AND DET."FlagDeterioro" = 'S'
AND DET."PorcentajeDeterioro" > 0
AND GAR."FechaOperacion" <=:p_FechaContable
AND CP."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora) AS MOVS
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
	 		 ON MF."IdMoneda" 			= MOVS."IdMoneda" 
	 		AND MF."IdUsoMonedaFormato" = 'GEN'
	 		AND MF."IdTipoInstrumento" 	= ''
	 		AND MF."IdTipoOperacion" 	= '';
