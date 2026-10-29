SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::MES.ConsultarMovimientosOperaciones";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MES.ConsultarMovimientosOperaciones" AS
SELECT
    "IdEmpresa",
    "IdCartera",
    "IdOperacion",
    "IdOperacionUI",
    "FolioOperacion",
    "FechaOperacion",
    "FechaLiquidacion",
    "FechaEntregamos",
    "FechaRecibimos",
    "HoraOperacion",
    "FechaOperacionOPE",
    "NombreInstrumento",
    "Nemotecnico",
    "IdTipoOperacion",
    "TipoOperacion",
    "IdTipoInstrumento",
    "RutCliente",
    "NombreCliente",
    "CodigoPropositoPortafolio",
    "IdMonedaPrincipal",
    "IdMonedaSecundaria",
    "MontoMonedaPrincipal",
    "MontoMonedaSecundaria",
    "ResultadoMonedaLocalPuntas",
    "ResultadoMonedaLocalBidAsk",
    "ResultadoMonedaLocalDO",
    "ResultadoMonedaLocalTCSpot",
    "ResultadoMonedaLocalTCCierreMesa",
    "TipoCambioCierre",
    "TipoCambioCosto",
    "ParidadCierre",
    "ParidadCosto",
    "MontoEquivalente",
    "Margen",
    "Utilidad",
    "MontoDiferido",
    "Modalidad",
    "ModalidadLiquidacion",
    "ModalidadLiquidacionValor",
    "Plazo",
    "PlazoResidual",
    "FechaVencimiento",
    "FechaEfectiva",
    "Agente",
    "NombreAgente",
    "OperacionFicticia",
    "CodigoUsuario",
    "Estado",
    "Mesa",
    "IdTipoCanalIngreso",
    "INGCantidad",
    "INGMontoMonedaPrincipal",
    "INGMontoMonedaSecundaria",
    "Accion",
    "IdOperacionSpotReferencia",
    "EstadoConfirmacion",
    "FolioOrigenFW",
    "IdOperacionSpotOrigen",
    "IdOperacionSpotVigente",
    "IdEstado",
    "TipoVencimiento",
    "IdSolicitudAporteRescate",
    "EstadoAporteRescate",
    "TipoAporteRescate",
    "FechaLiquidacionAporteRescate",
    "FechaRespaldo",
    "IdPortafolio",
    "CodigoComercio",
    "MontoCalce",
    "MontoDisponible",
    "Glosa",
    "InstrumentoFM",
    "FondosNacionalesValutaCero",
    "AfectoADerivado",
    "DolarObservado",
    "FechaModificacionRegistro",
    "IdUsuarioMod",
    "IdAsignacion",
    "FechaOperacionLocal",
    "FechaModificacion",
    "HoraModificacion",
    "UsuarioMod",
    "IdPlataformaNegociacion",
    "IdMonedaCompensacion",
    "FormaPagoEntregamos",
    "FormaPagoRecibe",
    "NombreEmpleadoDigitador"
