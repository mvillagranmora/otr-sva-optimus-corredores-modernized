SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarFechaVencimiento";

-- 
--  Proposito: 
--  Fecha Creacion: 02/12/2016
--  

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarFechaVencimiento"(
	IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    IN p_IniFecha					NVARCHAR(1),
    --ENUMERAR VARIABLES DE ENTRADA
    IN p_Fecha						NVARCHAR(10),
    IN p_NumeroDias					INTEGER,
    IN p_EsHabil					NVARCHAR(1),
	IN p_IdPais						BIGINT,
	
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_Fecha 				NVARCHAR(10),
	OUT p_out_Plazo					INT,
	OUT p_out_EsHabil 				NVARCHAR(1),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520)
									) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" nvarchar(40), "Descripcion" nvarchar(520)) DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta	INT DEFAULT 0;  
	DECLARE v_errorhashmsg	table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg	nvarchar(520);	
	DECLARE v_AvanzaORetrocede		NVARCHAR(1);
	DECLARE v_DiasAvanzaORetrocede	INT;
	DECLARE v_NumDias				INT;
	DECLARE v_EsHabil 				INTEGER;
	DECLARE v_DiaEsHabil 			NVARCHAR(1);
	DECLARE v_Fecha 				DATE;
	DECLARE v_DiaSemana				INT;
	DECLARE v_out_Fecha 			NVARCHAR(10);
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 	
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log ser captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_Plazo					:= 0;
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		
		SELECT "Valor" INTO v_AvanzaORetrocede
		FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
		WHERE "IdTipoProducto" = 'MEFW' AND "CodigoParametro" = 'FECHANOHABIL';
		
		SELECT "Valor" INTO v_DiasAvanzaORetrocede
		FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
		WHERE "IdTipoProducto" = 'MEFW' AND "CodigoParametro" = 'DIASFECHANOHABIL';
		
		
		--Se agrega cantidad de dias (:p_NumeroDias) a la Fecha Operacion
		SELECT ADD_DAYS(TO_DATE(:p_Fecha,'YYYY-MM-DD'),:p_NumeroDias) INTO v_out_Fecha FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(:v_out_Fecha, :p_IdPais, :v_DiaEsHabil);
		
		
		--Se verifica si nueva fecha es Habil
		--Si no lo es, se busca dia habil Anterior o Siguiente (parametro FECHANOHABIL)
		--de acuerdo a la cantidad de dias (parametro DIASFECHANOHABIL)
		IF (:v_DiaEsHabil = 'N') THEN
			
			--IF RETROCEDE cuando se cambia el Plazo en pantalla Ingreso FWD
			IF (:v_AvanzaORetrocede = 'R' AND :p_IniFecha = 'N') THEN
				select
					:v_AvanzaORetrocede,
					:p_IniFecha,
					'Parametros entrada ConsultarDiaAnterior' AS "Servicio",
					'p_Fecha : '||:v_out_Fecha AS "v_out_Fecha",
					'p_NumeroDias : '||:v_DiasAvanzaORetrocede AS "v_DiasAvanzaORetrocede"
				from DUMMY;
				
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarDiaAnterior"(
					 :p_IdEmpresa
					,:p_IdUsuario
					,:p_IdModulo
					,:p_UsaReglasNegocio
					,:v_out_Fecha				--:p_Fecha
					,:v_DiasAvanzaORetrocede	--:p_NumeroDias
					,:p_EsHabil
					,:p_IdPais
					--
					,:p_out_Fecha
					,:p_out_EsHabil
					,:p_out_AdvertenciasContador
					,:p_out_AdvertenciasDetalle
					,:p_out_ErroresContador
					,:p_out_ErroresDetalle
				);
				select 'Salida ConsultarDiaAnterior' AS "Servicio", :p_out_Fecha AS "p_out_Fecha" from DUMMY;
				
				
				select 'Diferencia Fechas',
					:p_Fecha AS "p_Fecha", :v_out_Fecha AS "v_out_Fecha", :p_out_Fecha AS "p_out_Fecha",
					'Entre p_Fecha y p_out_Fecha : '||DAYS_BETWEEN(TO_DATE(:p_Fecha,'YYYY-MM-DD'),TO_DATE(:p_out_Fecha,'YYYY-MM-DD')) AS "DIF_p_Fecha"
					--,'Entre v_out_Fecha y p_out_Fecha'||DAYS_BETWEEN(TO_DATE(:v_out_Fecha,'YYYY-MM-DD'),TO_DATE(:p_out_Fecha,'YYYY-MM-DD')) AS "DIF_v_out_Fecha"
				from DUMMY;
				
				IF (DAYS_BETWEEN(TO_DATE(:p_Fecha,'YYYY-MM-DD'),TO_DATE(:p_out_Fecha,'YYYY-MM-DD')) <= 0) THEN
					DECLARE v_PlazoVencimientoMinimo INT;
					
					SELECT "Valor" INTO v_PlazoVencimientoMinimo
					FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
					WHERE "IdTipoProducto" = 'MEFW' AND "CodigoParametro" = 'PLAZOVENCIMIENTOMINIMO';
					
					select
						:v_AvanzaORetrocede,
						:p_IniFecha,
						'Parametros entrada ConsultarDiaSiguiente IF DAYS_BETWEEN' AS "Servicio",
						:p_Fecha,
						'p_NumeroDias : '||:v_PlazoVencimientoMinimo AS "v_PlazoVencimientoMinimo"
					from DUMMY;
					
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarDiaSiguiente"(
						 :p_IdEmpresa
						,:p_IdUsuario
						,:p_IdModulo
						,:p_UsaReglasNegocio
						,:p_Fecha	--:p_Fecha	v_out_Fecha
						,:v_PlazoVencimientoMinimo	--:v_PlazoVencimientoMinimo	--:p_NumeroDias	--:v_DiasAvanzaORetrocede
						,:p_EsHabil
						,:p_IdPais
						--
						,:p_out_Fecha
						,:p_out_EsHabil
						,:p_out_AdvertenciasContador
						,:p_out_AdvertenciasDetalle
						,:p_out_ErroresContador
						,:p_out_ErroresDetalle
					);
					select 'Salida ConsultarDiaSiguiente' AS "Servicio", :p_out_Fecha AS "p_out_Fecha" from DUMMY;
					
				END IF;
				
			ELSEIF (:v_AvanzaORetrocede = 'A' OR (:v_AvanzaORetrocede = 'R' AND :p_IniFecha = 'S')) THEN
				select
					:v_AvanzaORetrocede,
					:p_IniFecha,
					'Parametros entrada ConsultarDiaSiguiente' AS "Servicio",
					'p_Fecha : '||:v_out_Fecha AS "v_out_Fecha", --:p_Fecha,
					'p_NumeroDias : '||:v_DiasAvanzaORetrocede AS "v_DiasAvanzaORetrocede"
				from DUMMY;
				
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarDiaSiguiente"(
					 :p_IdEmpresa
					,:p_IdUsuario
					,:p_IdModulo
					,:p_UsaReglasNegocio
					,:v_out_Fecha				--:p_Fecha
					,:v_DiasAvanzaORetrocede	--:p_NumeroDias
					,:p_EsHabil
					,:p_IdPais
					--
					,:p_out_Fecha
					,:p_out_EsHabil
					,:p_out_AdvertenciasContador
					,:p_out_AdvertenciasDetalle
					,:p_out_ErroresContador
					,:p_out_ErroresDetalle
				);
				select 'Salida ConsultarDiaSiguiente' AS "Servicio", :p_out_Fecha AS "p_out_Fecha" from DUMMY;
			END IF;
			
			SELECT DAYS_BETWEEN(TO_DATE(:p_Fecha,'YYYY-MM-DD'),TO_DATE(:p_out_Fecha,'YYYY-MM-DD')) INTO p_out_Plazo FROM DUMMY;
		
		--Si nueva fecha es dia Habil
		ELSE
			p_out_Fecha := :v_out_Fecha;
			p_out_Plazo := :p_NumeroDias;
		END IF;
		
		p_out_EsHabil := :v_DiaEsHabil;
		
	END;

END;
