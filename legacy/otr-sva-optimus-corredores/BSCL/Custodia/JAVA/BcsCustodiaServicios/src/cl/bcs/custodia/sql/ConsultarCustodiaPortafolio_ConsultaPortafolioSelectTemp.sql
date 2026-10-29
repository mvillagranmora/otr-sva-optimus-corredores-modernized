select
 PORT."IdPortafolio"
,PORT."IdMatrizAgrupadorAPV"
,PORT."CodigoPortafolio"
,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "CodigoPropositoPortafolio"
from  "BSCL.Custodia.BD::CUS.ConsultarCustodiaPortafolio_Portafolios" p
left join "BSCL.Portafolio.BD::POR.Portafolio" PORT on p."IdPortafolio" = PORT."IdPortafolio"
WHERE "IdTrabajo" = :IdTrabajo

