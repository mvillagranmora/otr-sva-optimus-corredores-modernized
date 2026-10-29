SELECT TO_DECIMAL(COALESCE(SUM(ccs."INGMontoAPlazo"),0.0),26,8) AS "Monto"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" ccs
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" cpcp ON (cpcp."IdPortafolio" = ccs."IdPortafolio" AND cpcp."IdEmpresa" = :p_IdEmpresa)
WHERE ccs."IdTipoOperacion" = 'COMSIM'
	AND TO_DATE(ccs."FechaVencimientoOperacion",'YYYY-MM-DD') BETWEEN TO_DATE(:p_FechaConsulta,'YYYY-MM-DD') and add_days(TO_DATE(:p_FechaConsulta,'YYYY-MM-DD'),7)
	AND ccs."IdEstado" = 'VIG';
