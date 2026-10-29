SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCambioEjecutivoCliente"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCambioEjecutivoCliente" AS
SELECT DISTINCT--quitar cuando se saque referencia de IdPersonaEmpleado
	CAB."IdCliente", 
	CAST(CAB."FechaRegistro" AS SECONDDATE) AS "FechaRegistro",
	--CAB."FechaRegistro",--quita los milisegundos para el distinct.
	CAB."CodigoUsuario",
	DET."ValorAntiguo",
	DET."ValorNuevo",
	DET."CampoRegistro",
	EJEANT."NombreCompleto" AS "NombreCompletoAntiguo",
	EJEANT."CodigoEmpleado" AS "CodigoEmpleadoAntiguo",
	EJENUE."NombreCompleto" AS "NombreCompletoNuevo",
	EJENUE."CodigoEmpleado" AS "CodigoEmpleadoNuevo"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" CAB  
	JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle" DET 
		ON (DET."SubRegistro"='CLIENTE' AND  CAB."IdLog"=DET."IdLog" AND
		(DET."CampoRegistro"='IDPERSONAEJECUTIVO' OR DET."CampoRegistro"='IdPersonaEjecutivo'))
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJEANT 
		ON (cast(EJEANT."IdPersonaEjecutivo" AS NVARCHAR)=DET."ValorAntiguo")
	JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJENUE 
		ON (cast(EJENUE."IdPersonaEjecutivo" AS NVARCHAR)=DET."ValorNuevo")
WHERE CAB."NombreModulo"='SOCIONEGOCIO' AND CAB."IdTipoLog"='MODIFICACION';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCambioEjecutivoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 