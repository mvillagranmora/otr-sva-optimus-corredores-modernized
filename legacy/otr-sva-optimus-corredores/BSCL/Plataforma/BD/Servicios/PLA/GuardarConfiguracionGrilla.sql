SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.GuardarConfiguracionGrilla" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para guardar las configuraciones de grilla por defecto y por empresa.
--  Autor: RCATALAN - BCS
--  Fecha Creacion: 16/02/2017
-- 

"BSCL.Plataforma.BD.Servicios::PLA.GuardarConfiguracionGrilla" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
		  
	-- Parametros entrada del servicio publico
	IN  p_Configuracion			TABLE("Llave" NVARCHAR(5000), "Valor" NVARCHAR(5000)),
		
	-- Parametros salida del servicio publico
    OUT p_out_Resultado 		NVARCHAR(3),
    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
				
		v_operaciones = SELECT
		CASE WHEN COALESCE(CU."IdConfiguracionGrilla",0) = 0 THEN "BSCL.Plataforma.BD.Secuencias::PLA.ConfiguracionGrilla".NEXTVAL ELSE CU."IdConfiguracionGrilla" END AS "IdConfiguracionGrilla",
		CASE WHEN COALESCE(CU."IdEmpresa",0) = 0 THEN p_IdEmpresa ELSE CU."IdEmpresa" END AS "IdEmpresa",
		C."Llave",
		C."Valor",
		CASE WHEN COALESCE(CU."IdConfiguracionGrilla",0) = 0 THEN 'N' ELSE 'S' END AS "Existe"
		FROM "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla" CU
		RIGHT JOIN :p_Configuracion C ON CU."IdEmpresa" = p_IdEmpresa AND CU."Llave" = C."Llave";
		
		--------------------
		--Negocio Aqui
		--------------------
		BEGIN
			--Actualizar los registros que existen
			UPDATE CU
				SET
				CU."Valor" = C."Valor"
			FROM "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla" CU, :v_operaciones C
			WHERE CU."Llave" = C."Llave" AND CU."IdEmpresa" = p_IdEmpresa AND C."Existe" = 'S';
			
			--Crear los registros que no existen	
			INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla"("IdConfiguracionGrilla", "IdEmpresa", "Llave", "Valor")
			SELECT 
				"IdConfiguracionGrilla",
				"IdEmpresa",
				"Llave",
				"Valor"
			FROM :v_operaciones c where c."Existe" = 'N';
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;