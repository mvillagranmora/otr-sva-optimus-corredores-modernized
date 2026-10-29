SELECT  
	''									AS "Titulo"
	, 'Simultaneas (obligaciones)'		AS "Categoria"
	, 0.0									AS "MontoGarantia"
	, 0.0									AS "MontoBancosyEmpresas"
	, 0.0									AS "FactorA"
	, 0.0									AS "FactorB"
	, TO_DECIMAL(SUM(COALESCE(ccs."INGMontoAPlazo",0.0)),26,8) AS "MontoCobertura"
	, 0.0									AS "MontoEntidadFinanciera"
	,:FechaContable					    AS "Fecha" 
	,TO_BIGINT(:IdEmpresa)				AS "IdEmpresa"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" ccs
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" cpcp ON (cpcp."IdPortafolio" = ccs."IdPortafolio" AND cpcp."IdEmpresa" = :IdEmpresa)
WHERE ccs."IdTipoOperacion" = 'VENSIM'
AND TO_DATE(ccs."FechaVencimientoOperacion",'YYYY-MM-DD') BETWEEN TO_DATE(:FechaContable,'YYYY-MM-DD') and add_days(TO_DATE(:FechaContable,'YYYY-MM-DD'),7)
AND ccs."IdEstado" = 'VIG'