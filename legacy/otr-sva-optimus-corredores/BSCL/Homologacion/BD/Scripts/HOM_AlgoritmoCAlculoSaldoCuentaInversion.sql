
SET SCHEMA "BSCL";
--- Recibo tabla
DROP TABLE #Entrada;
DROP TABLE #SALIDA;

CREATE LOCAL TEMPORARY TABLE #Entrada ("Moneda" NVARCHAR(3), "tipo" NVARCHAR(1), "Fecha" DATE, "Monto" decimal(26,8), "Saldo"  decimal(26,8));

insert into #Entrada values ('CLP','A','2016-01-01',100,0);
insert into #Entrada values ('CLP','C','2016-01-02',90,0);
insert into #Entrada values ('CLP','A','2016-01-03',200,0);
insert into #Entrada values ('CLP','A','2016-01-04',40,0);
insert into #Entrada values ('CLP','C','2016-01-05',50,0);
insert into #Entrada values ('USD','A','2016-01-01',100,0);
insert into #Entrada values ('USD','C','2016-01-02',90,0);
insert into #Entrada values ('USD','A','2016-01-03',200,0);
insert into #Entrada values ('USD','A','2016-01-04',40,0);
insert into #Entrada values ('USD','C','2016-01-05',50,0);



CREATE LOCAL TEMPORARY TABLE #SALIDA ("ID" BIGINT, "Moneda" NVARCHAR(3), "tipo" NVARCHAR(1), "Fecha" DATE, "Monto" decimal(26,8), "Saldo"  decimal(26,8));

/*
update #Entrada 
set "Saldo" = (	select sum("Monto" * (case when "tipo" = 'A' then 1 else -1 end)) 
				from #Entrada as t2 
				where #Entrada."Fecha" < t2."Fecha"
			   );
*/

insert into #SALIDA
SELECT   ROW_NUMBER () OVER (order by "Fecha") as "ID", "Moneda", "tipo", "Fecha", "Monto", "Saldo"
 FROM #Entrada;
			
select * from #SALIDA order by "Moneda","ID";			
/*
select t1.*, ifnull((select sum("Monto" * (case when t2."tipo" = 'A' then 1 else -1 end)) from #SALIDA t2 where t1."ID" > t2."ID") ,0) + (t1."Monto" * (case when t1."tipo" = 'A' then 1 else -1 end))
from #SALIDA t1
order by 1
*/

update #SALIDA 
set "Saldo" = 
				ifnull((select sum("Monto" * (case when t2."tipo" = 'A' then 1 else -1 end)) from #SALIDA t2 where t1."ID" > t2."ID" and t1."Moneda" = t2."Moneda") ,0) 
				+ (t1."Monto" * (case when t1."tipo" = 'A' then 1 else -1 end))
				
from #SALIDA as t1

select * from #SALIDA order by "Moneda","ID";			
