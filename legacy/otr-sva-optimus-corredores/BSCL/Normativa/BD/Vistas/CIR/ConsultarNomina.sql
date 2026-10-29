
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" AS

SELECT
   NOM."IdNomina",
   NOM."IdDocumento",
   NOM."IdCliente",
   NOM."IdPortafolio",
   NOM."CodigoPortafolio",
   NOM."IdPersona",
   --Datos del Envio (Nomina)
   CASE 
   WHEN (SELECT "Valor" 
   		FROM "BSCL.Normativa.BD::NOR.Configuracion"
   		WHERE "CodigoParametro" = 'VerCartaConsolidada') = 'S' and DIRE_CIRC_CONS."Direccion" is not null THEN DIRE_CIRC_CONS."Direccion"
   ELSE DIRE."Direccion" END AS "DireccionElectronica",
   CASE LENGTH(COALESCE(NOM."DireccionFisica",'')) WHEN 0 THEN COALESCE(DIRF."NombreLargo",'') ELSE NOM."DireccionFisica" END AS "DireccionFisica" ,
   NOM."FormaEnvio",
   NOM."Notificar",
   NOM."IdEstadoNotificacion",
   COALESCE(ESTN."Nombre",'') 			  AS "EstadoNotificacion",
   NOM."IdEstadoRespuesta",
   COALESCE(ESTR."Nombre",'') 	  		  AS "EstadoRespuesta",
   NOM."Motivo",
   NOM."FechaIngreso",
   NOM."FechaCreacion",
   NOM."FechaModificacion",
   NOM."IdUsuarioCreador",
   DOC."AnoMes" AS "Periodo",
   --Datos Descripcion
   cli."DocumentoIdentidad",
   cli."NombreCompleto" AS "NombreCliente",
   COALESCE(USU."CodigoUsuario",'') AS "CodigoUsuarioCreador",
   (CASE WHEN COALESCE(CER."IdPortafolio",0) > 0 THEN 'SI' ELSE 'NO' END ) AS "PDFMasivo"
FROM "BSCL.Normativa.BD::CIR.Nomina"  AS NOM
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" ESTR
	ON(NOM."IdEstadoRespuesta"=ESTR."IdEstado" AND ESTR."NombreEntidad"='CIRCULARIZACION-RESPUESTA')
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" ESTN
	ON(NOM."IdEstadoNotificacion"=ESTN."IdEstado" AND ESTN."NombreEntidad"='CIRCULARIZACION-NOTIFICACION')
	INNER JOIN "BSCL.Normativa.BD::DOC.Documento" AS DOC
	ON (NOM."IdDocumento"=DOC."IdDocumento")
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
    ON (NOM."IdCliente"=POR."IdCliente" AND NOM."IdPortafolio"=POR."IdPortafolio")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIRF
	ON (POR."IdDireccionEnvioCorrespondencia"=DIRF."IdDireccionFisica") 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
	ON (CLI."IdCliente"=NOM."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
	ON (PER."IdPersona"=NOM."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DIRE
	ON (PER."IdDireccionElectronicaPrincipal"=DIRE."IdDireccionElectronica")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DIRE_CIRC_CONS
	ON (PER."IdPersona"=DIRE_CIRC_CONS."IdPersona" AND DIRE_CIRC_CONS."CodigoProposito"='CIRC')
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU
	ON (NOM."IdUsuarioCreador"=USU."IdUsuario")
	LEFT JOIN "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente" CER
    ON (NOM."IdCliente"=CER."IdCliente" AND NOM."IdPortafolio"=CER."IdPortafolio" AND DOC."AnoMes"=CER."Periodo" AND CER."IdTipoDocumento"='CIRCCarta');
	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 

