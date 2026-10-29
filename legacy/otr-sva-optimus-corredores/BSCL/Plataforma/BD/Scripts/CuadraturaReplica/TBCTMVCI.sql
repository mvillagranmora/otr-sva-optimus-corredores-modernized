--llave de tabla es foranea a mvto

select t.CANTIDAD ,t2.CANTIDAD ,t2.fec_mvto,t2.FEC_ING_MVTO ,* from "2022_05_31_TBCTMVCI" t 
left join "2022_05_31_TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31'
order by t.SEC_MOVTO desc


select t.CANTIDAD ,t2.CANTIDAD ,t2.fec_mvto,t2.FEC_ING_MVTO ,* 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI" t 
left join "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31'
order by t.SEC_MOVTO desc

select * from 
(
	select t2.RUT_CLI ,t2.SEC_RUT_CLI ,t2.nemo,sum(t.cantidad)  as c1
	from "2022_05_31_TBCTMVCI" t 
	left join "2022_05_31_TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
	where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31' and t2.ind_anulado != 'A' and t2.ind_tit_cus = 'C'   --EN OPTIMUS NOMINALES NO GENERAN MOVIMIENTO
	AND T2.COD_MOVTO NOT IN ('NVTP','RGRV','VTIF','IIIF')   --estos movimientos no se cargaron, son de vencimiento de simultaneas o vencimiento de pacto
	group by t2.rut_cli,t2.SEC_RUT_CLI,t2.nemo
) as sebra
left join
(
	select t2.RUT_CLI ,t2.SEC_RUT_CLI ,t2.nemo,sum(t.cantidad)  as c1
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI" t 
	left join "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
	where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31' and t2.ind_anulado != 'A' --EN OPTIMUS HAY MOVIMIENTOS ANULADOS
	group by t2.rut_cli,t2.SEC_RUT_CLI ,t2.nemo
) as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and sebra.nemo = optimus.nemo
where (sebra.c1 != optimus.c1 or optimus.c1 is null) and sebra.nemo not in ('PAGARE R','PAGARE NR','PDBC')
order by sebra.rut_cli,sebra.sec_rut_cli,sebra.nemo

--t2.RUT_CLI  = ' 19296115-7' and t2.SEC_RUT_CLI  = 0 and nemo = 'COLO COLO' cartera mal cargada, tiene movimientos repetidos, todos los rv estan asi
--t2.RUT_CLI  = ' 96564330-3' and t2.SEC_RUT_CLI  = 1 and nemo = 'CAP' 2 mov por vencimiento no se cargaron x 4230
--t2.RUT_CLI  = ' 96564330-3' and t2.SEC_RUT_CLI  = 1 and nemo = 'VAPORES' 2 mov por vencimiento no se cargaron x 358454


select * from 
(
	select t2.RUT_CLI ,t2.SEC_RUT_CLI ,t2.nemo,sum(case when t2.nemo = 'PDBC' THEN t2.val_resc*T2.CANTIDAD ELSE T2.VAL_RESC END)  as c1
	from "2022_05_31_TBCTMVCI" t 
	left join "2022_05_31_TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
	where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31' and t2.ind_anulado != 'A' and t2.ind_tit_cus = 'C'   --EN OPTIMUS NOMINALES NO GENERAN MOVIMIENTO
	AND T2.COD_MOVTO NOT IN ('NVTP','RGRV','VTIF','IIIF','VRIC')   --estos movimientos no se cargaron, son de vencimiento de simultaneas o vencimiento de pacto
	group by t2.rut_cli,t2.SEC_RUT_CLI,t2.nemo
) as sebra
left join
(
	select t2.RUT_CLI ,t2.SEC_RUT_CLI ,t2.nemo,sum(t2.val_resc)  as c1
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI" t 
	left join "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
	where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31' and t2.ind_anulado != 'A' --EN OPTIMUS HAY MOVIMIENTOS ANULADOS
	group by t2.rut_cli,t2.SEC_RUT_CLI ,t2.nemo
) as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and sebra.nemo = optimus.nemo
where (sebra.c1 != optimus.c1 or optimus.c1 is null)
order by sebra.rut_cli,sebra.sec_rut_cli,sebra.nemo

--and t2.RUT_CLI  = ' 81585900-6' and t2.SEC_RUT_CLI  = 0 and nemo = 'PAGARE NR' CUADRA, SE ANULO UN PACTO MAL INGRESADO
--and t2.RUT_CLI  = ' 96564330-3' and t2.SEC_RUT_CLI  = 1 and nemo = 'PAGARE NR' NO SE PUEDE CUADRAR YA QUE HAY ANULACIONES Y MOVIMIENTOS DE RETIRO ENTRE MEDIO
--and t2.RUT_CLI  = ' 96564330-3' and t2.SEC_RUT_CLI  = 1 and nemo = 'PDBC' NO SE PUEDE CUADRAR, LA CARGA DE PACTOS HIZO MOVIMIENTOS ADICIONALES

select t2.VAL_RESC ,t2.COD_MOVTO ,t.CANTIDAD ,t2.CANTIDAD ,t2.fec_mvto,t2.FEC_ING_MVTO ,* 
from "2022_05_31_TBCTMVCI" t 
left join "2022_05_31_TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31' and t2.IND_ANULADO != 'A'
and t2.RUT_CLI  = ' 96564330-3' and t2.SEC_RUT_CLI  = 1 and nemo = 'PDBC'
order by t.SEC_MOVTO desc

select t2.VAL_RESC ,t2.COD_MOVTO ,t.CANTIDAD ,t2.CANTIDAD ,t2.fec_mvto,t2.FEC_ING_MVTO ,* 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI" t 
left join "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t2 on t.SEC_MOVTO  = t2.SEC_MOVTO 
where t2.FEC_ING_MVTO = '2022-05-31' and t2.FEC_MVTO  = '2022-05-31'  and t2.IND_ANULADO != 'A'
and t2.RUT_CLI  = ' 96564330-3' and t2.SEC_RUT_CLI  = 1 and nemo = 'PDBC'
order by t.SEC_MOVTO desc

	 76090492-9	0	BCHIAR0613
	  96564330-3	1	BCHIAR0613
 
select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst where FEC_ING_MVTO = '2022-05-31' and RUT_CLI = '  5102409-5' and SEC_RUT_CLI  = 0 --7675

select * from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31' and RUT_CLI = ' 94627000-8' and SEC_RUT_CLI  = 0

select * from "2022_05_31_TBCTMVCI" t

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst  where SEC_MOVTO  = 83860 or SEC_MOVTO = -83860


SELECT * FROM "BSCL.Sebra.BD::MIG.TBPLTMOV" bsbmt WHERE CODIGO = 'CRIC'

select * from "2022_05_31_TBCTCURF" t where RUT_CLI = ' 76090492-9'

select * from "2022_05_31_TBCTCIIF" t  where NEMO_SVS = 'FNBCI-300622'

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" brbvst where RUT_CLI = ' 61216000-7' and EMISOR  = 'BCI'-- and NEMO_SVS = 'FNBCI-300622'

select * from "2022_05_31_TBCTMVTO" t  where NEMO = 'BCHIAR0613'