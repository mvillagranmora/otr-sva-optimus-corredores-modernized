SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa";
GO
CREATE PROCEDURE
-- 
--  Proposito: consultar para obtener precios de bolsa
--  Autor: TEAM ORDENES, AW YEAH
--  Fecha Creacion: 20/02/2017

"BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa" ( 
	IN p_Instrumentos TABLE(
				"IdEmpresa"					BIGINT,
				"IdInstrumento"				BIGINT,
				"IdBolsa"					NVARCHAR(3),
				"Fecha"						NVARCHAR(10),
				"IdCondicionDeLiquidacion"	NVARCHAR(2)
	),
	OUT p_out_Instrumento   TABLE(
				"IdInstrumento" 			BIGINT,
				"IdBolsa" 					NVARCHAR(3),
				"FechaConsulta"				NVARCHAR(10),
				"Fecha"						DATE,
				"VolumenTransado" 			DECIMAL(26,8),
 		 		"MontoTransado" 			DECIMAL(26,8),
 		 		"PrecioBase"				DECIMAL(26,8), 
			 	"PrecioCierre"				DECIMAL(26,8),
			 	"PuntaCompra"				DECIMAL(26,8),
			 	"PuntaVenta"				DECIMAL(26,8),
			 	"Variacion"					DECIMAL(26,8),
			 	"IdMoneda"					NVARCHAR(3),
			 	"PrecioApertura"			DECIMAL(26,8), 
			 	"PrecioMayor"				DECIMAL(26,8), 
			 	"PrecioMenor"				DECIMAL(26,8), 
			 	"PrecioMedio"				DECIMAL(26,8),
			 	-------RFIF
			 	"PrecioMercado"				DECIMAL(26,8),
			 	"TasaMercado"				DECIMAL(26,8)
	),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador		int,
	OUT p_out_AdvertenciasDetalle		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador			int,
	OUT p_out_ErroresDetalle			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN											 
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales"
	SELECT ins."IdEmpresa", 0 as "IdUsuario", 0 as "IdModulo", emp."IdPais",100 as "IdTrabajo"
	FROM :p_Instrumentos as ins
	INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" as emp ON emp."IdEmpresa" = ins."IdEmpresa";
	
	INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos"
	SELECT
		 row_number() over (order by inn."IdInstrumento",inn."Fecha" desc) as "IdRegistro"
		,100 as "IdTrabajo"
		,'0' as "TipoCartera"
		,'CARTERA' as "IdUso"
		,'0' as "IdTipoCriterioTasa"
		,tinst."IdClasificacionTipoInstrumento"
		,tinst."IdTipoInstrumento"
		,inst."IdInstrumento"
		,inst."Nemotecnico"
		,inst."IdMoneda"
		,inn."Fecha" as "FechaConsulta"
		,0 as "PlazoResidual"
	FROM :p_Instrumentos as inn
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on inn."IdInstrumento" = inst."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tinst on inst."IdTipoInstrumento" = tinst."IdTipoInstrumento";
	
	--SELECT * FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos";
	
	CALL "BSCL.Plataforma.BD.Servicios::TAS.EntregarPreciosTasas"(
		p_IdTrabajo => 100
	);
	
	--SELECT * FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos";
	
	p_out_Instrumento = 
		WITH precios as(		
			SELECT row_number() over (partition by inn."IdInstrumento",bolins."IdBolsa" order by inn."IdInstrumento",prec."Fecha" desc) AS row_num,inn."IdInstrumento",bolins."IdBolsa",inn."Fecha" AS "FechaConsulta",
			prec."Fecha",prec."UnidadesTransadas" AS "VolumenTransado","MontoTransado",COALESCE(inst."Precio",PreTas."Precio") AS "PrecioBase",prec."PrecioCierre",puntas."PuntaCompra",puntas."PuntaVenta",prec."Variacion",inst."IdMoneda",
			prec."PrecioApertura",prec."PrecioMayor",prec."PrecioMenor",prec."PrecioMedio",PreTas."Precio" as "PrecioMercado",PreTas."Tasa" as "TasaMercado"
			FROM :p_Instrumentos inn
			INNER JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumento" bolins ON bolins."IdInstrumento" = inn."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on inn."IdInstrumento" = inst."IdInstrumento"
		 	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec on prec."IdBolsa" = inn."IdBolsa" and prec."IdInstrumento" = inn."IdInstrumento" and prec."Fecha" <= inn."Fecha"
		 	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas" puntas on inst."IdInstrumento" = puntas."IdInstrumento" and puntas."Fecha" = inn."Fecha" and puntas."IdBolsa" = inn."IdBolsa" and puntas."IdCondicionDeLiquidacion" = COALESCE(inn."IdCondicionDeLiquidacion",'T2')
		 	LEFT JOIN "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos" PreTas on PreTas."IdInstrumento" = inn."IdInstrumento" and PreTas."IdTrabajo" = 100
		 )
	 	SELECT "IdInstrumento","IdBolsa","FechaConsulta","Fecha","VolumenTransado","MontoTransado","PrecioBase","PrecioCierre","PuntaCompra","PuntaVenta","Variacion",
	 		   "IdMoneda","PrecioApertura","PrecioMayor","PrecioMenor","PrecioMedio","PrecioMercado","TasaMercado"
		FROM precios
	 	WHERE row_num = 1
	 	ORDER BY "IdInstrumento","IdBolsa","FechaConsulta","Fecha" desc;
END;
GO