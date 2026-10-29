SELECT 
	  MAX(CASE WHEN "TipoParametro"='RUT'	THEN "Valor" ELSE NULL END) AS "RutCliente"
	, MAX(CASE WHEN "TipoParametro"='IDCLI'	THEN "Valor" ELSE NULL END) AS "IdCliente"
	, MAX(CASE WHEN "TipoParametro"='TRUT'	THEN "Valor" ELSE NULL END) AS "TRUT"
	, MAX(CASE WHEN "TipoParametro"='PERI'	THEN "Valor" ELSE NULL END) AS "Periodo"				
	, MAX(CASE WHEN "TipoParametro"='USUA'	THEN "Valor" ELSE NULL END) AS "Usuario"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" 
WHERE "IdSolicitudCircular" = :IdSolicitudCircular AND "TipoParametro"<>'RUT_MAS'
GROUP BY "IdSolicitudCircular"