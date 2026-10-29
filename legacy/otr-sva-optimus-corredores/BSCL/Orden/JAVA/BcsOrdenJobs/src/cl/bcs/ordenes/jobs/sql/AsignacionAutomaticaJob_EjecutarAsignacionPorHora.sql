/********************************************************** 
Proposito	: Script para ejecutar asignaciones con hora
autor		: RU
fecha		: 01/02/2017
***********************************************************/
CALL "BSCL.Orden.BD.Servicios::EjecutarAsignacionPorHora"
(	p_IdEmpresa  						=> :p_IdEmpresa,
	p_IdUsuario  						=> :p_IdUsuario,
	p_IdModulo  						=> :p_IdModulo ,
	p_simularOperacion					=> :p_simularOperacion,
	p_UsaReglasNegocio	 				=> :p_UsaReglasNegocio,
	p_HoraHasta 						=> :p_HoraHasta,
	p_IdTipoAsignacion 					=> :p_IdTipoAsignacion ,
	p_IdLibroOrdenes 					=> :p_IdLibroOrdenes,
	p_IdClasificacionTipoInstrumento 	=> :p_IdClasificacionTipoInstrumento,
	p_IdTipoInstrumento 				=> :p_IdTipoInstrumento,
	p_IdAccionOrden 					=> :p_IdAccionOrden,
	p_IdBolsa 							=> :p_IdBolsa,
	p_out_Transacciones 				=> $p_out_Transacciones,
	p_out_Ordenes	 					=> $p_out_Ordenes,
	p_out_Resultado 					=> $p_out_Resultado,
	p_out_cobros 						=> $p_out_cobros,
	p_out_InformacionContador 			=> @p_out_InformacionContador,
	p_out_InformacionDetalle 			=> $p_out_InformacionDetalle,
	p_out_AdvertenciasContador 			=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 			=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 				=> @p_out_ErroresContador,
	p_out_ErroresDetalle 				=> $p_out_ErroresDetalle
	
);
