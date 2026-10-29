SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultaDetalleTraspasosEgresos"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultaDetalleTraspasosEgresos"  AS 

SELECT
	DISTINCT Traspaso."IdTraspaso" ,
	Traspaso."IdContratoSAAPV" ,
	Traspaso."FechaIngreso" ,
	Traspaso."FechaModificacion" ,
	Traspaso."CodigoTipoTraspaso" ,
	Traspaso."FechaSuscripcion",
	Traspaso."IdCliente" ,
	SocioNegocioCli."IdSocioDeNegocio" AS "IdSocioNegocioCli" ,
	SocioNegocioCli."NombreCompleto" ,
	Traspaso."IdPersona" ,
	COALESCE(SocioNegocioEmp."IdSocioDeNegocio",
	0) AS "IdSocioNegocioEMP" ,
	COALESCE(SocioNegocioEmp."RazonSocial",
	'') AS "RazonSocial" ,
	Traspaso."CodigoTipoAhorro" ,
	Traspaso."NombreEntidad" ,
	Traspaso."DocumentoEntidad" ,
	Traspaso."IdEstado" ,
	DetalleTraspaso."IdEntidadRecaudadora" ,
	DetalleTraspaso."IdPortafolio" ,
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
LEFT OUTER JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" AS DetalleTraspaso ON
	DetalleTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON
	Portafolio."IdCliente" = Traspaso."IdCliente"
	AND Portafolio."IdMatrizAgrupadorAPV" != 0
LEFT OUTER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS MatrizAgrupador ON
	MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocioCli ON
	SocioNegocioCli."IdCliente" = Traspaso."IdCliente"
	AND SocioNegocioCli."IdEmpresa" = Traspaso."IdEmpresa"
	--AND SocioNegocioCli."CodigoTipoPersona" = 'N'
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocioEmp ON
	SocioNegocioEmp."IdPersona" = Traspaso."IdPersona"
	AND SocioNegocioEmp."IdEmpresa" = Traspaso."IdEmpresa";

GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultaDetalleTraspasosEgresos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
