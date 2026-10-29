SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" AS 
SELECT
	  OS."IdOperacionSpot" 	AS "IdOperacion"
	, OS."IdOperacionSpotOrigen" 	AS "IdOperacionUI"
	, 0						AS "FolioOperacion"
	, OS."FechaOperacion" 	AS "FechaOperacion"
 	, OS."FechaLiquidacion" AS "FechaLiquidacion"
	, FormaPagoEnt."FechaLiquidacionComprobantePagoOperacion" AS "FechaEntregamos"
	, FormaPagoRec."FechaLiquidacionComprobantePagoOperacion" AS "FechaRecibimos"
	, TO_NVARCHAR(ADD_SECONDS("FechaOperacion",Conf."Valor"),'HH24:MI:SS') AS "HoraOperacion"
	, CLI."IdCliente"
	, TI."IdInstrumento"
	, TI."RazonSocial"  AS "NombreInstrumento"
	, TI."Nemotecnico"
	, CO."IdTipoOperacion"
	, CO."Nombre" AS "TipoOperacion"
	, PER."DocumentoIdentidad"  AS "RutCliente"
	, PER."NombreCompleto" AS "NombreCliente"
	, CP."CodigoPortafolio"||' '||CP."Proposito" AS "CodigoPropositoPortafolio"
	, OS."IdMonedaLocal"
	, OS."IdMonedaPrincipal"
	, OS."IdMonedaSecundaria" 
	, COALESCE(OS."MontoMonedaPrincipal",0)					AS "MontoMonedaPrincipal"
	, COALESCE(OS."MontoMonedaSecundaria",0)				AS "MontoMonedaSecundaria"
	, COALESCE(OS."ResultadoMonedaLocalPuntas",0)			AS "ResultadoMonedaLocalPuntas"
	, COALESCE(OS."ResultadoMonedaLocalBidAsk",0)			AS "ResultadoMonedaLocalBidAsk"
	, COALESCE(OS."ResultadoMonedaLocalDO",0)				AS "ResultadoMonedaLocalDO"
	, COALESCE(OS."ResultadoMonedaLocalTCSpot",0)			AS "ResultadoMonedaLocalTCSpot"
	, COALESCE(OS."ResultadoMonedaLocalTCCierreMesa",0)		AS "ResultadoMonedaLocalTCCierreMesa"
	, COALESCE(OS."ResultadoMonedaLocalConsolidado",0)		AS "ResultadoMonedaLocalConsolidado"
	, COALESCE(OS."MontoMonedaPrincipalEquivalente",0)      AS "MontoMonedaPrincipalEquivalente"
	, OS."IdMonedaEquivalente"
	, COALESCE(OS."PuntaCompra",0)						    AS "PuntaCompra"
	, COALESCE(OS."PuntaVenta",0)						    AS "PuntaVenta"
	, COALESCE(OS."TipoCambioCierre",0)						AS "TipoCambioCierre"
	, COALESCE(OS."TipoCambioCosto",0)						AS "TipoCambioCosto"
	, COALESCE(OS."ParidadCierre",0)						AS "ParidadCierre"
	, COALESCE(OS."ParidadCosto",0)							AS "ParidadCosto"
	, CASE WHEN OS."MontoMonedaPrincipal" > 0 THEN COALESCE(OS."MontoMonedaPrincipalEquivalente"/OS."MontoMonedaPrincipal",0) ELSE 0 END AS "ParidadMercado"
	, COALESCE(OS."ParidadBid",0)							AS "ParidadBid"
	, COALESCE(OS."ParidadAsk",0)							AS "ParidadAsk"
	, COALESCE(OS."MontoEquivalente",0)  					AS "MontoEquivalente"
	,COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"( COALESCE(OS."Margen",0), FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), COALESCE(OS."Margen",0)) As "Margen" 
	,COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"( COALESCE(OS."Utilidad",0), FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), COALESCE(OS."Utilidad",0)) As "Utilidad" 
	, 0 AS "MontoDiferido"
	, 0 AS "Modalidad"
	, '' AS "ModalidadLiquidacion"
	, '' AS "ModalidadLiquidacionValor"
	, null AS "Plazo"
	, null AS "PlazoResidual" 
	, '' AS "FechaVencimiento" 
	, '' AS "FechaEfectiva"
	, OS."IdAgente" as "Agente"  
	, CASE WHEN AG."Nombres" !='' AND AG."ApellidoPaterno" != '' THEN   (AG."Nombres" ||' '||AG."ApellidoPaterno") 
		ELSE AG."RazonSocial" END AS "NombreAgente"
	, OS."OperacionFicticia"
	, US."CodigoUsuario"
	, CASE WHEN OS."Estado" = 'PEN' AND COALESCE(AUT."IdEstado",'') = 'VIG' THEN 'PAU' ELSE OS."Estado" END AS "Estado"
	, OS."IdMesa" AS "IdMesa"
	, CO."IdTipoProducto" AS "Mesa"
	, OS."IdTipoDolar"
	, OS."TipoComprobanteFacturacion"
	, OS."Observacion"
	, SOC."IdSocioDeNegocio" 	AS "IdSocioDeNegocio"
	, OS."IdTipoCanalIngreso"	AS "IdTipoCanalIngreso"
	, CASE WHEN OS."IdTipoCanalIngreso" = 'FWD' THEN 'S' ELSE 'N' END AS "OrigenForward"
	, 'SPOT' AS "Producto"
	, "CodigoComercio"
	, PlanillaBancoCentral."Descripcion" as "NombrePlanillaBancoCentral"
	, "CodigoComercio"||' - '||PlanillaBancoCentral."Descripcion" as "CodigoYNombrePlanillaBancoCentral"
	, OS."IdUsuario"
	, OS."IdEmpresa"
	, OS."IdPortafolio"
	, CLI."IdPersona"
	, TI."IdTipoInstrumento"
	, OS."IdComprobanteFacturacion"
	, OS."IdComprobanteTesoreriaIngreso"
	, OS."IdComprobanteTesoreriaEgreso"
	, CO."Accion"
	, OS."IdOperacionSpotReferencia"
	, OS."EstadoConfirmacion"
	, OS."FolioOrigenFW"
	, OS."IdOperacionSpotOrigen"
	, OS."IdOperacionSpotVigente"
	, OS."TipoDocumento"
	, OS."IdContacto"
	,'SP' AS "IdClasificacionTipoInstrumento"
	--Fondos mutuos
	, OS."IdSolicitudAporteRescate"
	, COALESCE(ESTADOFM."Nombre",'') AS "EstadoAporteRescate" 
	, COALESCE(TIPOFM."Descripcion",'') AS "TipoAporteRescate"
	, COALESCE(SOL."FechaLiquidacion", '') AS "FechaLiquidacionAporteRescate"
	, COALESCE(SOL."IdSerieFondo", '') AS "IdSerieFondoAporteRescate"
	,CASE WHEN (
		FONDOORIGEN."IdPaisOrigen" = ConfPaisSistema."Valor"
		AND FONDOORIGEN."IdMoneda" = ConfMoneda."Valor"
		AND FONDOORIGEN."DiasRetencionesRescate" = 1
		AND FONDOORIGEN."DiasRetencionesAporte" = 0
		AND SOL."IdTipoMovimientoFondoMutuo" = 'APOREINV'
	) OR (
		FONDO."IdPaisOrigen" = ConfPaisSistema."Valor"
		AND FONDO."IdMoneda" = ConfMoneda."Valor"
		AND FONDO."DiasRetencionesRescate" = 1
		AND FONDO."DiasRetencionesAporte" = 0
	) THEN 'S'
	ELSE 'N'
	END AS "FondosNacionalesValutaCero"
	, COALESCE(OS."AfectoADerivado", 'N') AS "AfectoADerivado"
	, COALESCE(OS."DolarObservado", 'N') AS "DolarObservado"
	, AN."IdAsignacion"
	--Plataforma Negociacion
	, COALESCE(OS."IdPlataformaNegociacion", '') AS "IdPlataformaNegociacion"
	, "OperacionFicticia" AS "OperacionNoFacturable"
	, OS."IdEmpleadoDigitador" AS "IdEmpleadoDigitador"
	, CE."NombreCompleto" AS "NombreEmpleadoDigitador"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" AS OS
