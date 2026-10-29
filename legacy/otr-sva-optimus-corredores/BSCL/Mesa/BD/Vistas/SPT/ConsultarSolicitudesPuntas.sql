SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarSolicitudesPuntas";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarSolicitudesPuntas" 
AS
SELECT  
	SP."IdSolicitudPuntas",
	SP."FechaRegistro",
	SP."FechaVigencia",	
	SP."TipoCambioCostoSolicitado",
	SP."TipoCambioCostoAprobado",
	SP."IdOperacion"					AS "FolioOperacion",
	SP."IdTipoOperacion" 				AS "IdTipoOperacion",
	TIPOOPERACION."Nombre" 				AS "TipoOperacion",
	SP."IdCliente" 						AS "IdCliente",
	CLI."NombreCompleto"				AS "NombreCompleto",
	CLI."NombreCompletoPortafolio"		AS "NombreCompletoPortafolio",
	SP."IdUsuarioSolicitud",
	UsuarioSolicitud."CodigoUsuario" 	AS "UsuarioSolicitud",
	SP."IdUsuarioAutorizador",
	UsuarioAutorizador."CodigoUsuario" 	AS "UsuarioAutorizador",
	SP."MontoMonedaPrincipal"			AS "Monto",		
	SP."IdMonedaPrincipal" 				AS "IdMoneda",	
	SP."IdEstado"						AS "IdEstadoOperacion",
	EST."Nombre"						AS "NombreEstado",
	SP."ObservacionSolicitud",
	SP."ObservacionAutorizador"
	
FROM "BSCL.Mesa.BD::SPT.SolicitudesPuntas" AS SP	
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CLI  ON (CLI."IdCliente" =  SP."IdCliente" AND CLI."IdPortafolio" = SP."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION ON TIPOOPERACION."IdTipoOperacion" = SP."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS UsuarioSolicitud ON SP."IdUsuarioSolicitud" = UsuarioSolicitud."IdUsuario" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS UsuarioAutorizador ON SP."IdUsuarioAutorizador" = UsuarioAutorizador."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON SP."IdEstado" = EST."IdEstado";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarSolicitudesPuntas" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
