SELECT   
    "IdTarjetaExistencia"                                       AS "IdMovimiento"
    ,TO_BIGINT(:IdTipoCentralizacion)                           AS "IdTipoCentralizacion"
    ,CASE WHEN "Perdida"<>0 THEN 'PERDIDA' ELSE 'GANANCIA' END  AS "IdEvento"
    ,CASE WHEN "Perdida"<>0 THEN 'PER' ELSE 'GAN' END           AS "Nombre"             
    ,CASE WHEN "Perdida"<>0 THEN "Perdida" ELSE "Ganancia" END  AS "MontoEvento"
    ,"IdMoneda"                                                 AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaTarjetaDeExistencia"
WHERE "FechaProceso"   	= :FechaContable
AND "IdEmpresa" 		= :IdEmpresa
AND "Tipo"           	= 'EGRESO'
AND "IdEstado"       	= 'VIG';