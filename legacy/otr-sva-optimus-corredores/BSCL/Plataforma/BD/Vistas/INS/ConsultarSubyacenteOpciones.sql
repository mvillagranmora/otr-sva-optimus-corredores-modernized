SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS 
	SELECT	INS."IdInstrumento",  
			INS."Nemotecnico",  
			TRIM(INS."EsPutEsCall")				AS "Tipo", 		-- call/put, 
			INS."Tamano"							AS "LotePadron",-- Lote Padron 
			INS."EstiloOpcion"	AS "Estilo",
			INS."ModalidadLiquidacion" AS "Modo", 			-- Modo Liquidacion F/C
			INS."RazonSocial" AS "Nemo",
			INS."FechaVencimiento", 
			INS."IdEmisor", 
			FAM.* 
	FROM	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarFamilia" FAM 
			ON INS."IdTipoInstrumento" = FAM."IdTipoInstrumento";
	
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;



