
SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.RechazarLiquidacionSpot";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.RechazarLiquidacionSpot"
  ( 
	IN p_IdEmpresa 			BIGINT,
    IN p_IdUsuario    		BIGINT,
    IN p_IdModulo   		NVARCHAR(3),
    IN p_UsaReglasNegocio	NVARCHAR(1),
    IN p_DatosOperacion		TABLE ( "NumeroOperacion" NVARCHAR(64) ),

     -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE (	"IdInterno"	NVARCHAR(40), 
											"IdDatatec" NVARCHAR(40), 
											"Codigo" NVARCHAR(100),
											"RequiereAutorizacion" NVARCHAR(40),
											"Descripcion" NVARCHAR(520) ),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE (	"IdInterno"	NVARCHAR(40), 
											"IdDatatec" NVARCHAR(40), 
											"Codigo" NVARCHAR(100),
											"Descripcion" NVARCHAR(520) )
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA   
AS
BEGIN
 

	DECLARE v_Parametro NVARCHAR(1);
	DECLARE v_NroErrores BIGINT;
	DECLARE v_OperacionYA_Rechazada BIGINT;
	
	
	--* Verifica que parametro este encendido
	--* -------------------------------------		
	SELECT	"Valor"
	INTO	v_Parametro
	FROM	"BSCL.Mesa.BD::MES.ConfiguracionMesa"
	WHERE	"CodigoParametro" = 'ENVIA_SPOT_A_CONFIRMAR_LIQUIDACION';

	--* Default NO
	v_Parametro = COALESCE( v_Parametro, 'N');

	--* Inicializar salidas
	p_out_ErroresContador = 0;
	p_out_AdvertenciasContador = 0;
	p_out_AdvertenciasDetalle = SELECT TOP 0 * FROM :p_out_AdvertenciasDetalle;

	IF v_Parametro = 'N' THEN
		p_out_ErroresContador = 1;
		p_out_ErroresDetalle =		
			SELECT	'' AS "IdInterno", 
					'' AS "IdDatatec", 
					'PRM_APAGADO' AS "Codigo", 
					'No esta habilitado parametro de Liquidacion Spot' AS "Descripcion"
			FROM	DUMMY;
	ELSE
		CALL "BSCL.Mesa.BD.Servicios::SPT.AnularOperacionSpot" (
			p_IdEmpresa => p_IdEmpresa
			,p_IdUsuario => p_IdUsuario
			,p_IdModulo => p_IdModulo
			,p_UsaReglasNegocio => p_UsaReglasNegocio
			,p_DatosOperacion => :p_DatosOperacion
			
			 -- Salidas para mensajes de advertencias y errores
			,p_out_AdvertenciasContador	=> p_out_AdvertenciasContador
			,p_out_AdvertenciasDetalle => p_out_AdvertenciasDetalle
			,p_out_ErroresContador => p_out_ErroresContador
			,p_out_ErroresDetalle => p_out_ErroresDetalle
		);


		IF p_out_ErroresContador = 0 THEN
		
			--* Todo OK => Marco la operacion como rechazada
			UPDATE	"BSCL.Mesa.BD::SPT.OperacionSpot" 
			SET		"EstadoConfirmacion" = 'RZD'
			WHERE	"IdOperacionSpot" IN ( SELECT "NumeroOperacion" FROM :p_DatosOperacion );

		END IF;
	END IF;
 END;