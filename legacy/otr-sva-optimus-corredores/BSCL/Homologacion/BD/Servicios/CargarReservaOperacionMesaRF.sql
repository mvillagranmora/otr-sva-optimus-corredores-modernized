 SET SCHEMA BSCL;
 DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarReservaOperacionMesaRF";
 CREATE PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarReservaOperacionMesaRF"(
 -- Parametros entrada comunes en todos los servicios publicos
 IN  p_IdEmpresa     BIGINT,   -- ID Empresa destino
 IN  p_IdUsuario     BIGINT,   -- ID del usuario que solicita el servicio
 IN  p_IdCarga       BIGINT,          -- ID de la carga
 IN  p_Modulo              NVARCHAR(3),     -- modulo
 IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental,
 IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
 IN  p_FechaFinal     NVARCHAR(10),   --YYYY-MM-DD
 -- Salidas para mensajes de advertencias y errores
 OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
 OUT p_out_ErroresContador int,
 OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
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
 DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
 DECLARE v_erroroutmsg  NVARCHAR(520);
 DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
 BEGIN
 --salida/error/advertencia controlado por este servicio
 select count(1) into p_out_ErroresContador
 from :p_out_ErroresDetalle;
 END;
 DECLARE EXIT HANDLER FOR SQLEXCEPTION
 BEGIN
 declare v_errorExt NVARCHAR(40);
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
 p_out_ErroresContador  := 0;
 p_out_ErroresDetalle  = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
 BEGIN
 ----------------------------------------------------
 --Declaraciones Variables
 -----------------------------------------------------
 -- declaracion variables
 DECLARE v_total_mov_cargadas DECIMAL(26,8):=0;
 DECLARE v_count_ope BIGINT :=0;
 DECLARE v_Quantity BIGINT:=0;
 -----------------------------------------
 -- ELIMINO ERRORES DE ANTERIORES CARGAS--
 -----------------------------------------
		 delete from "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		 where "IdCarga" = p_IdCarga
		 and "Entidad" ='MESARF_RESERVA';
		 if p_Modalidad ='R' then
		 delete from "BSCL"."BSCL.Homologacion.BD::HOM.Reserva"
		 where "IdCarga" = p_IdCarga;
		 end if;
		 
		 v_operaciones = SELECT * FROM
		 (
		 select
		 RANK() over (partition by  DETALLE_PACTO."SEC_COMPROM" order by DETALLE_PACTO."COD_PER" desc ) as RANK
		 ,DETALLE_PACTO."SEC_COMPROM"
		 ,DETALLE_PACTO."COD_PER"
		 ,DETALLE_PACTO."SEC_DETALLE"
		 from "BSCL_VIRTUAL"."GES_TBCPMSPC" AS PACTO
		 INNER JOIN "BSCL_VIRTUAL"."GES_TBCPDTPC" AS DETALLE_PACTO
		 ON DETALLE_PACTO."COD_PER" = PACTO."COD_PER"
		 AND DETALLE_PACTO."SEC_COMPROM" = PACTO."SEC_COMPROM"
		 where PACTO."FEC_OPERAC" BETWEEN p_FechaInicial AND p_FechaFinal
		 )
		 WHERE RANK = 1;
		 select count(1) into v_count_ope from :v_operaciones;
		 
		 IF v_count_ope > 0 THEN
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Reserva"
			 (
			 "IdReserva","IdOperacion","FolioCarteraOrigen","FolioOperacion","IdEstado","IdUsuarioCreacion","IdTipoProducto","IdConcepto"
			 ,"Tasa","Precio","MontoValorizado","IdClasificacionTipoInstrumento","IdInstrumento","IdTipoInstrumento","IdTipoReajuste","IdEmisor"
			 ,"FechaVencimiento","EsFungible","IdBolsa","IdBoveda","CodigoIFRS","CantidadInicial","ValorRescateInicial","MontoValorizadoReserva"
			 ,"FechaVencimientoReserva","FechaVencimientoProducto","FechaCreacion","IngIdUsuario","IngCantidad","IngValorRescate","IngMontoValorizado"
			 ,"IngFecha","EgrIdUsuario","EgrCantidad","EgrValorRescate","EgrMontoValorizado","EgrFecha","SecuenciaCompromisoMigracion","IdCarga"
			 ,"COD_PER","SEC_COMPROM","EMISOR","NEMO","TIPO_REAJ","MERCADO","SEC_DETALLE","RUT_CLI","SEC_RUT_CLI","LIN_COMPROM"
			 )
			 select
			 "BSCL.Custodia.BD.Secuencias::Reserva".NEXTVAL as "IdReserva"
			 , OPERACION."IdOperacion"   as "IdOperacion"
			 , 0 as "FolioCarteraOrigen"
			 , 0 as "FolioOperacion"
			 , 'ING' as "IdEstado"
			 , OPERACION."IdAgente" as "IdUsuarioCreacion"
			 , 'MERF' as "IdTipoProducto"
			 , '' as "IdConcepto"
			 , TBCPDTPC."TIR" as "Tasa"
			 , TBCPDTPC."PRECIO" as "Precio"
			 , TBCPDTPC."VALOR_PTE" as "MontoValorizado"
			 , TIPO_INSTRUMENTO."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			 , INSTRUMENTO."IdInstrumento" as "IdInstrumento"
			 , INSTRUMENTO."IdTipoInstrumento" as "IdTipoInstrumento"
			 , TIPO_REAJUSTE."ValorDestino" as "IdTipoReajuste"
			 , TBCPDTPC."EMISOR" as "IdEmisor"
			 , TBCPDTPC."FEC_VCTO" as "FechaVencimiento" -- DEL INSTRUMENTO
			 , TBCPDTPC."IND_FUNGIBILIDAD" as "EsFungible"
			 , ''--OPERACION."IdBolsa" as "IdBolsa" // se debe revisar ya que debe obtener de las carteras
			 , TBCPDTPC."COD_BOVEDA" as "IdBoveda"
			 , '' as "CodigoIFRS"
			 , TBCPDTPC."MONTO_INI_CAP" as "CantidadInicial"
			 , TBCPDTPC."VAL_RESC" as "ValorRescateInicial"
			 , TBCPDTPC."VALOR_PTE" as "MontoValorizadoReserva"
			 , OPERACION."FechaOperacion" as "FechaVencimientoReserva"-- REVISAR
			 , OPERACION."FechaVencimiento" as "FechaVencimientoProducto"-- DEL PACTO
			 , OPERACION."FechaOperacion" as "FechaCreacion"
			 , OPERACION."IdAgente" as "IngIdUsuario"
			 , TBCPDTPC."CANTIDAD" as "IngCantidad"
			 , TBCPDTPC."VAL_RESC" as "IngValorRescate"
			 , TBCPDTPC."VALOR_PTE" as "IngMontoValorizado"
			 , OPERACION."FechaOperacion" as "IngFecha"
			 , 0 as "EgrIdUsuario"
			 , 0 as "EgrCantidad"
			 , 0 as "EgrValorRescate"
			 , 0 as "EgrMontoValorizado"
			 , '0001-01-01' as "EgrFecha"
			 , TBCPDTPC."SEC_COMPROM" as "SecuenciaCompromisoMigracion"
			 , p_IdCarga as "IdCarga"
			 , TBCPDTPC."COD_PER" as "COD_PER"
			 , TBCPDTPC."SEC_COMPROM" as "SEC_COMPROM"
			 , TBCPDTPC."EMISOR" as "EMISOR"
			 , TBCPDTPC."NEMO" as "NEMO"
			 , TBCPDTPC."TIPO_REAJ" as "TIPO_REAJ"
			 , TBCPDTPC."MERCADO" as "MERCADO"
			 , TBCPDTPC."SEC_DETALLE" as "SEC_DETALLE"
			 , TBCPDTPC."RUT_CLI" as "RUT_CLI"
			 , TBCPDTPC."SEC_RUT_CLI" as "SEC_RUT_CLI"
			 , TBCPDTPC."LIN_COMPROM" as "LIN_COMPROM"
			 FROM "BSCL_VIRTUAL"."GES_TBCPDTPC" AS TBCPDTPC
			 INNER JOIN  :v_operaciones AS OPERACIONES_TEMP
			 ON  OPERACIONES_TEMP."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 AND OPERACIONES_TEMP."COD_PER" = TBCPDTPC."COD_PER"
			 AND OPERACIONES_TEMP."SEC_DETALLE" = TBCPDTPC."SEC_DETALLE"
			 INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" AS OPERACION
			 ON OPERACION."SEC_COMPROM" = OPERACIONES_TEMP."SEC_COMPROM"
			 AND OPERACION."COD_PER" = OPERACIONES_TEMP."COD_PER"
			 INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
			 ON INSTRUMENTO."Nemotecnico" = TRIM(TBCPDTPC."NEMO")
			 INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO
			 ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
			 LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_REAJUSTE
			 ON TIPO_REAJUSTE."TipoValor"    = 'TipoReajuste'
			 AND TIPO_REAJUSTE."Entidad"     = 'FACTURA'
			 AND TIPO_REAJUSTE."ValorOrigen" = TBCPDTPC."TIPO_REAJ"
			 LEFT JOIN "BSCL.Homologacion.BD::HOM.Reserva" as RESERVA
			 ON RESERVA."COD_PER" = TBCPDTPC."COD_PER"
			 AND RESERVA."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 WHERE TBCPDTPC."RUT_CLI"  = CASE WHEN p_RutCliente = '        0-0'  THEN  TBCPDTPC."RUT_CLI" ELSE p_RutCliente END
			 AND OPERACION."FechaOperacion" BETWEEN p_FechaInicial and p_FechaFinal
			 AND RESERVA."SEC_COMPROM" IS NULL;
			 END IF;
			 -- contamos las operaciones cargadas
			 SELECT COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Homologacion.BD::HOM.Reserva"
			 WHERE "IdCarga" = p_IdCarga;
			 
			 ------------------------------
			 --cargamos el log de errores--
			 ------------------------------
			 -- debe existir la operacion
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
			 ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
			 ,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5"
			 ,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8"
			 ,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
			 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
			 , p_IdCarga as "IdCarga"
			 ,'MESARF_RESERVA' as "Entidad"
			 ,'Detalle de tiene su cabecera en tabla operacion' as "Mensaje"
			 , 'COD_PER' as "NombreCampoGenerico1"
			 , TBCPDTPC."COD_PER"  as "ValorCampoGenerico1"
			 , '' as "NombreCampoGenerico2"
			 , '' as "ValorCampoGenerico2"
			 , '' as "NombreCampoGenerico3"
			 , '' as "ValorCampoGenerico3"
			 , '' as "NombreCampoGenerico4"
			 , '' as "ValorCampoGenerico4"
			 , '' as "NombreCampoGenerico5"
			 , '' as "ValorCampoGenerico5"
			 ,'SEC_COMPROM' as "NombreCampoGenerico6"
			 , TBCPDTPC."SEC_COMPROM" as "ValorCampoGenerico6"
			 ,'' as "NombreCampoGenerico7"
			 , 0 as "ValorCampoGenerico7"
			 ,'' as "NombreCampoGenerico8"
			 , 0 as "ValorCampoGenerico8"
			 ,'' as "NombreCampoGenerico9"
			 ,'0001-01-01' as "ValorCampoGenerico9"
			 ,'' as "NombreCampoGenerico10"
			 ,'0001-01-01' as "ValorCampoGenerico10"
			 FROM "BSCL_VIRTUAL"."GES_TBCPDTPC" AS TBCPDTPC
			 INNER JOIN  :v_operaciones AS OPERACIONES_TEMP
			 ON  OPERACIONES_TEMP."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 AND OPERACIONES_TEMP."COD_PER" = TBCPDTPC."COD_PER"
			 AND OPERACIONES_TEMP."SEC_DETALLE" = TBCPDTPC."SEC_DETALLE"
			 LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" AS OPERACION
			 ON OPERACION."SEC_COMPROM" = OPERACIONES_TEMP."SEC_COMPROM"
			 AND OPERACION."COD_PER" = OPERACIONES_TEMP."COD_PER"
			 LEFT JOIN "BSCL.Homologacion.BD::HOM.Reserva" as RESERVA
			 ON RESERVA."COD_PER" = TBCPDTPC."COD_PER"
			 AND RESERVA."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 WHERE TBCPDTPC."RUT_CLI"  = CASE WHEN p_RutCliente = '        0-0'  THEN  TBCPDTPC."RUT_CLI" ELSE p_RutCliente END
			 AND OPERACION."FechaOperacion" BETWEEN p_FechaInicial and p_FechaFinal
			 AND RESERVA."SEC_COMPROM" IS NULL
			 AND OPERACION."SEC_COMPROM" IS NULL;
			 
			 
			 --- el instrumento debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
			 ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
			 ,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5"
			 ,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8"
			 ,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
			 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
			 ,p_IdCarga as "IdCarga"
			 ,'MESARF_RESERVA' as "Entidad"
			 ,'El INSTRUMENTO en el detalle no existe en optimus' as "Mensaje"
			 , 'COD_PER' as "NombreCampoGenerico1"
			 , TBCPDTPC."COD_PER"  as "ValorCampoGenerico1"
			 , 'NEMO' as "NombreCampoGenerico2"
			 , TBCPDTPC."NEMO" as "ValorCampoGenerico2"
			 , '' as "NombreCampoGenerico3"
			 , '' as "ValorCampoGenerico3"
			 , '' as "NombreCampoGenerico4"
			 , '' as "ValorCampoGenerico4"
			 , '' as "NombreCampoGenerico5"
			 , '' as "ValorCampoGenerico5"
			 ,'SEC_COMPROM' as "NombreCampoGenerico6"
			 , TBCPDTPC."SEC_COMPROM" as "ValorCampoGenerico6"
			 ,'' as "NombreCampoGenerico7"
			 , 0 as "ValorCampoGenerico7"
			 ,'' as "NombreCampoGenerico8"
			 , 0 as "ValorCampoGenerico8"
			 ,'' as "NombreCampoGenerico9"
			 ,'0001-01-01' as "ValorCampoGenerico9"
			 ,'' as "NombreCampoGenerico10"
			 ,'0001-01-01' as "ValorCampoGenerico10"
			 FROM "BSCL_VIRTUAL"."GES_TBCPDTPC" AS TBCPDTPC
			 INNER JOIN  :v_operaciones AS OPERACIONES_TEMP
			 ON  OPERACIONES_TEMP."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 AND OPERACIONES_TEMP."COD_PER" = TBCPDTPC."COD_PER"
			 AND OPERACIONES_TEMP."SEC_DETALLE" = TBCPDTPC."SEC_DETALLE"
			 INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" AS OPERACION
			 ON OPERACION."SEC_COMPROM" = OPERACIONES_TEMP."SEC_COMPROM"
			 AND OPERACION."COD_PER" = OPERACIONES_TEMP."COD_PER"
			 LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
			 ON INSTRUMENTO."Nemotecnico" = TRIM(TBCPDTPC."NEMO")
			 LEFT JOIN "BSCL.Homologacion.BD::HOM.Reserva" as RESERVA
			 ON RESERVA."COD_PER" = TBCPDTPC."COD_PER"
			 AND RESERVA."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 WHERE TBCPDTPC."RUT_CLI"  = CASE WHEN p_RutCliente = '        0-0'  THEN  TBCPDTPC."RUT_CLI" ELSE p_RutCliente END
			 AND OPERACION."FechaOperacion" BETWEEN p_FechaInicial and p_FechaFinal
			 AND RESERVA."SEC_COMPROM" IS NULL
			 AND INSTRUMENTO."Nemotecnico" IS NULL;
			 
			 
			 --- el tipo instrumento debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
			 ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
			 ,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5"
			 ,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8"
			 ,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
			 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
			 ,p_IdCarga as "IdCarga"
			 ,'MESARF_RESERVA' as "Entidad"
			 ,'El TIPO INSTRUMENTO asociado al NEMO no existe en optimus' as "Mensaje"
			 , 'COD_PER' as "NombreCampoGenerico1"
			 , TBCPDTPC."COD_PER"  as "ValorCampoGenerico1"
			 , 'NEMO' as "NombreCampoGenerico2"
			 , TBCPDTPC."NEMO" as "ValorCampoGenerico2"
			 , '' as "NombreCampoGenerico3"
			 , '' as "ValorCampoGenerico3"
			 , '' as "NombreCampoGenerico4"
			 , '' as "ValorCampoGenerico4"
			 , '' as "NombreCampoGenerico5"
			 , '' as "ValorCampoGenerico5"
			 ,'SEC_COMPROM' as "NombreCampoGenerico6"
			 , TBCPDTPC."SEC_COMPROM" as "ValorCampoGenerico6"
			 ,'' as "NombreCampoGenerico7"
			 , 0 as "ValorCampoGenerico7"
			 ,'' as "NombreCampoGenerico8"
			 , 0 as "ValorCampoGenerico8"
			 ,'' as "NombreCampoGenerico9"
			 ,'0001-01-01' as "ValorCampoGenerico9"
			 ,'' as "NombreCampoGenerico10"
			 ,'0001-01-01' as "ValorCampoGenerico10"
			 FROM "BSCL_VIRTUAL"."GES_TBCPDTPC" AS TBCPDTPC
			 INNER JOIN  :v_operaciones AS OPERACIONES_TEMP
			 ON  OPERACIONES_TEMP."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 AND OPERACIONES_TEMP."COD_PER" = TBCPDTPC."COD_PER"
			 AND OPERACIONES_TEMP."SEC_DETALLE" = TBCPDTPC."SEC_DETALLE"
			 INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" AS OPERACION
			 ON OPERACION."SEC_COMPROM" = OPERACIONES_TEMP."SEC_COMPROM"
			 AND OPERACION."COD_PER" = OPERACIONES_TEMP."COD_PER"
			 INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO
			 ON INSTRUMENTO."Nemotecnico" = (TBCPDTPC."NEMO")
			 INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPO_INSTRUMENTO
			 ON TIPO_INSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
			 LEFT JOIN "BSCL.Homologacion.BD::HOM.Reserva" as RESERVA
			 ON RESERVA."COD_PER" = TBCPDTPC."COD_PER"
			 AND RESERVA."SEC_COMPROM" = TBCPDTPC."SEC_COMPROM"
			 WHERE TBCPDTPC."RUT_CLI"  = CASE WHEN p_RutCliente = '        0-0'  THEN  TBCPDTPC."RUT_CLI" ELSE p_RutCliente END
			 AND OPERACION."FechaOperacion" BETWEEN p_FechaInicial and p_FechaFinal
			 AND RESERVA."SEC_COMPROM" IS NULL
			 AND TIPO_INSTRUMENTO."IdTipoInstrumento" IS NULL;
			 
			 -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
			 p_ResumenDatosProcesados = select 'CARGAMOVIMIENTOFACTURACION' as "CodigoProceso"
			 ,'FAC' as "Modulo"
			 ,v_count_ope as "TotalRegistrosSebra"
			 ,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
			 union all
			 select * from :p_ResumenDatosProcesados;
 ------------------
 -- FIN SP
 -------------------
 	END;
 END;
