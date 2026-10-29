select trim(f."FOLIO_TRANS"),SUBSTR(f.FEC_TRANS,0,10),T."IdTransaccion",T."Folio",T."FechaSistemaCreacion",t."Nemotecnico",f.nemo,t."Cantidad",f.cantidad,
t."CodigoOperadorCorredor",f.COD_OPE_EXT,f.OPE_INT
from BSCL_VIRTUAL."GES_TBPLTBRV" f
left join BSCL."BSCL.Orden.BD::ORD.Transaccion" t on trim(f."FOLIO_TRANS") = T."Folio" and SUBSTR(f.FEC_TRANS,0,10) = T."FechaSistemaCreacion"
where to_date(SUBSTR(f.FEC_TRANS,0,10)) >= '2019-10-01'

commit

update t
set "CodigoOperadorCorredor" = f.COD_OPE_EXT
from BSCL."BSCL.Orden.BD::ORD.Transaccion" t
inner join BSCL_VIRTUAL."GES_TBPLTBRV" f on trim(f."FOLIO_TRANS") = T."Folio" and SUBSTR(f.FEC_TRANS,0,10) = T."FechaSistemaCreacion"
where to_date(SUBSTR(f.FEC_TRANS,0,10)) >= '2019-10-01' 

select * from BSCL."BSCL.Orden.BD::ORD.Transaccion" where "Folio" = 210433

select "FechaSistemaCreacion",count(1) 
from BSCL."BSCL.Orden.BD::ORD.Transaccion" 
group by "FechaSistemaCreacion" order by "FechaSistemaCreacion"


alter sequence bscl."BSCL.Orden.BD.Secuencias::Transaccion" restart with 86981
commit