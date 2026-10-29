SELECT
	"NumeroReferencia","IdMedioDePago","IdModalidadDePago","IdMoneda","Monto","IdCuentaBancariaCorredora",		
	"IdTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora","FechaDocumentoPagoCorredora",		
	"IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio",
	"IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio",
	"IdTipoTarjetaBancariaSocio","NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio",
	"NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio","NombreMedioDePago",
	"NombreModalidadDePago","IdPersona","NombreCompleto","DocumentoIdentidadDefecto","CodigoTipoPersona",
	"IdCuentaBanco","IdBanco","IdPlaza","PlazoRetencionMismoBanco","PlazoRetencionOtroBanco",
	"NombreCuentaBancaria","NumeroCuentaBancaria","IdTipoCuentaBancaria","NombreTipoCuentaBancaria",
	"IdTipoTarjetaBancaria","NombreTipoTarjetaBancaria","NumeroTarjetaBancaria"
FROM  "BSCL.Tesoreria.BD::GBL.FormaDePagoGenerarComprobante_Proceso";