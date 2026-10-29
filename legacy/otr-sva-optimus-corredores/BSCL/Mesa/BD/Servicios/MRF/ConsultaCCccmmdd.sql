SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::MRF.ConsultaCCccmmdd";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  
"BSCL.Mesa.BD.Servicios::MRF.ConsultaCCccmmdd" ( 
	IN p_IdEmpresa      BIGINT,		
	IN p_IdUsuario     	BIGINT,
	IN p_Fecha     		NVARCHAR(10),
	
   	OUT p_out_encabezadoCCccmmdd table (
			  "TipoRegistro"  			NVARCHAR(1)
			 ,"Fecha" 					NVARCHAR(8)
			 ,"Corredor" 				NVARCHAR(3)
			 ,"Descripcion" 			NVARCHAR(50)
			 ,"Filler" 					NVARCHAR(130)
   	),
	
   	OUT p_out_registrosCCccmmdd table (
			 "TipoRegistro" 			NVARCHAR(1)
			,"RutCliente"  				NVARCHAR(11)
			,"Relacionado"			 	NVARCHAR(1)
			,"NombreCliente"			NVARCHAR(50)
			,"SumaMontoPacto" 			BIGINT
			,"Patrimonio"				DECIMAL(26,8)
   	),
   	
   	OUT p_out_pieCCccmmdd table (
   			"TipoRegistro"				NVARCHAR(1)
   			,"TotalNumeroClientes"		BIGINT
   			,"TotalSumaMontosPactos"	DECIMAL(26,8)
   			,"Filler"					NVARCHAR(165)
   	)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION

		--- Controlado
		DECLARE IdCliente 						BIGINT;
		DECLARE IdPersona 						BIGINT;
		DECLARE CodigoCorredora 				NVARCHAR(3);
		DECLARE v_out_AdvertenciasContador		INT;
		DECLARE v_out_AdvertenciasDetalle		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520));
		DECLARE v_out_ErroresContador 			INT;
		DECLARE v_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520));
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
		END;	
		
		
		SELECT 
            "IdCliente" INTO IdCliente
        FROM "BSCL.Mesa.BD::MES.Mesa" 
        WHERE "PorDefecto" = 'S' 
        AND "IdTipoProducto"='MERF';
		
		SELECT 
             "IdPersona" INTO IdPersona
        FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
        WHERE "IdCliente" = :IdCliente;
        
        SELECT DISTINCT
        	LPAD("CodigoCorredora",3,'0') INTO CodigoCorredora
        FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos"
        WHERE "IdPersona" = :IdPersona;
		
		p_out_encabezadoCCccmmdd =
			SELECT
				  '1' 															AS "TipoRegistro"
				 ,REPLACE(:p_Fecha, '-', '')									AS "Fecha"
			 	,:CodigoCorredora												AS "Corredor"
			 	,RPAD('Clientes con Compromisos vigentes',50,' ') 				AS "Descripcion"
			 	,RPAD('',24,' ') 												AS "Filler"
			FROM DUMMY;
		

		v_quincenalDePactos = SELECT * FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarQuincenalDePactos";
		
		p_out_registrosCCccmmdd =
			SELECT DISTINCT
				 '2' 														AS "TipoRegistro"
				,COALESCE(LPAD("RutCliente",11,' '),' ')					AS "RutCliente"
				,COALESCE("IdRelacionCorredor", ' ')						AS "Relacionado"
				,LEFT(COALESCE((	SELECT 
						"NombreCompleto"
					FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
					WHERE "DocumentoIdentidad" = "RutCliente"), ' '), 50)	AS "NombreCliente"
				,COALESCE("CantidadComprometida", 0) 						AS "SumaMontoPacto"
				,COALESCE(CPT."Maximo", 0)									AS "Patrimonio"
			FROM :v_quincenalDePactos QP
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
			ON QP."RutCliente" = CC."DocumentoIdentidad"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP
			ON CC."IdPersona" = CP."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio" CPT
			ON CP."IdRangoPatrimonio" = CPT."IdRangoPatrimonio"
			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP ON QP."FolioOperacion" = OP."FolioOperacion"
			WHERE OP."IdEstado" = 'VIG';


		p_out_pieCCccmmdd =
			SELECT 
   				'9' 														AS "TipoRegistro"				
   				,COALESCE(COUNT(CQP."IdQuincenalPactos"),0)						AS "TotalNumeroClientes"	
   				,COALESCE(SUM(CQP."CantidadComprometida"),0) 					AS "TotalSumaMontosPactos"		
   				,RPAD('',54,' ')											AS "Filler"
   			FROM :v_quincenalDePactos CQP
 			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP ON CQP."FolioOperacion" = OP."FolioOperacion"
 			WHERE OP."IdEstado" = 'VIG';
   							
   	
		
		SIGNAL SALIDA_CONTROLADA;		     
END;