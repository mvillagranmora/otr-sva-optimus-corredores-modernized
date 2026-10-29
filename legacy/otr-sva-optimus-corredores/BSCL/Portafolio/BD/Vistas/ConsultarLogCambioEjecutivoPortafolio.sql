SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Portafolio.BD.Vistas::POR.ConsultarLogCambioEjecutivoPortafolio"; 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::POR.ConsultarLogCambioEjecutivoPortafolio" AS
select distinct
	CAB."IdPortafolio", 
	CAST(CAB."FechaRegistro" AS SECONDDATE) as "FechaRegistro",
	--CAB."FechaRegistro",--quita los milisegundos para el distinct.
	CAB."CodigoUsuario",
	DET."ValorAntiguo",
	DET."ValorNuevo",
	EJEANT."NombreCompleto" as "NombreCompletoAntiguo",
	EJEANT."CodigoEmpleado" as "CodigoEmpleadoAntiguo",
	EJENUE."NombreCompleto" as "NombreCompletoNuevo",
	EJENUE."CodigoEmpleado" as "CodigoEmpleadoNuevo"
from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" CAB  
	join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle" DET 
		on (DET."SubRegistro"='PORTAFOLIO' and  CAB."IdLog"=DET."IdLog" and
		(DET."CampoRegistro"='IDEMPLEADOEJECUTIVO' OR DET."CampoRegistro"='IdEmpleadoEjecutivo'))
	left join "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJEANT 
		on (cast(EJEANT."IdEmpleadoEjecutivo" as NVARCHAR)=DET."ValorAntiguo")
	join "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJENUE 
		on (cast(EJENUE."IdEmpleadoEjecutivo" as NVARCHAR)=DET."ValorNuevo")
where CAB."NombreModulo"='SOCIONEGOCIO' and CAB."IdTipoLog"='MODIFICACION';
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::POR.ConsultarLogCambioEjecutivoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;