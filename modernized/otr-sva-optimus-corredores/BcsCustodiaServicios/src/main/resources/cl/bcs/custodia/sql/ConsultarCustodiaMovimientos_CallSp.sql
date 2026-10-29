CALL "BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos" (
	
	--VARIABLES DE ENTRADA OBLIGATORIAS
    p_IdEmpresa=> :p_IdEmpresa,
    p_IdUsuario=> :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
	
    --VARIABLES DEL PROCESO
    p_FechaInicio => :p_FechaInicio,
	p_FechaTermino => :p_FechaTermino,
	p_VariablesProceso => #p_VariablesProceso,
	p_IdProductoAgrupador => :p_IdProductoAgrupador,
	
    --VARIABLES DE SALIDA OBLIGATORIAS
    p_out_Tabla => $p_out_Tabla
)