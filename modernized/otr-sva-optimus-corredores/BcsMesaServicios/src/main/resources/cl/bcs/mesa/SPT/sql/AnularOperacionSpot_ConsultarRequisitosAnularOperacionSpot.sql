with OperacionSpotTesoreria as (
	SELECT 
		INGEGR."IdOperacion",
		CASE 
			WHEN MIN(CASE WHEN INGEGR."IdEstado" IN ('ING', 'ANU') THEN 1 ELSE 0 END) = 1
			THEN 1
			ELSE 0
		END AS "PuedeAnularTesoreria",
		CASE 
			WHEN MIN(CASE WHEN INGEGR."IdEstado" IN ('ING', 'ANU') THEN 1 ELSE 0 END) = 1
			THEN 'OK'
			ELSE 'NO_ANULADO_TESORERIA'
		END AS "CodigoAnularTesoreria"
	FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" INGEGR
	INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT 
	    ON SPT."IdOperacionSpot" = INGEGR."IdOperacion"
	WHERE 
	    INGEGR."IdTipoOperacion" IN ('COMSPT', 'VENSPT')
	    AND SPT."OperacionFicticia" = 'S'
	GROUP BY INGEGR."IdOperacion"
) SELECT
	 SPT."IdOperacionSpot"	AS "NumeroOperacion"
	,SPT."IdOperacionSpot"
	,TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion"
	,SPT."IdMesa"
	,MES."IdEstadoMesa"
	,MLO."IdEstado"			AS "IdEstadoModulo"
	,MLO."TipoEstado"		AS "TipoEstadoModulo"
	,SPT."IdUsuario"
	,COALESCE(USUSUP."IdUsuario",0)				AS "IdUsuarioSupervisor"
	,COALESCE(SPTFAC."PuedeModificarAnular",1)	AS "PuedeModificarAnular"
	,COALESCE(SPTFAC."CodigoModificarAnular",'') AS "CodigoModificarAnular"
	,COALESCE(OTES."PuedeAnularTesoreria",1)	AS "PuedeAnularTesoreria"
	,COALESCE(OTES."CodigoAnularTesoreria",'') AS "CodigoAnularTesoreria"
	,COALESCE(USU2."IdUsuario",0)   AS "IdUsuarioReemplaza"
	,COALESCE(SolApoRes."IdTipoCanalIngreso",'') AS "IdTipoCanalIngresoSolApoRes"
	,SPT."IdTipoCanalIngreso"
	,SolApoRes."Estado"
	,COALESCE(EstadoSpot."Nombre",'') AS "NombreEstadoSpot"
   	,CASE 
		WHEN SolApoRes."TipoSolicitud" = 'APO' THEN 'APORTE'
       	WHEN SolApoRes."TipoSolicitud" = 'RES' THEN 'RESCATE'
       	ELSE '' 
   	END AS "TipoSolicitud"
    ,SolApoRes."IdSolicitudAporteRescate"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES
	ON (SPT."IdMesa" = MES."IdMesa" AND
		MES."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" MLO 
	ON (MLO."IdModulo" = 'MSP' AND
		MLO."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU
	ON (SPT."IdUsuario" = USU."IdUsuario" AND
		USU."TipoEntidad" = 'EMP')
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
	ON (USU."IdEntidad" = EMP."IdEmpleado")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUSUP
	ON (EMP."IdEmpleadoSupervisor" = USUSUP."IdEntidad" AND
		USUSUP."TipoEntidad" = 'EMP')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF
ON (CONF."CodigoParametro" = 'PLA.FechaSistema' AND CONF."IdEmpresa" = :IdEmpresa )
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJEREEM
	ON (EJEREEM."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
	AND CONF."Valor" BETWEEN EJEREEM."FechaInicioReemplazo" AND EJEREEM."FechaFinReemplazo")
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USU2
	ON (USU2."IdEntidad" = EJEREEM."IdEmpleadoReemplaza"
		AND USU2."IdUsuario"=:IdUsuario)
LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarSpotFacturado" SPTFAC
	ON (SPT."IdOperacionSpot" = SPTFAC."IdOperacionSpot" AND
		SPTFAC."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolApoRes 
	ON (SolApoRes."IdSolicitudAporteRescate" = SPT."IdSolicitudAporteRescate" 
		AND SolApoRes."IdEmpresa" = :IdEmpresa)
LEFT JOIN OperacionSpotTesoreria OTES ON OTES."IdOperacion" = spt."IdOperacionSpot"
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoSpot 
	ON (EstadoSpot."IdEstado" = SPT."Estado")
WHERE SPT."IdOperacionSpot" IN (:NumeroOperacion)
  AND SPT."IdEmpresa" = :IdEmpresa;