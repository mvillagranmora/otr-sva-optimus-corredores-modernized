SELECT 
	POR."IdPortafolio" AS "IdPortafolio"
	,POR."CodigoPortafolio" AS "CodigoPortafolio"
	,POR."IdPerfilComercial" AS "IdPerfilComercial"
	,POR."IdPerfilCobros" AS "IdPerfilCobros"
	,POR."IdPerfilProductos" AS "IdPerfilProductos"
	,POR."IdFormaCobro" AS "IdFormaCobro"
	,POR."IdCliente" AS "IdCliente"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarDatosPortafolio" AS POR 
WHERE POR."IdCliente" IN (:IdCliente);