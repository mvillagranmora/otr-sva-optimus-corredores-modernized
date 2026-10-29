SELECT P."IdPortafolio", P."CodigoPortafolio", CLI."NombreCompleto", CLI."DocumentoIdentidad", C.*, 'N' as "EsExtra"  
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P 
	ON CLI."IdCliente" = P."IdCliente" 
JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" C 
	ON C."IdPerfilProductos" = P."IdPerfilProductos"
WHERE CLI."IdEmpresa" = :IdEmpresa
