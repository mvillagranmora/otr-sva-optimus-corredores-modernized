CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
(      p_IdEmpresa                  => :IdEmpresa,
       p_IdUsuario                  => :IdUsuario,
       p_IdModulo                   => 'PLA',
       p_IdAgenda                   => 0,
       p_IdTipoTarea                => :IdTipoTarea,
       p_Segundos                   => 0,
       p_TablaParametros            => #tabla_Parametros,
       p_CreaParam                  => 'S',
       
       p_out_IdPrograma             => ?,
       p_out_AdvertenciasContador   => ?,
       p_out_AdvertenciasDetalle 	=> ?,
       p_out_ErroresContador        => ?,
       p_out_ErroresDetalle         => ?         
)
			