SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarIdContratoSAAPV"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarIdContratoSAAPV"  AS 

SELECT
	Contrato."NombreCliente" || ' ' || Contrato."ApellidoPaternoCliente" || ' ' || Contrato."ApellidoMaternoCliente"	AS "Nombres"
   ,Contrato."DocumentoIdentidadCliente"																				AS "RutCliente"
   ,Contrato."IdContratoSAAPV"																							AS "IdContratoSAAPV"
   ,Contrato."FolioContratoSAAPV"																						AS "FolioContratoSAAPV"
   ,PortafolioContrato."IdPortafolio"																					AS "Portafolio"
   ,Estado."Nombre"																										AS "Estado"
   ,PortafolioContrato."OrigenAhorro"																					AS "TipoContrato"
   ,Contrato."IdMedioRecaudacionFormaPago"
   ,MatrizAgrupador."Codigo"																							AS "Regimen"
   ,MedioRecaudacion."CodigoMedioRecaudacion"
   FROM 
	"BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
LEFT OUTER JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MedioRecaudacion
			 ON MedioRecaudacion."IdMedioRecaudacion" = Contrato."IdMedioRecaudacionFormaPago"
LEFT OUTER JOIN 
	"BSCL.APV.BD::APV.Estado" AS Estado 
ON  Estado."Codigo" = Contrato."IdEstado"
LEFT OUTER JOIN
	"BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
ON  PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
				AND Portafolio."IdCliente" = Contrato."IdCliente"
    			AND Portafolio."Proposito" = 'PROPOSITOAPV'
				AND Portafolio."IdEstado" <> 'INA'
LEFT OUTER JOIN (SELECT MAT."IdMatrizAgrupadorAPV"
						 , REG."Codigo"
					FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
					INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS REG
							ON REG."IdRegimenTributario" = MAT."IdRegimenTributario"
					INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador
							ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
						   AND ProductoAgrupador."IdEstado" = 'VIG'
					INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" AS PlanTipoInversion
							ON PlanTipoInversion."IdPlanTipoInversion" = MAT."IdPlanTipoInversion"
						   AND PlanTipoInversion."IdEstado" = 'VIG'
					WHERE MAT."IdEstado" = 'VIG'	
					ORDER BY MAT."IdMatrizAgrupadorAPV"
	) AS MatrizAgrupador
    			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"	
WHERE
	Estado."Nombre" = 'VIGENTE' AND PortafolioContrato."OrigenAhorro" IN ('APV','DC');
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarIdContratoSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 