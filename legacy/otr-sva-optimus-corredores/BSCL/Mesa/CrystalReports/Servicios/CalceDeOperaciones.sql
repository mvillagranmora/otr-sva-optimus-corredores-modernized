SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::CalceDeOperaciones";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::CalceDeOperaciones" ( 
	--Entrada
 	IN p_IdEmpresa				BIGINT,
	IN p_FechaCartera 			DATE,
	IN p_IdCliente				BIGINT,
	IN p_IdPortafolio			BIGINT,
	IN p_IdTipoDeInstrumento	VARCHAR(3),
	IN p_IdMonedaPrincipal 		VARCHAR(3),
	IN p_IdMonedaSecundaria 	VARCHAR(3),
	IN p_Usuario				BIGINT	   
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN

	DECLARE v_countOperaciones 	BIGINT;
	DECLARE v_usuario			VARCHAR(50);
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	
	SELECT "CodigoUsuario" INTO v_usuario FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto"
	WHERE "IdUsuario" = :p_Usuario;
	
	
	v_logo = SELECT TOP 1 "Binario" AS "LogoCorredora"
	        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	        WHERE "NombreImagen" = 'LOGO_CORREDOR' 
	        ORDER BY "Version" Desc;
	       
	v_OperacionesCalzadas = SELECT 
								 "PortafolioCliente"	AS "PortafolioCliente"
								,ConsOperCalz."IdOperacion"	AS "OperacionCalzada"
								,"FolioCarteraOri"		AS "FolioCarteraOri"
								,ConsOperCalz."FolioCartera" AS "FolioCartera"
								,TO_NVARCHAR(TO_DATE("FechaInicio"), 'DD/MM/YYYY')       AS "FechaInicio"
								,"Plazo" 				AS "PlazoOperacion"
								,DAYS_BETWEEN (TO_SECONDDATE((SELECT CON."Valor" 
								                              FROM "BSCL.Plataforma.BD::PLA.Configuracion" CON 
								                              WHERE CON."IdEmpresa" = CarLite."IdEmpresa" 
								                                AND CON."CodigoParametro" = 'PLA.FechaSistema'),'YYYY-MM-DD'),
								               TO_SECONDDATE((CASE 
								                                 WHEN CarLite."IdEstado" = 'ANT' OR (CarLite."IdEstado" = 'VCD' AND CarLite."EGRIdTipoOperacion" = 'ANTFWD') THEN CarLite."EGRFechaOperacion"
								                               ELSE CarLite."FechaVencimientoOperacion" 
								                               END), 'YYYY-MM-DD')
								              ) AS "PlazoRemanente"
								,"NombreCliente"		AS "NombreCliente"
								,"NombreModalidad"      AS "NombreModalidad"
								,ConsOperCalz."IdMonedaPrincipal"	AS "IdMonedaPrincipal"
								,ConsOperCalz."IdMonedaSecundaria"	AS "IdMonedaSecundaria"
								, CASE WHEN ConsOperCalz."IdTipoOperacion" = 'COMFWD' THEN "MontoOperacion" ELSE 0 END AS "MontoCompra"                                                                            
								, CASE WHEN ConsOperCalz."IdTipoOperacion" = 'VENFWD' THEN "MontoOperacion" ELSE 0 END AS "MontoVenta"
								,"MontoTotalCalce"		AS "CalceUtilizado"
								,"TipoInstrumento"		AS "TipoInstrumento"
								,v_usuario 				AS "Usuario"
								,'' 					AS "MensajeError"
								,Logo."LogoCorredora"	AS "Logo"			
							FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesCalzadas" AS ConsOperCalz
							  INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" AS CarLite
							     ON CarLite."FolioCartera" =   ConsOperCalz."FolioCartera" AND CarLite."IdEstado" = 'VIG'
							  LEFT JOIN :v_logo AS Logo ON (Logo."LogoCorredora" IS NOT NULL)
							WHERE (:p_IdCliente IS NULL OR ConsOperCalz."IdCliente" = :p_IdCliente)
							AND (:p_IdPortafolio IS NULL OR ConsOperCalz."IdPortafolio" = :p_IdPortafolio)
							AND ConsOperCalz."IdTipoInstrumento" = :p_IdTipoDeInstrumento
							AND ConsOperCalz."IdMonedaPrincipal" = :p_IdMonedaPrincipal
							AND ConsOperCalz."IdMonedaSecundaria" = :p_IdMonedaSecundaria
							AND ConsOperCalz."IdEstado" = 'VIG'
							AND CarLite."FechaVencimientoOperacion" >= :p_FechaCartera
							AND "MontoTotalCalce" > 0;
					
	
	SELECT COUNT(*) INTO v_countOperaciones  FROM :v_OperacionesCalzadas;

	IF (v_countOperaciones > 0) THEN
		BEGIN	
			SELECT * FROM :v_OperacionesCalzadas;				
		END;	
	ELSE 
		v_OperacionesCalzadasVacio = SELECT 
										 ''	AS "PortafolioCliente"
										,0	AS "OperacionCalzada"
										,0			AS "FolioCarteraOri"
										,0  AS "FolioCartera"
										,TO_NVARCHAR(TO_DATE(:p_FechaCartera), 'DD/MM/YYYY')       AS "FechaInicio" 
										,0 	AS "PlazoOperacion"
										,0 	AS "PlazoRemanente"
										,''	AS "NombreCliente"
										,'' AS "NombreModalidad"
										,''	AS "IdMonedaPrincipal"
										,''	AS "IdMonedaSecundaria"
										,0  AS "MontoCompra"                                                                            
										,0  AS "MontoVenta"
										,0  AS "CalceUtilizado"
										,''	AS "TipoInstrumento"
										,v_usuario 	AS "Usuario"
										,Logo."LogoCorredora"				AS "Logo"
										,'No se encontraron datos.' AS "MensajeError"
										FROM DUMMY
										INNER JOIN :v_logo AS Logo
											ON (Logo."LogoCorredora" IS NOT NULL);
		BEGIN	
			SELECT * FROM :v_OperacionesCalzadasVacio;				
		END;
	END IF;							
END;	