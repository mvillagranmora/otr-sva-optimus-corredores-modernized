SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" AS

SELECT
	car."IdOperacion",
	car."FolioOperacion",
	car."FolioCartera",
	car."FechaOperacion",
	car."IdTipoProducto",
	car."IdTipoOperacion",
	tOpe."IdInstrumento",
	tOper."Nombre" AS "NombreTipoOperacion",
	car."IdOperacionRelacionada",
	car."Intermesa",
	car."IdMesaOrigen",
	mesOrigen."Nombre" AS "NombreMesaOrigen",	
	car."IdMesaDestino",
	mesDestino."Nombre" AS "NombreMesaDestino",
	soc."IdSocioDeNegocio",
	car."IdCliente",
	cli."NombreCompleto",
	cli."DocumentoIdentidad",
	car."IdClienteCorredor",
	car."IdPortafolio",
	por."CodigoPropositoPortafolio",
	car."AfectaCustodia",
	car."IdPortafolioCorredor",
	car."IdEmpresa",
	CASE
		WHEN CLI."Tipo" = 'J' THEN CLI."RazonSocial"
		ELSE CLI."NombreCompleto"
	END AS "RazonSocial",
	car."MontoOperacion",
	car."MontoOperacionInicial",
	car."MontoOperacionFinal",
	car."IdTipoReajuste",
	car."Tasa",
	car."IdMoneda",
	car."MontoOperacionMoneda",
	car."MontoOperacionInicialMoneda",
	car."MontoOperacionFinalMoneda",
	car."IdMonedaLiquidacion",
	car."PlazoEnDias",
	car."FechaVencimiento",
	car."FechaLiquidacion",
	car."IdCondicionLiquidacion",
	cond."Nombre" AS "NombreCondicionLiquidacion",
	car."FechaCondicionLiquidacion",
	car."IdAgente",
	per."NombreCompleto" AS "NombreEjecutivo",
	car."IdEstado",
	est."NombreEstado",
	car."FechaCreacion",
	car."TasaAgente",
	car."IdUsuarioCreacion",
	usr."CodigoUsuario",
	car."IdTipoCanalIngreso",
	car."Glosa",
	car."MontoOperacionAnticipo",
	car."MontoOperacionInicialAnticipo",
	car."MontoOperacionFinalAnticipo",
	car."TasaAnticipo",
	car."MontoOperacionMonedaAnticipo",
	car."MontoOperacionInicialMonedaAnticipo",
	car."MontoOperacionFinalMonedaAnticipo",
	car."IdMonedaLiquidacionAnticipo",
	car."IdCondicionLiquidacionAnticipo",
	condAnt."Nombre" AS "NombreCondicionLiquidacionAnticipo",
	car."FechaCondicionLiquidacionAnticipo",
	car."IdEmisor",
	res."IdConcepto" AS "IdConceptoDetalle",
	res."IdReserva" AS "IdReservaDetalle",
	CASE
		WHEN res."FolioCarteraOrigen" = 0 THEN 'S'
		ELSE 'N'
	END AS "CarteraNominal",
	res."FolioCarteraOrigen" AS "FolioCarteraDetalle",
	res."FolioCarteraCustodia" AS "FolioCarteraCustodiaDetalle",
	res."IdEmisor" AS "IdEmisorDetalle",
	res."CorteMinimo" AS "CorteMinimoDetalle",
	res."IdTipoReajuste" AS "IdTipoReajusteDetalle",
	res."IdMoneda" AS "IdMonedaDetalle",
	res."IdMonedaEmisionInstrumento" AS "IdMonedaEmisionInstrumentoDetalle",
	res."IdMonedaLiquidacionInstrumento" AS "IdMonedaLiquidacionInstrumentoDetalle",
	res."IdMonedaLiquidacionOperacion" AS "IdMonedaLiquidacionOperacionDetalle",
	res."CantidadInicial" AS "CantidadDetalle",
	res."IdInstrumento" AS "IdInstrumentoDetalle",
    res."Nemotecnico" AS "NemotecnicoDetalle",
	ins."TipoValorizacion" AS "TipoValorizacionDetalle",
	cla."ValorClasificacionRiesgo" AS "ValorClasificacionRiesgoDetalle",
	res."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoDetalle",
	res."IdTipoInstrumento" AS "IdTipoInstrumentoDetalle",
	res."Precio" AS "PrecioCompraDetalle",
	res."PrecioMercado" AS "PrecioMercadoDetalle",
	res."Tasa" AS "TasaCompraDetalle",
	res."TasaMercado" AS "TasaMercadoDetalle",
	res."MontoValorizado" AS "MontoCompraDetalle",
	res."MontoLiquidacion" AS "MontoLiquidacionDetalle",
	res."MontoLiquidacionOperacion" AS "MontoLiquidacionOperacionDetalle",
	res."FechaVencimiento" AS "FechaVencimientoInstrumentoDetalle",
	res."EsFungible" AS "EsFungibleDetalle",
	res."IngCantidad" AS "CantidadReservadaDetalle",
	res."IngValorRescate" AS "ValorRescateDetalle",
	res."IdUsuarioCreacion" AS "IdUsrReservaDetalle",
	res."IdOperacion" AS "IdOperacionDetalle",
	res."IdBolsa" AS "IdBolsaDetalle",
	res."IdBoveda" AS "IdBovedaDetalle",
	res."INGMontoAPlazo" AS "INGMontoAPlazoDetalle",
	res."INGMontoAPlazoMonedaInstrumento" AS "INGMontoAPlazoMonedaInstrumentoDetalle",
	COALESCE(CatIFRS."CodigoCarteraIFRS",'') AS "CodigoIFRSDetalle",
	COALESCE(CatIFRS."CarteraIFRS",	'') AS "CodigoDescIFRSDetalle",
	Boveda."Nombre" AS "NombreBovedaDetalle",
	ClasInst."Nombre" AS "ClasificacionTipoInstrumentoDetalle",
	res."MontoValorizadoReserva",
	res."IdCarteraOrigen" AS "IdCarteraDetalle",
	car."TipoDocumento" AS "TipoDocumento",
	car."IdContacto" AS "IdContacto",
	cuentaPropia."NombreCompleto" as "NombreCuentaPropia",
	cuentaPropiaPortafolio."CodigoPortafolio" AS "portafolioCodigo",
	cuentaPropiaPortafolio."Proposito" AS "portafolioProposito",
	por."CodigoPortafolio"
