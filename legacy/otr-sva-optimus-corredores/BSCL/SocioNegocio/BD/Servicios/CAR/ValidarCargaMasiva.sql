SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios.CAR::ValidarCargaMasiva";
GO                                         
CREATE PROCEDURE
--
--  Autor: GSALINI
--  Fecha Creacion: 04/04/2023
--
"BSCL.SocioNegocio.BD.Servicios.CAR::ValidarCargaMasiva"(	

	IN  p_CargaMasivaValidar TABLE (
		"IdCargaMasiva"       BIGINT,
		"CodigoArchivo"		  NVARCHAR(40)
	),

	OUT p_out_CargaMasivaInvalida TABLE ("IdCargaMasiva"       BIGINT),
	OUT p_out_ErroresValidacion TABLE (
		"IdCargaMasiva"			 BIGINT,
		"IdCargaMasivaDetalle"   BIGINT,
		"ObservacionValidacion"	 NVARCHAR(520)
	)

) 	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT;
	DECLARE v_MensajesValidacion	TABLE (
		"Codigo"		NVARCHAR(100),
		"Descripcion"	NVARCHAR(520)
	);

	p_out_CargaMasivaInvalida   = SELECT 0 AS "IdCargaMasiva" FROM dummy WHERE 1=2;
	p_out_ErroresValidacion		= SELECT 0 AS "IdCargaMasiva", 0 AS "IdCargaMasivaDetalle", '' AS "ObservacionValidacion" FROM dummy WHERE 1=2;
	v_MensajesValidacion		= SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1=2;


	BEGIN	
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
		DECLARE v_TablaValidaciones    	TABLE ("Cumple" BIGINT, "CodigoError" NVARCHAR(40), "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdCargaMasivaDetalle" BIGINT, "IdCargaMasiva" BIGINT);
		DECLARE v_Codigos				TABLE ("IdCargaMasivaDetalle" BIGINT, "Patron" NVARCHAR(100));
	
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaValidaciones    	= SELECT  0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasivaDetalle", 0 AS "IdCargaMasiva"  FROM dummy WHERE (1=2);		


		--------------------------------------------   
		-- VALIDACIONES
		--------------------------------------------
		v_CargaMasivaDetalle = 
		SELECT
			CMD."IdCargaMasiva",
			CMD."IdCargaMasivaDetalle",
			CMV."CodigoArchivo",
			CMD."DocumentoIdentidad",
			COALESCE(POR."IdPortafolio",-1) AS "IdPortafolio",
			CMD."CodigoPortafolio",
			CMD."Entidad",
			CMD."CodigoIndicador",
			CMD."ValorIndicador"
		FROM :p_CargaMasivaValidar AS CMV
		INNER JOIN "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMV."IdCargaMasiva")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON CMD."DocumentoIdentidad"=PER."DocumentoIdentidad"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON PER."IdPersona"=CLI."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarDatosPortafolio" AS POR ON CLI."IdCliente"=POR."IdCliente";
		
		-- Valida que exista Socio de Negocio
		v_TablaValidaciones = 
		SELECT  
		CASE WHEN COALESCE(PER."DocumentoIdentidad",'') = '' THEN 0					 
			 ELSE 1 END AS "Cumple",
		CASE WHEN COALESCE(PER."DocumentoIdentidad",'') = '' THEN 'SOCIO_NEGOCIO_NO_EXISTE'
			 ELSE '' END AS "CodigoError",
		'DocumentoIdentidad' AS "CodigoDato",
       	CMD."DocumentoIdentidad" AS "ValorDato",
       	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		-- Valida la coherencia de la Clase de dato, Codigo Indicador y Valor Indicador
		v_TablaValidaciones = 
		SELECT  
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 0
			WHEN TIN."RequiereAutorizacion"='S' THEN 0
			WHEN TIN."ClaseDato"='NVARCHAR' THEN CASE
				WHEN TIN."LongitudMaxima"!=0 AND LENGTH(CMD."ValorIndicador")>TIN."LongitudMaxima" THEN 0
				WHEN TIN."LongitudMinima"!=0 THEN CASE
					WHEN LENGTH(CMD."ValorIndicador")<TIN."LongitudMinima" THEN 0
					ELSE 1 END
				ELSE 1 END
			WHEN TIN."ClaseDato"='CHECKBOX' THEN CASE
				WHEN (CMD."ValorIndicador"='S' OR CMD."ValorIndicador"='N') THEN 1
				ELSE 0 END
			WHEN TIN."ClaseDato"='DATE' THEN CASE
				WHEN 'S' IN (SELECT "BSCL.Plataforma.BD.Utilitarios::EsFecha"(SUBSTRING(CMD."ValorIndicador",1,10)) FROM DUMMY) THEN 1
				ELSE 0 END
			ELSE 0 END AS "Cumple",
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 'UI_ERROR_VALORINDICADOR'
			WHEN TIN."RequiereAutorizacion"='S' THEN 'UI_ERROR_CODIGOINDICADOR_AUT'
			WHEN TIN."ClaseDato"='CHECKBOX' THEN CASE
				WHEN (CMD."ValorIndicador"='S' OR CMD."ValorIndicador"='N') THEN ''
				ELSE 'UI_ERROR_VALORINDICADOR_CHECKBOX' END
			WHEN TIN."ClaseDato"='DATE' THEN CASE
				WHEN 'S' IN (SELECT "BSCL.Plataforma.BD.Utilitarios::EsFecha"(SUBSTRING(CMD."ValorIndicador",1,10)) FROM DUMMY) THEN ''
				ELSE 'UI_ERROR_VALORINDICADOR_FECHA_FORMATO' END
			ELSE 'UI_ERROR_VALORINDICADOR' END AS "CodigoError",
		'ValorIndicador' AS "CodigoDato",
	   	CAST(CMD."ValorIndicador" AS NVARCHAR(50))AS "ValorDato",
	   	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador")
		WHERE TIN."IdTipoEntidad"=CMD."Entidad" AND TIN."ClaseDato"!='COMBO' AND TIN."ClaseDato"!='MULTI' AND TIN."ClaseDato"!='NVARCHAR'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		-- Valida la coherencia de la Clase de dato NVARCHAR y Valor Indicador
		v_TablaValidaciones = 
		SELECT  
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 0
			WHEN TIN."RequiereAutorizacion"='S' THEN 0
			WHEN TIN."ClaseDato"='NVARCHAR' THEN CASE
				WHEN TIN."LongitudMinima"!=0 AND LENGTH(CMD."ValorIndicador")<TIN."LongitudMinima" THEN 0
				ELSE 1 END
			ELSE 0 END AS "Cumple",
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 'UI_ERROR_VALORINDICADOR'
			WHEN TIN."RequiereAutorizacion"='S' THEN 'UI_ERROR_CODIGOINDICADOR_AUT'
			WHEN TIN."ClaseDato"='NVARCHAR' THEN CASE
				WHEN TIN."LongitudMinima"!=0 AND LENGTH(CMD."ValorIndicador")<TIN."LongitudMinima" THEN 'UI_ERROR_VALORINDICADOR_NVARCHAR_MAX'
				ELSE '' END
			ELSE 'UI_ERROR_VALORINDICADOR' END AS "CodigoError",
		'ValorIndicador' AS "CodigoDato",
	   	CAST(CMD."ValorIndicador" AS NVARCHAR(50))AS "ValorDato",
	   	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador")
		WHERE TIN."IdTipoEntidad"=CMD."Entidad" AND TIN."ClaseDato"='NVARCHAR'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;		
		
		v_TablaValidaciones = 
		SELECT  
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 0
			WHEN TIN."RequiereAutorizacion"='S' THEN 0
			WHEN TIN."ClaseDato"='NVARCHAR' THEN CASE
				WHEN TIN."LongitudMaxima"!=0 AND LENGTH(CMD."ValorIndicador")>TIN."LongitudMaxima" THEN 0
				ELSE 1 END
			ELSE 0 END AS "Cumple",
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 'UI_ERROR_VALORINDICADOR'
			WHEN TIN."RequiereAutorizacion"='S' THEN 'UI_ERROR_CODIGOINDICADOR_AUT'
			WHEN TIN."ClaseDato"='NVARCHAR' THEN CASE
				WHEN TIN."LongitudMaxima"!=0 AND LENGTH(CMD."ValorIndicador")>TIN."LongitudMaxima" THEN 'UI_ERROR_VALORINDICADOR_NVARCHAR_MAX'
				ELSE '' END
			ELSE 'UI_ERROR_VALORINDICADOR' END AS "CodigoError",
		'ValorIndicador' AS "CodigoDato",
	   	CAST(CMD."ValorIndicador" AS NVARCHAR(50))AS "ValorDato",
	   	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador")
		WHERE TIN."IdTipoEntidad"=CMD."Entidad" AND TIN."ClaseDato"='NVARCHAR'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;		
		
		-- Valida la coherencia de la Clase de dato COMBO y Valor Indicador
		v_TablaValidaciones = 
		SELECT  
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 0
			WHEN TIN."RequiereAutorizacion"='S' THEN 0
			WHEN COALESCE(DIN."Codigo",'') = '' THEN 0
			ELSE 1 END AS "Cumple",
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 'UI_ERROR_VALORINDICADOR'
			WHEN TIN."RequiereAutorizacion"='S' THEN 'UI_ERROR_CODIGOINDICADOR_AUT'
			WHEN COALESCE(DIN."Codigo",'') = '' THEN 'UI_ERROR_VALORINDICADOR_COMBO'
			ELSE '' END AS "CodigoError",
		'ValorIndicador' AS "CodigoDato",
	   	CAST(CMD."ValorIndicador" AS NVARCHAR(50))AS "ValorDato",
	   	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador")
		LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DIN ON (TIN."IdTipoIndicador"=DIN."IdTipoIndicador" AND DIN."Codigo"=CMD."ValorIndicador")
		WHERE TIN."IdTipoEntidad"=CMD."Entidad" AND TIN."ClaseDato"='COMBO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		-- Valida la coherencia de la Clase de dato MULTI y Valor Indicador
		v_TablaValidaciones = 
		SELECT
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 0
			WHEN "BSCL.SocioNegocio.BD.Utilitarios::CAR.ValidarFormatoMulti"(CMD."ValorIndicador",COD."Patron")=0 THEN 0
			ELSE 1 END AS "Cumple",
		CASE
			WHEN COALESCE(CMD."ValorIndicador",'') = '' THEN 'UI_ERROR_VALORINDICADOR'
			WHEN "BSCL.SocioNegocio.BD.Utilitarios::CAR.ValidarFormatoMulti"(CMD."ValorIndicador",COD."Patron")=0 THEN 'UI_ERROR_VALORINDICADOR_MULTI'
			ELSE '' END AS "CodigoError",
		'ValorIndicador' AS "CodigoDato",
	   	CAST(CMD."ValorIndicador" AS NVARCHAR(50))AS "ValorDato",
	   	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador")
		LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DIN ON (TIN."IdTipoIndicador"=DIN."IdTipoIndicador" AND DIN."Codigo"=CMD."ValorIndicador")
		INNER JOIN (
			SELECT TIN."CodigoTipoIndicador", STRING_AGG(DIN."Codigo",'|') AS "Patron"
			FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIN
			LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DIN ON TIN."IdTipoIndicador"=DIN."IdTipoIndicador"
			WHERE TIN."ClaseDato"='MULTI'
			GROUP BY TIN."CodigoTipoIndicador"
		) AS COD ON COD."CodigoTipoIndicador"=CMD."CodigoIndicador"
		WHERE TIN."IdTipoEntidad"=CMD."Entidad" AND TIN."ClaseDato"='MULTI'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		-- Valida codigo indicador existe
		v_TablaValidaciones = 
		SELECT  
		CASE WHEN (COALESCE(TIN."CodigoTipoIndicador",'') = '' OR COALESCE(CMD."CodigoIndicador",'') = '') THEN 0
			 ELSE 1 END AS "Cumple",
		CASE WHEN (COALESCE(TIN."CodigoTipoIndicador",'') = '' OR COALESCE(CMD."CodigoIndicador",'') = '') THEN 'UI_ERROR_CODIGOINDICADOR'
			 ELSE '' END AS "CodigoError",
		'CodigoIndicador' AS "CodigoDato",
		CAST(CMD."CodigoIndicador" AS NVARCHAR(50))AS "ValorDato",
		CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador" AND TIN."IdTipoEntidad"=CMD."Entidad")
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		-- Valida que el portafolio exista y pertenezca a la persona
		v_TablaValidaciones = 
		SELECT  
		CASE WHEN (COALESCE(POR."CodigoPortafolio",'') = '' OR POR."IdPortafolio"=0) THEN 0					 
			 ELSE 1 END AS "Cumple",
		CASE WHEN (COALESCE(POR."CodigoPortafolio",'') = '' OR POR."IdPortafolio"=0)  THEN 'UI_VALIDACION_PORTAFOLIO_NO_EXISTE'
			 ELSE '' END AS "CodigoError",
		'CodigoPortafolio' AS "CodigoDato",
       	CAST(CMD."CodigoPortafolio" AS NVARCHAR(50))AS "ValorDato",
       	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" POR ON (POR."IdPersona" = PER."IdPersona" AND RTRIM(LTRIM(UCASE(POR."CodigoPortafolio"))) = RTRIM(LTRIM(UCASE(CMD."CodigoPortafolio"))))
		WHERE CMD."Entidad"='PORTAFOLIO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		-- Valida el estado autorizacion del portafolio
		v_TablaValidaciones = 
		SELECT  
		CASE WHEN (COALESCE(POR."IdEstadoPortafolioAutorizacion",'') = 'PDA') THEN 0					 
			 ELSE 1 END AS "Cumple",
		CASE WHEN(COALESCE(POR."IdEstadoPortafolioAutorizacion",'') = 'PDA')  THEN 'UI_PORTAFOLIO_PENDIENTE_DE_AUTORIZACION'
			 ELSE '' END AS "CodigoError",
		'CodigoPortafolio' AS "CodigoDato",
       	CAST(CMD."CodigoPortafolio" AS NVARCHAR(50))AS "ValorDato",
       	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" CCP ON (CCP."IdPersona" = PER."IdPersona" AND RTRIM(LTRIM(UCASE(CCP."CodigoPortafolio"))) = RTRIM(LTRIM(UCASE(CMD."CodigoPortafolio"))))
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON (CCP."IdPortafolio" = POR."IdPortafolio")
		WHERE CMD."Entidad"='PORTAFOLIO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		-- Valida la entidad
		v_TablaValidaciones =
		SELECT
		CASE WHEN COALESCE(CEN."TipoEntidad",'')='' THEN 0
			 ELSE 1 END AS "Cumple",
		CASE WHEN COALESCE(CEN."TipoEntidad",'') = '' THEN 'UI_ENTIDAD_NO_VALIDA'
			 ELSE '' END AS "CodigoError",
		'Entidad' AS "CodigoDato",
	   	CAST(CMD."Entidad" as NVARCHAR(40)) AS "ValorDato",
	   	CMD."IdCargaMasivaDetalle",
		CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEntidad" AS CEN ON (CEN."DocumentoIdentidad"=CMD."DocumentoIdentidad" AND (CEN."TipoEntidad"=SUBSTRING(CMD."Entidad",1,3) OR (CMD."Entidad"='PORTAFOLIO' AND CEN."TipoEntidad"='CLI')))
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;



		-- Cargas Masivas con registros invalidos
	    p_out_CargaMasivaInvalida =
	    SELECT "IdCargaMasiva"
	    FROM :v_TablaValidaciones
	    WHERE "Cumple" = 0
		GROUP BY "IdCargaMasiva";
	    
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaValidaciones
		WHERE "Cumple" = 0;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_MensajesValidacion);


		p_out_ErroresValidacion = 
		SELECT 
			TV."IdCargaMasiva",
			TV."IdCargaMasivaDetalle",
			(CASE
				WHEN COALESCE(TV."ValorDato",'') = '' THEN MV."Descripcion"
				ELSE MV."Descripcion" || ' (' || TV."ValorDato" || ')'
			END) AS "ObservacionValidacion"
		FROM  :v_TablaValidaciones AS TV
		INNER JOIN :v_MensajesValidacion AS MV  ON (TV."CodigoError" = MV."Codigo")
		WHERE TV."Cumple" = 0;	
		
    END;
	
END;
