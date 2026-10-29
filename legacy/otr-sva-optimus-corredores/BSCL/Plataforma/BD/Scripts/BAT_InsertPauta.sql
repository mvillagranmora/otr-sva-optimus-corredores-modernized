--****************************************************
-- Script para agendar vencimientos  
-- <JG>
--****************************************************
set schema bscl;

-- BORRAR LA BASURITA
--------------------------------------------------------
select * from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "EsPauta" = 'S';

call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VCTO_SIMU');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VALORIZARCUSTODIA');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_PRESTAMOS');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_PACTOS');


call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'CAMBIO_FECHA_SISTEMA');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'TIPIFICAR_INSTRUMENTOS');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VALORIZAR_CUSTODIA');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_PACTO');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_SIMULTANEA');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_PRESTAMO');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_RF');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'VENCIMIENTO_IF');
call "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas"(p_IdTipoPrograma => 'ABRIR_SISTEMA');

commit;

/*
IdTipoPrograma;Descripcion;MaximoIntentos;Modulo;NombreClasePrograma;TipoClasePrograma;Nodo;EsPauta
VALORIZAR_CUSTODIA;Valorización períodica de la custodia;1;PLA;cl.bcs.custodia.jobs.PautaValorizarCustodia;JAVA;;S
TIPIFICAR_INSTRUMENTOS;Tipificar instrumentos para valorización. evita llamada extra valorizar;1;PLA;cl.bcs.custodia.jobs.PautaTipificarInstrumentos;JAVA;;S
VENCIMIENTO_SIMULTANEA;Vencimiento de Simultaneas;1;PLA;cl.bcs.custodia.jobs.PautaVencimientoSIM;JAVA;;S
VENCIMIENTO_PRESTAMO;Vencimiento de Prestamos y Emprestitos;1;PLA;cl.bcs.custodia.jobs.PautaVencimientoPRT;JAVA;;S
VENCIMIENTO_RF;Vencimiento RF;1;PLA;cl.bcs.custodia.jobs.PautaVencimientoRF;JAVA;;S
VENCIMIENTO_PACTO;Vencimiento Pactos (reversas);1;PLA;cl.bcs.custodia.jobs.PautaVencimientoPAC;JAVA;;S
VENCIMIENTO_IF;Vencimiento IF;1;PLA;cl.bcs.custodia.jobs.PautaVencimientoIF;JAVA;;S
*/
commit;
set schema bscl;
-- CAMBIO DE FECHA DEL SISTEMA
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'CAMBIO_FECHA_SISTEMA',
	p_Descripcion			=> 'Cambio de fecha del sistema. Revisa cierre sistema',
	p_NombreClasePrograma	=> 'cl.bcs.plataforma.jobs.PautaCambioFechaSistema',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '11',
	p_Minuto				=> '53',
	p_DependenciaAnterior	=> 'CORE_CIERRE_SISTEMA',
	p_DependenciaProximo	=> 'TIPIFICAR_INSTRUMENTOS',
	p_Parametros			=> #param);
	commit;
select current_utctimestamp from dummy;

-- TIPIFICACIÓN DE INSTRUMENTOS PARA LA VALORIZACION
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('MaximoProcesar','100'); //subir a 2000
insert into #param values('ForzarProcesarTodos','N'); 
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'TIPIFICAR_INSTRUMENTOS',
	p_Descripcion			=> 'Tipificar instrumentos para valorización. evita llamada extra valorizar',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaTipificarInstrumentos',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '20',
	p_Minuto				=> '15',
	p_DependenciaAnterior	=> 'CAMBIO_FECHA_SISTEMA',
	p_DependenciaProximo	=> 'VALORIZAR_CUSTODIA',
	p_Parametros			=> #param);

-- VALORIZACION DE LA CUSTODIA
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('ValorizarCarteras','S');
insert into #param values('ValorizarGarantias','S');
insert into #param values('ValorizaATasaMercado','S');
insert into #param values('ValorizaTIF','S');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'VALORIZAR_CUSTODIA',
	p_Descripcion			=> 'Valorización períodica de la custodia',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaValorizarCustodia',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '21',
	p_Minuto				=> '00',
	p_DependenciaAnterior	=> 'TIPIFICAR_INSTRUMENTOS',
	p_DependenciaProximo	=> 'VENCIMIENTO_PACTO',
	p_Parametros			=> #param);
	
