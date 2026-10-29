SELECT
	TO_NVARCHAR("IdSucursal") AS "IdSucursal"
	,"NombreSucursal"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal"
WHERE TO_NVARCHAR("IdSucursal") in (:IdsSucursal);