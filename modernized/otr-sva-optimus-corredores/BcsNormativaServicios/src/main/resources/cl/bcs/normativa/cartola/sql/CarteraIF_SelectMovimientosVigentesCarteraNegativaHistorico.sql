SELECT
	CAR."IdCliente",
	CAR."IdPortafolio",
	CAR."IdCarteraNegativa" AS "IdCartera",
	CAR."IdCarteraNegativa" AS "FolioCartera",
	CAR."FechaOperacion" AS "FechaOperacion",
	CAR."FechaLiquidacion" as "FechaLiquidacion",
	CAR."IdBoveda",
	INS."IdTipoInstrumento",
	CAR."IdInstrumento",
	CAR."IdEmisor",
	CAR."FechaVencimientoOperacion" AS "FechaVencimientoInstrumento",
	CAR."IdTipoReajuste",
	INS."FechaEmision" AS "FechaEmisionInstrumento",
	CAR."Nemotecnico",
	- 1 * CAR."Cantidad" AS "CantidadOriginal",
	- 1 * CAR."ValorRescate" AS "ValorRescateOriginal",
	- 1 * CAR."Cantidad" AS "CantidadCartera",
	- 1 * CAR."ValorRescate" AS "ValorRescateCartera",
	CAR."IdMonedaInstrumento",
	CAR."TasaOperacion" AS "TasaCompra",
	CAR."PrecioOperacion" AS "PrecioCompra",
	CAR."MontoOperacionMonedaInstrumento" AS "MontoCompraMonedaSistema",
	CAR."MontoOperacionMonedaInstrumento" AS "MontoCompraMonedaAdicional",
	CAR."TasaMercado" AS "TasaMercado",
	CAR."PrecioMercado" AS "PrecioMercado",
	- 1 * CAR."MontoMercadoMonedaSistema" AS "MontoMercadoMonedaSistema",
	- 1 * CAR."MontoMercadoMonedaSistema" AS "MontoMercadoMonedaAdicional",
	--			,CAR."MontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema" 
	car."MontoInteresCorteCupon" AS "MontoInteresCorridoMercadoActualMonedaSistema",
	car."MontoInteresCorteCupon" AS "MontoInteresCorridoMercadoActualMonedaAdicional", 
	--			,CAR."MontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
	'S' AS "TieneCorteDeCupon",
	CAR."FechaPagoCorteCupon" AS "FechaPagoCorteCupon",
CASE
		WHEN CAR."FechaPagoCorteCupon" <= :FechaHasta THEN CAR."MontoCapitalCorteCupon"
		ELSE TO_DECIMAL(0, 26, 8)
	END as "CuponesPagadosMonedaSistema"
FROM
	"BSCL.Custodia.BD::HIS.CarteraNegativa" CAR
	INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
WHERE CAR."FechaRespaldo" = :FechaHasta
	AND INS."IdClasificacionTipoInstrumento" = 'IF'
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdTipoOperacion" != 'VENPAC'
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."FechaOperacion" <= :FechaHasta
	AND INS."IdTipoInstrumento" NOT IN ('SIM', 'PRT', 'EMP', 'PAC', 'RRV', 'RRF', 'PRF', 'ERF')