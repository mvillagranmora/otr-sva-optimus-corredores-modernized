SET SCHEMA BSCL GO
--CONFIGURACIONES CAMPOS EN ENVIO A CONTABILIDAD

DELETE FROM "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap" GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','TaxDate','FechaComprobante') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Portafolio','Portafolio') GO
	
--Centralizacion
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Comprobante','TipoComprobante') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Folio','Folio') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_CV','') GO--cortar por primer caracter
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Nemo','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('200','U_BCS_Relacion','Relacion') GO
---------------------------------------------------------------------------------------------------------------
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','TaxDate','FechaComprobante') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Portafolio','Portafolio') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Comprobante','TipoComprobante') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Folio','Folio') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Nemo','Nemotecnico') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('205','U_BCS_Relacion','Relacion') GO
------------------------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','TaxDate','FechaMovimiento') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','CostingCode2','CodigoProducto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Portafolio','Portafolio') GO
	
--Centralizacion
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_EmitidoA','EmitidoA') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Comprobante','ComprobanteTipo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Folio','Folio') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_NDcto','NumeroDocumentoPago') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_CV','') GO--cortar por primer caracter
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Nemo','Nemotecnico') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_CodigoProducto','CodigoProducto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('210','U_BCS_Relacion','Relacion') GO
----------------------------------------------------------------------ACA VOY----------------------------------------------
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','TaxDate','FechaDocumento') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_EmitidoA','EmitidoA') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Comprobante','ComprobanteTipo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Folio','Folio') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_NDcto','NumeroDocumentoPago') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Nemo','Nemotecnico') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('215','U_BCS_Relacion','Relacion') GO

----------------------------------------------------------------------------------------------------------
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Portafolio','Portafolio') GO
	
--Centralizacion
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('220','U_BCS_Relacion','Relacion') GO
-------------------------------------------------------------------------------------OK

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('225','U_BCS_Relacion','Relacion') GO

-----------------------------------------------------------------------------------ok

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Rut','DocumentoIdentidad') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Portafolio','PortafolioCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('230','U_BCS_Relacion','Relacion') GO
----------------------------------------------------------------OK	


INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_CV','') GO--cortar por primer caracter
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('235','U_BCS_Relacion','Relacion') GO

--------------------------------------------------------------------------------------------ACA VOY
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('240','U_BCS_Relacion','Relacion') GO
----------------------------------------------------------------------------------ACA VOY

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','DueDate','FechaLiquidacion') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('241','U_BCS_Relacion','Relacion') GO
------------------------------------------------------------------------------------aca voy

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','DueDate','FechaLiquidacion') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_CV','') GO--cortar por primer caracter
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('242','U_BCS_Relacion','Relacion') GO

---------------------------------------------------------------------------------------aca voy

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Portafolio','Portafolio') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('245','U_BCS_Relacion','Relacion') GO

----------------------------------------------------------------------------------------------ACA VOY

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Concepto',Concepto'') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('250','U_BCS_Relacion','Relacion') GO
----------------------------------------------------------------------------------------------------ACA Voy	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','CostingCode','CentroCosto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','CostingCode2','ProductoMovimiento') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','CostingCode4','CodigoBolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Portafolio','Portafolio') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Nombre','') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Comprobante','TipoCmbteCont') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Folio','FolioCmbteCont') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_CodigoProducto','ProductoMovimiento') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Bolsa','CodigoBolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('255','U_BCS_Relacion','Relacion') GO
-----------------------------------------------------------------------------ok	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('260','U_BCS_Relacion','Relacion') GO
----------------------------------------------------------------------------------------------aca voy	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('265','U_BCS_Relacion','Relacion') GO

-----------------------------------------------------------------------------------------aca voy
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_CV','') GO--cortar por primer caracter
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('270','U_BCS_Relacion','Relacion') GO

-------------------------------------------------------------------------------------------ok	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','DueDate','FechaLiquidacion') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('275','U_BCS_Relacion','Relacion') GO

