select COALESCE("ObligacionesTasaPapel",0)  AS "ObligacionesTasaPapel",
       COALESCE("DerechosTasaPapel",0) AS "DerechosTasaPapel",
       COALESCE("ObligacionesTasaPactos",0) AS "ObligacionesTasaPactos",
       COALESCE("DerechosTasaPactos",0) AS "DerechosTasaPactos",
       COALESCE("ObligacionesTasaPapelMenores8Dias",0) AS "ObligacionesTasaPapelMenores8Dias",
       COALESCE("DerechosTasaPapelMenores8Dias",0) AS "DerechosTasaPapelMenores8Dias",
       COALESCE("ObligacionesTasaPactosMenores8Dias",0) AS "ObligacionesTasaPactosMenores8Dias",
       COALESCE("DerechosTasaPactosMenores8Dias",0) AS "DerechosTasaPactosMenores8Dias"
       from (
select sum(case "IdTipoOperacion" when 'COMPAC' then suby."INGMontoATasaCompraActual" else 0 end) as "ObligacionesTasaPapel",
       sum(case "IdTipoOperacion" when 'VENPAC' then suby."INGMontoATasaCompraActual" else 0 end) as "DerechosTasaPapel"
from "BSCL.Custodia.BD::CUS.Cartera" car
inner join "BSCL.Custodia.BD::CUS.Subyacente" suby on (suby."FolioCarteraRelacionada" = car."FolioCarteraRelacionada")
where car."IdTipoInstrumento" = 'PAC' and car."IdEstado"='VIG' AND suby."IdTipoReajuste" = 'USD')
CROSS JOIN
--col 3 y 4
(select sum(case "IdTipoOperacion" when 'COMPAC' then "INGMontoATasaCompraActual" else 0 end) as "ObligacionesTasaPactos",
        sum(case "IdTipoOperacion" when 'VENPAC' then "INGMontoATasaCompraActual" else 0 end) as "DerechosTasaPactos"
from "BSCL.Custodia.BD::CUS.Cartera" car
where car."IdTipoInstrumento" = 'PAC' and car."IdEstado"='VIG' AND car."IdTipoReajuste" = 'USD')
CROSS JOIN
--col 6 y 7
(select sum(case "IdTipoOperacion" when 'COMPAC' then "INGMontoATasaCompraActual" else 0 end) as "ObligacionesTasaPapelMenores8Dias",
        sum(case "IdTipoOperacion" when 'VENPAC' then "INGMontoATasaCompraActual" else 0 end) as "DerechosTasaPapelMenores8Dias"
from "BSCL.Custodia.BD::CUS.Cartera" car
where car."IdTipoInstrumento" = 'PAC' and car."IdEstado"='VIG'
and (car."FechaVencimientoOperacion" between TO_DATE(:FechaContable,'YYYY-MM-DD') and TO_DATE(:FechaHasta,'YYYY-MM-DD')) AND car."IdTipoReajuste" = 'USD')
CROSS JOIN
--col 5 y 8
(select sum(case "IdTipoOperacion" when 'COMPAC' then car."INGMontoATasaCompraActual" else 0 end) as "ObligacionesTasaPactosMenores8Dias",
        sum(case "IdTipoOperacion" when 'VENPAC' then car."INGMontoATasaCompraActual" else 0 end) as "DerechosTasaPactosMenores8Dias"
from "BSCL.Custodia.BD::CUS.Cartera" car
inner join "BSCL.Custodia.BD::CUS.Subyacente" suby on (suby."FolioCarteraRelacionada" = car."FolioCartera")
where car."IdTipoInstrumento" = 'PAC' and car."IdEstado"='VIG'
and (car."FechaVencimientoOperacion" between TO_DATE(:FechaContable,'YYYY-MM-DD') and TO_DATE(:FechaHasta,'YYYY-MM-DD')) AND car."IdTipoReajuste" = 'USD')