WITH MOVIMIENTOS AS (
SELECT
 	TipoMovimientoFondoMutuo."IndicadorAporteRescate" ,
	SolicitudAporteRescate."IdSolicitudAporteRescate",
	Cliente."DocumentoIdentidad",
	Portafolio."CodigoPortafolio",
	SolicitudAporteRescate."FechaIngresoRegistro",
	SolicitudAporteRescate."CalculoPorMontoCuota",
	SolicitudAporteRescate."NumeroCuotas" ,
	SolicitudAporteRescate."Monto" ,
	INDICADOR."ValorIndicador",
	SolicitudAporteRescate."FechaLiquidacion" ,
	Fondo."Nemotecnico",
	SolicitudAporteRescate."Estado",
	SolicitudAporteRescate."IdMoneda",
	Fondo."IdMoneda" AS "IdMonedaFondo",
	SolicitudAporteRescate."TipoSolicitud",
	SolicitudAporteRescate."IdTipoMovimientoFondoMutuo",
	SolicitudAporteRescate."IdSerieFondo",
	TO_NVARCHAR(SolicitudAporteRescate."FechaValorCuota",'YYYY-MM-DD') AS "FechaValorCuota",
	'' AS "ValorDestino",
	CASE 
		WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate"='APO' THEN SerieDeFondo."DiasRetencionesRescate"
		WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate"='RES' THEN SerieDeFondo."DiasRetencionesAporte"
	END AS "DiasRetenciones",
	SolicitudAporteRescate."IdMedioDePago",
	Empleado."CodigoEmpleado" AS "CodigoEjecutivo"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SolicitudAporteRescate
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo ON (TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescate."IdTipoMovimientoFondoMutuo")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoSolicitud ON (EstadoSolicitud."IdEstado" = SolicitudAporteRescate."Estado")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON (SerieDeFondo."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondo" )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo" )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RegimenFondoMutuo ON (RegimenFondoMutuo."IdRegimenFondoMutuo" = SolicitudAporteRescate."IdRegimenTributario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario ON (Usuario."IdUsuario" = SolicitudAporteRescate."IdUsuario")
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON  (Portafolio."IdPortafolio" = SolicitudAporteRescate."IdPortafolio" )
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT OUTER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON MatrizAgrupador."IdProductoAgrupador" = ProductoAgrupador."IdProductoAgrupador"
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente ON  (Cliente."IdCliente" = Portafolio."IdCliente" )
LEFT OUTER JOIN"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS INDICADOR ON(Cliente."IdCliente" = INDICADOR."IdCliente" AND INDICADOR."CodigoTipoIndicador"='INVCALIF') 
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle" AS OmnibusDetalle ON (SolicitudAporteRescate."FolioOmnibus" = OmnibusDetalle."IdOmnibusDetalle")
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PortafolioDestino ON  (PortafolioDestino."IdPortafolio" = SolicitudAporteRescate."IdPortafolioDestino")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ClienteDestino ON  (ClienteDestino."IdCliente" = PortafolioDestino."IdCliente" )
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS Empleado ON (Empleado."IdEmpleado" = SolicitudAporteRescate."IdEmpleadoEjecutivo")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON (MedioDePago."IdMedioDePago" = SolicitudAporteRescate."IdMedioDePago")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescateRelacionado ON (SolicitudAporteRescateRelacionado."IdSolicitudAporteRescate" = SolicitudAporteRescate."IdSolicitudAporteRescateRelacionado")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuoRelacionado ON (TipoMovimientoFondoMutuoRelacionado."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescateRelacionado."IdTipoMovimientoFondoMutuo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondoRelacionado ON (SerieDeFondoRelacionado."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondoDestino" )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FondoRelacionado ON (FondoRelacionado."IdFondo" = SerieDeFondoRelacionado."IdFondo" )
LEFT OUTER JOIN (SELECT top 1 "IdTipoComprobanteOrigen", "IdComprobanteOrigen", ComprobanteDetalle."IdEstado",Comprobante."IdComprobante"  FROM
					"BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle
					INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = ComprobanteDetalle."IdComprobante"
				) AS ComprobanteDetalle ON (ComprobanteDetalle."IdTipoComprobanteOrigen" = 'FMM' AND ComprobanteDetalle."IdComprobanteOrigen" = SolicitudAporteRescate."IdSolicitudAporteRescate" AND ComprobanteDetalle."IdEstado" IN ('ING','LIQ','CRT'))
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago ON (ComprobanteFormaDePago."IdComprobante" = ComprobanteDetalle."IdComprobante" AND ComprobanteFormaDePago."IdEstado" IN ('CRT','EBV','EMI','GIR','ING','LIQ'))
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TesoreriaSpot ON (SolicitudAporteRescate."IdIngresoEgreso" = TesoreriaSpot."IdIngresoEgreso")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoTesoreriaSpot ON (EstadoTesoreriaSpot."IdEstado" =  TesoreriaSpot."IdEstado")
LEFT OUTER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS Spot ON (Spot."IdOperacionSpotOrigen" = SolicitudAporteRescate."IdOperacionSpot" AND Spot."Estado" != 'MOD' AND Spot."IdOperacionSpotOrigen" != 0)
LEFT OUTER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OperSpotPrin ON (Spot."IdOperacionSpot"=OperSpotPrin."IdOperacionSpot")				  
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoSpot ON (EstadoSpot."IdEstado" = Spot."Estado")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoConfirmacionSpot ON (EstadoConfirmacionSpot."IdEstado" = OperSpotPrin."EstadoConfirmacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipoOperacionSpot ON (TipoOperacionSpot."IdTipoOperacion" = Spot."IdTipoOperacion")
LEFT OUTER JOIN (SELECT MovComprobante."IdTransaccion", MovComprobante."FolioTransaccion", MovComprobante."IdTipoCanalIngreso", 
					MovComprobante."IdComprobante",TesoreriaSpot1."IdIngresoEgreso",MovComprobante."IdTipoProducto", 
					TesoreriaSpot1."IdEstado",TesoreriaSpot1."FechaLiquidacion", ROW_NUMBER() OVER(PARTITION BY TesoreriaSpot1."IdOperacion") AS rownumber
					FROM  "BSCL.Facturacion.BD::FAC.MovimientoComprobante"   AS MovComprobante 
					INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TesoreriaSpot1 ON (TesoreriaSpot1."IdComprobanteOrigen"= MovComprobante."IdComprobante" AND TesoreriaSpot1."IdModulo"='FAC' AND TesoreriaSpot1."IdTipoProducto"= 'MEME')
				)AS MovComprobante
ON (MovComprobante."IdTipoCanalIngreso"='FMM'  AND  MovComprobante."IdTransaccion" = Spot."IdOperacionSpot" AND MovComprobante.rownumber=1 )
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoTesoreriaSpot1 ON (EstadoTesoreriaSpot1."IdEstado" =  MovComprobante."IdEstado")
LEFT OUTER JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso ON (OrdenTraspaso."IdSolicitudAporteRescate" = SolicitudAporteRescate."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.APV.BD::TRA.Traspaso" AS TraspasoAPV ON (OrdenTraspaso."IdTraspaso" = TraspasoAPV."IdTraspaso")
LEFT OUTER JOIN "BSCL.APV.BD::REC.Cliente" AS RecCliente ON (RecCliente."IdRecaudacionCliente" = SolicitudAporteRescate."IdOrigenRecaCliente")
LEFT OUTER JOIN "BSCL.APV.BD::REC.Empleador" AS RecEmpleador ON (RecEmpleador."IdRecaudacionEmpleador"  = RecCliente."IdRecaudacionEmpleador")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" AS TraspasoDestino ON (TraspasoDestino."IdSolicitudAporteRescate" <> 0 AND SolicitudAporteRescate."IdSolicitudAporteRescate" = TraspasoDestino."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" AS TraspasoOrigen ON (TraspasoOrigen."IdSolicitudAporteRescate" <> 0 AND SolicitudAporteRescate."IdSolicitudAporteRescate" = TraspasoOrigen."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::TRA.Traspaso" AS Traspaso ON (TraspasoOrigen."IdTraspaso" = Traspaso."IdTraspaso" OR TraspasoDestino."IdTraspaso" = Traspaso."IdTraspaso")
LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora ON (Traspaso."IdEntidadRecaudadora" = EntidadRecaudadora."IdEntidadRecaudadora")
WHERE
	SolicitudAporteRescate."IdEmpresa" = :IdEmpresa
	AND (SolicitudAporteRescate."FechaAceptacion" = :FechaCurse)
	AND (TipoMovimientoFondoMutuo."IndicadorAporteRescate"= :TipoSolicitud)
	AND (:Moneda IS NULL OR :Moneda = ''  OR SolicitudAporteRescate."IdMoneda" = :Moneda)
	AND (SolicitudAporteRescate."Estado" = :EstadoMovimiento OR ((:EstadoMovimiento = 'TODOS' OR :EstadoMovimiento IS NULL OR :EstadoMovimiento = '') AND SolicitudAporteRescate."Estado" != 'ANU'))
	AND (:Emisor IS NULL OR :Emisor= ''OR Fondo."IdEmisor" = :Emisor)
	AND (:FondoMutuo IS NULL OR :FondoMutuo= '' OR SolicitudAporteRescate."IdSerieFondo" = :FondoMutuo)
)
SELECT	"IndicadorAporteRescate" ,
		"IdSolicitudAporteRescate",
		"DocumentoIdentidad",
		"CodigoPortafolio",
		"FechaIngresoRegistro",
		"CalculoPorMontoCuota",
		"NumeroCuotas" ,
		"Monto" ,
		"ValorIndicador",
		"FechaLiquidacion" ,
		"Nemotecnico",
		"Estado",
		"IdMoneda",
		"ValorDestino",
		"DiasRetenciones",
		"IdMonedaFondo",
		"TipoSolicitud",
		"IdTipoMovimientoFondoMutuo",
		"IdSerieFondo",
		"FechaValorCuota",
		"IdMedioDePago",
		"CodigoEjecutivo"
FROM MOVIMIENTOS 
ORDER BY "FechaIngresoRegistro";