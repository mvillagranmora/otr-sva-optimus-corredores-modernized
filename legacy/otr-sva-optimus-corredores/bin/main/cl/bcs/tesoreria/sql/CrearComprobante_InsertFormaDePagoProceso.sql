
	
INSERT INTO "BSCL.Tesoreria.BD::GBL.FormaDePagoGenerarComprobante_Proceso"
(	
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
)

SELECT  FP.*, MeP."NombreMedioDePago",MeP."NombreModalidadDePago",
		IbC."IdPersona",IbC."NombreCompleto",IbC."DocumentoIdentidadDefecto",IbC."CodigoTipoPersona",
		IbC."IdCuentaBanco",IbC."IdBanco",IbC."IdPlaza",IbC."PlazoRetencionMismoBanco",IbC."PlazoRetencionOtroBanco",
		IbC."NombreCuentaBancaria",IbC."NumeroCuentaBancaria",IbC."IdTipoCuentaBancaria",IbC."NombreTipoCuentaBancaria",
		TBC."IdTipoTarjetaBancaria",TBC."NombreTipoTarjetaBancaria",TBC."NumeroTarjetaBancaria"
  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago" AS FP
  LEFT JOIN (SELECT MeP."IdMedioDePago",MeP."Nombre" as "NombreMedioDePago",MoP."IdModalidadDePago", MoP."Nombre" as "NombreModalidadDePago"
			  FROM "BSCL.Plataforma.BD::PAG.MedioDePago" AS MeP
			 INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MePMoP ON (MePMoP."IdMedioDePago" = MeP."IdMedioDePago")
			 INNER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MoP ON (MoP."IdModalidadDePago" = MePMoP."IdModalidadDePago")
  			) AS MeP
  		 ON MeP."IdMedioDePago" = FP."IdMedioDePago"
  LEFT JOIN (SELECT CB."IdPersona",SN."NombreCompleto",SN."DocumentoIdentidadDefecto",SN."CodigoTipoPersona",
				    CB."IdCuentaBanco",CB."IdBanco",CB."IdPlaza",CB."PlazoRetencionMismoBanco",CB."PlazoRetencionOtroBanco",
				    CB."NombreCuentaBancaria",CB."NumeroCuentaBancaria",CB."IdTipoCuentaBancaria",TCBC."NombreTipoCuentaBancaria"
			   FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CB
			  INNER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CB."IdTipoCuentaBancaria")
			  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" as SN ON SN."IdPersona" = CB."IdPersona"
			  WHERE SN."IdEmpresa" = :IdEmpresa
  			) AS IbC
  		 ON IbC."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TBC 
		 ON TBC."IdTarjetaBancaria" = FP."IdTarjetaBancariaCorredora";	
			 
			 