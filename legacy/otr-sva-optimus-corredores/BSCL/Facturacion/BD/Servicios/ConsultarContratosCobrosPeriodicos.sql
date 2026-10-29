SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ConsultarContratosCobrosPeriodicos";
GO
CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::ConsultarContratosCobrosPeriodicos" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                		BIGINT,
    IN p_IdUsuario                		BIGINT,
    IN p_IdModulo                 		NVARCHAR(3), 
  
	IN p_IdCliente 						BIGINT default 0,
	IN p_IdPortafolio 					BIGINT default 0,
	IN p_IdClasificacionTipoInstrumento NVARCHAR(3),
	IN p_IdTipoInstrumento 				NVARCHAR(3),
	IN p_FechaConsultaDesde				NVARCHAR(10) default '0001-01-01',	
	IN p_FechaConsultaHasta				NVARCHAR(10) default '0001-01-01',
	IN p_IdTipoContratoCobroPeriodico	BIGINT default 0,

   -- Parametros de Salida
   	OUT p_out_tbl						TABLE(	"IdContratoCobroPeriodico"			BIGINT,
   												"DocumentoIdentidad"				NVARCHAR(11),
   												"NombreCliente"						NVARCHAR(70),
   												"NombreContrato"					NVARCHAR(70),
   												"FormatoContratoCobroPeriodico"		NVARCHAR(30),
   												"MontoOperado"						DECIMAL(26,8),
   												"MontoComision"						DECIMAL(26,8),
   												"MontoDerecho"						DECIMAL(26,8)
   												)

 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"

AS
BEGIN

	DECLARE v_DocumentoIdentidadCuentaPropia    NVARCHAR(40);
	DECLARE v_IdPersonaCuentaPropia             BIGINT;
	DECLARE v_IdClienteCuentaPropia             BIGINT;				
	DECLARE v_FechaConsulta 					NVARCHAR(10) := '2300-12-31';

