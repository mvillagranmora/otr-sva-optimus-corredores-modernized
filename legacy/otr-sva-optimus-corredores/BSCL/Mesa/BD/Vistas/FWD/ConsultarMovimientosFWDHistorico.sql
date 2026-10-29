SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" AS

SELECT
	  CAR."IdEmpresa"
	, EMP."Nombre"														AS "NombreEmpresa"
	, CAR."IdCartera"
	, CAR."FolioCartera" 												AS "IdOperacion"
	, CAR."FolioCartera" 												AS "IdOperacionUI"
	, CAR."IdOperacion"													AS "FolioOperacion"
	, CAR."FechaOperacion" 												AS "FechaOperacion"
	/*, CASE
		WHEN CAR."IdEstado"='ANT' OR (CAR."IdEstado" IN ('VCD','LIQ') AND CAR."EGRIdTipoOperacion"='ANTFWD') THEN CAR."EGRFechaLiquidacion" 
		ELSE CAR."FechaLiquidacion" END									AS "FechaLiquidacion"*/
	, CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPE."FechaLiquidacionComprobantePagoOperacion"
		   WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPR."FechaLiquidacionComprobantePagoOperacion"
		   ELSE COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion") END AS "FechaLiquidacion"
	, CAR."EGRFechaOperacion"											AS "FechaAnticipo"
	, FPE."FechaLiquidacionComprobantePagoOperacion"					AS "FechaEntregamos"
	--, FPE."NombreMedioDePago"											AS "NombreMedioDePagoEntregamos"
	, FPR."FechaLiquidacionComprobantePagoOperacion"					AS "FechaRecibimos"
	--, FPR."NombreMedioDePago"											AS "NombreMedioDePagoRecibimos"
	, TO_CHAR(OPE."FechaOperacion",'HH24:MI:SS')						AS "HoraOperacion"
	, OPE."FechaOperacion"												AS "FechaOperacionOPE"
	, CAR."IdTipoInstrumento"											AS "IdTipoInstrumento"
	, CAR."IdInstrumento"												AS "IdInstrumento"
	--, CAR."RazonSocialInstrumento"										AS "NombreInstrumento"
	, INS."RazonSocial"													AS "NombreInstrumento"
	, CAR."Nemotecnico"													AS "Nemotecnico"
	, CAR."IdTipoOperacion"												AS "IdTipoOperacion"
	--, CAR."NombreTipoOperacion" 										AS "TipoOperacion"
	, TOPE."Nombre"				 										AS "TipoOperacion"
	, CAR."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"
	--, CAR."IdTipoProductoTIPOPE"										AS "IdTipoProductoTIPOPE"
	, TOPE."IdTipoProducto"												AS "IdTipoProductoTIPOPE"
	, CLI."IdCliente"													AS "IdCliente"
	--, CLI."DocumentoIdentidad"  										AS "RutCliente"
	, PER."DocumentoIdentidad"											AS "RutCliente"
	--, CLI."NombreCompleto"						 						AS "NombreCliente"
	, PER."NombreCompleto"												AS "NombreCliente"
	--, CLI."TipoDocumentoIdentidad"										AS "TipoDocumentoIdentidad"
	, PER."TipoDocumentoIdentidad"										AS "TipoDocumentoIdentidad"
	, 0 																AS "IdMonedaLocal"
	, CAR."IdMonedaPrincipal"											AS "IdMonedaPrincipal"
	, CAR."IdMonedaSecundaria" 											AS "IdMonedaSecundaria"
	, CAR."MontoMonedaPrincipal" 										AS "MontoMonedaPrincipalIngreso"
	, CASE
		WHEN CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaPrincipal" 
		ELSE CAR."INGMontoMonedaPrincipal" END							AS "MontoMonedaPrincipal"
	, CAR."MontoMonedaSecundaria" 										AS "MontoMonedaSecundariaIngreso"
	, CASE
		WHEN CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaSecundaria" 
		ELSE CAR."INGMontoMonedaSecundaria" END							AS "MontoMonedaSecundaria"
	, 0 																AS "ResultadoMonedaLocalPuntas"
	, 0 																AS "ResultadoMonedaLocalBidAsk"
	, 0 																AS "ResultadoMonedaLocalDO"
	, 0 																AS "ResultadoMonedaLocalTCSpot"
	, 0 																AS "ResultadoMonedaLocalTCCierreMesa"
	, 0 																AS "ResultadoMonedaLocalConsolidado"
	, 0 																AS "MontoMonedaPrincipalEquivalente"
	, 0 																AS "IdMonedaEquivalente"
	, CAR."ValorTipoCambioCierre" 										AS "TipoCambioCierre"
	, CAR."ValorTipoCambioCosto" 										AS "TipoCambioCosto"
	, CAR."ValorParidadCierre" 											AS "ParidadCierre"
	, CAR."ValorParidadCosto" 											AS "ParidadCosto"
	, CAR."INGMontoDiferido" 											AS "MontoEquivalente"
	, CAR."INGMontoMargen" 												AS "Margen"
	, 0 																AS "Utilidad"
	, CAR."INGMontoDiferido" 											AS "MontoDiferido"
	, 0																	AS "MontoCalce"
	, CASE
		WHEN COALESCE(TABLAFWDDISPONIBLE."FolioCartera",0) = 0 THEN CAR."MontoMonedaPrincipal"
		ELSE TABLAFWDDISPONIBLE."MontoDisponible" END					AS "MontoDisponible"
	, CAR."EGRMontoLiquidacion"
	, CAR."EGRMontoARecibirAnticipo"
	, CASE
		--WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaPrincipal"
		--WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" <> 'ANTFWD' THEN CAR."INGMontoMonedaPrincipal"
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN
			CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."EGRMontoMonedaPrincipal" ELSE CAR."EGRMontoMonedaSecundaria" END
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" <> 'ANTFWD' THEN
			CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."INGMontoMonedaPrincipal" ELSE CAR."INGMontoMonedaSecundaria" END
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'COMFWD' AND CAR."EGRMontoLiquidacion" < 0 
			THEN ABS(CAR."EGRMontoLiquidacion")
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'VENFWD' AND CAR."EGRMontoLiquidacion" > 0 
			THEN CAR."EGRMontoLiquidacion"
		ELSE 0 END 														AS "MontoRecibimos"
	, CASE
		--WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN CAR."EGRMontoMonedaSecundaria"
		--WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" <> 'ANTFWD' THEN CAR."INGMontoMonedaSecundaria"
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN
			CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."EGRMontoMonedaSecundaria" ELSE CAR."EGRMontoMonedaPrincipal" END
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' AND CAR."EGRIdTipoOperacion" <> 'ANTFWD' THEN
			CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."INGMontoMonedaSecundaria" ELSE CAR."INGMontoMonedaPrincipal" END
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'COMFWD' AND CAR."EGRMontoLiquidacion" > 0 
			THEN CAR."EGRMontoLiquidacion"
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'VENFWD' AND CAR."EGRMontoLiquidacion" < 0 
			THEN ABS(CAR."EGRMontoLiquidacion")
		ELSE 0 END														AS "MontoPagamos"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN
			CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."IdMonedaPrincipal" ELSE CAR."IdMonedaSecundaria" END
		WHEN CAR."IdEstado" = 'LIQ' AND COALESCE(CAR."EGRIdMonedaLiquidacion",'') <> '' THEN CAR."EGRIdMonedaLiquidacion"
		ELSE 'CLP' END													AS "IdMonedaMontoRecibimos"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN
			CASE WHEN CAR."IdTipoOperacion" = 'COMFWD' THEN CAR."IdMonedaSecundaria" ELSE CAR."IdMonedaPrincipal" END
		WHEN CAR."IdEstado" = 'LIQ' AND COALESCE(CAR."EGRIdMonedaLiquidacion",'') <> '' THEN CAR."EGRIdMonedaLiquidacion"
		ELSE 'CLP' END													AS "IdMonedaMontoPagamos"
	, CASE	--campo para determinar si se hace Ingreso y/o Egreso en ENVIAR A TESORERIA de blotter Vencimiento Forward
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN 'INGEGR'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'COMFWD' AND CAR."EGRMontoLiquidacion" < 0 THEN 'EGR'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'VENFWD' AND CAR."EGRMontoLiquidacion" > 0 THEN 'EGR'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'COMFWD' AND CAR."EGRMontoLiquidacion" > 0 THEN 'ING'
		WHEN PARAM."ValorParametro" = 'COMPENSADA' AND CAR."IdTipoOperacion" = 'VENFWD' AND CAR."EGRMontoLiquidacion" < 0 THEN 'ING'
		ELSE '' END														AS "IngresoEgreso"
	, CAR."INGEsPuntos"													AS "EsPuntos"
	, CAR."INGTasaPuntosMonedaPrincipal"								AS "PuntosForward"
	, CAR."INGTasaPuntosMonedaSecundaria"								AS "TasaForward"
	, CAR."IdModalidadCumplimiento" 									AS "Modalidad"
	, PARAM."NombreParametro" 											AS "ModalidadLiquidacion"
	, PARAM."ValorParametro" 											AS "ModalidadLiquidacionValor"
	, CAR."IdFixing"													AS "IdFixing"
	, CAR."FechaFixing"													AS "FechaFixingCartera"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
		--WHEN PARAM."ValorParametro" = 'COMPENSADA' AND PARAMFIXING."ValorParametro" = 'BANCOCENTRAL' THEN ''
		WHEN TO_CHAR(CAR."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
		ELSE TO_CHAR(CAR."FechaFixing",'yyyy-MM-dd') END				AS "FechaFixing"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
		--WHEN PARAM."ValorParametro" = 'COMPENSADA' AND PARAMFIXING."ValorParametro" = 'BANCOCENTRAL' THEN ''
		WHEN TO_CHAR(CAR."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
		ELSE TO_CHAR(CAR."FechaFixing",'dd-MM-yyyy') END				AS "FechaFixingGrillaVcto"
	, CASE
		WHEN PARAM."ValorParametro" = 'ENTREGA_FISICA' THEN ''
		--WHEN PARAM."ValorParametro" = 'COMPENSADA' AND PARAMFIXING."ValorParametro" = 'BANCOCENTRAL' THEN ''
		WHEN TO_CHAR(CAR."FechaFixing",'yyyy-MM-dd') = '0001-01-01' THEN ''
		ELSE TO_CHAR(CAR."HoraFixing",'HH24:MI') END					AS "HoraFixing"
	, PARAMFIXING."NombreParametro"										AS "FixingNombre"
	, PARAMFIXING."ValorParametro"										AS "Fixing"
	--, CAR."PlazoEnDiasOperacion" 										AS "Plazo"
	, CASE
		WHEN CAR."PlazoEnDiasOperacion" > 0 THEN CAR."PlazoEnDiasOperacion"
		WHEN CAR."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN
			DAYS_BETWEEN(CAR."FechaOperacion", CAR."FechaVencimientoOperacion")
		ELSE 0 END AS "Plazo"
	, GREATEST(COALESCE(DAYS_BETWEEN (
		TO_SECONDDATE((SELECT CON."Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" CON 
					   WHERE CON."IdEmpresa" = CAR."IdEmpresa" AND CON."CodigoParametro" = 'PLA.FechaSistema'),'YYYY-MM-DD'),
		TO_SECONDDATE((CASE WHEN CAR."IdEstado" = 'ANT' OR 
								(CAR."IdEstado" = 'VCD' AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRFechaOperacion" 
							ELSE CAR."FechaVencimientoOperacion" END), 'YYYY-MM-DD')), 0), 0) AS "PlazoResidual" 
	, CAR."FechaVencimientoOperacion" 									AS "FechaVencimiento"
	, CAR."FechaVencimientoOperacion" 									AS "FechaVencimientoFiltro"
	, COALESCE(CAR."EGRValorParidad",0)									AS "Precio"
	, CASE
		WHEN CAR."Nemotecnico" = 'SECARE' THEN
			CASE WHEN CAR."IdEstado" = 'ANT' OR 
					 (CAR."IdEstado" IN ('VCD','LIQ') AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRTipoCambioAnticipo"
				 WHEN CAR."IdEstado" = 'VCD' OR CAR."IdEstado" = 'LIQ' THEN CAR."EGRPrecio"
				 WHEN CAR."IdEstado" = 'VIG' THEN 
				   CASE WHEN COALESCE(PARUF."TipoCambioMercado",0)=0 THEN NULL ELSE PARUSD."TipoCambioMercado"/PARUF."TipoCambioMercado" END
				 ELSE 0 END
		WHEN CAR."Nemotecnico" != 'ARBITR' THEN
			CASE WHEN CAR."IdEstado" = 'ANT' OR 
					 (CAR."IdEstado" IN ('VCD','LIQ') AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRTipoCambioAnticipo"
				 WHEN CAR."IdEstado" = 'VCD' OR CAR."IdEstado" = 'LIQ' THEN CAR."EGRPrecio"
				 WHEN CAR."IdEstado" = 'VIG' THEN PAR."TipoCambioMercado"
				 ELSE 0 END
		ELSE 0 END														AS "PrecioVencimiento"
	, CASE
		WHEN CAR."Nemotecnico" = 'ARBITR' THEN
			CASE WHEN CAR."IdEstado" = 'ANT' OR 
					 (CAR."IdEstado" IN ('VCD','LIQ') AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRTipoCambioAnticipo"
				 WHEN CAR."IdEstado" = 'VCD' OR CAR."IdEstado" = 'LIQ' THEN CAR."EGRPrecio"
				 WHEN CAR."IdEstado" = 'VIG' THEN PAR."ParidadMercado"
				 ELSE 0 END
		ELSE 0 END														AS "ParidadVencimiento"
	, CASE
		WHEN (CAR."IdEstado" = 'ANT' OR (CAR."IdEstado" = 'VCD' AND CAR."EGRIdTipoOperacion" = 'ANTFWD')) AND 
			  CAR."Nemotecnico" != 'ARBITR' THEN CAR."EGRTipoCambioAnticipo" 
		ELSE 0 END														AS "TipoCambioAnticipo"
	, CASE
		WHEN (CAR."IdEstado" = 'ANT' OR (CAR."IdEstado" = 'VCD' AND CAR."EGRIdTipoOperacion" = 'ANTFWD')) AND 
			  CAR."Nemotecnico" = 'ARBITR' THEN CAR."EGRTipoCambioAnticipo"
		ELSE 0 END														AS "ParidadAnticipo"
	, CASE
		WHEN CAR."IdEstado" = 'LIQ' THEN CAR."EGRIdMonedaLiquidacion"
		--WHEN CAR."Nemotecnico" = 'ARBITR' THEN 'USD' 
		ELSE CASE WHEN PARAM."ValorParametro" = 'COMPENSADA' THEN OPE."IdMonedaCompensacion" ELSE 'CLP' END END	AS "IdMonedaLiquidacion" 
	, CASE
		WHEN PARAM."ValorParametro" = 'COMPENSADA' THEN CAR."FechaFixing"
		ELSE CAR."FechaVencimientoOperacion" END						AS "FechaEfectiva"
	, CAR."IdAgente" 													AS "IdAgente"
	, CAR."IdAgente" 													AS "Agente"  
	, (AG."Nombres" ||' '||AG."ApellidoPaterno")						AS "NombreAgente"
	, CAR."IdSucursal"													AS "IdSucursal"
	, '' 																AS "OperacionFicticia"
	, USU."CodigoUsuario" 												AS "CodigoUsuario"
	, CAR."IdEstado"													AS "IdEstado"
	--, CAR."IdEstado"													AS "Estado"
	, CASE WHEN CAR."IdEstado" = 'PEN' AND COALESCE(AUT."IdEstado",'') = 'VIG' THEN 'PAU' ELSE CAR."IdEstado" END AS "Estado"
	, OPE."IdEstado"													AS "EstadoOperacion"
	, CAR."IdTipoProducto"												AS "IdTipoProducto"
	, CAR."IdTipoProducto"												AS "Mesa"
	, SOC."IdSocioDeNegocio"											AS "IdSocioDeNegocio"
	, CAR."IdPortafolio"												AS "IdPortafolio"
	, POR."CodigoPortafolio" 											AS "CodigoPropositoPortafolio"
	, POR."Proposito" 													AS "Proposito"
	, CAR."IdTipoCanalIngreso"											AS "IdTipoCanalIngreso"
	, CAR."INGMontoValorRazonableActivo"								AS "MontoValorRazonableActivo"	
	, CAR."INGMontoValorRazonablePasivo"								AS "MontoValorRazonablePasivo"
	, CAR."INGMontoValorRazonableNeto"									AS "MontoValorRazonableNeto"	
	, CAR."INGTasaValorRazonableActivo"									AS "TasaValorRazonableActivo"
	, CAR."INGTasaValorRazonablePasivo"									AS "TasaValorRazonablePasivo"
	, CAR."INGCantidad"													AS "INGCantidad"
	, CAR."INGMontoMonedaPrincipal"										AS "INGMontoMonedaPrincipal"
	, CAR."INGMontoMonedaSecundaria"									AS "INGMontoMonedaSecundaria"
	, CASE
		WHEN CAR."IdEstado"='ANT' OR (CAR."IdEstado" IN ('VCD','LIQ') AND CAR."EGRIdTipoOperacion" = 'ANTFWD') THEN CAR."EGRFechaOperacion" 
		ELSE CAR."FechaVencimientoOperacion" END						AS "FechaFiltroGrillaVcto"
	, CAR."IdMesa"
	, OPE."IdSpotAsociado"
	, OPE."IdClasificacionCartera"
	, PCC."ValorParametro"												AS "ClasificacionCartera"
	, CAR."Glosa"
	, CAR."EGRTipoCambioObservado"										AS "TCObservadoTABANT"
	, CAR."EGRMontoMonedaPrincipal"										AS "MontoMonedaPricipalTABANT"
	, CAR."EGRMontoMonedaSecundaria"									AS "MontoMonedaSecundariaTABANT"
	, CAR."EGRTasaDescuentoAnticipo"									AS "TasaDescuentoTABANT"
	, CAR."EGRMontoARecibirAnticipo"									AS "MontoAnticipoTABANT"
	, CAR."EGRTipoCambioAnticipo"										AS "TCAnticipoTABANT"
	, CAR."IdPlaza"
	, CAR."TasaCompra"
	, CAR."EGRIdSpotAsociado"
	, TO_VARCHAR(OPE."FechaLiquidacion",'YYYY-MM-DD')					AS "FechaPago"
	, CAR."INGMontoATasaMercadoActual" AS "MontoATasaMercadoActual" --Monto Valorizado a moneda sistema
	, TIOP."Accion"
	, TIPMON."MultiplicaODivide"
	, PER."IdPersona"
	, PER."IdPaisResidencia"
	, CAR."EGRIdTipoOperacion"
	, CASE WHEN CAR."IdEstado" IN ('ANT','VCD','LIQ') THEN
			CASE WHEN CAR."EGRIdTipoOperacion" = 'ANTFWD' THEN 'ANTICIPADA' ELSE 'VENCIMIENTO' END
		   ELSE '' END													AS "TipoVencimiento"
	, CAR."FechaRespaldo"
	, CAR."FechaModificacionRegistro"
	, CAR."IdUsuarioMod"
	, OPE."IdMonedaCompensacion"
	, CE."NombreCompleto" AS "NombreEmpleadoDigitador"
FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarTablaCartera" CAR
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
	ON (OPE."IdOperacion" = CAR."IdOperacion" AND OPE."IdTipoProducto" = CAR."IdTipoProducto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
	ON (CAR."IdEmpresa" = EMP."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
	ON (CAR."IdUsuario" = USU."IdUsuario")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
	ON (CAR."IdInstrumento" = INS."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE
	ON (CAR."IdTipoOperacion" = TOPE."IdTipoOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS AG
	ON (CAR."IdAgente" = AG."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON (CAR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
	ON CLI."IdPersona" = PER."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
	ON (CLI."IdPersona" = SOC."IdPersona")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON (POR."IdCliente" = CAR."IdCliente" AND POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdEstado" IN ('ACT','BLO'))
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM
	ON (PARAM."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PARAM."IdEmpresa" = CAR."IdEmpresa")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAMFIXING
	ON (PARAMFIXING."IdParametro" = CAR."IdFixing")
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
	ON TABLAFWDDISPONIBLE."FolioCartera" = CAR."FolioCartera"
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
	ON (PAR."IdMoneda" = CAR."IdMonedaPrincipal" AND
		PAR."Fecha" = (CASE WHEN CAR."IdEstado" = 'ANT' OR (CAR."IdEstado" = 'VCD' AND CAR."EGRIdTipoOperacion" = 'ANTFWD') 
							THEN CAR."EGRFechaOperacion" ELSE CAR."FechaVencimientoOperacion" END))
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARUSD
	ON (PARUSD."IdMoneda" = 'USD' AND PARUSD."Fecha" = CAR."FechaVencimientoOperacion")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARUF
	ON (PARUF."IdMoneda" = 'UF' AND PARUF."Fecha" = CAR."FechaVencimientoOperacion")
LEFT JOIN (
        SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa", "FechaIngresoComprobantePagoOperacion"
        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
    ) AS FPE
    ON (FPE."IdOperacion" = CAR."IdCartera" AND FPE."IdEmpresa" = CAR."IdEmpresa" AND FPE."FechaIngresoComprobantePagoOperacion" BETWEEN ADD_DAYS(TO_DATE(OPE."FechaOperacion"),-2) AND ADD_DAYS(TO_DATE(OPE."FechaOperacion"),2))
LEFT JOIN (
        SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa", "FechaIngresoComprobantePagoOperacion"
        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
    ) AS FPR
   ON (FPR."IdOperacion" = CAR."IdCartera" AND FPR."IdEmpresa" = CAR."IdEmpresa" AND FPR."FechaIngresoComprobantePagoOperacion" BETWEEN ADD_DAYS(TO_DATE(OPE."FechaOperacion"),-2) AND ADD_DAYS(TO_DATE(OPE."FechaOperacion"),2))
LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" AS PCC
	ON (PCC."IdParametro" = OPE."IdClasificacionCartera" AND PCC."NombreTipoParametro" = 'ClasificacionCartera' AND PCC."IdEstado" = 'VIG')
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIOP
	ON (CAR."IdTipoOperacion" = TIOP."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (CAR."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS AUT
	ON (TO_NVARCHAR(CAR."IdOperacion") = COALESCE(AUT."NumeroOperacionConcepto",'0') AND
		CAR."IdEmpresa" = AUT."IdEmpresa" AND
		CAR."IdTipoOperacion" = AUT."IdTipoOperacion" AND
		AUT."IdModulo" = 'MFW')
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS CE
	ON (CE."IdEmpleado" = OPE."IdEmpleadoDigitador")
WHERE CAR."IdTipoProducto" = 'MEFW';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