LEFT OUTER JOIN  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SOL ON (OS."IdSolicitudAporteRescate"=SOL."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TIPOFM ON (TIPOFM."IdTipoMovimientoFondoMutuo" = SOL."IdTipoMovimientoFondoMutuo")
LEFT OUTER JOIN  "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOFM
    ON (ESTADOFM."IdEstado" =  CASE WHEN OS."IdSolicitudAporteRescate" > 0 THEN  (CASE COALESCE(OS."Estado",'') WHEN 'VIG' THEN SOL."Estado" ELSE 'PEN' END) ELSE '' END )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS FONDO ON (FONDO."IdSerieFondoNombre" = SOL."IdSerieFondo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS FONDOORIGEN ON (FONDOORIGEN."IdSerieFondoNombre" = SOL."IdSerieFondoOrigen")
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AN 
ON (AN."IdOperacionSpot" = OS."IdOperacionSpot")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US
	ON OS."IdUsuario" = US."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" TI
	ON OS."IdInstrumento" = TI."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AG
	ON OS."IdAgente" = AG."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON OS."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
	ON CLI."IdPersona" = SOC."IdPersona"  
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" CO
	ON OS."IdTipoOperacion" = CO."IdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP
	ON CP."IdPortafolio" = OS."IdPortafolio"
LEFT JOIN (
        SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
        --FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
        WHERE "IdTipoProducto" = 'MEME' AND "RecibePaga" = 'P'
    	) AS FormaPagoEnt
    ON (FormaPagoEnt."IdOperacion" =  OS."IdOperacionSpot" AND FormaPagoEnt."IdEmpresa"= OS."IdEmpresa")
LEFT JOIN (
		SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
        --FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
        WHERE "IdTipoProducto" = 'MEME' AND "RecibePaga" = 'R'
		) AS FormaPagoRec
	ON (FormaPagoRec."IdOperacion" =  OS."IdOperacionSpot" AND FormaPagoRec."IdEmpresa"= OS."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD::MES.PlanillaBancoCentral" PlanillaBancoCentral
	ON OS."CodigoComercio" = PlanillaBancoCentral."IdPlanillaBancoCentral"
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AUT
	ON (TO_NVARCHAR(OS."IdOperacionSpot") = COALESCE(AUT."NumeroOperacionConcepto",'0') AND
		OS."IdEmpresa" = AUT."IdEmpresa" AND
		OS."IdTipoOperacion" = AUT."IdTipoOperacion" AND
		TO_NVARCHAR(OS."FechaOperacion",'YYYY-MM-DD') = TO_NVARCHAR(AUT."Fecha",'YYYY-MM-DD') AND
		AUT."IdModulo" = 'MSP')
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS FormMvto  
ON FormMvto."IdMoneda"= OS."IdMonedaSecundaria" AND FormMvto."IdUsoMonedaFormato"='SPT'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS ConfMoneda ON (ConfMoneda."IdEmpresa" = OS."IdEmpresa" AND ConfMoneda."CodigoParametro" = 'PLA.MonedaAdicional')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS ConfPaisSistema ON (ConfPaisSistema."IdEmpresa" = OS."IdEmpresa" AND ConfPaisSistema."CodigoParametro" = 'PLA.PaisSistema')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS Conf ON OS."IdEmpresa" = Conf."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS CE ON "CE"."IdEmpleado" = OS."IdEmpleadoDigitador"
WHERE Conf."CodigoParametro" = ('PLA.SegundosZonaHoraria');

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" TO BCS_DESARROLLADOR WITH GRANT OPTION;