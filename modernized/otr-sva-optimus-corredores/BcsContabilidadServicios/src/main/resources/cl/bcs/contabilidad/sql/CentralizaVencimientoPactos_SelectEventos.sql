    SELECT
        ConsultarCartera."IdOperacion"   AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'MONTO_PAPEL'                              AS "IdEvento"
        ,'Monto Papel'                              AS "Nombre"
        ,ConsultaSubyacente."MontoSubyacente"       AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')
        
UNION 
    SELECT
        ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'VALOR_PAPEL'                                              AS "IdEvento"
        ,'Valor Papel'                                              AS "Nombre"
        ,ConsultaSubyacente."MontoSubyacenteMonedaInstrumento"      AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"                   AS "CodigoMoneda"   
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')
        AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
UNION
    SELECT
        ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'MONTO_INICIAL_PACTO'                      AS "IdEvento"
        ,'Monto Inicial Pacto'                      AS "Nombre"
        ,"MontoCompra"                              AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')

UNION
    SELECT
        ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'MONTO_FINAL_PACTO'                        AS "IdEvento"
        ,'Monto Final Pacto'                        AS "Nombre"
        ,"INGMontoAPlazo"                           AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')


UNION
    SELECT
        ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'MONTO_DIFERENCIA'                         AS "IdEvento"
        ,'Monto Diferencia'                         AS "Nombre"
        ,("INGMontoAPlazo"  - "MontoCompra")        AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')

UNION

    SELECT
        ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'VALOR_INICIAL_PACTO'                      AS "IdEvento"
        ,'Valor Inicial Pacto'                      AS "Nombre"
        ,ConsultarCartera."MontoCompraMonedaInstrumento"            AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"                       AS "CodigoMoneda"   
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')
        AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
UNION
    SELECT
        ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'VALOR_FINAL_PACTO'                        AS "IdEvento"
        ,'Valor Final Pacto'                        AS "Nombre"
        ,"MontoAPlazoMonedaInstrumento"             AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"   AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')
        AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
UNION
    SELECT
         ConsultarCartera."IdOperacion"  AS "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
        ,'VALOR_DIFERENCIA'                                                 AS "IdEvento"
        ,'Valor Diferencia'                                                 AS "Nombre"
        ,("MontoAPlazoMonedaInstrumento" - "MontoCompraMonedaInstrumento")  AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"                           AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
    INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
        AND "FechaVencimientoOperacion" = :p_FechaContable              
        AND "IdTipoOperacion" in ('COMPAC','VENPAC')
        AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento";
        