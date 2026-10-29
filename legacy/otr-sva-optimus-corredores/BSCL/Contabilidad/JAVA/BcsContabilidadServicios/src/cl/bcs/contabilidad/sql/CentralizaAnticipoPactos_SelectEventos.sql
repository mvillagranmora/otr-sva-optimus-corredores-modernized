SELECT
        TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"      AS "IdMovimiento"
        ,'MONTO_PAPEL'                              AS "IdEvento"
        ,'Monto Papel'                              AS "Nombre"
        ,ConsultaSubyacente."MontoSubyacente"       AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'      

UNION 

    SELECT
        TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'VALOR_PAPEL'                                              AS "IdEvento"
        ,'Valor Papel'                                              AS "Nombre"
        ,ConsultaSubyacente."MontoSubyacenteMonedaInstrumento"      AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"                   AS "CodigoMoneda"   
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"    = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'
    AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
    
UNION

SELECT
         TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'MONTO_INICIAL_PACTO'                      AS "IdEvento"
        ,'Monto Inicial Pacto'                      AS "Nombre"
        ,"MontoCompra"                              AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'
    
UNION

SELECT
         TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'MONTO_FINAL_PACTO'                        AS "IdEvento"
        ,'Monto Final Pacto'                        AS "Nombre"
        ,"INGMontoAPlazo"                           AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'
    
UNION

SELECT
         TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'MONTO_DIFERENCIA'                         AS "IdEvento"
        ,'Monto Diferencia'                         AS "Nombre"
        ,("INGMontoAPlazo"  - "MontoCompra")        AS "MontoEvento"
        ,"IdMonedaSistema"                          AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'

UNION

SELECT
         TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'VALOR_INICIAL_PACTO'                      AS "IdEvento"
        ,'Valor Inicial Pacto'                      AS "Nombre"
        ,ConsultarCartera."MontoCompraMonedaInstrumento"            AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"                       AS "CodigoMoneda"   
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'
    AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
    
UNION

SELECT
         TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'VALOR_FINAL_PACTO'                        AS "IdEvento"
        ,'Valor Final Pacto'                        AS "Nombre"
        ,"MontoAPlazoMonedaInstrumento"             AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"   AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'
    AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento"
    
UNION

SELECT
        TO_BIGINT(:IdTipoCentralizacion)                                   AS "IdTipoCentralizacion"
        ,ConsultarCartera."IdOperacion"                      AS "IdMovimiento"
        ,'VALOR_DIFERENCIA'                                                 AS "IdEvento"
        ,'Valor Diferencia'                                                 AS "Nombre"
        ,("MontoAPlazoMonedaInstrumento" - "MontoCompraMonedaInstrumento")  AS "MontoEvento"
        ,ConsultaSubyacente."IdMonedaInstrumento"                           AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"     ConsultaSubyacente
ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
WHERE   ConsultarCartera."IdEmpresa"            = :IdEmpresa 
    AND ConsultarCartera."IdTipoInstrumento"    = 'PAC'              
    AND ConsultarCartera."EGRFechaOperacion"       = :FechaContable                   
    AND ConsultarCartera."IdEstado"             ='ANT'
    AND "IdMonedaSistema"   <> ConsultaSubyacente."IdMonedaInstrumento";    