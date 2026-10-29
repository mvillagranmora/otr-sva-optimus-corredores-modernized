DROP PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::CargarMasivo";

CREATE PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::CargarMasivo"
 (
 IN  p_IdEmpresa			VARCHAR(40),
 IN  p_Modelo				VARCHAR(40), -- Modelo a Cargar. Ej: Movimientos Facturar
 IN  p_FechaCarga			VARCHAR(10),
 OUT p_out_IdCarga 			BIGINT,
 OUT p_out_FechaHoraCarga   TIMESTAMP, 
 OUT p_out_ErroresContador  BIGINT   
 ) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	---READS SQL DATA 
AS
BEGIN
	   DECLARE	v_out_refID BIGINT;
	   DECLARE  v_out_ErroresContador  BIGINT;
	   DECLARE  v_TotalCargados BIGINT;
	   DECLARE  v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	
	   DECLARE  v_out_AdvertenciasContador int;
	   DECLARE  v_out_AdvertenciasDetalle table ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520));

    p_out_ErroresContador :=0;
	
	IF p_Modelo = 'FACTURACION' then
		--- 1) Obtiene Fecha de Sistema
		call "BSCL"."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa, :p_out_FechaHoraCarga);
	   
   		--- 2) Secuencia de Cargas Masiva para Entidades Inscritas en Carga
        	-- Obtiene ID de Carga
       select "BSCL"."BSCL.Homologacion.BD.Secuencias::CargaMasiva".NEXTVAL into p_out_IdCarga from dummy;
       
   		--- 3) Ejecuta Carga de Movimietos Facturar  
       		--Inscribe Proceso al LOG
   	   		--call "BSCL"."BSCL.Plataforma.BD.Servicios::CrearLog" ('INICIO','PROCESOCARGA', 'FAC',1,p_IdEmpresa,'Inicio Carga Movimientos Comprobantes','SIS','','',0,0,0,'I',:p_out_IdCarga,:v_out_refID,:v_out_ErroresContador,:v_out_ErroresDetalle);

       		-- Invoca a Servicio Carga Movimientos Facturar sin comprobantes
			-- Cargar datos homologados al modelo Clientes, Tipos de Movimientos, 
			-- Estados: Ingresado (no facturado), Anulado, Facturandose()
			-- Agregar Opcion de: 
						-- Setear fecha fija para todos los movimientos cargados
						-- Setear Estado a Ingresado						
						-- Setear Todo a un Cliente
   	   		--CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarDatosMovimientosComprobantes" ( :p_out_IdCarga , :p_FechaCarga, :p_IdEmpresa, :v_TotalCargados, :v_out_ErroresContador, :v_out_ErroresDetalle);	
   	   		--CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarMovimientosFacturarRV"(:p_IdEmpresa,1,p_out_IdCarga,'HOM','R','0001-01-01','4000-12-31',v_out_AdvertenciasContador,v_out_AdvertenciasDetalle,v_out_ErroresContador,v_out_ErroresDetalle); 

   	   		---CARGA OFICIAL DEL MODELO ES CONTRA EMITIDO   	   		
   	   		---Emitido(Facturado) OJO-cargar modelo de comprobantes, 
   				-- Considerar Cargar por tipo de Comprobante

   	   		--Inscribe Proceso al LOG
   	   		--Call "BSCL"."BSCL.Plataforma.BD.Servicios::CrearLog" ('TERMINO','PROCESOCARGA', 'FAC',1,p_IdEmpresa,'Fin Carga Movimientos Comprobantes','SIS','','',0,0,0,'I',:p_out_IdCarga,:v_out_refID,:v_out_ErroresContador,:v_out_ErroresDetalle);
   	   
   	END IF;
   	IF p_Modelo = 'CLIENTE' then
   		--- 1) Obtiene Fecha de Sistema
		call "BSCL"."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa, :p_out_FechaHoraCarga);
	   
   		--- 2) Secuencia de Cargas Masiva para Entidades Inscritas en Carga
        	-- Obtiene ID de Carga
     	select "BSCL"."BSCL.Homologacion.BD.Secuencias::CargaMasiva".NEXTVAL into p_out_IdCarga from dummy;
       
   		--- 3) Ejecuta Carga de Movimietos Facturar  
       		--Inscribe Proceso al LOG
   	   		--call "BSCL"."BSCL.Plataforma.BD.Servicios::CrearLog" ('INICIO','PROCESOCARGA', 'CLI',1,p_IdEmpresa,'Inicio Carga Movimientos Comprobantes','SIS','','',0,0,0,'I',:p_out_IdCarga,:v_out_refID,:v_out_ErroresContador,:v_out_ErroresDetalle);

			-- Modalidad de Carga Cliente:
			-- Listar Eliminados SEBRA:  Listar Clientes NO Activos en SEBRA  y Activos HANA --> Bloquearlos HANA  
			-- Clientes Nuevos        :  Listar Clientes Activos SEBRA y no Activos en HANA  --> Crearlos HANA  
			-- Crear solo Personas que son clientes:
					--- Cargar Documentos Identidad (de 100 clientes p.ej)
					--- Cargar Personas             (de 100 clientes p.ej)
					--- Cargar Personas DoctoIdent. (de 100 clientes p.ej)
					--- Cargar Clientes				(de 100 clientes p.ej)
					--- Cargar Portafolios          (de 100 clientes p.ej, 150 Portafolios)
			--- Validar que se graben en el modelo completo los datos
			
   	   		--CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarDatosClientes" ( :p_out_IdCarga , :p_FechaCarga, :p_IdEmpresa, :v_TotalCargados, :v_out_ErroresContador, :v_out_ErroresDetalle);	
   	
   	   		--Inscribe Proceso al LOG
   	   		--Call "BSCL"."BSCL.Plataforma.BD.Servicios::CrearLog" ('TERMINO','PROCESOCARGA', 'CLI',1,p_IdEmpresa,'Fin Carga Movimientos Comprobantes','SIS','','',0,0,0,'I',:p_out_IdCarga,:v_out_refID,:v_out_ErroresContador,:v_out_ErroresDetalle);   	   
   	END IF;
   	
   	--- CREAR OPCION de LIMPIAR CARGAS ESPECIFICAS
END;

