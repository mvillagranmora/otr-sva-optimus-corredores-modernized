SELECT "IdEmpresa", 
"IdPerfilCobros", 
"IdTipoProducto", 
"IdTipoOperacion", 
"IdTipoCanalIngreso", 
"IdBolsa", 
"CodigoDefinicionCobro", 
"IdEstadoAutorizacion", 
"IdUsuarioCreador",
"Borrado",
"IdUsuarioBorra" 
FROM "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro"
WHERE "IdPerfilCobros" = :IdPerfilCobros;