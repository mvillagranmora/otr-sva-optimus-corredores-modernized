-- test fixture for ProcedimientoAlmacenadoTest (legacy call-text markers)
CALL "Prueba" (
    p_Texto => :p_Texto,
    p_Monto => :p_Monto,
    p_Nulo => :p_Nulo,
    p_out_Contador => @p_out_Contador,
    p_out_Detalle => $p_out_Detalle
)
