SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1823.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1823.PreOrigen" (
	 "IdEmpresa"
	,"IdSolicitud"
	,"RutInstitucionReceptora"
	,"RutCliente"
	,"Monto"
	,"Spread"
	,"FechaCertificacion"
	,"Numero"
	,"SaldoAhorroNetoHasta31Dic2014MontoPositivo"
	,"SaldoAhorroNetoHasta31Dic2014MontoNegativo"
	,"MontoRetiroInversion57BIS"
	,"SaldoAhorroNetoDesde01Ene2015MontoPositivo"
	,"SaldoAhorroNetoDesde01Ene2015MontoNegativo"
	,"SaldoArrastreEjercicioDesde01Ene2015MontoPositivo"
	,"SaldoArrastreEjercicioDesde01Ene2015MontoNegativo"
	,"MontoInversionEjercicioDesde01Ene2015"
	,"MontoRetiroEjercicioDesde01Ene2015"
	,"MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo"
	,"MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo"
	,"MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo"
	,"MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo"
	,"NumeroCertificado"					
	,"IdCliente"
	,"IdPortafolio" 
) AS
SELECT 
    SOLICITUDES."IdEmpresa"					AS "IdEmpresa",
    SOLICITUDES."IdSolicitudAporteRescate"	AS "IdSolicitud",
    EMISOR."DocumentoIdentidad"				AS "RutInstitucionReceptora",
    SOLICITUDES."DocumentoIdentidad" 		AS "RutCliente",
    SOLICITUDES."Monto"						AS "Monto",
    SOLICITUDES."Spread"					AS "Spread",
    SOLiCITUDES."FechaCertificacion"		AS "FechaCertificacion",
    SOLICITUDES."IdSolicitudAporteRescate" 	AS "Numero",
    CASE WHEN SOLICITUDES."Spread" > 0 THEN SOLICITUDES."Spread"
    ELSE 0
    END	AS "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
    CASE WHEN SOLICITUDES."Spread" < 0 THEN SOLICITUDES."Spread"
    ELSE 0
    END AS "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
    SOLICITUDES."Monto"						AS "MontoRetiroInversion57BIS",
    0										AS "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
    0										AS "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
    0										AS "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
    0										AS "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
    0										AS "MontoInversionEjercicioDesde01Ene2015",
    0										AS "MontoRetiroEjercicioDesde01Ene2015",
    0										AS "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
    0										AS "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
    0										AS "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
    0										AS "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo",
    0										AS "NumeroCertificado",
    SOLICITUDES."IdCliente"					AS "IdCliente",
    SOLICITUDES."IdPortafolio" 				AS "IdPortafolio"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUDES
LEFT OUTER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CARTERAS ON (CARTERAS."EGRFolioComprobante" = SOLICITUDES."IdSolicitudAporteRescate" AND SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESREINV') 
																		AND CARTERAS."IdModulo" = 'FMM' AND CARTERAS."IdTipoProductoTIPOPE" = 'INFM' 
																		AND CARTERAS."IdClasificacionTipoInstrumento" = 'FM'
																		AND CARTERAS."EGRIdConcepto" = 'RESCFM'
																		AND CARTERAS."IdEstado" IN ('LIQ','MOD','RPA'))
LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUD_ORIGINAL ON (CARTERAS."FolioComprobante" = SOLICITUD_ORIGINAL."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = SOLICITUDES."IdEmisor")
WHERE SOLICITUDES."EstadoSolicitud" = 'CRT' AND SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('RESREINV','RESNORM') AND SOLICITUD_ORIGINAL."TipoCarteraNormativa" = '57BIS' AND SOLICITUD_ORIGINAL."FechaCertificacion" < '2014-12-31'
UNION ALL
SELECT 
    SOLICITUDES."IdEmpresa"					AS "IdEmpresa",
    SOLICITUDES."IdSolicitudAporteRescate"	AS "IdSolicitud",
    EMISOR."DocumentoIdentidad"				AS "RutInstitucionReceptora",
    SOLICITUDES."DocumentoIdentidad" 		AS "RutCliente",
    SOLICITUDES."Monto"						AS "Monto",
    SOLICITUDES."Spread"					AS "Spread",
    SOLiCITUDES."FechaCertificacion"		AS "FechaCertificacion",
    SOLICITUDES."IdSolicitudAporteRescate" 	AS "Numero",
    0										AS "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
    0										AS "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
    0										AS "MontoRetiroInversion57BIS",
    CASE WHEN SOLICITUDES."Spread" > 0 THEN SOLICITUDES."Spread"
    ELSE 0
    END										AS "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
    CASE WHEN SOLICITUDES."Spread" < 0 THEN SOLICITUDES."Spread"
    ELSE 0
    END										AS "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
    0										AS "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
    0										AS "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
    0										AS "MontoInversionEjercicioDesde01Ene2015",
    SOLICITUDES."Monto"						AS "MontoRetiroEjercicioDesde01Ene2015",
    0										AS "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
    0										AS "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
    0										AS "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
    0										AS "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo",
    0										AS "NumeroCertificado",
    SOLICITUDES."IdCliente"					AS "IdCliente",
    SOLICITUDES."IdPortafolio" 				AS "IdPortafolio"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUDES
LEFT OUTER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CARTERAS ON (CARTERAS."EGRFolioComprobante" = SOLICITUDES."IdSolicitudAporteRescate" AND SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESREINV') 
																		AND CARTERAS."IdModulo" = 'FMM' AND CARTERAS."IdTipoProductoTIPOPE" = 'INFM' 
																		AND CARTERAS."IdClasificacionTipoInstrumento" = 'FM'
																		AND CARTERAS."EGRIdConcepto" = 'RESCFM'
																		AND CARTERAS."IdEstado" IN ('LIQ','MOD','RPA'))
LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUD_ORIGINAL ON (CARTERAS."FolioComprobante" = SOLICITUD_ORIGINAL."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = SOLICITUDES."IdEmisor")
WHERE SOLICITUDES."EstadoSolicitud" = 'CRT' AND SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('RESREINV','RESNORM') AND SOLICITUD_ORIGINAL."TipoCarteraNormativa" = '57BIS' AND SOLICITUD_ORIGINAL."FechaCertificacion" >= '2015-01-01' AND SOLICITUD_ORIGINAL."FechaCertificacion" <= '2016-12-31'
UNION ALL
SELECT 
    SOLICITUDES."IdEmpresa"					AS "IdEmpresa",
    SOLICITUDES."IdSolicitudAporteRescate"	AS "IdSolicitud",
    EMISOR."DocumentoIdentidad"				AS "RutInstitucionReceptora",
    SOLICITUDES."DocumentoIdentidad" 		AS "RutCliente",
    SOLICITUDES."Monto"						AS "Monto",
    SOLICITUDES."Spread"					AS "Spread",
    SOLiCITUDES."FechaCertificacion"		AS "FechaCertificacion",
    SOLICITUDES."IdSolicitudAporteRescate" 	AS "Numero",
    0										AS "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
    0										AS "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
    0										AS "MontoRetiroInversion57BIS",
    0										AS "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
    0										AS "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
    0										AS "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
    0										AS "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
    SOLICITUDES."Monto"						AS "MontoInversionEjercicioDesde01Ene2015",
    0										AS "MontoRetiroEjercicioDesde01Ene2015",
    0										AS "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
    0										AS "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
    0										AS "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
    0										AS "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo",
    0										AS "NumeroCertificado",
    SOLICITUDES."IdCliente"					AS "IdCliente",
    SOLICITUDES."IdPortafolio" 				AS "IdPortafolio"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" SOLICITUDES
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = SOLICITUDES."IdEmisor")
WHERE SOLICITUDES."EstadoSolicitud" = 'CRT' AND SOLICITUDES."IdTipoMovimientoFondoMutuo" IN ('APONORM','APORESPRO','APOOVER') AND SOLICITUDES."TipoCarteraNormativa" = '57BIS'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1823.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;
