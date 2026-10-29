		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::MRF.ConsultaPAccmmdd";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  
"BSCL.Mesa.BD.Servicios::MRF.ConsultaPAccmmdd" ( 
	IN p_IdEmpresa      BIGINT,		
	IN p_IdUsuario     	BIGINT,
	IN p_Fecha     		NVARCHAR(10),
	
   	OUT p_out_encabezadoPAccmmdd table (
			  "TipoRegistro"  			NVARCHAR(1)
			 ,"Fecha" 					NVARCHAR(8)
			 ,"Corredor" 				NVARCHAR(3)
			 ,"Descripcion" 			NVARCHAR(50)
			 ,"Filler" 					NVARCHAR(130)
   	),
	
   	OUT p_out_registrosPAccmmdd table (
			 "TipoRegistro" 			NVARCHAR(1)
			,"TipoCompromiso"			NVARCHAR(10)
			,"IndicadorCobertura"       NVARCHAR(1)
			,"FechaOperacion"			NVARCHAR(10)
			,"NombreCliente"			NVARCHAR(50)
			,"RutCliente"  				NVARCHAR(11)
			,"Relacionado"			 	NVARCHAR(1)
			,"Mercado"  				NVARCHAR(2)
			,"Nemotecnico"  			NVARCHAR(12)
			,"Emisor" 					NVARCHAR(10)
			,"TipoReajusteInstrumento" 	NVARCHAR(2)
			,"Unidades" 				DECIMAL(26,8)
			,"NumeroOperacion" 			BIGINT
			,"FechaVencimientoPacto" 	NVARCHAR(10)
			,"FechaVencimiento" 		NVARCHAR(10)
			,"MontoPacto" 				DECIMAL(26,8)
			,"TipoMonedaPacto"  		NVARCHAR(2)
			,"MontoOperacion"			DECIMAL(26,8)
			,"TasaPacto"				DECIMAL(26,8)
   	),
   	
   	OUT p_out_piePAccmmdd table (
   			"TipoRegistro"				NVARCHAR(1)
   			,"TotalRegistrosCargados"	NVARCHAR(8)
   			,"TotalMontoOperacion"		NVARCHAR(18)
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
		
		p_out_encabezadoPAccmmdd =
			SELECT
				  '1' 															AS "TipoRegistro"
				 ,REPLACE(:p_Fecha, '-', '')									AS "Fecha"
			 	,:CodigoCorredora												AS "Corredor"
			 	,RPAD('Compromisos de compra y venta de instrumentos',50,' ') 	AS "Descripcion"
			 	,RPAD('',130,' ') 												AS "Filler"
			FROM DUMMY;
		
		v_quincenalDePactos = SELECT * FROM "BSCL.Mesa.BD::MES.QuincenalPactos";
		
		p_out_registrosPAccmmdd =
			SELECT DISTINCT
				 '2' 														AS "TipoRegistro"
				,CASE 	WHEN QP."IdTipoOperacion" = 'VENPAC'
							THEN 'VRC' 
						WHEN QP."IdTipoOperacion" = 'COMPAC'
							THEN 'CRV'
						ELSE
							'   '
				END  														AS "TipoCompromiso"
				,COALESCE(QP."IndicadorCobertura", ' ')						AS "IndicadorCobertura"
				,COALESCE(TO_NVARCHAR(TO_DATE(QP."FechaInicioCompromiso", 'DD-MM-YYYY'), 'YYYY-MM-DD'),' ')     	AS "FechaOperacion"
				,LEFT(COALESCE((	SELECT 
						"NombreCompleto"
					FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
					WHERE "DocumentoIdentidad" = "RutCliente"), ' '),50) 	AS "NombreCliente"
				,COALESCE(LPAD(QP."RutCliente",11,' '),' ')					AS "RutCliente"
				,COALESCE(QP."IdRelacionCorredor", ' ')						AS "Relacionado"
				,COALESCE(QP."ClasificacionTipoInstrumento", '  ')			AS "Mercado"
				,QP."Nemo" 													AS "Nemotecnico"
				,QP."Emisor" 												AS "Emisor" 
				,CASE	WHEN QP."TipoReajuste" = 'CLP' 
							THEN '$'
						WHEN QP."TipoReajuste" = 'USD'
							THEN 'US'
						WHEN QP."TipoReajuste" != 'CLP' 
						AND QP."TipoReajuste" != 'USD'
							THEN SUBSTRING(QP."TipoReajuste",1,2)
				END															AS "TipoReajusteInstrumento"
				,QP."MontoNominal" 											AS "Unidades"
				,QP."FolioOperacion" 											AS "NumeroOperacion"
				,COALESCE(TO_VARCHAR(TO_DATE(QP."FechaVencimientoCompromiso", 'DD-MM-YYYY'), 'YYYY-MM-DD'), ' ')	AS "FechaVencimientoPacto"
				,COALESCE(TO_VARCHAR(TO_DATE(QP."FechaVencimientoInstrumento", 'DD-MM-YYYY'), 'YYYY-MM-DD'), ' ')	AS "FechaVencimiento"
				,QP."CantidadComprometida" 									AS "MontoPacto"
				,CASE	WHEN QP."TipoReajustePacto" = 'CLP' 
							THEN '$'
						WHEN QP."TipoReajustePacto" = 'USD'
							THEN 'US'
						WHEN QP."TipoReajustePacto" != 'CLP' 
						AND QP."TipoReajustePacto" != 'USD'
							THEN SUBSTRING(QP."TipoReajustePacto",1,2)
				END															AS "TipoMonedaPacto"
				,QP."CantidadComprometida" 									AS "MontoOperacion"
				,QP."TasaPacto" 											AS "TasaPacto"
				FROM :v_quincenalDePactos QP
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP ON QP."FolioOperacion" = OP."FolioOperacion"
				WHERE OP."IdEstado" = 'VIG';

		p_out_piePAccmmdd =
			SELECT 
   				'9' 														AS "TipoRegistro"				
   				,LPAD(COUNT(QP."IdQuincenalPactos"),8,' ')						AS "TotalRegistrosCargados"	
   				,LPAD(SUM(QP."CantidadComprometida"),18,' ')					AS "TotalMontoOperacion"		
   				,RPAD('',165,' ')											AS "Filler"
   			FROM :v_quincenalDePactos QP
 			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP ON QP."FolioOperacion" = OP."FolioOperacion"
 			WHERE OP."IdEstado" = 'VIG';
		
		SIGNAL SALIDA_CONTROLADA;		     
END;