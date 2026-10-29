SELECT 
	 TRASPASO."IdTraspaso"
    ,TRASPASO."IdEmpresa"
    ,TRASPASO."IdModulo"
	,TRASPASO."IdUsuario"
    ,TRASPASO."IdCliente"
    ,TRASPASO."IdPortafolio"
 	,TRASPASO."IdEmisor"
	,TRASPASO."IdMotivoTraspaso"
	,TRASPASO."IdTipoMovimientoTraspaso"
    ,TRASPASO."IdTipoTraspaso"
    ,TRASPASO."Glosa"
    ,TO_NVARCHAR(TRASPASO."FechaHoraSuscripcion",'YYYY-MM-DD HH24:MI:SS') AS "FechaHoraSuscripcion"
    ,TRASPASO."IdDivisionPais"
    ,TRASPASO."FolioTraspaso"
    ,TRASPASO."IdEstado"
    ,TRASPASO."Monto" 
    ,TRASPASO."FechaIngreso"
    ,TRASPASO."FechaAceptacion"
    ,TRASPASO."FechaAnulacion"
    ,TRASPASO."FechaModificacion"
    ,TRASPASO."FechaIngresoRegistro"
    ,TRASPASO."FechaAceptacionRegistro" 
    ,TRASPASO."FechaAnulacionRegistro" 
    ,TRASPASO."FechaModificacionRegistro"
 	,ORIGEN."IdDetalleTraspaso"
	,ORIGEN."IdTraspaso"
	,ORIGEN."IdSerieFondo"
	,ORIGEN."Cantidad"
	,ORIGEN."IdEstado"
	,ORIGEN."FechaIngreso"
    ,ORIGEN."FechaModificacion"
    ,ORIGEN."FechaIngresoRegistro"
    ,ORIGEN."FechaModificacionRegistro"
    ,ORIGEN."IdRegimenTributario" AS "IdRegimenFondoMutuo"
    ,FONDO."IdMoneda"
FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" AS TRASPASO
INNER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" 		AS ORIGEN			ON  ( TRASPASO."IdTraspaso" 	=  ORIGEN."IdTraspaso")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIEFONDO ON (SERIEFONDO."IdSerieFondo" = ORIGEN."IdSerieFondo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON ( FONDO."IdFondo" = SERIEFONDO."IdFondo")
WHERE TRASPASO."IdEmpresa"=:IdEmpresa AND TRASPASO."IdTraspaso" IN (:IdTraspaso) AND TRASPASO."IdEstado"='ING'
AND ORIGEN."IdEstado"='ACT'
ORDER BY TRASPASO."IdTraspaso",ORIGEN."IdDetalleTraspaso";