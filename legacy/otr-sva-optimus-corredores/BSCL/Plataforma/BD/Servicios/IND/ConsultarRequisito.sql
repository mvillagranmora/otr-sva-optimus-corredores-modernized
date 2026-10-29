SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::IND.ConsultarRequisito";

CREATE PROCEDURE
-- 
--  Proposito: Devuelve Indicadores de la entidad especificada
--  Autor: - CFLORES
--  Fecha Creacion: 13/04/2016
--  
"BSCL.Plataforma.BD.Servicios::IND.ConsultarRequisito" (
IN p_IdEmpresa				BIGINT,
IN p_IdEntidad				BIGINT			DEFAULT 0,
IN p_IdTipoEntidad			NVARCHAR(40)	DEFAULT '', ---CLIENTE/PORTAFOLIO
IN p_CodigoTipoIndicador  	NVARCHAR(10)	DEFAULT '', 
IN p_GrupoIndicador       	NVARCHAR(40) 	DEFAULT '',
IN p_ModeloCliente			NVARCHAR(40)	DEFAULT '', ---<TipoCliente>
IN p_IdIndicador			BIGINT			DEFAULT 0,

OUT p_out_IndicadoresContador BIGINT,
OUT p_out_Indicadores table ("IdIndicador"    		BIGINT
							,"IdTipoIndicador" 		BIGINT
							,"NombreTipoIndicador" 	NVARCHAR(255)
							,"ClaseDato"			NVARCHAR(40)
							,"CodigoTipoIndicador" 	NVARCHAR(10)
							,"RelacionDocumento"	NVARCHAR(1)
							,"ValorIndicador"		NVARCHAR(255)
							,"IdEstadoAutorizacion"	NVARCHAR(3)
							,"GrupoIndicador"		NVARCHAR(40)
							,"IdUso"				NVARCHAR(3)
						   	,"CantidadDecimales"	BIGINT
						    ,"LongitudMinima"		BIGINT
						  	,"LongitudMaxima"		BIGINT
						  	,"IdTipoEntidad" 		NVARCHAR(40)
							,"IdEntidad"			BIGINT
							,"ModeloCliente"		NVARCHAR(40)
						  	,"IdTipoPersona"		NVARCHAR(2)   
						   ),
OUT	p_out_GrupoIndicadoresContador BIGINT,					   
OUT p_out_GrupoIndicadores TABLE ("GrupoIndicador" NVARCHAR(40))
				   
						   )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS

