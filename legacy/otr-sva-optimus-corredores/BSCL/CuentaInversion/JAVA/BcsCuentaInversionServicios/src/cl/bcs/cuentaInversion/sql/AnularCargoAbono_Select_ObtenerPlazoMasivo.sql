SELECT
	TO_BIGINT(0) as "IdInterno", 
	:FechaMovimiento as "FechaDesde"
	,TO_NVARCHAR(CASE WHEN CA."FechaEfectivaMovimiento" <= :FechaMovimiento THEN :FechaMovimiento ELSE CA."FechaEfectivaMovimiento" END, 'YYYY-MM-DD') as "FechaHasta"
--	,EM."IdPais" as "IdPais"
	, 'S' as "SoloDiasHabiles"
FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" as CA
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" as EM ON EM."IdEmpresa" = CA."IdEmpresa" AND EM."IdEmpresa" = :IdEmpresa;