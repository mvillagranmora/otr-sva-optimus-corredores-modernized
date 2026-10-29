SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::MRF.QuincenalDePactos";
GO
CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::MRF.QuincenalDePactos" ( 
	--Entrada
	IN p_Fecha 			DATE,
 	IN p_IdEmpresa		BIGINT,
	IN p_IdUsuario		BIGINT	   
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN

	DECLARE v_countOperaciones 			BIGINT;
	DECLARE v_usuario					VARCHAR(50);
	DECLARE v_out_FolioNemo 			NVARCHAR(1);
	DECLARE v_out_subyacenteFolio	 	TABLE ("FolioCarteraRelacionada" BIGINT,"Cantidad" BIGINT);
	DECLARE v_out_subyacenteNemo	 	TABLE ("Nemotecnico" NVARCHAR(10),"IdEmisor" NVARCHAR(40),"FechaVencimiento" DATE,"IdMoneda"	NVARCHAR(3),"Cantidad"	BIGINT);
	DECLARE v_out_countQuincenales		BIGINT;	
	DECLARE v_out_AdvertenciasContador	INT;
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520));
	DECLARE v_out_ErroresContador 		INT;
	DECLARE v_out_ErroresDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520));
	
	CALL "BSCL.Mesa.BD.Servicios::MRF.ConsultarQuincenalDePactos"(:p_Fecha, :p_IdEmpresa, :p_IdUsuario, :v_out_FolioNemo, :v_out_quincenalDePactos, :v_out_subyacenteFolio, :v_out_subyacenteNemo, :v_out_countQuincenales, :v_out_AdvertenciasContador,:v_out_AdvertenciasDetalle,:v_out_ErroresContador, :v_out_ErroresDetalle);
			
	v_quincenalPactos =		SELECT 
								CASE WHEN "IdTipoOperacion" = 'VENPAC' 
										THEN 'VRC' 
									 WHEN "IdTipoOperacion" = 'COMPAC' 
										THEN 'CRV' 
								END 														AS "Codigo"  
								,"FechaInicioCompromiso" 									AS "FechaOperacion"
								,(SELECT 
									"NombreCompleto" 
								  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
								  WHERE "DocumentoIdentidad" = "RutCliente") 				AS "NombreCliente"
								,CASE WHEN "IdRelacionCorredor" = 'S' THEN '*' ELSE '' END 	AS "REL"
								,CONCAT("TipoInstrumento",CONCAT('-',"Emisor") )				AS "Instrumento"
								,"MontoNominal" 											AS "UnidadesTransadas"
								,"FolioOperacion" 											AS "NumeroPacto"
								,"FechaVencimientoCompromiso" 								AS "FechaPacto"
								,"FechaVencimientoInstrumento" 								AS "FechaVencimiento"
								,"ValorPresente" 											AS "PrecioPacto"		--Pendiente
								,"IdMoneda" 												AS "Moneda"
								,"FolioOperacion" 											AS "NroCompraOperacion"
								,"ValorComprometido" 										AS "PrecioOperacion" 	--Pendiente
								,"TasaPacto" 												AS "Renta"	
								,"IndicadorCobertura" 										AS "IndicadorCobertura"
								,"TipoAmortizacion"											AS "TipoAmortizacion"
								,"CategoriaRiesgo"											AS "CategoriaRiesgo"
								,"ValorFinal"												AS "ValorFinal"
								,"TipoReajustePacto"										AS "MonedaPacto"
								,"TipoReajuste"												AS "MonedaDetalle"
								,"DecimalesPacto"											AS "DecimalesPacto"
								,"DecimalesDetalle"											AS "DecimalesDetalle"
								,"MontoFinal"												AS "MontoFinal"
								,"PrecioPacto"												AS "PrecioDePacto"
								,:p_Fecha													AS "FechaParametro"
								,"Grupo"													AS "Grupo"
							FROM :v_out_quincenalDePactos;
		
	SELECT COUNT(*) INTO v_countOperaciones  FROM :v_quincenalPactos;

	IF (v_countOperaciones > 0) THEN
		BEGIN	
			SELECT * FROM :v_quincenalPactos;
		END;	
	ELSE 
		v_QuincenalDePactosVacio = 	SELECT 
			 						'' 							AS "Codigo"  
									,'1900-01-01' 				AS "FechaOperacion"
									,'' 						AS "NombreCliente"
									,'' 						AS "REL"
									,'' 						AS "Instrumento"
									,0 							AS "UnidadesTransadas"
									,0 							AS "NumeroPacto"
									,'1900-01-01' 				AS "FechaPacto"
									,'1900-01-01'			 	AS "FechaVencimiento"
									,0 							AS "PrecioPacto"
									,'' 						AS "Moneda"
									,'' 						AS "NroCompraOperacion"
									,0 							AS "PrecioOperacion"
									,0 							AS "Renta"
									,'' 						AS "IndicadorCobertura"
									,2							AS "TipoAmortizacion"
									,'' 						AS "CategoriaRiesgo"
									,'No se encontraron datos.' AS "MensajeError" 
									,''							AS "MonedaPacto"
									,''							AS "MonedaDetalle"
									,0							AS "DecimalesPacto"
									,0							AS "DecimalesDetalle"
									,0							AS "MontoFinal"
									,0							AS "PrecioDePacto"
									,:p_Fecha					AS "FechaParametro"
									,'N'						AS "Grupo"
								FROM DUMMY;
		BEGIN	
			SELECT * FROM :v_QuincenalDePactosVacio;				
		END;
	END IF;				
					
END;


				
			
