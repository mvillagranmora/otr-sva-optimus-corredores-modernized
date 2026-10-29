SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaVectorPrecios";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaVectorPrecios" ( 

	
	IN	p_limpiaTablas						NVARCHAR(1) DEFAULT 'N',
	
	OUT	p_out_Resultado						NVARCHAR(40),
	OUT	p_out_Mensaje						NVARCHAR(520)
										
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
BEGIN

	
	DECLARE v_countNuevos,v_countActualizados BIGINT;
	
	v_Precios = SELECT ins."IdInstrumento",FECHA as "FechaVector",SEC_LISTA as "SecuenciaLista",TIPO as "Tipo",
						VALOR as "ValorPrecioAlgorithmics100",VALOR_95 as "ValorPrecioAlgorithmics95",VALOR_98 as "ValorPrecioAlgorithmics98",
						VALOR_01 as "ValorPrecioOperacionesPlazo01",VALOR_02 as "ValorPrecioVentaCorta02",VALOR_03 as "ValorOtrosPrecios03",VALOR_04 as "ValorOtrosPrecios04",
						VALOR_05 as "ValorOtrosPrecios05",PRECIO_BASE as "ValorPrecioBase"
				FROM "BSCL.Plataforma.BD::MER.CargaVectorPrecios" vec
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = vec."NEMO"
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento" AND tins."IdClasificacionTipoInstrumento" <> 'FM';
	
	v_Registros = SELECT ins."IdInstrumento",pre."FechaVector",CASE WHEN pre."IdInstrumento" IS NULL THEN 'S' ELSE 'N' END AS "RegistroNuevo"
			FROM :v_Precios ins
			LEFT JOIN "BSCL.Plataforma.BD::INS.VectorPrecios" pre on ins."FechaVector" = pre."FechaVector" and pre."IdInstrumento" = ins."IdInstrumento";
	
	
	SELECT 'v_PreciosNuevos',* from :v_Precios pre INNER JOIN :v_Registros reg on reg."IdInstrumento" = pre."IdInstrumento"
			WHERE reg."RegistroNuevo" = 'S';
	SELECT 'v_PreciosExistentes',* from :v_Precios pre INNER JOIN :v_Registros reg on reg."IdInstrumento" = pre."IdInstrumento"
			WHERE reg."RegistroNuevo" = 'N';
	
	
	INSERT INTO "BSCL.Plataforma.BD::INS.VectorPrecios"
	("IdInstrumento","FechaVector","SecuenciaLista","Tipo","ValorPrecioAlgorithmics100","ValorPrecioAlgorithmics95","ValorPrecioAlgorithmics98",
			"ValorPrecioOperacionesPlazo01","ValorPrecioVentaCorta02","ValorOtrosPrecios03","ValorOtrosPrecios04","ValorOtrosPrecios05","ValorPrecioBase")
	SELECT  pre."IdInstrumento",pre."FechaVector","SecuenciaLista","Tipo","ValorPrecioAlgorithmics100","ValorPrecioAlgorithmics95","ValorPrecioAlgorithmics98",
			"ValorPrecioOperacionesPlazo01","ValorPrecioVentaCorta02","ValorOtrosPrecios03","ValorOtrosPrecios04","ValorOtrosPrecios05","ValorPrecioBase"
			FROM :v_Precios pre
			INNER JOIN :v_Registros reg on reg."IdInstrumento" = pre."IdInstrumento"
			WHERE reg."RegistroNuevo" = 'S';
	
	UPDATE vec
	SET
	"SecuenciaLista" 				= pre."SecuenciaLista",
	"Tipo" 							= pre."Tipo",
	"ValorPrecioAlgorithmics100" 	= pre."ValorPrecioAlgorithmics100",
	"ValorPrecioAlgorithmics95" 	= pre."ValorPrecioAlgorithmics95",
	"ValorPrecioAlgorithmics98" 	= pre."ValorPrecioAlgorithmics98",
	"ValorPrecioOperacionesPlazo01" = pre."ValorPrecioOperacionesPlazo01",
	"ValorPrecioVentaCorta02" 		= pre."ValorPrecioVentaCorta02",
	"ValorOtrosPrecios03" 			= pre."ValorOtrosPrecios03",
	"ValorOtrosPrecios04"		 	= pre."ValorOtrosPrecios04",
	"ValorOtrosPrecios05" 			= pre."ValorOtrosPrecios05",
	"ValorPrecioBase" 				= pre."ValorPrecioBase"
		
	FROM "BSCL.Plataforma.BD::INS.VectorPrecios" vec
	INNER JOIN :v_Precios pre on vec."IdInstrumento" = pre."IdInstrumento" and vec."FechaVector" = pre."FechaVector"
	INNER JOIN :v_Registros reg on reg."IdInstrumento" = pre."IdInstrumento" and pre."FechaVector" = reg."FechaVector" WHERE reg."RegistroNuevo" = 'N';

	IF (:p_limpiaTablas = 'S') THEN
		DELETE FROM "BSCL.Plataforma.BD::MER.CargaVectorPrecios";
	END IF;
	
	SELECT COUNT(1) INTO v_countNuevos FROM :v_Registros WHERE "RegistroNuevo" = 'S';
	SELECT COUNT(1) INTO v_countActualizados FROM :v_Registros WHERE "RegistroNuevo" = 'N';
	
	p_out_Resultado = 'EXITO';
	p_out_Mensaje = 'REGISTROS NUEVOS: ' || :v_countNuevos || ' INSTRUMENTO REGISTROS: ' || :v_countActualizados;

	
END;
