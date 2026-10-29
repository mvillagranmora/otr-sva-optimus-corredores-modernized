WITH 
	CargoAbonoMovimiento AS (
		SELECT 	cuentainversion."IdCuentaInversion",
				cuentainversion."IdCliente",
				cuentainversion."IdPortafolio",
				indicadorCliente."ValorIndicador",
				cuentainversion."IdMoneda",
				cargoabonomov."Saldo",
				cargoabonomov."IdCargoAbonoMovimiento",
				cargoabonomov."FechaCertificacion",
				cargoabonomov."FechaEfectivaMovimiento",
				cargoabonomov."FechaIngresoRegistro",
				cargoabonomov."IdEjecutivo",
				RANK() OVER ( PARTITION BY cuentainversion."IdCuentaInversion"  
					ORDER BY  cargoabonomov."FechaCertificacion" DESC, cargoabonomov."FechaIngresoRegistro" DESC, cargoabonomov."IdCargoAbonoMovimiento" DESC) AS "Rank"
		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
		INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion 
			ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteAnalitica" AS indicadorCliente 
			ON (cuentainversion."IdCliente" = indicadorCliente."IdCliente" AND indicadorCliente."CodigoTipoIndicador"= 'RELCORR')
		WHERE cargoabonomov."IdEstado" = 'CRT'
	)
SELECT 	CAM."IdCuentaInversion",
		PER."IdPersona",
		CAM."IdCliente",
		DI."DocumentoIdentidad",
		CAM."IdPortafolio",
		(POR."CodigoPortafolio" ||' - '||POR."Proposito") AS "CodigoProposito",
		CASE WHEN CAM."ValorIndicador" = 'S' THEN 'R' ELSE '' END AS "Relacion",
		PER."NombreCompleto" AS "NombreCliente",
		CAM."FechaCertificacion",
		CAM."FechaEfectivaMovimiento",
		CAM."FechaIngresoRegistro",
		CAM."IdMoneda",
		CAM."Saldo",
		TO_DECIMAL(0,26,8) AS "SaldoRetenido",
		TO_DECIMAL(0,26,8) AS "MoneyMarket",
		TO_DECIMAL(0,26,8) AS "Garantias",
		TO_DECIMAL(0,26,8) AS "Simultanea",
		TO_DECIMAL(0,26,8) AS "Pacto",
		AGT."NombreCompleto" AS "NombreAgenteCliente", /* EJECUTIVO DEL CLIENTE */
		AGT."DocumentoIdentidad" AS "DocumentoIdentidadAgente",
		CAM."IdEjecutivo",
		EMP."NombreCompleto" AS "NombreEjecutivoMovimiento", /* EJECUTIVO INSERT MOVIMIENTO */
		EMP."DocumentoIdentidad" AS "DocumentoIdentidadEjecutivo"
FROM CargoAbonoMovimiento AS CAM
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CAM."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PER ON (PER."IdPersona" = CLI."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (PER."IdPersona" = DI."IdPersona")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR ON (POR."IdPortafolio" = CAM."IdPortafolio")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS AGT ON (AGT."IdPersona" = CLI."IdPersonaEjecutivo")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP ON (EMP."IdEmpleado" = CAM."IdEjecutivo")
WHERE "Rank" = 1 
AND CLI."IdEmpresa" = :IdEmpresa
AND 
	(:IdMoneda = '0'OR CAM."IdMoneda" = :IdMoneda)
AND -- CONDICION TIPOSALDO
(
	(:TipoSaldo = 'NEG' AND CAM."Saldo"<0 ) OR 
	(:TipoSaldo = 'POS' AND CAM."Saldo">=0) OR 
	(:TipoSaldo = 'TOD')
)	
ORDER BY CAM."FechaEfectivaMovimiento" DESC, CAM."IdCuentaInversion"

