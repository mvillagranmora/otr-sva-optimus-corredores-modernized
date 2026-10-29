
SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::CAR.ConsultarCargaMasivaDetalle";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" AS

SELECT
	CM."IdCargaMasiva",	
	CM."IdTipoCargaMasiva",
	CM."IdEmpresa",
	CM."IdUsuario",
  	CM."FechaCarga",   			-- Fecha Sistema
 	CM."FechaProceso", 			-- Fecha en que se procesa CM
 	CM."FechaIngresoRegistro",	-- Fecha de BD al ingreso
 	CM."NombreArchivo",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	CMD."IdCargaMasivaDetalle",
	CMD."IdConcepto",
	COALESCE(CON."Nombre", CMD."IdConcepto") AS "NombreConcepto",	
	CMD."IdTipoOperacion",
	COALESCE(OPE."Nombre", CMD."IdTipoOperacion") AS "NombreTipoOperacion",
	CMD."DocumentoIdentidad",
	PER."NombreCompleto" AS "NombreSocioDeNegocio",	 
	CMD."IdPortafolio",
	CMD."CodigoPortafolio",
	POR."Proposito" AS "PropositoPortafolio",
	CMD."IdTipoComprobante",
	COALESCE(TCO."Nombre", CMD."IdTipoComprobante") AS "NombreTipoComprobante",
	CMD."IdCondicionDeLiquidacion",
	COALESCE(CDL."Codigo", CMD."IdCondicionDeLiquidacion") AS "CodigoCondicionDeLiquidacion",
	CMD."IdMoneda",
	CMD."Monto",
	CMD."Glosa",
	CMD."Referencia",
	CMD."TipoReferencia",
	CMD."IdMedioDePago",
	COALESCE(MEP."Nombre", CMD."IdMedioDePago") AS "NombreMedioDePago",
	CMD."IdModalidadDePago",
	COALESCE(MOP."Nombre",CMD."IdModalidadDePago") AS "NombreModalidadDePago",
	CMD."ObservacionValidacion",	
	CMD."IdEstado" AS "IdEstadoCargaMasivaDetalle",
	EST."Nombre" AS "NombreEstadoCargaMasivaDetalle",
	CMD."IdMovimientoComprobante",
	CMD."IdMatrizAgrupador"
	
FROM "BSCL.Facturacion.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CMD."IdEstado")
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = CMD."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MEP ON (MEP."IdMedioDePago" = CMD."IdMedioDePago")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MOP ON (MOP."IdModalidadDePago" = CMD."IdModalidadDePago")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON (CON."IdConcepto" = CMD."IdConcepto")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CDL ON (CDL."IdCondicionDeLiquidacion" = CMD."IdCondicionDeLiquidacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPE ON (OPE."IdTipoOperacion" = CMD."IdTipoOperacion")
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TCO ON (TCO."IdTipoComprobante" = CMD."IdTipoComprobante")


WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