/*	IF p_FechaConsultaDesde = '0001-01-01' THEN
		Select top 1 "Valor" into v_FechaConsulta from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema' and "IdEmpresa" = p_IdEmpresa;
	ELSE
		v_FechaConsulta := p_FechaConsulta;
	END IF;


	-- Obtiene los datos de la corredora(para cuenta propia)	
	SELECT "IdPersona", "IdCliente", "DocumentoIdentidad" 
	INTO v_IdPersonaCuentaPropia, v_IdClienteCuentaPropia, v_DocumentoIdentidadCuentaPropia 
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
	WHERE "IdEmpresa" = :p_IdEmpresa
	GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad";


	-- Obtiene los relacionados a la corredora
	v_tblRelacionados = 
	SELECT distinct PER."IdPersonaDestino"
	FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS PER
	WHERE PER."IdPersonaOrigen" = :v_IdPersonaCuentaPropia AND PER."IdEmpresa" = p_IdEmpresa 
	AND PER."IdEstado" = 'ACT';	
*/	
 
	BEGIN 

     	-- FACTURAS
		INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes" (
					  "IdCliente" ,"IdPortafolio" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,
					  "MontoFactura","MontoCobroFactura","MontoNotaCredito","MontoCobroNotaCredito","MontoFacturaOtros",
					  "MontoCobroFacturaOtros","MontoNotaCreditoOtros","MontoCobroNotaCreditoOtros")
			SELECT	 "IdCliente"
					,"IdPortafolio"
					,"IdClasificacionTipoInstrumento" 
					,"IdTipoInstrumento" 
					,Sum("Monto")						AS "MontoFactura" 
					,Sum("MontoCobro")					AS "MontoCobroFactura"
					,0									AS "MontoNotaCredito"
					,0									AS "MontoCobroNotaCredito"
					,0									AS "MontoFacturaOtros"
					,0									AS "MontoCobroFacturaOtros"
					,0									AS "MontoNotaCreditoOtros"
					,0									AS "MontoCobroNotaCreditoOtros"															
			FROM "BSCL.Facturacion.BD::FAC.Comprobante"
			WHERE "MontoCobro" = 0
			  AND ("IdCliente" = :p_IdCliente OR :p_IdCliente = 0)  
			  AND ("IdPortafolio" = :p_IdPortafolio OR :p_IdPortafolio = 0)  
			  AND ("IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')    
			  AND ("IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento = '')
			  AND "FechaComprobante" BETWEEN :p_FechaConsultaDesde AND :p_FechaConsultaHasta
			  AND "IdEstadoComprobante" <> 'ANU'
			  AND "IdTipoComprobante" IN ('FAE', 'FAC')
			GROUP BY "IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento";
			
		-- NOTAS DE CRÉDITO
		INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes" (
					  "IdCliente" ,"IdPortafolio" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,
					  "MontoFactura","MontoCobroFactura","MontoNotaCredito","MontoCobroNotaCredito","MontoFacturaOtros",
					  "MontoCobroFacturaOtros","MontoNotaCreditoOtros","MontoCobroNotaCreditoOtros")
			SELECT	 "IdCliente"
					,"IdPortafolio"
					,"IdClasificacionTipoInstrumento" 
					,"IdTipoInstrumento" 
					,0									AS "MontoFactura" 
					,0									AS "MontoCobroFactura"
					,Sum("Monto")						AS "MontoNotaCredito"
					,Sum("MontoCobro")					AS "MontoCobroNotaCredito"
					,0									AS "MontoFacturaOtros"
					,0									AS "MontoCobroFacturaOtros"
					,0									AS "MontoNotaCreditoOtros"
					,0									AS "MontoCobroNotaCreditoOtros"															
			FROM "BSCL.Facturacion.BD::FAC.Comprobante"
			WHERE "MontoCobro" = 0
			  AND ("IdCliente" = :p_IdCliente OR :p_IdCliente = 0)  
			  AND ("IdPortafolio" = :p_IdPortafolio OR :p_IdPortafolio = 0)  
			  AND ("IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')    
			  AND ("IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento = '')
			  AND "FechaComprobante" BETWEEN :p_FechaConsultaDesde AND :p_FechaConsultaHasta
			  AND "IdEstadoComprobante" <> 'ANU'
			  AND "IdTipoComprobante" IN ('NCR', 'NCE')
			GROUP BY "IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento";
			
		-- FACTURAS OTROS
		INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes" (
					  "IdCliente" ,"IdPortafolio" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,
					  "MontoFactura","MontoCobroFactura","MontoNotaCredito","MontoCobroNotaCredito","MontoFacturaOtros",
					  "MontoCobroFacturaOtros","MontoNotaCreditoOtros","MontoCobroNotaCreditoOtros")
			SELECT	 "IdCliente"
					,"IdPortafolio"
					,"IdClasificacionTipoInstrumento" 
					,"IdTipoInstrumento" 
					,0									AS "MontoFactura" 
					,0									AS "MontoCobroFactura"
					,0									AS "MontoNotaCredito"
					,0									AS "MontoCobroNotaCredito"
					,Sum("Monto")						AS "MontoFacturaOtros"
					,Sum("MontoCobro")					AS "MontoCobroFacturaOtros"					
					,0									AS "MontoNotaCreditoOtros"
					,0									AS "MontoCobroNotaCreditoOtros"										
			FROM "BSCL.Facturacion.BD::FAC.Comprobante"
			WHERE "MontoCobro" = 0
			  AND ("IdCliente" = :p_IdCliente OR :p_IdCliente = 0)  
			  AND ("IdPortafolio" = :p_IdPortafolio OR :p_IdPortafolio = 0)  
			  AND ("IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')    
			  AND ("IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento = '')
			  AND "FechaComprobante" BETWEEN :p_FechaConsultaDesde AND :p_FechaConsultaHasta
			  AND "IdEstadoComprobante" <> 'ANU'
			  AND "IdTipoComprobante" IN ('FOE', 'FOX', 'FOC')
			GROUP BY "IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento";

		-- NOTAS DE CREDITO OTROS
		INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes" (
					  "IdCliente" ,"IdPortafolio" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" ,
					  "MontoFactura","MontoCobroFactura","MontoNotaCredito","MontoCobroNotaCredito","MontoFacturaOtros",
					  "MontoCobroFacturaOtros","MontoNotaCreditoOtros","MontoCobroNotaCreditoOtros")
			SELECT	 "IdCliente"
					,"IdPortafolio"
					,"IdClasificacionTipoInstrumento" 
					,"IdTipoInstrumento" 
					,Sum("Monto")						AS "MontoFactura" 
					,Sum("MontoCobro")					AS "MontoCobroFactura"
					,0									AS "MontoNotaCredito"
					,0									AS "MontoCobroNotaCredito"
					,0									AS "MontoFacturaOtros"
					,0									AS "MontoCobroFacturaOtros"
					,Sum("Monto")						AS "MontoNotaCreditoOtros"
					,Sum("MontoCobro")					AS "MontoCobroNotaCreditoOtros"					
			FROM "BSCL.Facturacion.BD::FAC.Comprobante"
			WHERE "MontoCobro" = 0
			  AND ("IdCliente" = :p_IdCliente OR :p_IdCliente = 0)  
			  AND ("IdPortafolio" = :p_IdPortafolio OR :p_IdPortafolio = 0)  
			  AND ("IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')    
			  AND ("IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento = '')
			  AND "FechaComprobante" BETWEEN :p_FechaConsultaDesde AND :p_FechaConsultaHasta
			  AND "IdEstadoComprobante" <> 'ANU'
			  AND "IdTipoComprobante" IN ('NEO')
			GROUP BY "IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento";

		INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" (
 					"IdContratoCobroPeriodico","IdCliente","IdPortafolio","DocumentoIdentidad","NombreCliente",
 					"IdTipoContratoCobroPeriodico","NombreContrato","FormatoContratoCobroPeriodico","IdClasificacionTipoInstrumento",
 					"IdTipoInstrumento","IdConceptoCobroPeriodico","IdCobro","MontoMinimoACobrar","MontoMaximoACobrar",
 					"TipoCobroPeriodico","PorRango","MontoCobroPeriodico","PorcentajeCobroPeriodico","MontoDesde","MontoHasta")
			SELECT	 ContCobPer."IdContratoCobroPeriodico"
					,ContCobPer."IdCliente"
					,ContCobPer."IdPortafolio"
					,ContCobPer."DocumentoIdentidad"
					,ContCobPer."NombreLargoCliente"					AS "NombreCliente"
					,ContCobPer."IdTipoContratoCobroPeriodico"
					,TipoContrato."Nombre"								AS "NombreContrato"
					,ContCobPer."FormatoContratoCobroPeriodico"
					,DetContCobPer."IdClasificacionTipoInstrumento" 
					,DetContCobPer."IdTipoInstrumento"
					,DetContCobPer."IdConceptoCobroPeriodico"
					,CobroPeriodico."IdCobro"
					,CobroPeriodico."MontoMinimoACobrar"
					,CobroPeriodico."MontoMaximoACobrar"
					,CobroPeriodico."TipoCobroPeriodico"
					,CobroPeriodico."PorRango"
					,CobroPeriodicoDetalle."MontoCobroPeriodico"
					,CobroPeriodicoDetalle."PorcentajeCobroPeriodico"
					,CobroPeriodicoDetalle."MontoDesde"
					,CobroPeriodicoDetalle."MontoHasta"
			FROM "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodico" As ContCobPer
				LEFT JOIN  "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodicoDetalle" As DetContCobPer
					ON ContCobPer."IdContratoCobroPeriodico" = DetContCobPer."IdContratoCobroPeriodico"
				LEFT JOIN "BSCL.Facturacion.BD::FAC.CobroPeriodico"  AS CobroPeriodico
					ON ContCobPer."IdContratoCobroPeriodico" = CobroPeriodico."IdContratoCobroPeriodico"
				LEFT JOIN "BSCL.Facturacion.BD::FAC.CobroPeriodicoDetalle"  AS CobroPeriodicoDetalle
					ON CobroPeriodico."IdCobroPeriodico" = CobroPeriodicoDetalle."IdCobroPeriodico"
				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoInstrumentoContratoCobroPeriodico" AS Instrumento
					ON DetContCobPer."IdClasificacionTipoInstrumento" = Instrumento."IdClasificacionTipoInstrumento"
				   AND DetContCobPer."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS Concepto
					ON DetContCobPer."IdConceptoCobroPeriodico" = Concepto."IdConcepto"
				LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoContratoCobroPeriodico" AS TipoContrato
					ON ContCobPer."IdTipoContratoCobroPeriodico" = TipoContrato."IdTipoContratoCobroPeriodico"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" AS Cobro
					ON CobroPeriodico."IdCobro" = Cobro."IdCobro"
			WHERE (ContCobPer."IdTipoContratoCobroPeriodico" = :p_IdTipoContratoCobroPeriodico OR  :p_IdTipoContratoCobroPeriodico = 0 )
			  AND (ContCobPer."IdCliente" = :p_IdCliente OR :p_IdCliente = 0)
			  AND (ContCobPer."IdPortafolio" = :p_IdPortafolio OR :p_IdPortafolio = 0)
			  AND (DetContCobPer."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR :p_IdClasificacionTipoInstrumento = '')
			  AND (DetContCobPer."IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento = '');
		  
			  
		-- CONTRATOS AGRUPADOS POR RUT
		UPDATE Comprob
		SET Comprob."IdPortafolio" =  Contrat."IdPortafolio"
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Contrat."FormatoContratoCobroPeriodico" = 2;
			
    	INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" (
    				"IdCliente" ,"IdPortafolio" ,"IdClasificacionTipoInstrumento" ,"IdTipoInstrumento" , "MontoOperado", "MontoComision","MontoDerecho")
		SELECT	"IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento" 	 
				,(SUM("MontoFactura") - SUM("MontoNotaCredito")) AS "MontoOperado"
				,0 AS "MontoComision"
				,0 AS "MontoDerecho"
    	FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Comprobantes"
    	GROUP BY "IdCliente", "IdPortafolio", "IdClasificacionTipoInstrumento", "IdTipoInstrumento";
    	

    	---------------------------------------------- COMISION ----------------------------------------------
    	-- COMISION VARIABLE POR RANGO
		UPDATE Comprob
		SET Comprob."MontoComision" = (Comprob."MontoOperado" * Contrat."PorcentajeCobroPeriodico")
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('COFI','COSI','COVA','COAI','COEX','COIF','CCOR')
			   AND Contrat."TipoCobroPeriodico" = 'V'
			   AND Contrat."PorRango" = 'S' 
			   AND Comprob."MontoOperado" BETWEEN  Contrat."MontoDesde" AND Contrat."MontoHasta"; 

    	-- COMISION FIJA POR RANGO 
		UPDATE Comprob
		SET Comprob."MontoComision" = Contrat."MontoCobroPeriodico"
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('COFI','COSI','COVA','COAI','COEX','COIF','CCOR')
			   AND Contrat."TipoCobroPeriodico" = 'F'
			   AND Contrat."PorRango" = 'S' 
			   AND Comprob."MontoOperado" BETWEEN  Contrat."MontoDesde" AND Contrat."MontoHasta"; 
    	
    	-- COMISION VARIABLE SIN RANGO
		UPDATE Comprob
		SET Comprob."MontoComision" = (Comprob."MontoOperado" * Contrat."PorcentajeCobroPeriodico")
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('COFI','COSI','COVA','COAI','COEX','COIF','CCOR')
			   AND Contrat."TipoCobroPeriodico" = 'V'
			   AND Contrat."PorRango" = 'N' ;
			   
    	-- COMISION FIJA SIN RANGO 
		UPDATE Comprob
		SET Comprob."MontoComision" = Contrat."MontoCobroPeriodico"
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('COFI','COSI','COVA','COAI','COEX','COIF','CCOR')
			   AND Contrat."TipoCobroPeriodico" = 'F'
			   AND Contrat."PorRango" = 'N' ;
 			   
			   
	---------------------------------------------- DERECHO ----------------------------------------------
    	-- DERECHO VARIABLE POR RANGO
		UPDATE Comprob
		SET Comprob."MontoDerecho" = (Comprob."MontoOperado" * Contrat."PorcentajeCobroPeriodico")
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('DESI','DEFI','DEEX','DEVA')
			   AND Contrat."TipoCobroPeriodico" = 'V'
			   AND Contrat."PorRango" = 'S' 
			   AND Comprob."MontoOperado" BETWEEN  Contrat."MontoDesde" AND Contrat."MontoHasta" ;

    	-- DERECHO FIJA POR RANGO 
		UPDATE Comprob
		SET Comprob."MontoDerecho" = Contrat."MontoCobroPeriodico"
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('DESI','DEFI','DEEX','DEVA')
			   AND Contrat."TipoCobroPeriodico" = 'F'
			   AND Contrat."PorRango" = 'S' 
			   AND Comprob."MontoOperado" BETWEEN  Contrat."MontoDesde" AND Contrat."MontoHasta" ;
    	
    	-- DERECHO VARIABLE SIN RANGO
		UPDATE Comprob
		SET Comprob."MontoDerecho" = (Comprob."MontoOperado" * Contrat."PorcentajeCobroPeriodico")
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('DESI','DEFI','DEEX','DEVA')
			   AND Contrat."TipoCobroPeriodico" = 'V'
			   AND Contrat."PorRango" = 'N' ;
			   
    	-- DERECHO FIJA SIN RANGO 
		UPDATE Comprob
		SET Comprob."MontoDerecho" = Contrat."MontoCobroPeriodico"
		FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Comprob
			INNER JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contrat 
				ON Comprob."IdCliente" = Contrat."IdCliente"
			   AND Comprob."IdPortafolio" = Contrat."IdPortafolio"
			   AND Comprob."IdClasificacionTipoInstrumento" = Contrat."IdClasificacionTipoInstrumento"
			   AND Comprob."IdTipoInstrumento" = Contrat."IdTipoInstrumento"
			   AND Contrat."IdCobro" IN ('DESI','DEFI','DEEX','DEVA')
			   AND Contrat."TipoCobroPeriodico" = 'F'
			   AND Contrat."PorRango" = 'N';
			   
		p_out_tbl = SELECT   Contratos."IdContratoCobroPeriodico"
							,Contratos."DocumentoIdentidad"
							,Contratos."NombreCliente"
							,Contratos."NombreContrato"
							,CASE WHEN Contratos."FormatoContratoCobroPeriodico" = 2 THEN 'Agrupado'
								ELSE 'Detallado'
							END As "FormatoContratoCobroPeriodico"
							,CAST(SUM(IFNULL(Cobros."MontoOperado", 0)) as DECIMAL(26,8)) 		AS "MontoOperado"
							,CAST(SUM(IFNULL(Cobros."MontoComision", 0)) as DECIMAL(26,8)) 		AS "MontoComision"
							,CAST(SUM(IFNULL(Cobros."MontoDerecho", 0)) as DECIMAL(26,8)) 		AS "MontoDerecho"
					FROM "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Contratos" Contratos
						LEFT JOIN "BSCL.Facturacion.BD::GBL.ConsultarContratosCobrosPeriodicos_PRC_Cobros" Cobros
							ON Contratos."IdCliente" = Cobros."IdCliente"
						   AND Contratos."IdPortafolio" = Cobros."IdPortafolio"
						   AND Contratos."IdClasificacionTipoInstrumento" = Cobros."IdClasificacionTipoInstrumento"
						   AND Contratos."IdTipoInstrumento" = Cobros."IdTipoInstrumento"
					GROUP BY Contratos."IdContratoCobroPeriodico"
							,Contratos."DocumentoIdentidad"
							,Contratos."NombreCliente"
							,Contratos."NombreContrato"
							,Contratos."FormatoContratoCobroPeriodico"
					ORDER BY Contratos."IdContratoCobroPeriodico" DESC;
	END;	   
	------------------------------------------
	-- SALIDA
	------------------------------------------

END;
GO