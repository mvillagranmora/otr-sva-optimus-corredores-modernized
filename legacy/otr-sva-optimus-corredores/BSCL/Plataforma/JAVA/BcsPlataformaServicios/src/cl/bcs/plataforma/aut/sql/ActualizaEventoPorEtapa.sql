	
UPDATE EVT 
SET EVT."IdEstado" = CASE WHEN DUMMY.AUTORIZADO > 0 AND DUMMY.TOTAL = DUMMY.AUTORIZADO THEN 'AUT'
				          WHEN DUMMY.RECHAZADO > 0 THEN 'RZD'
				          ELSE EVT."IdEstado"
                     END,
	EVT."Comentario" = 
    	CASE 
        	WHEN DUMMY.RECHAZADO > 0 THEN :Comentario
        	ELSE ''
    	END,
	EVT."IdUsuarioCambioEstado" 	= :IdUsuarioCambioEstado,
	EVT."FechaHoraCambioEstado" 	= :FechaHoraCambioEstado                     
FROM "BSCL.Plataforma.BD::AUT.Evento" EVT
JOIN (
		select 
			COUNT(1) AS TOTAL,
			SUM(CASE WHEN "IdEstado" = 'AUT' THEN 1 ELSE 0 END) AS AUTORIZADO,
			SUM(CASE WHEN "IdEstado" = 'RZD' THEN 1 ELSE 0 END) AS RECHAZADO
		from "BSCL.Plataforma.BD::AUT.EtapaEvento"  where  "IdEvento" = :IdEvento
	) as DUMMY
ON  EVT."IdEvento" = :IdEvento  AND EVT."IdEstado" = 'VIG' 

