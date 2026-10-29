SELECT
	"IdCliente",
	"DocumentoIdentidad"

FROM 
	"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
WHERE 
	"DocumentoIdentidad" IN (:DocumentoIdentidad)
GROUP BY 	
	"IdCliente",
	"DocumentoIdentidad";
