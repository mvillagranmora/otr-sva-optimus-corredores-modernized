SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV"  AS 

SELECT
	DISTINCT Traspaso."IdTraspaso" ,
	Traspaso."IdContratoSAAPV" ,
	Traspaso."FechaIngreso" ,
	Traspaso."FechaModificacion" ,
	Traspaso."CodigoTipoTraspaso" ,
	Traspaso."IdCliente" ,
	SocioNegocioCli."IdSocioDeNegocio" AS "IdSocioNegocioCli" ,
	SocioNegocioCli."NombreCompleto" ,
	SocioNegocioCli."DocumentoIdentidadDefecto" ,
	Traspaso."IdPersona" ,
	COALESCE(SocioNegocioEmp."IdSocioDeNegocio",
	0) AS "IdSocioNegocioEMP" ,
	COALESCE(SocioNegocioEmp."RazonSocial",
	'') AS "RazonSocial" ,
	Traspaso."CodigoTipoAhorro" ,
	Traspaso."NombreEntidad" ,
	Traspaso."DocumentoEntidad" ,
	Traspaso."IdEstado" ,
	DetalleTraspaso."IdDetalleTraspaso" ,
	DetalleTraspaso."IdPortafolio" ,
	DetalleTraspaso."FechaIngreso" AS "FechaIngresoDetalle" ,
	DetalleTraspaso."MontoPeso" ,
	DetalleTraspaso."MontoUF" ,
	DetalleTraspaso."Porcentaje" ,
	DetalleTraspaso."IdEstado" AS "IdEstadoDetalle" ,
	DetalleTraspaso."EsReapertura",
	MatrizAgrupador."IdMatrizAgrupadorAPV" ,
	MatrizAgrupador."IdRegimenTributario" ,
	MatrizAgrupador."IdPlanTipoInversion" ,
	MatrizAgrupador."NombrePlan" ,
	MatrizAgrupador."IdPropositoPortafolio" ,
	MatrizAgrupador."IdProductoAgrupador" ,
	MatrizAgrupador."Producto" ,
	MatrizAgrupador."NombreAgrupador" ,
	MatrizAgrupador."CodigoAgrupador" ,
	MatrizAgrupador."Codigo"
FROM
	"BSCL.APV.BD::TRA.Traspaso" AS Traspaso
INNER JOIN "BSCL.APV.BD::TRA.DetalleTraspaso" AS DetalleTraspaso ON
	DetalleTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
LEFT OUTER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato ON
	Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV"
	AND Contrato."CodigoTipoSolicitud" = 'TRA'
	AND Contrato."IdEmpresa" = Traspaso."IdEmpresa"
LEFT OUTER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContrato ON
	PortafolioContrato."IdContratoSAAPV" = Contrato."IdContratoSAAPV"
	AND PortafolioContrato."IdPortafolio" = DetalleTraspaso."IdPortafolio"
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON
	Portafolio."IdPortafolio" = DetalleTraspaso."IdPortafolio"
	AND Portafolio."IdCliente" = Traspaso."IdCliente"
	AND Portafolio."IdMatrizAgrupadorAPV" != 0
LEFT OUTER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS MatrizAgrupador ON
	MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocioCli ON
	SocioNegocioCli."IdCliente" = Traspaso."IdCliente"
	AND SocioNegocioCli."IdEmpresa" = Traspaso."IdEmpresa"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocioEmp ON
	SocioNegocioEmp."IdPersona" = Traspaso."IdPersona"
	AND SocioNegocioEmp."IdEmpresa" = Traspaso."IdEmpresa"
WHERE Portafolio."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

