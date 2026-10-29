CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate"(
	p_IdEmpresa							=> :p_IdEmpresa,
	p_IdPais							=> :p_IdPais,
	p_SolicitudAporteRescate			=> #p_SolicitudAporteRescate,

	p_FechasAporteRescate				=> $p_FechasAporteRescate
);