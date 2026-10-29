SELECT 
	"DocumentoIdentidad",
	"IdCliente", 
	"CodigoPropositoPortafolio", 
	"IdPortafolio" 
	FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 
	WHERE "IdCliente" IN (:IdCliente) AND "IdPortafolio" IN (:IdPortafolio);
