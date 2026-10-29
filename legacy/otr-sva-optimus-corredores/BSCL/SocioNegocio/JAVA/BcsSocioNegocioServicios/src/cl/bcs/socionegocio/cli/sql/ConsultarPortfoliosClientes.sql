
SELECT         
    "IdCliente"			AS "IdCliente"  ,  
	"IdPortafolio"		AS "IdPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 
WHERE "IdCliente" = :p_IdCliente;
