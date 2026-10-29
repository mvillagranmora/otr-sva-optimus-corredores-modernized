SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarFiltrosValor";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 08/09/2016
--  Id Servicio = 12006
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarFiltrosValor" (
	IN p_IdTipoDatoEvento				NVARCHAR(30)		default '',
	IN p_Estado							NVARCHAR(1)			default '',
	-- Parametros salida del servicio publico
	OUT p_out_TabFiltrosValor TABLE ("Valor" NVARCHAR(60), "Descripcion" NVARCHAR(100)),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),  
	OUT p_out_ErroresContador           int,   
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	-- FIN Sección Fija
	)
      LANGUAGE SQLSCRIPT
      SQL SECURITY INVOKER
      DEFAULT SCHEMA "BSCL"
      --READS SQL DATA
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT);	        	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle; 

		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select 0 As "IdComprobante",'' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 As "IdComprobante", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_ResultadoProceso	    = select '' as "Campo",    0 as "Id", '' as "Servicio", '' as "Resultado" From dummy where 1=2;	
	v_RechazadosPorValidacion   = select 0 As "IdComprobante" from dummy where 1=2;

	BEGIN
		p_out_TabFiltrosValor = 
			SELECT "Valor","Descripcion" FROM (
			  SELECT CASE WHEN CTB_itzEventoTipoDato."TipoDato" = 'V' THEN 
							CTB_itzEventoTipoDatoValor."ValorVarchar" 
						WHEN CTB_itzEventoTipoDato."TipoDato" = 'D' THEN 
							CAST(CTB_itzEventoTipoDatoValor."ValorDecimal" AS varchar(60)) 
					END 																		AS "Valor",
					CTB_itzEventoTipoDatoValor."Descripcion"
			  FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" AS CTB_itzEventoTipoDatoValor 
			  		LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS CTB_itzEventoTipoDato 
			  									 ON CTB_itzEventoTipoDato."IdTipoDatoEvento" = CTB_itzEventoTipoDatoValor."IdTipoDatoEvento" 
			  									AND (CTB_itzEventoTipoDato."Estado" 		= p_Estado OR p_Estado = '')
			 WHERE CTB_itzEventoTipoDatoValor."IdTipoDatoEvento" = p_IdTipoDatoEvento
			   AND (CTB_itzEventoTipoDatoValor."Estado" 		 = p_Estado OR p_Estado = '')
			   AND CTB_itzEventoTipoDatoValor."IdTipoDatoEvento" NOT IN ('Concepto','Moneda','MonedaLiquidacion','MonedaComprobante')
			 UNION ALL
			 SELECT Concepto."IdConcepto" AS "Valor",
					Concepto."IdConcepto" || ' - ' || Concepto."Nombre" AS "Descripcion"
			 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS Concepto 
			 WHERE p_IdTipoDatoEvento = 'Concepto' AND Concepto."IdEstado" = 'VIG'
			 UNION ALL
			 SELECT Moneda."IdMoneda" AS "Valor",
					Moneda."NombreLargo" AS "Descripcion"
			 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS Moneda 
			 WHERE p_IdTipoDatoEvento IN ('Moneda','MonedaLiquidacion','MonedaComprobante') AND Moneda."IdEstado" = 'ACT'
			)
			ORDER BY "Descripcion" ASC;
	END;
END;