-- VENCIMIENTO PACTOS
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'VENCIMIENTO_PACTO',
	p_Descripcion			=> 'Vencimiento Pactos (reversas)',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaVencimientoPAC',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '01',
	p_Minuto				=> '00',
	p_DependenciaAnterior	=> 'VALORIZAR_CUSTODIA',
	p_DependenciaProximo	=> 'VENCIMIENTO_SIMULTANEA',
	p_Parametros			=> #param);	
		
-- VENCIMIENTO SIMULTANEAS
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');

call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'VENCIMIENTO_SIMULTANEA',
	p_Descripcion			=> 'Vencimiento de Simultaneas',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaVencimientoSIM',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '01',
	p_Minuto				=> '30',
	p_DependenciaAnterior	=> 'VENCIMIENTO_PACTO',
	p_DependenciaProximo	=> 'VENCIMIENTO_PRESTAMO',
	p_Parametros			=> #param);
	
-- VENCIMIENTO PRESTAMOS Y EMPRESTITOS
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');
	
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'VENCIMIENTO_PRESTAMO',
	p_Descripcion			=> 'Vencimiento de Prestamos y Emprestitos',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaVencimientoPRT',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '02',
	p_Minuto				=> '00',
	p_DependenciaAnterior	=> 'VENCIMIENTO_SIMULTANEA',
	p_DependenciaProximo	=> 'VENCIMIENTO_RF',
	p_Parametros			=> #param);
	
-- VENCIMIENTO INSTRUMENTOS RF
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');
	
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'VENCIMIENTO_RF',
	p_Descripcion			=> 'Vencimiento RF',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaVencimientoRF',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '02',
	p_Minuto				=> '30',
	p_DependenciaAnterior	=> 'VENCIMIENTO_PRESTAMO',
	p_DependenciaProximo	=> 'VENCIMIENTO_IF',
	p_Parametros			=> #param);	

-- VENCIMIENTO INSTRUMENTOS IF
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('SimularOperacion','N');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');
	
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'VENCIMIENTO_IF',
	p_Descripcion			=> 'Vencimiento IF',
	p_NombreClasePrograma	=> 'cl.bcs.custodia.jobs.PautaVencimientoIF',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '03',
	p_Minuto				=> '00',
	p_DependenciaAnterior	=> 'VENCIMIENTO_RF',
	p_DependenciaProximo	=> 'ABRIR_SISTEMA',
	p_Parametros			=> #param);	
			
			
-- ABRIR SISTEMA
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','37');
insert into #param values('IdPais','44');
insert into #param values('IdPersonaNotificar','32878');
insert into #param values('EmailNotificar','jgarces@bolsadesantiago.com');
	
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'ABRIR_SISTEMA',
	p_Descripcion			=> 'Apertura automática del sistema, tras ejecución secuencial de la pauta.',
	p_NombreClasePrograma	=> 'cl.bcs.plataforma.jobs.PautaAbrirSistema',
	p_Nodo					=> '',
	p_IdModulo				=> 'PLA',
	p_Hora					=> '05',
	p_Minuto				=> '00',
	p_DependenciaAnterior	=> 'VENCIMIENTO_IF',
	p_DependenciaProximo	=> '',
	p_Parametros			=> #param);	


-- FIN
commit;
--------------------------------------------------------


---  	*		NORMATIVA 			*
--------------------------------------------

-- LECTURA EJECUCION DE ORDENES - CIR1985/OFI253
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
			insert into #param values('IdEmpresa','1');
			insert into #param values('IdTipoTarea','PROCESOLECTURAEJECUCIONORDENES');
				
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'PROCESOLECTURAEJECUCIONORDENES',
	p_Descripcion			=> 'Proceso Lectura Ejecución de Ordenes - Cir1985',
	p_NombreClasePrograma	=> 'cl.bcs.normativa.jobs.BcsJobProcesarPautaServ',
	p_Nodo					=> '',
	p_IdModulo				=> 'NOR',
	p_Hora					=> '20',
	p_Minuto				=> '00',--'**',
	p_DependenciaAnterior	=> '',
	p_DependenciaProximo	=> 'NOTIFICACIONEJECUCIONORDENES_DIARIA',
	p_Parametros			=> #param);	
	

