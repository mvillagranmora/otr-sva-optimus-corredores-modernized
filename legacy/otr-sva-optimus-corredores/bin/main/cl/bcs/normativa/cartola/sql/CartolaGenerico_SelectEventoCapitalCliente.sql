select sum("Total") as "GananciaPerdida" from "BSCL.Custodia.BD::CUS.EventoCapital" as "a"
join "BSCL.Custodia.BD::CUS.EventoCapitalCliente" as "b"
on ( "a"."Nemo" = :IdNemo and "a"."IdEventoCapital" = "b"."IdEventoCapital" )
where  "b"."IdPortafolio"  = :IdPortafolio AND "a"."IdEstado" = 'PRO'
and "a"."IdClasificacionTipoInstrumento" = 'RV' and "b"."FechaPago" >= :FechaDesde;