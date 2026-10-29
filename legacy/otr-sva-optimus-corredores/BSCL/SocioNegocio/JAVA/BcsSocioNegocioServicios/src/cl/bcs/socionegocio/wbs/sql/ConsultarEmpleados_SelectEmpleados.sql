SELECT 
	EMP."IdEmpleado",
	EMP."CodigoEmpleado",
	EMP."DocumentoIdentidad",
	CASE WHEN COALESCE(EJE."IdEmpleadoEjecutivo",0) != 0 THEN 'S' ELSE 'N' END AS "EsEjecutivo",
	EMP."NombreCompleto" AS "NombreEmpleado",
	COALESCE(EMP."IdEstado", '') AS "IdEstadoEmpleado",
	COALESCE(EMP."NombreEstado", '')  AS "EstadoEmpleado",
	COALESCE(EJE."IdEstado", '') AS "IdEstadoEjecutivo",
	COALESCE(EJE."NombreEstado", '')  AS "EstadoEjecutivo",
	COALESCE(EJE."NombreSupervisor",'') AS "NombreSupervisor",
	COALESCE(SUP."CodigoEmpleado",'') AS "CodigoSupervisor",
	COALESCE(EJE."NombreCompletoReemplaza",'') AS "NombreEjecutivoReemplazo",
	TO_NVARCHAR(COALESCE(EJE."FechaInicioReemplazo",'0001-01-01'), 'YYYY-MM-DD') AS  "FechaInicioDeReemplazo",
	TO_NVARCHAR(COALESCE(EJE."FechaFinReemplazo",'0001-01-01'), 'YYYY-MM-DD') AS  "FechaFinDeReemplazo"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS EJE ON EMP."IdEmpleado" = EJE."IdEmpleadoEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS SUP ON EJE."IdEmpleadoSupervisor" = SUP."IdEmpleado"
WHERE (COALESCE(:documentosIdentidad, '') = '' OR EMP."DocumentoIdentidad" IN (:documentosIdentidad)) AND
(COALESCE(:idsEstadoEmpleado, '') = '' OR EMP."IdEstado" IN (:idsEstadoEmpleado)) AND
(COALESCE(:idsEstadoEjecutivo, '') = '' OR EJE."IdEstado" IN (:idsEstadoEjecutivo)) AND
(COALESCE(:codigosEmpleado, '') = '' OR EMP."CodigoEmpleado" IN (:codigosEmpleado)) AND
(COALESCE(:codigosSupervisor, '') = '' OR SUP."CodigoEmpleado" IN (:codigosSupervisor)) AND
(COALESCE(:esEjecutivo, '') = '' OR (CASE WHEN COALESCE(EJE."IdEmpleadoEjecutivo", 0) != 0 THEN 'S' ELSE 'N' END) IN (:esEjecutivo));
