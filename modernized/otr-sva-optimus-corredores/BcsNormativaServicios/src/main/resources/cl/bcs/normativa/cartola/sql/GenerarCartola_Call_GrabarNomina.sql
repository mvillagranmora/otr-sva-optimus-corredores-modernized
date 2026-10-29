/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 01/09/2017
***********************************************************/


CALL "BSCL.Normativa.BD.Servicios::CAR.GrabarNomina"(
	p_IdEmpresa					=>:p_IdEmpresa,
	p_IdUsuario					=>:p_IdUsuario,
	p_IdModulo					=>:p_IdModulo,
	p_IdNomina					=>:p_IdNomina,			
	p_Periodo					=>:p_Periodo,
	p_FechaDesde				=>:p_FechaDesde,
	p_FechaHasta				=>:p_FechaHasta,
	p_Comentario				=>:p_Comentario,
	p_Certificada				=>:p_Certificada,
	p_FechaCertificacion		=>:p_FechaCertificacion,		
	p_IdUsuarioCertificador		=>:p_IdUsuarioCertificador,
	p_NombreUsuarioCertificador	=>:p_NombreUsuarioCertificador,
	p_NotaCertificacion			=>:p_NotaCertificacion,
	p_TipoProceso				=>:p_TipoProceso,
	p_EstadoProceso				=>:p_EstadoProceso,
	p_SegundosEsperaProceso		=>:p_SegundosEsperaProceso,
	p_MensajeProceso			=>:p_MensajeProceso,	
	p_out_id					=>@p_out_id,
	p_out_AdvertenciasContador	=>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=>$p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=>@p_out_ErroresContador,
	p_out_ErroresDetalle		=>$p_out_ErroresDetalle
);