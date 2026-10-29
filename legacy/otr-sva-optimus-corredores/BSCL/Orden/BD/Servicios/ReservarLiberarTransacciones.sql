SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::ReservarLiberarTransacciones";

CREATE PROCEDURE 
-- 
--  Proposito: reservar/liberar Transacciones
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Orden.BD.Servicios::ReservarLiberarTransacciones"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
 
    IN p_Accion						NVARCHAR(1), -- R(RESERVAR), L(LIBERAR)
	IN p_Motivo						NVARCHAR(40),
	IN p_transacciones				TABLE ( "IdTransaccion" BIGINT),


	OUT p_out_transacciones				TABLE ( "IdTransaccion" BIGINT, "Resultado" NVARCHAR(1), "Codigo" NVARCHAR(40)),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
    )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_transacciones			= select 0 as "IdTransaccion" , '' as "Resultado", '' as "Codigo" from dummy where 1=2;
	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		CREATE LOCAL TEMPORARY TABLE #transac (
			"IdTransaccion" 	BIGINT,
			"Resultado" 			NVARCHAR(40), 
			"Codigo" 			NVARCHAR(40)

		);
		-----------------------------------------------------
		--Validaciones b�sicas (Codigos,Existencias,etc)
		-----------------------------------------------------
	
		INSERT INTO #transac
		SELECT "p_tran"."IdTransaccion", 
			CASE 
				WHEN "tran"."IdTransaccion" IS NULL THEN 'ERROR'
				WHEN "tran"."Bloqueado" = 'S' THEN 'ERROR' 
				WHEN "tran"."Reservado" = 'S' and p_Accion = 'R' THEN 'ERROR'
				WHEN "tran"."Reservado" = 'N' and p_Accion = 'R' THEN 'OK'
				WHEN "tran"."Reservado" = 'S' and p_Accion = 'L' THEN 'OK' 
				WHEN "tran"."Reservado" = 'N' and p_Accion = 'L' THEN 'ERROR' 		
			END as "Resultado",
			CASE 
				WHEN "tran"."IdTransaccion" IS NULL THEN 'TRANSACCION_NO_EXISTE'
				WHEN "tran"."Bloqueado" = 'S' THEN 'TRANSACCION_ESTA_BLOQUEADA'
				WHEN "tran"."Reservado" = 'S' and p_Accion = 'R' THEN 'TRANSACCION_YA_RESERVADA'
				WHEN "tran"."Reservado" = 'N' and p_Accion = 'R' THEN ''
				WHEN "tran"."Reservado" = 'S' and p_Accion = 'L' THEN '' 
				WHEN "tran"."Reservado" = 'N' and p_Accion = 'L' THEN 'TRANSACCION_ESTA_LIBERADA' 
				ELSE ''
			END as "Codigo"
		FROM :p_transacciones as "p_tran"
		LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Transaccion" as "tran" on "p_tran"."IdTransaccion" = "tran"."IdTransaccion";
	
		--------------------
		--Negocio Aqui
		--------------------    	    
		UPDATE trannn
	    SET "Reservado" = CASE 
	    		WHEN p_Accion = 'R' THEN 'S'
	    		WHEN p_Accion = 'L' THEN 'N' END,
	    	"IdUsuarioReserva" = CASE
	    		WHEN p_Accion = 'R' THEN :p_IdUsuario
	    		WHEN p_Accion = 'L' THEN 0 END,
	    	"MotivoReserva" = :p_Motivo
	    FROM "BSCL.Orden.BD::ORD.Transaccion" trannn INNER JOIN #transac AS "tran" ON trannn."IdTransaccion" = "tran"."IdTransaccion"
	    WHERE "tran"."Resultado" = 'OK' and "tran"."IdTransaccion" = trannn."IdTransaccion";
	    
	    
	    
		p_out_transacciones =	SELECT TR."IdTransaccion",TR."Resultado",TR."Codigo"
							  	from #transac as TR;

		DROP TABLE #transac;

	END ;	
END;
