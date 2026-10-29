SELECT 
	MES."IdMesa"
	, MES."IdEstadoMesa" AS "IdEstado"
	, MES."CodigoModulo"
	, M."NombreModulo"
FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" AS M ON M."CodigoModulo" = MES."CodigoModulo" 
WHERE MES."CodigoModulo" IN (:p_IdsModulo) AND MES."IdEmpresa" = :p_IdEmpresa AND MES."IdEstadoMesa" = 'A';