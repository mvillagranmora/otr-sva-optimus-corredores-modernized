SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.CrearEmisor";

CREATE PROCEDURE
--  Proposito: crear emisores 
--  Autor: - TEAM ORDENES 
--  Fecha Creacion: 20/07/2015 
 
"BSCL.Plataforma.BD.Servicios::MER.CrearEmisor" ( 
    
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.

	IN p_Emisores	TABLE(
						"IdInterno"			BIGINT,
						"Codigo"			NVARCHAR(40),
						"CodigoRegulador"	NVARCHAR(40)
								
	),
    
    OUT p_out_Resultado		TABLE(
    	"IdInterno"			BIGINT,
    	"IdInstrumento"		BIGINT,
    	"Nemotecnico"		NVARCHAR(40) 	
    ) DEFAULT EMPTY,						
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE maxIdInstrumento	BIGINT;
	
	SELECT MAX("IdInstrumento")  INTO maxIdInstrumento FROM "BSCL.Plataforma.BD::INS.Instrumento";
	
	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador = 0;
	p_out_AdvertenciasDetalle = select 0 as "IdInterno",'' as "Codigo", '' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle = select 0 as "IdInterno",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	/*
	---------------------------------------------------------------------------------------
	--validacion nemo en blanco
	sin_nemo = select "IdInterno"
						from :p_instrumentos p_ins 
						where COALESCE("Nemotecnico",'') = '';
	
	v_out_ErroresDetalle = select "IdInterno", 'NEMOTECNICO_VACIO' AS "Codigo"
							from :sin_nemo;
	---------------------------------------------------------------------------------------
	--validacion nemo repetido
	nemos_repetidos = select "IdInterno" 
						from :p_instrumentos p_ins 
						INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on p_ins."Nemotecnico" =  inst."Nemotecnico";
	
	
	v_out_ErroresDetalle = select "IdInterno", 'NEMOTECNICO_YA_EXISTE' AS "Codigo"
							from :nemos_repetidos
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------	
	--validacion nemo sin tipo instrumento
	nemo_sin_tipo_instrumento = select "IdInterno" 
						from :p_instrumentos p_ins 
						where COALESCE("IdTipoInstrumento",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'INSTRUMENTO_SIN_TIPO' AS "Codigo"
							from :nemo_sin_tipo_instrumento
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------	
	--validacion nemo sin emisor
	nemo_sin_emisor = select "IdInterno" 
						from :p_instrumentos p_ins 
						where COALESCE("IdEmisor",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'INSTRUMENTO_SIN_EMISOR' AS "Codigo"
							from :nemo_sin_emisor
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------	
	--validacion nemo sin emisor
	nemo_sin_moneda = select "IdInterno" 
						from :p_instrumentos p_ins 
						where COALESCE("IdMoneda",'') = '';
	
	
	v_out_ErroresDetalle = select "IdInterno", 'INSTRUMENTO_SIN_MONEDA' AS "Codigo"
							from :nemo_sin_moneda
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
	---------------------------------------------------------------------------------------					
	nemos_ok =	select "IdInterno" 
				from :p_instrumentos p_ins 
				except
				select "IdInterno" from :nemos_repetidos
				except
				select "IdInterno" from :nemo_sin_tipo_instrumento
				except
				select "IdInterno" from :nemo_sin_emisor
				except
				select "IdInterno" from :nemo_sin_moneda;
				
	v_instrumentos = SELECT p_ins."IdInterno",:maxIdInstrumento + 1 +  row_number()  over (order by p_ins."IdInterno") as "IdInstrumento",
							p_ins."Nemotecnico",
							COALESCE("ISIN",'') AS "ISIN",
							COALESCE("CorteMinimo",0) AS "CorteMinimo",
							COALESCE("EstaInscrito",'N') AS "EstaInscrito",
							COALESCE("RazonSocial",'') as "RazonSocial",
							COALESCE("FechaEmision",'0001-01-01') as "FechaEmision",
							COALESCE("FechaVencimiento",'0001-01-01') as "FechaVencimiento",
							"IdEmisor",
							COALESCE("IdTipoReajuste","IdMoneda") as "IdTipoReajuste",
							"IdMoneda",
							"IdTipoInstrumento",
							COALESCE("IdEstado",'INA') as "IdEstado",
							COALESCE("Tamano",0) as "Tamano",
							COALESCE("IdEntidadClasificadoraRiesgo",'') AS "IdEntidadClasificadoraRiesgo",
							COALESCE("ValorClasificacionRiesgo",'') AS "ValorClasificacionRiesgo"
							FROM :p_instrumentos p_ins 
							INNER JOIN :nemos_ok nemo on p_ins."IdInterno" = nemo."IdInterno";
	---------------------------------------------------------------------------------------
	INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" ("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","RazonSocial","FechaEmision",
	"FechaVencimiento","IdEmisor","IdTipoReajuste","IdMoneda","IdTipoInstrumento","IdEstado","Tamano","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo")
	SELECT "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","RazonSocial","FechaEmision",
	"FechaVencimiento","IdEmisor","IdTipoReajuste","IdMoneda","IdTipoInstrumento","IdEstado","Tamano","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo"
	FROM :v_instrumentos;
	
	v_tablaMensajes = select "IdInterno","Codigo",'' as "CodigoDato1", '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from :v_out_ErroresDetalle;
	
	CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_tablaMensajes,v_out_TablaMensajes);
	
	p_out_ErroresDetalle = select det."IdInterno",det."Codigo",outt."Descripcion" from :v_out_ErroresDetalle det INNER JOIN :v_out_TablaMensajes outt on outt."IdInterno" = det."IdInterno" and outt."Codigo" = det."Codigo";
	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
	
	p_out_Resultado = select "IdInterno","IdInstrumento","Nemotecnico" from :v_instrumentos;
	
	*/
	
END;