---------------------------------------------------------------------------------------OK	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','DueDate','FechaLiquidacion') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','TaxDate','FechaLiquidacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','CostingCode2','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Tipopersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_CodigoProducto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('280','U_BCS_Relacion','Relacion') GO

-------------------------------------------------------------------------------------------------- OK

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','DueDate','FechaSistema') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','TaxDate','FechaSistema') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_TipoPersona','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Portafolio','Portafolio') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_CV','') 
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Nemo','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('285','U_BCS_Relacion','Relacion') GO

----------------------------------------------------------------------------------------- OK

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','DueDate','Fecha') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','TaxDate','Fecha') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Portafolio','Portafolio') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_CV','CompraVenta') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Nemo','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('290','U_BCS_Relacion','Relacion') GO
	
----------------------------------------------------------------------------------------------------ok
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','DueDate','FechaLiquidacion') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('295','U_BCS_Relacion','Relacion') GO

-----------------------------------------------------------------------------------------------------ok	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','DueDate','') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','CostingCode','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','CostingCode4','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_TipoPersona','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Bolsa','Bolsa') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('300','U_BCS_Relacion','Relacion') GO

--------------------------------------------------------------------------------------------------- OK

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','DueDate','') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','CostingCode','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_TipoPersona','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('305','U_BCS_Relacion','Relacion') GO
-----------------------------------------------------------------------------------ok

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','DueDate','') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','CostingCode','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_TipoPersona','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('310','U_BCS_Relacion','Relacion') GO
	
---------------------------------------------------------------------------------------------ok

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','DueDate','') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','CostingCode','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_TipoPersona','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Rut','DocumentoIdentidad') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Portafolio','PortafolioCliente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('315','U_BCS_Relacion','Relacion') GO
-----------------------------------------------------------------------------------------------ok

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','DueDate','') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Rut','DocumentoIdentidad') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Portafolio','PortafolioCliente') GO
	

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Comprobante','TipoComprobante') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_CV','CompraVenta') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('320','U_BCS_Relacion','Relacion') GO
--------------------------------------------------------------------------------------------------OK

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','DueDate','') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Rut','DocumentoIdentidad') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Portafolio','PortafolioCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Comprobante','TipoComprobante') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_CV','CompraVenta') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Nemo','Nemotecnico') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('325','U_BCS_Relacion','Relacion') GO
	
-----------------------------------------------------------------------------------------ok
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','DueDate','FechaLiquidacion') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Rut','DocumentoIdentidad') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Nombre','NombreCliente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Portafolio','PortafolioCliente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Concepto','Concepto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_IFRSCtaPropia','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','U_BCS_Relacion','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('330','TasaCambio','TasaCambio') GO

----------------------------------------------------------------------------------------------- OK

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','DueDate','FechaLiquidacion') GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','TaxDate','FechaOperacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','CostingCode','CentroCostoAgente') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','CostingCode2','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','CostingCode3','Relacion') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','CostingCode4','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','CostingCode5','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_TipoPersona','TipoPersona') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Rut','DocumentoIdentidad') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Portafolio','PortafolioCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Nombre','NombreCliente') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_CtaDCV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_EmitidoA','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Comprobante','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Folio','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_NDcto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Concepto','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_CV','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Nemo','Nemo') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_IFRSCtaPropia','CodigoIFRS') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_CodigoProducto','Producto') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Bolsa','') GO
	
INSERT INTO "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
("IdTipoCentralizacion","CampoSAP","CampoTipoCentralizacion") 
	VALUES('340','U_BCS_Relacion','Relacion') GO
	
	------------------------------------------------------------------------------------ok
GO

--OTROS VARIOS

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" VALUES('EsContraparte','V','N',0,0,0,'','S','V') GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo" 
SELECT distinct "IdTipoMovimiento",'EsContraparte' ,'V' FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" VALUES('Relacion','V','N',0,0,0,'','S','V') GO

insert into "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo" 
SELECT distinct "IdTipoMovimiento",'Relacion' ,'V' FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo"
GO


