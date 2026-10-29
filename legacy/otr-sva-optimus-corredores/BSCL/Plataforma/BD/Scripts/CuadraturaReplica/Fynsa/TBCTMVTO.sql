

--CUADRAR MVTO SEBRA VS OPTIMUS
select opt.rut_cli,opt.sec_rut_cli,opt.c1 as C_OPTIMUS,sebra.c2 as C_SEBRA,
opt.p1 as P_OPTIMUS,sebra.p2 as P_SEBRA,opt.m1 as M_OPTIMUS, sebra.m2 as M_SEBRA
from (
	select FEC_MVTO,RUT_CLI ,SEC_RUT_CLI ,SUM(CANTIDAD) as c1 , sum(PRECIO) as p1, 
	COD_MOVTO ,sum(monto) as m1
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst 
	where IND_ANULADO != 'A'
	GROUP BY fec_mvto,RUT_CLI ,SEC_RUT_CLI,COD_MOVTO 
) opt
left join (
	select fec_mvto, RUT_CLI ,SEC_RUT_CLI ,SUM(CANTIDAD) as c2 , sum(PRECIO) as p2,
	COD_MOVTO ,sum(monto) as m2
	from "BSCL.Sebra.BD::MIG.TBCTMVTO" brbvst 
	where IND_ANULADO != 'A'
	GROUP BY fec_mvto,RUT_CLI ,SEC_RUT_CLI,COD_MOVTO 
) sebra on opt.fec_mvto = sebra.fec_mvto and sebra.cod_movto = opt.cod_movto
and sebra.rut_cli = opt.rut_cli and sebra.sec_rut_cli = opt.sec_rut_cli
where opt.cod_movto in ('NCTP','NTPC','NTPV','NVTP')
and opt.fec_mvto = '2023-07-05'
and (c1 != c2 or p1 != p2 or m1 != M2)


--CUADRAR MVTO SEBRA VS OPTIMUS
select opt.rut_cli,opt.sec_rut_cli,opt.nemo,opt.c1 as C_OPTIMUS,sebra.c2 as C_SEBRA,
opt.p1 as P_OPTIMUS,sebra.p2 as P_SEBRA,opt.m1 as M_OPTIMUS, sebra.m2 as M_SEBRA
from (
	select nemo,FEC_MVTO,RUT_CLI ,SEC_RUT_CLI ,SUM(CANTIDAD) as c1 , sum(PRECIO) as p1, 
	COD_MOVTO ,sum(monto) as m1
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst 
	where IND_ANULADO != 'A'
	GROUP BY nemo,fec_mvto,RUT_CLI ,SEC_RUT_CLI,COD_MOVTO 
) opt
left join (
	select nemo,fec_mvto, RUT_CLI ,SEC_RUT_CLI ,SUM(CANTIDAD) as c2 , sum(PRECIO) as p2,
	COD_MOVTO ,sum(monto) as m2
	from "BSCL.Sebra.BD::MIG.TBCTMVTO" brbvst 
	where IND_ANULADO != 'A'
	GROUP BY nemo,fec_mvto,RUT_CLI ,SEC_RUT_CLI,COD_MOVTO 
) sebra on opt.fec_mvto = sebra.fec_mvto and sebra.cod_movto = opt.cod_movto
and sebra.rut_cli = opt.rut_cli and sebra.sec_rut_cli = opt.sec_rut_cli and sebra.nemo = opt.nemo
where opt.cod_movto IN ('NCTP','NTPC','NTPV','NVTP')
and opt.fec_mvto = '2023-07-05'
and (c1 != c2 or p1 != p2 or m1 != M2)


