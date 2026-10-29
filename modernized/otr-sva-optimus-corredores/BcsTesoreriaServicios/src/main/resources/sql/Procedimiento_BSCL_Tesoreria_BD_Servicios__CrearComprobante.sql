-- Call text of the HANA stored procedure BSCL.Tesoreria.BD.Servicios::CrearComprobante (legacy markers, see ProcedimientoAlmacenado)
call "BSCL.Tesoreria.BD.Servicios::CrearComprobante" (
    p_IdEmpresa                              => :p_IdEmpresa,
    p_IdUsuario                              => :p_IdUsuario,
    p_IdModulo                               => :p_IdModulo,
    p_SimularOperacion                       => :p_SimularOperacion,
    p_UsaReglasNegocio                       => :p_UsaReglasNegocio,
    p_FechaMovimiento                        => :p_FechaMovimiento,
    p_IdCaja                                 => :p_IdCaja,
    p_GenerarComprobanteDesdeMovimientos     => :p_GenerarComprobanteDesdeMovimientos,
    p_GenerarComprobanteDesdeComprobantes    => :p_GenerarComprobanteDesdeComprobantes,
    p_EsIngresoAutomatico                    => :p_EsIngresoAutomatico,
    p_out_InformacionContador                => @p_out_InformacionContador,
    p_out_AdvertenciasContador               => @p_out_AdvertenciasContador,
    p_out_ErroresContador                    => @p_out_ErroresContador)
