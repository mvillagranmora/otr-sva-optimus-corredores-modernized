/********************************************************** 
Proposito	: Script para Calcular Exigibilidad
autor		: EME
fecha		: 22/02/2018
***********************************************************/
CALL "BSCL.Plataforma.BD.Servicios::CalcularExigibilidad" (
	    p_IdEmpresa							=> :p_IdEmpresa,
	    p_IdUsuario							=> :p_IdUsuario,
	    p_IdModulo							=> :p_IdModulo,
	    p_simularOperacion					=> :p_simularOperacion,
		p_UsaReglasNegocio					=> :p_UsaReglasNegocio,
		
	    p_TableExigibilidadSimulacion		=> #p_TableExigibilidadSimulacion,	    
	    
	    p_IdTipoInstrumento					=> :p_IdTipoInstrumento,
	    p_IdPortafolio						=> :p_IdPortafolio,
	    p_IdBolsa							=> :p_IdBolsa,
	    p_IdInstrumento						=> :p_IdInstrumento,
	    
		p_out_TableExigibilidadSimulacion	=> $p_out_TableExigibilidadSimulacion,
		p_out_TableExigibilidadPortafolio	=> $p_out_TableExigibilidadPortafolio,
		p_out_AdvertenciasContador			=> @p_out_AdvertenciasContador,
		p_out_AdvertenciasDetalle			=> $p_out_AdvertenciasDetalle,
		p_out_ErroresContador				=> @p_out_ErroresContador,
		p_out_ErroresDetalle				=> $p_out_ErroresDetalle
		);
		
	