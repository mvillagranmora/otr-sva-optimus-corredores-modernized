SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobro" AS
	SELECT 
		VC."IdValorCobro",
		VC."DescripcionValorCobro",
		VC."IdTipoProducto",
		CASE VC."IdTipoProducto" WHEN '0' THEN 'TODOS' ELSE IFNULL(TP."Nombre",'') END as "NombreTipoProducto",
		VC."IdCobro",
		CC."IdCobroClasificacion",
		CC."CodigoCobroClasificacion",
		C."NombreCobro",
		C."FormaCobro", -- Fijo/Variable.
		VC."ValorCobro", -- ej: si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
		VC."ValorTopeMinimoOrden",
		VC."IdMoneda",
		VC."IdEstado",
		ESTVC."Nombre" as "Estado",
		VC."IdEmpresa",
		VC."EsPersonalizado"
	FROM 
		"BSCL.Plataforma.BD::COM.ValorCobro" as VC
		INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as C 
			on (C."IdCobro" = VC."IdCobro")
		INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" as CC
			on (C."IdCobroClasificacion" =  CC."IdCobroClasificacion")
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" as TP
			on (TP."IdTipoProducto" = VC."IdTipoProducto")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTVC
			ON (ESTVC."IdEstado" = VC."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
