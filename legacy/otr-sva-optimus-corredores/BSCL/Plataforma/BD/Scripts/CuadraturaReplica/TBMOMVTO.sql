select * from "2022_05_31_TBMOMVTO" t  where FEC_MVTO >= '2022-05-31';

select * from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  where FEC_MVTO >= '2022-05-31'

and rut_cli = ' 97036000-K';

--ESTADO L LIQUIDADO EN SPOT ESTA MALO REVISAR FMMOVTOSSPOT

--CUADRE DE CANTIDAD DE OPERACIONES POR RUT CUENTA
select *
from 
(
	select rut_cli,sec_rut_cli,count(1) as c 
	from "2022_05_31_TBMOMVTO" t 
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
) as sebra
right join 
(
	select rut_cli,sec_rut_cli,count(1) as c from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
)  as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli
where optimus.c != sebra.c

--cuadre monto_dolares
select *
from 
(
	select rut_cli,sec_rut_cli,sum(monto_dolares) as c 
	from "2022_05_31_TBMOMVTO" t 
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
) as sebra
right join 
(
	select rut_cli,sec_rut_cli,sum(monto_dolares) as c from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
)  as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli
where optimus.c != sebra.c


--cuadre monto_pesos
select *
from 
(
	select rut_cli,sec_rut_cli,sum(monto_pesos) as c 
	from "2022_05_31_TBMOMVTO" t 
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
) as sebra
right join 
(
	select rut_cli,sec_rut_cli,sum(monto_pesos) as c from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
)  as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli
where optimus.c != sebra.c

--cuadre tasa_agente
select *
from 
(
	select rut_cli,sec_rut_cli,sum(tasa_agente) as c 
	from "2022_05_31_TBMOMVTO" t 
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
) as sebra
right join 
(
	select rut_cli,sec_rut_cli,sum(tasa_agente) as c from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
)  as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli
where optimus.c != sebra.c

--no cuadra porque no se ingresaron las mismas puntas

--cuadre tasa_cambio
select *
from 
(
	select rut_cli,sec_rut_cli,sum(tasa_cambio) as c 
	from "2022_05_31_TBMOMVTO" t 
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
) as sebra
right join 
(
	select rut_cli,sec_rut_cli,sum(tasa_cambio) as c from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
)  as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli
where optimus.c != sebra.c

--cuadre cantidad_origen
select *
from 
(
	select rut_cli,sec_rut_cli,sum(cantidad_origen) as c 
	from "2022_05_31_TBMOMVTO" t 
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
) as sebra
right join 
(
	select rut_cli,sec_rut_cli,sum(cantidad_origen) as c from "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" brbvst  
	where FEC_MVTO >= '2022-05-31' and estado != 'N'
	group by rut_cli,sec_rut_cli
)  as optimus on optimus.rut_cli = sebra.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli
where optimus.c != sebra.c


--sec_movto_fac
--tipo_comp_adj

select distinct TIPO_COMP_ADJ  from "2022_05_31_TBMOMVTO" t 

select * from "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot" brbvsfs where FOLIOTRANSACCION  = 586975
