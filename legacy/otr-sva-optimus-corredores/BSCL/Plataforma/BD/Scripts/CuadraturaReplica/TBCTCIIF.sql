--TBCTCIIF
SELECT  sebra.rut_cli,optimus.rut_cli,sebra.sec_rut_cli,optimus.sec_rut_cli
,sebra.can_actual AS can_actual_sebra ,optimus.can_actual AS can_actual_optimus
,sebra.val_resc AS val_resc_sebra ,optimus.val_resc AS val_resc_optimus
,sebra.nemo_svs
,sebra.nemo AS nemo_sebra ,optimus.nemo AS nemo_optimus
,sebra.emisor AS emisor_sebra ,optimus.emisor AS emisor_optimus
,sebra.tipo_reaj AS tipo_reaj_sebra ,optimus.tipo_reaj AS tipo_reaj_optimus
,sebra.monto_val_disp AS monto_val_disp_sebra ,optimus.monto_val_disp AS monto_val_disp_optimus
,sebra.val_resc AS val_resc_sebra ,optimus.val_resc AS val_resc_optimus
,sebra.can_com_plazo AS can_com_plazo_sebra ,optimus.can_com_plazo AS can_com_plazo_optimus
,sebra.can_garantia AS can_garantia_sebra ,optimus.can_garantia AS can_garantia_optimus
,sebra.monto_act_ajus AS monto_act_ajus_sebra ,optimus.monto_act_ajus AS monto_act_ajus_optimus
,sebra.monto_ini_ajus AS monto_ini_ajus_sebra ,optimus.monto_ini_ajus AS monto_ini_ajus_optimus
,sebra.tasa_med_cie AS tasa_med_cie_sebra ,optimus.tasa_med_cie AS tasa_med_cie_optimus
,sebra.tasa_prom_comp AS tasa_prom_comp_sebra ,optimus.tasa_prom_comp AS tasa_prom_comp_optimus
from
(
select rut_cli,sec_rut_cli,nemo,emisor,tipo_reaj,nemo_svs,
	sum(case when nemo = 'PDBC' THEN MONTO_VAL_DISP * (CAN_ACTUAL) ELSE monto_val_disp end) as monto_val_disp,
	sum(case when nemo = 'PDBC' THEN CAN_ACTUAL+CAN_GARANTIA+CAN_COM_PLAZO ELSE VAL_RESC END)  as val_resc,
	--sum(val_resc) as val_resc,
	/*sum(case when nemo = 'PDBC' and can_actual > 0 THEN 1 ELSE can_actual END) as can_actual,
	sum(case when nemo = 'PDBC' and can_com_plazo > 0 THEN 1 ELSE can_com_plazo END) as can_com_plazo,
	sum(case when nemo = 'PDBC' and can_garantia > 0 THEN 1 ELSE can_garantia END) as can_garantia,*/
	sum(can_actual) as can_actual,
	sum(can_com_plazo) as can_com_plazo,
	sum(can_garantia) as can_garantia,
	sum(monto_act_ajus) as monto_act_ajus,
	sum(monto_ini_ajus) as monto_ini_ajus,
	avg(tasa_med_cie) as tasa_med_cie,
	avg(tasa_prom_comp) as tasa_prom_comp
	from "2022_05_31_TBCTCIIF" where COD_PER = '2022-05' and fec_vcto >= '2022-05-31'
	group by rut_cli,sec_rut_cli,nemo,emisor,tipo_reaj,nemo_svs
) AS sebra
left JOIN
(
select rut_cli,sec_rut_cli,nemo,emisor,tipo_reaj,nemo_svs,
	sum(case when nemo = 'PDBC' THEN MONTO_VAL_DISP * (CAN_ACTUAL) ELSE monto_val_disp end) as monto_val_disp,
	/*sum(case when can_actual > 0 or can_com_plazo > 0 or can_garantia > 0 then val_resc else 0 end) as val_resc,*/
	sum(case when nemo = 'PDBC' THEN CAN_ACTUAL+CAN_GARANTIA+CAN_COM_PLAZO ELSE VAL_RESC  END)  as val_resc,
	--sum(val_resc) as val_resc,
	/*sum(case when nemo = 'PDBC' and CAN_ACTUAL > 0 THEN 1 ELSE CAN_ACTUAL END ) as can_actual,*/
	sum(can_actual) as can_actual,
	sum(can_com_plazo) as can_com_plazo,
	sum(can_garantia) as can_garantia,
	sum(monto_act_ajus) as monto_act_ajus,
	sum(monto_ini_ajus) as monto_ini_ajus,
	avg(tasa_med_cie) as tasa_med_cie,
	avg(tasa_prom_comp) as tasa_prom_comp
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" where COD_PER = '2022-05' and fec_vcto >= '2022-05-31'
	group by rut_cli,sec_rut_cli,nemo,emisor,tipo_reaj,nemo_svs
) as optimus
on  sebra.rut_cli = optimus.rut_cli
AND sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.emisor = optimus.emisor
AND sebra.nemo = optimus.nemo
and sebra.nemo_svs = optimus.nemo_svs
AND sebra.tipo_reaj = optimus.tipo_reaj
where (
optimus.val_resc is null or
abs(sebra.monto_val_disp - optimus.monto_val_disp) > 1000000 and --diferencias porque hay carteras sin valorizar
abs(sebra.val_resc - optimus.val_resc) > 0		or		--diferencias con sebra
--abs(sebra.can_actual - optimus.can_actual) > 1  --no sirve can actual por la fungibilidad
--abs(sebra.can_com_plazo - optimus.can_com_plazo) > 1 --diferencias por los pactos
--abs(sebra.can_garantia - optimus.can_garantia) > 0  ok
abs(sebra.monto_act_ajus - optimus.monto_act_ajus) > 1
--abs(sebra.monto_ini_ajus - optimus.monto_ini_ajus) > 1
--abs(sebra.tasa_med_cie - optimus.tasa_med_cie) > 1
--abs(sebra.tasa_prom_comp - optimus.tasa_prom_comp) > 1
)
order by sebra.rut_cli,sebra.nemo

