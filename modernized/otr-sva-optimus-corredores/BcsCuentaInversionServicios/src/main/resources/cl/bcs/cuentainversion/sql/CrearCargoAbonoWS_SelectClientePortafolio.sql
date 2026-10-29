SELECT 
	PORT."CodigoPortafolio",
	PORT."DocumentoIdentidad",
	PORT."IdPortafolio",
	PORT."NombreCompletoPortafolio",
	PORT."IdEstado",
	PORT."IdEstadoCliente",
	CASE WHEN CURRENT_UTCTIMESTAMP <= ADD_SECONDS(CLI."FechaCreacion", COALESCE(TO_BIGINT(OPECIN."Valor"),0)*3600) AND CLI."IdEstado" != 'ACT' THEN 'S' ELSE 'N' END AS "EsClienteNuevoProspecto"
FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS PORT
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON PORT."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS OPECIN ON OPECIN."CodigoParametro" = 'PLA.HorasOperaCINClienteNuevoBloqueado'
WHERE 	PORT."DocumentoIdentidad" IN (:DocumentoIdentidad)
AND 	PORT."CodigoPortafolio" IN (:CodigoPortafolio)
AND 	PORT."IdEmpresa" = :IdEmpresa;

