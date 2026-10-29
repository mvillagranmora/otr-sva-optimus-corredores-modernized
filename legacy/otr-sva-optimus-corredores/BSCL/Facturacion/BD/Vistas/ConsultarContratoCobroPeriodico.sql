SET SCHEMA "BSCL";

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodico";

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodico" AS

 SELECT 
	Cont."IdContratoComision" AS "IdContratoCobroPeriodico"
 	 ,Cont."Nombre" AS "ContratoCobroPeriodico"
	 ,Cmbte."IdComprobante"
	 ,Cmbte."IdCliente"
	 ,Cmbte."IdPortafolio"
	 ,Cli."DocumentoIdentidad"
	 ,Cli."NombreCompleto" AS "NombreLargoCliente"
	 ,Por."CodigoPropositoPortafolio" AS "CodigoPortafolio"
	 ,Cont."IdTipoContrato" AS "IdTipoContratoCobroPeriodico"
	 ,Tcont."Nombre"		AS "TipoContratoCobroPeriodico"
	 ,COALESCE(Fcob."IdFormatoCobro",'') AS "IdFormatoContratoCobroPeriodico"
	 ,COALESCE(Fcob."Nombre",'') AS "FormatoContratoCobroPeriodico"
	 ,Cont."IdMoneda"
 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
 	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cli ON (Cmbte."IdCliente" = Cli."IdCliente")
 	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Por ON (Cmbte."IdPortafolio" = Por."IdPortafolio")
 	INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS Cont ON (Cmbte."IdCliente" = Cont."IdCliente" AND Cmbte."IdClasificacionTipoInstrumento" = Cont."IdClasificacionTipoInstrumento")
 	INNER JOIN "BSCL.Plataforma.BD::COM.TipoContrato" Tcont ON (Cont."IdTipoContrato" = Tcont."IdTipoContrato")
 	LEFT JOIN "BSCL.Plataforma.BD::COM.FormatoCobro" Fcob ON (Cont."IdFormatoCobro" = Fcob."IdFormatoCobro")
 	LEFT JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" ContDet ON (Cont."IdContratoComision" = ContDet."IdContratoComision")
    
WITH READ ONLY;

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodico" TO BCS_DESARROLLADOR WITH GRANT OPTION;                                       