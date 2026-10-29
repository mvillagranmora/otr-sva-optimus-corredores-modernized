SELECT
	 POR."IdCliente"
	,POR."IdPortafolio"
--FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR
FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
WHERE POR."IdCliente" IN (:IdCliente);