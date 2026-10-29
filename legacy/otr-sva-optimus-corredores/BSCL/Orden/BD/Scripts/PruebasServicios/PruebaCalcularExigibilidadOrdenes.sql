SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdInstrumento" 	BIGINT,				
	"Cantidad" 			DECIMAL(26,8),
	"Precio" 			DECIMAL(26,8),
	"Monto" 			DECIMAL(26,8),
	"IdBolsa" 			NVARCHAR(3)
);

insert into #TT select 330267,0,0,10000,'BCS' from dummy;

CALL "BSCL.Orden.BD.Servicios::CalcularExigibilidadOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_simularOperacion				=> 'S',
	p_UsaReglasNegocio				=> 'N',
	p_TableExigibilidadSimulacion 	=> #TT,
	p_EsVentaCorta					=> 'N',
	p_IdTipoInstrumento				=> 'FDO',
	p_IdPortafolio					=> 343710,
	p_IdBolsa						=> 'BCS',
	p_IdInstrumento					=> 330267,
	
	p_out_TableExigibilidadSimulacion	=> ?,
	p_out_TableExigibilidadPortafolio	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);
	
	
delete from  #TT ;
insert into #TT select 1368,0,0,10000,'BCS' from dummy;


CALL "BSCL.Orden.BD.Servicios::CalcularExigibilidadOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_simularOperacion				=> 'S',
	p_UsaReglasNegocio				=> 'N',
	p_TableExigibilidadSimulacion 	=> #TT,
	p_EsVentaCorta					=> 'S',
	p_IdTipoInstrumento				=> '',
	p_IdPortafolio					=> 343710,
	p_IdBolsa						=> 'BCS',
	p_IdInstrumento					=> 0,
	
	p_out_TableExigibilidadSimulacion	=> ?,
	p_out_TableExigibilidadPortafolio	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);
	

delete from  #TT ;
insert into #TT select 1368,0,0,10000,'BCS' from dummy;

CALL "BSCL.Orden.BD.Servicios::CalcularExigibilidadOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_simularOperacion				=> 'S',
	p_UsaReglasNegocio				=> 'N',
	p_TableExigibilidadSimulacion 	=> #TT,
	p_EsVentaCorta					=> 'N',
	p_IdTipoInstrumento				=> 'SIM',
	p_IdPortafolio					=> 343710,
	p_IdBolsa						=> 'BCS',
	p_IdInstrumento					=> 2249,
	
	p_out_TableExigibilidadSimulacion	=> ?,
	p_out_TableExigibilidadPortafolio	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);