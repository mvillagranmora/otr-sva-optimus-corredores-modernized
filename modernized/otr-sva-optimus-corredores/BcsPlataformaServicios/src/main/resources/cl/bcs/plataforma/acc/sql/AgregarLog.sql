SELECT CURRENT_DATE into p_sysDate from DUMMY
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'ACTUALIZACION' 
		,p_IdModulo => 'PLA' 
		,p_IdUsuario => :p_IdUsuario 
		,p_IdEmpresa => :p_IdEmpresa 
		,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'USUARIO'
		,p_DetalleLog => :p_DetalleLog
		);