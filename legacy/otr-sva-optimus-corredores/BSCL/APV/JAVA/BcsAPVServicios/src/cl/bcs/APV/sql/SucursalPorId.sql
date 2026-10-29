SELECT 
	 "IdSucursal"
	,"NombreSucursal"
	,"IdBanco"
	,"IdEstado"
FROM "BSCL.Tesoreria.BD::TES.SucursalesBancarias"
WHERE "IdSucursal" = :p_IdSucursal;