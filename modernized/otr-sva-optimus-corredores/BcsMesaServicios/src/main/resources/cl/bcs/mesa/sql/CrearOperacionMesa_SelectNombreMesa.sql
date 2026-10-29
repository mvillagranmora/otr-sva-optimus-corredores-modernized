SELECT
	MES."IdCliente",
	MES."IdMesa",
	MES."Nombre" ,
	MES."IdPortafolio"
	
FROM "BSCL.Mesa.BD::MES.Mesa" AS MES 
	WHERE MES."IdMesa" = :IdMesa 
	AND MES."IdEstadoRegistro" = 'ACT' 
	AND MES."IdTipoProducto" = 'MERF';