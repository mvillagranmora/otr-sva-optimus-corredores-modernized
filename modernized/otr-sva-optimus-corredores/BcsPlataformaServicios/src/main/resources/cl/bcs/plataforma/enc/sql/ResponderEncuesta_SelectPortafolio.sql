SELECT 
	 "IdPortafolio",
	 "IdPerfilProductos" 
FROM  "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"  
WHERE "IdCliente"= :IdCliente;
