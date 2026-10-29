SELECT * FROM BSCL.TBCPMSPC where cod_per = '2021-03' AND estado in ('A','E') AND FEC_LIQUID > '2021-03-31';

SELECT cod_per,estado,fec_liquid,* FROM "BSCL"."BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC" OPT where cod_per = '2021-04' AND estado in ('A','E') AND FEC_LIQUID > '2021-03-31';


SELECT SEBRA.VALOR_PTE,OPT.VALOR_PTE,* FROM BSCL.TBCPMSPC sebra
LEFT JOIN "BSCL"."BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC" OPT ON UPPER(sebra.RUT_CLI) = opt.RUT_CLI AND sebra.SEC_RUT_CLI = opt.sec_rut_cli
AND sebra.CANTIDAD_CAPITAL = opt.CANTIDAD_CAPITAL AND sebra.FEC_VCTO_COM = opt.fec_vcto_com AND sebra.TIPO_COMPROM = opt.tipo_comprom
where sebra.cod_per = '2021-03' AND sebra.estado in ('A','E') AND sebra.FEC_LIQUID > '2021-03-31';;}

SELECT * FROM "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactos";