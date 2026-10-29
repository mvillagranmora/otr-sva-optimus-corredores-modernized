SET SCHEMA BSCL;
DROP TABLE #solicitudes;
CREATE LOCAL TEMPORARY TABLE #solicitudes(
											"IdInterfazAgf"	BIGINT,
											"IdEstado"		NVARCHAR(3),
											"Observacion"	NVARCHAR(500)
										);
call "BSCL.FondoMutuo.BD.Servicios::ConsultarSolicitudesParaAGF" (
	p_IdEmpresa	 => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FMM',
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

CALL "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAgf"(
	p_IdEmpresa				   => 1,
	p_SolicitudesAGF 		   => #solicitudes,
	 p_out_Resultado		   => ?,
	 p_out_AdvertenciasContador 				=>?,
	 p_out_AdvertenciasDetalle 	=> ?,
	 p_out_ErroresContador 		=> ?,
	 p_out_ErroresDetalle 		=> ?
);

--DROP TABLE #RESULTADO_CAMBIARESTADOSOLICITUDAPORTERESCATEAGFMASIVO;