SELECT
	DJ.ID,
	CAST(DJ."KIdContrato" AS BIGINT) AS "IdContrato"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultarMovimientosFWDDJ1" FWD
INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1" DJ
	ON DJ."KIdContrato"=FWD."KIdContrato" AND DJ."Seccion"=5
WHERE 
	(FWD."EGRIdTipoOperacion"='ANTFWD' AND SUBSTRING(TO_VARCHAR(FWD."FechaAnticipo",'YYYY/MM/DD'),1,7)=:periodo AND FWD."FechaAnticipo">=:FechaDesde AND FWD."FechaAnticipo"<=:FechaHasta)
	OR (
		FWD."EGRIdTipoOperacion"='VCDFWD'
		AND ((FWD."IdTipoOperacion"='COMFWD' AND SUBSTRING(TO_VARCHAR(FWD."FechaRecibimos",'YYYY/MM/DD'),1,7)=:periodo AND FWD."FechaRecibimos">=:FechaDesde AND FWD."FechaRecibimos"<=:FechaHasta)
		OR (FWD."IdTipoOperacion"='VENFWD' AND SUBSTRING(TO_VARCHAR(FWD."FechaEntregamos",'YYYY/MM/DD'),1,7)=:periodo AND FWD."FechaEntregamos">=:FechaDesde AND FWD."FechaEntregamos"<=:FechaHasta)
	)
);