-- Tabla que define codigo "Relacion"
CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ParametrosCodigoRelacionSAP"(
  "IdEmpresa"                  			BIGINT 	 		 NOT NULL,
  "CodigoOptimus"            			NVARCHAR(10)     NOT NULL,
  "CodigoSAP"    						NVARCHAR(10)     NOT NULL,
  "Descripcion"    						NVARCHAR(50)     NULL,
  
  PRIMARY KEY ("IdEmpresa","CodigoOptimus")
)
GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.ParametrosCodigoRelacionSAP" VALUES(1,'CP','100','Cuenta Propia') GO
INSERT INTO "BSCL.Contabilidad.BD::ITZ.ParametrosCodigoRelacionSAP" VALUES(1,'RE','101','Relacionado') GO
INSERT INTO "BSCL.Contabilidad.BD::ITZ.ParametrosCodigoRelacionSAP" VALUES(1,'NR','102','Tercero') GO

GO

-- Para hacer visibles los costing code en UI
insert into "BSCL.Contabilidad.BD::ITZ.EventoTipoDato"
("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado") 
values('CostingCode','V','N',0,0,0,'','N','V') GO

insert into "BSCL.Contabilidad.BD::ITZ.EventoTipoDato"
("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado") 
values('CostingCode2','V','N',0,0,0,'','N','V') GO

insert into "BSCL.Contabilidad.BD::ITZ.EventoTipoDato"
("IdTipoDatoEvento","TipoDato","ValorLibre","LargoVarchar","Enteros","Decimales","FormatoEdicion","DatosPreDefinidos","Estado") 
values('CostingCode3','V','N',0,0,0,'','N','V') GO

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ConfiguracionParametros"(
  "Codigo"            					NVARCHAR(40)     NOT NULL,
  "Descripcion"    						NVARCHAR(100)    NOT NULL,
  "Valor"                  				NVARCHAR(100)  	 NOT NULL,--A, 
  "EsModificableUsuario"            	NVARCHAR(1)      NOT NULL,
  "Uso"    								NVARCHAR(5000)   NOT NULL,
  "ValorDefecto"        				NVARCHAR(5000)	 NOT NULL,
  "ValoresPosibles"            			NVARCHAR(5000)   NOT NULL,--
  "ModificablePorUsuario"    			NVARCHAR(512)    NOT NULL,
  "ModificablePostProduccion"        	NVARCHAR(1)  	 NOT NULL,
  "CreadorParametro"            		NVARCHAR(512)    NOT NULL,
  "FechaCreacion"    					SECONDDATE   NOT NULL DEFAULT CURRENT_UTCTIMESTAMP,
  "ModificadorParametro"                NVARCHAR(512)  	 NOT NULL,
  "FechaModificacion"            		SECONDDATE   NOT NULL DEFAULT CURRENT_UTCTIMESTAMP,
  "Observaciones"    					NVARCHAR(5000)   NOT NULL,
  "IdEmpresa"                  			BIGINT 	 		 null,
  
  PRIMARY KEY ("Codigo")
);

GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.ConfiguracionParametros"
("Codigo","Descripcion","Valor","EsModificableUsuario","Uso",
"ModificablePostProduccion","CreadorParametro","ModificadorParametro",
"Observaciones","IdEmpresa","ValorDefecto","ValoresPosibles","ModificablePorUsuario") 
VALUES
('FormaEnvioSAP','A:Enviar FDebit y FCredit, y Moneda; B:Enviar Debit,Credit,FDebit,FCredit y FCurrency','A','N','','','GB','','',1,'A','A/B','')

GO

COMMIT

GO

--new


ALTER TABLE "BSCL.Contabilidad.BD::ITZ.MovimientoDato"
  ADD (	"TipoEvento" NVARCHAR(25) DEFAULT '' NOT NULL);
GO

ALTER TABLE "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_PROC_DatosMovimiento"
  ADD (	"TipoEvento" NVARCHAR(25) DEFAULT '' NOT NULL);
GO  
  
DROP TABLE "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"
  
