SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacion"; 
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacion"  
AS SELECT 
	cobasig."IdCobroAsignacion",
	cobasig."IdAsignacion",
	cobasig."IdCobro",
	cobasig."FechaIngresoCobro",
	cobasig."FechaIngresoRegistro",
	ord."IdPerfilCobros",
	cobasig."FormaCobro",
	mcob."NombreCobro",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(cobasig."FormaCobro") as "NombreFormaCobro", 
	cobasig."ValorCobro" as "ValorCobroPerfil" ,
	cobasig."ValorCobroAplicado" ,
	cobasig."MontoCobroCalculado" ,
	cobasig."MontoCobroCalculadoLocal" ,
	cobasig."MontoCobroCalculadoLiquidacion" ,
	"CobroClasificacion_$2"."CodigoCobroClasificacion",
	cobasig."IdMoneda" as "IdMoneda",
	cobasig."ValorCobroFinal",
	ord."IdMonedaLiquidacionOrden"
FROM "BSCL.Orden.BD::ORD.CobroAsignacion" AS cobasig
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" as asig on asig."IdAsignacion" = cobasig."IdAsignacion"
INNER JOIN "BSCL.Orden.BD::ORD.Orden" as ord on ord."IdOrden" = asig."IdOrden"
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS mcob ON  mcob."IdCobro" = cobasig."IdCobro"  
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS "CobroClasificacion_$2" ON  "CobroClasificacion_$2"."IdCobroClasificacion" = mcob."IdCobroClasificacion"

UNION ALL

SELECT 
	cobasig."IdCobroAsignacion",
	cobasig."IdAsignacion",
	cobasig."IdCobro",
	cobasig."FechaIngresoCobro",
	cobasig."FechaIngresoRegistro",
	ord."IdPerfilCobros",
	cobasig."FormaCobro",
	def."Descripcion" AS "NombreCobro",
	TIPO."NombreTipoCobro" as "NombreFormaCobro", 
	cobasig."ValorCobro" as "ValorCobroPerfil" ,
	cobasig."ValorCobroAplicado" ,
	cobasig."MontoCobroCalculado" ,
	cobasig."MontoCobroCalculadoLocal" ,
	cobasig."MontoCobroCalculadoLiquidacion" ,
	cobasig."CodigoCobroClasificacion",
	cobasig."IdMoneda" as "IdMoneda",
	cobasig."ValorCobroFinal",
	ord."IdMonedaLiquidacionOrden"
FROM "BSCL.Orden.BD::ORD.CobroAsignacion" AS cobasig
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" as asig on asig."IdAsignacion" = cobasig."IdAsignacion"
INNER JOIN "BSCL.Orden.BD::ORD.Orden" as ord on ord."IdOrden" = asig."IdOrden"
INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def on def."CodigoDefinicionCobro" = cobasig."IdCobro" and ord."IdEmpresa" = def."IdEmpresa"
INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" TIPO ON def."IdTipoCobro" = TIPO."IdTipoCobro"
;
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO