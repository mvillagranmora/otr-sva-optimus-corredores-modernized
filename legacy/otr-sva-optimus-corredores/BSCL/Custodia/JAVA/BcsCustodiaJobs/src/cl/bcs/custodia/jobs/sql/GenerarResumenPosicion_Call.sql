CALL "BSCL.Custodia.BD.Servicios::ConsultarTarjetaExistenciaResumen"(
    p_IdEmpresa => :p_IdEmpresa,
    p_IdUsuario => :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
    
    p_IdInstrumento => :p_IdInstrumento,
    p_IdConcepto => :p_IdConcepto,
    p_FechaConsultaDesde => :p_FechaConsultaDesde,
    p_FechaConsultaHasta => :p_FechaConsultaHasta,
    
    p_out_TarjetaExistenciaResumen => $p_out_TarjetaExistenciaResumen,
    p_out_ResumenPosicionesRV => $p_out_ResumenPosicionesRV
);