GO
CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"(
  "CodigoBO"    						NVARCHAR(60)   NOT NULL,
  "ProductoConcepto"                  	NVARCHAR(60)   NOT NULL,
  "Relacionado"            				NVARCHAR(60)   NOT NULL,
  "Tipo"    							NVARCHAR(60)   NOT NULL,
  "Bolsa"        						NVARCHAR(60)   NOT NULL,
  "Proceso"            					NVARCHAR(60)   NOT NULL,
  
  
  PRIMARY KEY ("ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
)

GO
--para peru
SET SCHEMA "BSCL"

delete from "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111315','INRV','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111316','INRV','TERCEROS','INGRESO','FRD','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111317','INRV','TERCEROS','INGRESO','OUT','FACTURACION_PRODUCTOS')  GO 

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111318','INRV','TERCEROS','INGRESO','BCS','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111318','INRV','TERCEROS','INGRESO','BVC','FACTURACION_PRODUCTOS')  GO
--ok
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111318','INRV','TERCEROS','INGRESO','BMV','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111319','PRRV','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111319','PRRV','TERCEROS','INGRESO','FRD','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111320','OPRV','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111321','EXOP','TERCEROS','INGRESO','OUT','FACTURACION_PRODUCTOS')  GO

--ok

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111322','INRF','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111323','INRF','TERCEROS','INGRESO','FRD','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111324','INRF','TERCEROS','INGRESO','OUT','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111325','PRRF','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111325','PRRF','TERCEROS','INGRESO','FRD','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111326','OPRF','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111327','RERF','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111328','RERV','TERCEROS','INGRESO','BVL','FACTURACION_PRODUCTOS')  GO

--ok
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111329','COMPRA_VENTA_MONEDA_SALES','TERCEROS','INGRESO','','FACTURACION_OTROS_CONCEPTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111330','COMPRA_VENTA_DERIVADOS','TERCEROS','INGRESO','','FACTURACION_OTROS_CONCEPTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111331','EQUITY_CAPITAL_MARKET','TERCEROS','INGRESO','','FACTURACION_OTROS_CONCEPTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111332','DEBT_CAPITAL_MARKET','TERCEROS','INGRESO','','FACTURACION_OTROS_CONCEPTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111333','SERVICIO_MARKET_MAKER','TERCEROS','INGRESO','','FACTURACION_OTROS_CONCEPTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111334','OTROS','TERCEROS','INGRESO','','FACTURACION_OTROS_CONCEPTOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','INRV','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','PRRV','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','OPRV','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','EXOP','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RV_VENTA')  GO
--ok
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','INRV','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','PRRV','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','OPRV','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','EXOP','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','INRV','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','PRRV','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','OPRV','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','EXOP','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','INRV','CUENTA_PROPIA','INGRESO','BCS','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','PRRV','CUENTA_PROPIA','INGRESO','BCS','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','OPRV','CUENTA_PROPIA','INGRESO','BCS','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','EXOP','CUENTA_PROPIA','INGRESO','BCS','FACTURACION_RV_VENTA')  GO



INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','INRV','CUENTA_PROPIA','INGRESO','BVC','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','PRRV','CUENTA_PROPIA','INGRESO','BVC','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','OPRV','CUENTA_PROPIA','INGRESO','BVC','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','EXOP','CUENTA_PROPIA','INGRESO','BVC','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','INRV','CUENTA_PROPIA','INGRESO','BMV','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','PRRV','CUENTA_PROPIA','INGRESO','BMV','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','OPRV','CUENTA_PROPIA','INGRESO','BMV','FACTURACION_RV_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111335','EXOP','CUENTA_PROPIA','INGRESO','BMV','FACTURACION_RV_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','INRV','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','PRRV','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','OPRV','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','EXOP','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','INRV','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','PRRV','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','OPRV','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','EXOP','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','INRV','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','PRRV','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','OPRV','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','EXOP','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','INRV','CUENTA_PROPIA','INGRESO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','PRRV','CUENTA_PROPIA','INGRESO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','OPRV','CUENTA_PROPIA','INGRESO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','EXOP','CUENTA_PROPIA','INGRESO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','INRV','CUENTA_PROPIA','INGRESO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','PRRV','CUENTA_PROPIA','INGRESO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','OPRV','CUENTA_PROPIA','INGRESO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','EXOP','CUENTA_PROPIA','INGRESO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','INRV','CUENTA_PROPIA','INGRESO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','PRRV','CUENTA_PROPIA','INGRESO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','OPRV','CUENTA_PROPIA','INGRESO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111336','EXOP','CUENTA_PROPIA','INGRESO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','INRV','CUENTA_PROPIA','INGRESO','BVL','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','PRRV','CUENTA_PROPIA','INGRESO','BVL','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','OPRV','CUENTA_PROPIA','INGRESO','BVL','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','EXOP','CUENTA_PROPIA','INGRESO','BVL','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','INRV','CUENTA_PROPIA','INGRESO','FRD','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','PRRV','CUENTA_PROPIA','INGRESO','FRD','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','OPRV','CUENTA_PROPIA','INGRESO','FRD','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','EXOP','CUENTA_PROPIA','INGRESO','FRD','DIVIDENDOS')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','INRV','CUENTA_PROPIA','INGRESO','OUT','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','PRRV','CUENTA_PROPIA','INGRESO','OUT','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','OPRV','CUENTA_PROPIA','INGRESO','OUT','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','EXOP','CUENTA_PROPIA','INGRESO','OUT','DIVIDENDOS')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','INRV','CUENTA_PROPIA','INGRESO','BCS','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','PRRV','CUENTA_PROPIA','INGRESO','BCS','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','OPRV','CUENTA_PROPIA','INGRESO','BCS','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','EXOP','CUENTA_PROPIA','INGRESO','BCS','DIVIDENDOS')  GO



INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','INRV','CUENTA_PROPIA','INGRESO','BVC','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','PRRV','CUENTA_PROPIA','INGRESO','BVC','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','OPRV','CUENTA_PROPIA','INGRESO','BVC','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','EXOP','CUENTA_PROPIA','INGRESO','BVC','DIVIDENDOS')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','INRV','CUENTA_PROPIA','INGRESO','BMV','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','PRRV','CUENTA_PROPIA','INGRESO','BMV','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','OPRV','CUENTA_PROPIA','INGRESO','BMV','DIVIDENDOS')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111337','EXOP','CUENTA_PROPIA','INGRESO','BMV','DIVIDENDOS')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','INRF','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','PRRF','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','OPRF','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','RERF','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','RERV','CUENTA_PROPIA','INGRESO','BVL','FACTURACION_RF_BVL_VENTA')  GO



INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','INRF','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','PRRF','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','OPRF','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','RERF','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','RERV','CUENTA_PROPIA','INGRESO','FRD','FACTURACION_RF_BVL_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','INRF','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','PRRF','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','OPRF','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','RERF','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111338','RERV','CUENTA_PROPIA','INGRESO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','INRF','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','PRRF','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','OPRF','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','RERF','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','RERV','CUENTA_PROPIA','INGRESO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','INRF','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','PRRF','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','OPRF','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','RERF','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','RERV','CUENTA_PROPIA','INGRESO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','INRF','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','PRRF','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','OPRF','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','RERF','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111339','RERV','CUENTA_PROPIA','INGRESO','OUT','MAME_RV_RF')  GO



INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','INRF','CUENTA_PROPIA','INGRESO','BVL','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','PRRF','CUENTA_PROPIA','INGRESO','BVL','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','OPRF','CUENTA_PROPIA','INGRESO','BVL','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','RERF','CUENTA_PROPIA','INGRESO','BVL','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','RERV','CUENTA_PROPIA','INGRESO','BVL','CUPONES')  GO

