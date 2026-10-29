-- Call text of the HANA stored procedure BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes.
-- LEGACY DEFECT kept (see README TES-A-266): every parameter is bound to the wrong input name (:p_IdEmpresa, :p_IdUsuario, :p_IdModulo, :p_SimularOperacion),
-- none of which the legacy supplies, so the procedure receives NULLs.
call "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes" (
    p_FechaMovimiento                        => :p_IdEmpresa,
    p_IdCaja                                 => :p_IdUsuario,
    p_GenerarComprobanteDesdeMovimientos     => :p_IdModulo,
    p_GenerarComprobanteDesdeComprobantes    => :p_SimularOperacion,
    p_ValidaComprobanteGenerado              => :p_SimularOperacion)
