SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotVistaAnalitica";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotVistaAnalitica" AS
SELECT
	  OS."IdOperacionSpot"									AS "IdOperacion"
	, 0														AS "FolioOperacion"
	, OS."FechaIngresoRegistro"
	, OS."FechaOperacion"									AS "FechaOperacion"
	, OS."FechaLiquidacion"									AS "FechaLiquidacion"
	, COALESCE(FPE."IdMedioDePago",'')						AS "IdMedioPagoEntregamos"
	, FPE."NombreMedioDePago"								AS "NombreMedioPagoEntregamos"
	, FPE."Monto"											AS "MontoPagoEntregamos"
	, FPE."FechaCondicionLiquidacionGrilla"					AS "FechaLiquidacionEntregamos"
	, FPE."FechaCondicionLiquidacionGrilla"					AS "FechaEntregamos"
	, COALESCE(FPR."IdMedioDePago",'')  					AS "IdMedioPagoRecibimos"
	, FPR."NombreMedioDePago"								AS "NombreMedioPagoRecibimos"
	, FPR."Monto"											AS "MontoPagoRecibimos"
	, FPR."FechaCondicionLiquidacionGrilla"					AS "FechaLiquidacionRecibimos"
	, FPR."FechaCondicionLiquidacionGrilla"					AS "FechaRecibimos"
	, TO_CHAR("FechaOperacion",'HH24:MI')					AS "HoraOperacion"
	, CLI."IdCliente"
	, TI."IdInstrumento"
	, TI."RazonSocial"										AS "NombreInstrumento"
	, TI."Nemotecnico"
	, CO."IdTipoOperacion"
	, CO."Nombre"											AS "NombreTipoOperacion"	--"TipoOperacion"
	, CLI."DocumentoIdentidad"								AS "DocumentoIdentidadCliente"	--"RutCliente"
	, CLI."NombreCompleto"									AS "NombreCliente"
	, CP."CodigoPortafolio"									AS "NombrePortafolio"
	, CP."CodigoPortafolio"
	, CP."CodigoPropositoPortafolio"
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
	, COALESCE(OS."MontoEquivalente",0)  					AS "MontoEquivalente"
	, COALESCE(OS."Margen",0)								AS "Margen"
	, COALESCE(OS."Utilidad",0)								AS "Utilidad"
	, 0														AS "MontoDiferido"
	, 0														AS "Modalidad"
	, ''													AS "ModalidadLiquidacion"
	, ''													AS "ModalidadLiquidacionValor"
	, null													AS "Plazo"
	, null													AS "PlazoResidual"
	, ''													AS "FechaVencimiento"
	, ''													AS "FechaEfectiva"
	, OS."IdAgente"											AS "IdAgente"	--"Agente"
	, (AG."Nombres" ||' '||AG."ApellidoPaterno")			AS "NombreAgente"
	, OS."OperacionFicticia"
	, US."CodigoUsuario"
	, OS."Estado"											AS "IdEstado"
	, EST."Nombre"											AS "NombreEstado"
	, OS."IdMesa"											AS "IdMesa"
	, MES."Nombre"											AS "NombreMesa"
	, CO."IdTipoProducto"									AS "Mesa"
	, OS."IdTipoDolar"
	, OS."TipoComprobanteFacturacion"
	, OS."Observacion"
	, SOC."IdSocioDeNegocio"								AS "IdSocioDeNegocio"
	, OS."IdTipoCanalIngreso"								AS "IdCanalIngreso"	--"IdTipoCanalIngreso"
	, TIPCAN."Nombre"										AS "NombreCanalIngreso"
	, CASE WHEN OS."IdTipoCanalIngreso" = 'FWD' THEN 'S' ELSE 'N' END AS "OrigenForward"
	, 'SPOT'												AS "Producto"
	,(SELECT "Nombre"  FROM "BSCL.Plataforma.BD::INS.TipoProducto" WHERE "IdTipoProducto" = 'MEME') AS "NombreProducto"
	, "CodigoComercio"
	, PlanillaBancoCentral."Descripcion"							AS "NombreCodigoComercio"	--"NombrePlanillaBancoCentral"
	, "CodigoComercio"||' - '||PlanillaBancoCentral."Descripcion"	AS "CodigoYNombrePlanillaBancoCentral"
	, OS."IdUsuario"
	, OS."IdEmpresa"
	, EMP."Nombre"													AS "NombreEmpresa"
	, OS."IdPortafolio"
	, CLI."IdPersona"
	, TI."IdTipoInstrumento"
	, OS."IdComprobanteFacturacion"
	, OS."IdComprobanteTesoreriaIngreso"
	, OS."IdComprobanteTesoreriaEgreso"
	, OS."IdOperacionSpotReferencia"
	, OS."IdSolicitudAporteRescate"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OS
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US
	ON OS."IdUsuario" = US."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" TI
	ON OS."IdInstrumento" = TI."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AG
	ON OS."IdAgente" = AG."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	ON OS."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" SOC
	ON CLI."IdPersona" = SOC."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion" CO
	ON OS."IdTipoOperacion" = CO."IdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
	ON CP."IdPortafolio" = OS."IdPortafolio"
----- MSO
LEFT JOIN (
		SELECT
			"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
			"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
			MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "RecibePaga" = 'P' AND "IdTipoProducto" = 'MEME'
		GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPE
	ON (FPE."IdOperacion" = OS."IdOperacionSpot")	

LEFT JOIN (
		SELECT
			"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
			"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
			MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "RecibePaga" = 'R' AND "IdTipoProducto" = 'MEME'
		GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPR
	ON (FPR."IdOperacion" = OS."IdOperacionSpot")	

LEFT JOIN "BSCL.Mesa.BD::MES.PlanillaBancoCentral" PlanillaBancoCentral
	ON OS."CodigoComercio" = PlanillaBancoCentral."IdPlanillaBancoCentral"
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
	ON (OS."IdMesa" = MES."IdMesa" AND OS."IdEmpresa" = MES."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TIPCAN
	ON (OS."IdTipoCanalIngreso" = TIPCAN."IdTipoCanalIngreso")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON (OS."IdEmpresa" = EMP."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST
	ON (OS."Estado" = EST."IdEstado");
	
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpotVistaAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;