--

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','INRF','CUENTA_PROPIA','INGRESO','FRD','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','PRRF','CUENTA_PROPIA','INGRESO','FRD','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','OPRF','CUENTA_PROPIA','INGRESO','FRD','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','RERF','CUENTA_PROPIA','INGRESO','FRD','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','RERV','CUENTA_PROPIA','INGRESO','FRD','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','INRF','CUENTA_PROPIA','INGRESO','OUT','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','PRRF','CUENTA_PROPIA','INGRESO','OUT','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','OPRF','CUENTA_PROPIA','INGRESO','OUT','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','RERF','CUENTA_PROPIA','INGRESO','OUT','CUPONES')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('3111340','RERV','CUENTA_PROPIA','INGRESO','OUT','CUPONES')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111324','INRF','TERCEROS','COSTO','OUT','FACTURACION_RF_OTC_OUT')  GO
--ok
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','INRV','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','PRRV','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','OPRV','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','EXOP','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RV_COMPRA_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','INRV','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','PRRV','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','OPRV','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','EXOP','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RV_COMPRA_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','INRV','CUENTA_PROPIA','COSTO','BCS','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','PRRV','CUENTA_PROPIA','COSTO','BCS','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','OPRV','CUENTA_PROPIA','COSTO','BCS','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','EXOP','CUENTA_PROPIA','COSTO','BCS','FACTURACION_RV_COMPRA_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','INRV','CUENTA_PROPIA','COSTO','BVC','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','PRRV','CUENTA_PROPIA','COSTO','BVC','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','OPRV','CUENTA_PROPIA','COSTO','BVC','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','EXOP','CUENTA_PROPIA','COSTO','BVC','FACTURACION_RV_COMPRA_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','INRV','CUENTA_PROPIA','COSTO','BMV','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','PRRV','CUENTA_PROPIA','COSTO','BMV','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','OPRV','CUENTA_PROPIA','COSTO','BMV','FACTURACION_RV_COMPRA_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111334','EXOP','CUENTA_PROPIA','COSTO','BMV','FACTURACION_RV_COMPRA_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','INRV','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','PRRV','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','OPRV','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','EXOP','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','INRV','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','PRRV','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','OPRV','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','EXOP','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','INRV','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','PRRV','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','OPRV','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','EXOP','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','INRV','CUENTA_PROPIA','COSTO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','PRRV','CUENTA_PROPIA','COSTO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','OPRV','CUENTA_PROPIA','COSTO','BCS','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','EXOP','CUENTA_PROPIA','COSTO','BCS','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','INRV','CUENTA_PROPIA','COSTO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','PRRV','CUENTA_PROPIA','COSTO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','OPRV','CUENTA_PROPIA','COSTO','BVC','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','EXOP','CUENTA_PROPIA','COSTO','BVC','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','INRV','CUENTA_PROPIA','COSTO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','PRRV','CUENTA_PROPIA','COSTO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','OPRV','CUENTA_PROPIA','COSTO','BMV','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111335','EXOP','CUENTA_PROPIA','COSTO','BMV','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','INRF','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','PRRF','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','OPRF','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','RERF','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','RERV','CUENTA_PROPIA','COSTO','BVL','FACTURACION_RF_BVL_VENTA')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','INRF','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','PRRF','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','OPRF','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','RERF','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','RERV','CUENTA_PROPIA','COSTO','FRD','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','INRF','CUENTA_PROPIA','COSTO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','PRRF','CUENTA_PROPIA','COSTO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','OPRF','CUENTA_PROPIA','COSTO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','RERF','CUENTA_PROPIA','COSTO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111337','RERV','CUENTA_PROPIA','COSTO','OUT','FACTURACION_RF_BVL_VENTA')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','INRF','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','PRRF','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','OPRF','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','RERF','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','RERV','CUENTA_PROPIA','COSTO','BVL','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','INRF','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','PRRF','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','OPRF','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','RERF','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','RERV','CUENTA_PROPIA','COSTO','FRD','MAME_RV_RF')  GO


INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','INRF','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','PRRF','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','OPRF','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','RERF','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO

INSERT INTO "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"("CodigoBO","ProductoConcepto","Relacionado","Tipo","Bolsa","Proceso")
values('2111338','RERV','CUENTA_PROPIA','COSTO','OUT','MAME_RV_RF')  GO


GO

COMMIT