FROM
	"BSCL.Mesa.BD::MES.Operacion" car
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" est 
	ON
(car."IdTipoProducto" = est."IdTipoProducto"
	AND est."NombreEntidad" = 'INGRESO'
	AND car."IdEstado" = est."IdEstado")
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" cond
	ON
(car."IdTipoOperacion" = cond."IdTipoOperacion"
	AND car."IdCondicionLiquidacion" = cond."IdCondicionDeLiquidacion")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" condAnt
	ON
(car."IdTipoOperacion" = condAnt."IdTipoOperacion"
	AND car."IdCondicionLiquidacionAnticipo" = condAnt."IdCondicionDeLiquidacion")
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" mesOrigen
	ON
(car."IdMesaOrigen" = mesOrigen."IdMesa")
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" mesDestino
	ON
(car."IdMesaDestino" = mesDestino."IdMesa")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cuentaPropia
		ON
	(mesOrigen."IdCliente" = cuentaPropia."IdCliente")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" cuentaPropiaPortafolio
		ON
	(mesOrigen."IdPortafolio" = cuentaPropiaPortafolio."IdPortafolio")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" res
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" carr ON
(res."IdCarteraOrigen" = carr."IdCartera")
		ON
	(car."IdOperacion" = res."IdOperacion")
--falta unirlo con la cartera para cuando la oepracion se encuentre cerrada
	/*left join "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" cPag 
		on (car."IdEmpresa" = cPag."IdEmpresa" and car."IdOperacion" = cPag."IdOperacion" and car."IdTipoProducto" = cPag."IdTipoProducto")*/
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins
--"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins
		ON
	(res."IdInstrumento" = ins."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" cla ON
	(cla."IdInstrumento" = res."IdInstrumento")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc
		ON
	(car."IdCliente" = soc."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
		ON
	(car."IdCliente" = cli."IdCliente")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por
		ON
	(car."IdPortafolio" = por."IdPortafolio")
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tOper
		ON
	(car."IdTipoOperacion" = tOper."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tOpe
		ON
	(car."IdTipoOperacion" = tOpe."IdTipoOperacion"
	AND res."IdTipoInstrumento" = tOpe."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usr
--"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" usr
		ON
	(car."IdUsuarioCreacion" = usr."IdUsuario")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per
		ON
	(car."IdAgente" = per."IdPersona")
--* MASH
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS Boveda 
			  ON
	(Res."IdBoveda" = Boveda."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS ClasInst 
			  ON
	(ClasInst."IdClasificacionTipoInstrumento" = res."IdClasificacionTipoInstrumento")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" AS CatIFRS
			  ON
	(Res."CodigoIFRS" = CatIFRS."CodigoCarteraIFRS");

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" TO BCS_DESARROLLADOR WITH GRANT OPTION;