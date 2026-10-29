SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarFolioContratoSAAPV"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarFolioContratoSAAPV"  AS 

SELECT Contrato."IdContratoSAAPV"
	 , Contrato."FolioContratoSAAPV"
	 , Contrato."NombrePlan"
	 , Contrato."IdCliente"
	 , Contrato."IdPersona"
	 , MatrizAgrupador."Producto"
	 , MatrizAgrupador."IdPropositoPortafolio"
	 , Contrato."IdEstado"
	 , Contrato."IdMedioRecaudacionFormaPago"
	 , Portafolio."IdPortafolio"
	 , Contrato."DiaDescuento"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" Contrato
	LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato
				 ON PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				 ON Portafolio."IdPortafolio" = PortafolioContrato."IdPortafolio"
	LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
    			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE Portafolio."IdEstado" <> 'INA';

GRANT SELECT ON "BSCL.APV.BD.Vistas::SCS.ConsultarFolioContratoSAAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;