SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosOrden"; 
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosOrden"  
AS SELECT 
	cob."IdCobroOrden" , 
	cob."IdGrupoCobro" , 
	cob."IdOrden" , 
	cob."IdCobro" , 
	cob."ValorCobroPerfil" , 
	cob."ValorCobroAplicado" , 
	cob."MontoCobroCalculado" ,
	cob."MontoCobroCalculadoLocal" , 
	cob."MontoCobroCalculadoLiquidacion" , 
	cob."FormaCobro" ,
	mcob."NombreCobro",
	cob."MontoMinimo" ,  
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(cob."FormaCobro") as "NombreFormaCobro", 
	cob."EsComprobante" , 
	ord."IdPerfilCobros" , 
	cob."FechaIngresoCobro" ,
	cob."CodigoCobroClasificacion",
	ord."IdMonedaLiquidacionOrden",
	cob."IdMoneda",
	cob."IdOrigenCobro",
	cob."IdAsociacion",
	ord."IdEmpresa",
	cob."ValorCobroFinal",
	cob."CodigoCobroClasificacion" AS "IdClaseCobroBase"
FROM "BSCL.Orden.BD::ORD.Orden" ord
INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" cob on cob."IdOrden" = ord."IdOrden" and cob."IdGrupoCobro" = ord."IdGrupoCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" mcob on mcob."IdCobro" = cob."IdCobro" and ord."IdEmpresa" = mcob."IdEmpresa"
UNION
SELECT 
	cob."IdCobroOrden" , 
	cob."IdGrupoCobro" , 
	cob."IdOrden" , 
	cob."IdCobro" , 
	cob."ValorCobroPerfil" , 
	cob."ValorCobroAplicado" , 
	cob."MontoCobroCalculado" ,
	cob."MontoCobroCalculadoLocal" , 
	cob."MontoCobroCalculadoLiquidacion" , 
	cob."FormaCobro" ,
	def."Descripcion" AS "NombreCobro",
	cob."MontoMinimo" ,  
	TIPO."NombreTipoCobro" as "NombreFormaCobro", 
	cob."EsComprobante" , 
	ord."IdPerfilCobros" , 
	cob."FechaIngresoCobro" ,
	cob."CodigoCobroClasificacion",
	ord."IdMonedaLiquidacionOrden",
	cob."IdMoneda",
	cob."IdOrigenCobro",
	cob."IdAsociacion",
	ord."IdEmpresa",
	cob."ValorCobroFinal",
	cla."IdClaseCobroBase"
FROM "BSCL.Orden.BD::ORD.Orden" ord
INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" cob on cob."IdOrden" = ord."IdOrden" and cob."IdGrupoCobro" = ord."IdGrupoCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def on def."CodigoDefinicionCobro" = cob."IdCobro" and ord."IdEmpresa" = def."IdEmpresa"
INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla on def."IdClaseCobro" = cla."IdClaseCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" tipo ON def."IdTipoCobro" = tipo."IdTipoCobro"
;
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCobrosOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
