SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizaciones";
GO
CREATE PROCEDURE
 --
 --  Proposito: Vista
 --
 --  Empresa: Ingeniería ProNet IT SpA.
 --  Autor: Hans Muller :: HMR
 --  Fecha Creacion: 03/09/2016
 --  Id Servicio = 12001
 --
 "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizaciones" (
	 IN p_IdEmpresa              	BIGINT    DEFAULT 0,
	 IN p_Fecha       	 			NVARCHAR(10)  DEFAULT '0001-01-01',
	 IN p_Proceso       			NVARCHAR(40)  DEFAULT '',
	 IN p_FechaContableInicio    	NVARCHAR(10) DEFAULT '',
	 IN p_FechaContableFinal     	NVARCHAR(10) DEFAULT '',
	 IN p_IdTipoCentralizacion    	SMALLINT  DEFAULT 0,
	 IN p_IdCentralizacion     		NUMERIC(18,0) DEFAULT 0,
	 IN p_FechaProcesoInicio     	VARCHAR(10)  DEFAULT '',
	 IN p_FechaProcesoFinal     	VARCHAR(10)  DEFAULT '',
	 IN p_Estado        			NVARCHAR(1)  DEFAULT '',
	 -- Parametros salida del servicio publico
	 OUT p_out_TabCentralizaciones TABLE (
										 "NombreTipoContralizacion" NVARCHAR(50),
										 "FechaProceso"    NVARCHAR(10),
										 "HoraProceso"    NVARCHAR(8 ),
										 "FechaContable"    NVARCHAR(10),
										 "CodigoUsuario"    NVARCHAR(40),
										 "Estado"     NVARCHAR(1),
										 "NombreEstado"		NVARCHAR(50),
										 "NemoEmpresa"    NVARCHAR(10),
										 "Glosa"      NVARCHAR(50),
										 "IdTipoCentralizacion"  BIGINT,
										 "IdCentralizacion"   BIGINT
									 ),
 --Salidas para mensajes de advertencias y errores
 OUT p_out_ResultadoProceso      	TABLE ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
 OUT p_out_AdvertenciasContador     INT,
 OUT p_out_AdvertenciasDetalle      TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
 OUT p_out_ErroresContador          INT,
 OUT p_out_ErroresDetalle           TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
 DECLARE SALIDA_CONTROLADA    CONDITION;
 DECLARE SALIDA_CONTROLADA_NO_DATA  CONDITION;
 DECLARE v_resultadoRegla    NVARCHAR(40);
 DECLARE v_ErroresDetalle   TABLE("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT
 SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
 DECLARE v_RechazadosPorValidacion TABLE("IdComprobante" BIGINT) DEFAULT
 SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;
 DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
 BEGIN
 --salida/error/advertencia controlado por este servicio
 SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
 SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
 END;
 DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
 BEGIN
	 v_hash= SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2",
	 '' AS "ValorDato2" FROM DUMMY;
	 v_out_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
	 CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
	 p_out_ErroresDetalle = SELECT "IdInterno" AS "IdComprobante", "Codigo" AS "Codigo", "Descripcion" AS "Descripcion"
	 FROM :v_out_hash UNION ALL
	 SELECT * FROM :p_out_ErroresDetalle;
	 SELECT COUNT(1) INTO p_out_ErroresContador
	 FROM :p_out_ErroresDetalle;
 END;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION
 
 BEGIN
 DECLARE v_errorExt NVARCHAR(40);
	 IF ::SQL_ERROR_CODE > 10000 THEN
		 --error pseudo-controlado pero desde un Sub-SP
		 CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
		 p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
		 SELECT 0 AS "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
		 SELECT COUNT(1)
		 INTO p_out_ErroresContador
		 FROM :p_out_ErroresDetalle;
	 ELSE
		 --Error interno no manejado  (El log será captado por la capa de XS control)
		 RESIGNAL;
	 END IF;
 END;
 
	 p_out_AdvertenciasContador  := 0;
	 p_out_ErroresContador  := 0;
	 p_out_AdvertenciasDetalle = SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
	 p_out_ErroresDetalle  = SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
	 p_out_ResultadoProceso     = SELECT '' AS "Campo", 0 AS "Id", '' AS "Servicio", '' AS "Resultado" FROM DUMMY WHERE 1=2;
	 v_RechazadosPorValidacion   = SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;
	 BEGIN
	 
		 p_out_TabCentralizaciones =
		 SELECT
		 ctb_itzcentralizaciontipo."Nombre"             AS "NombreTipoContralizacion"
		 ,ctb_itzcentralizacion."FechaProceso"               AS "FechaProceso"
		 ,ctb_itzcentralizacion."HoraProceso"                                             AS "HoraProceso"
		 ,ctb_itzcentralizacion."FechaContable"              AS "FechaContable"
		 ,ctb_itzcentralizacion."CodigoUsuario"                                           AS "CodigoUsuario"
		 ,ctb_itzcentralizacion."Estado"                                                  AS "Estado"
		 ,EstadoContabilidad."NombreEstado"													AS "NombreEstado"	
		 ,ctb_itzempresa."Nemo"                                                           AS "NemoEmpresa"
		 ,ctb_itzcentralizacion."Glosa"                                                   AS "Glosa"
		 ,ctb_itzcentralizacion."IdTipoCentralizacion"                                    AS "IdTipoCentralizacion"
		 ,ctb_itzcentralizacion."IdCentralizacion"                                        AS "IdCentralizacion"
		 FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion" AS ctb_itzcentralizacion
		 INNER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS ctb_itzcentralizaciontipo ON
		 ctb_itzcentralizaciontipo."IdEmpresa"     = ctb_itzcentralizacion."IdEmpresa"
		 AND ctb_itzcentralizaciontipo."IdTipoCentralizacion" = ctb_itzcentralizacion."IdTipoCentralizacion"
		 AND (ctb_itzcentralizaciontipo."IdTipoCentralizacion"  = p_IdTipoCentralizacion or p_IdTipoCentralizacion = 0)
		 INNER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS ctb_itzempresa ON
		 ctb_itzempresa."IdEmpresa"        = ctb_itzcentralizaciontipo."IdEmpresa"
		 LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  	EstadoContabilidad."IdEstadoContabilidad" = ctb_itzcentralizacion."Estado"	 
		 WHERE
		 (ctb_itzcentralizacion."IdEmpresa"   = p_IdEmpresa)
		 AND ((ctb_itzcentralizacion."FechaContable" BETWEEN p_FechaContableInicio AND p_FechaContableFinal)  OR p_FechaContableInicio = '')
		 AND (ctb_itzcentralizacion."IdCentralizacion" = p_IdCentralizacion          OR p_IdCentralizacion = 0)
		 AND ((ctb_itzcentralizacion."FechaProceso"  BETWEEN p_FechaProcesoInicio AND p_FechaProcesoFinal)  OR p_FechaProcesoInicio = '')
		 AND (ctb_itzcentralizacion."Estado"    = p_Estado             OR p_Estado = '')
		 ORDER BY ctb_itzcentralizaciontipo."Nombre" ASC;
	 END;
 END;