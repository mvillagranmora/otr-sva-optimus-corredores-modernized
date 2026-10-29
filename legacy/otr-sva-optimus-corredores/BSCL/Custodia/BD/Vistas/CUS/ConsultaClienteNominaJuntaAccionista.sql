SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaClienteNominaJuntaAccionista";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaClienteNominaJuntaAccionista" 
AS
SELECT 
	  NOMINA."IdClienteNominaJuntaAccionista" 	   AS "IdClienteNominaJuntaAccionista"
	, NOMINA."IdCliente" 				   	 	   AS "IdCliente"
	, NOMINA."IdEventoJuntaAccionista" 		       AS "IdEventoJuntaAccionista"
	, NOMINA."IdEstadoNotificacion"				   AS "IdEstadoNotificacion"
	, ESTADO_NOTIFICACION."Nombre"				   AS "NombreEstadoNotificacion"
	, NOMINA."IdEstadoRespuesta"				   AS "IdEstadoRespuesta"
	, ESTADO_RESPUESTA."Nombre"					   AS "NombreEstadoRespuesta"
	, NOMINA."FechaIngreso"					   	   AS "FechaIngreso"
	, NOMINA."FechaIngresoRegistro"				   AS "FechaIngresoRegistro"
	, CLIENTE."NombreCompleto" 					   AS "NombreCliente"
	, DIRECCION_FISICA."NombreLargo"		       AS "Direccion"
	, DIRECCION_ELECTRONICA."Direccion" 		   AS "Correo"
	, USUARIO."IdUsuario" 						   AS "IdUsuario"
	, USUARIO."CodigoUsuario"					   AS "NombreUsuario"
	, CLIENTE."IdDireccionElectronicaPrincipal"    AS "IdDireccionElectronicaPrincipal"
	, CLIENTE."IdDireccionFisicaPrincipal"		   AS "IdDireccionFisicaPrincipal"
	, CLIENTE."IdPersona"						   AS "IdPersona"
	, ConsultarJuntaAccionistas."Lugar"            AS "Lugar"
	, CIUDAD."NombreCiudad"                        AS "CiudadCliente"
	, ConsultarJuntaAccionistas."FechaRealizacion" AS "FechaRealizacion"
	, ConsultarJuntaAccionistas."HoraRealizacion"  AS "HoraRealizacion"
	, ConsultarJuntaAccionistas."FechaCierre"      AS "FechaCierre"
	, ConsultarJuntaAccionistas."Materias"         AS "Materias"
FROM "BSCL.Custodia.BD::CUS.ClienteNominaJuntaAccionista" AS NOMINA
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = NOMINA."IdCliente")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USUARIO ON (USUARIO."IdUsuario" = NOMINA."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ESTADO_NOTIFICACION ON (ESTADO_NOTIFICACION."IdEstado" = NOMINA."IdEstadoNotificacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ESTADO_RESPUESTA ON (ESTADO_RESPUESTA."IdEstado" = NOMINA."IdEstadoRespuesta")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIRECCION_ELECTRONICA ON (DIRECCION_ELECTRONICA."IdDireccionElectronica" = CLIENTE."IdDireccionElectronicaPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = CLIENTE."IdDireccionFisicaPrincipal")
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD ON (CIUDAD."IdCiudad" = DIRECCION_FISICA."IdCiudad")
LEFT OUTER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarJuntaAccionistas" AS ConsultarJuntaAccionistas ON (ConsultarJuntaAccionistas."IdEventoJuntaAccionista" = NOMINA."IdEventoJuntaAccionista");
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaClienteNominaJuntaAccionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
