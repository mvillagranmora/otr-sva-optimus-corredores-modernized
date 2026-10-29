-- Obtiene desde tabla global las solicitudes de cambio a las direcciones electronicas
SELECT
	"IdSolicitudCambio",
   	"CampoModificado",
   	"ValorAntiguo",
   	"ValorNuevo",
  	"IdEntidadOrigen",
  	"IdDireccionElectronica",
  	'' AS "IdTipoEntidad"
FROM "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaDireccionElectronicaMasivo_SolicitudCambio";
