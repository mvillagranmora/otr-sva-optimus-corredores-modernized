SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales" as
SELECT 
		CNP."IdConcepto",
		CNP."TipoConcepto",
		CNP."Nombre" 																			AS "NombreConcepto",	 
		CNP."IdEstado",
		CNP."IdTipoCriterioNeteo",
		CNP."NombreConceptoCartola",
		CNP."ExclusivoCartola",
		CNP."ConceptoAbonoReplica",
		CNP."ConceptoCargoReplica",
		COALESCE(TCN."NombreTipoCriterioNeteo",'') as "NombreTipoCriterioNeteo",
		EST."Nombre" 																			AS "NombreEstado",
		--------------------------------------------------
		CUS."IdConceptoCustodia",
		CASE WHEN CUS."NombreEntidad" = 'CUS.INGRESORETIROTITULO' 	THEN 'Titulo'
			 WHEN CUS."NombreEntidad" = 'CUS.GARANTIA'				THEN 'Garantia'
			 WHEN CUS."NombreEntidad" = 'CUS.PRESTAMO'				THEN 'Prestamo'
			 WHEN CUS."NombreEntidad" = 'CUS.SORTEOLETRAS'			THEN 'SorteoLetras'
			 WHEN CUS."NombreEntidad" = 'CUS.CARGAMASIVACUSTODIA'	THEN 'CargaMasivaCustodia'
		 END 						        AS "UsoCUS",
		CUS."IdClasificacionTipoInstrumento",
		COALESCE(CTI."Nombre",'') 		 AS "NombreClasificacionTipoInstrumento",
		CUS."IdTipoInstrumento",
		COALESCE(TI."Nombre",'')       			AS "NombreTipoInstrumento",
		CUS."IdInstrumento",
		COALESCE(INS."Nemotecnico",'') 				AS "Nemotecnico",
		CUS."IdContraparte",
		COALESCE(EDG."NombreEntidad",'') 			AS "NombreContraparte",
		COALESCE(CUS."IdTipoCuenta",'') 				AS "IdTipoCuenta",
		COALESCE(TCC."Nombre",'') 										           			AS "TipoCuenta",
		CUS."ConCertificadoAutomatico",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."ConCertificadoAutomatico") 	as "ConCertificadoAutomaticoTra",
		CUS."GeneraMovimientoEnTransito",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."GeneraMovimientoEnTransito") 	as "GeneraMovimientoEnTransitoTra",
		CUS."GeneraTraspaso",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."GeneraTraspaso") 				as "GeneraTraspasoTra",
		CUS."EsGarantiaVendible",
		CUS."RetieneDividendos",
		CUS."FolioObligatorio",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."EsGarantiaVendible") 			as "EsGarantiaVendibleTra",
		CUS."GeneraTransferenciaBoveda",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."GeneraTransferenciaBoveda") 	as "GeneraTransferenciaBovedaTra",
		CUS."EsTransferenciaPortafolio",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."EsTransferenciaPortafolio") 	as "EsTransferenciaPortafolioTra",
		CUS."EsTransferenciaCliente",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."EsTransferenciaCliente") 		as "EsTransferenciaClienteTra",
		CUS."VisiblePorEjecutivo" 																AS "VisiblePorEjecutivoCUS",
		CUS."VisibleOptimus" 																	AS "VisibleOptimus",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."VisiblePorEjecutivo") 			AS "VisiblePorEjecutivoCUSTra",
		CUS."RetieneAmortizacion",
		CUS."RetieneIntereses",
		CUS."VisibleOptimus" 																	AS "ConceptoConfiguradoOptimusCus",
		CUS."VisibleFiori" 																		AS "ConceptoConfiguradoFioriCus",
		CUS."VisibleWS" 																		AS "ConceptoConfiguradoWsCus",
		CUS."EsGarantiaPrendaria" 																AS "EsGarantiaPrendaria",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CUS."ConfiguradoEnModulo") 			AS "ConfiguradoEnModuloCUSTra",
		--------------------------------------------------
		
		CASE WHEN TES."NombreEntidad" = 'TES.IngresoEgreso'   THEN 'Comprobante'
			 WHEN TES."NombreEntidad" = 'TES.Deposito'	      THEN 'Deposito'
			 WHEN TES."NombreEntidad" = 'TES.CargaMasiva'     THEN 'CargaMasiva'
		 END AS "UsoTES",
		TES."DisponibleIngresoManual" 															AS "DisponibleIngresoManualTES",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TES."DisponibleIngresoManual") 		AS "DisponibleIngresoManualTESTra",
		TES."VisiblePorEjecutivo" 																AS "VisiblePorEjecutivoTES",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TES."VisiblePorEjecutivo") 			AS "VisiblePorEjecutivoTESTra",
		TES."VisibleOptimus" 																	AS "ConceptoConfiguradoOptimusTes",
		TES."VisibleFiori" 																		AS "ConceptoConfiguradoFioriTes",
		TES."VisibleWS" 																		AS "ConceptoConfiguradoWsTes",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TES."ConfiguradoEnModulo") 			AS "ConfiguradoEnModuloTESTra",
		COALESCE(TES."NoValidaSaldoCaja",'N') 													AS "NoValidaSaldoCajaTES",
		TES."GeneraComprobanteAutomaticamente"													AS "GeneraComprobanteAutomaticamenteTES",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TES."GeneraComprobanteAutomaticamente") AS "GeneraComprobanteAutomaticamenteTESTra",
		
		--------------------------------------------------
		CTA."DisponibleIngresoManual" 															AS "DisponibleIngresoManualCTA",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CTA."DisponibleIngresoManual") 		AS "DisponibleIngresoManualCTATra",
		CTA."VisiblePorEjecutivo" 																AS "VisiblePorEjecutivoCTA",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CTA."VisiblePorEjecutivo") 			AS "VisiblePorEjecutivoCTATra",
		CTA."VisibleOptimus" 																	AS "ConceptoConfiguradoOptimusCta",
		CTA."VisibleFiori" 																		AS "ConceptoConfiguradoFioriCta",
		CTA."VisibleWS" 																		AS "ConceptoConfiguradoWsCta",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CTA."ConfiguradoEnModulo") 			AS "ConfiguradoEnModuloCTATra",

-- CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci?n de cargos y abonos se agrega campo 'AfectaRentabilidad'
		CTA."AfectaRentabilidad"																AS "AfectaRentabilidadTES", 
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CTA."AfectaRentabilidad")           AS "AfectaRentabilidadTESTra", 
-- CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci?n de cargos y abonos se agrega campo 'AfectaRentabilidad'		

		--------------------------------------------------
-- OPTIMUSPE-324 Implementaci�n Requerimientos de Desarrollo Contabilidad Per�
		FAC."EsOtrosConceptos"																	AS "EsOtrosConceptos",
		FAC."EsReembolso"																		AS "EsReembolso",
		FAC."Contrapartida"																		AS "Contrapartida",
		FAC."AbonoClpContrapartida"																AS "AbonoClpContrapartida",
		FAC."CentroCosto"																		AS "CentroCosto",		
		FAC."CentroCosto"																		AS "OtrosConceptos",	
		FAC."VisibleOptimus" 																	AS "ConceptoConfiguradoOptimusFac",
		FAC."VisibleFiori" 																		AS "ConceptoConfiguradoFioriFac",
		FAC."VisibleWS" 																		AS "ConceptoConfiguradoWsFac",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(FAC."ConfiguradoEnModulo") 			AS "ConfiguradoEnModuloFACTra"
		
  FROM  "BSCL.Plataforma.BD::PLA.Concepto" 													AS CNP
 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" 												AS EST 
 		 ON EST."IdEstado" = CNP."IdEstado"
  LEFT JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" 										AS CUS 
  		 ON CUS."IdConcepto" = CNP."IdConcepto" 
  LEFT JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" 										AS TES 
  		 ON TES."IdConcepto" = CNP."IdConcepto"
  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"							AS CTA 
  		 ON CTA."IdConcepto" = CNP."IdConcepto"
  LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" 							AS CTI
  		 ON CTI."IdClasificacionTipoInstrumento" = CUS."IdClasificacionTipoInstrumento"
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" 										AS TI
  		 ON TI."IdTipoInstrumento" = CUS."IdTipoInstrumento"
  LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 											AS INS
  		 ON INS."IdInstrumento" = CUS."IdInstrumento"
  LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" 					AS EDG
  		 ON EDG."IdContraparte" = CUS."IdContraparte"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCuenta" 											AS TCC
  		 ON (TCC."IdTipoCuenta" = CUS."IdTipoCuenta" AND TCC."IdModulo"='CUS') 
-- OPTIMUSPE-324 Implementaci�n Requerimientos de Desarrollo Contabilidad Per� 
  LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"									AS FAC
  		ON FAC."IdConcepto" = CNP."IdConcepto" 
  LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCriterioNeteo" TCN
  		ON TCN."IdTipoCriterioNeteo" = CNP."IdTipoCriterioNeteo"
  		 	 
 --WHERE CNP."TipoConcepto" = 'MANUAL' 
ORDER BY CNP."Nombre" ASC
WITH READ ONLY;
GO
GRANT SELECT on "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales" to BCS_DESARROLLADOR with grant option;
GO
