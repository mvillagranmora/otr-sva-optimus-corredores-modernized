UPDATE "BSCL.Plataforma.BD::COM.ContratoComision" 
SET
	"Nombre" 							= :Nombre,
	"Descripcion" 						= :Descripcion,
	"IdTipoContrato" 					= :IdTipoContrato,
	"CodigoArea" 						= :CodigoArea,
	"IdFormatoCobro" 					= :IdFormatoCobro,
	"IdClasificacionTipoInstrumento" 	= :IdClasificacionTipoInstrumento,
	"IdMoneda" 							= :IdMoneda,
	"IdSegmentoCliente" 				= :IdSegmentoCliente,
	"IdPortafolio"						= :IdPortafolio,
	"IdCliente" 						= :IdCliente,
	"IdEmpresa"							= :IdEmpresa,
	"IdEstado"							= :IdEstado
WHERE "IdContratoComision" = :IdContratoComision;