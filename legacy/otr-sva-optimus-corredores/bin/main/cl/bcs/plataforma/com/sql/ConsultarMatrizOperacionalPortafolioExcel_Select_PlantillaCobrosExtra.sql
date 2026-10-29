SELECT P."IdPortafolio", P."CodigoPortafolio", CLI."NombreCompleto", CLI."DocumentoIdentidad", PCOB.*, 'S' as "EsExtra" 
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P 
	ON CLI."IdCliente" = P."IdCliente"
	AND P."IdEmpresa" = :IdEmpresa
JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB 
	ON PCOB."IdPerfilCobros" = IFNULL(P."IdPerfilCobrosExtra",'')
	AND PCOB."IdEmpresa" = :IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB2
	ON PCOB."IdPerfilCobros" = PCOB2."IdPerfilCobros"
	AND PCOB."IdBolsa" = PCOB2."IdBolsa"  
	AND PCOB."IdTipoProducto" = PCOB2."IdTipoProducto"
	AND PCOB."IdTipoOperacion" = PCOB2."IdTipoOperacion"
	AND PCOB."IdCanalIngreso" = PCOB2."IdCanalIngreso"
	AND PCOB."CodigoCobroClasificacion" = PCOB2."CodigoCobroClasificacion"
	AND PCOB."Prioridad" > PCOB2."Prioridad"
WHERE PCOB2."Prioridad" IS NULL
	AND CLI."IdEmpresa" = :IdEmpresa
	AND PCOB."EstadoPerfilCobros" = 'ACT'
	AND PCOB."EstadoDetalle" = 'ACT'
