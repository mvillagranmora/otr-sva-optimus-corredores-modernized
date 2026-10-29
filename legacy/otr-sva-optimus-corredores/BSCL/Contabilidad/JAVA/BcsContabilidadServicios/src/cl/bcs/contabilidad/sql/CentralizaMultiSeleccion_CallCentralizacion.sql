CALL ":SP_CENTRALIZACION"
(
	p_IdEmpresa						=> :p_IdEmpresa
	,p_IdUsuario					=> :p_IdUsuario
	,p_IdModulo						=> :p_IdModulo
	,p_SimulaOperacion				=> :p_SimulaOperacion
	,p_IdTipoCentralizacion			=> :p_IdTipoCentralizacion
	,p_FechaContable				=> :p_FechaContable
	,p_Ejercicio					=> :p_Ejercicio
	,p_out_Resultado				=> $p_out_Resultado
);