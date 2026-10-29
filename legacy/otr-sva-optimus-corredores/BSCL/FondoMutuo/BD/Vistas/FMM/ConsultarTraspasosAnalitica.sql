SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica"  
AS

	SELECT 'APV' AS "TipoTraspaso",t."IdTraspaso",c."DocumentoIdentidad",t."IdCliente",r."IdPortafolio","TipoMovimiento" AS "IdTipoMovimientoTraspaso",
	TO_NVARCHAR(CASE WHEN t."TipoMovimiento" = 'INGRESO' THEN t."FechaIngreso" ELSE t."FechaEgreso" END,'YYYY-MM-DD') AS "FechaIngresoSolicitud",
	o."IdSolicitudAporteRescate","NombreEntidad" AS "NombreEntidadRecaudadora","DocumentoEntidad" AS "DocumentoIdentidadEntidadRecaudadora", 0 AS "FolioCSC"
	FROM "BSCL.APV.BD::TRA.Traspaso" t
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" c ON t."IdCliente" = c."IdCliente"
	LEFT JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" o ON t."IdTraspaso" = o."IdTraspaso"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" r ON r."IdSolicitudAporteRescate" = o."IdSolicitudAporteRescate"
	 
	UNION ALL 
	 
	--INGRESO
	SELECT 'FFMM' AS "TipoTraspaso",t."IdTraspaso",c."DocumentoIdentidad",t."IdCliente",t."IdPortafolio",
	CASE "IdTipoMovimientoTraspaso" WHEN 'TRAEGR' THEN 'EGRESO' WHEN 'TRAING' THEN 'INGRESO' ELSE '' END AS "IdTipoMovimientoTraspaso",
	TO_NVARCHAR(t."FechaIngreso",'YYYY-MM-DD') AS "FechaIngresoSolicitud",
	CASE "IdTipoMovimientoTraspaso" WHEN 'TRAEGR' THEN ORIGEN."IdSolicitudAporteRescate" ELSE DESTINO."IdSolicitudAporteRescate" END AS "IdSolicitudAporteRescate",
	ENTIDAD."Nombre" AS "NombreEntidadRecaudadora",ENTIDAD."DocumentoIdentidad" AS "DocumentoIdentidadEntidadRecaudadora", t."FolioCSC" AS "FolioCSC"
	FROM "BSCL.FondoMutuo.BD::TRA.Traspaso"  t
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" c ON t."IdCliente" = c."IdCliente"
	LEFT JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"        AS ENTIDAD ON  ENTIDAD."IdEntidadRecaudadora" =  t."IdEntidadRecaudadora"
	LEFT JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" ORIGEN ON ORIGEN."IdTraspaso" = t."IdTraspaso"
	LEFT JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" DESTINO ON DESTINO."IdTraspaso" = t."IdTraspaso"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" movorigen ON movorigen."IdSolicitudAporteRescate" = ORIGEN."IdSolicitudAporteRescate"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" movdestino ON movdestino."IdSolicitudAporteRescate" = DESTINO."IdSolicitudAporteRescate";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 