SELECT
    "IdTarjetaExistencia"                                       AS "IdMovimiento"
    ,TO_BIGINT(:IdTipoCentralizacion)                           AS "IdTipoCentralizacion"
    ,CASE WHEN "Perdida"<>0 THEN 'PER' ELSE 'GAN' END           AS "IdTipoMovimiento"               
    ,"IdEmpresa"                                                AS "IdEmpresa"
    ,"FolioCartera"                                             AS "Numero"
    ,TO_BIGINT(0)                                               AS "Linea"
    ,YEAR("FechaProceso") ||'-'||  RIGHT('0' || MONTH("FechaProceso"),2) ||'-'|| RIGHT('0' || DAYOFMONTH("FechaProceso"),2) AS "Fecha"
    ,"DocumentoIdentidad"                                       AS "DocumentoIdentidad"
    ,"IdPortafolio"                                             AS "IdPortafolio"
    ,TO_BIGINT(YEAR("FechaProceso"))                            AS "Ejercicio"
    ,RIGHT('0' || MONTH("FechaProceso"),2) ||'-'|| YEAR("FechaProceso") AS "Periodo"
    ,CASE WHEN "Perdida"<>0 THEN "Perdida" ELSE "Ganancia" END  AS "Monto"
    ,"IdMoneda"                                                 AS "IdMoneda"
    ,"IdEstado"                                                 AS "Estado"     
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia"
WHERE "FechaProceso"   	= :FechaContable
AND "IdEmpresa" 		= :IdEmpresa
AND "Tipo"           	= 'EGRESO'
AND "IdEstado"       	= 'VIG';

