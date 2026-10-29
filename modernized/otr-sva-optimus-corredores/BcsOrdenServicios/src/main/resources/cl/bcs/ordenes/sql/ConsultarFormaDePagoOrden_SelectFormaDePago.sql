SELECT 
	TO_BIGINT(0) AS "IdOrden",
	PREPAGPOR."IdPreferenciaPagoPortafolio",
	PREPAG."IdMedioDePago",
	PREPAGPOR."IdCuentaBancaria",
	CUBAN."IdTipoCuentaBancaria",
	CUBAN."NumeroCuentaBancaria",
	PREPAGPOR."IdTarjetaBancaria",
	TARBAN."IdTipoTarjetaBancaria",
	TARBAN."NumeroTarjetaBancaria",
	MODPAG."IdModalidadDePago",
	CUBAN."IdBanco",
	PREPAGPOR."PorDefecto"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PREPAGPOR
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PREPAG
	ON PREPAGPOR."IdPreferenciaPago" = PREPAG."IdPreferenciaPago"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CUBAN
	ON PREPAGPOR."IdCuentaBancaria" = CUBAN."IdCuentaBancaria"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" TARBAN
	ON PREPAGPOR."IdTarjetaBancaria" = TARBAN."IdTarjetaBancaria"
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MODPAG
	ON PREPAG."IdMedioDePago" = MODPAG."IdMedioDePago"
WHERE PREPAGPOR."IdPortafolio" = :IdPortafolio
	AND PREPAG."IdTipoOperacion" = :IdTipoOperacion
	AND PREPAG."IdMoneda" = :IdMonedaLiquidacionOrden
	AND PREPAG."IdEstado" = 'ACT';