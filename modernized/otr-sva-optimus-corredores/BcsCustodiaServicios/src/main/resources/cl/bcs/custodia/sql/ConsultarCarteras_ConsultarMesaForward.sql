-- Consultamos si el portafolio consultado corresponde a la mesa forward para incluir las carteras forward
SELECT
	"IdMesa",
	"IdCliente",	
	"DocumentoIdentidad",
	"IdPortafolio",
	"CodigoPortafolio"		
FROM "BSCL.Mesa.BD.Vistas::ConsultarMantenedorMesas"
WHERE 	"IdEmpresa" 		= :IdEmpresa
AND 	"IdPortafolio" 		= :IdPortafolio
AND 	"IdTipoProducto" 	= 'MEFW';  -- Mesa Forward