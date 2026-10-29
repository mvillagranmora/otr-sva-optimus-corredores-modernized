SELECT
    TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion",
    ConsultaSubyacente."IdSubyacente"                   AS "IdMovimiento",
    CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'   THEN 'VPACTOVTA'            
        WHEN 'COMPAC' THEN  'VPACTOCOMP'

        ELSE '????????????'         
    END                                                 AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."IdOperacion"                   AS "Numero",
    TO_BIGINT(0)                                        AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."EGRFechaOperacion"))   AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarCartera."EGRFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."EGRFechaOperacion")  AS "Periodo",
    ABS(ConsultaSubyacente."MontoSubyacente")  AS "Monto",     
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",                                
    ConsultarCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND ConsultarCartera."FechaVencimientoOperacion" = :p_FechaContable              
    AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
    AND Operacion."IdEstado" = 'LIQ'
UNION ALL
SELECT
    TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion",
    ConsultarCartera."IdCartera"       AS "IdMovimiento",
    CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'   THEN 'VOPACTOVTA'            
        WHEN 'COMPAC'   THEN 'VOPACTOCOM'

        ELSE '????????????'         
    END                                                 AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    TO_BIGINT(0)                                        AS "Linea",
    YEAR(:p_FechaContable) ||'-'|| RIGHT('0' || MONTH(:p_FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:p_FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."EGRFechaOperacion"))       AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarCartera."EGRFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."EGRFechaOperacion")  AS "Periodo",
    ABS(Operacion."MontoOperacionFinal")  				AS "Monto",     
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",                                
    ConsultarCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND ConsultarCartera."FechaVencimientoOperacion" = :p_FechaContable              
    AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
    AND Operacion."IdEstado" = 'LIQ';
