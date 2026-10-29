SELECT
	"Contrato"."IdPersona"
   ,"Contrato"."IdCliente"
   ,"Contrato"."IdEstado"
   ,"Contrato"."IdEstadoSolicitud"
   ,"Contrato"."FolioContratoSAAPV"
   ,"Contrato"."IdPlan"
   ,IFNULL("Contrato"."IdMedioRecaudacionFormaPago",0) AS "IdMedioRecaudacionFormaPago"
   ,"Contrato"."IdMedioRecaudacionTrabajador"
   ,"RelPortafolioContrato"."Pago"
   ,"Portafolio"."CodigoPortafolio"
   ,"Portafolio"."IdMatrizAgrupadorAPV"
   ,TO_BIGINT("MatrizAPV"."IdProductoAgrupador") AS "IdProductoAgrupador"
   ,"MatrizAPV"."IdRegimenTributario"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS "Contrato"
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS "RelPortafolioContrato" ON "RelPortafolioContrato"."IdContratoSAAPV" = "Contrato"."IdContratoSAAPV"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "Portafolio" ON "RelPortafolioContrato"."IdPortafolio" = "Portafolio"."IdPortafolio"
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS "MatrizAPV" ON "MatrizAPV"."IdMatrizAgrupadorAPV" = "Portafolio"."IdMatrizAgrupadorAPV"
WHERE
	"Contrato"."IdEstado" = 'VIG'
AND "Contrato"."IdEstadoSolicitud" = 'APR'
AND "Contrato"."IdCliente" = :p_IdCliente
AND "Portafolio"."IdEstado" <> 'INA';