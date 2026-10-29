SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos";

CREATE PROCEDURE
-- 
--  Proposito: Agrupador de las validaciones basicas para Empresa, Modulo y Usuario
--  Autor: NRM - BCS
--  Fecha Creacion: 28/07/2015  
--     
"BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos" (
	-- Parametros entrada
	IN  p_IdEmpresa				BIGINT,
    IN  p_IdUsuario				BIGINT, 
    IN  p_IdModulo				NVARCHAR(3),
	-- Salidas para mensajes de errores	
	OUT p_out_ErroresDetalle	TABLE("Codigo" NVARCHAR(100),"Descripcion" nvarchar(520),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520)) DEFAULT EMPTY,
	OUT p_out_resultadoRegla	NVARCHAR(40)   --OK=Cumple todos los requisitos,			 								  	   
                                               --RECH=Alguna de las reglas detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN
	p_out_resultadoRegla  := 'OK';
	
	v_Validaciones_Empresa =
	SELECT CAST('IdEmpresa' as NVARCHAR(100)) as "CodigoDato",CAST(:p_IdEmpresa as NVARCHAR(520)) as "ValorDato" 
		  ,CASE WHEN COALESCE(:p_IdEmpresa,0) <= 0 THEN 'RECH' ELSE 'OK' END "EMPRESA_VACIA"
		  ,CASE WHEN :p_IdEmpresa > 0 AND COALESCE(EM."IdEmpresa",0) <= 0 THEN 'RECH' ELSE 'OK' END "EMPRESA_NO_EXISTE"
		  ,CASE WHEN :p_IdEmpresa > 0 AND COALESCE(EM."IdSocioDeNegocio",0) = 0 THEN 'RECH' ELSE 'OK' END "SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA"
		  ,CASE WHEN :p_IdEmpresa > 0 AND COALESCE(CSN."IdSocioDeNegocio",0) = 0  THEN 'RECH' ELSE 'OK' END "SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE"
		  --,CASE WHEN :p_IdEmpresa > 0 AND COALESCE(CSN."IdContraparte",0) = 0 THEN 'RECH' ELSE 'OK' END "EMPRESA_NO_CONFIGURADA_COMO_CONTRAPARTE"
		  ,'OK' AS  "EMPRESA_NO_CONFIGURADA_COMO_CONTRAPARTE"
		  ,CASE WHEN :p_IdEmpresa > 0 AND COALESCE(CSN."IdCliente",0) = 0 THEN 'RECH' ELSE 'OK' END "EMPRESA_NO_CONFIGURADA_COMO_CLIENTE"
	  FROM DUMMY as DM
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as EM ON EM."IdEmpresa" = :p_IdEmpresa
	  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = EM."IdSocioDeNegocio";

	
	v_Validaciones_Usuario =
	SELECT CAST('IdUsuario' as NVARCHAR(100)) as "CodigoDato",CAST(:p_IdUsuario as NVARCHAR(520)) as "ValorDato" 
	      ,CASE WHEN COALESCE(:p_IdUsuario,-1) = -1 THEN 'RECH' ELSE 'OK' END "USUARIO_VACIO"
		  ,CASE WHEN COALESCE(U."IdUsuario",-1) = -1 THEN 'RECH' ELSE 'OK' END "USUARIO_NO_EXISTE"
		  ,CASE WHEN COALESCE(U."IdUsuario",-1) != -1 AND COALESCE(U."IdEstado",'') <> 'ACT' THEN 'RECH' ELSE 'OK' END "USUARIO_NO_VIGENTE"
	  FROM DUMMY as DM
	  LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as U ON U."IdUsuario" = :p_IdUsuario;
	
	
	v_Validaciones_Modulo =
	SELECT CAST('IdModulo' as NVARCHAR(100)) as "CodigoDato",CAST(:p_IdModulo as NVARCHAR(520)) as "ValorDato" 
	      ,CASE WHEN COALESCE(:p_IdModulo,'') = '' THEN 'RECH' ELSE 'OK' END "MODULO_VACIO"
		  ,CASE WHEN :p_IdModulo <> '' AND COALESCE(MD."IdModulo",'') = '' THEN 'RECH' ELSE 'OK' END "MODULO_NO_EXISTE"
	  FROM DUMMY as DM
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" as MD ON MD."IdModulo" = :p_IdModulo;

	
	
	p_out_ErroresDetalle =
	SELECT "Codigo",CAST('' as NVARCHAR(520)) as "Descripcion","CodigoDato" as "CodigoDato1","ValorDato" as "ValorDato1",CAST('' as NVARCHAR(100)) as "CodigoDato2",CAST('' as NVARCHAR(520)) as "ValorDato2"
	   FROM (
			 SELECT "CodigoDato","ValorDato",
			 		COALESCE("1"||'EMPRESA_VACIA',
			 				 "2"||'EMPRESA_NO_EXISTE',
			 				 "3"||'SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA',
			 				 "4"||'SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE',
			 				 "5"||'EMPRESA_NO_CONFIGURADA_COMO_CONTRAPARTE',
			 				 "6"||'EMPRESA_NO_CONFIGURADA_COMO_CLIENTE'
			 		) AS "Codigo",
			 		COALESCE("1"||"EMPRESA_VACIA",
			 				 "2"||"EMPRESA_NO_EXISTE",
			 				 "3"||"SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA",
			 				 "4"||"SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE",
			 				 "5"||"EMPRESA_NO_CONFIGURADA_COMO_CONTRAPARTE",
			 				 "6"||"EMPRESA_NO_CONFIGURADA_COMO_CLIENTE"
			 		) AS "Resultado"
			   FROM :v_Validaciones_Empresa,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	    AND "Resultado" = 'RECH'
	  UNION ALL
	SELECT "Codigo",CAST('' as NVARCHAR(520)) as "Descripcion","CodigoDato" as "CodigoDato1","ValorDato" as "ValorDato1",CAST('' as NVARCHAR(100)) as "CodigoDato2",CAST('' as NVARCHAR(520)) as "ValorDato2"
	   FROM (
			 SELECT "CodigoDato","ValorDato",
			 		COALESCE("1"||'USUARIO_VACIO',"2"||'USUARIO_NO_EXISTE',"3"||'USUARIO_NO_VIGENTE') AS "Codigo",
			 		COALESCE("1"||"USUARIO_VACIO","2"||"USUARIO_NO_EXISTE","3"||"USUARIO_NO_VIGENTE") AS "Resultado"
			   FROM :v_Validaciones_Usuario,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	    AND "Resultado" = 'RECH'
	  UNION ALL
	 SELECT "Codigo",CAST('' as NVARCHAR(520)) as "Descripcion","CodigoDato" as "CodigoDato1","ValorDato" as "ValorDato1",CAST('' as NVARCHAR(100)) as "CodigoDato2",CAST('' as NVARCHAR(520)) as "ValorDato2"
	   FROM (
			 SELECT "CodigoDato","ValorDato",
			 		COALESCE("1"||'MODULO_VACIO',"2"||'MODULO_NO_EXISTE') AS "Codigo",
			 		COALESCE("1"||"MODULO_VACIO","2"||"MODULO_NO_EXISTE") AS "Resultado"
			   FROM :v_Validaciones_Modulo,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	    AND "Resultado" = 'RECH';
	 
	
	IF (NOT IS_EMPTY(:p_out_ErroresDetalle)) THEN
		p_out_resultadoRegla  := 'RECH';
	END IF;
END;
