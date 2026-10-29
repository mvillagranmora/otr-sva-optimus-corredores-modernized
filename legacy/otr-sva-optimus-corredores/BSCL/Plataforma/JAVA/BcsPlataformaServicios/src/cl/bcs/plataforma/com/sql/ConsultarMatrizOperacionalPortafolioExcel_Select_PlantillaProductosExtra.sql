SELECT P."IdPortafolio", P."CodigoPortafolio", CLI."NombreCompleto", CLI."DocumentoIdentidad", C.*, 'S' as "EsExtra"  
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P 
	ON CLI."IdCliente" = P."IdCliente"
	AND P."IdEmpresa" = :IdEmpresa
JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" C 
	ON IFNULL(P."IdPerfilProductosExtra",'') = C."IdPerfilProductos"
	AND C."IdEmpresa" = :IdEmpresa
WHERE CLI."IdEmpresa" = :IdEmpresa
