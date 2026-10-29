WITH cuentas_inv_clientes AS (
	SELECT DISTINCT ci."IdCuentaInversion",ci."IdCliente"
	FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" ci
	JOIN "BSCL.SocioDeNegocio.BD::GBL.ConsultaAvanzadaClientes_Proceso_Paso" capp ON capp."IdCliente" = ci."IdCliente"
)
SELECT 
	COALESCE(MAX(cargoAbMov."FechaSistema"),TO_DATE('0001-01-01')) AS "FechaSistema",
	cuentas_inv_clientes."IdCliente"
FROM cuentas_inv_clientes
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" cargoAbMov ON (cuentas_inv_clientes."IdCuentaInversion" = cargoAbMov."IdCuentaInversion")
GROUP BY cuentas_inv_clientes."IdCliente";