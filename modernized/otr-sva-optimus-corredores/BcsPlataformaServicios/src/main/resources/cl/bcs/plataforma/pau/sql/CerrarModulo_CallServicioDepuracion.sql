CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(
	IdEmpresa				=> :p_IdEmpresa,
	Fecha 					=> CURRENT_UTCTIMESTAMP,
	IdUsuario				=> :p_IdUsuario,
	Servicio				=> :p_Servicio,
	Bandera					=> :p_Bandera,
	Detalle					=> :p_Detalle
);