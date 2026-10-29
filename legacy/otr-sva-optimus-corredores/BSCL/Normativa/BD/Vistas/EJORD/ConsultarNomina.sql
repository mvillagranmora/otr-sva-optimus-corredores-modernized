
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina" AS

SELECT
   NOM."IdEmpresa",
   NOM."IdEjecucion",
   NOM."IdDocumento",
   NOM."FechaEjecucion",
   NOM."IdCliente",
   NOM."IdPortafolio",
   POR."CodigoPortafolio",

   CLI."IdPersona",
   --Datos del Envio (Nomina)
   PER."IdDireccionElectronicaPrincipal",
   COALESCE(DIRE."Direccion",'') AS "DireccionElectronica",
   COALESCE(DIRF."NombreLargo",'') AS "DireccionFisica",
   NOM."IdEstadoNotificacion",
   COALESCE(ESTN."Nombre",'') 			  AS "EstadoNotificacion",
   NOM."IdEstadoCarta",
   COALESCE(ESTR."Nombre",'') 	  		  AS "EstadoCarta",
   NOM."FechaIngreso",
   NOM."FechaCreacion",
   NOM."IdUsuarioCreador",
   --DOC."AnoMes" AS "Periodo",
   --Datos Descripcion
   cli."NombreCompleto" AS "NombreCliente",
   COALESCE(USU."CodigoUsuario",'') AS "CodigoUsuarioCreador"	
FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes"  AS NOM
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" ESTR
	ON(NOM."IdEstadoCarta"=ESTR."IdEstado" AND ESTR."NombreEntidad"='EJECUCIONORDENES-CARTA')
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" ESTN
	ON(NOM."IdEstadoNotificacion"=ESTN."IdEstado" AND ESTN."NombreEntidad"='EJECUCIONORDENES-NOTIFICACION')
	--INNER JOIN "BSCL.Normativa.BD::DOC.Documento" AS DOC
	--ON (NOM."IdDocumento"=DOC."IdDocumento")
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
    ON (NOM."IdCliente"=POR."IdCliente" AND NOM."IdPortafolio"=POR."IdPortafolio")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIRF
	ON (POR."IdDireccionEnvioCorrespondencia"=DIRF."IdDireccionFisica") 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
	ON (CLI."IdCliente"=NOM."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
	ON (PER."IdPersona"=CLI."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DIRE
	ON (PER."IdDireccionElectronicaPrincipal"=DIRE."IdDireccionElectronica")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU
	ON (NOM."IdUsuarioCreador"=USU."IdUsuario")	;
	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 


