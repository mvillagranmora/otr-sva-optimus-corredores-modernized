-- Obtiene desde tabla global las solicitudes de cambio a telefonos
SELECT
	"IdSolicitudCambio",
   	"CampoModificado",
   	"ValorAntiguo",
   	"ValorNuevo",
  	"IdEntidadOrigen",
  	"IdTelefono",
  	'' AS "IdTipoEntidad"
FROM "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaTelefonoMasivo_SolicitudCambio";


