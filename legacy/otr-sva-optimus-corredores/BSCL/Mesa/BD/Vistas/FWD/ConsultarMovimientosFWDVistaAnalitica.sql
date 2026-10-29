SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVistaAnalitica";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVistaAnalitica" AS

SELECT
	  FWD."IdEmpresa"
	, FWD."NombreEmpresa"
	, FWD."FechaIngresoRegistro"
	, FWD."IdCartera"
	, FWD."FolioCartera"
	, FWD."IdOperacion"
	, FWD."FechaOperacion" 												AS "FechaOperacion"
	, FWD."FechaLiquidacion"											AS "FechaLiquidacion"
	
	,CASE
		WHEN FWD."IdEstado"='ANT' OR (FWD."IdEstado" IN ('VCD','LIQ') AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRFechaOperacion"
		ELSE '' END	AS "FechaAnticipo"
	
	, coalesce(FPE."IdMedioDePago",'')									AS "IdMedioPagoEntregamos"
	, FPE."NombreMedioDePago"											AS "NombreMedioPagoEntregamos"
	, FPE."Monto"														AS "MontoPagoEntregamos"
	, FPE."FechaCondicionLiquidacionGrilla"								AS "FechaLiquidacionEntregamos"
	, FPE."FechaCondicionLiquidacionGrilla"								AS "FechaEntregamos"
	, coalesce(FPR."IdMedioDePago",'')									AS "IdMedioPagoRecibimos"
	, FPR."NombreMedioDePago"											AS "NombreMedioPagoRecibimos"
	, FPR."Monto"														AS "MontoPagoRecibimos"
	, FPR."FechaCondicionLiquidacionGrilla"								AS "FechaLiquidacionRecibimos"
	, FPR."FechaCondicionLiquidacionGrilla"								AS "FechaRecibimos"
	, TO_CHAR(OPE."FechaOperacion",'HH24:MI')							AS "HoraOperacion"
	, OPE."FechaOperacion"												AS "FechaOperacionOPE"
	, FWD."IdTipoInstrumento"											AS "IdTipoInstrumento"
	, FWD."IdInstrumento"												AS "IdInstrumento"
	, FWD."RazonSocialInstrumento"										AS "NombreInstrumento"
	, FWD."Nemotecnico"													AS "Nemotecnico"
	, FWD."IdTipoOperacion"												AS "IdTipoOperacion"
	, FWD."NombreTipoOperacion"											--AS "TipoOperacion"
	, FWD."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"
	, FWD."IdTipoProductoTIPOPE"										AS "IdTipoProductoTIPOPE"
	, CLI."IdCliente"													AS "IdCliente"
	, FWD."DocumentoIdentidad"  										AS "DocumentoIdentidadCliente"	--"RutCliente"
	, NOM."NombreCompleto"						 						AS "NombreCliente"
	, 0 																AS "IdMonedaLocal"
	, FWD."IdMonedaPrincipal"											AS "IdMonedaPrincipal"
	, MONPRI."Nombre"													AS "NombreMonedaPrincipal"
	, FWD."IdMonedaSecundaria" 											AS "IdMonedaSecundaria"
	, MONSEC."Nombre"													AS "NombreMonedaSecundaria"
	, FWD."MontoMonedaPrincipal" 										AS "MontoMonedaPrincipal"
	, FWD."MontoMonedaSecundaria" 										AS "MontoMonedaSecundaria"
	, 0 																AS "ResultadoMonedaLocalPuntas"
	, 0 																AS "ResultadoMonedaLocalBidAsk"
	, 0 																AS "ResultadoMonedaLocalDO"
	, 0 																AS "ResultadoMonedaLocalTCSpot"
	, 0 																AS "ResultadoMonedaLocalTCCierreMesa"
	, 0 																AS "ResultadoMonedaLocalConsolidado"
	, 0 																AS "MontoMonedaPrincipalEquivalente"
	, 0 																AS "IdMonedaEquivalente"
	, FWD."ValorTipoCambioCierre" 										AS "TipoCambioCierre"
	, FWD."ValorTipoCambioCosto" 										AS "TipoCambioCosto"
	, FWD."ValorParidadCierre" 											AS "ParidadCierre"
	, FWD."ValorParidadCosto" 											AS "ParidadCosto"
	, FWD."INGMontoDiferido" 											AS "MontoEquivalente"
	, FWD."INGMontoMargen" 												AS "Margen"
	, 0 																AS "Utilidad"
	, FWD."INGMontoDiferido" 											AS "MontoDiferido"
	, 0																	AS "MontoCalce"
	, CASE
		WHEN COALESCE(TABLAFWDDISPONIBLE."FolioCartera",0) = 0 THEN FWD."MontoMonedaPrincipal"
		ELSE TABLAFWDDISPONIBLE."MontoDisponible" END					AS "MontoDisponible"
	, FWD."EGRMontoLiquidacion"
	, FWD."EGRMontoARecibirAnticipo"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoMonedaSecundaria"
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoMonedaPrincipal"
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'COMFWD' AND FWD."EGRMontoLiquidacion" > 0 
			THEN FWD."EGRMontoLiquidacion"
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'VENFWD' AND FWD."EGRMontoLiquidacion" > 0 --
			THEN FWD."EGRMontoLiquidacion"
		ELSE 0 END 														AS "MontoRecibimos"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoMonedaPrincipal"
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoMonedaSecundaria"
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'COMFWD' AND FWD."EGRMontoLiquidacion" < 0 
			THEN ABS(FWD."EGRMontoLiquidacion")
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'VENFWD' AND FWD."EGRMontoLiquidacion" < 0 --
			THEN ABS(FWD."EGRMontoLiquidacion")
		ELSE 0 END														AS "MontoPagamos"
	, CASE	--campo para determinar si se hace Ingreso y/o Egreso en ENVIAR A TESORERIA de blotter Vencimiento Forward
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN 'INGEGR'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'COMFWD' AND FWD."EGRMontoLiquidacion" > 0 THEN 'ING'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'VENFWD' AND FWD."EGRMontoLiquidacion" > 0 THEN 'ING'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'COMFWD' AND FWD."EGRMontoLiquidacion" < 0 THEN 'EGR'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND FWD."IdTipoOperacion" = 'VENFWD' AND FWD."EGRMontoLiquidacion" < 0 THEN 'EGR'
		ELSE '' END														AS "IngresoEgreso"
	, FWD."INGEsPuntos"													AS "EsPuntos"
	, FWD."INGTasaPuntosMonedaPrincipal"								AS "PuntosForward"
	, FWD."INGTasaPuntosMonedaSecundaria"								AS "TasaForward"
	, FWD."IdModalidadCumplimiento" 									AS "Modalidad"
	, PARAM."NombreParametro" 											AS "NombreModalidad"	--"ModalidadLiquidacion"
	, PARAM."ValorParametro" 											AS "ModalidadLiquidacionValor"
	, FWD."IdFixing"													AS "IdFixing"
	, FWD."FechaFixing"													AS "FechaFixingCartera"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
		--WHEN PARAM."ValorParametro" = 'COMPENSADA' AND PARAMFIXING."ValorParametro" = 'BANCOCENTRAL' THEN ''
		WHEN TO_CHAR(FWD."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
		ELSE TO_CHAR(FWD."FechaFixing",'yyyy-MM-dd') END				AS "FechaFixing"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
		--WHEN PARAM."ValorParametro" = 'COMPENSADA' AND PARAMFIXING."ValorParametro" = 'BANCOCENTRAL' THEN ''
		WHEN TO_CHAR(FWD."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
		ELSE TO_CHAR(FWD."FechaFixing",'dd-MM-yyyy') END				AS "FechaFixingGrillaVcto"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
		--WHEN PARAM."ValorParametro" = 'COMPENSADA' AND PARAMFIXING."ValorParametro" = 'BANCOCENTRAL' THEN ''
		WHEN TO_CHAR(FWD."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
		ELSE TO_CHAR(FWD."HoraFixing",'HH24:MI') END					AS "HoraFixing"
	, PARAMFIXING."NombreParametro"										AS "NombreFixing"	--"FixingNombre"
	, PARAMFIXING."ValorParametro"										AS "Fixing"
	, FWD."PlazoEnDiasOperacion" 										AS "Plazo"
	, GREATEST(COALESCE(DAYS_BETWEEN (
		TO_SECONDDATE((SELECT CON."Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" CON 
					   WHERE CON."IdEmpresa" = FWD."IdEmpresa" AND CON."CodigoParametro" = 'PLA.FechaSistema'),'YYYY-MM-DD'),
		TO_SECONDDATE((CASE WHEN FWD."IdEstado" = 'ANT' OR 
								(FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRFechaOperacion"
							ELSE FWD."FechaVencimientoOperacion" END), 'YYYY-MM-DD')), 0), 0) AS "PlazoResidual" 
	, FWD."FechaVencimientoOperacion" 									AS "FechaVencimiento"
	, COALESCE(FWD."EGRValorParidad",0)									AS "Precio"
	, CASE
		WHEN FWD."Nemotecnico" = 'SECARE' THEN
			CASE WHEN FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'LIQ' AND FWD."EGRFechaOperacion" != '') OR 
					 (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRTipoCambioAnticipo"
				 WHEN FWD."IdEstado" = 'VCD' OR (FWD."IdEstado" = 'LIQ' AND FWD."EGRFechaOperacion" = '0001-01-01') THEN FWD."EGRPrecio"
				 WHEN FWD."IdEstado" = 'VIG' THEN 
				   CASE WHEN COALESCE(PARUF."TipoCambioMercado",0)=0 THEN NULL ELSE PARUSD."TipoCambioMercado"/PARUF."TipoCambioMercado" END
				 ELSE 0 END
		WHEN FWD."Nemotecnico" != 'ARBITR' THEN
			CASE WHEN FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'LIQ' AND FWD."EGRFechaOperacion" != '') OR 
					 (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRTipoCambioAnticipo"
				 WHEN FWD."IdEstado" = 'VCD' OR (FWD."IdEstado" = 'LIQ' AND FWD."EGRFechaOperacion" = '0001-01-01') THEN FWD."EGRPrecio"
				 WHEN FWD."IdEstado" = 'VIG' THEN PAR."TipoCambioMercado"
				 ELSE 0 END
		ELSE 0 END														AS "PrecioVencimiento"
	, CASE
		WHEN FWD."Nemotecnico" = 'ARBITR' THEN
			CASE WHEN FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'LIQ' AND FWD."EGRFechaOperacion" != '') OR 
					 (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRTipoCambioAnticipo"
				 WHEN FWD."IdEstado" = 'VCD' OR (FWD."IdEstado" = 'LIQ' AND FWD."EGRFechaOperacion" = '0001-01-01') THEN FWD."EGRPrecio"
				 WHEN FWD."IdEstado" = 'VIG' THEN PAR."ParidadMercado"
				 ELSE 0 END
		ELSE 0 END														AS "ParidadVencimiento"
	, CASE
		WHEN (FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD')) AND 
			  FWD."Nemotecnico" != 'ARBITR' THEN FWD."EGRTipoCambioAnticipo" 
		ELSE 0 END														AS "TipoCambioAnticipo"
	, CASE
		WHEN (FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD')) AND 
			  FWD."Nemotecnico" = 'ARBITR' THEN FWD."EGRTipoCambioAnticipo"
		ELSE 0 END														AS "ParidadAnticipo"
	, CASE WHEN FWD."Nemotecnico" != 'ARBITR' THEN 'CLP' ELSE 'USD' END	AS "IdMonedaLiquidacion" 
	, CASE
		WHEN PARAM."ValorParametro" = 'COMPENSADA' THEN FWD."FechaFixing"
		ELSE FWD."FechaVencimientoOperacion" END						AS "FechaEfectiva"
	, FWD."IdAgente" 													AS "IdAgente"
	, FWD."IdAgente" 													AS "Agente"  
	, (AG."Nombres" ||' '||AG."ApellidoPaterno")						AS "NombreAgente"
	, '' 																AS "OperacionFicticia"
	, FWD."NombreUsuario" 												AS "CodigoUsuario"
	, FWD."IdEstado"													AS "IdEstado"
	, FWD."IdEstado"													AS "Estado"
	, EST."Nombre"														AS "NombreEstado"
	, OPE."IdEstado"													AS "EstadoOperacion"
	, FWD."IdTipoProducto"												AS "IdTipoProducto"
	, FWD."IdTipoProducto"												AS "Mesa"
	, (SELECT "Nombre" FROM "BSCL.Plataforma.BD::INS.TipoProducto" WHERE "IdTipoProducto" = 'MEFW') AS "NombreProducto"
	, SOC."IdSocioDeNegocio"											AS "IdSocioDeNegocio"
	, FWD."IdPortafolio"												AS "IdPortafolio"
	, POR."CodigoPortafolio"
	, POR."CodigoPortafolio"											AS "NombrePortafolio"
	, POR."CodigoPortafolio" ||' '||POR."Proposito"						AS "CodigoPropositoPortafolio"
	, FWD."IdTipoCanalIngreso"											AS "IdTipoCanalIngreso"
	, TIPCAN."Nombre"													AS "NombreCanalIngreso"
	, FWD."INGMontoValorRazonableActivo"								AS "MontoValorRazonableActivo"	
	, FWD."INGMontoValorRazonablePasivo"								AS "MontoValorRazonablePasivo"
	, FWD."INGMontoValorRazonableNeto"									AS "MontoValorRazonableNeto"	
	, FWD."INGTasaValorRazonableActivo"									AS "TasaValorRazonableActivo"
	, FWD."INGTasaValorRazonablePasivo"									AS "TasaValorRazonablePasivo"
	, FWD."INGCantidad"													AS "INGCantidad"
	, FWD."INGMontoMonedaPrincipal"										AS "INGMontoMonedaPrincipal"
	, FWD."INGMontoMonedaSecundaria"									AS "INGMontoMonedaSecundaria"
	, CASE
		WHEN FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') THEN FWD."EGRFechaOperacion" 
		ELSE FWD."FechaVencimientoOperacion" END						AS "FechaFiltroGrillaVcto"
	, FWD."IdMesa"
	, MES."Nombre"														AS "NombreMesa"
	, OPE."IdSpotAsociado"
	, OPE."IdClasificacionCartera"
	, PCC."ValorParametro"												AS "ClasificacionCartera"
	, FWD."Glosa"
	, FWD."EGRTipoCambioObservado"										AS "TCObservadoTABANT"
	, FWD."EGRMontoMonedaPrincipal"										AS "MontoMonedaPricipalTABANT"
	, FWD."EGRMontoMonedaSecundaria"									AS "MontoMonedaSecundariaTABANT"
	, FWD."EGRTasaDescuentoAnticipo"									AS "TasaDescuentoTABANT"
	, FWD."EGRMontoARecibirAnticipo"									AS "MontoAnticipoTABANT"
	, FWD."EGRTipoCambioAnticipo"										AS "TCAnticipoTABANT"
	, FWD."IdPlaza"
	, PLAZA."NombreCiudad"												AS "NombrePlaza"
	, FWD."TasaCompra"													AS "TCForward"
	, FWD."EGRIdSpotAsociado"
	, OPE."IdUsuarioCreacion"
	, USU."CodigoUsuario"												AS "CodigoUsuarioCreacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS FWD
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
	ON (OPE."IdOperacion" = FWD."IdOperacion" AND OPE."IdTipoProducto" = FWD."IdTipoProducto")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS AG
	ON (FWD."IdAgente" = AG."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON (FWD."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS NOM
	ON (NOM."IdPersona" = CLI."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC
	ON (CLI."IdPersona" = SOC."IdPersona")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON (POR."IdCliente" = FWD."IdCliente" AND POR."IdPortafolio" = FWD."IdPortafolio")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM
	ON (PARAM."IdParametro" = FWD."IdModalidadCumplimiento" AND
		PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PARAM."IdEmpresa" = FWD."IdEmpresa")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAMFIXING
	ON (PARAMFIXING."IdParametro" = FWD."IdFixing")
LEFT JOIN (
		SELECT
			CUS."FolioCartera", SUM(RES."IngCantidad") AS "MontoCalce", CUS."Cantidad" AS "MontoOperacion",
			ABS(SUM(RES."IngCantidad") - CUS."Cantidad") AS "MontoDisponible"
		FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CUS
			ON (CUS."IdOperacion" = RES."IdOperacion" AND RES."IdTipoProducto" = CUS."IdTipoProducto" AND CUS."IdEstado" = 'VIG')
		WHERE RES."IdTipoProducto" = 'MEFW'
		GROUP BY CUS."FolioCartera", CUS."Cantidad"
		) AS TABLAFWDDISPONIBLE
	ON TABLAFWDDISPONIBLE."FolioCartera" = FWD."FolioCartera"
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
	ON (PAR."IdMoneda" = FWD."IdMonedaPrincipal" AND
		PAR."Fecha" = (CASE WHEN FWD."IdEstado" = 'ANT' OR (FWD."IdEstado" = 'VCD' AND FWD."EGRIdTipoOperacion" = 'ANTFWD') 
							THEN FWD."EGRFechaOperacion" ELSE FWD."FechaVencimientoOperacion" END))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUSD
	ON (PARUSD."IdMoneda" = 'USD' AND PARUSD."Fecha" = FWD."FechaVencimientoOperacion")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUF
	ON (PARUF."IdMoneda" = 'UF' AND PARUF."Fecha" = FWD."FechaVencimientoOperacion")

LEFT JOIN (
		SELECT
			"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
			"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
			MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "RecibePaga" = 'P' AND "IdTipoProducto" = 'MEFW'
		GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPE
	ON (FWD."IdCartera" = FPE."IdOperacion")

LEFT JOIN (
		SELECT
			"IdOperacion", "IdFormaDePago" AS "IdMedioDePago" , "NombreFormaDePago" AS "NombreMedioDePago", sum("Monto") AS "Monto", 
			"FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacionGrilla",
			MAX("FechaLiquidacionComprobantePagoOperacion") AS "FechaCondicionLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "RecibePaga" = 'R' AND "IdTipoProducto" = 'MEFW'
		GROUP BY "IdOperacion", "IdFormaDePago", "NombreFormaDePago", "FechaLiquidacionComprobantePagoOperacion") AS FPR
	ON (FWD."IdCartera" = FPR."IdOperacion")	
LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" AS PCC
	ON (PCC."IdParametro" = OPE."IdClasificacionCartera" AND PCC."NombreTipoParametro" = 'ClasificacionCartera' AND PCC."IdEstado" = 'VIG')
--NUEVOS CAMPOS
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES
	ON (FWD."IdMesa" = MES."IdMesa" AND FWD."IdEmpresa" = MES."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONPRI
	ON (FWD."IdMonedaPrincipal" = MONPRI."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONSEC
	ON (FWD."IdMonedaSecundaria" = MONSEC."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS PLAZA
	ON (FWD."IdPlaza" = PLAZA."IdCiudad")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TIPCAN
	ON (FWD."IdTipoCanalIngreso" = TIPCAN."IdTipoCanalIngreso")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
	ON (OPE."IdUsuarioCreacion" = USU."IdUsuario")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST
	ON (FWD."IdEstado" = EST."IdEstado")

WHERE FWD."IdTipoProducto" = 'MEFW';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVistaAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION; 