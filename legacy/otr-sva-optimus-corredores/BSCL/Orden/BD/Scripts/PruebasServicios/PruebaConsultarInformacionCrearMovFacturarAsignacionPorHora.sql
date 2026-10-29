SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdInstrumento" BIGINT, "Cantidad" DECIMAL(26,8), "PrecioMercado" DECIMAL(26,8),"Monto" DECIMAL(26,8)
);

delete from #TT;
insert into #TT select 122,0,0,1201 from dummy;
insert into #TT select 32,0,0,1201 from dummy;

CALL "BSCL"."BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_UsaReglasNegocio				=> 'N',
	p_IdPortafolio					=> 343710,
	p_IdBolsa						=> 'BCS',
	p_IdInstrumento					=> 2249,
	p_IdTipoInstrumento				=> 'SIM',
	p_EsVentaCorta					=> 'N',
	p_Simulacion					=> #TT,
	p_out_TableCoberturaSimulacion	=> ?,
	p_out_TableCoberturaPortafolio	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);
	
	
	delete from #TT;
insert into #TT select 122,0,0,1201 from dummy;

CALL "BSCL"."BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_UsaReglasNegocio				=> 'N',
	p_IdPortafolio					=> 343710,
	p_IdBolsa						=> 'BCS',
	p_IdInstrumento					=> 0,
	p_IdTipoInstrumento				=> '',
	p_EsVentaCorta					=> 'S',
	p_Simulacion					=> #TT,
	p_out_TableCoberturaSimulacion	=> ?,
	p_out_TableCoberturaPortafolio	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);
	
	
	delete from #TT;
insert into #TT select 122,0,0,1201 from dummy;

CALL "BSCL"."BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_UsaReglasNegocio				=> 'N',
	p_IdPortafolio					=> 343710,
	p_IdBolsa						=> 'BCS',
	p_IdInstrumento					=> 330267,
	p_IdTipoInstrumento				=> 'FDO',
	p_EsVentaCorta					=> 'N',
	p_Simulacion					=> #TT,
	p_out_TableCoberturaSimulacion	=> ?,
	p_out_TableCoberturaPortafolio	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);
	

