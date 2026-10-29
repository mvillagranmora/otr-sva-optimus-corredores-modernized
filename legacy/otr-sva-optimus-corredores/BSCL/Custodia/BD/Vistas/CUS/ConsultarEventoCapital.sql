SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital"
AS 
SELECT
	EC."IdEventoCapital"					AS "IdEventoCapital",
	EC."IdEmpresa"							AS "IdEmpresa",
	EC."IdTipoEventoCapital"				AS "IdTipoEventoCapital",
	EC."Secuencia"							AS "Secuencia",
	EC."Descripcion"						AS "Descripcion",
	EC."EsInscrito"							AS "EsInscrito",
	EC."IdClasificacionTipoInstrumento"		AS "IdClasificacionTipoInstrumento",
	EC."IdTipoInstrumento"					AS "IdTipoInstrumento",
	EC."IdInstrumento"						AS "IdInstrumento",
	EC."Nemo"								AS "Nemo",
	CI."ISIN"								AS "ISIN",
	EC."Factor"								AS "Factor",
	EC."Impuesto"							AS "Impuesto",
	EC."Valor"								AS "Valor",
	EC."IdMoneda"							AS "IdMoneda",
	EC."IdMonedaLiquidacion"				AS "IdMonedaLiquidacion",
	EC."Paridad"							AS "Paridad",
	MON."Nombre"							AS "NombreMoneda",
	EC."IdInstrumentoNuevo"					AS "IdInstrumentoNuevo",
	EC."NemoNuevo"							AS "NemoNuevo",
	EC."FactorNuevo"						AS "FactorNuevo",
	EC."FechaActualizacion"					AS "FechaActualizacion",
	EC."FechaLimite"						AS "FechaLimite",
	EC."FechaPago"							AS "FechaPago",
	EC."NumeroDividendo"					AS "NumeroDividendo",
	EC."IdTipoDividendo"					AS "IdTipoDividendo",
	EC."PrecioAnterior"						AS "PrecioAnterior",
	EC."FechaHoraIngresoRegistro"			AS "FechaHoraIngresoRegistro",
	EC."FechaHoraUltimaActualizacion"		AS "FechaHoraUltimaActualizacion",
	EC."TipoIngreso"						AS "TipoIngreso",
	EC."IdUsuario"							AS "IdUsuario",
	EC."IdEstado"							AS "IdEstado",
    TP."Nombre"           					AS "TipoEvento",
    EC."CuentaDepositanteSorteoLetras"		AS "CuentaDepositanteSorteoLetras",
    EC."SorteadoSorteoLetras"				AS "SorteadoSorteoLetras",
    EC."APagarSorteoLetras"					AS "APagarSorteoLetras",
    EC."APagarPesosSorteoLetras"			AS "APagarPesosSorteoLetras",
    CASE EC."Origen"          			
  		WHEN 'SIS'	THEN 'SISTEMA'
  		WHEN 'RUE'	THEN 'RUEBAS'
  		WHEN 'AUT'	THEN 'AUTOMATICO'
  		ELSE 'MANUAL'
  	END  									AS "Origen",
  	EC."IdEmisor"							AS "IdEmisor",
  	EC."EstadoNotificacion"					AS "EstadoNotificacion",
  	CASE EC."EsInscrito" 
  		WHEN 'S' THEN EC."Indice"
  		ELSE 0	
  	END										AS "Indice",
	CASE EC."EsInscrito" 
  		WHEN 'S' THEN MAX(VC."ACV_INDICE")
  		ELSE 0	
  	END										AS  "IndiceRue",
  	EC."CantidadCuotas",
  	EC."Monto",
	OPEVIG."OperacionesVigentes",
	BOV."Nombre" AS "IdBoveda",
	SUM(COALESCE(EVTCAPCLI."Total",0))		AS "MontoRepartido",
	COALESCE(EC."ISFUT",'N') 				AS "ISFUT",
	COALESCE(EC."TasaEfectiva",0) 			AS "TasaEfectiva",
	EC."SecuenciaRelacionada"				AS "SecuenciaRelacionada",
	EC."SuscripcionComplementaria"			AS "SuscripcionComplementaria",
	CASE WHEN EC."IdTipoEventoCapital" <> 'CC' THEN '' ELSE CI."AfectoArt104" END	AS "RETART104",
	EC."ImpuestoDivExtranjero",
	EC."GastoTransferencia",
	PAIS."NombrePais"						AS "PaisEmisorInstrumento"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" 							AS EC
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalBursatil"					AS VC
		ON (EC."IdTipoEventoCapital" 			= VC."ACF_TIPO_VC"				AND
			EC."Secuencia"						= VC."ACF_SEC_VC"				AND
			EC."IdClasificacionTipoInstrumento"	= CASE VC."ACF_MERCADO"
													WHEN 'AC' THEN 'RV'
													ELSE VC."ACF_MERCADO"
												  END	
		AND LTRIM(RTRIM(EC."Nemo"))			= 	LTRIM(RTRIM(VC."ACF_NEMO")))	
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" 		AS TP
	    ON EC."IdTipoEventoCapital" = TP."IdTipoEventoCapital"    
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS MON
		ON EC."IdMoneda" = MON."IdMoneda"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"		AS CI
		ON EC."IdInstrumento" = CI."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOV ON BOV."IdBoveda" = EC."IdBoveda" 
	
	LEFT JOIN (
				SELECT "IdEventoCapital",
					   COALESCE("OperacionesVigentes",'') AS "OperacionesVigentes"
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				WHERE "OperacionesVigentes" = 'S'
				GROUP BY "IdEventoCapital","OperacionesVigentes"
	) AS OPEVIG
		   ON OPEVIG."IdEventoCapital" = EC."IdEventoCapital"
   LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EVTCAPCLI ON 
   					EVTCAPCLI."IdInstrumento" = EC."IdInstrumento" AND EC."IdEstado" IN ('CER') 
   					AND EVTCAPCLI."TipoSocioNegocio" = 'CLI' AND  EVTCAPCLI."IdEstado" <> 'ANU' 
   					AND EVTCAPCLI."NumeroDividendo" = EC."NumeroDividendo" AND EC."IdTipoEventoCapital" = 'DV' --
   					AND (TO_DATE(EVTCAPCLI."FechaPago") BETWEEN TO_DATE(EC."FechaLimite") AND TO_DATE(EC."FechaPago"))
   	LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI  ON CI."IdEmisor" = EMI."IdEmisor"
   	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON EMI."IdPersona" = PER."IdPersona"
   	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON PER."IdPaisResidencia" = PAIS."IdPais"
