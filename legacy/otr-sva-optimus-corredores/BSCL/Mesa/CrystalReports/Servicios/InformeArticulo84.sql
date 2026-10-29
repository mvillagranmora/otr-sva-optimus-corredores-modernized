SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::InformeArticulo84";
GO
CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 19/12/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::InformeArticulo84" ( 
	--Entrada
	IN p_IdEmpresa  	INT,
	IN p_FechaDesde 	NVARCHAR(10),
	IN p_FechaHasta 	NVARCHAR(10),
	IN p_TipoProducto 	NVARCHAR(4),
	IN p_TipoLimite 	NVARCHAR(15)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_HayLimites	INT; 

	v_Limites = SELECT TO_CHAR(EVA."FechaIngreso",'YYYY-MM-DD') AS "Fecha"
					 , EVA."IdOperacion"						AS "FolioOperacion"
				     , EVA."IdLimite"							AS "Limite"
				     , CLI."DocumentoIdentidad"					AS "RutCliente"
				     , POR."CodigoPropositoPortafolio"			AS "Portafolio"
				     , PRO."Nombre" 							AS "Producto"
				     , OPE."Accion" 							AS "TipoOperacion"
				     , EST."Descripcion" 						AS "Estado"
				     , 1 										AS "Tipo"
				     , EVA."MontoUtilizado" 					AS "MontoReserva"
				     , EVA."IdTipoLimite"						AS "IdTipoLimite"
				     , :p_FechaDesde							AS "FechaDesde"
				     , :p_FechaHasta							AS "FechaHasta"
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEvaluacionLimite" AS EVA
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE
						ON OPE."IdTipoOperacion" = EVA."IdTipoOperacion"			
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" AS PRO
						ON PRO."IdTipoProducto" = OPE."IdTipoProducto"
				INNER JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEstadoLimites" AS EST
				        ON EST."IdEstado" = EVA."IdEstado"
				       AND EST."NombreEntidad" = 'EVALUACION'
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				    	ON EVA."IdCliente" = CLI."IdCliente"
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC
					    ON SOC."IdPersona" = CLI."IdPersona"	   
				INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
						ON POR."IdCliente" = EVA."IdCliente"
					   AND POR."IdPortafolio" = EVA."IdPortafolio"
					   AND POR."IdEstado" = 'ACT'
				WHERE EVA."IdTipoLimite" = CASE WHEN COALESCE(:p_TipoLimite,'') = '' THEN
										   		EVA."IdTipoLimite"
										   ELSE :p_TipoLimite
										   END
				  AND TO_CHAR(EVA."FechaIngreso",'YYYY-MM-DD') BETWEEN :p_FechaDesde AND :p_FechaHasta
				  AND OPE."IdTipoProducto" = CASE WHEN COALESCE(:p_TipoProducto,'') = '' THEN
										   		OPE."IdTipoProducto"
										     ELSE :p_TipoProducto
										     END 
				GROUP BY TO_CHAR(EVA."FechaIngreso",'YYYY-MM-DD')
					 , EVA."IdOperacion"
				     , EVA."IdLimite"
				     , CLI."DocumentoIdentidad"
				     , POR."CodigoPropositoPortafolio"
				     , PRO."Nombre"
				     , OPE."Accion"
				     , EST."Descripcion"
				     , EVA."MontoUtilizado"
				     , EVA."IdTipoLimite"
			 	ORDER BY TO_CHAR(EVA."FechaIngreso",'YYYY-MM-DD') DESC;
	BEGIN
	
		SELECT Count(1) INTO v_HayLimites 
		FROM :v_Limites; 	
		
		IF (v_HayLimites != 0) THEN
			SELECT * 
			FROM :v_Limites;
		ELSE
			SELECT
				  '00001-01-01' 		AS "Fecha"
				 , 0					AS "FolioOperacion"
				 , ''					AS "Limite"
				 , '0-0'				AS "RutCliente"
				 , ''					AS "Portafolio"
				 , '' 					AS "Producto"
				 , '' 					AS "TipoOperacion"
				 , '' 					AS "Estado"
				 , 1 					AS "Tipo"
				 , 0 					AS "MontoReserva"
				 , ''					AS "IdTipoLimite"
				 , :p_FechaDesde		AS "FechaDesde"
				 , :p_FechaHasta		AS "FechaHasta"		
			FROM DUMMY;
		END IF;
	END;
END;
GO