SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ConsultaCobrosClientes";

CREATE PROCEDURE 
--  
--  Proposito: 
--  Autor: NRM - BCS 
--  Fecha Creacion: 07/02/2017
-- 
-- Tablas Salida
-- BSCL.Facturacion.BD::GBL.ConsultaCobrosClientes_OUT_Cobros
--
"BSCL.Facturacion.BD.Servicios::ConsultaCobrosClientes" (
	IN  p_IdEmpresa			BIGINT,
	IN	p_IdEjecutivo		BIGINT,
	IN	p_IdCliente			BIGINT,
	IN	p_IdPortafolio		BIGINT,
	IN	p_IdTipoCliente		NVARCHAR(40),
	IN	p_FechaDesde		NVARCHAR(10),
	IN	p_FechaHasta		NVARCHAR(10),
	IN	p_IdTipoInstrumento	NVARCHAR(3),
	IN	p_SoloConContrato	NVARCHAR(1)		DEFAULT 'N'
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	
	v_Cobros =
	WITH
	Cobros as (
			SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComision",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDerecho",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGasto",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpuesto",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalRetencion"
             FROM (
             		SELECT  CmbteCobro."IdComprobante"
					       ,Cobro."IdCobroClasificacion"
					       ,CobroClasificacion."CodigoCobroClasificacion"
					       ,SUM(COALESCE(CmbteCobro."MontoCobro",0)) AS "MontoCobro"
					  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" AS CmbteCobro 
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS Cobro ON (Cobro."IdCobro" = CmbteCobro."IdCobro")
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CobroClasificacion ON (CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion")
					 WHERE CmbteCobro."IdEstado" = 'ACT'
					 GROUP BY CmbteCobro."IdComprobante",Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion"
				  )
            GROUP BY "IdComprobante"
	),
	Contratos as (
			SELECT  CobPer."IdPortafolio"
				   ,TipoContr."Nombre" as "TipoContratoCobroPeriodico"
				   ,Dicc."Descripcion" as "FormatoContratoCobroPeriodico"
				   ,CobPer."IdMoneda"
				   ,CobPerDet."IdTipoInstrumento"
				   ,CobPerDet."IdClasificacionTipoInstrumento"
				   ,CobPerDet."IdConceptoCobroPeriodico"
			  FROM "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodico" as CobPer
			 INNER JOIN "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodicoDetalle" as CobPerDet ON CobPerDet."IdContratoCobroPeriodico" = CobPer."IdContratoCobroPeriodico"
			 INNER JOIN "BSCL.Facturacion.BD::FAC.TipoContratoCobroPeriodico" as TipoContr ON TipoContr."IdTipoContratoCobroPeriodico" = CobPer."IdTipoContratoCobroPeriodico"
			  LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" as Dicc ON Dicc."CodDiccionario" = (CASE WHEN CobPer."FormatoContratoCobroPeriodico" = 2 THEN 'UI_AGRUPADO'
			 																						   ELSE 'UI_DETALLADO'
			 																					   END)
	),
	TipoOperacion as (
			SELECT   "IdTipoOperacion"
					,"Accion"
					,CASE WHEN "Accion" = 'COMPRA' THEN -1 ELSE 1 END AS "FuncionOperador"
			  FROM "BSCL.Plataforma.BD::INS.TipoOperacion"
	)
	SELECT   comprobante."IdCliente"
			,CAST(Cliente."NombreCompleto" as NVARCHAR(300)) as "NombreCliente"
			,Cliente."DocumentoIdentidad"
			,Cliente."IdTipoCliente"
			,comprobante."IdPortafolio"
			,CAST(portalio."CodigoPortafolio" ||' '|| portalio."Proposito" as NVARCHAR(300)) AS "Portafolio"
			,Contratos."TipoContratoCobroPeriodico"
			,Contratos."FormatoContratoCobroPeriodico"
			,CAST(ejecutivo."NombreCompletoEjecutivo" as NVARCHAR(300)) AS "NombreCompletoEjecutivo"
			,ejecutivo."NombreSucursal"
			
			----------------------------------COMPROBANTES
			,comprobante."IdClasificacionTipoInstrumento"
			,comprobante."IdTipoInstrumento"
			,TipIns."Nombre" As "NombreTipoInstrumento"
			,comprobante."IdMonedaComprobante"
			,SUM("FuncionOperador" * COALESCE(comprobante."MontoComprobante",0)) AS "MontoComprobante"
			,SUM(COALESCE(comprobante."MontoComprobanteCobro",0)) AS "MontoCobroComprobante"
			,SUM(COALESCE(comprobante."MontoFinal",0)) AS "MontoFinal"
	
			----------------------------------COBROS
			,SUM(COALESCE(Cobros."TotalComision",0)) AS "TotalComision"
			,SUM(COALESCE(Cobros."TotalDerecho",0)) AS "TotalDerecho"
			,SUM(COALESCE(Cobros."TotalGasto",0)) AS "TotalGasto"
			,SUM(COALESCE(Cobros."TotalImpuesto",0)) AS "TotalImpuesto"
			,SUM(COALESCE(Cobros."TotalRetencion",0)) AS "TotalRetencion"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" as ejecutivo 
				 ON ejecutivo."IdPortafolio" = comprobante."IdPortafolio"
				AND ejecutivo."EsEjecutivoVigente" = 'S'
	LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as portalio 
				 ON portalio."IdPortafolio" = comprobante."IdPortafolio"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cliente 
				 ON Cliente."IdCliente" = comprobante."IdCliente"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" As TipIns 
				 ON TipIns."IdTipoInstrumento" = comprobante."IdTipoInstrumento"
	LEFT OUTER JOIN TipoOperacion AS TipoOperacion
				 ON TipoOperacion."IdTipoOperacion" = comprobante."IdTipoOperacion"
	LEFT OUTER JOIN Cobros AS Cobros 
				 ON Cobros."IdComprobante" = comprobante."IdComprobante"
	LEFT OUTER JOIN Contratos AS Contratos 
				 ON Contratos."IdPortafolio" = comprobante."IdPortafolio"
				AND Contratos."IdClasificacionTipoInstrumento" = comprobante."IdClasificacionTipoInstrumento"
				AND Contratos."IdTipoInstrumento" = comprobante."IdTipoInstrumento"
				AND Contratos."IdMoneda" = comprobante."IdMonedaComprobante"
				--AND Contratos."IdConceptoCobroPeriodico" = comprobante."IdConcepto"
								
   WHERE comprobante."IdEmpresa" = :p_IdEmpresa
     AND comprobante."FechaComprobante" BETWEEN :p_FechaDesde AND :p_FechaHasta
     AND (:p_IdCliente IS NULL OR comprobante."IdCliente" = :p_IdCliente)
     AND (:p_IdPortafolio IS NULL OR comprobante."IdPortafolio" = :p_IdPortafolio)
     AND (:p_IdEjecutivo IS NULL OR ejecutivo."IdEmpleadoEjecutivoPortafolio" = :p_IdEjecutivo)
	 AND (:p_IdTipoInstrumento IS NULL OR TipIns."IdTipoInstrumento" = :p_IdTipoInstrumento)
	 AND (:p_IdTipoCliente IS NULL OR Cliente."IdTipoCliente" = :p_IdTipoCliente)
	 AND (Contratos."TipoContratoCobroPeriodico" IS NOT NULL OR :p_SoloConContrato = 'N')
	 
   GROUP BY  comprobante."IdCliente",Cliente."NombreCompleto",Cliente."DocumentoIdentidad",Cliente."IdTipoCliente",comprobante."IdPortafolio"
			,portalio."Proposito",portalio."CodigoPortafolio" ,Contratos."TipoContratoCobroPeriodico",Contratos."FormatoContratoCobroPeriodico"
			,ejecutivo."NombreCompletoEjecutivo",ejecutivo."NombreSucursal",comprobante."IdClasificacionTipoInstrumento"
			,comprobante."IdTipoInstrumento",TipIns."Nombre",comprobante."IdMonedaComprobante"
						
   ORDER BY comprobante."IdCliente",Cliente."NombreCompleto",comprobante."IdPortafolio";


   
   INSERT INTO "BSCL.Facturacion.BD::GBL.ConsultaCobrosClientes_OUT_Cobros"
   SELECT "IdCliente"
		 ,"NombreCliente"
		 ,"DocumentoIdentidad"
		 ,"IdTipoCliente"
		 ,"IdPortafolio"
		 ,"Portafolio"
		 ,"TipoContratoCobroPeriodico"
		 ,"FormatoContratoCobroPeriodico"
		 ,"NombreCompletoEjecutivo"
		 ,"NombreSucursal"
		 ,"IdClasificacionTipoInstrumento"
		 ,"IdTipoInstrumento"
		 ,"NombreTipoInstrumento"
		 ,"IdMonedaComprobante"
		 ,"MontoComprobante"
		 ,"MontoCobroComprobante"
		 ,"MontoFinal"
		 ,"TotalComision"
		 ,"TotalDerecho"
		 ,"TotalGasto"
		 ,"TotalImpuesto"
		 ,"TotalRetencion"
     FROM :v_Cobros;
   
END;
