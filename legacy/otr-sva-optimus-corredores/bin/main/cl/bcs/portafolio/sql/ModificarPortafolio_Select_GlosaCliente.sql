SELECT
	CASE "Tipo" 
		WHEN 'J' THEN "DocumentoIdentidad" || ' / ' || "RazonSocial" || ' / CodPort:' || :CodigoPortafolio
		ELSE "DocumentoIdentidad" || ' / ' || "Nombres" || ' ' || "ApellidoPaterno" || ' ' || "ApellidoMaterno" || ' / CodPort:' || :CodigoPortafolio
	END AS "Glosa"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
WHERE "IdCliente" = :IdCliente