9965136968.00000000

FNBCI-300622

  60706000-2	0	BCI	PAGARE NR descuadre
 60706000-2	0	ITAUCORP	PAGARE NR descuadre
 61216000-7	0	BCI	PAGARE R   descuadre
 61216000-7	0	BCI	PAGARE NR descuadre
 61216000-7	1	BCI	PAGARE NR descuadre
 61219000-3	0	BCI	PAGARE NR descuadre
 61604000-6	0	BCI	PAGARE NR descuadre
 70016330-K	0	BCI	PAGARE NR descuadre
 70016330-K	0	CENTRAL	PDBC descuadre
 76275453-3	0	BCI	PAGARE R descuadre
 76840089-K	0	BCI	PAGARE NR descuadre
 77330030-5	0	ESTADO	PAGARE NR descuadre
 78996910-8	0	ITAUCORP	PAGARE NR descuadre
 91705000-7	0	BCI	PAGARE NR descuadre
 91705000-7	0	ITAUCORP	PAGARE NR descuadre
 96504160-5	0	ITAUCORP	PAGARE NR descuadre
 96564330-3	1	ESTADO	PAGARE NR descuadre
 96856780-2	0	ITAUCORP	PAGARE NR descuadre
 97030000-7	0	BCI	PAGARE NR descuadre
 99597320-0	0	BCI	PAGARE R descuadre
 
SELECT MONTO_VAL_DISP ,VAL_RESC ,VAL_RESC_PLAZO ,CAN_ACTUAL,CAN_GARANTIA ,CAN_COM_PLAZO  ,VAL_RESC as v,* FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" 
--WHERE NEMO_svs  = 'FNEST-180223' and RUT_CLI  = ' 96564330-3'
where NEMO_svs  = 'FNEST-080622'  and RUT_CLI  = ' 96564330-3'
AND COD_PER='2022-05'
order by v

2557062568.00	12230453173

SELECT MONTO_VAL_DISP ,VAL_RESC ,VAL_RESC_PLAZO , CAN_ACTUAL,CAN_GARANTIA ,CAN_COM_PLAZO  ,VAL_RESC as v ,* FROM "2022_05_31_TBCTCIIF" 
--WHERE NEMO_svs  = 'FNEST-180223' and RUT_CLI  = ' 96564330-3'
where NEMO_svs  = 'FNEST-080622'  and RUT_CLI  = ' 96564330-3'
AND COD_PER='2022-05'
order by v 

select * from "BSCL.Plataforma.BD::PLA.Empresa" EMPRESA
			LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EMPRESA."IdSocioDeNegocio"
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CORR ON CORR."IdPersona" = SOC."IdPersona"

18997179164.0000	17278664616.00000000

SELECT CAN_ACTUAL,CAN_GARANTIA ,CAN_COM_PLAZO  ,VAL_RESC ,* FROM "2022_05_31_TBCTCIIF" 
where COD_PER = '2022-05' and CAN_COM_PLAZO = 1 and VAL_RESC = 0

SELECT CAN_ACTUAL,CAN_GARANTIA ,CAN_COM_PLAZO  ,VAL_RESC ,* FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" brbvst 
where COD_PER = '2022-05' and CAN_COM_PLAZO = 1 and VAL_RESC = 0




SELECT 
				s."FolioSubyacente" AS "FolioCustodia",
				s."INGFechaOperacion",
				c."IdClienteRelacionado" AS "IdCliente",
				c."IdPortafolioRelacionado",
				s."IdTipoInstrumento",
				s."IdInstrumento",
				s."IdEmisor",
				s."Nemotecnico",
				s."NemoSVS",
				'' AS "IdBoveda",
				s."IdTipoReajuste",
				c."PlazoEnDiasOperacion",
				s."EsFungible",
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				c."IdTipoComprobante",
--				CASE WHEN COALESCE ("FolioExternoReferencia",0) = 0 THEN  c."FolioComprobante" ELSE c."FolioExternoReferencia" END AS "FolioComprobante",
				"FolioComprobante",
				c."FechaComprobante",
				0  AS "Cantidad", ---"INGCantidad"
				S."INGValorRescate" AS "ValorRescate",	--"INGValorRescate"
				s."INGMontoATasaCompraActual" AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				S."INGMontoATasaSubyacenteActual" AS "MontoCartera",
				c."ValorParidadCierre",
				s."INGMontoCompraAnteriorMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')  THEN S."INGValorRescate" ELSE 0 END AS "ValorRescate_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Venta_Plazo",
			   CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGValorRescate" ELSE 0 END AS "ValorRescate_Venta_Plazo",
			   0 AS "CantidadGarantia",-- , C."IdTipoOperacion",
			   'CC' AS "TipoCartera"
			FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'IF' -- AND "IdClienteRelacionado" =  6069
			  AND C."IdTipoInstrumento" IN ('SIM','PAC')	AND C."IdTipoOperacion"  in ('VENSIM','VENPAC')		
				and c."IdCliente" = 9497 and s."NemoSVS"  = 'FNITA-121222'
				
				
				select "IdCliente" ,"IdClienteRelacionado" ,"INGValorRescate" ,"IdTipoOperacion" ,* from "BSCL.Custodia.BD::CUS.Cartera" as C where "NemoSVS"  = 'FNITA-121222'
				