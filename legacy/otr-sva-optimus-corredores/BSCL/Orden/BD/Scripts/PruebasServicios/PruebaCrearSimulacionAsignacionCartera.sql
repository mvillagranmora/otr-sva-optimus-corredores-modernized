SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdTransaccion"	BIGINT,
		"Cantidad"		DECIMAL(26,8)
		
);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2 (
		"IdPortafolio"	BIGINT,
		"FolioCartera"	BIGINT,
		"Cantidad" DECIMAL (26,8)
		
);

insert into #TT select 16406328,15000000 from DUMMY;
--insert into #TT2  select top 10 "IdPortafolio","FolioCartera","Cantidad" from "BSCL"."BSCL.Custodia.BD::CUS.Cartera" where "Cantidad" > 0;
insert into #TT2 select 132695,20570780,200 from dummy;
/*insert into #TT2 select 1,992,50 from dummy;
insert into #TT2 select 1,993,600 from dummy;
insert into #TT2 select 1,994,70 from dummy;
insert into #TT2 select 1,995,3000 from dummy;
insert into #TT2 select 1,996,100 from dummy;
insert into #TT2 select 1,997,200 from dummy;
insert into #TT2 select 1,998,50 from dummy;
insert into #TT2 select 1,999,600 from dummy;
insert into #TT2 select 1,1000,700 from dummy;
insert into #TT2 select 1,1001,600 from dummy;
insert into #TT2 select 1,1002,600 from dummy;
insert into #TT2 select 1,1003,550 from dummy;
insert into #TT2 select 1,1004,790 from dummy;
insert into #TT2 select 1,1005,320 from dummy;
insert into #TT2 select 1,1006,980 from dummy;
insert into #TT2 select 1,1007,120 from dummy*/;

CALL "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCartera" (
	p_IdEmpresa 			=> 1,
	p_IdUsuario 			=> 1,
	p_IdModulo 				=> 'ORD',
	p_UsaReglasNegocio 		=> 'N',
	p_IdTipoAsignacion 		=> 'CARTERA',
	p_IdTipo 				=> 'ASIG',
	p_Origen 				=> '',
	p_Transacciones 		=> #TT,
	p_Carteras 				=> #TT2,
	p_out_SimAsignacion 		=> ?,
	p_out_cobrosAsignacion 		=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,
	p_out_ErroresDetalle 		=> ?
	
									
								);
								
								
								
								
