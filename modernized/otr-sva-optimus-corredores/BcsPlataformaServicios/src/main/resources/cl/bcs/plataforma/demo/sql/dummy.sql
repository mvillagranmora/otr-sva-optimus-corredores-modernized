--Probando comentario

/* 

probando segundo comentario

Se pasan parametros utilizando :NombreParametro

*/

select A,B,C,D,E,F from (select 1 as A,2 as B,'SOY TEXTO' as C,NOW() as D,TO_DATE('2010-01-12', 'YYYY-MM-DD') as E,12.1234567890 as F from dummy) as TT
where 
TT.B = :B AND TT.C IN ( :LISTA_C )
union all 
select 11 as A,22 as B,'SOY TEXTO3' as C,NOW() as D,null as E,12.123456789011 as F from dummy
