-- Obtiene desde tabla global las solicitudes de cambio a las cuentas bancarias
SELECT
	"IdSolicitudCambio",
   	"CampoModificado",
   	"ValorAntiguo",
   	"ValorNuevo",
  	"IdEntidadOrigen",
  	"IdCuentaBancaria",
  	'' AS "IdTipoEntidad"
FROM "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaCuentaBancariaMasivo_SolicitudCambio";

