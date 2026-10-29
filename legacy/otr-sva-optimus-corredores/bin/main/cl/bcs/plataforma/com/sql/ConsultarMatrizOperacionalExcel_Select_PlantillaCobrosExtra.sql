SELECT '' as "DescripcionPerfilComercialBase", PCOB.* 
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB2
	ON PCOB."IdPerfilCobros" = PCOB2."IdPerfilCobros"
	AND PCOB."IdBolsa" = PCOB2."IdBolsa"  
	AND PCOB."IdTipoProducto" = PCOB2."IdTipoProducto"
	AND PCOB."IdTipoOperacion" = PCOB2."IdTipoOperacion"
	AND PCOB."IdCanalIngreso" = PCOB2."IdCanalIngreso"
	AND PCOB."CodigoCobroClasificacion" = PCOB2."CodigoCobroClasificacion"
	AND PCOB."Prioridad" > PCOB2."Prioridad"
WHERE PCOB2."Prioridad" IS NULL
	AND PCOB."IdPerfilCobros" = ''
	AND PCOB."IdEmpresa" = :IdEmpresa
	AND PCOB."EstadoPerfilCobros" = 'ACT'
	AND PCOB."EstadoDetalle" = 'ACT'
