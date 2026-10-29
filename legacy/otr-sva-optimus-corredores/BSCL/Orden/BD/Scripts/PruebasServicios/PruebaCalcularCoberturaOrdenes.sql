SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdInstrumento" BIGINT, "Cantidad" DECIMAL(26,8), "PrecioMercado" DECIMAL(26,8),"Monto" DECIMAL(26,8)
);

delete from #TT;
insert into #TT select 308,0,0,4000 from dummy;
insert into #TT select 32,0,0,1201 from dummy;

CALL "BSCL"."BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 25,
	p_IdModulo						=> 'ORD',
	p_UsaReglasNegocio				=> 'N',
	p_IdPortafolio					=> 189419,
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
	
	
	CALL "BSCL.Plataforma.BD.Servicios::CalcularCobertura"(
	    	p_IdEmpresa						=> 1,
		    p_IdUsuario						=> 25,
		    p_IdModulo						=> 'ORD',
			p_UsaReglasNegocio				=> 'S',
			p_IdPortafolio					=> 189419,
			p_IdEntidadDestinoGarantia		=> 2,
			p_IdTipoInstrumentoExigeGarantia=> 'SIM',
		    p_IdInstrumentoExigeGarantia	=> 2249,
			p_Simulacion					=> #TT,
			
		    p_out_TableCoberturaSimulacion		=> ?,
			p_out_TableCoberturaPortafolio		=> ?,
		    p_out_AdvertenciasContador			=> ?,
			p_out_AdvertenciasDetalle			=> ?,
			p_out_ErroresContador				=> ?,
			p_out_ErroresDetalle				=> ?);
	
	
	delete from #TT;
insert into #TT select 122,0,0,1201 from dummy;

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdInstrumento" = 308

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
	

