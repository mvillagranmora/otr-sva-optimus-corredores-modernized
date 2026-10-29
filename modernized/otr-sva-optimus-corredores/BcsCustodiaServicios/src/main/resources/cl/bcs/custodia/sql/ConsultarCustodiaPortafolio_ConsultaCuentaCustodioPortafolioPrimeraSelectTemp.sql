SELECT
cc."IdPortafolio" || cc."IdBoveda" AS "Id"
,cc."IdPortafolio" as "IdPortafolio"
,cc."IdBoveda" as "IdBoveda"
,cc."IdCuentaCustodioPortafolio" as "IdCuentaCustodioPortafolio"
,cc."Codigo" as "Codigo"
FROM  "BSCL.Custodia.BD::CUS.ConsultarCustodiaPortafolio_Portafolios" por
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioPrimera" cc on por."IdPortafolio" = cc."IdPortafolio"
WHERE "IdTrabajo" = :IdTrabajo and cc."IdPortafolio" is not null