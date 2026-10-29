SET SCHEMA BSCL;

---revision de procediminetos

CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarDatosPersona"
	 (
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     ,1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 ,88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 ,'ORD'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
     ,'N'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     ,'0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 ,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	 ,?--OUT p_out_ErroresContador BIGINT,
	 ,?--OUT p_out_ErroresDetalle  table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))	
	 ); 	 	 

-- cargamos cliente portafolio         
call "BSCL"."BSCL.Homologacion.BD.Servicios::CargarDatosClientesPortafolio"
	 (
	 -- Parametros entrada comunes en todos los servicios publicos
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     ,1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 ,88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 ,'CLI'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
     ,'N'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     ,'0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 ,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	 ,?--OUT p_out_ErroresContador BIGINT,
	 ,?--OUT p_out_ErroresDetalle  table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))	
);

-- cargamos cuenta de inversion
CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarCuentaInversion"
	(  
	--  Fecha Creacion : 
	 	1--IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	,1--IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	,88--IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	,'CTA'--IN  p_Modulo           			NVARCHAR(3),     -- modulo 
     	,'N'--IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	,'0-0'--IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	,'2015-09-01'--IN---              NVARCHAR(10),   --YYYY-MM-DD
		,'2015-12-31'--IN				NVARCHAR(10),   --YYYY-MM-DD
		,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalPortafoliosCuentaACargar" DECIMAL(26,8),"TotalPortafoliosConCuentaInversion" DECIMAL(26,8)),
		,?--OUT p_out_ErroresContador int,
		,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
    );

-- cargamos contrapartes
CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarContraparte"
	 (
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     ,1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 ,88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 ,'DOC'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
     ,'R'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     ,'0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 ,?
	 ,?--OUT p_out_ErroresContador BIGINT,
	 ,?--OUT p_out_ErroresDetalle  table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))	
	 );
  

-- actualizamos indicadores de segmento cliente y categoria de inversionista
call "BSCL"."BSCL.Homologacion.BD.Servicios::ActualizarIndicadoresCliente"
 (
 '0-0' --IN  p_RutCliente			VARCHAR(40),
 ,88 --IN p_IdCarga, 
 ,?--OUT p_out_ErroresContador int,
 ,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
 ); 

  

-- cargamos sucursales
call "BSCL.Homologacion.BD.Servicios::CargarSucursal" (1);

--cargamos empleados
-- en caso que no se hayan cargado todas las personas 
-- lo mas seguro que no se carguen los empleados ya que no existen las 
-- personas asociadas
       
call "BSCL.Homologacion.BD.Servicios::CargarEmpleadoAgente"(
	1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
    ,1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	,88--IN  p_IdCarga 		   BIGINT,          -- ID de la carga 
	,'EMP'--IN  p_Modulo           NVARCHAR(3),     -- modulo 
    ,'N'--IN  P_Modalidad        NVARCHAR(1),     -- Ingreso I incremental, 
	,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	,?--OUT p_out_ErroresContador int,
	,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
);

-- cargamos transaccion
-- se cargan masivas no estan asociadas a un cliente
CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarTransaccion"(
	-- Parametros entrada comunes en todos los servicios publicos
	1-- IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
    ,1-- IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	,88-- IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	,'ORD'-- IN  p_Modulo              NVARCHAR(3),     -- modulo 
    , 'I'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, R --REFRESCO
	, '0001-01-01'--IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	, '4000-01-01'--IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
    ,'2015-05-17 17:00:00'--IN p_fechaCambio NVARCHAR(20),--'YYYY-MM-DD HH:MM:SS'
    ,?---OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	,?--OUT p_out_ErroresContador int,
	,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
);

/*
 CALL "BSCL.Homologacion.BD.Servicios::CargarOrdenesYAsignacion"(
	-- Parametros entrada comunes en todos los servicios publicos
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
    , 1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	, 88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	, 'ORD'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
    , 'I'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
    , '0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	, '0001-01-01'---N  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	, '4000-01-01'--IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	,?--OUT p_out_ErroresContador int,
	,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
); 
*/

CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarOrdenes"(
	-- Parametros entrada comunes en todos los servicios publicos
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
    , 1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	, 88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	, 'ORD'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
    , 'I'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
    , '0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	, '0001-01-01'---N  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	, '4000-01-01'--IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
    , '2015-05-17 17:00:00'--IN p_fechaCambio NVARCHAR(20),--'YYYY-MM-DD HH:MM:SS'
	,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	,?--OUT p_out_ErroresContador int,
	,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 

CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarAsignacion"(
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
    , 1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	, 88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	, 'ORD'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
    , 'I'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
    , '0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	, '0001-01-01'---N  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	, '4000-01-01'--IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
    , '2015-05-17 17:00:00'--IN p_fechaCambio NVARCHAR(20),--'YYYY-MM-DD HH:MM:SS'
	,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	,?--OUT p_out_ErroresContador int,
	,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 


CALL "BSCL.Homologacion.BD.Servicios::CargarMovimientosFacturarRV"(
	 1--IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     ,1--IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 ,88--IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 ,'ORD'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
     ,'I'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     ,'0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 ,'0001-01-01'--'IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 ,'4000-01-01'--IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	 ,?--OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	 ,?--OUT p_out_ErroresContador int,
	 ,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
); 

rut de prueba '76116575-5'

CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarMovimientoCargoAbonoInicial"
	(  
	 	1--IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	,1---IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	,88--IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	,'CTA'--IN  p_IdModulo         			NVARCHAR(3),     -- modulo 
     	,'I'--IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	,'0-0'--IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	,'2015-09-01'--IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
		,'2015-12-31'--IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
		,?--OUT p_out_ErroresContador 		int,
		,?---OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
		,?--OUT p_ResumenDatosProcesados  table ("CodigoProceso" NVARCHAR(40),"IdModulo" NVARCHAR(40),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)) 
    ); 


CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarMovimientosCuentaInversion"  
(
        -- Parametros entrada comunes en todos los servicios publicos
		1--IN  p_IdEmpresa		      BIGINT,			-- ID Empresa destino
	    ,1--IN  p_IdUsuario		      BIGINT,			-- ID del usuario que solicita el servicio
		,88--IN  p_IdCarga 			  BIGINT,          -- ID de la carga 
		,'ORD'--IN  p_Modulo              NVARCHAR(3),     -- modulo 
	    ,'I'--IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
	    ,'0-0'--IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
		,'2015-09-01'---IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	    ,'2015-09-30'--IN  p_FechaFinal		  NVARCHAR(10),   --YYYY-MM-DD
	    ,?---OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),	
		,?--OUT p_out_ErroresContador int,
		,?--OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
);
