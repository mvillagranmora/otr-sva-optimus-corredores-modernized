SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.VencimientoPactoPauta"; 

CREATE PROCEDURE
--ALTER PROCEDURE
-- 
--  Proposito		: Vencimiento simultaneas invocada desde la pauta.
--  Autor			: Jorge Garces <JG>
--  Fecha Creacion	: 13/12/2016
--
"BSCL.Plataforma.BD.Servicios::PAU.VencimientoPactoPauta"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa					bigint,
    IN p_IdUsuario					bigint,
    IN p_IdModulo					nvarchar(3),
    IN p_IdPais						bigint,
    IN p_Fecha						nvarchar(10) default '0001-01-01',
    IN p_IdPrograma					bigint,
	-- Parametros salida del servicio publico
	OUT p_out_ErroresContador		int,
	OUT p_out_ErroresDescripcion	nvarchar(100),
	OUT p_out_TicketProcesamiento	bigint
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	declare v_Fecha 			nvarchar(10):='0001-01-01';
	declare v_IdTipoPrograma	nvarchar(40):='';
	declare v_Ejecucion			nvarchar(40):='';
	
	DECLARE EXIT Handler FOR SQLEXCEPTION
	BEGIN	
		p_out_ErroresContador := 1;
		p_out_TicketProcesamiento:= -1;	
		insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
		:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'ERR','Vencimiento Pacto con error. || Ticket:'||cast(p_out_TicketProcesamiento as NVARCHAR) from dummy;				
		
		RESIGNAL;		
	END;
	
	BEGIN -- NEGOCIO

		declare v_out_ErroresContador 		bigint:=0;
		declare v_out_TicketProcesamiento 	bigint :=0; 
		
		declare v_vensim_out_TablaRefId table (
			"Modulo" 						nvarchar(3),
			"Accion" 						nvarchar(40),
			"RefId" 						bigint
		);
		declare v_vensim_out_AdvertenciasContador bigint:=0;
		declare v_vensim_out_AdvertenciasDetalle table (
			"IdGenerarMovimientoCustodia"	bigint,
			"Codigo"						nvarchar(100),
			"RequiereAutorizacion"			nvarchar(40),
			"Descripcion"					nvarchar(5000)
		);
		declare v_vensim_out_ErroresContador bigint:=0;
		
		declare v_vensim_out_ErroresDetalle table(
			"IdGenerarMovimientoCustodia"	bigint,
			"Codigo"						nvarchar(100),
			"Descripcion"					nvarchar(5000)
		);
		
		declare v_log_DetalleLog table (
			"SubRegistro" 		nvarchar(40), 
			"CampoRegistro" 	nvarchar(40), 
			"ValorAntiguo" 		nvarchar(250), 
			"ValorNuevo" 		nvarchar(250)
		);	
		
		declare v_CountControlPauta			bigint:=0;
		declare v_ControlPauta table (
			"IdControlPauta"         		BIGINT, 
		    "IdPrograma"          			BIGINT, 
		    "IdTipoPrograma"				NVARCHAR(40),
		    "Ejecucion"          			NVARCHAR(40), -- MANUAL, PROGRAMADA, REINTENTO 
			"FechaSistema" 					DATE,   
			"FechaCreacion" 				SECONDDATE,
			"IdEstado"						NVARCHAR(3), -- PEN, TER, ERR
			"Mensaje"						NVARCHAR(5000)
		);
			
		--datos IdTipoPrograma,Ejecucion
		v_ControlPauta = select 
			"IdControlPauta",
		    "IdPrograma",
		    "IdTipoPrograma",
		    "Ejecucion",
			"FechaSistema",
			"FechaCreacion",
			"IdEstado",
			"Mensaje" from "BSCL.Plataforma.BD::PAU.ControlPauta" where "IdPrograma" = :p_IdPrograma;
			
		if (IS_EMPTY(:v_ControlPauta)) THEN
			select top 1 "IdTipoPrograma",'PROGRAMADA' into v_IdTipoPrograma,v_Ejecucion  from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = :p_IdPrograma;
	    else
	    	select top 1 "IdTipoPrograma","Ejecucion" into v_IdTipoPrograma,v_Ejecucion  from :v_ControlPauta;
	    END IF;
		
		--fecha
		v_Fecha = :p_Fecha;
		if (v_Fecha = '0001-01-01') then
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa,v_Fecha);
		end if;
		
		-- loguear: inicializando variables para ejecucion.
		insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
		:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'PEN','Inicia proceso de vencimiento de pactos. Preparando variables de entrada' from dummy;

		v_Instrumentos = select 
			 null as "IdClasificacionTipoInstrumento"
			,null as "IdTipoInstrumento"
			,null as "IdInstrumento"
			,null as "IdEmisor"
			,null as "FechaVencimiento"
			,null as "IdTipoReajuste"
			,null as "EsFungible"
			,null as "IdBoveda" from dummy;
		
		v_Carteras = select
			 null as "FolioCartera"
			,null as "FolioGarantia"
			,null as "FolioSubyacente"
			,null as "IdCliente"
			,null as "IdPortafolio" from dummy;
		

		
		--call "BSCL.Plataforma.BD.Servicios::ConsultarPrerequisitoPauta";
		
		--lógica antigua tenía referencia a prerequisito
		v_log_DetalleLog = select
			null as "SubRegistro",
			null as "CampoRegistro",
			null as "ValorAntiguo", 
			null as "ValorNuevo"
		from dummy where 1=2;
		
		call "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			 p_IdTipoLog					=> 'INICIO',
			 p_IdModulo						=> 'PLA',
			 p_IdUsuario					=> :p_IdUsuario,
			 p_IdEmpresa					=> :p_IdEmpresa,
			 p_FechaSistemaLog				=> :v_Fecha,
			 p_IdUsuarioAtendido			=> :p_IdUsuario,
			 p_IdCliente					=> 0,
			 p_EstadoCliente				=> '',
			 p_NombreCliente				=> '',
			 p_IdTipoDocumentoIdentidad		=> '',
			 p_IdDocumentoIdentidad			=> '',
			 p_IdPortafolio 				=> 0,
			 p_EstadoPortafolio				=> '',
			 p_IdTipoRegistro				=> 'PAUTA',
			 p_IdRegistro					=> '',
			 p_EstadoRegistro				=> '',
			 p_IdTipoOperacion				=> '',
			 p_Folio						=> 0,
			 p_Cantidad						=> 0,
			 p_PrecioTasa					=> 0,
			 p_Monto						=> 0,
			 p_IdMonedaOrigen				=> '',
			 p_IdMonedaLiquidacion			=> '',
			 p_TipoCambio					=> '',
			 p_Nemo							=> '',
			 p_IdProceso					=> :p_IdPrograma,
			 p_EstadoProceso				=> 'VIG',
			 p_NombreProceso				=> 'VENCIMIENTO DE SIMULTANEAS',
			 p_IdTipoPrograma				=> 'VCTO_SIMU',
		 	 p_DetalleLog 					=> :v_log_DetalleLog
		 );
		 
		 insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
		:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'PEN','Se invoca SP VencimientoPactoPauta' from dummy;
		
		/*call "BSCL.Custodia.BD.Servicios::SIM.VencimientoSimultanea" (
			p_IdEmpresa              	=> :p_IdEmpresa, 
		    p_IdUsuario             	=> :p_IdUsuario, 
		    p_IdModulo             		=> 'CUS',       
			p_UsaReglasNegocio	 		=> 'N',
		    p_FechaSistema				=> :v_Fecha,
		    p_IdPais					=> :p_IdPais,
		    p_FolioCartera				=> 0,
		    p_out_TablaRefId			=> v_vensim_out_TablaRefId,
			p_out_AdvertenciasContador	=> v_vensim_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle	=> v_vensim_out_AdvertenciasDetalle,
			p_out_ErroresContador     	=> v_vensim_out_ErroresContador,
			p_out_ErroresDetalle      	=> v_vensim_out_ErroresDetalle
		);*/

		
		insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
		:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'PEN','Ejecucion SP VencimientoPactoPauta finalizada, evaluando errores' from dummy;
		
		p_out_ErroresContador 		:= v_out_ErroresContador;
		p_out_TicketProcesamiento 	:= v_out_TicketProcesamiento;
		p_out_ErroresDescripcion	:= '';
		
		-- con el ticket invocar un resumen de la operacion para loguear.

		-- loguear fin de la operacion
		if(v_vensim_out_ErroresContador=0 and v_vensim_out_AdvertenciasContador=0) then
			insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
			:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'TER','Vencimiento Pacto finalizada con éxito || Ticket:'||cast(p_out_TicketProcesamiento as NVARCHAR) from dummy;
		elseif(v_vensim_out_ErroresContador=0 and v_vensim_out_AdvertenciasContador>0) then
			insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
			:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'TER','Vencimiento Pacto con advertencias. || Ticket:'||cast(p_out_TicketProcesamiento as NVARCHAR) from dummy;
		elseif(v_vensim_out_ErroresContador>0) then
			insert into "BSCL.Plataforma.BD::PAU.ControlPauta" select "BSCL.Plataforma.BD.Secuencias::ControlPauta".nextval,
			:p_IdPrograma,:v_IdTipoPrograma,:v_Ejecucion,:v_Fecha,CURRENT_UTCTIMESTAMP,'ERR','Vencimiento Pacto con error. || Ticket:'||cast(p_out_TicketProcesamiento as NVARCHAR) from dummy;
		end if;
		
	END; -- NEGOCIO
END;
