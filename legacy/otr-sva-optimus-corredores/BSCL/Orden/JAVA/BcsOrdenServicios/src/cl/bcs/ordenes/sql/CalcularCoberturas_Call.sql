/********************************************************** 
Proposito	: Script para Calcular exigibilidad
autor		: EME
fecha		: 22/02/2018
***********************************************************/
CALL "BSCL.Plataforma.BD.Servicios::CalcularCobertura"(
	    	p_IdEmpresa						=> :p_IdEmpresa,
		    p_IdUsuario						=> :p_IdUsuario,
		    p_IdModulo						=> :p_IdModulo,
			p_UsaReglasNegocio				=> :p_UsaReglasNegocio,
			p_IdPortafolio					=> :p_IdPortafolio,
			p_IdEntidadDestinoGarantia		=> :p_IdEntidadDestinoGarantia,
			p_IdTipoInstrumentoExigeGarantia=> :p_IdTipoInstrumentoExigeGarantia,
		    p_IdInstrumentoExigeGarantia	=> :p_IdInstrumentoExigeGarantia,
			p_Simulacion					=> #p_SimulacionCobertura,
			
		    p_out_TableCoberturaSimulacion		=> $p_out_TableCoberturaSimulacion,
			p_out_TableCoberturaPortafolio		=> $p_out_TableCoberturaPortafolio,
		    p_out_AdvertenciasContador			=> @p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle			=> $p_out_AdvertenciasDetalle,
			p_out_ErroresContador				=> @p_out_ErroresContador,
			p_out_ErroresDetalle				=> $p_out_ErroresDetalle
);