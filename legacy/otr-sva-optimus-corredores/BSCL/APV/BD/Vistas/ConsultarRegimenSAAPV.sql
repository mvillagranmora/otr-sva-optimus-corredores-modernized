SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.APV.BD.Vistas::ConsultarRegimenSAAPV";
GO

CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarRegimenSAAPV" AS
SELECT
	CON."IdCliente"
   ,CON."IdPersona"
   ,CON."DocumentoIdentidad"
   ,CON."TipoCuenta"
   ,CON."IdContratoSAAPV"
   ,CON."IdPortafolio"
   ,CON."IdPropositoPortafolio"
   ,CON."IdMatrizAgrupador"
   ,CON."CodigoAgrupador"
   ,CON."OrigenAhorro"
   ,CON."MesInicio"
   ,CON."MesTermino"
   ,CON."Pago"
   ,CON."TipoRecaudacion"
   ,CON."PorcentajeRegimenImponible"
   ,CON."Producto"
   ,CON."IdRegimen"
   ,CON."Regimen"
   ,CON."Cuenta"
   ,CON."MontoTotal"
   ,CON."Moneda"
FROM
(
	SELECT
		ContratoSAAPV."IdCliente"																AS "IdCliente"
	   ,ContratoSAAPV."IdPersona"																AS "IdPersona"
	   ,DocumentoIdentidad."DocumentoIdentidad"													AS "DocumentoIdentidad"
	   ,RegimenTributario."Codigo"																AS "TipoCuenta"
	   ,ContratoSAAPV."IdContratoSAAPV"															AS "IdContratoSAAPV"
	   ,Portafolio."IdPortafolio"																AS "IdPortafolio"
	   ,Portafolio."Proposito"																	AS "IdPropositoPortafolio"
	   ,MatrizAgrupador."IdProductoAgrupador"													AS "IdProductoAgrupador"
	   ,Portafolio."IdMatrizAgrupadorAPV"														AS "IdMatrizAgrupador"
	   ,MatrizAgrupador."CodigoAgrupador"														AS "CodigoAgrupador"
	   ,PortafolioContratoSAAPV."OrigenAhorro"													AS "OrigenAhorro"
	   ,PortafolioContratoSAAPV."PeriodoInicio"													AS "MesInicio"
	   ,PortafolioContratoSAAPV."PeriodoTermino"												AS "MesTermino"
	   ,PortafolioContratoSAAPV."Pago"															AS "Pago"
	   ,MAP(PortafolioContratoSAAPV."Pago",'IND','INDEFINIDO','TEM','TEMPORAL','UNI','UNICO')	AS "TipoRecaudacion"
	   ,PortafolioContratoSAAPV."PorcentajeRegimenImponible"									AS "PorcentajeRegimenImponible"
	   ,ProductoAgrupador."NombreProductoAgrupador"												AS "Producto"
	   ,RegimenTributario."IdRegimenTributario"													AS "IdRegimen"
	   ,RegimenTributario."Codigo"																AS "Regimen"
	   ,SUBSTR_BEFORE(RegimenTributario."Codigo",'-')											AS "Cuenta"
	   ,CASE
	   		WHEN PortafolioContratoSAAPV."MontoPesos" > 0 THEN PortafolioContratoSAAPV."MontoPesos"
	   		WHEN PortafolioContratoSAAPV."MontoUF" > 0 THEN PortafolioContratoSAAPV."MontoUF"
	   		ELSE NULL
	   	END																						AS "MontoTotal"
	   ,CASE
	   		WHEN PortafolioContratoSAAPV."MontoPesos" > 0 THEN 'CLP'
	   		WHEN PortafolioContratoSAAPV."MontoUF" > 0 THEN 'UF'
	   		ELSE NULL
	   END																						AS "Moneda"
	FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS ContratoSAAPV
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = ContratoSAAPV."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContratoSAAPV ON PortafolioContratoSAAPV."IdContratoSAAPV" = ContratoSAAPV."IdContratoSAAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PortafolioContratoSAAPV."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario ON RegimenTributario."IdRegimenTributario" = MatrizAgrupador."IdRegimenTributario"
	LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	WHERE Portafolio."IdEstado" <> 'INA'
)
AS CON
ORDER BY
	CON."IdContratoSAAPV"
   ,CON."IdPortafolio";
GO

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarRegimenSAAPV" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO