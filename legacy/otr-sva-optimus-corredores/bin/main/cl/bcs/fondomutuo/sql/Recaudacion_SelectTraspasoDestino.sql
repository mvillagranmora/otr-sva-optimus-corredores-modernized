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
    ,DESTINO."IdDetalleTraspaso"
	,DESTINO."IdTraspaso"
	,DESTINO."IdSerieFondo"
	,DESTINO."Porcentaje"
	,DESTINO."IdEstado"
    ,DESTINO."FechaIngreso"
    ,DESTINO."FechaModificacion"
    ,DESTINO."FechaIngresoRegistro"
    ,DESTINO."FechaModificacionRegistro"
    ,DESTINO."IdRegimenFondoMutuo"
    ,FONDO."IdMoneda" AS "IdMonedaFondo"
    ,CASE WHEN COALESCE(TRASPASO."FolioMLT",0) <> 0 THEN 'S' ELSE 'N' END AS "IndicadorMLT"
FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" AS TRASPASO
INNER JOIN  "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" AS DESTINO			ON  ( TRASPASO."IdTraspaso" 	=  DESTINO."IdTraspaso")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIEFONDO ON (SERIEFONDO."IdSerieFondo" = DESTINO."IdSerieFondo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON ( FONDO."IdFondo" = SERIEFONDO."IdFondo")
WHERE TRASPASO."IdEmpresa"=:IdEmpresa AND TRASPASO."IdTraspaso" IN (:IdTraspaso) AND TRASPASO."IdEstado"='ING'
AND DESTINO."IdEstado"='ACT'
ORDER BY TRASPASO."IdTraspaso",DESTINO."IdDetalleTraspaso";

