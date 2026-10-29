CALL "BSCL.Normativa.BD.Servicios::NOR.REPO3P1.EliminarDetalles" (
	p_IdEmpresa  =>:IdEmpresa,
    p_IdUsuario  => :IdUsuario,
    p_IdModulo 	 => :IdModulo,
	p_List   => #ELIMINARREPORTE_ID,
	p_out_AdvertenciasContador     =>$p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle      =>$p_out_AdvertenciasDetalle,
    p_out_ErroresContador          =>$p_out_ErroresContador,
    p_out_ErroresDetalle           =>$p_out_ErroresDetalle
)
