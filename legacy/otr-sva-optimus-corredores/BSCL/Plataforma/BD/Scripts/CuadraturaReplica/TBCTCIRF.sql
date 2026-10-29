--TBCTCIRF
SELECT  sebra.rut_cli,sebra.sec_rut_cli
,sebra.tipo_fam AS tipo_fam_sebra
,sebra.nemo AS nemo_sebra
,sebra.emisor AS emisor_sebra
,sebra.monto_val_disp AS monto_val_disp_sebra ,optimus.monto_val_disp AS monto_val_disp_optimus
,sebra.can_actual AS can_actual_sebra ,optimus.can_actual AS can_actual_optimus
,sebra.can_com_plazo AS can_com_plazo_sebra ,optimus.can_com_plazo AS can_com_plazo_optimus
,sebra.can_garantia AS can_garantia_sebra ,optimus.can_garantia AS can_garantia_optimus
,sebra.monto_act_ajus AS monto_act_ajus_sebra ,optimus.monto_act_ajus AS monto_act_ajus_optimus
--,sebra.monto_cup AS monto_cup_sebra ,optimus.monto_cup AS monto_cup_optimus
,sebra.monto_ini_ajus AS monto_ini_ajus_sebra ,optimus.monto_ini_ajus AS monto_ini_ajus_optimus
,sebra.precio_med_cie AS precio_med_cie_sebra ,optimus.precio_med_cie AS precio_med_cie_optimus
,sebra.precio_prom_comp AS precio_prom_comp_sebra ,optimus.precio_prom_comp AS precio_prom_comp_optimus
,sebra.tir_med_cie AS tir_med_cie_sebra ,optimus.tir_med_cie AS tir_med_cie_optimus
from
(
	select rut_cli,sec_rut_cli,nemo,emisor,tipo_fam,
	sum(monto_val_disp) as monto_val_disp,
	sum(can_actual) as can_actual,
	sum(can_com_plazo) as can_com_plazo,
	sum(can_garantia) as can_garantia,
	sum(monto_act_ajus) as monto_act_ajus,
	--sum(monto_cup) as monto_cup,
	sum(monto_ini_ajus) as monto_ini_ajus,
	avg(precio_med_cie) as precio_med_cie,
	avg(precio_prom_comp) as precio_prom_comp,
	avg(tir_med_cie) as tir_med_cie
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF" where COD_PER = '2022-05'
	group by rut_cli,sec_rut_cli,nemo,emisor,tipo_fam
) as optimus
,
(
	select rut_cli,sec_rut_cli,nemo,emisor,tipo_fam,
	sum(monto_val_disp) as monto_val_disp,
	sum(can_actual) as can_actual,
	sum(can_com_plazo) as can_com_plazo,
	sum(can_garantia) as can_garantia,
	sum(monto_act_ajus) as monto_act_ajus,
	--sum(monto_cup) as monto_cup,
	sum(monto_ini_ajus) as monto_ini_ajus,
	avg(precio_med_cie) as precio_med_cie,
	avg(precio_prom_comp) as precio_prom_comp,
	avg(tir_med_cie) as tir_med_cie
	from "2022_05_31_TBCTCIRF" where COD_PER = '2022-05'
	group by rut_cli,sec_rut_cli,nemo,emisor,tipo_fam
) AS sebra
WHERE  sebra.rut_cli = optimus.rut_cli
AND sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.emisor = optimus.emisor
AND sebra.nemo = optimus.nemo
AND sebra.tipo_fam = optimus.tipo_fam
AND (
--abs(sebra.monto_val_disp - optimus.monto_val_disp) > 1 or
abs(sebra.can_actual - optimus.can_actual) > 0 or --ok
abs(sebra.can_com_plazo - optimus.can_com_plazo) > 0 or --ok
abs(sebra.can_garantia - optimus.can_garantia) > 0  or --ok
abs(sebra.monto_act_ajus - optimus.monto_act_ajus) > 1000 or --diferencias menores en valorizacion
--abs(sebra.monto_cup - optimus.monto_cup) > 1
--abs(sebra.monto_ini_ajus - optimus.monto_ini_ajus) > 1 ----diferencias menores en valorizacion
--abs(sebra.precio_med_cie - optimus.precio_med_cie) > 1  --ok
abs(sebra.precio_prom_comp - optimus.precio_prom_comp) > 1 --diferencias porque el sistema no esta valorizando algunas carteras compradas con pacto, no calcula el ppp
--abs(sebra.tir_med_cie - optimus.tir_med_cie) > 1 ignorar
)

select CAN_ACTUAL ,MONTO_ACT_AJUS ,MONTO_INI_AJUS ,PRECIO_CIERRE,PRECIO_PROM_COMP ,* from "2022_05_31_TBCTCIRF" t  
where COD_PER  = '2022-05' and RUT_CLI  = '  1726769-8' and NEMO = 'CHILE'

681364.00

select PRECIO_MED_CIE ,CAN_ACTUAL ,MONTO_ACT_AJUS ,MONTO_INI_AJUS,PRECIO_PROM_COMP ,* 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF" t  
where RUT_CLI  = ' 91550000-5' and NEMO = 'BSECZ31217'
