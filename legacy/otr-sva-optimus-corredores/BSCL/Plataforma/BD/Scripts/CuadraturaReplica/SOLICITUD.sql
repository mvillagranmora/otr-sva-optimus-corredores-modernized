select * from "2022_05_31_SOLICITUD" s where SOL_FECHA >= '2022-05-31 00:00:00' order by SOL_FECHA desc

select distinct OPE_ID from "2022_05_31_SOLICITUD" s where SOL_FECHA >= '2022-05-31 00:00:00'
IFVE
IFCO
ACCO
ACVE
ACCS
ENCO
CAAB
CACA

select * from "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" brbvss where SOL_FECHA >= '2022-05-31 00:00:00' order by SOL_FECHA desc

select distinct ope_id from "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" brbvss where SOL_FECHA >= '2022-05-31 00:00:00'
ACVE
ACCO
IFCO
ACCS
ENCO
RFCO
RFVE
IFVE

select * from "2022_05_31_SOLICITUD" s 
where SOL_FECHA >= '2022-05-31 00:00:00' 
and PER_ID = 13933720
and MERCADO = 'AC'
order by SOL_FECHA desc

select *
from "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" sol
where SOL_FECHA >= '2022-05-31 00:00:00'
and PER_ID = 13933720
and MERCADO = 'AC'
order by SOL_FECHA desc

--CUADRAMOS ORDENES POR MERCADO Y PERID

select sebra.*,opt.* from 
(
	select per_id,mercado,count(1) as c1
	from "2022_05_31_SOLICITUD" s 
	where SOL_FECHA >= '2022-05-31 00:00:00'
	group by PER_ID , mercado
) as sebra
left join 
(
	select per_id,mercado,count(1) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" s 
	where SOL_FECHA >= '2022-05-31 00:00:00' and sol_estado != 'CA'
	group by PER_ID , mercado
) as opt on opt.per_id = sebra.per_id and sebra.mercado = opt.mercado
where c1 != c2

--NO HAY CUADRE DE 94 CASOS AL PARECER NO SE DIGITARON IGUAL QUE EN SEBRA

--OPERACIONES CACA Y CAAB NO ESTAN HOMOLOGADAS YA QUE EN OPTIMUS NO EXISTE LA SOLICITUD DE TESORERIA COMO CRM

select distinct mercado from "2022_05_31_SOLICITUD" s 