BEGIN
	DECLARE v_IdUso				NVARCHAR(3):='REQ'; --Se buscan solo indicadores para la Logica de Negocio
	DECLARE v_IdEntidadBase		BIGINT:=0;
	DECLARE v_IdTipoCliente	 	NVARCHAR(40):='';
	DECLARE v_Tab_Ind_TipoCliente	table ("IdIndicador"  BIGINT
											,"IdTipoIndicador" 		BIGINT
											,"NombreTipoIndicador" 	NVARCHAR(255)
											,"ClaseDato"			NVARCHAR(40)
											,"CodigoTipoIndicador" 	NVARCHAR(10)
											,"RelacionDocumento"	NVARCHAR(1)
											,"ValorIndicador"		NVARCHAR(255)
											,"IdEstadoAutorizacion"	NVARCHAR(3)
											,"GrupoIndicador"		NVARCHAR(40)
											,"IdUso"				NVARCHAR(3)
											,"CantidadDecimales"	BIGINT
											,"LongitudMinima"		BIGINT
											,"LongitudMaxima"		BIGINT
											,"IdTipoEntidad" 		NVARCHAR(40)
											,"IdEntidad"			BIGINT
											,"ModeloCliente"		NVARCHAR(40)
										  	,"IdTipoPersona"		NVARCHAR(2)   
										  );	
	DECLARE V_IdIndicadorCero NVARCHAR(1):='N';				
	p_out_GrupoIndicadores = SELECT TPO."GrupoIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR
	ON (ATR."IdIndicador" = TPO."IdTipoIndicador"
		 AND (ATR."IdEntidad" = p_IdEntidad OR p_IdEntidad = 0 )
 	 	 AND (p_ModeloCliente = '' OR  (ATR."ModeloCliente"= p_ModeloCliente AND ATR."IdEntidad" =0 ) )--Si viene modelo cliente el Id de Entidad siempre es 0
      	 AND (p_IdIndicador = 0 OR  ATR."IdIndicador"= p_IdIndicador)
		 ) 
		 
		 
	WHERE (p_IdTipoEntidad = '' OR TPO."IdTipoEntidad" = p_IdTipoEntidad)
	AND TPO."IdUso" = v_IdUso
	AND TPO."IdEstado" = 'ACT'
	AND TPO."IdEmpresa" = p_IdEmpresa
	AND (p_CodigoTipoIndicador = '' OR  TPO."CodigoTipoIndicador"= p_CodigoTipoIndicador)
	AND (p_GrupoIndicador = '' OR  TPO."GrupoIndicador"= p_GrupoIndicador)
	GROUP BY TPO."GrupoIndicador";
	
	select count(1) into p_out_GrupoIndicadoresContador from :p_out_GrupoIndicadores;
	
	p_out_Indicadores =
	SELECT IFNULL(ATR."IdIndicador",0) AS "IdIndicador",
		TPO."IdTipoIndicador",
		TPO."NombreTipoIndicador", 
		TPO."ClaseDato", 
		TPO."CodigoTipoIndicador",
		CASE WHEN DOC."IdTipoDocumento" IS NULL THEN 'N' ELSE 'S' END as "RelacionDocumento",
		IFNULL(ATR."ValorIndicador",TPO."ValorIndicadorDefecto") as "ValorIndicador", 
		IFNULL(ATR."IdEstadoAutorizacion",'ACT') as "IdEstadoAutorizacion",
		TPO."GrupoIndicador",
		TPO."IdUso",
		TPO."CantidadDecimales",
		TPO."LongitudMinima",
		TPO."LongitudMaxima",
		TPO."IdTipoEntidad",
		IFNULL(ATR."IdEntidad",0) AS "IdEntidad",
		IFNULL(ATR."ModeloCliente",'') AS "ModeloCliente",
	  	TPO."IdTipoPersona"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR
	ON (ATR."IdTipoIndicador" = TPO."IdTipoIndicador" AND (ATR."IdEntidad" = p_IdEntidad OR p_IdEntidad = 0 )
		AND (p_ModeloCliente = '' OR  (ATR."ModeloCliente"= p_ModeloCliente AND ATR."IdEntidad"=0))	--Si viene modelo cliente el Id de Entidad siempre es 0
		AND (p_IdIndicador = 0 OR  ATR."IdIndicador"= p_IdIndicador)	
	) 
	LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" DOC ON TPO."CodigoTipoIndicador" = DOC."CodigoTipoIndicador"
	WHERE (p_IdTipoEntidad = '' OR TPO."IdTipoEntidad" = p_IdTipoEntidad)
	AND TPO."IdUso" = v_IdUso
	AND TPO."IdEstado" = 'ACT'
	AND (p_CodigoTipoIndicador = '' OR  TPO."CodigoTipoIndicador"= p_CodigoTipoIndicador)
	AND (p_GrupoIndicador = '' OR  TPO."GrupoIndicador"= p_GrupoIndicador)
	AND TPO."IdEmpresa" = p_IdEmpresa;
	
	/*PORTAFOLIO: Al existir indicadores sin valor, se Obtiene valor de indicadores desde modelo TIPOCLIENTE */
	--IF p_IdTipoEntidad='PORTAFOLIO' OR p_IdTipoEntidad='' THEN
	
		IF p_IdEntidad > 0 THEN
			---Identificacion del cliente segun portafolio
			v_IdEntidadBase:=p_IdEntidad;
			
			
			if  p_IdTipoEntidad='PORTAFOLIO' OR  p_IdTipoEntidad='CLIENTE' THEN
				if  p_IdTipoEntidad='PORTAFOLIO' THEN
					SELECT "IdCliente" INTO v_IdEntidadBase FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 	 WHERE "IdPortafolio"=p_IdEntidad;
				END IF;
				
  				---Identificacmos el tipo de cliente
				SELECT "IdTipoCliente" INTO v_IdTipoCliente FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE "IdCliente"=v_IdEntidadBase;
				
			END IF;
			
			/* Si NO se envia modelo de cliente y se envia Id de Entidad, el retorno de Id Endicador siempre es cero*/
			/* al obtener valores desde modelo cliente (Base)*/
			IF p_ModeloCliente = '' THEN
				V_IdIndicadorCero := 'S';
			END IF; 
	
		END IF;
		
		/*Se envian valores configurados como modelo cliente (Base). para aquellos indicadores sin valor en la entidad*/
		
		v_Tab_Ind_TipoCliente =
				SELECT (CASE WHEN V_IdIndicadorCero = 'S' THEN 0 ELSE (IFNULL(ATR."IdIndicador",0)) END) AS "IdIndicador",
						TPO."IdTipoIndicador",
						TPO."NombreTipoIndicador", 
						TPO."ClaseDato", 
						TPO."CodigoTipoIndicador",
						CASE WHEN DOC."IdTipoDocumento" IS NULL THEN 'N' ELSE 'S' END as "RelacionDocumento",
						IFNULL(ATR."ValorIndicador",TPO."ValorIndicadorDefecto") as "ValorIndicador", 
						IFNULL(ATR."IdEstadoAutorizacion",'ACT') as "IdEstadoAutorizacion",
						TPO."GrupoIndicador",
						TPO."IdUso",
						TPO."CantidadDecimales",
						TPO."LongitudMinima",
						TPO."LongitudMaxima",
						TPO."IdTipoEntidad",
						IFNULL(ATR."IdEntidad",0) AS "IdEntidad",
						IFNULL(ATR."ModeloCliente",'') AS "ModeloCliente",
					  	TPO."IdTipoPersona"						
				FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
				LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR
				 ON  (--indicadores segun tipo de cliente
						(ATR."IdTipoIndicador" = TPO."IdTipoIndicador")
					AND (p_ModeloCliente = '' OR  (ATR."ModeloCliente"= p_ModeloCliente AND ATR."IdEntidad"=0 ))	--Si viene modelo cliente el Id de Entidad siempre es 0
					AND (p_IdIndicador = 0 OR  ATR."IdIndicador"= p_IdIndicador))	
				INNER JOIN :p_out_Indicadores IND ON  --- Indicadores sin valor
				(IND."CodigoTipoIndicador" = TPO."CodigoTipoIndicador" AND IND."GrupoIndicador" = TPO."GrupoIndicador"
				AND ATR."ModeloCliente"= (CASE WHEN v_IdTipoCliente ='' THEN ATR."ModeloCliente" ELSE v_IdTipoCliente END)) 
				LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" DOC ON TPO."CodigoTipoIndicador" = DOC."CodigoTipoIndicador"
				WHERE TPO."IdTipoEntidad" = (CASE WHEN p_IdTipoEntidad ='' THEN ATR."IdTipoEntidad" ELSE p_IdTipoEntidad END) --'CLIENTE' 
					
					AND TPO."IdUso" = v_IdUso AND TPO."IdEstado" = 'ACT'
					AND (p_CodigoTipoIndicador = '' 	OR  TPO."CodigoTipoIndicador"= p_CodigoTipoIndicador)
					AND (p_GrupoIndicador = ''		 	OR  TPO."GrupoIndicador"= p_GrupoIndicador)	
					AND IND."IdIndicador"=0
					AND TPO."IdEmpresa" = p_IdEmpresa;
					--AND IND."IdTipoEntidad"='PORTAFOLIO'; --Siempre Portafolio
	
				
	
		---Reemplazo datos en la salida
		p_out_Indicadores = SELECT  * FROM :p_out_Indicadores IND where NOT EXISTS (SELECT 1 FROM :v_Tab_Ind_TipoCliente  TCLI
																					WHERE IND."CodigoTipoIndicador" = TCLI."CodigoTipoIndicador" 
																					  AND IND."GrupoIndicador" = TCLI."GrupoIndicador"  ) 
							UNION ALL SELECT * FROM :v_Tab_Ind_TipoCliente;
		
	
	--END IF;
	/*FIN PORTAFOLIO*/

	
	select count(1) into p_out_IndicadoresContador from :p_out_Indicadores;

END;
