SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS
SELECT
	ComprobanteFormaDePago."IdComprobanteFormaDePago" ,
	ComprobanteFormaDePago."IdComprobante" ,
	ComprobanteFormaDePago."IdTipoMovimiento" ,
	TipoMovimiento."Nombre" AS "NombreTipoMovimiento" ,
	ComprobanteFormaDePago."IdMedioDePago" ,
	ComprobanteFormaDePago."ClaveDCV" ,
	ComprobanteFormaDePago."NombreMedioDePago" ,
	MedioDePago."TipoMedioDePago" ,
	ComprobanteFormaDePago."IdModalidadDePago" ,
	ComprobanteFormaDePago."NombreModalidadDePago" ,
	Comprobante."IdCaja" ,
	Caja."Nombre" AS "NombreCaja" ,
	Caja."Codigo" AS "CodigoCaja" ,
	ComprobanteFormaDePago."IdCuentaBancariaCorredora" ,
	ComprobanteFormaDePago."IdTipoCuentaBancariaCorredora" ,
	ComprobanteFormaDePago."NombreTipoCuentaBancariaCorredora" ,
	BancoCorredora."IdBanco" AS "IdBancoCorredora" ,
	BancoCorredora."NombreBanco" AS "NombreBancoCorredora" ,
	ComprobanteFormaDePago."NombreCuentaBancariaCorredora" ,
	ComprobanteFormaDePago."NumeroCuentaBancariaCorredora" ,
	ComprobanteFormaDePago."IdTarjetaBancariaCorredora" ,
	ComprobanteFormaDePago."IdTipoTarjetaBancariaCorredora" ,
	ComprobanteFormaDePago."NombreTipoTarjetaBancariaCorredora" ,
	ComprobanteFormaDePago."NumeroTarjetaBancariaCorredora" ,
	ComprobanteFormaDePago."IdChequera" ,
	Chequera."Serie" AS "NumeroSerieCorredora" ,
	ComprobanteFormaDePago."NumeroDocumentoCorredora" ,
	ComprobanteFormaDePago."FechaDocumentoPagoCorredora" ,
	Comprobante."TipoComprobante" ,
	Comprobante."IdEstado" AS "IdEstadoComprobante" ,
	ComprobanteFormaDePago."IdCuentaBancariaSocio" ,
	ComprobanteFormaDePago."IdTipoCuentaBancariaSocio" ,
	ComprobanteFormaDePago."NombreTipoCuentaBancariaSocio" ,
	ComprobanteFormaDePago."NombreCuentaBancariaSocio" ,
	ComprobanteFormaDePago."IdBancoSocio" ,
	BancoSocio."NombreBanco" AS "NombreBancoSocio" ,
	ComprobanteFormaDePago."NumeroCuentaBancariaSocio" ,
	ComprobanteFormaDePago."IdTarjetaBancariaSocio" ,
	ComprobanteFormaDePago."IdTipoTarjetaBancariaSocio" ,
	ComprobanteFormaDePago."NombreTipoTarjetaBancariaSocio" ,
	ComprobanteFormaDePago."NumeroTarjetaBancariaSocio" ,
	ComprobanteFormaDePago."NumeroSerieSocio" ,
	ComprobanteFormaDePago."NumeroDocumentoSocio" ,
	ComprobanteFormaDePago."FechaDocumentoPagoSocio" ,
	ComprobanteFormaDePago."IdMoneda" ,
	Moneda."Nombre" AS "NombreMoneda" ,
	ComprobanteFormaDePago."Monto" ,
	(CASE
		WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVEGR' THEN ComprobanteFormaDePago."Monto"
		ELSE 0
	END) AS "MontoEgreso" ,
	(CASE
		WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVING' THEN ComprobanteFormaDePago."Monto"
		ELSE 0
	END) AS "MontoIngreso" ,
	(CASE
		WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVING' THEN ComprobanteFormaDePago."Monto"
		ELSE -1 * ComprobanteFormaDePago."Monto"
	END) AS "MontoConSigno" ,
	ComprobanteFormaDePago."FechaIngresoRegistro" ,
	ComprobanteFormaDePago."IdEstado" ,
	Estado."Nombre" AS "NombreEstado" ,
	ComprobanteFormaDePago."IdUsuarioGiro" ,
	UsuarioGiro."NombreCompleto" AS "NombreUsuarioGiro" ,
	ComprobanteFormaDePago."FechaGiro" ,
	COALESCE(ComprobanteFormaDePago."IdComprobantePartida",
	0) AS "IdComprobantePartida" ,
	Comprobante."IdTipoPersona" ,
	TipoSocio."NombreTipoSocioDeNegocio" ,
	Comprobante."IdPersona" ,
	Comprobante."NombrePersona" ,
	Comprobante."IdTipoDocumentoIdentidad" ,
	Comprobante."DocumentoIdentidad" ,
	Comprobante."IdTipoPersonaTitularPago" ,
	Comprobante."IdPersonaTitularPago" ,
	Comprobante."NombrePersonaTitularPago" ,
	Comprobante."IdPortafolioTitularPago" ,
	Portafolio."CodigoPortafolio" AS "CodigoPortafolioTitularPago" ,
	Portafolio."CodigoPortafolio" || ' ' || Portafolio."Proposito" AS "NombrePortafolioTitularPago" ,
	Comprobante."IdConcepto" ,
	Concepto."Nombre" AS "NombreConcepto" ,
	ComprobanteFormaDePago."IdDeposito" ,
	COALESCE(ComprobanteFormaDePago."Depositado",
	'N') AS "Depositado" ,
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(COALESCE(ComprobanteFormaDePago."Depositado",
	'N')) AS "DepositadoTraduccion" ,
	ComprobanteFormaDePago."IdNominaTransferencia" ,
	Comprobante."IdEmpresa" ,
	ComprobanteFormaDePago."IdUsuarioCertificacion" ,
	UsuarioCertifica."NombreCompleto" AS "NombreUsuarioCertificacion" ,
	ComprobanteFormaDePago."FechaHoraCertificacion" ,
	ComprobanteFormaDePago."IdUsuarioLiquidacion" ,
	UsuarioLiquidacion."NombreCompleto" AS "NombreUsuarioLiquidacion" ,
	CAST(ComprobanteFormaDePago."FechaIngresoRegistro" AS DATE) AS "FechaMovimiento" ,
	ComprobanteFormaDePago."FechaIngresoRegistro" AS "FechaHoraMovimiento" ,
	CAST(ComprobanteFormaDePago."FechaAfectaSaldo" AS DATE) AS "FechaAfectaSaldo" ,
	ComprobanteFormaDePago."FechaAfectaSaldo" AS "FechaHoraAfectaSaldo" ,
	Comprobante."FechaLiquidacion" AS "FechaLiquidacionComprobante" ,
	ComprobanteFormaDePago."DiasRetencion" ,
	CAST(
	(
		CASE
		WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' THEN ADD_DAYS (TO_DATE (Comprobante."FechaLiquidacion",
		'YYYY-MM-DD'),
		ComprobanteFormaDePago."DiasRetencion")
		ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"
	END) AS DATE) AS "FechaLiquidacion" ,
	(CASE
		WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' THEN ADD_DAYS (TO_DATE (Comprobante."FechaLiquidacion",
		'YYYY-MM-DD'),
		ComprobanteFormaDePago."DiasRetencion")
		ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"
	END) AS "FechaHoraLiquidacion" ,
	ComprobanteFormaDePago."SaldoDisponible" ,
	ComprobanteFormaDePago."SaldoContable" ,
	CASE
		WHEN instr(TipoMovimientoMedioDePago."AfectaSaldoEnEstado",
		ComprobanteFormaDePago."IdEstado")> 0 THEN 'S'
		ELSE 'N'
	END AS "AfectoSaldo" ,
	CASE
		WHEN instr(TipoMovimientoMedioDePago."EstadosPosibleTipoMovimientoMedioDePago",
		'NDP')> 0 THEN 'S'
		ELSE 'N'
	END AS "DocumentoEnviaADeposito"
