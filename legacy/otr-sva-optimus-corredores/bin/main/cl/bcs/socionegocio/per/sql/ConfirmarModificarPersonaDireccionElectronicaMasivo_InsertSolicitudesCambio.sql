
-- Inserta en tabla global las solicitudes de cambio a las direcciones electronicas
INSERT INTO "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaDireccionElectronicaMasivo_SolicitudCambio"
(
	"IdSolicitudCambio",
   	"CampoModificado",
   	"ValorAntiguo",
   	"ValorNuevo",
  	"IdEntidadOrigen",
  	"IdTipoEntidad",
  	"IdDireccionElectronica"
)
SELECT
   SOL."IdSolicitudCambio",
   SOL."CampoModificado",   
   SOL."ValorAntiguo",   
   SOL."ValorNuevo",
   SOL."IdEntidadOrigen",
   :IdTipoEntidad,
   CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdDireccionElectronica"
                
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOL
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaDireccionElectronicaMasivo_DireccionElectronica" AS DELE ON  CAST(DELE."IdDireccionElectronica" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
WHERE SOL."IdTipoEntidad" 		    = :IdTipoEntidad
AND   SOL."IdEstadoAutorizacion"	= 'PEN';

