SELECT DISTINCT Mesa."IdCliente"
FROM "BSCL.Mesa.BD::MES.Mesa" AS Mesa 
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocNeg
 	ON SocNeg."IdCliente" =  Mesa."IdCliente"
WHERE Mesa."IdTipoProducto" = 'MERF'
  AND Mesa."IdEmpresa" = :IdEmpresa;   