FROM
	"BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON
	(Comprobante."IdComprobante" = ComprobanteFormaDePago."IdComprobante")
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON
	(ComprobanteFormaDePago."IdEstado" = Estado."IdEstado")
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TipoMovimiento ON
	(TipoMovimiento."IdModulo" = 'TES'
	AND TipoMovimiento."IdTipoMovimiento" = ComprobanteFormaDePago."IdTipoMovimiento")
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON
	(Concepto."IdConcepto" = Comprobante."IdConcepto")
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda ON
	(Moneda."IdMoneda" = ComprobanteFormaDePago."IdMoneda")
INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TipoMovimientoMedioDePago ON
	(TipoMovimientoMedioDePago."IdTipoMovimiento" = ComprobanteFormaDePago."IdTipoMovimiento"
	AND TipoMovimientoMedioDePago."IdMedioDePago" = ComprobanteFormaDePago."IdMedioDePago" )
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS Caja ON
	Caja."IdCaja" = Comprobante."IdCaja"
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Chequera" AS Chequera ON
	(Chequera."IdChequera" = ComprobanteFormaDePago."IdChequera")
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CuentaBancariaCorredora ON
	(CuentaBancariaCorredora."IdCuentaBanco" = ComprobanteFormaDePago."IdCuentaBancariaCorredora")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BancoCorredora ON
	BancoCorredora."IdBanco" = CuentaBancariaCorredora."IdBanco"
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BancoSocio ON
	(BancoSocio."IdBanco" = ComprobanteFormaDePago."IdBancoSocio")
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio ON
	(Portafolio."IdPortafolio" = Comprobante."IdPortafolioTitularPago")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON
	(MedioDePago."IdMedioDePago" = ComprobanteFormaDePago."IdMedioDePago")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TipoSocio ON
	TipoSocio."IdTipoSocioDeNegocio" = Comprobante."IdTipoPersona"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UsuarioLiquidacion ON
	UsuarioLiquidacion."IdUsuario" = ComprobanteFormaDePago."IdUsuarioLiquidacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UsuarioGiro ON
	UsuarioGiro."IdUsuario" = ComprobanteFormaDePago."IdUsuarioGiro"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UsuarioCertifica ON
	UsuarioCertifica."IdUsuario" = ComprobanteFormaDePago."IdUsuarioCertificacion"
WHERE
	ComprobanteFormaDePago."IdEstado" <> 'MOD'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
