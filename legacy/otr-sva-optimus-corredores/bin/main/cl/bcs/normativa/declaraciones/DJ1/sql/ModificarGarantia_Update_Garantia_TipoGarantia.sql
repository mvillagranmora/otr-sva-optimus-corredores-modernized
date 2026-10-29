-- Actualiza el campo S6_TipoGar
UPDATE  GAR
SET
	GAR."S6_TipoGar" = RC."ValorNuevo",
	GAR."FecUltMod" = TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
	GAR."UsrUltMod" =  USU."CodigoUsuario"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" GAR
INNER JOIN "BSCL.Normativa.BD::GBL.ModificarGarantia_RegistroCambio" RC 
ON (
RC."RutContraparte" = GAR."KRutCtraparte1"
AND RC."IdContrato" = GAR."KIdContrato"
AND RC."FechaContrato" = GAR."KFechaContrato"
AND RC."IdOperacion" = GAR."KIdOpeEstruc"
AND RC."Producto" = GAR."kProducto"
AND RC."PeriodoComercial" = GAR."kPeriodoComercial"
AND RC."IdGarantia" = GAR."KS6_IdGar"
)
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON USU."IdUsuario" = RC."IdUsuario"
WHERE RC."CampoModificado" = :CampoModificado; -- S6_TipoGar

