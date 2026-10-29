
-- Inserta en tabla global las solicitudes de cambio a telefonos
INSERT INTO "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaTelefonoMasivo_SolicitudCambio"
(
	"IdSolicitudCambio",
   	"CampoModificado",
   	"ValorAntiguo",
   	"ValorNuevo",
  	"IdEntidadOrigen",
  	"IdTipoEntidad",  	
  	"IdTelefono"  
)
SELECT
   SOL."IdSolicitudCambio",
   SOL."CampoModificado",   
   SOL."ValorAntiguo",   
   SOL."ValorNuevo",
   SOL."IdEntidadOrigen",
   :IdTipoEntidad,
   CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdTelefono"
                
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOL
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaTelefonoMasivo_Telefono" AS CBAN ON  CAST(CBAN."IdTelefono" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
WHERE SOL."IdTipoEntidad" 		    = :IdTipoEntidad
AND   SOL."IdEstadoAutorizacion"	= 'PEN';