WHERE EC."IdEstado" <> 'ANU'  -- Los anulados fueron borrados por el usuario(no deben verse, solo quedan respaldados en BD)		
	GROUP BY
		EC."IdEventoCapital",
		EC."IdEmpresa",
		EC."IdTipoEventoCapital",
		EC."Secuencia",
		EC."Descripcion",
		EC."EsInscrito",
		EC."IdClasificacionTipoInstrumento",
		EC."IdTipoInstrumento",
		EC."IdInstrumento",
		EC."Nemo",
		CI."ISIN",
		EC."Factor",
		EC."Impuesto",
		EC."Valor",
		EC."IdMoneda",
		EC."IdMonedaLiquidacion",
		EC."Paridad",
		MON."Nombre",
		EC."IdInstrumentoNuevo",
		EC."NemoNuevo",
		EC."FactorNuevo",
		EC."FechaActualizacion",
		EC."FechaLimite",
		EC."FechaPago",
		EC."NumeroDividendo",
		EC."IdTipoDividendo",
		EC."PrecioAnterior",
		EC."FechaHoraIngresoRegistro",
		EC."FechaHoraUltimaActualizacion",
		EC."TipoIngreso",
		EC."IdUsuario",
		EC."IdEstado",
		TP."Nombre", 
		EC."CuentaDepositanteSorteoLetras",
		EC."SorteadoSorteoLetras",
		EC."APagarSorteoLetras",
		EC."APagarPesosSorteoLetras",
		EC."Origen",          			
		EC."IdEmisor",
		EC."EstadoNotificacion",
		EC."Indice",
		EC."CantidadCuotas",
		EC."Monto",
		OPEVIG."OperacionesVigentes",
	 	BOV."Nombre",
	 	EC."ISFUT",
	 	EC."TasaEfectiva",
	 	EC."SecuenciaRelacionada",
	 	EC."SuscripcionComplementaria",
		CI."AfectoArt104",
		EC."ImpuestoDivExtranjero",
		EC."GastoTransferencia",
		PAIS."NombrePais";
GO	 
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital" TO BCS_DESARROLLADOR WITH GRANT OPTION;
