set schema bscl;
DROP table #filtros;
create local temporary table #filtros(
													"IdPortafolio"			BIGINT,
													"IdMoneda" 	 			NVARCHAR(3)
												);
INSERT INTO #filtros VALUES(512348,'USD');

select * from #filtros;

call "BSCL.Plataforma.BD.Servicios::ConsultarFlujoCaja" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'CTA',
	p_UsaReglasNegocio => 'S',
	p_Uso => 'INGRESO_APORTE_FMM',
	p_FechaDesde => '2016-07-25',
	p_FechaHasta => '2016-07-25',
	p_PortafolioAConsultar => #filtros,
	p_out_FlujoCaja => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

