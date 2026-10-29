CALL "BSCL.Facturacion.CrystalReports.Servicios::ConsultarGananciaCapital"
(
 	 p_IdEmpresa  =>:IdEmpresa,
	 p_IdUsuario  =>:IdUsuario,
	 p_IdModulo   =>:IdModulo,
	 p_FechaConsulta =>:FechaConsulta,  
	 p_Datos  =>#GENERARINFORMEIGK_DATOSDEFILTRO,
	 p_Salida=>$p_Salida
)