SET SCHEMA "BSCL";


DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioPuntas";

CREATE PROCEDURE
-- 
--  Proposito: consultar para obtener precios de bolsa
--  Autor: TEAM ORDENES, AW YEAH
--  Fecha Creacion: 20/02/2017

"BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioPuntas" ( 
	IN p_IdTipoInstrumento			NVARCHAR(3),
	IN p_IdBolsa					NVARCHAR(3),
	IN p_Fecha						NVARCHAR(10),
	IN p_IdCondicionDeLiquidacion	NVARCHAR(2),
	
	OUT p_out_Instrumento   TABLE(
				"IdInstrumento" 			BIGINT,
				"Nemotecnico"				NVARCHAR(40),
				"IdMoneda"					NVARCHAR(3),
				"FechaPrecioCierre"			DATE,
				"FechaPuntas"				DATE,
				"UnidadesTransadas" 		DECIMAL(26,8),
 		 		"MontoTransado" 			DECIMAL(26,8), 
			 	"PrecioMayor"				DECIMAL(26,8), 
			 	"PrecioMenor"				DECIMAL(26,8), 
			 	"PrecioMedio"				DECIMAL(26,8), 
			 	"PrecioApertura"			DECIMAL(26,8), 
			 	"PrecioCierre"				DECIMAL(26,8), 
			 	"PuntaCompra"				DECIMAL(26,8), 
			 	"PuntaVenta"				DECIMAL(26,8),
			 	"Variacion"					DECIMAL(26,8)
			 	
			 	
			 	
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
		
		p_out_Instrumento = SELECT inst."IdInstrumento","Nemotecnico","IdMoneda",COALESCE(precios."Fecha",'0001-01-01') AS "FechaPrecioCierre",COALESCE(puntas."Fecha",'0001-01-01') as "FechaPuntas",
	 	COALESCE("UnidadesTransadas",0) AS "UnidadesTransadas",COALESCE("MontoTransado",0) as "MontoTransado",COALESCE("PrecioMayor",0) as "PrecioMayor",
	 	COALESCE("PrecioMenor",0) as "PrecioMenor",COALESCE("PrecioMedio",0) as "PrecioMedio",COALESCE("PrecioApertura",0) as "PrecioApertura",COALESCE("PrecioCierre",0) as "PrecioCierre",
	 	COALESCE("PuntaCompra",0) AS "PuntaCompra",COALESCE("PuntaVenta",0) AS "PuntaVenta",COALESCE("Variacion" ,0) AS "Variacion"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" inst
		LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" precios on precios."IdInstrumento" = inst."IdInstrumento" and precios."Fecha" = :p_Fecha and precios."IdBolsa" = :p_IdBolsa
		LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas" puntas on inst."IdInstrumento" = puntas."IdInstrumento" and puntas."Fecha" = :p_Fecha and puntas."IdBolsa" = :p_IdBolsa and "IdCondicionDeLiquidacion" = :p_IdCondicionDeLiquidacion
		WHERE inst."IdTipoInstrumento" = :p_IdTipoInstrumento AND inst."IdEstado" = 'ACT'-- AND precios."IdInstrumento" IS NOT NULL
		ORDER BY "Nemotecnico";
	 	
END;
