select RUT_CLI ,SEC_RUT_CLI , nemo,CAN_ACTUAL,* from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV"
where NEMO  = 'CASHCLP'

SELECT "FolioCartera" AS "FolioCustodia","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor",
			   "Nemotecnico","NemoSVS","IdBoveda","IdTipoReajuste","PlazoEnDiasOperacion","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS","FechaEmisionInstrumento",
			   "IdTipoComprobante","FolioComprobante","FechaComprobante",
			   sum("INGCantidad") AS "Cantidad",sum("INGMontoMercadoActualMonedaInstrumento") AS "Monto",sum("INGPrecioMercadoActual") AS "PrecioMercado",
			   avg("PrecioCompra") AS "PrecioCompra", max("IdMonedaInstrumento") AS "IdMonedaInstrumento",sum("INGMontoATasaCompraActual") AS "MontoCompraAjus"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
		  AND "Nemotecnico" = 'CASHCLP'
		 GROUP BY "FolioCartera","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor",
			   "Nemotecnico","NemoSVS","IdBoveda","IdTipoReajuste","PlazoEnDiasOperacion","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS","FechaEmisionInstrumento",
			   "IdTipoComprobante","FolioComprobante","FechaComprobante"

select RUT_CLI ,SEC_RUT_CLI , nemo,CAN_CUSTODIA  from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV"
order by CAN_CUSTODIA asc



--CUADRAR CARTERA VS CUSTODIA
select * from (
select RUT_CLI ,SEC_RUT_CLI ,nemo,sum(CAN_CUSTODIA) as  c1  from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV"
where COD_BOVEDA = 'TOT'
group by RUT_CLI ,SEC_RUT_CLI ,nemo
) cus
left join (
	select RUT_CLI ,SEC_RUT_CLI , nemo,sum(CAN_ACTUAL) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV"
	group by RUT_CLI ,sec_rut_cli,nemo
) car on cus.rut_cli = car.rut_cli and cus.sec_rut_cli = car.sec_rut_cli and cus.nemo = car.nemo
where cus.c1 != car.c2

select * from (
select RUT_CLI ,SEC_RUT_CLI ,nemo,sum(monto_valoriz) as  c1  from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV"
where COD_BOVEDA = 'TOT'
group by RUT_CLI ,SEC_RUT_CLI ,nemo
) cus
left join (
	select RUT_CLI ,SEC_RUT_CLI , nemo,sum(monto_act_ajus) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV"
	group by RUT_CLI ,sec_rut_cli,nemo
) car on cus.rut_cli = car.rut_cli and cus.sec_rut_cli = car.sec_rut_cli and cus.nemo = car.nemo
where cus.c1 != car.c2

select CAN_CUSTODIA ,* from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURV" brbvst 
where RUT_CLI = '  9255443-0' and NEMO = 'LTM'

select CAN_ACTUAL ,* from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV" brbvst 
where RUT_CLI = '  9255443-0' and NEMO = 'LTM'

SELECT 'TRA' AS "Tipo" , "FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
				CASE WHEN trim(coalesce(t."IdBoveda",'')) = '' THEN NULL ELSE trim(t."IdBoveda") END AS "IdBoveda",
				0 AS "PrecioMercado","PrecioCompra","IdMonedaInstrumento",
				"Cantidad" AS "Cantidad",0 as "PrecioAPlazo",t."IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t 
		JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'

SELECT "IdMovimientoCarteraEnTransito","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",
				CASE WHEN trim(coalesce(t."IdBoveda",'')) = '' THEN NULL ELSE trim(t."IdBoveda") END AS "IdBoveda",
				"PrecioCompra" AS "INGPrecioPromedioCompra", 
				"IdMonedaInstrumento" AS "IdMonedaInstrumento",
				"Cantidad" * - 1 AS "Cantidad","MontoCompra" AS "Monto"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'  AND "IdClasificacionTipoInstrumento" = 'RV'
		
select * from (
select RUT_CLI ,SEC_RUT_CLI ,nemo,sum(CAN_CUSTODIA) as  c1  
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF"
where COD_BOVEDA = 'TOT'
group by RUT_CLI ,SEC_RUT_CLI ,nemo
) cus
left join (
	select RUT_CLI ,SEC_RUT_CLI , nemo,sum(CAN_ACTUAL) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF"
	group by RUT_CLI ,sec_rut_cli,nemo
) car on cus.rut_cli = car.rut_cli and cus.sec_rut_cli = car.sec_rut_cli and cus.nemo = car.nemo
where cus.c1 != car.c2





select * from (
select RUT_CLI ,SEC_RUT_CLI ,nemo,avg(tir_prom) as  c1  
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF"
where COD_BOVEDA = 'TOT'
group by RUT_CLI ,SEC_RUT_CLI ,nemo
) cus
left join (
	select RUT_CLI ,SEC_RUT_CLI , nemo,avg(tir_prom_comp) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF"
	group by RUT_CLI ,sec_rut_cli,nemo
) car on cus.rut_cli = car.rut_cli and cus.sec_rut_cli = car.sec_rut_cli and cus.nemo = car.nemo
where cus.c1 != car.c2

select TIR_MED_CIE ,TIR_PROM ,*
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF"
where COD_BOVEDA = 'TOT' and RUT_CLI = '  4087196-9'


select PRECIO_PROM_COMP ,TIR_MED_CIE ,TIR_PROM_COMP ,*
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF"
where RUT_CLI = '  4087196-9'





select count(1)
from "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRF"


select Estado,count(*) 
from FMCarteraForward 
where FechaInicio<='2023-06-30'  
group by Estado



select COD_MOVTO , * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst 
where FEC_MVTO >= '2024-10-21' and COD_MOVTO = 'NVRV'

