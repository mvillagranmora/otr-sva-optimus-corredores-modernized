--TBCTCIRV

SELECT  sebra.rut_cli,optimus.rut_cli,sebra.sec_rut_cli,optimus.sec_rut_cli
,sebra.nemo AS nemo_sebra ,optimus.nemo AS nemo_optimus
,sebra.can_actual AS can_actual_sebra ,optimus.can_actual AS can_actual_optimus
,sebra.can_com_plazo AS can_com_plazo_sebra ,optimus.can_com_plazo AS can_com_plazo_optimus
,sebra.can_garantia AS can_garantia_sebra ,optimus.can_garantia AS can_garantia_optimus
,sebra.monto_act_ajus AS monto_act_ajus_sebra ,optimus.monto_act_ajus AS monto_act_ajus_optimus
--,sebra.monto_cup AS monto_cup_sebra ,optimus.monto_cup AS monto_cup_optimus
,sebra.monto_ini_ajus AS monto_ini_ajus_sebra ,optimus.monto_ini_ajus AS monto_ini_ajus_optimus
,sebra.precio_prom_comp AS precio_prom_comp_sebra ,optimus.precio_prom_comp AS precio_prom_comp_optimus
,sebra.precio_cierre, optimus.precio_cierre
from
(
	select rut_cli,sec_rut_cli,nemo,
	sum(can_actual) as can_actual,
	sum(can_com_plazo) as can_com_plazo,
	sum(can_garantia) as can_garantia,
	sum(monto_act_ajus) as monto_act_ajus,
	--sum(monto_cup) as monto_cup,
	sum(monto_ini_ajus) as monto_ini_ajus,
	avg(case when precio_prom_comp = 0 then null else precio_prom_comp end) as precio_prom_comp,
	avg(case when precio_cierre = 0 then null else precio_cierre end) as precio_cierre
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV" where COD_PER = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) as optimus
,
(
	select rut_cli,sec_rut_cli,nemo,
	sum(can_actual) as can_actual,
	sum(can_com_plazo) as can_com_plazo,
	sum(can_garantia) as can_garantia,
	sum(monto_act_ajus) as monto_act_ajus,
	--sum(monto_cup) as monto_cup,
	sum(monto_ini_ajus) as monto_ini_ajus,
	avg(case when precio_prom_comp = 0 then null else precio_prom_comp end) as precio_prom_comp,
	avg(case when precio_cierre = 0 then null else precio_cierre end) as precio_cierre
from "2022_05_31_TBCTCIRV" where COD_PER = '2022-05'
	group by rut_cli,sec_rut_cli,nemo
) AS sebra
WHERE  sebra.rut_cli = optimus.rut_cli
AND sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.nemo = optimus.nemo
AND (
--abs(sebra.monto_val_disp - optimus.monto_val_disp) > 1
abs(sebra.can_actual - optimus.can_actual) > 0 or  --ok
abs(sebra.can_com_plazo - optimus.can_com_plazo) > 0 or  --okok
abs(sebra.can_garantia - optimus.can_garantia) > 0 or --ok 
--abs(sebra.monto_act_ajus - optimus.monto_act_ajus) > 10 or  --diferencia de monto valorizado con sebra, en optimus pxq se ve bien
--abs(sebra.monto_ini_ajus - optimus.monto_ini_ajus) > 10  or   --en sebra hay carteras sin valorizar
abs(sebra.precio_cierre - optimus.precio_cierre) > 10    --diferencia porque la garantia no se valoriza a mercado si no con castigo
--abs(sebra.precio_med_cie - optimus.precio_med_cie) > 1
--abs(sebra.precio_prom_comp - optimus.precio_prom_comp) > 1 ignorar
)

select CAN_ACTUAL ,MONTO_ACT_AJUS ,MONTO_INI_AJUS ,PRECIO_CIERRE,PRECIO_PROM_COMP ,* from "2022_05_31_TBCTCIRV" t  
where COD_PER  = '2022-05' and RUT_CLI  = '  1726769-8' and NEMO = 'CHILE'

681364.00

select CAN_ACTUAL ,MONTO_ACT_AJUS ,MONTO_INI_AJUS ,PRECIO_CIERRE,PRECIO_PROM_COMP ,* from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV" t  
where RUT_CLI  = ' 96564330-3' and NEMO = 'ANTARCHILE'

1030850563.80000000

select * from "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" reg
inner join "BSCL.Plataforma.BD::INS.Instrumento" ins on reg."IdInstrumento"  = ins."IdInstrumento" 
where ins."Nemotecnico"  = 'ANTARCHILE'

select * from "BSCL.Custodia.BD::CUS.Cartera" bcbcc where "FolioCartera"  = 67480


select count(1) 
from "BSCL.Custodia.BD::CUS.Cartera" bcbcc 
where "IdEstado"  = 'VIG' AND ("MontoCompra" = 0 OR "PrecioCompra" = 0)
AND "IdClasificacionTipoInstrumento" = 'RV'

BCI
CHILE
ISANPA
GASCOINV
ENELTXCH
RANQUIL
ESVAL-A
POTASIOS-B
POTASIOS-A
MELON
P&S
INDISA
NITRATOS
ELUXSA
SIXTERRA
BETLAN DOS
CUPRUM
OXIQUIM
CEM
PLANVITAL
ICBLANCA
SIPSA-OSA
LIBERTY
NORTE SUR
BOLVALORES