FROM (
    SELECT
        "T1".*, 
        CASE
            WHEN "T1"."Mesa" = 'MEME' THEN (TO_DATE(TO_VARCHAR(ADD_SECONDS("T1"."FechaOperacion",TO_INTEGER(conf."Valor")),'yyyy-MM-dd'),'yyyy-MM-dd'))
            WHEN "T1"."Mesa" = 'MEFW' THEN "T1"."FechaOperacion"
        END AS "FechaOperacionLocal" ,
        CASE WHEN "T1"."IdTipoInstrumento"='FWD' AND "T1"."IdEstado" IN ('MOD','ANU') THEN "T1"."FechaModificacionRegistro"
            ELSE '0001-01-01' END AS "FechaModificacion" ,
        CASE WHEN "T1"."IdTipoInstrumento"='FWD' AND "T1"."IdEstado" IN ('MOD','ANU') THEN TO_CHAR("T1"."FechaModificacionRegistro",'HH24:MI:SS')
            ELSE '' END AS "HoraModificacion" ,
        CASE WHEN "T1"."IdTipoInstrumento"='FWD' AND "T1"."IdEstado" IN ('MOD','ANU') THEN COALESCE(US."CodigoUsuario",'')
            ELSE '' END AS "UsuarioMod",
        ROW_NUMBER() OVER (
            PARTITION BY
                "T1"."IdOperacion", "T1"."IdTipoInstrumento" 
            ORDER BY
                EXTRACT(YEAR FROM "T1"."FechaOperacion") DESC, 
                "T1"."FechaOperacionOPE" DESC, 
                "T1"."FechaModificacionRegistro" DESC, 
                "T1"."FolioOperacion" DESC        
        ) as "ROWNUMERS",
         MAX(CASE WHEN "T1"."IdEstado" = 'MOD' THEN 1 ELSE 0 END) OVER (PARTITION BY "T1"."IdOperacion", "T1"."IdTipoInstrumento") as "Operaciones_Modificadas",
        COALESCE(FDPSPOT."FormaPagoEntregamos", FDPFW."FormaPagoEntregamos") AS "FormaPagoEntregamos",
		COALESCE(FDPSPOT."FormaPagoRecibe", FDPFW."FormaPagoRecibe") AS "FormaPagoRecibe"
    FROM
        (
        SELECT
	"IdEmpresa"
	,"IdCartera"
	,"IdOperacion"
	,"IdOperacionUI"
	,"FolioOperacion"
	,"FechaOperacion"
	,"FechaLiquidacion"
	,"FechaEntregamos"
	,"FechaRecibimos"
	,"HoraOperacion"
	,"FechaOperacionOPE"
	,"NombreInstrumento"
	,"Nemotecnico"
	,"IdTipoOperacion"
	,"TipoOperacion"
	,"IdTipoInstrumento"
	,"RutCliente"
	,"NombreCliente"
	,"CodigoPropositoPortafolio"
	,"IdMonedaPrincipal"
	,"IdMonedaSecundaria" 
	,"MontoMonedaPrincipal"
	,"MontoMonedaSecundaria"
	,"ResultadoMonedaLocalPuntas"
	,"ResultadoMonedaLocalBidAsk"
	,"ResultadoMonedaLocalDO"
	,"ResultadoMonedaLocalTCSpot"
	,"ResultadoMonedaLocalTCCierreMesa"
	,"TipoCambioCierre" 
	,"TipoCambioCosto" 
	,"ParidadCierre"
	,"ParidadCosto"
	,"MontoEquivalente"  
	,"Margen"  
	,"Utilidad"
	,"MontoDiferido"
	,"Modalidad"
	,"ModalidadLiquidacion"
	,"ModalidadLiquidacionValor"
	,"Plazo"
	,"PlazoResidual" 
	,"FechaVencimiento" 
	,"FechaEfectiva"
	,"Agente"  
	,"NombreAgente"
	,"OperacionFicticia"
	,"CodigoUsuario"
	,"Estado"
	,"Mesa"
	,"IdTipoCanalIngreso"
	,"INGCantidad"
	,"INGMontoMonedaPrincipal"
	,"INGMontoMonedaSecundaria"
	,"Accion"
	,0 AS "IdOperacionSpotReferencia"
	,'' AS "EstadoConfirmacion"
	,0 AS "FolioOrigenFW"
	,0 AS "IdOperacionSpotOrigen"
	,0 AS "IdOperacionSpotVigente"
	,"IdEstado"
	,"TipoVencimiento"
	--Fondos mutuos
	,0 AS "IdSolicitudAporteRescate"
	,'' AS  "EstadoAporteRescate" 
	,'' AS  "TipoAporteRescate"
	,'' AS  "FechaLiquidacionAporteRescate"
	,"FechaRespaldo"
	,"IdPortafolio"
	,0 AS "CodigoComercio"
	,"MontoCalce"
	,"MontoDisponible"
	,"Glosa"
	,'' AS "InstrumentoFM"
	,'' AS "FondosNacionalesValutaCero"
	,'NO' AS "AfectoADerivado"
	,'NO' AS "DolarObservado"
	,"FechaModificacionRegistro"
	,"IdUsuarioMod"
	,0 AS "IdAsignacion"
	,'OTC' AS "IdPlataformaNegociacion"
	,"IdMonedaCompensacion"
	,"NombreEmpleadoDigitador"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico"
UNION
SELECT
	"IdEmpresa"
	,"IdCartera"
	,"IdOperacion"
	,"IdOperacionUI"
	,"FolioOperacion"
	,"FechaOperacion"
	,"FechaLiquidacion"
	,"FechaEntregamos"
	,"FechaRecibimos"
	,"HoraOperacion"
	,"FechaOperacionOPE"
	,"NombreInstrumento"
	,"Nemotecnico"
	,"IdTipoOperacion"
	,"TipoOperacion"
	,"IdTipoInstrumento"
	,"RutCliente"
	,"NombreCliente"
	,"CodigoPropositoPortafolio"
	,"IdMonedaPrincipal"
	,"IdMonedaSecundaria" 
	,"MontoMonedaPrincipal"
	,"MontoMonedaSecundaria"
	,"ResultadoMonedaLocalPuntas"
	,"ResultadoMonedaLocalBidAsk"
	,"ResultadoMonedaLocalDO"
	,"ResultadoMonedaLocalTCSpot"
	,"ResultadoMonedaLocalTCCierreMesa"
	,"TipoCambioCierre" 
	,"TipoCambioCosto" 
	,"ParidadCierre"
	,"ParidadCosto"
	,"MontoEquivalente"  
	,"Margen"  
	,"Utilidad"
	,"MontoDiferido"
	,"Modalidad"
	,"ModalidadLiquidacion"
	,"ModalidadLiquidacionValor"
	,"Plazo"
	,"PlazoResidual" 
	,"FechaVencimiento" 
	,"FechaEfectiva"
	,"Agente"  
	,"NombreAgente"
	,"OperacionFicticia"
	,"CodigoUsuario"
	,"Estado"
	,"Mesa"
	,"IdTipoCanalIngreso"
	,"INGCantidad"
	,"INGMontoMonedaPrincipal"
	,"INGMontoMonedaSecundaria"
	,"Accion"
	,0 AS "IdOperacionSpotReferencia"
	,'' AS "EstadoConfirmacion"
	,0 AS "FolioOrigenFW"
	,0 AS "IdOperacionSpotOrigen"
	,0 AS "IdOperacionSpotVigente"
	,"IdEstado"
	,"TipoVencimiento"
	--Fondos mutuos
	,0 AS "IdSolicitudAporteRescate"
	,'' AS  "EstadoAporteRescate" 
	,'' AS  "TipoAporteRescate"
	,'' AS  "FechaLiquidacionAporteRescate"
	,'0001-01-01' AS "FechaRespaldo"
	,"IdPortafolio"
	,0 AS "CodigoComercio"
	,"MontoCalce"
	,"MontoDisponible"
	,"Glosa"
	,'' AS "InstrumentoFM"
	,'' AS "FondosNacionalesValutaCero"
	,'NO' AS "AfectoADerivado"
	,'NO' AS "DolarObservado"
	,"FechaModificacionRegistro"
	,"IdUsuarioMod"
	,0 AS "IdAsignacion"
	,'OTC' AS "IdPlataformaNegociacion"
	,"IdMonedaCompensacion"
	,"NombreEmpleadoDigitador"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
UNION
SELECT
	"IdEmpresa"
	,0 AS "IdCartera"
	,"IdOperacion"
	,"IdOperacionUI"
	,"FolioOperacion"
	,"FechaOperacion"
	,"FechaLiquidacion"
	,"FechaEntregamos"
	,"FechaRecibimos"
	,"HoraOperacion"
	,"FechaOperacion" AS "FechaOperacionOPE"
	,"NombreInstrumento"
	,"Nemotecnico"
	,"IdTipoOperacion"
	,"TipoOperacion"
	,"IdTipoInstrumento"
	,"RutCliente"
	,"NombreCliente"
	,"CodigoPropositoPortafolio"
	,"IdMonedaPrincipal"
	,"IdMonedaSecundaria" 
	,"MontoMonedaPrincipal"
	,"MontoMonedaSecundaria"
	,"ResultadoMonedaLocalPuntas"
	,"ResultadoMonedaLocalBidAsk"
	,"ResultadoMonedaLocalDO"
	,"ResultadoMonedaLocalTCSpot"
	,"ResultadoMonedaLocalTCCierreMesa"
	,"TipoCambioCierre" 
	,"TipoCambioCosto" 
	,"ParidadCierre"
	,"ParidadCosto"
	,"MontoEquivalente"  
	,"Margen"  
	,"Utilidad"
	,"MontoDiferido"
	,"Modalidad"
	,"ModalidadLiquidacion"
	,"ModalidadLiquidacionValor"
	,"Plazo"
	,"PlazoResidual" 
	,"FechaVencimiento" 
	,"FechaEfectiva"
	,"Agente"  
	,"NombreAgente"
	,"OperacionFicticia"
	,"CodigoUsuario"
	,"Estado"
	,"Mesa"
	,"IdTipoCanalIngreso"
	,"MontoMonedaPrincipal"  AS "INGCantidad"
	,"MontoMonedaPrincipal"  AS "INGMontoMonedaPrincipal"
	,"MontoMonedaSecundaria" AS "INGMontoMonedaSecundaria"
	,"Accion"
	,"IdOperacionSpotReferencia"
	,"EstadoConfirmacion"
	,"FolioOrigenFW"
	,"IdOperacionSpotOrigen"
	,"IdOperacionSpotVigente"
	,"Estado" AS "IdEstado"
	,'' AS "TipoVencimiento"
	--Fondos mutuos
	, "IdSolicitudAporteRescate"
	, "EstadoAporteRescate" 
	, "TipoAporteRescate"
	, "FechaLiquidacionAporteRescate"
	, '0001-01-01' AS "FechaRespaldo"
	, "IdPortafolio"
	, "CodigoComercio"
	,0 AS "MontoCalce"
	,0 AS "MontoDisponible"
	,"Observacion" AS "Glosa"
	,"IdSerieFondoAporteRescate" AS "InstrumentoFM"
	,"FondosNacionalesValutaCero"
	, CASE WHEN ( COALESCE("AfectoADerivado", 'N') = 'N' ) THEN 'NO' ELSE 'SI' END AS "AfectoADerivado"
	, CASE WHEN ( COALESCE("DolarObservado", 'N') = 'N' ) THEN 'NO' ELSE 'SI' END AS "DolarObservado"
	,'0001-01-01' AS "FechaModificacionRegistro"
	,-1 AS "IdUsuarioMod"
	,"IdAsignacion"
	,COALESCE("IdPlataformaNegociacion",'') AS "IdPlataformaNegociacion"
	,'' AS "IdMonedaCompensacion"
	,"NombreEmpleadoDigitador"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot") AS t1
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf
	ON (conf."IdEmpresa" = t1."IdEmpresa" AND conf."CodigoParametro" = 'PLA.SegundosZonaHoraria')
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS US
	ON t1."IdUsuarioMod"=US."IdUsuario"
LEFT JOIN (
		SELECT
		DISTINCT
		"IdOperacion",
		STRING_AGG(CASE WHEN "RecibePaga" = 'P' THEN "NombreFormaDePago" END, ' / ') AS "FormaPagoEntregamos",
		STRING_AGG(CASE WHEN "RecibePaga" = 'R' THEN "NombreFormaDePago" END, ' / ') AS "FormaPagoRecibe"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		GROUP BY "IdOperacion") AS FDPSPOT ON (FDPSPOT."IdOperacion" = t1."IdOperacion" AND t1."Mesa" = 'MEME')
LEFT JOIN (
		SELECT
		DISTINCT
		"IdOperacion",
		STRING_AGG(CASE WHEN "RecibePaga" = 'P' THEN "NombreFormaDePago" END, ' / ') AS "FormaPagoEntregamos",
		STRING_AGG(CASE WHEN "RecibePaga" = 'R' THEN "NombreFormaDePago" END, ' / ') AS "FormaPagoRecibe"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		GROUP BY "IdOperacion") AS FDPFW ON (FDPFW."IdOperacion" = t1."IdCartera" AND t1."Mesa" = 'MEFW')) AS filtroOperaciones
WHERE filtroOperaciones."ROWNUMERS" = 1 OR 
(filtroOperaciones."ROWNUMERS" = 3 AND filtroOperaciones."Operaciones_Modificadas" = 1);
		
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MES.ConsultarMovimientosOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
