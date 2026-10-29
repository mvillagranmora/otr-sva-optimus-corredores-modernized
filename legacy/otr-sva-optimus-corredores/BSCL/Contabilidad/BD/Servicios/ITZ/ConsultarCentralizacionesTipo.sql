SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizacionesTipo";
GO
CREATE PROCEDURE
 --
 --  Proposito: Vista
 --
 --  Empresa: Ingeniería ProNet IT SpA.
 --  Autor: Hans Muller :: HMR
 --  Fecha Creacion: 03/09/2016
 --  Id Servicio = 12002
 --
 "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarCentralizacionesTipo" (
 IN p_IdEmpresa              BIGINT    DEFAULT 0,
 IN p_CodigoUsuario      NVARCHAR(40)  DEFAULT '',
 IN p_IdTipoCentralizacion    SMALLINT  DEFAULT 0,
 IN p_Nemo        NVARCHAR(20)  DEFAULT '',
 IN p_Estado        CHAR(1)   DEFAULT '',
 -- Parametros salida del servicio publico
 OUT p_out_TabCentralizacionesTipo TABLE (
 "Check"     BIGINT,
 "IdCentralizacionTipo" BIGINT,
 "NombreTipo"   NVARCHAR(50),
 "NemoTipo"    NVARCHAR(20),
 "NombreEmpresa"   NVARCHAR(60),
 "NemoEmpresa"   NVARCHAR(10),
 "Estado"     NVARCHAR(1),
 "RequiereFechaConsulta" NVARCHAR(1), 
 "NombreEstado" NVARCHAR(50)
 ),
 -- Sección Fija ----
 --Salidas para mensajes de advertencias y errores
 OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
 OUT p_out_AdvertenciasContador      INT,
 OUT p_out_AdvertenciasDetalle       TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
 OUT p_out_ErroresContador           INT,
 OUT p_out_ErroresDetalle            TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
 DECLARE SALIDA_CONTROLADA    CONDITION;
 DECLARE SALIDA_CONTROLADA_NO_DATA  CONDITION;
 DECLARE v_resultadoRegla    NVARCHAR(40);
 DECLARE v_ErroresDetalle   TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT
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
 p_out_ErroresDetalle = SELECT "IdInterno" AS "IdComprobante", "Codigo" AS "Codigo",
 "Descripcion" AS "Descripcion"
 FROM :v_out_hash UNION ALL
 SELECT * FROM :p_out_ErroresDetalle;
 SELECT COUNT(1)
 INTO p_out_ErroresContador
 FROM :p_out_ErroresDetalle;
 END;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION
 BEGIN
 DECLARE v_errorExt NVARCHAR(40);
 IF ::SQL_ERROR_CODE > 10000 THEN
 --error pseudo-controlado pero desde un Sub-SP
 CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE, v_errorExt);
 p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
 SELECT 0 AS "IdComprobante", v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;
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
 BEGIN
 p_out_TabCentralizacionesTipo =
 SELECT
 0              AS "Check"
 ,ctb_itzcentralizaciontipo."IdTipoCentralizacion"  AS "IdCentralizacionTipo"
 ,ctb_itzcentralizaciontipo."Nombre"      AS "NombreTipo"
 ,ctb_itzcentralizaciontipo."Nemo"      AS "NemoTipo"
 ,ctb_itzempresa."Nombre"        AS "NombreEmpresa"
 ,ctb_itzempresa."Nemo"         AS "NemoEmpresa"
 ,ctb_itzcentralizaciontipo."Estado"      AS "Estado"
 ,ctb_itzcentralizaciontipo."RequiereFechaConsulta"      AS "RequiereFechaConsulta"
 ,EstadoContabilidad."NombreEstado"			AS "NombreEstado"
 FROM "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS ctb_itzcentralizaciontipo
 INNER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS ctb_itzempresa ON
 ctb_itzempresa."IdEmpresa" = ctb_itzcentralizaciontipo."IdEmpresa"
 LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  	EstadoContabilidad."IdEstadoContabilidad" = ctb_itzcentralizaciontipo."Estado"	
 WHERE
 (ctb_itzempresa."IdEmpresa"        = p_IdEmpresa)
 AND (ctb_itzcentralizaciontipo."IdTipoCentralizacion"  = p_IdTipoCentralizacion  OR p_IdTipoCentralizacion = 0)
 AND (ctb_itzcentralizaciontipo."Nemo"      = p_Nemo      OR p_Nemo = '')
 AND (ctb_itzcentralizaciontipo."Estado"      = p_Estado      OR p_Estado = '')
 ORDER BY ctb_itzcentralizaciontipo."Nombre";
 END;
 END;
