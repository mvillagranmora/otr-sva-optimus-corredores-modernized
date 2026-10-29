SELECT
	DJ.ID,
	DJ."kPeriodoComercial",
	CAST(DJ."KIdContrato" AS BIGINT) AS "IdContrato"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" DJ
WHERE "IdDocumento"=:IdDocumento AND DJ."Seccion"=1 AND DJ."kPeriodoComercial">=:FechaDesde AND DJ."kPeriodoComercial"<=:FechaHasta;