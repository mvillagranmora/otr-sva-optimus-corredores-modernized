SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoResultadoBuscar";

CREATE PROCEDURE
--
--  Proposito		: Retorna Indices calculados
--  Empresa			: Ingeniería ProNet IT SpA.
--  Autor			: HMR
--  Fecha Creacion	: 20/02/2017
--
"BSCL.Normativa.BD.Servicios::ILS.ConceptoResultadoBuscar" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
	-- Parametros entrada
	IN p_IdTipoInforme		BIGINT,
	IN p_FechaContable		NVARCHAR(10),
	OUT p_tablaResultadoConcepto TABLE( "IdIndice"        	    	BIGINT,
									 	"NombreIndice" 	    	 	NVARCHAR(200),
									 	"IdIndiceNombre" 	    	NVARCHAR(223),
									 	"IdResultadoIndice"    	 	BIGINT,
									 	"IdConcepto"        	   	BIGINT,
									 	"NombreConcepto" 	    	NVARCHAR(200),
									 	"IdConceptoNombre"     	 	NVARCHAR(223),
									 	"IdResultadoConcepto"    	BIGINT,
									 	"Monto"        	         	DECIMAL(20,4),
									 	"Posicion"        	    	NVARCHAR(50),
									 	"Operador"        	    	NVARCHAR(50),
									 	"Factor"        	    	NVARCHAR(50),
									 	"MontoCalculado"         	DECIMAL(20,4),
									 	"Estado"        	        NVARCHAR(3)
									 	),
	-- Parametros de Salida
	OUT p_out_InformacionContador 		INT,
	OUT p_out_InformacionDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(1), "ValorParametro" NVARCHAR(100), "NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "ValorParametro", '' AS  "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "ValorParametro" NVARCHAR(100), "NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;

	DECLARE v_count_operaciones			BIGINT;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) 
		  INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) 
		  INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);

		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj ON 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj ON 
 								    													adv."Codigo" = msj."Codigo";
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));

		--------------------
		--Validaciones
		--------------------

		--------------------
		--Reglas Negocio
		--------------------

		--------------------
		--Lógica de Negocio 
		--------------------
		BEGIN
			p_tablaResultadoConcepto  =  SELECT InformeResultadoIndice."IdIndice"															AS "IdIndice",
												InformeResultadoIndice."NombreIndice"														AS "NombreIndice",
												InformeResultadoIndice."IdIndice" || ' - ' || InformeResultadoIndice."NombreIndice" 		AS "IdIndiceNombre",
												InformeResultadoConcepto."IdResultadoIndice"	 											AS "IdResultadoIndice",
												InformeResultadoConcepto."IdConcepto"			 											AS "IdConcepto",
												InformeResultadoConcepto."NombreConcepto"		 											AS "NombreConcepto",
												InformeResultadoConcepto."IdConcepto" || ' - ' || InformeResultadoConcepto."NombreConcepto" AS "IdConceptoNombre",
												InformeResultadoConcepto."IdResultadoConcepto"  											AS "IdResultadoConcepto",
												InformeResultadoConcepto."Monto"				 											AS "Monto",
												CASE InformeResultadoConcepto."Posicion"
													WHEN 'N' THEN
														'Numerador'
													WHEN 'D' THEN
														'Denominador'
													ELSE
														'N/A'
												END 																						AS "Posicion",
												InformeResultadoConcepto."Operador"															AS "Operador",
												InformeResultadoConcepto."Factor"															AS "Factor",
												(InformeResultadoConcepto."MontoCalculado" * InformeResultadoConcepto."Factor")/1000		AS "MontoCalculado",
												InformeResultadoConcepto."Estado"															AS "Estado"
										   FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS InformeResultadoIndice
															INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS InformeResultadoConcepto ON
																	InformeResultadoIndice."IdResultadoIndice" = InformeResultadoConcepto."IdResultadoIndice"
																AND InformeResultadoindice."IdTipoInforme" = :p_IdTipoInforme
																AND InformeResultadoindice."FechaContable" = :p_FechaContable
										  WHERE InformeResultadoConcepto."Estado" = 'VIG'
									   ORDER BY InformeResultadoIndice."IdIndice",
												InformeResultadoConcepto."IdConcepto" ASC;
		END;
	END;
END;