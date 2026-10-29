-- Inserta en tabla global las solicitudes de cambio a las cuentas bancarias
INSERT INTO "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaCuentaBancariaMasivo_SolicitudCambio"
(
	"IdSolicitudCambio",
   	"CampoModificado",
   	"ValorAntiguo",
   	"ValorNuevo",
  	"IdEntidadOrigen",
  	"IdTipoEntidad",
  	"IdCuentaBancaria"  
)
SELECT
   SOL."IdSolicitudCambio",
   SOL."CampoModificado",   
   SOL."ValorAntiguo",   
   SOL."ValorNuevo",
   SOL."IdEntidadOrigen",
   :IdTipoEntidad,
   CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdCuentaBancaria"
                
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOL
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaCuentaBancariaMasivo_CuentaBancaria" AS CBAN ON  CAST(CBAN."IdCuentaBancaria" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
WHERE SOL."IdTipoEntidad" 		    = :IdTipoEntidad
AND   SOL."IdEstadoAutorizacion"	= 'PEN';

