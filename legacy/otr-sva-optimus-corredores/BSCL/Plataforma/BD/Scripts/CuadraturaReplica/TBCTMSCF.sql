--TBCTMSCF
--CONTEO DE FACTURAS POR RUT CUENTA
select sebra.rut_cli,optimus.rut_cli,sebra.sec_rut_cli,optimus.sec_rut_cli,c1,c2
from
(
	select RUT_CLI,sec_rut_cli,count(1) as c1
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" WHERE FEC_ING_COMP ='2022-05-31'
	group by RUT_CLI,sec_rut_cli
) as optimus
,--LEFT JOIN
(
	select RUT_CLI,sec_rut_cli,count(1) as c2
	FROM "2022_05_31_TBCTMSCF" WHERE FEC_ING_COMP ='2022-05-31'
	group by RUT_CLI,sec_rut_cli
) AS sebra
WHERE  sebra.sec_rut_cli = optimus.sec_rut_cli
AND sebra.rut_cli = optimus.rut_cli
and c1 != c2

--DIFERENCIAS PORQUE ALGUNAS FACTURAS SE AGRUPARON

--INICIO CUADRE POR MERCADO
select sebra.mercado AS tipo_sebra, optimus.mercado AS tipo_optimus
,sebra.monto_com AS monto_com_sebra, optimus.monto_com AS monto_com_optimus,sebra.monto_ven AS monto_ven_sebra, optimus.monto_ven AS monto_ven_optimus
,sebra.monto_iva AS monto_iva_sebra, optimus.monto_iva AS monto_iva_optimus,sebra.monto_total AS monto_total_sebra, optimus.monto_total AS monto_total_optimus
from
(
	select mercado,
	sum(monto_com) as monto_com,
	sum(monto_ven) as monto_ven,
	sum(monto_iva) as monto_iva,
	sum(monto_total) as monto_total
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" WHERE FEC_ING_COMP ='2022-05-31' and rut_cli != '        1-9'
	and tipo_comp_fac != 'NC'
	group by mercado
) as optimus
left join
(
	select mercado,
	sum(monto_com) as monto_com,
	sum(monto_ven) as monto_ven,
	sum(monto_iva) as monto_iva,
	sum(monto_total) as monto_total
	FROM "2022_05_31_TBCTMSCF" WHERE FEC_ING_COMP ='2022-05-31' and rut_cli != '        1-9'
	and tipo_comp_fac != 'ND'
	group by MERCADO
) AS sebra on sebra.mercado = optimus.mercado
where (
abs(sebra.monto_com - optimus.monto_com) > 1
or abs(sebra.monto_ven - optimus.monto_ven) > 1
or abs(sebra.monto_iva - optimus.monto_iva) > 1
or abs(sebra.monto_total - optimus.monto_total) > 1
)

--FIN CUADRE POR MERCADO
--IGNORAMOS NC ANTICIPOS DE SIMULTANEAS ESTAN MALOS

select optimus.rut_cli,sebra.mercado AS tipo_sebra, optimus.mercado AS tipo_optimus
,sebra.monto_com AS monto_com_sebra, optimus.monto_com AS monto_com_optimus,sebra.monto_ven AS monto_ven_sebra, optimus.monto_ven AS monto_ven_optimus
,sebra.monto_iva AS monto_iva_sebra, optimus.monto_iva AS monto_iva_optimus,sebra.monto_total AS monto_total_sebra, optimus.monto_total AS monto_total_optimus
from
(
	select mercado,rut_cli,
	sum(monto_com) as monto_com,
	sum(monto_ven) as monto_ven,
	sum(monto_iva) as monto_iva,
	sum(monto_total) as monto_total
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" WHERE FEC_ING_COMP ='2022-05-31' and rut_cli != '        1-9'
	and tipo_comp_fac = 'NC'
	group by mercado,rut_cli
) as optimus
left join
(
	select mercado,rut_cli,
	sum(monto_com) as monto_com,
	sum(monto_ven) as monto_ven,
	sum(monto_iva) as monto_iva,
	sum(monto_total) as monto_total
	FROM "2022_05_31_TBCTMSCF" WHERE FEC_ING_COMP ='2022-05-31' and rut_cli != '        1-9'
	and tipo_comp_fac = 'ND'
	group by MERCADO,rut_cli
) AS sebra on sebra.mercado = optimus.mercado and sebra.rut_cli = optimus.rut_cli
WHERE abs(sebra.monto_com - optimus.monto_com) > 1
or abs(sebra.monto_ven - optimus.monto_ven) > 1
or abs(sebra.monto_iva - optimus.monto_iva) > 1
or abs(sebra.monto_total - optimus.monto_total) > 1

-- cuadro por mercado ignorado las NC, debido al error de las simultaneas de sebra
--76538390-0 descuadrado por el palo 161166, se descuadro al asignar a varios clientes
-- las NC fueron cuadradas por el equipo de implementacion y se cuadradon contra optimus