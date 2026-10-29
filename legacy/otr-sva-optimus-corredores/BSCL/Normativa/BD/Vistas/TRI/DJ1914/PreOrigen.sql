SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1914.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1914.PreOrigen" (
     "IdEmpresa"
	,"IdSolicitud"
	,"FechaCertificacion"
	,"CostoUF"
	,"ValorUFFechaInversion"
	,"MontoHistorico"
	,"Spread"
	,"CantidadInversion"							
	,"MontoMonedaOrigenInversion"				
	,"IdMonedaInversion"							
	,"ParidadFechaInversion"						
	,"FechaParidadInversion"						
	,"FechaCostoUF"								
	,"CantidadRescate"							
	,"MontoMonedaOrigenRescate"					
	,"IdMonedaRescate"							
	,"ParidadFechaRescate"						
	,"FechaParidadRescate"	
	,"IdCartera"
	,"FolioCartera"
	,"Numero"	
	,"RutCliente"
	,"TipoInstrumento"					
	,"NumeroIdentificadorInversion"	
	,"FechaInversionOriginal"					
	,"InversionReinversion54BISMontoNominal"							
	,"InversionReinversion54BISInteresPositivo"						
	,"InversionReinversion54BISInteresNegativo"					
	,"RetiroInversion54BISMontoNominal"				
	,"RetiroInversion54BISInteresPositivo"						
	,"RetiroInversion54BISInteresNegativo"				
	,"NumeroCertificado"					
	,"IdCliente"
	,"IdPortafolio"
) AS
SELECT 
    SOLICITUDES."IdEmpresa"					AS "IdEmpresa",
    SOLICITUDES."IdSolicitudAporteRescate"	AS "IdSolicitud",
    CARTERAS."INGFechaOperacion"			AS "FechaCertificacion",
    SOLICITUDES."CostoUF"					AS "CostoUF",
    SOLICITUDES."ValorUFFechaInversion"		AS "ValorUFFechaInversion",
    0 										AS "MontoHistorico",
    0										AS "Spread",
    CARTERAS."INGCantidad" 					AS "CantidadInversion",
    CARTERAS."MontoMonedaPrincipal" 		AS "MontoMonedaOrigenInversion",
    CARTERAS."IdMonedaPrincipal"			AS "IdMonedaInversion",
    SOLICITUDES."ParidadFechaInversion"		AS "ParidadFechaInversion",
    SOLICITUDES."FechaAceptacion"			AS "FechaParidadInversion",
    '0001-01-01'							AS "FechaCostoUF",
    0										AS "CantidadRescate",					
    0										AS "MontoMonedaOrigenRescate",
    ''										AS "IdMonedaRescate",						
	0										AS "ParidadFechaRescate",						
	'0001-01-01'							AS "FechaParidadRescate",
    COALESCE(CARTERAS."IdCartera",0)		AS "IdCartera",
    COALESCE(CARTERAS."FolioCartera",0)		AS "FolioCartera",
    SOLICITUDES."IdSolicitudAporteRescate" 	AS "Numero",
    SOLICITUDES."DocumentoIdentidad" 		AS "RutCliente",
    2										AS "TipoInstrumento",
    CARTERAS."Nemotecnico"					AS "NumeroIdentificadorInversion",
    CARTERAS."INGFechaOperacion"			AS "FechaInversionOriginal",
    CASE WHEN CARTERAS."IdMonedaPrincipal" = 'CLP' THEN CARTERAS."MontoMonedaPrincipal" 
    	 ELSE 0
    END 									AS "InversionReinversion54BISMontoNominal",
    0										AS "InversionReinversion54BISInteresPositivo",
    0										AS "InversionReinversion54BISInteresNegativo",
    0										AS "RetiroInversion54BISMontoNominal",
    0										AS "RetiroInversion54BISInteresPositivo",
    0										AS "RetiroInversion54BISInteresNegativo",
    0										AS "NumeroCertificado",
    CARTERAS."IdCliente"					AS "IdCliente",
    CARTERAS."IdPortafolio" 				AS "IdPortafolio"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CARTERAS 
INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" AS SOLICITUDES ON (CARTERAS."FolioComprobante" = SOLICITUDES."IdSolicitudAporteRescate")
WHERE CARTERAS."IdEstado" = 'VIG' AND CARTERAS."IdModulo" = 'FMM' AND CARTERAS."IdTipoProductoTIPOPE" = 'INFM' 
AND CARTERAS."IdClasificacionTipoInstrumento" = 'FM' AND CARTERAS."IdConcepto" = 'APORFM' AND SOLICITUDES."TipoCarteraNormativa" = '54BIS'
AND SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('APONORM','APORESPRO','APOOVER','APOREINV')
UNION ALL
SELECT
	SOLICITUDES."IdEmpresa"					AS "IdEmpresa",
	SOLICITUDES."IdSolicitudAporteRescate"	AS "IdSolicitud",
    CARTERAS."EGRFechaOperacion"			AS "FechaCertificacion",
    SOLICITUDES."CostoUF"					AS "CostoUF",
    SOLICITUDES."ValorUFFechaInversion"		AS "ValorUFFechaInversion",
    ROUND(SOLICITUDES."ValorUFFechaInversion" * SOLICITUDES."CostoUF",0)	AS "MontoHistorico",
    SOLICITUDES."Spread"					AS "Spread",
    0										AS "CantidadInversion",
    0 										AS "MontoMonedaOrigenInversion",
    ''										AS "IdMonedaInversion",
    0										AS "ParidadFechaInversion",
    '0001-01-01'							AS "FechaParidadInversion",
    SOLICITUDES."FechaCertificacion"		AS "FechaCostoUF",
    CARTERAS."EGRCantidad"					AS "CantidadRescate",					
    SOLICITUDES."Monto" 					AS "MontoMonedaOrigenRescate", --CAMBIAR POR "CARTERAS"."EGRMONTO"
    SOLICITUDES."IdMoneda"					AS "IdMonedaRescate",						
	SOLICITUDES."ParidadFechaInversion"		AS "ParidadFechaRescate",						
	SOLICITUDES."FechaCertificacion"		AS "FechaParidadRescate",
    COALESCE(CARTERAS."IdCartera",0)		AS "IdCartera",
    COALESCE(CARTERAS."FolioCartera",0)		AS "FolioCartera",
    SOLICITUDES."IdSolicitudAporteRescate" 	AS "Numero",
    SOLICITUDES."DocumentoIdentidad" 		AS "RutCliente",
    2										AS "TipoInstrumento",
    CARTERAS."Nemotecnico"					AS "NumeroIdentificadorInversion",
    CARTERAS."INGFechaOperacion"			AS "FechaInversionOriginal",
    0 										AS "InversionReinversion54BISMontoNominal",
    0										AS "InversionReinversion54BISInteresPositivo",
    0										AS "InversionReinversion54BISInteresNegativo",
    CASE WHEN CARTERAS."IdMonedaPrincipal" = 'CLP' THEN SOLICITUDES."Monto"   
    	 ELSE 0
    END										AS "RetiroInversion54BISMontoNominal", --CAMBIAR POR "CARTERAS"."EGRMONTO"
    0										AS "RetiroInversion54BISInteresPositivo",
    0										AS "RetiroInversion54BISInteresNegativo",
    0										AS "NumeroCertificado",
    SOLICITUDES."IdCliente"					AS "IdCliente",
    SOLICITUDES."IdPortafolio" 				AS "IdPortafolio"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUDES
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CARTERAS ON (CARTERAS."EGRFolioComprobante" = SOLICITUDES."IdSolicitudAporteRescate" )
INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUD_ORIGINAL ON (CARTERAS."FolioComprobante" = SOLICITUD_ORIGINAL."IdSolicitudAporteRescate")
WHERE SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('RESREINV','RESNORM') AND CARTERAS."IdModulo" = 'FMM' AND CARTERAS."IdTipoProductoTIPOPE" = 'INFM' 
AND CARTERAS."IdClasificacionTipoInstrumento" = 'FM' AND CARTERAS."EGRIdConcepto" = 'RESCFM' AND CARTERAS."IdEstado" IN ('LIQ','MOD') AND SOLICITUD_ORIGINAL."TipoCarteraNormativa" = '54BIS'
AND YEAR(CARTERAS."INGFechaOperacion") != YEAR(SOLICITUDES."FechaCertificacion")
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1914.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;