-- NOTIFICACION EJECUCION DE ORDENES (DIARIA) - CIR1985/OFI253
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
			insert into #param values('IdEmpresa','1');
			insert into #param values('IdTipoTarea','NOTIFICACIONEJECUCIONORDENES_DIARIA');
	
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'NOTIFICACIONEJECUCIONORDENES_DIARIA',
	p_Descripcion			=> 'Notificación (Diaria) Ejecución de Ordenes - Cir1985',
	p_NombreClasePrograma	=> 'cl.bcs.normativa.jobs.BcsJobProcesarPautaServ',
	p_Nodo					=> '',
	p_IdModulo				=> 'NOR',
	p_Hora					=> '20',
	p_Minuto				=> '10',--'**',
	p_DependenciaAnterior	=> 'PROCESOLECTURAEJECUCIONORDENES',
	p_DependenciaProximo	=> '',
	p_Parametros			=> #param);	


-- ESTADISTICA OPERACION CLIENTE - SOI
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
			insert into #param values('IdEmpresa','1');
			insert into #param values('IdTipoTarea','ESTADISTICAOPERACIONCLIENTE');
				
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'ESTADISTICAOPERACIONCLIENTE',
	p_Descripcion			=> 'Proceso estadistica de operaciones por Cliente - SOI',
	p_NombreClasePrograma	=> 'cl.bcs.normativa.jobs.BcsJobProcesarPautaServ',
	p_Nodo					=> '',
	p_IdModulo				=> 'NOR',
	p_Hora					=> '20',
	p_Minuto				=> '00',--'**',
	p_DependenciaAnterior	=> '',
	p_DependenciaProximo	=> 'PROCESOEVALUACIONMONTOSOI',
	p_Parametros			=> #param);	


-- PROCESO DE EVAUACION DEL MONTO OPERADO - SOI
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
			insert into #param values('IdEmpresa','1');
			insert into #param values('IdTipoTarea','PROCESOEVALUACIONMONTOSOI');
				
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'PROCESOEVALUACIONMONTOSOI',
	p_Descripcion			=> 'Proceso de Evaluacion Monto Operado - SOI',
	p_NombreClasePrograma	=> 'cl.bcs.normativa.jobs.BcsJobProcesarPautaServ',
	p_Nodo					=> '',
	p_IdModulo				=> 'NOR',
	p_Hora					=> '20',
	p_Minuto				=> '10',--'**',
	p_DependenciaAnterior	=> 'ESTADISTICAOPERACIONCLIENTE',
	p_DependenciaProximo	=> '',
	p_Parametros			=> #param);	




/*
-- PROCESO DE CONSOLIDACION DE CLIENTES - HISTORICO 
--------------------------------------------------------
drop table #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
insert into #param values('IdTipoTarea','CONSOLIDACLIENTEHISTORICO');
				
call "BSCL.Plataforma.BD.Utilitarios::UtilCrearPautas" (
	p_IdEmpresa				=> 1,
	p_IdTipoPrograma		=> 'CONSOLIDACLIENTEHISTORICO',
	p_Descripcion			=> 'Consolidacion info clientes para llenado de tabla historica',
	p_NombreClasePrograma	=> 'cl.bcs.normativa.jobs.BcsJobProcesarPautaServ',
	p_Nodo					=> '',
	p_IdModulo				=> 'NOR',
	p_Hora					=> '20', --ajustar a horario definitivo
	p_Minuto				=> '00',--'**',
	p_DependenciaAnterior	=> '',
	p_DependenciaProximo	=> '',
	p_Parametros			=> #param);	

*/
-- FIN NORMATIVA
--------------------------------------------------------



commit;
