SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemDetalleResultadoBuscar";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemDetalleResultadoBuscar" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdTipoInforme		BIGINT,
	IN p_FechaContable		NVARCHAR(10),
	OUT p_tablaResultadoItemDetalle TABLE(
									 "IdIndice"         	   	 BIGINT,
									 "NombreIndice" 	    	 NVARCHAR(50),
									 "IdNombreIndice"            NVARCHAR(60),  
									 "IdConcepto"        	   	 BIGINT,
									 "NombreConcepto"        	 NVARCHAR(200),
									 "IdNombreConcepto"        	 NVARCHAR(200), 
									 "IdConceptoGrupo"           BIGINT,
									 "NombreConceptoGrupo"       NVARCHAR(1000),
									 "IdNombreConceptoGrupo"     NVARCHAR(1000),
									 "IdItem"                    BIGINT,
									 "NombreItem"                NVARCHAR(200), 
									 "IdNombreItem"              NVARCHAR(222),  
									 "Posicion"        	    	 NVARCHAR(50), 
									 "IdTipoItem"        	     NVARCHAR(3),
									 "Monto"        	         DECIMAL(20,4),
									 "CuentaContable"          	 NVARCHAR(50),
									 "SaldoDebeHaber"          	 NVARCHAR(50),
									 "Condicion"             	 NVARCHAR(50),
									 "MontoCondicion" 	         DECIMAL(20,4),
									 "Operacion"      	    	 NVARCHAR(50),
									 "Factor"        	    	 NVARCHAR(50),
									 "MontoCalculado"         	 DECIMAL(20,4),
									 "MayorACero"      	    	 NVARCHAR(50),
									 "MenorACero"     	    	 NVARCHAR(50),
									 "PermiteCero"     	    	 NVARCHAR(50),
									 "ProcesoFuncion"  	    	 NVARCHAR(50),
									 "NombreTipoItem"         NVARCHAR(50),
									 "MontoContable"           DECIMAL(20,4),
									 "MontoDigitado"           DECIMAL(20,4),
									 "MontoProgramado"           DECIMAL(20,4),
									 "OperacionGC"				NVARCHAR(50)
									 ),    
									 
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
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
			p_tablaResultadoItemDetalle =  SELECT InformeResultadoIndice."IdIndice"             AS "IdIndice"
			                                      ,InformeResultadoIndice."NombreIndice"		AS "NombreIndice"
			                                      ,InformeResultadoIndice."IdIndice" || '-' || InformeResultadoIndice."NombreIndice" AS "IdNombreIndice"
			                                      ,InformeResultadoConcepto."IdConcepto"  		AS "IdConcepto"
			                                      ,InformeResultadoConcepto."NombreConcepto"	AS "NombreConcepto"
			                                      ,InformeResultadoConcepto."IdConcepto" || ' - ' || InformeResultadoConcepto."NombreConcepto" AS "IdNombreConcepto"
			                                      ,ConcGrupConf."IdConceptoGrupo"               AS "IdConceptoGrupo"
			                                      ,ConcGrupConf."Nombre"            			AS "NombreConceptoGrupo"
			                                      ,ConcGrupConf."IdConceptoGrupo" || '-' || ConcGrupConf."Nombre" AS "IdNombreConceptoGrupo"
			                                      ,InformeResultadoItem."IdItem"    			AS "IdItem"
			                                      ,InformeResultadoItem."NombreItem"		    AS "NombreItem"
			                                      ,InformeResultadoItem."IdItem" || ' - ' ||  InformeResultadoItem."NombreItem" AS "IdNombreItem"			                                      
			                                      ,CASE InformeResultadoItemDetalle."Posicion"
			                                         WHEN 'N' THEN 'Numerador'
			                                         WHEN 'D' THEN 'Denominador'
			                                       ELSE ''
			                                       END                                          AS "Posicion"			                                        
			                                      ,InformeResultadoItemDetalle."IdTipoItem" 	AS "IdTipoItem"	
			                                      ,InformeResultadoItemDetalle."Monto"			AS "Monto"			                                      
			                                      ,InformeResultadoItemDetalle."CuentaContable" AS "CuentaContable"
			                                      ,InformeResultadoItemDetalle."SaldoDebeHaber" AS "SaldoDebeHaber"
			                                      ,InformeResultadoItemDetalle."Condicion"      AS "Condicion"
			                                      ,InformeResultadoItemDetalle."MontoCondicion" AS "MontoCondicion"
			                                      ,InformeResultadoItemDetalle."Operacion"      AS "Operacion"
			                                      ,InformeResultadoItemDetalle."Factor"         AS "Factor"
			                                      ,InformeResultadoItemDetalle."MontoCalculado"/1000    AS "MontoCalculado"
			                                      ,InformeResultadoItemDetalle."MayorACero"     AS "MayorACero"
			                                      ,InformeResultadoItemDetalle."MenorACero"     AS "MenorACero"
			                                      ,InformeResultadoItemDetalle."PermiteCero"	AS "PermiteCero"
			                                      ,InformeResultadoItemDetalle."ProcesoFuncion"	AS "ProcesoFuncion"
			                                      ,ItemTipo."Nombre"							AS "NombreTipoItem"	  
												  ,InformeResultadoItemDetalle."MontoContable"/1000    AS "MontoContable"
												  ,InformeResultadoItemDetalle."MontoDigitado"/1000    AS "MontoDigitado"
												  ,InformeResultadoItemDetalle."MontoProgramado"/1000    AS "MontoProgramado"
												  ,ConcGrup."Operacion" AS "OperacionGC"
			                               FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS InformeResultadoIndice
			                                 INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoConcepto" AS InformeResultadoConcepto
			                                    ON InformeResultadoIndice."IdResultadoIndice" = InformeResultadoConcepto."IdResultadoIndice"
			                                   AND InformeResultadoConcepto."IdResultadoIndice" = InformeResultadoIndice."IdResultadoIndice" 
			                                   AND InformeResultadoIndice."IdTipoInforme" = :p_IdTipoInforme
			                                   AND InformeResultadoIndice."FechaContable" = :p_FechaContable
			                                 INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItem" as InformeResultadoItem
			                                    ON InformeResultadoItem."IdResultadoConcepto" = InformeResultadoConcepto."IdResultadoConcepto"
			                                   AND InformeResultadoItem."IdTipoInforme" = InformeResultadoIndice."IdTipoInforme"
			                                   AND InformeResultadoItem."IdConcepto" = InformeResultadoConcepto."IdConcepto"
			                                   AND InformeResultadoItem."IdTipoInforme" = InformeResultadoConcepto."IdTipoInforme"
			                                 INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConcGrupConf
			                                    ON ConcGrupConf."IdConcepto" = InformeResultadoConcepto."IdConcepto"	
			                                   AND ConcGrupConf."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo" 
			                                 INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItemDetalle" as InformeResultadoItemDetalle
			                                    ON InformeResultadoItemDetalle."IdResultadoItem" = InformeResultadoItem."IdResultadoItem"
			                                   AND InformeResultadoItemDetalle."IdIndice" = InformeResultadoIndice."IdIndice"
   			                                   AND InformeResultadoItemDetalle."IdConcepto" = InformeResultadoItem."IdConcepto"
			                                   AND InformeResultadoItemDetalle."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo"
			                                 INNER JOIN "BSCL.Normativa.BD::ILS.ItemTipo" as ItemTipo
			                                    ON InformeResultadoItem."IdTipoItem" = ItemTipo."IdTipoItem"
			                                 LEFT JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConcGrup
			                                 	ON ConcGrup."IdConcepto" = InformeResultadoConcepto."IdConcepto"
			                                   AND ConcGrup."IdConceptoGrupo" = InformeResultadoItem."IdConceptoGrupo"  
			                                   AND InformeResultadoItemDetalle."IdItem" = ConcGrup."IdItem"
			                               WHERE InformeResultadoConcepto."Estado" = 'VIG'
			                               ORDER BY InformeResultadoIndice."IdIndice", InformeResultadoConcepto."IdConcepto", ConcGrupConf."IdConceptoGrupo", InformeResultadoItem."IdItem"	ASC;
		END;		
	END;	
END;
