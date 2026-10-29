SELECT
	TO_NVARCHAR("IdDetalleIndicador") AS "IdDetalleIndicador"
	,"DetalleIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador"
WHERE TO_NVARCHAR("IdDetalleIndicador") in (:IdsDetalleIndicador);