SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarDetalleConfiguracionOperador"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarDetalleConfiguracionOperador"
AS

SELECT
	COP."CodigoOperador",
	COP."UtilizaPortafolio",
	COP."IdPortafolio",
	COP."ReservaTransaccion",
	TITR."IdTipoInstrumento",
	TINS."Nombre" as "NombreTipoInstrumento",
	DCOP."AsignacionAuto",
	DCOP."MovimientoFacturarAuto",
	DCOP."ComprobanteAuto",
	COP."IdEstado",
	COP."FechaCreacion",
	COP."FechaModificacion",
	COP."IdUsuarioCrea",
	COP."IdUsuarioModifica",
	DCOP."IdEstado" as "IdEstadoDetalle",
	ESTDCOP."Nombre" as "NombreEstadoDetalle",
	COP."IdEmpresa",
	CASE COALESCE(DCOP."IdEstado",'N') WHEN 'N' THEN 'N' ELSE 'S' END as "DetalleExiste",
	LIBORD."IdLibroOrdenes" as "IdLibroOrdenes",
	LIBORD."NombreLibroOrdenes" as "LibroOrdenes"
FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" as COP
INNER JOIN "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" as TITR ON (TITR."IdEmpresa" = COP."IdEmpresa" AND TITR."IdEstado"='ACT')
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON (TINS."IdTipoInstrumento" = TITR."IdTipoInstrumento")
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LIBORD ON (LIBORD."IdLibroOrdenes"=COP."IdLibroOrdenes")
LEFT JOIN "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" as DCOP ON (TITR."IdTipoInstrumento" = DCOP."IdTipoInstrumento" AND TITR."IdEmpresa" = DCOP."IdEmpresa" AND COP."CodigoOperador" = DCOP."CodigoOperador" AND TITR."IdEstado"='ACT')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTDCOP ON (DCOP."IdEstado" = ESTDCOP."IdEstado")
ORDER BY COP."CodigoOperador"; 

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarDetalleConfiguracionOperador" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
