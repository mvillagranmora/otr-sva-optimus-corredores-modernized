CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
	p_IdTipoLog 					=> :IdTipoLog 
	,p_IdModulo 					=> :IdModulo 
	,p_IdUsuario 					=> :IdUsuario 
	,p_IdEmpresa 					=> :IdEmpresa 
	,p_FechaSistemaLog 				=> TO_VARCHAR (:FechaHoraSistema, 'YYYY-MM-DD')
	,p_IdUsuarioAtendido 			=> :IdUsuario
	,p_IdCliente 					=> :IdCliente
	,p_NombreCliente 				=> :NombreCliente
	,p_IdDocumentoIdentidad 		=> :DocumentoIdentidad
	,p_IdTipoDocumentoIdentidad 	=> :TipoDocumentoIdentidad
	,p_IdTipoRegistro 				=> :TipoEntidad--'PORTAFOLIO'
	,p_DetalleLog 					=> "BSCL.Portafolio.BD::GBL.CrearPortafolio_AgregarDetalleLog"	-- hacia arriba obligatorios
	,p_NombreProceso 				=> :NombreProceso-- hacia abajo opcionales
	,p_IdRegistro 					=> :p_out_RefId
	,p_EstadoProceso 				=> :IdEstadoAutorizacion
);