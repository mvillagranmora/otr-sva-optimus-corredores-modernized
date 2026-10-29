SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Custodia.BD.Utilitarios::GenerarPlantillasMovimientoCustodia";

CREATE PROCEDURE "BSCL.Custodia.BD.Utilitarios::GenerarPlantillasMovimientoCustodia"(
-- 
--  Proposito: Actualizar los registros de la tabla campos por accion movimiento custodia
--			   IdDentroDeCampoTabla,SqlCampoAUtilizar 
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 24/08/2016
-----------------------------------------------------MODIFICACIONES--------------------------------------------------------------------------
--  Proposito: Generar las plantillas de datos para los distintos tipos de movimientos para la operacion de custodia.
--			   y asi evitar que sea llamado este proceso cada vez qeu es generado un movimiento de custodia
--  Autor: Andres Cespedes - ACC
--  Fecha Creacion: 21-12-2016
---------------------------------------------------------------------------------------------------------------------------------------------
--  Proposito: Se realizan algunas correcciones
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 23/12/2016
---------------------------------------------------------------------------------------------------------------------------------------------
--  Proposito: Si la plantilla tiene problemas, no se generara.
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 20/02/2017
---------------------------------------------------------------------------------------------------------------------------------------------
--  Proposito: Agregar campos para version PERU
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 18/04/2017
---------------------------------------------------------------------------------------------------------------------------------------------
--  Proposito: Se quita el campo 98, "IdConceptoGarantiaAutomatica", por considerarlo innecesario.
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 16/05/2017
---------------------------------------------------------------------------------------------------------------------------------------------
	p_IdUsuario	BIGINT,
	p_EsDemo	NVARCHAR(1) DEFAULT 'N'
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,1,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Fin',('Error: ' || ::SQL_ERROR_CODE ||', ' || ::SQL_ERROR_MESSAGE)
		FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
		RESIGNAL;
	END;
	
	BEGIN
		--VARIABLES DE ENTRADA
		DECLARE v_DefBIGINT BIGINT := -9876;
		DECLARE v_DefDECIMAL DECIMAL(26,8) := -9876;
		DECLARE v_DefVARCHAR NVARCHAR(1) := '!';
		DECLARE v_DefDATE DATE := '0001-01-03';
		DECLARE v_DefTIME TIME := '00:01:05';
		DECLARE v_IdServicioAgrupador	BIGINT :=0;
		--VARIABLES DE CAMPOS BUSQUEDA CUANDO NO SON UTILIZADOS
		DECLARE v_DefBusquedaBIGINT BIGINT := -9877;
		DECLARE v_DefBusquedaDECIMAL DECIMAL(26,8) := -9877;
		DECLARE v_DefBusquedaVARCHAR NVARCHAR(1) := '%';
		DECLARE v_DefBusquedaDATE DATE := '0001-01-04';
		DECLARE v_DefBusquedaTIME TIME := '00:01:06';
		
		DECLARE v_IdPlantilla TABLE ("IdPlantillaMovimientoCustodia" BIGINT);
		
		DECLARE v_COL1  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		--GARANTIAS
		DECLARE v_COL2  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL3  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL4  TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL5  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL6  TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL7  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		--OPERACION
		DECLARE v_COL8  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL9  TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL10 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL11 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL12 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL13 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL14 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL15 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL16 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL17 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL18 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL19 TABLE ("Campo" NVARCHAR(4),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL20 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL21 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL22 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL23 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL24 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL25 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL26 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL27 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL28 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL29 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL30 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL31 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL32 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL33 TABLE ("Campo" NVARCHAR(2),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL34 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL35 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL36 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL37 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL38 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL39 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL40 TABLE ("Campo" NVARCHAR(50),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL41 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL42 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		--SOLIICTADO POR FONDOS MUTUOS
		DECLARE v_COL43 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL44 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL45 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL46 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL47 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		--INSTRUMENTO
		DECLARE v_COL48 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL49 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL50 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL51 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL52 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL53 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL54 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL55 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL56 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL57 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL58 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL59 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL60 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL61 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		
		--CANTIDADES Y MONTOS
		DECLARE v_COL62 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL63 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL64 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL65 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL66 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL67 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL68 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL69 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL70 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL71 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL72 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		--INSTRUMENTO RELACIONADO
		DECLARE v_COL73 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL74 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL75 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL76 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL77 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL78 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL79 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL80 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL81 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL82 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL83 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL84 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL85 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL86 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
		DECLARE v_COL87 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL88 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL89 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL90 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL91 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL92 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL93 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL94 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL95 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL96 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL97 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		---RF Y FORWARD
		DECLARE v_COL99  TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL100 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL101 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL102 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL103 TABLE ("Campo" TIME,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL104 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL105 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL106 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL107 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL108 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL109 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL110 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		--FORWARD
		DECLARE v_COL111 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL112 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL113 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL114 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL115 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL116 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL117 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL118 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL119 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL120 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL121 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL122 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL123 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL124 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL125 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL126 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL127 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL128 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL129 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL130 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL131 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL132 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL133 TABLE ("Campo" NVARCHAR(1),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL134 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL135 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL136 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		
		--NUEVOS
		DECLARE v_COL137 TABLE ("Campo" NVARCHAR(100),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL138 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL139 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL140 TABLE ("Campo" NVARCHAR(40),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL141 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL142 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL143 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		
		--NUEVOS2
		DECLARE v_COL144 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL145 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL146 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL147 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL148 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		
		--NUEVOS3
		DECLARE v_COL149 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL150 TABLE ("Campo" NVARCHAR(6),"IdPlantillaMovimientoCustodia" BIGINT);
		
		--NUEVOS4
		DECLARE v_COL151 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL152 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		
		--NUEVOS5
		DECLARE v_COL153 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL154 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		
		--NUEVOS6
		--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
		DECLARE v_COL155 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL156 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
	
		--NUEVOS7
		--PARA VERSION PERU
		DECLARE v_COL157 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL158 TABLE ("Campo" BIGINT,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL159 TABLE ("Campo" NVARCHAR(3),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL160 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL161 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL162 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL163 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL164 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL165 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL166 TABLE ("Campo" DATE,"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL167 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL168 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		DECLARE v_COL169 TABLE ("Campo" DECIMAL(26,8),"IdPlantillaMovimientoCustodia" BIGINT);
		
		
		--ARREGLOS
		DECLARE v_ARR_ID_PLANTILLA BIGINT ARRAY;
		
		DECLARE v_ARR_COL1 BIGINT ARRAY;
		--GARANTIAS
		DECLARE v_ARR_COL2 BIGINT ARRAY;
		DECLARE v_ARR_COL3 BIGINT ARRAY;
		DECLARE v_ARR_COL4 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL5 BIGINT ARRAY;
		DECLARE v_ARR_COL6 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL7 BIGINT ARRAY;
		--OPERACION
		DECLARE v_ARR_COL8 BIGINT ARRAY;
		DECLARE v_ARR_COL9 BIGINT ARRAY;
		DECLARE v_ARR_COL10 DATE ARRAY;
		DECLARE v_ARR_COL11 DATE ARRAY;
		DECLARE v_ARR_COL12 BIGINT ARRAY;
		DECLARE v_ARR_COL13 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL14 BIGINT ARRAY;
		DECLARE v_ARR_COL15 BIGINT ARRAY;
		DECLARE v_ARR_COL16 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL17 BIGINT ARRAY;
		DECLARE v_ARR_COL18 BIGINT ARRAY;
		DECLARE v_ARR_COL19 NVARCHAR(4) ARRAY;
		DECLARE v_ARR_COL20 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL21 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL22 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL23 NVARCHAR(6) ARRAY;
		DECLARE v_ARR_COL24 NVARCHAR(6) ARRAY;
		DECLARE v_ARR_COL25 BIGINT ARRAY;
		DECLARE v_ARR_COL26 BIGINT ARRAY;
		DECLARE v_ARR_COL27 BIGINT ARRAY;
		DECLARE v_ARR_COL28 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL29 BIGINT ARRAY;
		DECLARE v_ARR_COL30 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL31 BIGINT ARRAY;
		DECLARE v_ARR_COL32 BIGINT ARRAY;
		DECLARE v_ARR_COL33 NVARCHAR(2) ARRAY;
		DECLARE v_ARR_COL34 DATE ARRAY;
		DECLARE v_ARR_COL35 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL36 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL37 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL38 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL39 NVARCHAR(6) ARRAY;
		DECLARE v_ARR_COL40 NVARCHAR(50) ARRAY;
		DECLARE v_ARR_COL41 BIGINT ARRAY;
		DECLARE v_ARR_COL42 NVARCHAR(3) ARRAY;
		--SOLIICTADO POR FONDOS MUTUOS
		DECLARE v_ARR_COL43 NVARCHAR(6) ARRAY;
		DECLARE v_ARR_COL44 DATE ARRAY;
		DECLARE v_ARR_COL45 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL46 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL47 DECIMAL(26,8) ARRAY;
		--INSTRUMENTO
		DECLARE v_ARR_COL48 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL49 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL50 BIGINT ARRAY;
		DECLARE v_ARR_COL51 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL52 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL53 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL54 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL55 DATE ARRAY;
		DECLARE v_ARR_COL56 DATE ARRAY;
		DECLARE v_ARR_COL57 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL58 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL59 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL60 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL61 BIGINT ARRAY;
		--CANTIDADES Y MONTOS
		DECLARE v_ARR_COL62 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL63 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL64 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL65 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL66 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL67 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL68 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL69 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL70 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL71 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL72 DECIMAL(26,8) ARRAY;
		--INSTRUMENTO RELACIONADO
		DECLARE v_ARR_COL73 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL74 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL75 BIGINT ARRAY;
		DECLARE v_ARR_COL76 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL77 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL78 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL79 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL80 DATE ARRAY;
		DECLARE v_ARR_COL81 DATE ARRAY;
		DECLARE v_ARR_COL82 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL83 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL84 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL85 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL86 BIGINT ARRAY;
		--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
		DECLARE v_ARR_COL87 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL88 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL89 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL90 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL91 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL92 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL93 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL94 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL95 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL96 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL97 NVARCHAR(1) ARRAY;
		---RF Y FORWARD
		DECLARE v_ARR_COL99 DATE ARRAY;
		DECLARE v_ARR_COL100 BIGINT ARRAY;
		DECLARE v_ARR_COL101 BIGINT ARRAY;
		DECLARE v_ARR_COL102 DATE ARRAY;
		DECLARE v_ARR_COL103 TIME ARRAY;
		DECLARE v_ARR_COL104 BIGINT ARRAY;
		DECLARE v_ARR_COL105 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL106 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL107 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL108 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL109 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL110 DECIMAL(26,8) ARRAY;
		--FORWARD
		DECLARE v_ARR_COL111 BIGINT ARRAY;
		DECLARE v_ARR_COL112 BIGINT ARRAY;
		DECLARE v_ARR_COL113 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL114 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL115 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL116 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL117 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL118 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL119 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL120 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL121 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL122 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL123 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL124 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL125 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL126 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL127 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL128 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL129 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL130 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL131 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL132 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL133 NVARCHAR(1) ARRAY;
		DECLARE v_ARR_COL134 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL135 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL136 DECIMAL(26,8) ARRAY;
		
		--NUEVOS
		DECLARE v_ARR_COL137 NVARCHAR(100) ARRAY;
		DECLARE v_ARR_COL138 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL139 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL140 NVARCHAR(40) ARRAY;
		DECLARE v_ARR_COL141 NVARCHAR(6) ARRAY;
		DECLARE v_ARR_COL142 DATE ARRAY;
		DECLARE v_ARR_COL143 BIGINT ARRAY;
		
		--NUEVOS 2
		DECLARE v_ARR_COL144 NVARCHAR(6) ARRAY;
		DECLARE v_ARR_COL145 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL146 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL147 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL148 DECIMAL(26,8) ARRAY;
		
		--NUEVOS3
		DECLARE v_ARR_COL149 BIGINT ARRAY;
		DECLARE v_ARR_COL150 NVARCHAR(6) ARRAY;
		
		--NUEVOS4
		DECLARE v_ARR_COL151 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL152 NVARCHAR(3) ARRAY;
		
		--NUEVOS5
		DECLARE v_ARR_COL153 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL154 BIGINT ARRAY;
		
		--NUEVOS6
		DECLARE v_ARR_COL155 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL156 DECIMAL(26,8) ARRAY;
		
		--NUEVOS7 PERU
		DECLARE v_ARR_COL157 BIGINT ARRAY;
		DECLARE v_ARR_COL158 BIGINT ARRAY;
		DECLARE v_ARR_COL159 NVARCHAR(3) ARRAY;
		DECLARE v_ARR_COL160 DATE ARRAY;
		DECLARE v_ARR_COL161 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL162 DATE ARRAY;
		DECLARE v_ARR_COL163 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL164 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL165 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL166 DATE ARRAY;
		DECLARE v_ARR_COL167 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL168 DECIMAL(26,8) ARRAY;
		DECLARE v_ARR_COL169 DECIMAL(26,8) ARRAY;
		
		DECLARE v_DatosCampos TABLE (
			"IdPlantillaMovimientoCustodia" BIGINT,"IdCamposPorAccionMovimientoCustodia" BIGINT,"IdCampoDentroDeTabla" BIGINT,"CampoAUtilizar" NVARCHAR(255),"UtilizaCampo" NVARCHAR(1));
		
		DECLARE v_DatosCamposSinFiltro TABLE (
			"IdPlantillaMovimientoCustodia" BIGINT,"IdCamposPorAccionMovimientoCustodia" BIGINT,"IdCampoDentroDeTabla" BIGINT,"CampoAUtilizar" NVARCHAR(255),"UtilizaCampo" NVARCHAR(1),"Posicion" BIGINT,
			"MayorSimilitud" INTEGER,
			----------------------------------------------------------------
			"IdTipoProductoLlave" NVARCHAR(4),"IdTipoOperacionLlave" NVARCHAR(6),"IdClasificacionTipoInstrumentoLlave" NVARCHAR(3),"IdTipoInstrumentoLlave" NVARCHAR(3),"IdInstrumentoLlave" BIGINT,
			"IdOrigenInstrumentoLlave" NVARCHAR(3),"EsGarantiaLlave" NVARCHAR(1),
			-----------------------------------------------------------------
			"IdTipoProducto" NVARCHAR(4),"IdTipoOperacion" NVARCHAR(6),"IdClasificacionTipoInstrumento" NVARCHAR(3),"IdTipoInstrumento" NVARCHAR(3),"IdInstrumento" BIGINT,"IdOrigenInstrumento" NVARCHAR(3),
			"EsGarantia" NVARCHAR(1),
			-----------------------------------------------------------------
			"SUMIdTipoProductoLlave" INTEGER,"SUMIdTipoOperacionLlave" INTEGER,"SUMIdClasificacionTipoInstrumentoLlave" INTEGER,"SUMIdTipoInstrumentoLlave" INTEGER,"SUMIdInstrumentoLlave" INTEGER,
			"SUMIdOrigenInstrumentoLlave" INTEGER,"SUMEsGarantiaLlave" INTEGER);
		
		DECLARE v_PlantillasConProblemas TABLE (
			"IdPlantillaMovimientoCustodia" BIGINT,"IdCamposPorAccionMovimientoCustodia" BIGINT,"IdCampoDentroDeTabla" BIGINT,"CampoAUtilizar" NVARCHAR(255),"MayorSimilitud" INTEGER);
			
		DECLARE v_CamposPorAccion TABLE(
			"IdCamposPorAccionMovimientoCustodia" BIGINT,"IdCampoDentroDeTabla" BIGINT,"IdTipoProducto" NVARCHAR(4),"IdTipoOperacion" NVARCHAR(6),"IdClasificacionTipoInstrumento" NVARCHAR(3),
			"IdTipoInstrumento" NVARCHAR(3),"IdInstrumento" BIGINT,"IdOrigenInstrumento" NVARCHAR(3),"EsGarantia" NVARCHAR(1),"EsUtilizadoCertificacion" NVARCHAR(1),"EsUtilizadoIngreso" NVARCHAR(1),
			"EsUtilizadoRetiro" NVARCHAR(1),"EsUtilizadoModificacion" NVARCHAR(1),"EsUtilizadoAnulacion" NVARCHAR(1),"EsUtilizadoAnticipo" NVARCHAR(1),"EsUtilizadoVencimiento" NVARCHAR(1),
			"CampoAUtilizar" NVARCHAR(255),"UtilizaValorInstrumento" NVARCHAR(1),"ValorDefecto" NVARCHAR(20),"EsCampoBusqueda" NVARCHAR(1),"EsComodinBusqueda" NVARCHAR(1),
			"EsInstrumentoRelacionado" INT,"TipoDatoCampoAUtilizar" NVARCHAR(15),"DefaultInst" NVARCHAR(10),"UtilizaValorBD" NVARCHAR(1));
		
		DECLARE v_tblAcciones TABLE ("IdAccionOperacionLlave" NVARCHAR(3)) DEFAULT SELECT 'CER' AS "IdAccionOperacionLlave" FROM DUMMY UNION ALL
																	  		  	   SELECT 'ING' AS "IdAccionOperacionLlave" FROM DUMMY UNION ALL
																	  		  	   SELECT 'RET' AS "IdAccionOperacionLlave" FROM DUMMY UNION ALL
																	  		  	   SELECT 'MOD' AS "IdAccionOperacionLlave" FROM DUMMY UNION ALL
																	  		  	   SELECT 'ANT' AS "IdAccionOperacionLlave" FROM DUMMY UNION ALL
																	  		  	   SELECT 'VEN' AS "IdAccionOperacionLlave" FROM DUMMY UNION ALL
																	  		  	   SELECT 'ANU' AS "IdAccionOperacionLlave" FROM DUMMY;
		
		DECLARE v_Plantilla "BSCL.Custodia.BD::TYP.PlantillaMovimientoCustodia"
			DEFAULT
				SELECT
					row_number() OVER (ORDER BY a."IdAccionOperacionLlave",b."IdTipoProducto",b."IdTipoOperacion",b."IdClasificacionTipoInstrumento",b."IdTipoInstrumento",
												b."IdInstrumento",b."IdOrigenInstrumento",b."EsGarantia") AS "IdPlantillaMovimientoCustodia",a."IdAccionOperacionLlave",
					CASE WHEN b."IdTipoProducto" = '' THEN NULL ELSE b."IdTipoProducto" END AS "IdTipoProductoLlave",
					CASE WHEN b."IdTipoOperacion" = '' THEN NULL ELSE b."IdTipoOperacion" END AS "IdTipoOperacionLlave",
					CASE WHEN b."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE b."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumentoLlave",
					CASE WHEN b."IdTipoInstrumento" = '' THEN NULL ELSE b."IdTipoInstrumento" END AS "IdTipoInstrumentoLlave",
					CASE WHEN b."IdInstrumento" = 0 THEN NULL ELSE b."IdInstrumento" END AS "IdInstrumentoLlave",
					CASE WHEN b."IdOrigenInstrumento" = '' THEN NULL ELSE b."IdOrigenInstrumento" END AS "IdOrigenInstrumentoLlave",
					CASE WHEN b."EsGarantia" = '' THEN NULL ELSE b."EsGarantia" END AS "EsGarantiaLlave",
					-------
					v_DefVARCHAR AS "TablaAfectadaBusqueda",v_DefBIGINT AS "IdCustodiaBusqueda",v_DefBIGINT AS "FolioComprobanteBusqueda",v_DefBIGINT AS "FolioCarteraGarantizada",
					v_DefBIGINT AS "IdInstrumentoCarteraGarantizada",v_DefVARCHAR AS "EsEspecifica",v_DefBIGINT AS "IdContraparteGarantia",v_DefVARCHAR AS "IdOrigenGarantia",v_DefBIGINT AS "IdEspecieGarantia",
					v_DefBIGINT AS "IdEmpresa",v_DefBIGINT AS "IdPais",v_DefDATE AS "FechaOperacion",v_DefDATE AS "FechaVencimientoOperacion",v_DefBIGINT AS "PlazoEnDiasOperacion",
					v_DefVARCHAR AS "EsNominalIngreso",v_DefBIGINT AS "IdClienteIngreso",v_DefBIGINT AS "IdPortafolioIngreso",v_DefBIGINT AS "IdCuentaCustodioPortafolioIngreso",
					v_DefVARCHAR AS "EsNominalRetiro",v_DefBIGINT AS "IdClienteRetiro",v_DefBIGINT AS "IdPortafolioRetiro",v_DefBIGINT AS "IdCuentaCustodioPortafolioRetiro",
					v_DefVARCHAR AS "IdTipoProducto",v_DefVARCHAR AS "IdTipoCanalIngreso",v_DefVARCHAR AS "IdMonedaSistema",v_DefVARCHAR AS "IdMonedaAdicional",v_DefDATE AS "FechaFactorMonedaAdicional",
					v_DefDECIMAL AS "ValorFactorMonedaAdicional",v_DefVARCHAR AS "CodigoIFRS",v_DefVARCHAR AS "IdConcepto",v_DefVARCHAR AS "IdTipoOperacion",v_DefBIGINT AS "IdGrupoOperacion",
					v_DefBIGINT AS "IdOperacion",v_DefBIGINT AS "FolioComprobante",v_DefVARCHAR AS "FolioExternoReferencia",v_DefBIGINT AS "IdMesa",v_DefVARCHAR AS "IdBolsa",v_DefBIGINT AS "IdSucursal",
					v_DefBIGINT AS "IdAgente",v_DefVARCHAR AS "IdCondicionLiquidacion",v_DefDATE AS "FechaLiquidacion",v_DefVARCHAR AS "EsIntraday",v_DefVARCHAR AS "UsaGarantiaEspecifica",
					v_DefVARCHAR AS "EsVentaCorta",v_DefVARCHAR AS "CobraPrimaAlInicio",v_DefVARCHAR AS "IdTipoPrestamo",v_DefVARCHAR AS "Glosa",v_DefBIGINT AS "IdUsuario",v_DefVARCHAR AS "IdModulo",
					v_DefVARCHAR AS "TipoCarteraNormativa",v_DefDATE AS "FechaCurse",v_DefVARCHAR AS "TipoFactorAjuste",v_DefDECIMAL AS "ValorFactorAjusteaFechaCurse",v_DefDECIMAL AS "CostoEnFactorAjuste",
					v_DefVARCHAR AS "IdClasificacionTipoInstrumento",v_DefVARCHAR AS "IdTipoInstrumento",v_DefBIGINT AS "IdInstrumento",v_DefVARCHAR AS "Nemotecnico",v_DefVARCHAR AS "IdEmisor",
					v_DefVARCHAR AS "NemoSVS",v_DefVARCHAR AS "IdTipoReajuste",v_DefVARCHAR AS "EsPagaderoEn",v_DefDATE AS "FechaEmisionInstrumento",v_DefDATE AS "FechaVencimientoInstrumento",
					v_DefVARCHAR AS "EsFungible",v_DefVARCHAR AS "IdBovedaEsIncluyente",v_DefVARCHAR AS "IdBoveda",v_DefVARCHAR AS "IdMonedaInstrumento",v_DefBIGINT AS "Tamano",
					v_DefDATE AS "FechaFactorMonedaInstrumento",v_DefDECIMAL AS "ValorFactorMonedaInstrumento",v_DefDECIMAL AS "Cantidad",v_DefDECIMAL AS "CorteMinimo",v_DefDECIMAL AS "ValorRescate",
					v_DefDECIMAL AS "PrecioCompra",v_DefDECIMAL AS "TasaCompra",v_DefDECIMAL AS "MontoCompra",v_DefDECIMAL AS "MontoCompraMonedaInstrumento",v_DefDECIMAL AS "MontoCompraMonedaAdicional",
					v_DefDECIMAL AS "TasaAgente",v_DefDECIMAL AS "PrecioAPlazo",v_DefDECIMAL AS "MontoAPlazo",v_DefDECIMAL AS "MontoAPlazoMonedaInstrumento",v_DefDECIMAL AS "MontoAPlazoMonedaAdicional",
					v_DefDECIMAL AS "INGCantidad",v_DefDECIMAL AS "INGValorRescate",v_DefVARCHAR AS "IdClasificacionTipoInstrumentoRelacionado",v_DefVARCHAR AS "IdTipoInstrumentoRelacionado",
					v_DefBIGINT AS "IdInstrumentoRelacionado",v_DefVARCHAR AS "NemotecnicoRelacionado",v_DefVARCHAR AS "IdEmisorRelacionado",v_DefVARCHAR AS "NemoSVSRelacionado",
					v_DefVARCHAR AS "IdTipoReajusteRelacionado",v_DefVARCHAR AS "EsPagaderoEnRelacionado",v_DefDATE AS "FechaEmisionInstrumentoRelacionado",v_DefDATE AS "FechaVencimientoInstrumentoRelacionado",
					v_DefVARCHAR AS "EsFungibleRelacionado",v_DefVARCHAR AS "IdBovedaEsIncluyenteRelacionado",v_DefVARCHAR AS "IdBovedaRelacionado",v_DefVARCHAR AS "IdMonedaInstrumentoRelacionado",
					v_DefBIGINT AS "TamanoRelacionado",v_DefDATE AS "FechaFactorMonedaInstrumentoRelacionado",v_DefDECIMAL AS "ValorFactorMonedaInstrumentoRelacionado",
					v_DefDECIMAL AS "CantidadInstrumentoRelacionado",v_DefDECIMAL AS "CorteMinimoInstrumentoRelacionado",v_DefDECIMAL AS "ValorRescateInstrumentoRelacionado",
					v_DefDECIMAL AS "PrecioCompraInstrumentoRelacionado",v_DefDECIMAL AS "TasaCompraInstrumentoRelacionado",v_DefDECIMAL AS "MontoCompraInstrumentoRelacionado",
					v_DefDECIMAL AS "MontoCompraMonedaInstrumentoRelacionado",v_DefDECIMAL AS "MontoCompraMonedaAdicionalRelacionado",v_DefDECIMAL AS "PrecioAPlazoInstrumentoRelacionado",
					v_DefDECIMAL AS "MontoAPlazoInstrumentoRelacionado",v_DefDECIMAL AS "MontoAPlazoMonedaInstrumentoRelacionado",v_DefDECIMAL AS "MontoAPlazoMonedaAdicionalRelacionado",
					v_DefVARCHAR AS "MueveGarantiaAutomatica",v_DefDATE AS "FechaEfectiva",v_DefBIGINT AS "IdPlaza",v_DefBIGINT AS "IdFixing",v_DefDATE AS "FechaFixing",v_DefTIME AS "HoraFixing",
					v_DefBIGINT AS "IdReferenciaMercado",v_DefVARCHAR AS "IdMonedaPrincipal",v_DefVARCHAR AS "IdMonedaSecundaria",v_DefDECIMAL AS "ValorTipoCambioCierre",v_DefDECIMAL AS "ValorParidadCierre",
					v_DefDECIMAL AS "ValorTipoCambioCosto",v_DefDECIMAL AS "ValorParidadCosto",v_DefBIGINT AS "IdSpotAsociado",v_DefBIGINT AS "IdModalidadCumplimiento",
					v_DefDECIMAL AS "MontoMonedaPrincipal",v_DefDECIMAL AS "MontoMonedaSecundaria",v_DefVARCHAR AS "IdMonedaLiquidacion",v_DefDECIMAL AS "MontoLiquidacion",
					v_DefDECIMAL AS "MontoLiquidacionMonedaPrincipal",v_DefDECIMAL AS "MontoDiferido",v_DefDECIMAL AS "MontoUtilidadDiferida",v_DefDECIMAL AS "MontoPerdidaDiferida",
					v_DefDECIMAL AS "MontoMargen",v_DefDECIMAL AS "MontoValorRazonableActivo",v_DefDECIMAL AS "MontoValorRazonablePasivo",v_DefDECIMAL AS "MontoValorRazonableNeto",
					v_DefDECIMAL AS "TasaValorRazonableActivo",v_DefDECIMAL AS "TasaValorRazonablePasivo",v_DefDECIMAL AS "MontoDevengoTipoCambio",v_DefDECIMAL AS "MontoDevengoTipoCambioAnterior",
					v_DefDECIMAL AS "MontoDiferidoDiario",v_DefDECIMAL AS "MontoDiferidoAcumulado",v_DefDECIMAL AS "ValorTipoCambioSpot",v_DefDECIMAL AS "ValorTipoCambioCotizador",v_DefVARCHAR AS "EsPuntos",
					v_DefDECIMAL AS "TasaPuntosMonedaPrincipal",v_DefDECIMAL AS "TasaPuntosMonedaSecundaria",v_DefDECIMAL AS "MontoMatriz",v_DefVARCHAR AS "RazonSocialContraparteGarantia",
					v_DefVARCHAR AS "CuentaContraparteGarantia",v_DefVARCHAR AS "CuentaBoveda",v_DefVARCHAR AS "CuentaBovedaRelacionado",v_DefVARCHAR AS "IdTipoComprobante",v_DefDATE AS "FechaComprobante",
					v_DefBIGINT AS "FolioOperacionRelacionada",v_DefVARCHAR AS "IdTipoComprobanteBusqueda",v_DefDECIMAL AS "TasaDescuentoAnticipo",v_DefDECIMAL AS "TipoCambioAnticipo",
					v_DefDECIMAL AS "MontoARecibirAnticipo",v_DefDECIMAL AS "TipoCambioObservado",v_DefBIGINT AS "FolioCustodiaBusqueda",v_DefVARCHAR AS "TipoCarteraCorredor" 						
				FROM (SELECT
					  DISTINCT "IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento", 
							   CASE WHEN "EsGarantia" <> 'S' THEN 'N' ELSE "EsGarantia" END AS "EsGarantia" 
					  FROM "BSCL.Custodia.BD::CUS.CamposPorAccionMovimientoCustodia"
					  WHERE "IdEstado" = 'ACT') b, :v_tblAcciones a;
		
		DECLARE v_Diccionario TABLE (
			"IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
		
		DECLARE v_TablaMensajes TABLE (
			"IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
			
		SELECT COALESCE(MAX("IdServicioAgrupador"),0) + 1 INTO v_IdServicioAgrupador FROM "BSCL.Plataforma.BD::PLA.Depuracion" WHERE "ServicioAgrupador" = ::CURRENT_OBJECT_NAME;
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Inicio','Inicio Utilitario'
		FROM DUMMY;
		
		v_CamposPorAccion =
		SELECT
			"IdCamposPorAccionMovimientoCustodia","IdCampoDentroDeTabla","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento",
			"IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento","EsGarantia","EsUtilizadoCertificacion",
			"EsUtilizadoIngreso","EsUtilizadoRetiro","EsUtilizadoModificacion","EsUtilizadoAnulacion","EsUtilizadoAnticipo",
			"EsUtilizadoVencimiento","CampoAUtilizar","UtilizaValorInstrumento","ValorDefecto","EsCampoBusqueda","EsComodinBusqueda",
			"EsInstrumentoRelacionado","TipoDatoCampoAUtilizar","DefaultInst","UtilizaValorBD"
		FROM 
		(SELECT
			CASE WHEN 
					MAX("IdCamposPorAccionMovimientoCustodia") over (PARTITION BY "IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento",
																				  "IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento",
																				  "EsGarantia","CampoAUtilizar")
     							= "IdCamposPorAccionMovimientoCustodia" THEN 'S' ELSE 'N' END AS "EsCampoMaximo",
			"IdCamposPorAccionMovimientoCustodia","IdCampoDentroDeTabla",
			CASE WHEN "IdTipoProducto" = '' THEN NULL ELSE "IdTipoProducto" END AS "IdTipoProducto",
			CASE WHEN "IdTipoOperacion" = '' THEN NULL ELSE "IdTipoOperacion" END AS "IdTipoOperacion",
			CASE WHEN "IdClasificacionTipoInstrumento" = '' THEN NULL ELSE "IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento",
			CASE WHEN "IdTipoInstrumento" = '' THEN NULL ELSE "IdTipoInstrumento" END AS "IdTipoInstrumento",
			CASE WHEN "IdInstrumento" = 0 THEN NULL ELSE "IdInstrumento" END AS "IdInstrumento",
			CASE WHEN "IdOrigenInstrumento" = '' THEN NULL ELSE "IdOrigenInstrumento" END AS "IdOrigenInstrumento",
			CASE WHEN "EsGarantia" = '' THEN NULL ELSE "EsGarantia" END AS "EsGarantia",
			"EsUtilizadoCertificacion","EsUtilizadoIngreso","EsUtilizadoRetiro","EsUtilizadoModificacion","EsUtilizadoAnulacion",
			"EsUtilizadoAnticipo","EsUtilizadoVencimiento","CampoAUtilizar","UtilizaValorInstrumento","ValorDefecto","EsCampoBusqueda",
			"EsComodinBusqueda",OCCURRENCES_REGEXPR('Relacionado' IN "CampoAUtilizar") AS "EsInstrumentoRelacionado","TipoDatoCampoAUtilizar",
			CASE WHEN "TipoDatoCampoAUtilizar" = 'DATE' THEN '0001-01-02' ELSE '0' END AS "DefaultInst","UtilizaValorBD"
		FROM "BSCL.Custodia.BD::CUS.CamposPorAccionMovimientoCustodia"
		WHERE "IdEstado" = 'ACT')
		WHERE "EsCampoMaximo" = 'S';
		
		v_DatosCamposSinFiltro = 
		SELECT
			"IdPlantillaMovimientoCustodia","IdCamposPorAccionMovimientoCustodia","IdCampoDentroDeTabla","CampoAUtilizar",
			CASE
				WHEN "IdAccionOperacionLlave" = 'CER' AND "EsUtilizadoCertificacion" = 'S' THEN 'S'
				WHEN "IdAccionOperacionLlave" = 'ING' AND "EsUtilizadoIngreso" = 'S' THEN 'S'
				WHEN "IdAccionOperacionLlave" = 'RET' AND "EsUtilizadoRetiro" = 'S' THEN 'S'
				WHEN "IdAccionOperacionLlave" = 'MOD' AND "EsUtilizadoModificacion" = 'S' THEN 'S'
				WHEN "IdAccionOperacionLlave" = 'ANT' AND "EsUtilizadoAnticipo" = 'S' THEN 'S'
				WHEN "IdAccionOperacionLlave" = 'VEN' AND "EsUtilizadoVencimiento" = 'S' THEN 'S'
				WHEN "IdAccionOperacionLlave" = 'ANU' AND "EsUtilizadoAnulacion" = 'S' THEN 'S'
				ELSE 'N'
			END AS "UtilizaCampo","Posicion","MayorSimilitud",
			----------------------------------------------------------------
			"IdTipoProductoLlave","IdTipoOperacionLlave","IdClasificacionTipoInstrumentoLlave","IdTipoInstrumentoLlave",
			"IdInstrumentoLlave","IdOrigenInstrumentoLlave","EsGarantiaLlave",
			-----------------------------------------------------------------
			"IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento",
			"IdInstrumento","IdOrigenInstrumento","EsGarantia",
			-----------------------------------------------------------------
			"SUMIdTipoProductoLlave","SUMIdTipoOperacionLlave","SUMIdClasificacionTipoInstrumentoLlave",
			"SUMIdTipoInstrumentoLlave","SUMIdInstrumentoLlave","SUMIdOrigenInstrumentoLlave","SUMEsGarantiaLlave"
		FROM
		(SELECT
			ROW_NUMBER() over (PARTITION BY "IdPlantillaMovimientoCustodia","IdCampoDentroDeTabla" ORDER BY "MayorSimilitud" DESC) AS "Posicion",
			"IdCampoDentroDeTabla","IdCamposPorAccionMovimientoCustodia","IdPlantillaMovimientoCustodia","IdAccionOperacionLlave","EsUtilizadoCertificacion",
			"EsUtilizadoIngreso","EsUtilizadoRetiro","EsUtilizadoModificacion","EsUtilizadoAnticipo","EsUtilizadoVencimiento",
			"EsUtilizadoAnulacion","CampoAUtilizar",
			"MayorSimilitud","IdTipoProductoLlave","IdTipoOperacionLlave","IdClasificacionTipoInstrumentoLlave","IdTipoInstrumentoLlave",
			"IdInstrumentoLlave","IdOrigenInstrumentoLlave","EsGarantiaLlave","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento",
			"IdTipoInstrumento","IdInstrumento","IdOrigenInstrumento","EsGarantia",
			"SUMIdTipoProductoLlave","SUMIdTipoOperacionLlave","SUMIdClasificacionTipoInstrumentoLlave","SUMIdTipoInstrumentoLlave",
			"SUMIdInstrumentoLlave","SUMIdOrigenInstrumentoLlave","SUMEsGarantiaLlave"
			FROM
			(SELECT
				(
				CASE WHEN COALESCE(d."IdTipoProductoLlave",'') = COALESCE(c."IdTipoProducto",'') THEN 2
					 WHEN COALESCE(d."IdTipoProductoLlave",'') = COALESCE(c."IdTipoProducto",d."IdTipoProductoLlave",'') THEN 1
					 ELSE 0
				END +
				CASE WHEN COALESCE(d."IdTipoOperacionLlave",'') = COALESCE(c."IdTipoOperacion",'') THEN 2
					 WHEN COALESCE(d."IdTipoOperacionLlave",'') = COALESCE(c."IdTipoOperacion",d."IdTipoOperacionLlave",'') THEN 1
					 ELSE 0
				END +
				CASE WHEN COALESCE(d."IdClasificacionTipoInstrumentoLlave",'') = COALESCE(c."IdClasificacionTipoInstrumento",'') THEN 2
					 WHEN COALESCE(d."IdClasificacionTipoInstrumentoLlave",'') = COALESCE(c."IdClasificacionTipoInstrumento",d."IdClasificacionTipoInstrumentoLlave",'') THEN 1
					 ELSE 0
				END +
				CASE WHEN COALESCE(d."IdTipoInstrumentoLlave",'') = COALESCE(c."IdTipoInstrumento",'') THEN 2
					 WHEN COALESCE(d."IdTipoInstrumentoLlave",'') = COALESCE(c."IdTipoInstrumento",d."IdTipoInstrumentoLlave",'') THEN 1
					 ELSE 0
				END +
				CASE WHEN COALESCE(d."IdInstrumentoLlave",0) = COALESCE(c."IdInstrumento",0) THEN 2
					 WHEN COALESCE(d."IdInstrumentoLlave",0) = COALESCE(c."IdInstrumento",d."IdInstrumentoLlave",0) THEN 1
					 ELSE 0
				END +
				CASE WHEN COALESCE(d."IdOrigenInstrumentoLlave",'') = COALESCE(c."IdOrigenInstrumento",'') THEN 2
					 WHEN COALESCE(d."IdOrigenInstrumentoLlave",'') = COALESCE(c."IdOrigenInstrumento",d."IdOrigenInstrumentoLlave",'') THEN 1
					 ELSE 0
				END +
				CASE WHEN COALESCE(d."EsGarantiaLlave",'') = COALESCE(c."EsGarantia",'') THEN 2
					 WHEN COALESCE(d."EsGarantiaLlave",'') = COALESCE(c."EsGarantia",d."EsGarantiaLlave",'') THEN 1
					 ELSE 0
				END) AS "MayorSimilitud",
				CASE WHEN COALESCE(d."IdTipoProductoLlave",'') = COALESCE(c."IdTipoProducto",'') THEN 2
					 WHEN COALESCE(d."IdTipoProductoLlave",'') = COALESCE(c."IdTipoProducto",d."IdTipoProductoLlave",'') THEN 1
					 ELSE 0
				END AS "SUMIdTipoProductoLlave",
				CASE WHEN COALESCE(d."IdTipoOperacionLlave",'') = COALESCE(c."IdTipoOperacion",'') THEN 2
					 WHEN COALESCE(d."IdTipoOperacionLlave",'') = COALESCE(c."IdTipoOperacion",d."IdTipoOperacionLlave",'') THEN 1
					 ELSE 0
				END AS "SUMIdTipoOperacionLlave",
				CASE WHEN COALESCE(d."IdClasificacionTipoInstrumentoLlave",'') = COALESCE(c."IdClasificacionTipoInstrumento",'') THEN 2
					 WHEN COALESCE(d."IdClasificacionTipoInstrumentoLlave",'') = COALESCE(c."IdClasificacionTipoInstrumento",d."IdClasificacionTipoInstrumentoLlave",'') THEN 1
					 ELSE 0
				END AS "SUMIdClasificacionTipoInstrumentoLlave",
				CASE WHEN COALESCE(d."IdTipoInstrumentoLlave",'') = COALESCE(c."IdTipoInstrumento",'') THEN 2
					 WHEN COALESCE(d."IdTipoInstrumentoLlave",'') = COALESCE(c."IdTipoInstrumento",d."IdTipoInstrumentoLlave",'') THEN 1
					 ELSE 0
				END AS "SUMIdTipoInstrumentoLlave",
				CASE WHEN COALESCE(d."IdInstrumentoLlave",0) = COALESCE(c."IdInstrumento",0) THEN 2
					 WHEN COALESCE(d."IdInstrumentoLlave",0) = COALESCE(c."IdInstrumento",d."IdInstrumentoLlave",0) THEN 1
					 ELSE 0
				END AS "SUMIdInstrumentoLlave",
				CASE WHEN COALESCE(d."IdOrigenInstrumentoLlave",'') = COALESCE(c."IdOrigenInstrumento",'') THEN 2
					 WHEN COALESCE(d."IdOrigenInstrumentoLlave",'') = COALESCE(c."IdOrigenInstrumento",d."IdOrigenInstrumentoLlave",'') THEN 1
					 ELSE 0
				END AS "SUMIdOrigenInstrumentoLlave",
				CASE WHEN COALESCE(d."EsGarantiaLlave",'') = COALESCE(c."EsGarantia",'') THEN 2
					 WHEN COALESCE(d."EsGarantiaLlave",'') = COALESCE(c."EsGarantia",d."EsGarantiaLlave",'') THEN 1
					 ELSE 0
				END AS "SUMEsGarantiaLlave",
				c."IdCampoDentroDeTabla",c."IdCamposPorAccionMovimientoCustodia",d."IdPlantillaMovimientoCustodia",d."IdAccionOperacionLlave",
				c."EsUtilizadoCertificacion",c."EsUtilizadoIngreso",c."EsUtilizadoRetiro",c."EsUtilizadoModificacion",
				c."EsUtilizadoAnticipo",c."EsUtilizadoVencimiento",c."EsUtilizadoAnulacion",c."CampoAUtilizar",
				
				d."IdTipoProductoLlave",d."IdTipoOperacionLlave",d."IdClasificacionTipoInstrumentoLlave",d."IdTipoInstrumentoLlave",
				d."IdInstrumentoLlave",d."IdOrigenInstrumentoLlave",d."EsGarantiaLlave",
				c."IdTipoProducto",c."IdTipoOperacion",c."IdClasificacionTipoInstrumento",c."IdTipoInstrumento",c."IdInstrumento",
				c."IdOrigenInstrumento",c."EsGarantia"
			FROM :v_Plantilla AS d,:v_CamposPorAccion AS c
			)
		);
		
		v_PlantillasConProblemas =
			SELECT
				"IdPlantillaMovimientoCustodia","IdCamposPorAccionMovimientoCustodia","IdCampoDentroDeTabla","CampoAUtilizar","MayorSimilitud"
			FROM(
				SELECT
					COUNT("MaximaSimilitud") over (PARTITION BY "IdPlantillaMovimientoCustodia","IdCampoDentroDeTabla","MaximaSimilitud" ORDER BY "IdPlantillaMovimientoCustodia","IdCampoDentroDeTabla") AS "SimilitudDuplicada",
					"IdPlantillaMovimientoCustodia","IdCampoDentroDeTabla","IdCamposPorAccionMovimientoCustodia","CampoAUtilizar","MayorSimilitud"
				FROM
					(SELECT
						MAX("MayorSimilitud") over (PARTITION BY "IdPlantillaMovimientoCustodia","IdCampoDentroDeTabla" ORDER BY "MayorSimilitud" DESC) AS "MaximaSimilitud",
						"IdPlantillaMovimientoCustodia","IdCamposPorAccionMovimientoCustodia","IdCampoDentroDeTabla","CampoAUtilizar","MayorSimilitud"
					FROM :v_DatosCamposSinFiltro)					
				WHERE "MayorSimilitud" = "MaximaSimilitud")
			WHERE "SimilitudDuplicada" > 1;

		IF (NOT IS_EMPTY(:v_PlantillasConProblemas)) THEN
			v_Diccionario =
				SELECT 1 AS "IdInterno",'ERROR__CREACION_PLANTILLA' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"	FROM DUMMY;
			
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (
				P_TablaCodigosMensajes	=> :v_Diccionario,
				p_out_TablaMensajes		=> v_TablaMensajes);
			
			SELECT * FROM :v_TablaMensajes;
			SELECT 'v_PlantillasConProblemas',* FROM :v_PlantillasConProblemas ORDER BY "IdPlantillaMovimientoCustodia","IdCampoDentroDeTabla","IdCamposPorAccionMovimientoCustodia";
			SELECT 'v_DatosCamposSinFiltro ',* FROM :v_PlantillasConProblemas p
			INNER JOIN :v_DatosCamposSinFiltro d ON (d."IdPlantillaMovimientoCustodia" = p."IdPlantillaMovimientoCustodia"
												 AND d."IdCampoDentroDeTabla" = p."IdCampoDentroDeTabla"
												 AND d."IdCamposPorAccionMovimientoCustodia" = p."IdCamposPorAccionMovimientoCustodia")
			ORDER BY d."IdPlantillaMovimientoCustodia",d."IdCampoDentroDeTabla",d."Posicion";
    	END IF;
    	
		--SE OBTIENE EL QUE TIENE MAYOR SIMILITUD, "IdTipoOperacion","IdClasificacionTipoInstrumento".. ETC.
		v_DatosCampos = 
			SELECT
				"IdPlantillaMovimientoCustodia","IdCamposPorAccionMovimientoCustodia","IdCampoDentroDeTabla","CampoAUtilizar","UtilizaCampo"
			FROM :v_DatosCamposSinFiltro
			WHERE "Posicion" = 1;
		 
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Configuracion','Fin obtención de configuración a utilizar'
		FROM DUMMY;
	
		v_IdPlantilla = SELECT "IdPlantillaMovimientoCustodia" FROM :v_Plantilla;
		
		v_COL1 =  SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FolioComprobanteBusqueda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =1;
		--GARANTIAS
		v_COL2  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FolioCarteraGarantizada" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla"=2;
		v_COL3  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdInstrumentoCarteraGarantizada" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =3;
		v_COL4  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsEspecifica" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =4;
		v_COL5  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdContraparteGarantia" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =5;
		v_COL6  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdOrigenGarantia" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =6;
		v_COL7  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdEspecieGarantia" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =7;
		--OPERACION
		v_COL8  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdEmpresa" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =8;
		v_COL9  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdPais" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =9;
		v_COL10 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaOperacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =10;
		v_COL11 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaVencimientoOperacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =11;
		v_COL12 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."PlazoEnDiasOperacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =12;
		v_COL13 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsNominalIngreso" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =13;
		v_COL14 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdClienteIngreso" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =14;
		v_COL15 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdPortafolioIngreso" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =15;
		v_COL16 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsNominalRetiro" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =16;
		v_COL17 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdClienteRetiro" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =17;
		v_COL18 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdPortafolioRetiro" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =18;
		v_COL19 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoProducto" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(4)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =19;
		v_COL20 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoCanalIngreso" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =20;
		v_COL21 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaSistema" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =21;
		v_COL22 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CodigoIFRS" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =22;
		v_COL23 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdConcepto" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(6)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =23;
		v_COL24 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoOperacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(6)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =24;
		v_COL25 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdGrupoOperacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =25;
		v_COL26 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdOperacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =26;
		v_COL27 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FolioComprobante" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =27;
		v_COL28 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FolioExternoReferencia" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =28;
		v_COL29 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMesa" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =29;
		v_COL30 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdBolsa" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =30;
		v_COL31 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdSucursal" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =31;
		v_COL32 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdAgente" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =32;
		v_COL33 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdCondicionLiquidacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(2)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =33;
		v_COL34 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaLiquidacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =34;
		v_COL35 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsIntraday" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =35;
		v_COL36 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."UsaGarantiaEspecifica" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =36;
		v_COL37 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsVentaCorta" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =37;
		v_COL38 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CobraPrimaAlInicio" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =38;
		v_COL39 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoPrestamo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(6)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =39;
		v_COL40 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."Glosa" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(50)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =40;
		v_COL41 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdUsuario" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =41;
		v_COL42 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdModulo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =42;
		--SOLIICTADO POR FONDOS MUTUOS
		v_COL43 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TipoCarteraNormativa" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(6)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =43;
		v_COL44 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaCurse" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =44;
		v_COL45 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TipoFactorAjuste" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =45;
		v_COL46 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorFactorAjusteaFechaCurse" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =46;
		v_COL47 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CostoEnFactorAjuste" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =47;
		--INSTRUMENTO
		v_COL48 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdClasificacionTipoInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =48;
		v_COL49 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =49;
		v_COL50 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =50;
		v_COL51 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."Nemotecnico" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =51;
		v_COL52 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdEmisor" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =52;
		v_COL53 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."NemoSVS" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =53;
		v_COL54 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoReajuste" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =54;
		v_COL55 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaEmisionInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =55;
		v_COL56 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaVencimientoInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =56;
		v_COL57 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsFungible" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =57;
		v_COL58 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdBovedaEsIncluyente" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =58;
		v_COL59 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdBoveda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =59;
		v_COL60 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =60;
		v_COL61 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."Tamano" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =61;
		--CANTIDADES Y MONTOS
		v_COL62 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."Cantidad" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =62;
		v_COL63 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CorteMinimo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =63;
		v_COL64 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorRescate" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =64;
		v_COL65 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."PrecioCompra" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =65;
		v_COL66 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaCompra" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =66;
		v_COL67 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoCompra" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =67;
		v_COL68 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoCompraMonedaInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =68;
		v_COL69 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaAgente" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =69;
		v_COL70 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."PrecioAPlazo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =70;
		v_COL71 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoAPlazo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =71;
		v_COL72 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoAPlazoMonedaInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =72;
		--INSTRUMENTO RELACIONADO
		v_COL73 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdClasificacionTipoInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =73;
		v_COL74 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =74;
		v_COL75 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =75;
		v_COL76 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."NemotecnicoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =76;
		v_COL77 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdEmisorRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =77;
		v_COL78 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."NemoSVSRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =78;
		v_COL79 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoReajusteRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =79;
		v_COL80 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaEmisionInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =80;
		v_COL81 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaVencimientoInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =81;
		v_COL82 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsFungibleRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =82;
		v_COL83 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdBovedaEsIncluyenteRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =83;
		v_COL84 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdBovedaRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3))  AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =84;
		v_COL85 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =85;
		v_COL86 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TamanoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =86;
		--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
		v_COL87 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CantidadInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =87;
		v_COL88 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CorteMinimoInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =88;
		v_COL89 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorRescateInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =89;
		v_COL90 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."PrecioCompraInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =90;
		v_COL91 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaCompraInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =91;
		v_COL92 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoCompraInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =92;
		v_COL93 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoCompraMonedaInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =93;
		v_COL94 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."PrecioAPlazoInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =94;
		v_COL95 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoAPlazoInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =95;
		v_COL96 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoAPlazoMonedaInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =96;
		v_COL97 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MueveGarantiaAutomatica" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =97;
		--RF Y FORWARD
		v_COL99  = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaEfectiva" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =99;
		v_COL100 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdPlaza" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =100;
		v_COL101 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdFixing" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =101;
		v_COL102 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaFixing" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =102;
		v_COL103 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."HoraFixing" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaTIME ELSE c1."ValorDefecto" END END END AS TIME) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =103;
		v_COL104 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdReferenciaMercado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =104;
		v_COL105 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaPrincipal" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =105;
		v_COL106 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaSecundaria" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =106;
		v_COL107 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorTipoCambioCierre" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =107;
		v_COL108 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorParidadCierre" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =108;
		v_COL109 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorTipoCambioCosto" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =109;
		v_COL110 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorParidadCosto" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =110;
		--FORWARD
		v_COL111 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdSpotAsociado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =111;
		v_COL112 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdModalidadCumplimiento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =112;
		v_COL113 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoMonedaPrincipal" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =113;
		v_COL114 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoMonedaSecundaria" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =114;
		v_COL115 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaLiquidacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =115;
		v_COL116 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoLiquidacion" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =116;
		v_COL117 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoLiquidacionMonedaPrincipal" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =117;
		v_COL118 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoDiferido" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =118;
		v_COL119 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoUtilidadDiferida" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =119;
		v_COL120 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoPerdidaDiferida" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =120;
		v_COL121 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoMargen" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =121;
		v_COL122 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoValorRazonableActivo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =122;
		v_COL123 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoValorRazonablePasivo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =123;
		v_COL124 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoValorRazonableNeto" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =124;
		v_COL125 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaValorRazonableActivo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =125;
		v_COL126 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaValorRazonablePasivo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =126;
		v_COL127 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoDevengoTipoCambio" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =127;
		v_COL128 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoDevengoTipoCambioAnterior" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =128;
		v_COL129 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoDiferidoDiario" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =129;
		v_COL130 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoDiferidoAcumulado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =130;
		v_COL131 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorTipoCambioSpot" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =131;
		v_COL132 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorTipoCambioCotizador" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =132;
		v_COL133 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsPuntos" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(1)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =133;
		v_COL134 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaPuntosMonedaPrincipal" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =134;
		v_COL135 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaPuntosMonedaSecundaria" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =135;
		v_COL136 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoMatriz" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =136;
		
		--NUEVOS
		v_COL137 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."RazonSocialContraparteGarantia" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(100)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =137;
		v_COL138 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CuentaContraparteGarantia" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =138;
		v_COL139 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CuentaBoveda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =139;
		v_COL140 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."CuentaBovedaRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(40)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =140;
		v_COL141 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoComprobante" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(6)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =141;
		v_COL142 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaComprobante" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =142;
		v_COL143 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FolioOperacionRelacionada" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =143;

		--NUEVOS2
		v_COL144 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdTipoComprobanteBusqueda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(6)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =144;
		v_COL145 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TasaDescuentoAnticipo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =145;
		v_COL146 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TipoCambioAnticipo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =146;
		v_COL147 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoARecibirAnticipo" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =147;
		v_COL148 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TipoCambioObservado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =148;
		
		--NUEVOS3
		v_COL149 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FolioCustodiaBusqueda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =149;
		v_COL150 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TipoCarteraCorredor" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =150;
		
		--nuevos4
		v_COL151 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsPagaderoEn" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =151;
		v_COL152 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."EsPagaderoEnRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =152;

		--nuevos5
		v_COL153 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."TablaAfectadaBusqueda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =153;
		v_COL154 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdCustodiaBusqueda" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =154;
		
		--NUEVOS6
		v_COL155 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."INGCantidad" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =155;
		v_COL156 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."INGValorRescate" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =156;
		
		--NUEVOS7, VERSION PERU
		v_COL157 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdCuentaCustodioPortafolioIngreso" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =157;
		v_COL158 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdCuentaCustodioPortafolioRetiro" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaBIGINT ELSE c1."ValorDefecto" END END END AS BIGINT) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =158;
		v_COL159 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."IdMonedaAdicional" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaVARCHAR ELSE c1."ValorDefecto" END END END AS NVARCHAR(3)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =159;
		v_COL160 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaFactorMonedaAdicional" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =160;
		v_COL161 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorFactorMonedaAdicional" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =161;
		v_COL162 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaFactorMonedaInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =162;
		v_COL163 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorFactorMonedaInstrumento" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =163;
		v_COL164 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoCompraMonedaAdicional" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =164;
		v_COL165 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoAPlazoMonedaAdicional" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =165;
		v_COL166 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."FechaFactorMonedaInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDATE ELSE c1."ValorDefecto" END END END AS DATE) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =166;	
		v_COL167 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."ValorFactorMonedaInstrumentoRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =167;
		v_COL168 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoCompraMonedaAdicionalRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =168;
		v_COL169 = SELECT CAST(CASE WHEN dc."UtilizaCampo" = 'S' THEN COALESCE(CASE WHEN c1."UtilizaValorInstrumento" = 'S' THEN c1."DefaultInst" ELSE e."MontoAPlazoMonedaAdicionalRelacionado" END,c1."ValorDefecto") ELSE CASE WHEN c1."UtilizaValorBD" = 'S' THEN NULL ELSE CASE WHEN c1."EsCampoBusqueda" = 'S' THEN :v_DefBusquedaDECIMAL ELSE c1."ValorDefecto" END END END AS DECIMAL(26,8)) AS "Campo",dc."IdPlantillaMovimientoCustodia" FROM :v_CamposPorAccion AS c1 INNER JOIN :v_DatosCampos AS dc ON (dc."IdCamposPorAccionMovimientoCustodia" = c1."IdCamposPorAccionMovimientoCustodia") INNER JOIN :v_Plantilla AS e ON (e."IdPlantillaMovimientoCustodia" = dc."IdPlantillaMovimientoCustodia") WHERE c1."IdCampoDentroDeTabla" =169;
		
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Tablas','Fin de cargas de variables tipo tablas'
		FROM DUMMY;
		
		v_ARR_ID_PLANTILLA:= ARRAY_AGG(:v_IdPlantilla."IdPlantillaMovimientoCustodia" ORDER BY "IdPlantillaMovimientoCustodia");
		
		v_ARR_COL1:= ARRAY_AGG(:v_COL1."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL2:= ARRAY_AGG(:v_COL2."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL3:= ARRAY_AGG(:v_COL3."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL4:= ARRAY_AGG(:v_COL4."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL5:= ARRAY_AGG(:v_COL5."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL6:= ARRAY_AGG(:v_COL6."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL7:= ARRAY_AGG(:v_COL7."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL8:= ARRAY_AGG(:v_COL8."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL9:= ARRAY_AGG(:v_COL9."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL10:= ARRAY_AGG(:v_COL10."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL11:= ARRAY_AGG(:v_COL11."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL12:= ARRAY_AGG(:v_COL12."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL13:= ARRAY_AGG(:v_COL13."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL14:= ARRAY_AGG(:v_COL14."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL15:= ARRAY_AGG(:v_COL15."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL16:= ARRAY_AGG(:v_COL16."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL17:= ARRAY_AGG(:v_COL17."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL18:= ARRAY_AGG(:v_COL18."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL19:= ARRAY_AGG(:v_COL19."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL20:= ARRAY_AGG(:v_COL20."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL21:= ARRAY_AGG(:v_COL21."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL22:= ARRAY_AGG(:v_COL22."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL23:= ARRAY_AGG(:v_COL23."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL24:= ARRAY_AGG(:v_COL24."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL25:= ARRAY_AGG(:v_COL25."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL26:= ARRAY_AGG(:v_COL26."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL27:= ARRAY_AGG(:v_COL27."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL28:= ARRAY_AGG(:v_COL28."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL29:= ARRAY_AGG(:v_COL29."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL30:= ARRAY_AGG(:v_COL30."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL31:= ARRAY_AGG(:v_COL31."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL32:= ARRAY_AGG(:v_COL32."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL33:= ARRAY_AGG(:v_COL33."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL34:= ARRAY_AGG(:v_COL34."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL35:= ARRAY_AGG(:v_COL35."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL36:= ARRAY_AGG(:v_COL36."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL37:= ARRAY_AGG(:v_COL37."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL38:= ARRAY_AGG(:v_COL38."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL39:= ARRAY_AGG(:v_COL39."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL40:= ARRAY_AGG(:v_COL40."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL41:= ARRAY_AGG(:v_COL41."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL42:= ARRAY_AGG(:v_COL42."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL43:= ARRAY_AGG(:v_COL43."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL44:= ARRAY_AGG(:v_COL44."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL45:= ARRAY_AGG(:v_COL45."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL46:= ARRAY_AGG(:v_COL46."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL47:= ARRAY_AGG(:v_COL47."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL48:= ARRAY_AGG(:v_COL48."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL49:= ARRAY_AGG(:v_COL49."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL50:= ARRAY_AGG(:v_COL50."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL51:= ARRAY_AGG(:v_COL51."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL52:= ARRAY_AGG(:v_COL52."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL53:= ARRAY_AGG(:v_COL53."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL54:= ARRAY_AGG(:v_COL54."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL55:= ARRAY_AGG(:v_COL55."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL56:= ARRAY_AGG(:v_COL56."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL57:= ARRAY_AGG(:v_COL57."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL58:= ARRAY_AGG(:v_COL58."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL59:= ARRAY_AGG(:v_COL59."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL60:= ARRAY_AGG(:v_COL60."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL61:= ARRAY_AGG(:v_COL61."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL62:= ARRAY_AGG(:v_COL62."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL63:= ARRAY_AGG(:v_COL63."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL64:= ARRAY_AGG(:v_COL64."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL65:= ARRAY_AGG(:v_COL65."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL66:= ARRAY_AGG(:v_COL66."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL67:= ARRAY_AGG(:v_COL67."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL68:= ARRAY_AGG(:v_COL68."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL69:= ARRAY_AGG(:v_COL69."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL70:= ARRAY_AGG(:v_COL70."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL71:= ARRAY_AGG(:v_COL71."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL72:= ARRAY_AGG(:v_COL72."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL73:= ARRAY_AGG(:v_COL73."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL74:= ARRAY_AGG(:v_COL74."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL75:= ARRAY_AGG(:v_COL75."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL76:= ARRAY_AGG(:v_COL76."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL77:= ARRAY_AGG(:v_COL77."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL78:= ARRAY_AGG(:v_COL78."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL79:= ARRAY_AGG(:v_COL79."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL80:= ARRAY_AGG(:v_COL80."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL81:= ARRAY_AGG(:v_COL81."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL82:= ARRAY_AGG(:v_COL82."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL83:= ARRAY_AGG(:v_COL83."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL84:= ARRAY_AGG(:v_COL84."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL85:= ARRAY_AGG(:v_COL85."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL86:= ARRAY_AGG(:v_COL86."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL87:= ARRAY_AGG(:v_COL87."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL88:= ARRAY_AGG(:v_COL88."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL89:= ARRAY_AGG(:v_COL89."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL90:= ARRAY_AGG(:v_COL90."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL91:= ARRAY_AGG(:v_COL91."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL92:= ARRAY_AGG(:v_COL92."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL93:= ARRAY_AGG(:v_COL93."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL94:= ARRAY_AGG(:v_COL94."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL95:= ARRAY_AGG(:v_COL95."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL96:= ARRAY_AGG(:v_COL96."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL97:= ARRAY_AGG(:v_COL97."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL99:= ARRAY_AGG(:v_COL99."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL100:= ARRAY_AGG(:v_COL100."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL101:= ARRAY_AGG(:v_COL101."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL102:= ARRAY_AGG(:v_COL102."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL103:= ARRAY_AGG(:v_COL103."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL104:= ARRAY_AGG(:v_COL104."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL105:= ARRAY_AGG(:v_COL105."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL106:= ARRAY_AGG(:v_COL106."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL107:= ARRAY_AGG(:v_COL107."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL108:= ARRAY_AGG(:v_COL108."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL109:= ARRAY_AGG(:v_COL109."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL110:= ARRAY_AGG(:v_COL110."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL111:= ARRAY_AGG(:v_COL111."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL112:= ARRAY_AGG(:v_COL112."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL113:= ARRAY_AGG(:v_COL113."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL114:= ARRAY_AGG(:v_COL114."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL115:= ARRAY_AGG(:v_COL115."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL116:= ARRAY_AGG(:v_COL116."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL117:= ARRAY_AGG(:v_COL117."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL118:= ARRAY_AGG(:v_COL118."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL119:= ARRAY_AGG(:v_COL119."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL120:= ARRAY_AGG(:v_COL120."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL121:= ARRAY_AGG(:v_COL121."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL122:= ARRAY_AGG(:v_COL122."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL123:= ARRAY_AGG(:v_COL123."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL124:= ARRAY_AGG(:v_COL124."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL125:= ARRAY_AGG(:v_COL125."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL126:= ARRAY_AGG(:v_COL126."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL127:= ARRAY_AGG(:v_COL127."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL128:= ARRAY_AGG(:v_COL128."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL129:= ARRAY_AGG(:v_COL129."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL130:= ARRAY_AGG(:v_COL130."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL131:= ARRAY_AGG(:v_COL131."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL132:= ARRAY_AGG(:v_COL132."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL133:= ARRAY_AGG(:v_COL133."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL134:= ARRAY_AGG(:v_COL134."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL135:= ARRAY_AGG(:v_COL135."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL136:= ARRAY_AGG(:v_COL136."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS
		v_ARR_COL137:= ARRAY_AGG(:v_COL137."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL138:= ARRAY_AGG(:v_COL138."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL139:= ARRAY_AGG(:v_COL139."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL140:= ARRAY_AGG(:v_COL140."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL141:= ARRAY_AGG(:v_COL141."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL142:= ARRAY_AGG(:v_COL142."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL143:= ARRAY_AGG(:v_COL143."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS 2
		v_ARR_COL144:= ARRAY_AGG(:v_COL144."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL145:= ARRAY_AGG(:v_COL145."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL146:= ARRAY_AGG(:v_COL146."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL147:= ARRAY_AGG(:v_COL147."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL148:= ARRAY_AGG(:v_COL148."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS 3
		v_ARR_COL149:= ARRAY_AGG(:v_COL149."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL150:= ARRAY_AGG(:v_COL150."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS 4
		v_ARR_COL151:= ARRAY_AGG(:v_COL151."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL152:= ARRAY_AGG(:v_COL152."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS 5
		v_ARR_COL153:= ARRAY_AGG(:v_COL153."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL154:= ARRAY_AGG(:v_COL154."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS 6
		v_ARR_COL155:= ARRAY_AGG(:v_COL155."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL156:= ARRAY_AGG(:v_COL156."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		
		--NUEVOS 7, VERSION PERU
		v_ARR_COL157:= ARRAY_AGG(:v_COL157."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL158:= ARRAY_AGG(:v_COL158."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL159:= ARRAY_AGG(:v_COL159."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL160:= ARRAY_AGG(:v_COL160."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL161:= ARRAY_AGG(:v_COL161."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL162:= ARRAY_AGG(:v_COL162."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL163:= ARRAY_AGG(:v_COL163."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL164:= ARRAY_AGG(:v_COL164."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL165:= ARRAY_AGG(:v_COL165."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL166:= ARRAY_AGG(:v_COL166."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL167:= ARRAY_AGG(:v_COL167."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL168:= ARRAY_AGG(:v_COL168."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		v_ARR_COL169:= ARRAY_AGG(:v_COL169."Campo" ORDER BY "IdPlantillaMovimientoCustodia");
		

		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Arreglos','Fin de cargas de arreglos'
		FROM DUMMY;
		
		rst = UNNEST(:v_ARR_ID_PLANTILLA,
					 --------------------------------------------------------------------------------------------------------------
					 :v_ARR_COL1,:v_ARR_COL2,:v_ARR_COL3,:v_ARR_COL4,:v_ARR_COL5,:v_ARR_COL6,:v_ARR_COL7,:v_ARR_COL8,:v_ARR_COL9,:v_ARR_COL10,
					 :v_ARR_COL11,:v_ARR_COL12,:v_ARR_COL13,:v_ARR_COL14,:v_ARR_COL15,:v_ARR_COL16,:v_ARR_COL17,:v_ARR_COL18,:v_ARR_COL19,:v_ARR_COL20,
					 :v_ARR_COL21,:v_ARR_COL22,:v_ARR_COL23,:v_ARR_COL24,:v_ARR_COL25,:v_ARR_COL26,:v_ARR_COL27,:v_ARR_COL28,:v_ARR_COL29,:v_ARR_COL30,
					 :v_ARR_COL31,:v_ARR_COL32,:v_ARR_COL33,:v_ARR_COL34,:v_ARR_COL35,:v_ARR_COL36,:v_ARR_COL37,:v_ARR_COL38,:v_ARR_COL39,:v_ARR_COL40,
					 :v_ARR_COL41,:v_ARR_COL42,:v_ARR_COL43,:v_ARR_COL44,:v_ARR_COL45,:v_ARR_COL46,:v_ARR_COL47,:v_ARR_COL48,:v_ARR_COL49,:v_ARR_COL50,
					 :v_ARR_COL51,:v_ARR_COL52,:v_ARR_COL53,:v_ARR_COL54,:v_ARR_COL55,:v_ARR_COL56,:v_ARR_COL57,:v_ARR_COL58,:v_ARR_COL59,:v_ARR_COL60,
					 :v_ARR_COL61,:v_ARR_COL62,:v_ARR_COL63,:v_ARR_COL64,:v_ARR_COL65,:v_ARR_COL66,:v_ARR_COL67,:v_ARR_COL68,:v_ARR_COL69,:v_ARR_COL70,
					 :v_ARR_COL71,:v_ARR_COL72,:v_ARR_COL73,:v_ARR_COL74,:v_ARR_COL75,:v_ARR_COL76,:v_ARR_COL77,:v_ARR_COL78,:v_ARR_COL79,:v_ARR_COL80,
					 :v_ARR_COL81,:v_ARR_COL82,:v_ARR_COL83,:v_ARR_COL84,:v_ARR_COL85,:v_ARR_COL86,:v_ARR_COL87,:v_ARR_COL88,:v_ARR_COL89,:v_ARR_COL90,
					 :v_ARR_COL91,:v_ARR_COL92,:v_ARR_COL93,:v_ARR_COL94,:v_ARR_COL95,:v_ARR_COL96,:v_ARR_COL97,:v_ARR_COL99,:v_ARR_COL100,
					 :v_ARR_COL101,:v_ARR_COL102,:v_ARR_COL103,:v_ARR_COL104,:v_ARR_COL105,:v_ARR_COL106,:v_ARR_COL107,:v_ARR_COL108,:v_ARR_COL109,:v_ARR_COL110,
					 :v_ARR_COL111,:v_ARR_COL112,:v_ARR_COL113,:v_ARR_COL114,:v_ARR_COL115,:v_ARR_COL116,:v_ARR_COL117,:v_ARR_COL118,:v_ARR_COL119,:v_ARR_COL120,
					 :v_ARR_COL121,:v_ARR_COL122,:v_ARR_COL123,:v_ARR_COL124,:v_ARR_COL125,:v_ARR_COL126,:v_ARR_COL127,:v_ARR_COL128,:v_ARR_COL129,:v_ARR_COL130,
					 :v_ARR_COL131,:v_ARR_COL132,:v_ARR_COL133,:v_ARR_COL134,:v_ARR_COL135,:v_ARR_COL136,:v_ARR_COL137,:v_ARR_COL138,:v_ARR_COL139,:v_ARR_COL140,
					 :v_ARR_COL141,:v_ARR_COL142,:v_ARR_COL143,:v_ARR_COL144,:v_ARR_COL145,:v_ARR_COL146,:v_ARR_COL147,:v_ARR_COL148,:v_ARR_COL149,:v_ARR_COL150,
					 :v_ARR_COL151,:v_ARR_COL152,:v_ARR_COL153,:v_ARR_COL154,:v_ARR_COL155,:v_ARR_COL156,:v_ARR_COL157,:v_ARR_COL158,:v_ARR_COL159,:v_ARR_COL160,
					 :v_ARR_COL161,:v_ARR_COL162,:v_ARR_COL163,:v_ARR_COL164,:v_ARR_COL165,:v_ARR_COL166,:v_ARR_COL167,:v_ARR_COL168,:v_ARR_COL169)
					 AS(
					 "IdPlantillaMovimientoCustodia",
					 --------------------------------------------------------------------------------------------------------------
					 "FolioComprobanteBusqueda","FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia",
					 "IdOrigenGarantia","IdEspecieGarantia","IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion",
					 "EsNominalIngreso","IdClienteIngreso","IdPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro","IdTipoProducto",
					 "IdTipoCanalIngreso","IdMonedaSistema","CodigoIFRS","IdConcepto","IdTipoOperacion","IdGrupoOperacion","IdOperacion","FolioComprobante",
					 "FolioExternoReferencia","IdMesa","IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday",
					 "UsaGarantiaEspecifica","EsVentaCorta","CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraNormativa",
					 "FechaCurse", "TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste","IdClasificacionTipoInstrumento",
					 "IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","FechaEmisionInstrumento",
					 "FechaVencimientoInstrumento","EsFungible","IdBovedaEsIncluyente","IdBoveda","IdMonedaInstrumento","Tamano","Cantidad","CorteMinimo",
					 "ValorRescate","PrecioCompra","TasaCompra","MontoCompra","MontoCompraMonedaInstrumento","TasaAgente","PrecioAPlazo","MontoAPlazo",
					 "MontoAPlazoMonedaInstrumento","IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado",
					 "NemotecnicoRelacionado","IdEmisorRelacionado","NemoSVSRelacionado","IdTipoReajusteRelacionado","FechaEmisionInstrumentoRelacionado",
					 "FechaVencimientoInstrumentoRelacionado","EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado",
					 "IdMonedaInstrumentoRelacionado","TamanoRelacionado","CantidadInstrumentoRelacionado","CorteMinimoInstrumentoRelacionado",
					 "ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado",
					 "MontoCompraInstrumentoRelacionado","MontoCompraMonedaInstrumentoRelacionado","PrecioAPlazoInstrumentoRelacionado",
					 "MontoAPlazoInstrumentoRelacionado","MontoAPlazoMonedaInstrumentoRelacionado","MueveGarantiaAutomatica","FechaEfectiva","IdPlaza",
					 "IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria","ValorTipoCambioCierre",
					 "ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto","IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal",
					 "MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion","MontoLiquidacionMonedaPrincipal","MontoDiferido",
					 "MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo","MontoValorRazonablePasivo",
					 "MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio","MontoDevengoTipoCambioAnterior",
					 "MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos","TasaPuntosMonedaPrincipal",
					 "TasaPuntosMonedaSecundaria","MontoMatriz","RazonSocialContraparteGarantia","CuentaContraparteGarantia","CuentaBoveda",
					 "CuentaBovedaRelacionado","IdTipoComprobante","FechaComprobante","FolioOperacionRelacionada","IdTipoComprobanteBusqueda",
					 "TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo","TipoCambioObservado","FolioCustodiaBusqueda","TipoCarteraCorredor",
					 "EsPagaderoEn","EsPagaderoEnRelacionado","TablaAfectadaBusqueda","IdCustodiaBusqueda","INGCantidad","INGValorRescate",
					 "IdCuentaCustodioPortafolioIngreso","IdCuentaCustodioPortafolioRetiro","IdMonedaAdicional","FechaFactorMonedaAdicional",
					 "ValorFactorMonedaAdicional","FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","MontoCompraMonedaAdicional",
					 "MontoAPlazoMonedaAdicional","FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado",
					 "MontoCompraMonedaAdicionalRelacionado","MontoAPlazoMonedaAdicionalRelacionado");
	
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'UNNEST','Fin función UNNEST'
		FROM DUMMY;
		
		IF (:p_EsDemo = 'N') THEN
			UPDATE "BSCL.Custodia.BD::CUS.PlantillaMovimientoCustodia"
			SET
				"IdEstado" = 'INA',"IdUsuarioModificacionRegistro" = :p_IdUsuario,"FechaModificacionRegistro" = CURRENT_UTCTIMESTAMP
			WHERE "IdEstado" = 'ACT';
			
			INSERT INTO "BSCL.Custodia.BD::CUS.PlantillaMovimientoCustodia"(
				"IdPlantillaMovimientoCustodia","IdAccionOperacionLlave","IdTipoProductoLlave","IdTipoOperacionLlave","IdClasificacionTipoInstrumentoLlave",
				"IdTipoInstrumentoLlave","IdInstrumentoLlave","IdOrigenInstrumentoLlave","EsGarantiaLlave",
				--------------------------------------------------------------------------------------------------------------
				"TablaAfectadaBusqueda","IdCustodiaBusqueda","FolioCustodiaBusqueda","IdTipoComprobanteBusqueda","FolioComprobanteBusqueda",
				"FolioOperacionRelacionada",
				--GARANTIAS,
				"FolioCarteraGarantizada","IdInstrumentoCarteraGarantizada","EsEspecifica","IdContraparteGarantia","RazonSocialContraparteGarantia",
				"CuentaContraparteGarantia","IdOrigenGarantia","IdEspecieGarantia",
				--OPERACION
				"IdEmpresa","IdPais","FechaOperacion","FechaVencimientoOperacion","PlazoEnDiasOperacion","EsNominalIngreso","IdClienteIngreso","IdPortafolioIngreso",
				"IdCuentaCustodioPortafolioIngreso","EsNominalRetiro","IdClienteRetiro","IdPortafolioRetiro","IdCuentaCustodioPortafolioRetiro","IdTipoProducto",
				"IdTipoCanalIngreso","IdMonedaSistema","IdMonedaAdicional","FechaFactorMonedaAdicional","ValorFactorMonedaAdicional","CodigoIFRS","IdConcepto",
				"IdTipoOperacion","IdGrupoOperacion","IdOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante","FolioExternoReferencia","IdMesa",
				"IdBolsa","IdSucursal","IdAgente","IdCondicionLiquidacion","FechaLiquidacion","EsIntraday","UsaGarantiaEspecifica","EsVentaCorta",
				"CobraPrimaAlInicio","IdTipoPrestamo","Glosa","IdUsuario","IdModulo","TipoCarteraCorredor",
				--FUERON CREADOS PARA FONDOS MUTUOS
				"TipoCarteraNormativa","FechaCurse","TipoFactorAjuste","ValorFactorAjusteaFechaCurse","CostoEnFactorAjuste",
				--INSTRUMENTO
				"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","NemoSVS","IdTipoReajuste","EsPagaderoEn",
				"FechaEmisionInstrumento","FechaVencimientoInstrumento","EsFungible","IdMonedaInstrumento","Tamano","IdBoveda","CuentaBoveda","IdBovedaEsIncluyente",
				--CANTIDADES Y MONTOS
				"FechaFactorMonedaInstrumento","ValorFactorMonedaInstrumento","Cantidad","CorteMinimo","ValorRescate","PrecioCompra","TasaCompra","MontoCompra",
				"MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional","TasaAgente","PrecioAPlazo","MontoAPlazo","MontoAPlazoMonedaInstrumento",
				"MontoAPlazoMonedaAdicional",
				--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
				"INGCantidad","INGValorRescate",
			 	--INSTRUMENTO RELACIONADO
				"IdClasificacionTipoInstrumentoRelacionado","IdTipoInstrumentoRelacionado","IdInstrumentoRelacionado","NemotecnicoRelacionado","IdEmisorRelacionado",
				"NemoSVSRelacionado","IdTipoReajusteRelacionado","EsPagaderoEnRelacionado","FechaEmisionInstrumentoRelacionado","FechaVencimientoInstrumentoRelacionado",
				"EsFungibleRelacionado","IdBovedaEsIncluyenteRelacionado","IdBovedaRelacionado","CuentaBovedaRelacionado","IdMonedaInstrumentoRelacionado",
				"TamanoRelacionado",
				--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
				"FechaFactorMonedaInstrumentoRelacionado","ValorFactorMonedaInstrumentoRelacionado","CantidadInstrumentoRelacionado","CorteMinimoInstrumentoRelacionado",
				"ValorRescateInstrumentoRelacionado","PrecioCompraInstrumentoRelacionado","TasaCompraInstrumentoRelacionado","MontoCompraInstrumentoRelacionado",
				"MontoCompraMonedaInstrumentoRelacionado","MontoCompraMonedaAdicionalRelacionado","PrecioAPlazoInstrumentoRelacionado","MontoAPlazoInstrumentoRelacionado",
				"MontoAPlazoMonedaInstrumentoRelacionado","MontoAPlazoMonedaAdicionalRelacionado","MueveGarantiaAutomatica",
				--RF Y FORWARD
				"FechaEfectiva","IdPlaza","IdFixing","FechaFixing","HoraFixing","IdReferenciaMercado","IdMonedaPrincipal","IdMonedaSecundaria",
				"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto",
				--FORWARD
				"IdSpotAsociado","IdModalidadCumplimiento","MontoMonedaPrincipal","MontoMonedaSecundaria","IdMonedaLiquidacion","MontoLiquidacion",
				"MontoLiquidacionMonedaPrincipal","MontoDiferido","MontoUtilidadDiferida","MontoPerdidaDiferida","MontoMargen","MontoValorRazonableActivo",
				"MontoValorRazonablePasivo","MontoValorRazonableNeto","TasaValorRazonableActivo","TasaValorRazonablePasivo","MontoDevengoTipoCambio",
				"MontoDevengoTipoCambioAnterior","MontoDiferidoDiario","MontoDiferidoAcumulado","ValorTipoCambioSpot","ValorTipoCambioCotizador","EsPuntos",
				"TasaPuntosMonedaPrincipal","TasaPuntosMonedaSecundaria","MontoMatriz","TasaDescuentoAnticipo","TipoCambioAnticipo","MontoARecibirAnticipo",
				"TipoCambioObservado",
				-----------------
				"IdEstado","IdUsuarioModificacionRegistro","FechaModificacionRegistro")
			SELECT 
				"BSCL.Custodia.BD.Secuencias::IdPlantillaMovimientoCustodia".NEXTVAL,p."IdAccionOperacionLlave",p."IdTipoProductoLlave",p."IdTipoOperacionLlave",
				p."IdClasificacionTipoInstrumentoLlave",p."IdTipoInstrumentoLlave",p."IdInstrumentoLlave",p."IdOrigenInstrumentoLlave",p."EsGarantiaLlave",
				-----------------------------------------------------------------------------------------------------------------
				r."TablaAfectadaBusqueda",r."IdCustodiaBusqueda",r."FolioCustodiaBusqueda",r."IdTipoComprobanteBusqueda",r."FolioComprobanteBusqueda",
				r."FolioOperacionRelacionada",
				--GARANTIAS,
				r."FolioCarteraGarantizada",r."IdInstrumentoCarteraGarantizada",r."EsEspecifica",r."IdContraparteGarantia",r."RazonSocialContraparteGarantia",
				r."CuentaContraparteGarantia",r."IdOrigenGarantia",r."IdEspecieGarantia",
				--OPERACION
				r."IdEmpresa",r."IdPais",r."FechaOperacion",r."FechaVencimientoOperacion",r."PlazoEnDiasOperacion",	r."EsNominalIngreso",r."IdClienteIngreso",
				r."IdPortafolioIngreso",r."IdCuentaCustodioPortafolioIngreso",r."EsNominalRetiro",r."IdClienteRetiro",r."IdPortafolioRetiro",
				r."IdCuentaCustodioPortafolioRetiro",r."IdTipoProducto",r."IdTipoCanalIngreso",r."IdMonedaSistema",r."IdMonedaAdicional",
				r."FechaFactorMonedaAdicional",	r."ValorFactorMonedaAdicional",r."CodigoIFRS",r."IdConcepto",r."IdTipoOperacion",r."IdGrupoOperacion",
				r."IdOperacion",r."IdTipoComprobante",r."FolioComprobante",r."FechaComprobante",r."FolioExternoReferencia",r."IdMesa",r."IdBolsa",r."IdSucursal",
				r."IdAgente",r."IdCondicionLiquidacion",r."FechaLiquidacion",r."EsIntraday",r."UsaGarantiaEspecifica",r."EsVentaCorta",r."CobraPrimaAlInicio",
				r."IdTipoPrestamo",r."Glosa",r."IdUsuario",r."IdModulo",r."TipoCarteraCorredor",
				--FUERON CREADOS PARA FONDOS MUTUOS
				r."TipoCarteraNormativa",r."FechaCurse",r."TipoFactorAjuste",r."ValorFactorAjusteaFechaCurse",r."CostoEnFactorAjuste",
				--INSTRUMENTO
				r."IdClasificacionTipoInstrumento",r."IdTipoInstrumento",r."IdInstrumento",r."Nemotecnico",r."IdEmisor",r."NemoSVS",r."IdTipoReajuste",
				r."EsPagaderoEn",r."FechaEmisionInstrumento",r."FechaVencimientoInstrumento",r."EsFungible",r."IdMonedaInstrumento",r."Tamano",r."IdBoveda",
				r."CuentaBoveda",r."IdBovedaEsIncluyente",
				--CANTIDADES Y MONTOS
				r."FechaFactorMonedaInstrumento",r."ValorFactorMonedaInstrumento",r."Cantidad",r."CorteMinimo",r."ValorRescate",r."PrecioCompra",r."TasaCompra",
				r."MontoCompra",r."MontoCompraMonedaInstrumento",r."MontoCompraMonedaAdicional",r."TasaAgente",r."PrecioAPlazo",r."MontoAPlazo",
				r."MontoAPlazoMonedaInstrumento",r."MontoAPlazoMonedaAdicional",
			 	--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
				r."INGCantidad",r."INGValorRescate",
			 	--INSTRUMENTO RELACIONADO
				r."IdClasificacionTipoInstrumentoRelacionado",r."IdTipoInstrumentoRelacionado",r."IdInstrumentoRelacionado",r."NemotecnicoRelacionado",
				r."IdEmisorRelacionado",r."NemoSVSRelacionado",r."IdTipoReajusteRelacionado",r."EsPagaderoEnRelacionado",r."FechaEmisionInstrumentoRelacionado",
				r."FechaVencimientoInstrumentoRelacionado",r."EsFungibleRelacionado",r."IdBovedaEsIncluyenteRelacionado",r."IdBovedaRelacionado",
				r."CuentaBovedaRelacionado",r."IdMonedaInstrumentoRelacionado",r."TamanoRelacionado",
				--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
				r."FechaFactorMonedaInstrumentoRelacionado",r."ValorFactorMonedaInstrumentoRelacionado",r."CantidadInstrumentoRelacionado",
				r."CorteMinimoInstrumentoRelacionado",r."ValorRescateInstrumentoRelacionado",r."PrecioCompraInstrumentoRelacionado",
				r."TasaCompraInstrumentoRelacionado",r."MontoCompraInstrumentoRelacionado",r."MontoCompraMonedaInstrumentoRelacionado",
				r."MontoCompraMonedaAdicionalRelacionado",r."PrecioAPlazoInstrumentoRelacionado",r."MontoAPlazoInstrumentoRelacionado",
				r."MontoAPlazoMonedaInstrumentoRelacionado",r."MontoAPlazoMonedaAdicionalRelacionado",r."MueveGarantiaAutomatica",
				--RF Y FORWARD
				r."FechaEfectiva",r."IdPlaza",r."IdFixing",r."FechaFixing",r."HoraFixing",r."IdReferenciaMercado",r."IdMonedaPrincipal",r."IdMonedaSecundaria",
				r."ValorTipoCambioCierre",r."ValorParidadCierre",r."ValorTipoCambioCosto",r."ValorParidadCosto",
				--FORWARD
				r."IdSpotAsociado",r."IdModalidadCumplimiento",r."MontoMonedaPrincipal",r."MontoMonedaSecundaria",r."IdMonedaLiquidacion",r."MontoLiquidacion",
				r."MontoLiquidacionMonedaPrincipal",r."MontoDiferido",r."MontoUtilidadDiferida",r."MontoPerdidaDiferida",r."MontoMargen",
				r."MontoValorRazonableActivo",r."MontoValorRazonablePasivo",r."MontoValorRazonableNeto",r."TasaValorRazonableActivo",r."TasaValorRazonablePasivo",
				r."MontoDevengoTipoCambio",r."MontoDevengoTipoCambioAnterior",r."MontoDiferidoDiario",r."MontoDiferidoAcumulado",r."ValorTipoCambioSpot",
				r."ValorTipoCambioCotizador",r."EsPuntos",r."TasaPuntosMonedaPrincipal",r."TasaPuntosMonedaSecundaria",r."MontoMatriz",r."TasaDescuentoAnticipo",
				r."TipoCambioAnticipo",r."MontoARecibirAnticipo",r."TipoCambioObservado",
				------------
				'ACT',:p_IdUsuario,CURRENT_UTCTIMESTAMP
			FROM :rst AS r
			INNER JOIN :v_Plantilla AS p ON (p."IdPlantillaMovimientoCustodia" = r."IdPlantillaMovimientoCustodia")
			ORDER BY r."IdPlantillaMovimientoCustodia";
		ELSE
			SELECT 'v_PlantillasConProblemas',* FROM :v_PlantillasConProblemas;
			SELECT 'v_Plantilla',* FROM :v_Plantilla;
			SELECT 'v_CamposPorAccion',* FROM :v_CamposPorAccion;
			SELECT 'v_DatosCampos',* FROM :v_DatosCampos;
			SELECT 
				r."IdPlantillaMovimientoCustodia",p."IdAccionOperacionLlave",p."IdTipoProductoLlave",p."IdTipoOperacionLlave",
				p."IdClasificacionTipoInstrumentoLlave",p."IdTipoInstrumentoLlave",p."IdInstrumentoLlave",p."IdOrigenInstrumentoLlave",p."EsGarantiaLlave",
				-----------------------------------------------------------------------------------------------------------------
				r."TablaAfectadaBusqueda",r."IdCustodiaBusqueda",r."FolioCustodiaBusqueda",r."IdTipoComprobanteBusqueda",r."FolioComprobanteBusqueda",
				r."FolioOperacionRelacionada",
				--GARANTIAS,
				r."FolioCarteraGarantizada",r."IdInstrumentoCarteraGarantizada",r."EsEspecifica",r."IdContraparteGarantia",r."RazonSocialContraparteGarantia",
				r."CuentaContraparteGarantia",r."IdOrigenGarantia",r."IdEspecieGarantia",
				--OPERACION
				r."IdEmpresa",r."IdPais",r."FechaOperacion",r."FechaVencimientoOperacion",r."PlazoEnDiasOperacion",	r."EsNominalIngreso",r."IdClienteIngreso",
				r."IdPortafolioIngreso",r."IdCuentaCustodioPortafolioIngreso",r."EsNominalRetiro",r."IdClienteRetiro",r."IdPortafolioRetiro",
				r."IdCuentaCustodioPortafolioRetiro",r."IdTipoProducto",r."IdTipoCanalIngreso",r."IdMonedaSistema",r."IdMonedaAdicional",
				r."FechaFactorMonedaAdicional",r."ValorFactorMonedaAdicional",r."CodigoIFRS",r."IdConcepto",r."IdTipoOperacion",r."IdGrupoOperacion",
				r."IdOperacion",r."IdTipoComprobante",r."FolioComprobante",r."FechaComprobante",r."FolioExternoReferencia",r."IdMesa",r."IdBolsa",r."IdSucursal",
				r."IdAgente",r."IdCondicionLiquidacion",r."FechaLiquidacion",r."EsIntraday",r."UsaGarantiaEspecifica",r."EsVentaCorta",r."CobraPrimaAlInicio",
				r."IdTipoPrestamo",r."Glosa",r."IdUsuario",r."IdModulo",r."TipoCarteraCorredor",
				--FUERON CREADOS PARA FONDOS MUTUOS
				r."TipoCarteraNormativa",r."FechaCurse",r."TipoFactorAjuste",r."ValorFactorAjusteaFechaCurse",r."CostoEnFactorAjuste",
				--INSTRUMENTO
				r."IdClasificacionTipoInstrumento",r."IdTipoInstrumento",r."IdInstrumento",r."Nemotecnico",r."IdEmisor",r."NemoSVS",r."IdTipoReajuste",
				r."EsPagaderoEn",r."FechaEmisionInstrumento",r."FechaVencimientoInstrumento",r."EsFungible",r."IdMonedaInstrumento",r."Tamano",r."IdBoveda",
				r."CuentaBoveda",r."IdBovedaEsIncluyente",
				--CANTIDADES Y MONTOS
				r."FechaFactorMonedaInstrumento",r."ValorFactorMonedaInstrumento",r."Cantidad",r."CorteMinimo",r."ValorRescate",r."PrecioCompra",r."TasaCompra",
				r."MontoCompra",r."MontoCompraMonedaInstrumento",r."MontoCompraMonedaAdicional",r."TasaAgente",r."PrecioAPlazo",r."MontoAPlazo",
				r."MontoAPlazoMonedaInstrumento",r."MontoAPlazoMonedaAdicional",
			 	--PARA ANTICIPOS,VENCIMIENTOS,ANULACIONES
				r."INGCantidad",r."INGValorRescate",
			 	--INSTRUMENTO RELACIONADO
				r."IdClasificacionTipoInstrumentoRelacionado",r."IdTipoInstrumentoRelacionado",r."IdInstrumentoRelacionado",r."NemotecnicoRelacionado",
				r."IdEmisorRelacionado",r."NemoSVSRelacionado",r."IdTipoReajusteRelacionado",r."EsPagaderoEnRelacionado",r."FechaEmisionInstrumentoRelacionado",
				r."FechaVencimientoInstrumentoRelacionado",r."EsFungibleRelacionado",r."IdBovedaEsIncluyenteRelacionado",r."IdBovedaRelacionado",
				r."CuentaBovedaRelacionado",r."IdMonedaInstrumentoRelacionado",r."TamanoRelacionado",
				--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
				r."FechaFactorMonedaInstrumentoRelacionado",r."ValorFactorMonedaInstrumentoRelacionado",r."CantidadInstrumentoRelacionado",
				r."CorteMinimoInstrumentoRelacionado",r."ValorRescateInstrumentoRelacionado",r."PrecioCompraInstrumentoRelacionado",
				r."TasaCompraInstrumentoRelacionado",r."MontoCompraInstrumentoRelacionado",r."MontoCompraMonedaInstrumentoRelacionado",
				r."MontoCompraMonedaAdicionalRelacionado",r."PrecioAPlazoInstrumentoRelacionado",r."MontoAPlazoInstrumentoRelacionado",
				r."MontoAPlazoMonedaInstrumentoRelacionado",r."MontoAPlazoMonedaAdicionalRelacionado",r."MueveGarantiaAutomatica",
				--RF Y FORWARD
				r."FechaEfectiva",r."IdPlaza",r."IdFixing",r."FechaFixing",r."HoraFixing",r."IdReferenciaMercado",r."IdMonedaPrincipal",r."IdMonedaSecundaria",
				r."ValorTipoCambioCierre",r."ValorParidadCierre",r."ValorTipoCambioCosto",r."ValorParidadCosto",
				--FORWARD
				r."IdSpotAsociado",r."IdModalidadCumplimiento",r."MontoMonedaPrincipal",r."MontoMonedaSecundaria",r."IdMonedaLiquidacion",r."MontoLiquidacion",
				r."MontoLiquidacionMonedaPrincipal",r."MontoDiferido",r."MontoUtilidadDiferida",r."MontoPerdidaDiferida",r."MontoMargen",
				r."MontoValorRazonableActivo",r."MontoValorRazonablePasivo",r."MontoValorRazonableNeto",r."TasaValorRazonableActivo",r."TasaValorRazonablePasivo",
				r."MontoDevengoTipoCambio",r."MontoDevengoTipoCambioAnterior",r."MontoDiferidoDiario",r."MontoDiferidoAcumulado",r."ValorTipoCambioSpot",
				r."ValorTipoCambioCotizador",r."EsPuntos",r."TasaPuntosMonedaPrincipal",r."TasaPuntosMonedaSecundaria",r."MontoMatriz",r."TasaDescuentoAnticipo",
				r."TipoCambioAnticipo",r."MontoARecibirAnticipo",r."TipoCambioObservado",
				------------
				'ACT',:p_IdUsuario,CURRENT_UTCTIMESTAMP
			FROM :rst AS r
			INNER JOIN :v_Plantilla AS p ON (p."IdPlantillaMovimientoCustodia" = r."IdPlantillaMovimientoCustodia")
			ORDER BY r."IdPlantillaMovimientoCustodia";
		END IF;
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
			"IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
		SELECT
			0,CURRENT_UTCTIMESTAMP,:p_IdUsuario,:v_IdServicioAgrupador,::CURRENT_OBJECT_NAME,::CURRENT_OBJECT_NAME,
			'Fin','Fin Utilitario'
		FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
	END;
END;