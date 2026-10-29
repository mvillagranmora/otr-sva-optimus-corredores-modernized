SET SCHEMA "BSCL";
GO 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTemplateFacturacion";
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTemplateFacturacion"  As
SELECT    
	Tf."IdTemplateFacturacion",
	Tf."CodigoTemplateFacturacion",
	Tem."Id" As "IdReporte",
	Upper(Tf."NombreReporte") AS "NombreReporte",
	Tf."IdServicio",
	Tf."IdTipoComprobante",
	Ser."RutaServicio",
	Tf."Descripcion",
	Tf."IdEmpresa",
	Tf."IdEstado",	
	Tem."ReportName",
	Tem."ReportFile",
	Tem."ReportVersion",	
	Tf."FormatoSalida",  -- txt/pdf
	Tf."LocalizadorSistema"

From "BSCL.Facturacion.BD::FAC.TemplateFacturacion" As Tf
  Inner Join "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" As Tem 
     On (UPPER(Tem."ReportName") = UPPER(Tf."NombreReporte")
    And Tf."IdEmpresa" = TEM."CompanyId")
  Inner Join "BSCL.Plataforma.BD::PLA.Servicio" As Ser 
     On (Ser."IdServicio" = Tf."IdServicio")

WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTemplateFacturacion"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO