SELECT 
	 "CodigoPortafolio"
	,"IdPortafolio"
	,"DocumentoIdentidad"

	,"IdEstado" AS "IdEstadoPortafolio"
FROM  "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
WHERE "DocumentoIdentidad" IN (:DocumentoIdentidad)
  AND "CodigoPortafolio"   IN (:CodigoPortafolio)
