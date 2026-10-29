INSERT INTO "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCuentaInversion"
( 
	"Saldo",
	"IdCliente",
	"IdPortafolio",
	"CodigoPortafolio",
	"SecuenciaSolicitud"
)
SELECT 
	TO_DECIMAL(SUM(CASE WHEN "IdMoneda" != 'CLP' THEN ("Saldo" * :ValorDolar)
						ELSE ("Saldo") END 
				  ),26,8) AS "Saldo",
	"IdCliente",
	"IdPortafolio",
	"CodigoPortafolio",
	:SecuenciaSolicitud AS "SecuenciaSolicitud"
FROM
(
------------MOVIMIENTOS
	SELECT   
		CI."IdMoneda"
		, SUM(CASE WHEN c."IdEstado" = 'CRT' THEN (CASE WHEN c."IdTipoMovimiento" = 'MOVCAR' THEN  -1*c."Monto" ELSE c."Monto" END) ELSE 0 END) AS "Saldo"
		,CI."IdCliente"
		,P."IdPortafolio"
		,P."CodigoPortafolio"
	FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CI ON CI."IdCuentaInversion" = c."IdCuentaInversion"
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON CI."IdCliente" = P."IdCliente" AND P."IdPortafolio" = CI."IdPortafolio" AND P."CodigoPortafolio" IN ('DC-R','APV-AN','BF','APV-A','APV-B','DC-DL','DC-AL')
	WHERE c."IdEstado" IN ('CRT','PEN') 
	AND (COALESCE(c."FechaSistema",TO_DATE(:FechaHasta)) = :FechaHasta)
	GROUP BY CI."IdMoneda",CI."IdCliente",P."CodigoPortafolio",P."IdPortafolio"

	UNION
-----------SALDO FECHA ANTERIOR EFECTIVA
	SELECT "IdMoneda","Saldo","IdCliente","IdPortafolio","CodigoPortafolio"
	FROM (
		SELECT
		    row_number() OVER (partition by B."IdCuentaInversion" order BY B."FechaSistema" DESC, B."IdCargoAbonoMovimiento" DESC) AS "Row"
			,B."IdCuentaInversion"
			,B."SaldoPorFechaMovimiento" AS "Saldo"
			,R."FechaAnterior"
			,R."MontoCargoPendiente" AS "SaldoCargoPendienteAnterior"
			,R."MontoAbonoPendiente" AS "SaldoAbonoPendienteAnterior"
			,R."IdMoneda"
			,R."IdCliente"
			,R."IdPortafolio"
			,R."CodigoPortafolio"
		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" B
		INNER JOIN (
			SELECT   
				c."IdCuentaInversion"
				,MAX(CASE WHEN c."IdEstado" = 'CRT' THEN c."FechaSistema" END) as "FechaAnterior"
				,SUM(CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVCAR' THEN c."Monto" ELSE 0 END) AS "MontoCargoPendiente"
				,SUM(CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVABN' THEN c."Monto" ELSE 0 END) AS "MontoAbonoPendiente"
				,CI."IdMoneda"
				,CI."IdCliente"
				,P."IdPortafolio"
				,P."CodigoPortafolio"
			FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c 
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CI ON CI."IdCuentaInversion" = c."IdCuentaInversion"
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON CI."IdCliente" = P."IdCliente" AND P."IdPortafolio" = CI."IdPortafolio" AND P."CodigoPortafolio" IN ('DC-R','APV-AN','BF','APV-A','APV-B','DC-DL','DC-AL')
			WHERE c."IdEstado" IN ('CRT','PEN') 
			AND COALESCE(c."FechaSistema",TO_DATE(:FechaHasta)) < :FechaHasta
			GROUP BY c."IdCuentaInversion",CI."IdMoneda",CI."IdCliente",P."IdPortafolio",P."CodigoPortafolio"
		) AS R ON 
		(
			B."FechaSistema" = R."FechaAnterior"
			AND B."IdCuentaInversion" = R."IdCuentaInversion"
		)
		WHERE B."IdEstado" = 'CRT'
	) 
	WHERE "Row" = 1
)
WHERE "Saldo" != 0
GROUP BY "IdCliente","CodigoPortafolio","IdPortafolio";