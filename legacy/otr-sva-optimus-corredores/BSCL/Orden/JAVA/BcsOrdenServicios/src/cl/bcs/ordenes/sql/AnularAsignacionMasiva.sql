/********************************************************** 
Proposito	: Script para anular asignaciones
autor		: RU
fecha		: 01/02/2017
***********************************************************/
CALL "BSCL.Orden.BD.Servicios::AnularAsignacionMasivaOptimizada"
(	p_IdEmpresa  				=> :p_IdEmpresa ,
	p_IdUsuario  				=> :p_IdUsuario ,
	p_IdModulo  				=> :p_IdModulo ,
	p_out_OrdenesAnuladas 		=> $p_out_OrdenesAnuladas,
	p_out_SalidaDetalle 		=> $p_out_SalidaDetalle ,
	p_out_InformacionContador 	=> @p_out_InformacionContador,
	p_out_InformacionDetalle 	=> $p_out_InformacionDetalle,
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);
