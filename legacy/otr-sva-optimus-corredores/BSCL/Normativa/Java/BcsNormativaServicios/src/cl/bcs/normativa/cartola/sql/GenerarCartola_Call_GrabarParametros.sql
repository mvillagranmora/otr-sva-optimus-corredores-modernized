/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 01/09/2017
***********************************************************/


CALL "BSCL.Normativa.BD.Servicios::CAR.GrabarParametro"(
	p_IdEmpresa					=>:p_IdEmpresa,
	p_IdUsuario					=>:p_IdUsuario,
	p_IdModulo					=>:p_IdModulo,
	p_IdParametro				=>:p_IdParametro,
	p_IdNomina					=>:p_IdNomina,		
	p_Nombre					=>:p_Nombre,
	p_Valor						=>:p_Valor,
	p_out_id					=>@p_out_id,
	p_out_AdvertenciasContador	=>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=>$p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=>@p_out_ErroresContador,
	p_out_ErroresDetalle		=>$p_out_ErroresDetalle
);