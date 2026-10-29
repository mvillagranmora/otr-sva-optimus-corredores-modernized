SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemMantiene";

-- 
--  Proposito: Mantenedor de item
--  Autor: Sandra Jimenez - SJZ
--  Fecha Creacion: 19/11/2016  
-- 
  
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemMantiene" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdItem 		BIGINT, 
	IN p_IdTipoItem 	NVARCHAR(3), 
	IN p_Nombre 		NVARCHAR(200), 
	IN p_NombreCorto 	NVARCHAR(20), 
	IN p_Estado			NVARCHAR(3),
	IN p_TipoOperacion 	NVARCHAR(1), 
	
	-- Parametros salida del servicio
	OUT p_out_IdItem		BIGINT, 
    OUT p_out_RefId			BIGINT,
    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
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
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
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
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        

		
		--------------------
		--Validaciones
		--------------------
		
		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
		
	 		-- IF :p_TipoOperacion = 'C' THEN 
			
			-- IF EXISTS (SELECT * FROM "NOR_ilsConceptoConfiguracion" WHERE NOR_ilsConceptoConfiguracion."IdItem" = :pIdItem) OR EXISTS (SELECT * FROM "NOR_ilsItemConfiguracion" WHERE NOR_ilsItemConfiguracion."IdItem" = :pIdItem AND NOR_ilsItemConfiguracion."Estado" = 'V') THEN 
			
				-- SELECT 'EXISTE' FROM DUMMY;
			-- [Note:Modifier] Use FROM DUMMY if there is no FROM clause in the SELECT statement
			-- ELSE SELECT 'NO EXISTE' FROM DUMMY;
			-- [Note:Modifier] Use FROM DUMMY if there is no FROM clause in the SELECT statement
			-- END IF			
			
			-----------------------------
			-- Elimina 
			-----------------------------
			IF :p_TipoOperacion = 'E' THEN
			
				DECLARE v_ExisteItemConfiguracion 		INT;
				DECLARE v_ExisteConceptoConfiguracion 	INT;
				DECLARE SALIDA_CONTROLADA_NO_DATA 		CONDITION;
				DECLARE v_ErrorNoData  					NVARCHAR(100);
								
				DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
				BEGIN
					--salida/error/advertencia controlado por este servicio
					select count(1) into p_out_ErroresContador 
					from :p_out_ErroresDetalle;
					select count(1) into p_out_AdvertenciasContador 
					from :p_out_AdvertenciasDetalle;
				END;

				--------------------
				--VALIDACIONES
				--------------------			 
								
				v_ExisteItemConfiguracion := 0;				
				v_ExisteConceptoConfiguracion := 0;	
				
				SELECT	COUNT(1)
				  INTO	v_ExisteConceptoConfiguracion
				  FROM	"BSCL.Normativa.BD::ILS.ConceptoConfiguracion"
				 WHERE	"IdItem" = :p_IdItem;				 
		
			
				IF :v_ExisteConceptoConfiguracion > 0 THEN
					p_out_ErroresDetalle = SELECT '' AS "Codigo"
						                        , 'Debe eliminar el item del Concepto Configuracion.' AS "Descripcion"
			                        		 FROM DUMMY;
					SIGNAL SALIDA_CONTROLADA_NO_DATA; --validar
	
				 	--MensajeError := 'No se puede eliminar el Item, existen configuracion asociadas en [NOR_ilsConceptoConfiguracion] IdItem = ' || ltrim(:p_IdItem);
				ELSE 
								
					SELECT	COUNT(1)
					  INTO	v_ExisteItemConfiguracion
					  FROM	"BSCL.Normativa.BD::ILS.ItemConfiguracion" AS NOR_ilsItemConfiguracion
					 INNER JOIN "BSCL.Normativa.BD::ILS.Item" AS NOR_ilsItem
					 		 ON NOR_ilsItem."IdItem" = NOR_ilsItemConfiguracion."IdItem"
					 WHERE	NOR_ilsItemConfiguracion."IdItem" = :p_IdItem
					   AND 	NOR_ilsItemConfiguracion."Estado" = 'VIG'
					   AND  NOR_ilsItem."IdTipoItem" <> 'DIG';
					 
					IF :v_ExisteItemConfiguracion > 0 THEN 
						--MensajeError := 'No se puede eliminar el Item, existen configuracion asociadas en [NOR_ilsItemConfiguracion] IdItem = ' || ltrim(:pIdItem);
						p_out_ErroresDetalle = SELECT '' AS "Codigo"
						                            , 'Debe eliminar la configuracion del item.' AS "Descripcion"
			                        		   FROM DUMMY;
						SIGNAL SALIDA_CONTROLADA_NO_DATA; --validar
					ELSE 
						DELETE FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion"
						WHERE "IdItem" = :p_IdItem;
						    
						DELETE FROM "BSCL.Normativa.BD::ILS.Item" 
						WHERE "IdItem" = :p_IdItem;
					END IF;
				END IF;
			END IF;	
			
			-----------------------------
			-- Inserta 
			-----------------------------
			IF :p_TipoOperacion = 'I' THEN 	
				SELECT "BSCL.Normativa.BD.Secuencias::ILS.IdItem".NEXTVAL
			      INTO p_out_IdItem
			   	  FROM DUMMY;
					
				INSERT INTO "BSCL.Normativa.BD::ILS.Item"
						(
						"IdItem",
						"IdTipoItem",
						"Nombre",
						"NombreCorto",
						"Estado"
						) VALUES (
						:p_out_IdItem,
						:p_IdTipoItem,
						:p_Nombre,
						:p_NombreCorto, 
						:p_Estado
						);	
				p_out_IdItem := p_out_IdItem;						
			END IF;			
		
			-----------------------------
			-- Lectura 
			-----------------------------
			IF :p_TipoOperacion = 'L' THEN
				
					SELECT 
							"NOR_ilsItem"."IdItem" 			AS "IdItem",
							"NOR_ilsItem"."IdTipoItem" 		AS "Tipo", 
							"NOR_ilsItem"."Nombre" 			AS "Nombre",
							"NOR_ilsItem"."NombreCorto" 	AS "NombreCorto", 
							"NOR_ilsItem"."Estado" 			AS "Estado", 
							ltrim("NOR_ilsItem"."IdTipoItem") || ' - ' || "NOR_ilsItemTipo"."Nombre" AS "TipoItemNombre" 
					  FROM 	"BSCL.Normativa.BD::ILS.Item" 		AS "NOR_ilsItem" 
				INNER JOIN 	"BSCL.Normativa.BD::ILS.ItemTipo" 	AS "NOR_ilsItemTipo" 
						ON 	"NOR_ilsItem"."IdTipoItem" = "NOR_ilsItemTipo"."IdTipoItem" 
					 WHERE 	"NOR_ilsItem"."Estado" = 'V' 
					   AND 	("NOR_ilsItem"."IdItem" LIKE (RTRIM(:p_IdItem)) || '%' OR LTRIM(RTRIM(:p_IdItem)) = 0 OR :p_IdItem IS NULL) 
					   AND 	("NOR_ilsItem"."Nombre" LIKE (RTRIM(:p_Nombre)) || '%' OR LTRIM(RTRIM(:p_Nombre)) = '' OR :p_Nombre IS NULL) 			 
					 ORDER BY "NOR_ilsItem"."Nombre";
			END IF;	
					
			-----------------------------
			-- Modifica 
			-----------------------------
			IF :p_TipoOperacion = 'M' THEN
				DECLARE v_ExisteItem INT;
				v_ExisteItem := 0; 
			
				SELECT	COUNT(1)
				  INTO	v_ExisteItem
				  FROM	"BSCL.Normativa.BD::ILS.Item"
				 WHERE	"IdItem" = :p_IdItem;				 
		
				IF :v_ExisteItem = 0 THEN				
					DELETE FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" WHERE "IdItem" = :p_IdItem;
				END IF;
				
				UPDATE 	"BSCL.Normativa.BD::ILS.Item" 
				   SET 	"IdTipoItem" 	= :p_IdTipoItem,
						"Nombre" 		= :p_Nombre,
						"NombreCorto" 	= :p_NombreCorto,
						"Estado" 		= :p_Estado 
				 WHERE 	"IdItem" 		= :p_IdItem;
				
			END IF;
			
			--Inserta ItemConfiguracion Digitado
			 IF :p_IdTipoItem = 'DIG' AND (:p_TipoOperacion = 'I' OR :p_TipoOperacion = 'M') THEN
				 DECLARE v_existe_digitado INT;
				 DECLARE v_idItem INT;
				 IF :p_TipoOperacion = 'I' THEN  v_idItem := p_out_IdItem; ELSE v_idItem := :p_IdItem; END IF;
				 
				 SELECT COUNT(1) INTO v_existe_digitado FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" WHERE "TipoItem" ='D' AND "IdItem" = :v_idItem;
				 
				 IF :v_existe_digitado = 0 THEN
					 INSERT INTO "BSCL.Normativa.BD::ILS.ItemConfiguracion" 
					 VALUES("BSCL.Normativa.BD.Secuencias::ILS.IdItemConfiguracion".NEXTVAL, :v_idItem, 0, '', '', '', '', 0.00000000, '', 0.00000000, 'N', 'N', 'N', 'VIG', 'D');
				 END IF;
			 END IF;
 
		END;		
	END;	
END;